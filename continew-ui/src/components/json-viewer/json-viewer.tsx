import * as React from 'react'

import { cn } from 'cn'
import { Check, ChevronRight, Copy, CopyPlus, FoldHorizontal, Search, UnfoldHorizontal, X } from 'lucide-react'

import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'

import { jsonThemes, type JsonColorTheme, type ShikiThemeName } from './themes'

type JsonValue = string | number | boolean | null | JsonValue[] | { [key: string]: JsonValue }

const ThemeContext = React.createContext<JsonColorTheme | null>(null)

function useThemeColors(): JsonColorTheme | null {
  return React.use(ThemeContext)
}

function typeOf(value: JsonValue): string {
  if (value === null) return 'null'
  if (Array.isArray(value)) return 'array'
  return typeof value
}

function countEntries(value: JsonValue): number {
  if (Array.isArray(value)) return value.length
  if (value !== null && typeof value === 'object') return Object.keys(value).length
  return 0
}

function buildPath(parent: string, key: string | number): string {
  if (parent === '') return String(key)
  if (typeof key === 'number') return `${parent}[${key}]`
  if (/^[a-zA-Z_$][a-zA-Z0-9_$]*$/.test(key)) return `${parent}.${key}`
  return `${parent}["${key}"]`
}

function matchesSearch(key: string | number, value: JsonValue, query: string): boolean {
  const q = query.toLowerCase()
  if (String(key).toLowerCase().includes(q)) return true
  if (value === null) return 'null'.includes(q)
  if (typeof value !== 'object') return String(value).toLowerCase().includes(q)
  return false
}

function hasSearchMatch(value: JsonValue, key: string | number, query: string): boolean {
  if (!query) return false
  if (matchesSearch(key, value, query)) return true
  if (value !== null && typeof value === 'object') {
    const entries = Array.isArray(value) ? value.map((v, i) => [i, v] as const) : Object.entries(value)
    return entries.some(([k, v]) => hasSearchMatch(v, k, query))
  }
  return false
}

function TokenSpan({
  token,
  children,
  className,
  italic
}: {
  token: keyof JsonColorTheme
  children: React.ReactNode
  className?: string
  italic?: boolean
}) {
  const theme = useThemeColors()

  if (theme) {
    return (
      <span style={{ color: theme[token], fontStyle: italic ? 'italic' : undefined }} className={className}>
        {children}
      </span>
    )
  }

  const fallbackMap: Record<string, string> = {
    key: 'text-violet-600 dark:text-violet-400',
    string: 'text-emerald-600 dark:text-emerald-400',
    number: 'text-sky-600 dark:text-sky-400',
    boolean: 'text-amber-600 dark:text-amber-400',
    null: 'text-muted-foreground/60',
    punctuation: 'text-muted-foreground',
    fg: '',
    bg: ''
  }

  return <span className={cn(fallbackMap[token], italic && 'italic', className)}>{children}</span>
}

function HighlightMatch({ text, query }: { text: string; query: string }) {
  if (!query) return <>{text}</>

  const idx = text.toLowerCase().indexOf(query.toLowerCase())
  if (idx === -1) return <>{text}</>

  return (
    <>
      {text.slice(0, idx)}
      <mark className="rounded-sm bg-amber-200/60 px-0.5 text-inherit dark:bg-amber-500/30">
        {text.slice(idx, idx + query.length)}
      </mark>
      {text.slice(idx + query.length)}
    </>
  )
}

interface JsonNodeProps {
  keyName: string | number
  value: JsonValue
  path: string
  depth: number
  searchQuery: string
  collapsedPaths: Set<string>
  onToggle: (path: string) => void
  isLast: boolean
}

