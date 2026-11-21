import { createElement, useEffect, useMemo, useRef, useState } from 'react'

import { ScrollFade } from '@animate-ui/react/primitives/effects/scroll-fade'
import { useVirtualizer } from '@tanstack/react-virtual'
import { Check, Search, X, icons, type LucideIcon } from 'lucide-react'

import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger
} from '@/components/ui/dialog'
import { InputGroup, InputGroupAddon, InputGroupInput } from '@/components/ui/input-group'
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { resolveMenuIcon } from '@/navigation/menu-icons'
import { cn } from 'cn'

interface IconCategory {
  key: string
  label: string
  keywords: string[]
}

const ICON_CATEGORIES: IconCategory[] = [
  {
    key: 'common',
    label: '常用推荐',
    keywords: [
      'layout',
      'dashboard',
      'home',
      'house',
      'app',
      'grid',
      'menu',
      'sidebar',
      'settings',
      'config',
      'user',
      'users',
      'role',
      'shield',
      'lock',
      'key',
      'folder',
      'file',
      'table',
      'list',
      'check',
      'bell',
      'search',
      'chart',
      'database',
      'server',
      'monitor',
      'terminal',
      'link',
      'mail',
      'message',
      'calendar',
      'clock',
      'tag',
      'flag',
      'bookmark',
      'filter',
      'download',
      'upload',
      'tool',
      'wrench',
      'help',
      'info',
      'alert',
      'power',
      'log-in',
      'log-out'
    ]
  },
  {
    key: 'user',
    label: '用户权限',
    keywords: [
      'user',
      'users',
      'person',
      'people',
      'member',
      'account',
      'contact',
      'shield',
      'lock',
      'key',
      'badge',
      'award',
      'medal',
      'crown',
      'fingerprint',
      'id-card',
      'license',
      'passcode',
      'security',
      'protect',
      'safe',
      'auth'
    ]
  },
  {
    key: 'system',
    label: '系统设备',
    keywords: [
      'settings',
      'cog',
      'gear',
      'sliders',
      'toggle',
      'switch',
      'cpu',
      'memory',
      'hard-drive',
      'database',
      'server',
      'terminal',
      'code',
      'bug',
      'network',
      'wifi',
      'bluetooth',
      'power',
      'battery',
      'plug',
      'radio',
      'satellite',
      'monitor',
      'computer',
      'laptop',
      'tablet',
      'smartphone',
      'device',
      'printer',
      'scanner',
      'keyboard',
      'mouse',
      'watch',
      'bot',
      'circuit'
    ]
  },
  {
    key: 'layout',
    label: '布局导航',
    keywords: [
      'layout',
      'grid',
      'sidebar',
      'panel',
      'split',
      'columns',
      'rows',
      'table',
      'window',
      'frame',
      'app',
      'dock',
      'maximize',
      'minimize',
      'fullscreen',
      'navigation',
      'compass',
      'map',
      'pin',
      'locate',
      'route',
      'signpost',
      'arrow',
      'chevron',
      'corner',
      'move',
      'pointer',
      'mouse'
    ]
  },
  {
    key: 'files',
    label: '文档文件',
    keywords: [
      'file',
      'files',
      'folder',
      'folders',
      'archive',
      'clipboard',
      'book',
      'bookmark',
      'notebook',
      'newspaper',
      'receipt',
      'invoice',
      'paperclip',
      'draft',
      'page',
      'signature',
      'contract',
      'text',
      'doc'
    ]
  },
  {
    key: 'chart',
    label: '数据图表',
    keywords: [
      'chart',
      'graph',
      'trending',
      'gauge',
      'activity',
      'analytics',
      'bar',
      'pie',
      'line',
      'scatter',
      'radar',
      'histogram',
      'target',
      'percent',
      'calculator',
      'scale',
      'binary',
      'variable',
      'math',
      'sigma'
    ]
  },
  {
    key: 'commerce',
    label: '商务交易',
    keywords: [
      'dollar',
      'currency',
      'coin',
      'credit-card',
      'wallet',
      'banknote',
      'payment',
      'shopping',
      'cart',
      'bag',
      'store',
      'shop',
      'package',
      'box',
      'boxes',
      'truck',
      'container',
      'warehouse',
      'barcode',
      'qr-code',
      'tag',
      'tags',
      'ticket',
      'coupon',
      'gift',
      'trophy'
    ]
  },
  {
    key: 'communication',
    label: '消息通讯',
    keywords: [
      'mail',
      'email',
      'inbox',
      'send',
      'message',
      'chat',
      'speech',
      'comment',
      'bell',
      'notification',
      'phone',
      'call',
      'contact',
      'share',
      'at-sign',
      'rss',
      'podcast',
      'megaphone',
      'announcement',
      'reply',
      'forward'
    ]
  },
  {
    key: 'editor',
    label: '编辑设计',
    keywords: [
      'edit',
      'pen',
      'pencil',
      'brush',
      'palette',
      'paint',
      'eraser',
      'scissors',
      'crop',
      'layers',
      'blend',
      'sparkles',
      'wand',
      'ruler',
      'compass',
      'bold',
      'italic',
      'underline',
      'strikethrough',
      'align',
      'heading',
      'type',
      'font',
      'quote',
      'highlighter'
    ]
  },
  {
    key: 'media',
    label: '媒体影音',
    keywords: [
      'image',
      'picture',
      'photo',
      'camera',
      'video',
      'film',
      'movie',
      'tv',
      'music',
      'audio',
      'sound',
      'volume',
      'speaker',
      'headphones',
      'mic',
      'play',
      'pause',
      'stop',
      'skip',
      'rewind',
      'disc',
      'radio'
    ]
  }
]

