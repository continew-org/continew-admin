import * as React from 'react'

import type { TreeKey, TreeNodeData } from '@/components/ui/tree'
import { TreeSelect } from '@/components/ui/tree-select'
import { collectSubtreeIds } from '@/lib/collect-subtree-ids'

interface ParentTreeSelectProps<T extends { id: string | number; children?: T[] }> {
  readonly data: T[]
  /** The record being edited; its own subtree is disabled so it cannot become its own parent. */
  readonly currentId?: string | number | null
  /** Projects the tree into tree-select nodes; each form supplies its own labels and icons. */
  readonly toNodeData: (nodes: T[], disabledIds: ReadonlySet<string>) => TreeNodeData<T>[]
  readonly value?: string | number | null
  readonly onChange: (value: string | number | null) => void
  readonly disabled?: boolean
  readonly invalid?: boolean
  readonly placeholder: string
  readonly searchPlaceholder: string
  readonly emptyText: string
  /** Adds an option that clears the parent, such as the menu form's "根目录". */
  readonly allOptionLabel?: string
}

/**
 * Parent picker shared by the department and menu forms.
 *
 * Both pick one node from their own tree and must keep that node's subtree out of reach, so a
 * caller only supplies the tree and how its nodes become a label and an icon.
 */
export function ParentTreeSelect<T extends { id: string | number; children?: T[] }>({
  data,
  currentId,
  toNodeData,
  value,
  onChange,
  disabled,
  invalid,
  placeholder,
  searchPlaceholder,
  emptyText,
  allOptionLabel
}: ParentTreeSelectProps<T>) {
  const disabledIds = React.useMemo(
    () => (currentId ? collectSubtreeIds(data, currentId) : new Set<string>()),
    [data, currentId]
  )

  const treeData = React.useMemo(() => toNodeData(data, disabledIds), [data, disabledIds, toNodeData])

  const selectedValue = value !== null && value !== undefined && value !== '' ? String(value) : null

  const handleChange = (nextKey: TreeKey | null) => {
    if (nextKey === null || nextKey === '') {
      onChange(null)
      return
    }

    // Tree keys are the stringified node IDs; keep them as strings so long snowflake IDs are not rounded.
    onChange(String(nextKey))
  }

  return (
    <TreeSelect
      data={treeData}
      value={selectedValue}
      onChange={handleChange}
      disabled={disabled}
      invalid={invalid}
      placeholder={placeholder}
      searchPlaceholder={searchPlaceholder}
      emptyText={emptyText}
      showAllOption={Boolean(allOptionLabel)}
      allOptionLabel={allOptionLabel}
    />
  )
}