function JsonNode({ keyName, value, path, depth, searchQuery, collapsedPaths, onToggle, isLast }: JsonNodeProps) {
  const theme = useThemeColors()
  const type = typeOf(value)
  const isExpandable = type === 'object' || type === 'array'
  const count = isExpandable ? countEntries(value) : 0

  const isCollapsed = collapsedPaths.has(path)
  const isExpanded = isExpandable && !isCollapsed

  const openBracket = type === 'array' ? '[' : '{'
  const closeBracket = type === 'array' ? ']' : '}'
  const comma = isLast ? '' : ','

  const nodeMatches = searchQuery && matchesSearch(keyName, value, searchQuery)

  const handleToggle = React.useCallback(() => {
    if (isExpandable) onToggle(path)
  }, [isExpandable, onToggle, path])

  const [pathCopied, setPathCopied] = React.useState(false)
  const [valueCopied, setValueCopied] = React.useState(false)

  const handleCopyPath = React.useCallback(() => {
    navigator.clipboard.writeText(path).then(() => {
      setPathCopied(true)
      setTimeout(() => setPathCopied(false), 1500)
    })
  }, [path])

  const handleCopyValue = React.useCallback(() => {
    // A string copies as its raw text, which is what a header or body usually needs; every
    // other value copies as the JSON subtree it stands for.
    const text = typeof value === 'string' ? value : (JSON.stringify(value, null, 2) ?? '')
    navigator.clipboard.writeText(text).then(() => {
      setValueCopied(true)
      setTimeout(() => setValueCopied(false), 1500)
    })
  }, [value])

  const hoverBg = theme ? `${theme.fg}10` : undefined

  const rowClass = cn(
    'group flex items-start gap-0 py-px',
    !theme && 'hover:bg-muted/40',
    !theme && nodeMatches && 'bg-amber-100/40 dark:bg-amber-900/20'
  )

  const rowStyle: React.CSSProperties = {
    paddingLeft: `${depth * 20 + 8}px`,
    ...(theme && nodeMatches ? { backgroundColor: `${theme.fg}15` } : {})
  }

  const copyIconClass = cn(
    'ml-1 size-4 shrink-0 hover:bg-transparent',
    theme ? 'opacity-0 group-hover:opacity-60 hover:!opacity-100 focus-visible:opacity-100' : undefined,
    !theme && 'text-muted-foreground/0 group-hover:text-muted-foreground focus-visible:text-muted-foreground',
    !theme && 'hover:!text-foreground'
  )

  function renderKey() {
    return (
      <TokenSpan token="key">
        {typeof keyName === 'string' ? (
          <>
            &quot;
            <HighlightMatch text={keyName} query={searchQuery} />
            &quot;
          </>
        ) : (
          keyName
        )}
      </TokenSpan>
    )
  }

  function renderValue() {
    if (typeof value === 'string') {
      return (
        <TokenSpan token="string">
          &quot;
          <HighlightMatch text={value} query={searchQuery} />
          &quot;
        </TokenSpan>
      )
    }
    if (value === null) {
      return (
        <TokenSpan token="null" italic>
          {searchQuery ? <HighlightMatch text="null" query={searchQuery} /> : 'null'}
        </TokenSpan>
      )
    }
    if (typeof value === 'number') {
      return (
        <TokenSpan token="number">
          <HighlightMatch text={String(value)} query={searchQuery} />
        </TokenSpan>
      )
    }
    if (typeof value === 'boolean') {
      return (
        <TokenSpan token="boolean">
          <HighlightMatch text={String(value)} query={searchQuery} />
        </TokenSpan>
      )
    }
    return <span>{String(value)}</span>
  }

  if (!isExpandable) {
    return (
      <div
        className={rowClass}
        style={rowStyle}
        onMouseEnter={
          theme
            ? e => {
                e.currentTarget.style.backgroundColor = hoverBg ?? ''
              }
            : undefined
        }
        onMouseLeave={
          theme
            ? e => {
                e.currentTarget.style.backgroundColor = nodeMatches ? `${theme.fg}15` : ''
              }
            : undefined
        }
      >
        <span className="w-4 shrink-0" />
        <span className="min-w-0 text-xs wrap-break-word">
          {renderKey()}
          <TokenSpan token="punctuation">: </TokenSpan>
          {renderValue()}
          <TokenSpan token="punctuation">{comma}</TokenSpan>
        </span>
        <Button
          type="button"
          variant="ghost"
          size="icon-xs"
          onClick={handleCopyPath}
          aria-label={`Copy path: ${path}`}
          className={copyIconClass}
          style={theme ? { color: theme.fg } : undefined}
        >
          {pathCopied ? <CopyPlus className="size-3 text-emerald-500" /> : <CopyPlus className="size-3" />}
        </Button>
        <Button
          type="button"
          variant="ghost"
          size="icon-xs"
          onClick={handleCopyValue}
          aria-label={`Copy value: ${path}`}
          className={copyIconClass}
          style={theme ? { color: theme.fg } : undefined}
        >
          {valueCopied ? <Check className="size-3 text-emerald-500" /> : <Copy className="size-3" />}
        </Button>
      </div>
    )
  }

  const entries = Array.isArray(value)
    ? value.map((v, i) => [i, v] as [number, JsonValue])
    : (Object.entries(value as Record<string, JsonValue>) as [string, JsonValue][])

  const filteredEntries = searchQuery ? entries.filter(([k, v]) => hasSearchMatch(v, k, searchQuery)) : entries
  const showAll = !searchQuery
  const displayEntries = showAll ? entries : filteredEntries

  return (
    <div>
      <div
        className={rowClass}
        style={rowStyle}
        onMouseEnter={
          theme
            ? e => {
                e.currentTarget.style.backgroundColor = hoverBg ?? ''
              }
            : undefined
        }
        onMouseLeave={
          theme
            ? e => {
                e.currentTarget.style.backgroundColor = nodeMatches ? `${theme.fg}15` : ''
              }
            : undefined
        }
      >
        <Button
          type="button"
          variant="ghost"
          size="icon-xs"
          onClick={handleToggle}
          aria-label={isExpanded ? 'Collapse' : 'Expand'}
          className="size-4 shrink-0"
          style={theme ? { color: theme.punctuation } : undefined}
        >
          <ChevronRight
            className={cn('size-3 transition-transform', isExpanded && 'rotate-90', !theme && 'text-muted-foreground')}
          />
        </Button>
        <span className="min-w-0 text-xs wrap-break-word">
          {renderKey()}
          <TokenSpan token="punctuation">: </TokenSpan>
          <TokenSpan token="punctuation">{openBracket}</TokenSpan>
          {!isExpanded && (
            <>
              <span
                className={cn('mx-1 text-[10px]', !theme && 'text-muted-foreground/60')}
                style={theme ? { color: `${theme.fg}60` } : undefined}
              >
                {count} {count === 1 ? 'item' : 'items'}
              </span>
              <TokenSpan token="punctuation">
                {closeBracket}
                {comma}
              </TokenSpan>
            </>
          )}
        </span>
        <Button
          type="button"
          variant="ghost"
          size="icon-xs"
          onClick={handleCopyPath}
          aria-label={`Copy path: ${path}`}
          className={copyIconClass}
          style={theme ? { color: theme.fg } : undefined}
        >
          {pathCopied ? <CopyPlus className="size-3 text-emerald-500" /> : <CopyPlus className="size-3" />}
        </Button>
        <Button
          type="button"
          variant="ghost"
          size="icon-xs"
          onClick={handleCopyValue}
          aria-label={`Copy value: ${path}`}
          className={copyIconClass}
          style={theme ? { color: theme.fg } : undefined}
        >
          {valueCopied ? <Check className="size-3 text-emerald-500" /> : <Copy className="size-3" />}
        </Button>
      </div>

      {isExpanded && (
        <>
          {displayEntries.map(([k, v], i) => (
            <JsonNode
              key={`${k}-${i}`}
              keyName={k}
              value={v}
              path={buildPath(path, k)}
              depth={depth + 1}
              searchQuery={searchQuery}
              collapsedPaths={collapsedPaths}
              onToggle={onToggle}
              isLast={i === displayEntries.length - 1}
            />
          ))}
          <div
            className={cn('text-xs', !theme && 'text-muted-foreground')}
            style={{
              paddingLeft: `${depth * 20 + 8 + 16}px`,
              ...(theme ? { color: theme.punctuation } : {})
            }}
          >
            {closeBracket}
            {comma}
          </div>
        </>
      )}
    </div>
  )
}

