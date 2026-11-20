import * as React from 'react'
import { cn } from 'cn'
import { Check, ChevronRight, Folder, FolderOpen } from 'lucide-react'

import { Button } from '@/components/ui/button'
import { Checkbox } from '@/components/ui/checkbox'
import { Skeleton } from '@/components/ui/skeleton'
import { toggleCheckedKeys } from '@/lib/tree-check'
import { buildTreeIndex, type TreeIndex, type TreeKey, type TreeNodeData } from '@/lib/tree-index'

export { buildTreeIndex } from '@/lib/tree-index'
export type { TreeIndex, TreeIndexOptions, TreeKey, TreeNodeData } from '@/lib/tree-index'

export interface TreeProps<T = Record<string, unknown>> {
  data: TreeNodeData<T>[]
  index?: TreeIndex<T>
  selectedKey?: TreeKey | null
  onSelect?: (key: TreeKey, node: TreeNodeData<T>) => void
  checkable?: boolean
  checkedKeys?: TreeKey[]
  onCheckedChange?: (keys: TreeKey[]) => void
  checkStrictly?: boolean
  expandedKeys?: TreeKey[]
  onExpandedKeysChange?: (keys: TreeKey[]) => void
  defaultExpandAll?: boolean
  searchKeyword?: string
  showIcons?: boolean
  emptyText?: React.ReactNode
  className?: string
}

export function HighlightText({ text, keyword }: { text?: string; keyword?: string }) {
  if (!text) return null
  if (!keyword || !keyword.trim()) {
    return <span className="truncate">{text}</span>
  }

  const trimmed = keyword.trim()
  const lowerText = text.toLowerCase()
  const lowerKeyword = trimmed.toLowerCase()
  const matchIndex = lowerText.indexOf(lowerKeyword)

  if (matchIndex === -1) {
    return <span className="truncate">{text}</span>
  }

  const before = text.slice(0, matchIndex)
  const matched = text.slice(matchIndex, matchIndex + trimmed.length)
  const after = text.slice(matchIndex + trimmed.length)

  return (
    <span className="truncate">
      {before}
      <mark className="rounded-xs bg-primary/20 px-0.5 font-medium text-primary">{matched}</mark>
      <HighlightText text={after} keyword={keyword} />
    </span>
  )
}

