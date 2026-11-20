import * as React from 'react'
import { cn } from 'cn'
import { ChevronDown, Search, X } from 'lucide-react'

import { TreeExpandActions } from '@/components/tree-expand-actions'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover'
import { buildTreeIndex, Tree, TreeSkeleton, type TreeKey, type TreeNodeData } from '@/components/ui/tree'
import { useTreeExpansion } from '@/hooks/use-tree-expansion'

export interface TreeSelectProps<T = Record<string, unknown>> {
  data: TreeNodeData<T>[]
  value?: TreeKey | null
  onChange?: (value: TreeKey | null, node: TreeNodeData<T> | null) => void
  placeholder?: string
  disabled?: boolean
  invalid?: boolean
  loading?: boolean
  clearable?: boolean
  searchable?: boolean
  searchPlaceholder?: string
  showExpandActions?: boolean
  showIcons?: boolean
  showPathInTrigger?: boolean
  pathSeparator?: string
  className?: string
  popupClassName?: string
  emptyText?: React.ReactNode
  size?: 'default' | 'sm'
  triggerPrefix?: React.ReactNode
  showAllOption?: boolean
  allOptionLabel?: string
  placeholderColor?: 'muted' | 'foreground'
}

export function TreeSelect<T = Record<string, unknown>>({
  data,
  value,
  onChange,
  placeholder = '请选择',
  disabled = false,
  invalid = false,
  loading = false,
  clearable = true,
  searchable = true,
  searchPlaceholder = '搜索节点',
  showExpandActions = true,
  showIcons = true,
  showPathInTrigger = false,
  pathSeparator = ' / ',
  className,
  popupClassName,
  emptyText = '暂无匹配节点',
  size = 'default',
  triggerPrefix,
  showAllOption = false,
  allOptionLabel = '全部',
  placeholderColor = 'muted'
}: TreeSelectProps<T>) {
  const [open, setOpen] = React.useState(false)
  const [searchKeyword, setSearchKeyword] = React.useState('')

  // Build the index once and hand it to the inner Tree, so the same data is not traversed twice.
  // A single-select tree never cascades, so the descendant relations can be skipped.
  const index = React.useMemo(() => buildTreeIndex(data, { cascade: false }), [data])
  const { nodeMap, ancestorsMap, allKeys } = index
  const { expandedKeys, setExpandedKeys, expandAll, collapseAll } = useTreeExpansion({ keys: allKeys })

  const selectedNode = value !== undefined && value !== null ? nodeMap.get(value) : null
  const hasValue = Boolean(selectedNode)

  const displayText = React.useMemo(() => {
    if (!selectedNode || value === undefined || value === null) {
      return ''
    }

    const ancestors = ancestorsMap.get(value) ?? []
    const pathSegments = [...ancestors.map(k => nodeMap.get(k)?.label).filter(Boolean), selectedNode.label]
    const path = pathSegments.join(pathSeparator)

    return showPathInTrigger ? path : selectedNode.label
  }, [selectedNode, value, ancestorsMap, nodeMap, pathSeparator, showPathInTrigger])

  React.useEffect(() => {
    if (!open) {
      setSearchKeyword('')
    }
  }, [open])

  // The trigger is disabled, but the popup can already be open when the prop flips (e.g. a form
  // starts saving); close it so the still-mounted tree cannot change the value.
  React.useEffect(() => {
    if (disabled && open) {
      setOpen(false)
    }
  }, [disabled, open])

  const handleSelect = (key: TreeKey, node: TreeNodeData<T>) => {
    if (disabled) return
    onChange?.(key, node)
    setOpen(false)
  }

  const handleClear = (e: React.MouseEvent) => {
    e.stopPropagation()
    e.preventDefault()
    if (disabled) return
    onChange?.(null, null)
  }

  const handleExpandAll = (e: React.MouseEvent) => {
    e.stopPropagation()
    expandAll()
  }

  const handleCollapseAll = (e: React.MouseEvent) => {
    e.stopPropagation()
    collapseAll()
  }

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger
        render={
          <Button
            type="button"
            variant="outline"
            size={size}
            disabled={disabled}
            aria-invalid={invalid}
            className={cn(
              'w-full justify-between gap-1.5 pr-2 pl-2.5 text-left text-sm font-normal',
              size === 'sm' ? 'h-7 rounded-[min(var(--radius-md),10px)]' : 'h-8 rounded-lg',
              className
            )}
          />
        }
      >
        <div className="flex min-w-0 flex-1 items-center gap-1.5 overflow-hidden">
          {triggerPrefix && <span className="shrink-0 text-muted-foreground">{triggerPrefix}</span>}
          <span
            className={cn(
              'truncate',
              !hasValue && placeholderColor === 'muted' ? 'text-muted-foreground' : 'text-foreground'
            )}
          >
            {loading ? '加载中…' : hasValue ? displayText : placeholder}
          </span>
        </div>

        <div className="flex shrink-0 items-center gap-1 text-muted-foreground">
          {clearable && hasValue && !disabled && (
            <span
              className="flex size-4 items-center justify-center rounded-xs opacity-70 transition-opacity hover:opacity-100"
              onClick={handleClear}
              onPointerDown={e => e.stopPropagation()}
            >
              <X className="size-3" />
            </span>
          )}
          <ChevronDown
            className={cn('size-4 text-muted-foreground transition-transform duration-150', open && 'rotate-180')}
          />
        </div>
      </PopoverTrigger>

      <PopoverContent
        align="start"
        sideOffset={4}
        className={cn(
          'isolate z-50 flex flex-col gap-0 overflow-hidden p-0',
          'w-(--anchor-width) max-w-md min-w-64 rounded-lg shadow-md ring-1 ring-foreground/10',
          popupClassName
        )}
      >
        {(searchable || showExpandActions) && (
          <div className="flex items-center gap-1.5 border-b border-border/60 bg-muted/20 px-2 py-1.5">
            {searchable && (
              <div className="relative flex flex-1 items-center">
                <Search className="pointer-events-none absolute left-2 size-3.5 text-muted-foreground" />
                <Input
                  type="text"
                  value={searchKeyword}
                  onChange={e => setSearchKeyword(e.target.value)}
                  placeholder={searchPlaceholder}
                  className="h-7 rounded-md border-0 bg-transparent py-0 pr-7 pl-7 text-xs focus-visible:ring-0 md:text-xs dark:bg-transparent"
                />
                {searchKeyword && (
                  <Button
                    type="button"
                    variant="ghost"
                    size="icon-xs"
                    tabIndex={-1}
                    className="absolute right-1.5 size-4 text-muted-foreground hover:text-foreground"
                    onClick={() => setSearchKeyword('')}
                  >
                    <X className="size-3" />
                  </Button>
                )}
              </div>
            )}

            {showExpandActions && !searchKeyword && (
              <TreeExpandActions onExpandAll={handleExpandAll} onCollapseAll={handleCollapseAll} />
            )}
          </div>
        )}

        <div className="max-h-72 overflow-y-auto overscroll-contain p-1">
          {showAllOption && !searchKeyword && (
            <Button
              type="button"
              variant="ghost"
              onClick={() => {
                if (disabled) return
                onChange?.(null, null)
                setOpen(false)
              }}
              className={cn(
                'mb-1 flex h-auto w-full cursor-pointer items-center justify-start gap-2 rounded-md px-2 py-1.5 transition-colors outline-none',
                !hasValue
                  ? 'bg-accent font-medium text-accent-foreground hover:bg-accent dark:hover:bg-accent'
                  : 'text-foreground hover:bg-accent/60 hover:text-accent-foreground dark:hover:bg-accent/60'
              )}
            >
              <span>{allOptionLabel}</span>
            </Button>
          )}
          {loading ? (
            <TreeSkeleton rows={5} />
          ) : (
            <Tree
              data={data}
              index={index}
              selectedKey={value}
              onSelect={handleSelect}
              expandedKeys={expandedKeys}
              onExpandedKeysChange={setExpandedKeys}
              searchKeyword={searchKeyword}
              showIcons={showIcons}
              emptyText={emptyText}
            />
          )}
        </div>
      </PopoverContent>
    </Popover>
  )
}
