import { EMPTY_PLACEHOLDER } from '@/lib/constants'

/** `domain` holds a host without a protocol, so the link adds one when it opens. */
export function DomainCell({ domain }: { domain: string | null }) {
  if (!domain) {
    return <span>{EMPTY_PLACEHOLDER}</span>
  }

  const href = /^https?:\/\//i.test(domain) ? domain : `https://${domain}`

  return (
    <a href={href} target="_blank" rel="noreferrer" className="text-sm text-primary hover:underline">
      {domain}
    </a>
  )
}

/** An empty expiry means the tenant never expires. */
export function ExpireTimeCell({ value }: { value: string | null }) {
  return <span className={value ? 'text-sm' : 'text-sm text-muted-foreground'}>{value || '永不过期'}</span>
}
