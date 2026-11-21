import * as React from 'react'

import { keepPreviousData, useQuery } from '@tanstack/react-query'
import {
  type ColumnDef,
  type ColumnVisibilityState,
  type PaginationState,
  type RowData,
  useTable
} from '@tanstack/react-table'

import { usePageClamp } from '@/hooks/use-page-clamp'
import { dataTableFeatures, type DataTableFeatures } from '@/lib/data-table-features'

interface PageData<TRow> {
  readonly list: TRow[]
  readonly total: number
}

interface UseListTableOptions<TRow extends RowData, TData extends PageData<TRow>> {
  /** Builds the request for the current page; wrap it in `useCallback`. */
  readonly buildQuery: (pagination: PaginationState) => {
    readonly queryKey: readonly unknown[]
    readonly queryFn: (signal: AbortSignal) => Promise<TData>
  }
  /** Builds the columns for the current row offset; wrap it in `useCallback`. */
  readonly buildColumns: (rowOffset: number) => ColumnDef<DataTableFeatures, TRow>[]
  readonly getRowId: (row: TRow) => string
  readonly initialColumnVisibility?: ColumnVisibilityState
  readonly pageSize?: number
  /** Delays the query, for a screen that must wait on a lookup before listing. */
  readonly enabled?: boolean
}

/**
 * The state wiring every paginated list screen shares: pagination, column visibility, the query
 * (with `keepPreviousData`), the page clamp and the table instance. Screens keep their own columns,
 * filters and toolbars, so only the repeated plumbing lives here.
 */
export function useListTable<TRow extends RowData, TData extends PageData<TRow>>({
  buildQuery,
  buildColumns,
  getRowId,
  initialColumnVisibility = {},
  pageSize = 10,
  enabled = true
}: UseListTableOptions<TRow, TData>) {
  const [pagination, setPagination] = React.useState<PaginationState>({ pageIndex: 0, pageSize })
  const [columnVisibility, setColumnVisibility] = React.useState<ColumnVisibilityState>(initialColumnVisibility)
  const emptyRows = React.useMemo<TRow[]>(() => [], [])

  const request = React.useMemo(() => buildQuery(pagination), [buildQuery, pagination])
  const query = useQuery({
    queryKey: request.queryKey,
    queryFn: ({ signal }) => request.queryFn(signal),
    placeholderData: keepPreviousData,
    enabled
  })

  // Keep `undefined` while the total is unknown (first load / page change) so the page clamp does
  // not treat the 0 stand-in as a confirmed empty result; `rowCount` still feeds the table a number.
  const total = query.data?.total
  const rowCount = total ?? 0
  const rowOffset = pagination.pageIndex * pagination.pageSize

  usePageClamp(total, pagination, setPagination)

  const columns = React.useMemo(() => buildColumns(rowOffset), [buildColumns, rowOffset])

  const table = useTable({
    features: dataTableFeatures,
    data: query.data?.list ?? emptyRows,
    columns,
    state: { columnVisibility, pagination },
    manualPagination: true,
    rowCount,
    pageCount: Math.max(Math.ceil(rowCount / pagination.pageSize), 1),
    autoResetPageIndex: false,
    onColumnVisibilityChange: setColumnVisibility,
    onPaginationChange: setPagination,
    getRowId
  })

  const resetPage = React.useCallback(() => {
    setPagination(current => (current.pageIndex === 0 ? current : { ...current, pageIndex: 0 }))
  }, [])

  const resetColumns = React.useCallback(() => setColumnVisibility(initialColumnVisibility), [initialColumnVisibility])

  return { query, table, columnVisibility, setColumnVisibility, resetPage, resetColumns }
}
