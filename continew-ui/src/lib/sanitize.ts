/**
 * Allowlist sanitizer for backend HTML (notice bodies) rendered via `dangerouslySetInnerHTML`.
 *
 * The schema mirrors the rich-text editor surface (StarterKit): block tags only, no inline styles,
 * no `on*` attributes. Attributes are restricted to a per-tag allowlist; URLs must be relative or
 * `http(s)`/`mailto:` so `javascript:`/`data:` payloads die with the attribute.
 *
 * Runs without a DOM parser so it works under `environment: 'node'` tests too.
 */

/** Tags the editor schema can emit. Anything else is unwrapped (children kept, tag dropped). */
const ALLOWED_TAGS: Record<string, true> = {
  p: true,
  h1: true,
  h2: true,
  h3: true,
  h4: true,
  h5: true,
  h6: true,
  ul: true,
  ol: true,
  li: true,
  blockquote: true,
  pre: true,
  code: true,
  hr: true,
  br: true,
  strong: true,
  b: true,
  em: true,
  i: true,
  s: true,
  u: true,
  a: true,
  img: true,
  table: true,
  thead: true,
  tbody: true,
  tr: true,
  th: true,
  td: true
}

const VOID_TAGS: Record<string, true> = { br: true, hr: true, img: true }
const URL_ATTRS: Record<string, true> = { href: true, src: true }

/** Tags dropped with their contents — their text children are code/markup, never copy. */
const DROP_CONTENTS: Record<string, true> = {
  script: true,
  style: true,
  iframe: true,
  object: true,
  embed: true,
  form: true,
  textarea: true,
  svg: true
}

const ALLOWED_ATTRS: Record<string, Record<string, true>> = {
  a: { href: true },
  img: { src: true, alt: true, title: true, width: true, height: true },
  td: { colspan: true, rowspan: true },
  th: { colspan: true, rowspan: true }
}

function isSafeUrl(value: string): boolean {
  const trimmed = value.trim()
  if (!trimmed) return false
  if (/^(https?):\/\//i.test(trimmed) || /^mailto:/i.test(trimmed)) return true
  // Relative paths/anchors; exclude protocol-relative `//host` — it inherits https fine but is
  // indistinguishable from an off-site asset, so it dies here too.
  return /^(#|\.?\.?\/)/.test(trimmed) || !/^[a-zA-Z][a-zA-Z0-9+.-]*:/.test(trimmed)
}

/** Escape `<`/`>` for text but leave `&` alone — source entities (`&amp;`, `&nbsp;`) must stay intact. */
function escapeText(value: string): string {
  return value.replace(/</g, '&lt;').replace(/>/g, '&gt;')
}

function escapeAttr(value: string): string {
  return value.replace(/&/g, '&amp;').replace(/"/g, '&quot;').replace(/</g, '&lt;')
}

function sanitizeAttrs(tag: string, attrsText: string): string {
  const allowed = ALLOWED_ATTRS[tag]
  if (!allowed || !attrsText.trim()) return ''

  const out: string[] = []
  // Match `name`, `name=value`, `name="value"`, `name='value'` attribute forms.
  const attrRe = /([a-zA-Z-]+)(?:\s*=\s*("[^"]*"|'[^']*'|[^\s"'=<>`]+))?/g
  let match: RegExpExecArray | null

  while ((match = attrRe.exec(attrsText)) !== null) {
    const name = match[1].toLowerCase()
    if (!allowed[name]) continue

    const raw = match[2]
    const value = raw === undefined ? '' : raw.replace(/^["']|["']$/g, '')

    if (URL_ATTRS[name]) {
      if (!isSafeUrl(value)) continue
      out.push(` ${name}="${escapeAttr(value)}"`)
      continue
    }

    // Numeric cell sizing only — an arbitrary string here is pointless.
    if ((name === 'width' || name === 'height' || name === 'colspan' || name === 'rowspan') && !/^\d+$/.test(value))
      continue

    out.push(` ${name}="${escapeAttr(value)}"`)
  }

  return out.join('')
}

/** Returns HTML containing only the editor-schema tags and safe attributes. */
export function sanitizeRichText(html: string): string {
  const tagRe = /<!--[\s\S]*?-->|<!\[CDATA\[[\s\S]*?\]\]>|<[^>]+>/g
  let out = ''
  let cursor = 0
  let dropping: { tag: string; depth: number } | null = null
  let match: RegExpExecArray | null

  while ((match = tagRe.exec(html)) !== null) {
    if (!dropping) out += escapeText(html.slice(cursor, match.index))
    cursor = match.index + match[0].length

    const raw = match[0]
    // Comments / CDATA / doctype carry no renderable content.
    if (!raw.startsWith('<') || raw.startsWith('<!') || raw.startsWith('<?')) continue

    const tagMatch = /^<\s*(\/?)\s*([a-zA-Z0-9-]+)([\s\S]*?)>$/.exec(raw)
    if (!tagMatch) {
      if (!dropping) out += escapeText(raw)
      continue
    }

    const closing = tagMatch[1] === '/'
    const tag = tagMatch[2].toLowerCase()
    const attrsText = tagMatch[3]

    if (dropping) {
      if (tag === dropping.tag) {
        dropping.depth += closing ? -1 : 1
        if (dropping.depth === 0) dropping = null
      }
      continue
    }

    if (DROP_CONTENTS[tag] && !closing) {
      dropping = { tag, depth: 1 }
      continue
    }

    if (!ALLOWED_TAGS[tag]) continue

    if (closing) {
      if (!VOID_TAGS[tag]) out += `</${tag}>`
      continue
    }

    out += `<${tag}${sanitizeAttrs(tag, attrsText)}>`
  }

  if (!dropping) out += escapeText(html.slice(cursor))
  return out
}