function collectPaths(
  value: JsonValue,
  path: string,
  maxDepth: number | true,
  depth: number,
  result: Set<string>
): void {
  if (value === null || typeof value !== 'object') return
  if (maxDepth !== true && depth >= maxDepth) {
    result.add(path)
  }
  const entries = Array.isArray(value) ? value.map((v, i) => [i, v] as const) : Object.entries(value)
  for (const [k, v] of entries) {
    collectPaths(v, buildPath(path, k), maxDepth, depth + 1, result)
  }
}

function allExpandablePaths(value: JsonValue, rootName: string): Set<string> {
  const result = new Set<string>()
  collectAllExpandable(value, rootName, result)
  return result
}

function collectAllExpandable(value: JsonValue, path: string, result: Set<string>): void {
  if (value === null || typeof value !== 'object') return
  result.add(path)
  const entries = Array.isArray(value) ? value.map((v, i) => [i, v] as const) : Object.entries(value)
  for (const [k, v] of entries) {
    collectAllExpandable(v, buildPath(path, k), result)
  }
}

const SEARCH_INPUT_CLASS = cn(
  'h-6 min-w-0 flex-1 rounded-none border-0 bg-transparent px-0 text-xs',
  'shadow-none focus-visible:ring-0 md:text-xs dark:bg-transparent'
)

