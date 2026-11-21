/**
 * Masking rules for sensitive values shown in tables and drawers.
 *
 * Every rule is `{ pattern, mask }`: `pattern` captures the part that must stay
 * readable, `mask` is the replacement string where `$1`…`$n` keep captures.
 * A rule whose pattern does not match is skipped and the value is shown
 * fully masked instead of leaking through.
 */

export interface MaskRule {
  readonly pattern: RegExp
  readonly mask: string
}

export const MASK_RULES = {
  /** 13812345678 → 138****5678 */
  phone: { pattern: /^(\d{3})\d{4}(\d{4})$/, mask: '$1****$2' },
  /** 110101199003077777 → 110101********7777 */
  idCard: { pattern: /^(.{6}).{8}(.{4})$/, mask: '$1********$2' },
  /** zhangsan@example.com → z***n@example.com */
  email: { pattern: /^(.).*?(.@.*)$/u, mask: '$1***$2' },
  /** 张三 → 张*；欧阳锋 → 欧* */
  realName: { pattern: /^(.).+$/u, mask: '$1*' },
  /** 6222020200112233 → **** **** 2233；不足 5 位整体遮蔽 */
  bankCard: { pattern: /^.+(.{4})$/, mask: '**** **** $1' },
  /** 192.168.1.100 → 192.***.***.100 */
  ipv4: { pattern: /^(\d{1,3})\.\d{1,3}\.\d{1,3}\.(\d{1,3})$/, mask: '$1.***.***.$2' },
  /** 北京市海淀区中关村大街 27 号 → 北京市****** */
  address: { pattern: /^(.{3}).+$/u, mask: '$1******' },
  /** 密码、密钥、token 等：长度无关，全遮蔽 */
  password: { pattern: /^.*$/s, mask: '********' }
} as const satisfies Record<string, MaskRule>

export type MaskType = keyof typeof MASK_RULES

const FALLBACK_MASK = '********'

/**
 * Masks `value` with a named rule or a custom `{ pattern, mask }` rule.
 * Unmatched input is masked wholesale so a wrong rule never leaks raw data.
 */
export function maskText(value: string, type: MaskType | MaskRule): string {
  if (!value) {
    return ''
  }
  const { pattern, mask } = typeof type === 'string' ? MASK_RULES[type] : type
  // `test` advances `lastIndex` on global/sticky patterns, which would make `replace` miss
  // and hand back the raw value; reset it before each call.
  pattern.lastIndex = 0
  if (!pattern.test(value)) {
    return FALLBACK_MASK
  }
  pattern.lastIndex = 0
  return value.replace(pattern, mask)
}
