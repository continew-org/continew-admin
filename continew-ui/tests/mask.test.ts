import { describe, expect, it } from 'vitest'

import { maskText, MASK_RULES } from '@/lib/mask'

describe('maskText', () => {
  it('masks a phone number keeping the prefix and suffix', () => {
    expect(maskText('13812345678', 'phone')).toBe('138****5678')
  })

  it('masks an id card keeping address code and tail', () => {
    expect(maskText('110101199003077777', 'idCard')).toBe('110101********7777')
  })

  it('masks an email keeping the domain', () => {
    expect(maskText('zhangsan@example.com', 'email')).toBe('z***n@example.com')
    expect(maskText('zhangsan@example.com', 'email')).toContain('@example.com')
  })

  it('masks a real name keeping the first character', () => {
    expect(maskText('张三', 'realName')).toBe('张*')
    expect(maskText('欧阳锋', 'realName')).toBe('欧*')
  })

  it('masks a bank card keeping only the tail', () => {
    expect(maskText('6222020200112233', 'bankCard')).toBe('**** **** 2233')
  })

  it('masks an ipv4 address keeping edge octets', () => {
    expect(maskText('192.168.1.100', 'ipv4')).toBe('192.***.***.100')
  })

  it('masks a password fully regardless of length', () => {
    expect(maskText('sk-abc123xyz', 'password')).toBe('********')
    expect(maskText('a', 'password')).toBe('********')
  })

  it('fully masks input that does not match the rule', () => {
    expect(maskText('1234', 'phone')).toBe('********')
    expect(maskText('not-an-email', 'email')).toBe('********')
  })

  it('supports a custom rule', () => {
    const custom = { pattern: /^(\d{4})\d+(\d{2})$/, mask: '$1...$2' }
    expect(maskText('62220202001122', custom)).toBe('6222...22')
  })

  it('does not leak raw input through a sticky or global custom rule', () => {
    expect(maskText('abcde', { pattern: /^\w+/y, mask: '***' })).toBe('***')
    const global = { pattern: /^(\d{3})\d+$/g, mask: '$1***' }
    expect(maskText('1234567', global)).toBe('123***')
    expect(maskText('1234567', global)).toBe('123***')
  })

  it('fully masks a bank card that is only its tail', () => {
    expect(maskText('1234', 'bankCard')).toBe('********')
  })

  it('keeps whole characters outside the BMP', () => {
    expect(maskText('𠮷野', 'realName')).toBe('𠮷*')
  })

  it('returns empty string for empty values', () => {
    expect(maskText('', 'phone')).toBe('')
  })

  it('exposes all builtin rules for reuse', () => {
    expect(Object.keys(MASK_RULES)).toEqual(
      expect.arrayContaining(['phone', 'idCard', 'email', 'realName', 'bankCard', 'ipv4', 'address', 'password'])
    )
  })
})
