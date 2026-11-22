import { describe, expect, it } from 'vitest'

import { sanitizeRichText } from '@/lib/sanitize'

describe('sanitizeRichText', () => {
  it('keeps the editor-schema markup untouched', () => {
    const html =
      '<h2>标题</h2><p><strong>加粗</strong> <em>斜体</em></p><ul><li>一</li><li>二</li></ul><blockquote><p>引用</p></blockquote>'
    expect(sanitizeRichText(html)).toBe(html)
  })

  it('drops script tags with their contents', () => {
    expect(sanitizeRichText('<p>ok</p><script>alert(1)</script><p>after</p>')).toBe('<p>ok</p><p>after</p>')
  })

  it('strips inline event handlers from images', () => {
    const out = sanitizeRichText('<p><img src="/a.png" alt="a" onerror="alert(1)"></p>')
    expect(out).toBe('<p><img src="/a.png" alt="a"></p>')
    expect(out).not.toContain('onerror')
  })

  it('rejects javascript: and data: URLs but keeps http links', () => {
    expect(sanitizeRichText('<a href="javascript:alert(1)">x</a>')).toBe('<a>x</a>')
    expect(sanitizeRichText('<img src="data:text/html;base64,AAAA"/>')).toBe('<img>')
    expect(sanitizeRichText('<a href="https://example.com/">x</a>')).toBe('<a href="https://example.com/">x</a>')
    expect(sanitizeRichText('<a href="/dashboard">x</a>')).toBe('<a href="/dashboard">x</a>')
  })

  it('unwraps disallowed tags but keeps their text', () => {
    expect(sanitizeRichText('<div><span>text</span></div>')).toBe('text')
  })

  it('escapes stray angle brackets in text', () => {
    expect(sanitizeRichText('a < b')).toBe('a &lt; b')
  })

  it('keeps entities intact instead of double-escaping', () => {
    expect(sanitizeRichText('<p>&nbsp;&amp;</p>')).toBe('<p>&nbsp;&amp;</p>')
  })

  it('drops style/iframe bodies and nested script bodies', () => {
    expect(sanitizeRichText('<style>body{display:none}</style><p>ok</p>')).toBe('<p>ok</p>')
    expect(sanitizeRichText('<iframe src="https://evil"><script>x()</script></iframe><p>ok</p>')).toBe('<p>ok</p>')
  })

  it('strips on* and style attributes from allowed tags', () => {
    const out = sanitizeRichText('<p onclick="alert(1)" style="color:red">x</p>')
    expect(out).toBe('<p>x</p>')
  })
})