function toKebabCase(string: string): string {
  return string
    .replace(/([a-z0-9])([A-Z])/g, '$1-$2')
    .replace(/([A-Z])([A-Z][a-z])/g, '$1-$2')
    .toLowerCase()
}

interface LucideIconMeta {
  pascal: string
  pascalLower: string
  kebab: string
  component: LucideIcon
  categories: Set<string>
}

/** Every Lucide icon tagged with the categories its kebab-case name matches. */
const ALL_ICONS: LucideIconMeta[] = Object.entries(icons).map(([pascal, component]) => {
  const kebab = toKebabCase(pascal)
  const matchedCategories = ICON_CATEGORIES.filter(category => category.keywords.some(key => kebab.includes(key)))

  return {
    pascal,
    pascalLower: pascal.toLowerCase(),
    kebab,
    component,
    categories: new Set(matchedCategories.map(category => category.key))
  }
})

const CATEGORY_ITEMS = [
  { value: 'all', label: `全部 (${ALL_ICONS.length})` },
  ...ICON_CATEGORIES.map(category => ({
    value: category.key,
    label: `${category.label} (${ALL_ICONS.filter(icon => icon.categories.has(category.key)).length})`
  }))
]

function getGridColumns() {
  if (window.matchMedia('(min-width: 1024px)').matches) return 10
  if (window.matchMedia('(min-width: 768px)').matches) return 8
  if (window.matchMedia('(min-width: 640px)').matches) return 6
  return 4
}

interface IconGridItemProps {
  readonly item: LucideIconMeta
  readonly selected: boolean
  readonly onSelect: (name: string) => void
}

function IconGridItem({ item, selected, onSelect }: IconGridItemProps) {
  const Icon = item.component

  return (
    <button
      type="button"
      onClick={() => onSelect(item.kebab)}
      className={cn(
        'group relative flex flex-col items-center justify-center gap-1.5 rounded-lg border p-2 text-xs transition-colors hover:border-foreground/30 hover:bg-muted/50',
        selected
          ? 'border-primary bg-primary/10 font-medium text-primary'
          : 'border-border/60 bg-background text-muted-foreground hover:text-foreground'
      )}
    >
      <Icon className="size-5 shrink-0 transition-transform group-hover:scale-110" />
      <span className="w-full truncate text-center text-[11px] leading-tight text-foreground/80">{item.kebab}</span>
      {selected && (
        <span className="absolute top-1 right-1 flex size-3 items-center justify-center rounded-full bg-primary text-primary-foreground">
          <Check className="size-2" />
        </span>
      )}
    </button>
  )
}

interface IconGridProps {
  readonly items: LucideIconMeta[]
  readonly value?: string | null
  readonly onSelect: (name: string) => void
}

function IconGrid({ items, value, onSelect }: IconGridProps) {
  const [columns, setColumns] = useState(getGridColumns)
  const scrollRef = useRef<HTMLDivElement>(null)
  const rowVirtualizer = useVirtualizer({
    count: Math.ceil(items.length / columns),
    getScrollElement: () => scrollRef.current,
    estimateSize: () => 64,
    overscan: 3
  })

  useEffect(() => {
    const updateColumns = () => setColumns(getGridColumns())
    window.addEventListener('resize', updateColumns)
    return () => window.removeEventListener('resize', updateColumns)
  }, [])

  useEffect(() => {
    scrollRef.current?.scrollTo({ top: 0 })
  }, [items])

  return (
    <ScrollFade ref={scrollRef} shrink={0} blur={4} className="max-h-84 overscroll-contain p-2.5">
      <div className="relative" style={{ height: rowVirtualizer.getTotalSize() }}>
        {rowVirtualizer.getVirtualItems().map(row => {
          const start = row.index * columns

          return (
            <div
              key={row.key}
              ref={rowVirtualizer.measureElement}
              data-index={row.index}
              className="absolute inset-x-0 top-0 grid gap-2 pb-2"
              style={{
                transform: `translateY(${row.start}px)`,
                gridTemplateColumns: `repeat(${columns}, minmax(0, 1fr))`
              }}
            >
              {items.slice(start, start + columns).map(item => (
                <IconGridItem
                  key={item.kebab}
                  item={item}
                  selected={value === item.kebab || value === item.pascal}
                  onSelect={onSelect}
                />
              ))}
            </div>
          )
        })}
      </div>
    </ScrollFade>
  )
}

