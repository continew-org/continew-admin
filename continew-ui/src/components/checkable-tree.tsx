import * as React from 'react'

import { Search, Square, SquareCheck, X } from 'lucide-react'

import { cn } from 'cn'

import { TreeExpandActions } from '@/components/tree-expand-actions'
import { Button } from '@/components/ui/button'
import { InputGroup, InputGroupAddon, InputGroupInput } from '@/components/ui/input-group'
import { Tooltip, TooltipContent, TooltipTrigger } from '@/components/ui/tooltip'
import { buildTreeIndex, Tree, type TreeNodeData, TreeSkeleton } from '@/components/ui/tree'
import { useTreeExpansion } from '@/hooks/use-tree-expansion'

export type CheckableTreeNode<T = Record<string, unknown>> = TreeNodeData<T>

export interface CheckableTreeProps<T = Record<string, unknown>> {
  readonly data: CheckableTreeNode<T>[]
  readonly checkedKeys: Array<string | number>
  readonly onCheckedChange: (keys: Array<string | number>) => void
  readonly checkStrictly?: boolean
  readonly loading?: boolean
  readonly showSearch?: boolean
  readonly searchPlaceholder?: string
  readonly showExpandActions?: boolean
  readonly showSelectActions?: boolean
  readonly showIcons?: boolean
  /** Fills the remaining height of a flex parent instead of the fixed `max-h-72` tree box. */
  readonly fillHeight?: boolean
  readonly className?: string
  readonly treeContainerClassName?: string
  readonly emptyText?: string
}

export function CheckableTree<T = Record<string, unknown>>({
  data,
  checkedKeys,
  onCheckedChange,
  checkStrictly = false,
  loading = false,
  showSearch = true,
  searchPlaceholder = '搜索节点',
  showExpandActions = true,
  showSelectActions = true,
  showIcons = false,
  fillHeight = false,
  className,
  treeContainerClassName,
  emptyText = '暂无匹配节点'
}: CheckableTreeProps<T>) {
  const [search, setSearch] = React.useState('')

  const index = React.useMemo(() => buildTreeIndex(data), [data])
  const { expandedKeys, setExpandedKeys, expandAll, collapseAll } = useTreeExpansion({
    keys: index.allKeys,
    autoExpandOnLoad: true
  })
  // The props stay `string | number` for the callers' form types; Tree keys are strings.
  const checkedKeyStrings = React.useMemo(() => checkedKeys.map(String), [checkedKeys])

  /** Disabled nodes are locked, so the batch actions below leave them untouched. */
  const enabledKeys = React.useMemo(
    () => index.allKeys.filter(key => !index.nodeMap.get(key)?.disabled),
    [index]
  )

  const handleSelectAll = () => {
    const next = new Set(checkedKeyStrings)
    for (const key of enabledKeys) {
      next.add(key)
    }
    onCheckedChange(Array.from(next))
  }

  const handleClearAll = () => {
    const next = new Set(checkedKeyStrings)
    for (const key of enabledKeys) {
      next.delete(key)
    }
    onCheckedChange(Array.from(next))
  }

  const isSearching = search.trim().length > 0

  if (loading) {
    return <TreeSkeleton rows={7} className={cn(fillHeight && 'min-h-0 flex-1', className)} />
  }

  return (
    <div className={cn('flex flex-col gap-2.5', fillHeight && 'min-h-0 flex-1', className)}>
      {/* Header action bar with pure icon buttons matching Dept tree */}
      <div className="flex shrink-0 items-center justify-between gap-2">
        {showSearch && (
          <div className="min-w-0 flex-1">
            <InputGroup className="h-8">
              <InputGroupAddon align="inline-start">
                <Search className="size-3.5 text-muted-foreground" />
              </InputGroupAddon>
              <InputGroupInput
                placeholder={searchPlaceholder}
                value={search}
                onChange={e => setSearch(e.target.value)}
                className="h-8 text-xs"
              />
              {search && (
                <Button type="button" variant="ghost" size="icon-xs" className="mr-2" onClick={() => setSearch('')}>
                  <X />
                  <span className="sr-only">清空搜索</span>
                </Button>
              )}
            </InputGroup>
          </div>
        )}

        <div className="flex shrink-0 items-center gap-0.5">
          {showExpandActions && !isSearching && (
            <TreeExpandActions onExpandAll={expandAll} onCollapseAll={collapseAll} />
          )}

          {showSelectActions && (
            <>
              <Tooltip>
                <TooltipTrigger
                  render={
                    <Button type="button" variant="ghost" size="icon-xs" aria-label="全选" onClick={handleSelectAll} />
                  }
                >
                  <SquareCheck className="size-3.5" />
                </TooltipTrigger>
                <TooltipContent side="top">全选</TooltipContent>
              </Tooltip>

              <Tooltip>
                <TooltipTrigger
                  render={
                    <Button type="button" variant="ghost" size="icon-xs" aria-label="清空" onClick={handleClearAll} />
                  }
                >
                  <Square className="size-3.5" />
                </TooltipTrigger>
                <TooltipContent side="top">清空</TooltipContent>
              </Tooltip>
            </>
          )}
        </div>
      </div>

      {/* Nodes list container with smooth native scrolling */}
      <div
        className={cn(
          'overflow-auto overscroll-contain rounded-md border bg-background/50 p-1.5 select-none',
          treeContainerClassName || (fillHeight ? 'min-h-0 flex-1' : 'max-h-72 min-h-36')
        )}
      >
        <Tree
          data={data}
          index={index}
          checkable
          checkedKeys={checkedKeyStrings}
          onCheckedChange={onCheckedChange}
          checkStrictly={checkStrictly}
          expandedKeys={expandedKeys}
          onExpandedKeysChange={setExpandedKeys}
          searchKeyword={search}
          emptyText={emptyText}
          showIcons={showIcons}
        />
      </div>

      {/* Selected summary */}
      <div className="flex shrink-0 items-center justify-between px-1 text-[11px] text-muted-foreground">
        <span>已勾选 {checkedKeys.length} 项</span>
        <span>共 {index.allKeys.length} 个节点</span>
      </div>
    </div>
  )
}
