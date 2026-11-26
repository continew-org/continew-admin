import { describe, expect, it } from 'vitest'

import { safeLocalHref } from '@/lib/route-access'

const base = 'https://admin.example.com/dashboard/workplace'

describe('safeLocalHref', () => {
  it('keeps a same-origin path with its query and fragment', () => {
    expect(safeLocalHref('/system/user?tab=role#top', '/home', base)).toBe('/system/user?tab=role#top')
  })

  it('rejects a protocol-relative URL that starts with a slash', () => {
    expect(safeLocalHref('//evil.example/path', '/home', base)).toBe('/home')
  })

  it('rejects other non-local targets', () => {
    for (const target of [undefined, null, 1, 'https://evil.example', 'javascript:alert(1)', '', 'system/user']) {
      expect(safeLocalHref(target, '/home', base)).toBe('/home')
    }
  })
})