interface IconPickerProps {
  readonly value?: string | null
  readonly onChange: (value: string | null) => void
  readonly disabled?: boolean
}

export function IconPicker({ value, onChange, disabled = false }: IconPickerProps) {
  const [open, setOpen] = useState(false)
  const [search, setSearch] = useState('')
  const [category, setCategory] = useState('common')

  /** Keep the identity stable, otherwise the grid resets its scroll position on unrelated renders. */
  const filteredIcons = useMemo(() => {
    const keyword = search.trim().toLowerCase()
    const byCategory = category === 'all' ? ALL_ICONS : ALL_ICONS.filter(icon => icon.categories.has(category))

    return keyword
      ? byCategory.filter(icon => icon.kebab.includes(keyword) || icon.pascalLower.includes(keyword))
      : byCategory
  }, [category, search])

  function selectIcon(kebab: string) {
    onChange(kebab)
    setOpen(false)
  }

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <div className="relative w-full">
        <DialogTrigger
          render={
            <Button
              type="button"
              variant="outline"
              disabled={disabled}
              className="h-8 w-full justify-start gap-2 pr-8 font-normal"
            />
          }
        >
          {value ? (
            <>
              {createElement(resolveMenuIcon(value), { className: 'size-4 shrink-0' })}
              <span className="truncate">{value}</span>
            </>
          ) : (
            <span className="truncate text-muted-foreground">点击选择图标</span>
          )}
        </DialogTrigger>
        {value && !disabled && (
          <Button
            type="button"
            variant="ghost"
            size="icon-xs"
            className="absolute top-1/2 right-1 -translate-y-1/2 text-muted-foreground hover:text-foreground"
            onClick={() => onChange(null)}
          >
            <X />
          </Button>
        )}
      </div>

      <DialogContent className="max-w-md sm:max-w-3xl">
        <DialogHeader>
          <DialogTitle>选择菜单图标</DialogTitle>
          <DialogDescription>项目内置完整的 Lucide 图标库</DialogDescription>
        </DialogHeader>

        <div className="space-y-3 pt-1">
          <div className="flex flex-col gap-2 sm:flex-row sm:items-center">
            <InputGroup className="flex-1">
              <InputGroupAddon>
                <Search className="size-4 text-muted-foreground" />
              </InputGroupAddon>
              <InputGroupInput
                placeholder="搜索图标名称"
                value={search}
                onChange={event => setSearch(event.target.value)}
                autoFocus
              />
              {search && (
                <InputGroupAddon align="inline-end">
                  <Button type="button" variant="ghost" size="icon-xs" onClick={() => setSearch('')}>
                    <X className="size-3.5" />
                  </Button>
                </InputGroupAddon>
              )}
            </InputGroup>

            <Select
              value={category}
              onValueChange={nextValue => {
                if (nextValue) setCategory(nextValue)
              }}
              items={CATEGORY_ITEMS}
            >
              <SelectTrigger>
                <SelectValue placeholder="选择分类" />
              </SelectTrigger>
              <SelectContent align="end">
                <SelectGroup>
                  {CATEGORY_ITEMS.map(item => (
                    <SelectItem key={item.value} value={item.value}>
                      {item.label}
                    </SelectItem>
                  ))}
                </SelectGroup>
              </SelectContent>
            </Select>
          </div>

          <div className="overflow-hidden rounded-lg border bg-muted/20">
            {filteredIcons.length === 0 ? (
              <div className="flex h-40 flex-col items-center justify-center gap-1.5 p-2.5 text-xs text-muted-foreground">
                <span>未找到匹配的图标</span>
                {category !== 'all' && (
                  <Button
                    type="button"
                    variant="link"
                    size="sm"
                    onClick={() => setCategory('all')}
                    className="h-auto p-0 text-xs text-primary"
                  >
                    切换至「全部」分类继续搜索
                  </Button>
                )}
              </div>
            ) : (
              <IconGrid items={filteredIcons} value={value} onSelect={selectIcon} />
            )}
          </div>
        </div>
      </DialogContent>
    </Dialog>
  )
}