export function Tree<T = Record<string, unknown>>({
  data,
  index: externalIndex,
  selectedKey,
  onSelect,
  checkable = false,
  checkedKeys: controlledCheckedKeys,
  onCheckedChange,
  checkStrictly = false,
  expandedKeys: controlledExpandedKeys,
  onExpandedKeysChange,
  defaultExpandAll = false,
  searchKeyword = '',
  showIcons = true,
  emptyText = '暂无匹配数据',
  className
}: TreeProps<T>) {
  const index = React.useMemo(() => externalIndex ?? buildTreeIndex(data), [externalIndex, data])

  const initialExpandedKeys = React.useMemo(() => {
    if (defaultExpandAll) {
      return new Set(index.allKeys)
    }
    if (selectedKey !== undefined && selectedKey !== null) {
      const ancestors = index.ancestorsMap.get(selectedKey) ?? []
      return new Set(ancestors)
    }
    return new Set<TreeKey>()
  }, [defaultExpandAll, selectedKey, index])

  const [uncontrolledExpandedKeys, setUncontrolledExpandedKeys] = React.useState<Set<TreeKey>>(initialExpandedKeys)

  const isControlledExpanded = controlledExpandedKeys !== undefined
  const expandedKeySet = React.useMemo(() => {
    return isControlledExpanded ? new Set(controlledExpandedKeys) : uncontrolledExpandedKeys
  }, [isControlledExpanded, controlledExpandedKeys, uncontrolledExpandedKeys])

  const updateExpandedKeys = React.useCallback(
    (nextSet: Set<TreeKey>) => {
      if (!isControlledExpanded) {
        setUncontrolledExpandedKeys(nextSet)
      }
      onExpandedKeysChange?.(Array.from(nextSet))
    },
    [isControlledExpanded, onExpandedKeysChange]
  )

  const checkedKeySet = React.useMemo(() => new Set(controlledCheckedKeys), [controlledCheckedKeys])

  /** Disabled nodes are locked: only `checkedKeys` decides their state, never a user action. */
  const disabledKeySet = React.useMemo(() => {
    const set = new Set<TreeKey>()
    for (const [key, node] of index.nodeMap.entries()) {
      if (node.disabled) {
        set.add(key)
      }
    }
    return set
  }, [index])

  const indeterminateKeySet = React.useMemo(() => {
    const set = new Set<TreeKey>()
    if (!checkable || checkStrictly) {
      return set
    }
    for (const key of checkedKeySet) {
      for (const a of index.ancestorsMap.get(key) ?? []) {
        if (!checkedKeySet.has(a)) {
          set.add(a)
        }
      }
    }
    return set
  }, [checkable, checkStrictly, checkedKeySet, index])

  const handleToggleCheck = React.useCallback(
    (nodeId: TreeKey) => {
      onCheckedChange?.(
        toggleCheckedKeys({
          index,
          nodeId,
          checked: checkedKeySet.has(nodeId),
          checkedKeys: checkedKeySet,
          checkStrictly,
          disabledKeys: disabledKeySet
        })
      )
    },
    [checkedKeySet, checkStrictly, disabledKeySet, index, onCheckedChange]
  )

  const lastAutoExpandedKey = React.useRef<TreeKey | null | undefined>(undefined)
  React.useEffect(() => {
    if (selectedKey === undefined || selectedKey === null) {
      lastAutoExpandedKey.current = undefined
      return
    }
    // The data can arrive after the selected key (async load). Do not record the key until its node
    // is in the index, or the same key would be skipped once its ancestors show up.
    if (!index.ancestorsMap.has(selectedKey)) return
    if (lastAutoExpandedKey.current === selectedKey) return
    lastAutoExpandedKey.current = selectedKey
    const ancestors = index.ancestorsMap.get(selectedKey) ?? []
    const missing = ancestors.filter(k => !expandedKeySet.has(k))
    if (missing.length === 0) return
    const next = new Set(expandedKeySet)
    missing.forEach(k => next.add(k))
    updateExpandedKeys(next)
  }, [selectedKey, index.ancestorsMap, expandedKeySet, updateExpandedKeys])

  const trimmedSearch = searchKeyword.trim().toLowerCase()
  const searchMatchInfo = React.useMemo(() => {
    if (!trimmedSearch) return null

    const matchedNodeKeys = new Set<TreeKey>()
    const searchExpandedKeys = new Set<TreeKey>()

    for (const [id, node] of index.nodeMap.entries()) {
      const content = `${node.label} ${node.searchTerms ?? ''}`.toLowerCase()
      if (content.includes(trimmedSearch)) {
        matchedNodeKeys.add(id)
        const ancestors = index.ancestorsMap.get(id) ?? []
        for (const ancestorId of ancestors) {
          matchedNodeKeys.add(ancestorId)
          searchExpandedKeys.add(ancestorId)
        }
      }
    }

    return { matchedNodeKeys, searchExpandedKeys }
  }, [trimmedSearch, index])

  const visibleNodes = React.useMemo(() => {
    const list: Array<{ node: TreeNodeData<T>; depth: number; hasChildren: boolean }> = []

    function traverse(nodes: TreeNodeData<T>[], depth: number) {
      for (const node of nodes) {
        const hasChildren = Boolean(node.children && node.children.length > 0)

        if (searchMatchInfo) {
          if (!searchMatchInfo.matchedNodeKeys.has(node.id)) {
            continue
          }
          list.push({ node, depth, hasChildren })
          if (hasChildren && searchMatchInfo.searchExpandedKeys.has(node.id)) {
            traverse(node.children ?? [], depth + 1)
          }
        } else {
          list.push({ node, depth, hasChildren })
          if (hasChildren && expandedKeySet.has(node.id)) {
            traverse(node.children ?? [], depth + 1)
          }
        }
      }
    }

    traverse(data, 0)
    return list
  }, [data, searchMatchInfo, expandedKeySet])

  const toggleExpand = React.useCallback(
    (key: TreeKey, e: React.MouseEvent) => {
      e.stopPropagation()
      const next = new Set(expandedKeySet)
      if (next.has(key)) {
        next.delete(key)
      } else {
        next.add(key)
      }
      updateExpandedKeys(next)
    },
    [expandedKeySet, updateExpandedKeys]
  )

  const handleSelect = React.useCallback(
    (node: TreeNodeData<T>) => {
      if (node.disabled) return
      if (checkable) {
        handleToggleCheck(node.id)
        return
      }
      onSelect?.(node.id, node)
    },
    [checkable, handleToggleCheck, onSelect]
  )

  // Roving tabindex: only one item is in the Tab order, and the arrow keys move between items.
  const [focusedKey, setFocusedKey] = React.useState<TreeKey | null>(null)
  const itemRefs = React.useRef(new Map<TreeKey, HTMLDivElement>())
  // Disabled items stay reachable (they announce `aria-disabled`); `handleSelect` already ignores them.
  const focusableKeys = visibleNodes.map(({ node }) => node.id)
  const tabStopKey = [focusedKey, selectedKey].find(k => k != null && focusableKeys.includes(k)) ?? focusableKeys[0]

  const moveFocus = (from: TreeKey, step: 1 | -1) => {
    const next = focusableKeys[focusableKeys.indexOf(from) + step]
    if (next === undefined) return
    setFocusedKey(next)
    itemRefs.current.get(next)?.focus()
  }

  if (visibleNodes.length === 0) {
    return (
      <div className={cn('flex flex-col items-center justify-center p-6 text-sm text-muted-foreground', className)}>
        {emptyText}
      </div>
    )
  }

  return (
    <div
      role="tree"
      className={cn(
        'flex w-fit min-w-full flex-col gap-0.5 select-none',
        '[--tree-indent:0.625rem] sm:[--tree-indent:1.125rem]',
        className
      )}
    >
      {visibleNodes.map(({ node, depth, hasChildren }) => {
        const isSelected = selectedKey === node.id
        const isChecked = checkable && checkedKeySet.has(node.id)
        const isExpanded = searchMatchInfo
          ? searchMatchInfo.searchExpandedKeys.has(node.id)
          : expandedKeySet.has(node.id)

        const isIndeterminate =
          checkable && !checkStrictly && !isChecked && hasChildren && indeterminateKeySet.has(node.id)

        return (
          <div
            key={node.id}
            ref={el => {
              if (el) itemRefs.current.set(node.id, el)
              return () => {
                itemRefs.current.delete(node.id)
              }
            }}
            role="treeitem"
            aria-selected={checkable ? undefined : isSelected}
            aria-checked={checkable ? (isIndeterminate ? 'mixed' : isChecked) : undefined}
            aria-expanded={hasChildren ? isExpanded : undefined}
            aria-disabled={node.disabled || undefined}
            tabIndex={node.id === tabStopKey ? 0 : -1}
            className={cn(
              'relative flex min-h-8 w-fit min-w-full items-center gap-1.5 rounded-md px-1.5 py-1 text-sm transition-colors outline-none focus-visible:ring-3 focus-visible:ring-ring/50 sm:min-h-7.5',
              node.disabled ? 'cursor-not-allowed opacity-50' : 'cursor-pointer',
              !checkable && isSelected
                ? 'bg-accent font-medium text-accent-foreground'
                : 'text-foreground hover:bg-accent/60 hover:text-accent-foreground'
            )}
            style={{ paddingLeft: `calc(${depth} * var(--tree-indent) + 0.375rem)` }}
            onFocus={() => setFocusedKey(node.id)}
            onClick={() => handleSelect(node)}
            onKeyDown={e => {
              if (e.key === 'Enter' || e.key === ' ') {
                e.preventDefault()
                handleSelect(node)
              } else if (e.key === 'ArrowDown' || e.key === 'ArrowUp') {
                e.preventDefault()
                moveFocus(node.id, e.key === 'ArrowDown' ? 1 : -1)
              } else if (e.key === 'ArrowRight' && hasChildren && !isExpanded && !searchMatchInfo) {
                e.preventDefault()
                const next = new Set(expandedKeySet)
                next.add(node.id)
                updateExpandedKeys(next)
              } else if (e.key === 'ArrowLeft' && hasChildren && isExpanded && !searchMatchInfo) {
                e.preventDefault()
                const next = new Set(expandedKeySet)
                next.delete(node.id)
                updateExpandedKeys(next)
              }
            }}
          >
            {hasChildren ? (
              searchMatchInfo ? (
                // Search forces the matched path open; the toggle would be a no-op, so show a
                // static, non-interactive caret instead of a dead button.
                <span
                  aria-hidden
                  className="-ml-1 flex size-6 shrink-0 items-center justify-center text-muted-foreground/40 sm:ml-0 sm:size-4"
                >
                  <ChevronRight
                    className={cn('size-3.5 transition-transform duration-150', isExpanded && 'rotate-90')}
                  />
                </span>
              ) : (
                <Button
                  type="button"
                  variant="ghost"
                  size="icon-xs"
                  tabIndex={-1}
                  className="-ml-1 touch-manipulation text-muted-foreground sm:ml-0 sm:size-4"
                  onClick={e => toggleExpand(node.id, e)}
                >
                  <ChevronRight
                    className={cn('size-3.5 transition-transform duration-150', isExpanded && 'rotate-90')}
                  />
                </Button>
              )
            ) : (
              <span className="-ml-1 size-6 shrink-0 sm:ml-0 sm:size-4" />
            )}

            {checkable && (
              // Display only: the treeitem owns focus, clicks and `aria-checked`.
              <Checkbox
                checked={isChecked}
                indeterminate={isIndeterminate}
                disabled={node.disabled}
                tabIndex={-1}
                aria-hidden
                className="pointer-events-none"
              />
            )}

            {showIcons && (node.icon || hasChildren) && (
              <span className="flex size-4 shrink-0 items-center justify-center text-muted-foreground">
                {node.icon ? (
                  React.isValidElement(node.icon) ? (
                    node.icon
                  ) : (
                    React.createElement(node.icon as React.ComponentType<{ className?: string }>, {
                      className: 'size-4'
                    })
                  )
                ) : hasChildren ? (
                  isExpanded ? (
                    <FolderOpen className="size-4 text-primary/80" />
                  ) : (
                    <Folder className="size-4 text-primary/80" />
                  )
                ) : null}
              </span>
            )}

            <div className="flex items-center gap-1.5 whitespace-nowrap">
              <span className="shrink-0">
                <HighlightText text={node.label} keyword={trimmedSearch} />
              </span>
              {node.tag}
              {node.subtitle}
            </div>

            {!checkable && isSelected && <Check className="size-4 shrink-0 text-primary" />}
          </div>
        )
      })}
    </div>
  )
}

export function TreeSkeleton({ rows = 5, className }: { rows?: number; className?: string }) {
  const depths = [0, 1, 2, 1, 0, 1, 2]

  return (
    <div className={cn('flex flex-col gap-1 p-2 select-none', className)}>
      {depths.slice(0, rows).map((depth, i) => (
        <div
          key={i}
          className="flex items-center gap-2 py-1.5"
          style={{ paddingLeft: `calc(${depth} * 1.125rem + 0.5rem)` }}
        >
          <Skeleton className="size-3.5 shrink-0 rounded-xs" />
          <Skeleton className="size-4 shrink-0 rounded-xs" />
          <Skeleton className="h-4 flex-1 rounded-sm" style={{ maxWidth: `${Math.max(80 - depth * 15, 35)}%` }} />
        </div>
      ))}
    </div>
  )
}
