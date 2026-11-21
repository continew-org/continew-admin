import * as React from 'react'
import { useQuery } from '@tanstack/react-query'

import { TreeSelect } from '@/components/ui/tree-select'
import { deptKeys, getDeptTree, type DeptResp } from '@/lib/api/dept'
import { toDeptNodeData } from '@/lib/dept-tree'

const EMPTY_NODES: DeptResp[] = []

export interface DeptTreeSelectProps {
  readonly value?: string | number | null
  readonly onChange: (nextValue: string) => void
  readonly disabled?: boolean
  readonly invalid?: boolean
  readonly placeholder?: string
  /**
   * Gates the fallback query.
   *
   * Callers that fetch the tree themselves and pass it as `data` should pass their open flag:
   * the query then waits until `data` appears instead of racing the caller.
   */
  readonly enabled?: boolean
  readonly className?: string
  /** Pre-fetched department tree, for forms that already load it. */
  readonly data?: DeptResp[]
  /** Loading state of `data`, when the caller fetches it. */
  readonly loading?: boolean
  readonly triggerPrefix?: React.ReactNode
  readonly showAllOption?: boolean
  readonly allOptionLabel?: string
  readonly placeholderColor?: 'muted' | 'foreground'
}

/**
 * Department picker over the shared dept tree query.
 *
 * Without `data` it fetches the tree itself, which suits list filters; forms pass their own
 * `data` so the picker never issues a second request.
 */
export function DeptTreeSelect({
  value,
  onChange,
  disabled = false,
  invalid = false,
  placeholder = '请选择所属部门',
  enabled = true,
  className,
  data: externalData,
  loading: externalLoading,
  triggerPrefix,
  showAllOption = false,
  allOptionLabel = '全部',
  placeholderColor = 'muted'
}: DeptTreeSelectProps) {
  const shouldQuery = externalData === undefined

  const depts = useQuery({
    queryKey: deptKeys.trees(),
    queryFn: ({ signal }) => getDeptTree(undefined, signal),
    enabled: enabled && shouldQuery
  })

  const rawNodes = externalData ?? depts.data ?? EMPTY_NODES
  const isLoading = externalLoading ?? (shouldQuery && depts.isPending)
  const isError = shouldQuery && depts.isError

  const treeData = React.useMemo(() => toDeptNodeData(rawNodes), [rawNodes])

  const selectedValue = value !== undefined && value !== null && value !== '' ? String(value) : null

  const handleChange = (nextKey: string | number | null) => {
    onChange(nextKey ? String(nextKey) : '')
  }

  return (
    <TreeSelect
      data={treeData}
      value={selectedValue}
      onChange={handleChange}
      loading={isLoading}
      disabled={disabled || isError}
      invalid={invalid}
      placeholder={isLoading ? '部门加载中' : isError ? '部门加载失败' : placeholder}
      searchPlaceholder="搜索部门"
      emptyText="未找到相关部门"
      className={className}
      triggerPrefix={triggerPrefix}
      showAllOption={showAllOption}
      allOptionLabel={allOptionLabel}
      placeholderColor={placeholderColor}
    />
  )
}