function resolveTheme(theme: ShikiThemeName | JsonColorTheme | undefined): JsonColorTheme | null {
  if (!theme) return null
  if (typeof theme === 'string') return jsonThemes[theme] ?? null
  return theme
}

interface JsonViewerProps extends Omit<React.ComponentProps<'div'>, 'children' | 'title'> {
  /** Any JSON-serializable value to display. */
  data: JsonValue
  /** Optional heading label. */
  title?: string
  /** Label for the root node. Defaults to "root". */
  rootName?: string
  /**
   * Depth to expand by default.
   * - Number: expand nodes up to this depth (default 1)
   * - `true`: expand all nodes
   */
  defaultExpanded?: number | true
  /**
   * Editor color theme. Pass a shiki theme name (e.g. "dracula", "github-dark")
   * or a custom JsonColorTheme object. When omitted, uses Tailwind theme colors.
   */
  colorTheme?: ShikiThemeName | JsonColorTheme
  /** Optional HTTP status code shown next to the node count. */
  statusCode?: number | null
}

function JsonViewer({
  data,
  title,
  rootName = 'root',
  defaultExpanded = 1,
  colorTheme,
  statusCode,
  className,
  ...props
}: JsonViewerProps) {
  const resolved = resolveTheme(colorTheme)

  const [collapsedPaths, setCollapsedPaths] = React.useState<Set<string>>(() => {
    if (defaultExpanded === true) return new Set()
    const collapsed = new Set<string>()
    collectPaths(data, rootName, defaultExpanded, 0, collapsed)
    return collapsed
  })
  const [searchQuery, setSearchQuery] = React.useState('')
  const [searchOpen, setSearchOpen] = React.useState(false)
  const [copiedAll, setCopiedAll] = React.useState(false)
  const searchRef = React.useRef<HTMLInputElement>(null)

  const togglePath = React.useCallback((path: string) => {
    setCollapsedPaths(prev => {
      const next = new Set(prev)
      if (next.has(path)) {
        next.delete(path)
      } else {
        next.add(path)
      }
      return next
    })
  }, [])

  const expandAll = React.useCallback(() => {
    setCollapsedPaths(new Set())
  }, [])

  const collapseAll = React.useCallback(() => {
    const all = allExpandablePaths(data, rootName)
    setCollapsedPaths(all)
  }, [data, rootName])

  const copyJson = React.useCallback(() => {
    navigator.clipboard.writeText(JSON.stringify(data, null, 2)).then(() => {
      setCopiedAll(true)
      setTimeout(() => setCopiedAll(false), 1500)
    })
  }, [data])

  const toggleSearch = React.useCallback(() => {
    setSearchOpen(prev => {
      if (!prev) {
        requestAnimationFrame(() => searchRef.current?.focus())
      } else {
        setSearchQuery('')
      }
      return !prev
    })
  }, [])

  React.useEffect(() => {
    if (searchQuery) {
      setCollapsedPaths(new Set())
    }
  }, [searchQuery])

  const isExpandable = data !== null && typeof data === 'object'
  const type = typeOf(data)

  return (
    <ThemeContext value={resolved}>
      <div
        data-slot="json-viewer"
        className={cn('overflow-hidden rounded-xl border border-border/60 bg-card', className)}
        {...props}
      >
        {/* Toolbar */}
        <div className="flex items-center justify-between gap-2 border-b border-border/40 px-3 py-2 sm:px-4">
          <div className="flex min-w-0 items-center gap-2">
            {title && <h3 className="text-sm font-semibold text-foreground">{title}</h3>}
            {isExpandable && (
              <Badge
                variant="secondary"
                className="h-auto rounded-full px-2 py-0.5 text-[10px] font-medium text-muted-foreground"
              >
                {countEntries(data)} {type === 'array' ? 'items' : 'keys'}
              </Badge>
            )}
            {statusCode !== null && statusCode !== undefined && (
              <Badge
                variant="secondary"
                className="h-auto rounded-full px-2 py-0.5 text-[10px] font-medium text-muted-foreground"
              >
                HTTP {statusCode}
              </Badge>
            )}
          </div>
          <div className="flex shrink-0 items-center gap-0.5">
            <Button
              type="button"
              variant="ghost"
              size="icon-xs"
              onClick={toggleSearch}
              aria-label={searchOpen ? 'Close search' : 'Search'}
              className={cn('text-muted-foreground', searchOpen && 'bg-muted text-foreground')}
            >
              <Search className="size-3.5" />
            </Button>
            <Button
              type="button"
              variant="ghost"
              size="icon-xs"
              onClick={expandAll}
              aria-label="Expand all"
              className="text-muted-foreground"
            >
              <UnfoldHorizontal className="size-3.5" />
            </Button>
            <Button
              type="button"
              variant="ghost"
              size="icon-xs"
              onClick={collapseAll}
              aria-label="Collapse all"
              className="text-muted-foreground"
            >
              <FoldHorizontal className="size-3.5" />
            </Button>
            <Button
              type="button"
              variant="ghost"
              size="icon-xs"
              onClick={copyJson}
              aria-label="Copy JSON"
              className="text-muted-foreground"
            >
              {copiedAll ? <Check className="size-3.5 text-emerald-500" /> : <Copy className="size-3.5" />}
            </Button>
          </div>
        </div>

        {/* Search bar */}
        {searchOpen && (
          <div className="flex items-center gap-2 border-b border-border/40 bg-muted/20 px-3 py-1.5 sm:px-4">
            <Search className="size-3.5 shrink-0 text-muted-foreground" />
            <Input
              ref={searchRef}
              value={searchQuery}
              onChange={e => setSearchQuery(e.target.value)}
              placeholder="筛选键或值"
              className={SEARCH_INPUT_CLASS}
            />
            {searchQuery && (
              <Button
                type="button"
                variant="ghost"
                size="icon-xs"
                onClick={() => setSearchQuery('')}
                aria-label="Clear search"
                className="text-muted-foreground"
              >
                <X className="size-3" />
              </Button>
            )}
          </div>
        )}

        {/* Tree */}
        <div
          className="overflow-auto py-1"
          style={
            resolved
              ? {
                  backgroundColor: resolved.bg,
                  color: resolved.fg
                }
              : undefined
          }
        >
          {isExpandable ? (
            <JsonNode
              keyName={rootName}
              value={data}
              path={rootName}
              depth={0}
              searchQuery={searchQuery}
              collapsedPaths={collapsedPaths}
              onToggle={togglePath}
              isLast
            />
          ) : (
            <div className="px-4 py-2 text-xs wrap-break-word">
              <TokenSpan token="key">{rootName}</TokenSpan>
              <TokenSpan token="punctuation">: </TokenSpan>
              {typeof data === 'string' ? (
                <TokenSpan token="string">&quot;{data}&quot;</TokenSpan>
              ) : typeof data === 'number' ? (
                <TokenSpan token="number">{String(data)}</TokenSpan>
              ) : typeof data === 'boolean' ? (
                <TokenSpan token="boolean">{String(data)}</TokenSpan>
              ) : (
                <TokenSpan token="null" italic>
                  null
                </TokenSpan>
              )}
            </div>
          )}
        </div>
      </div>
    </ThemeContext>
  )
}

export { JsonViewer, type JsonViewerProps, type JsonValue }
