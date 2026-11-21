import * as React from 'react'

import { type PaginationState } from '@tanstack/react-table'

import { Plus, RefreshCw } from 'lucide-react'

import { DataTable } from '@/components/data-table'
import { ListPageCard } from '@/components/list-page-card'
import { TableFillArea } from '@/components/table-fill-area'
import { TableSearch } from '@/components/table-search'
import { TableToolbar } from '@/components/table-toolbar'
import { Button } from '@/components/ui/button'
import { useFullscreenTransition } from '@/hooks/use-fullscreen-transition'
import { useListTable } from '@/hooks/use-list-table'
import { usePermission } from '@/hooks/use-permission'
import { demoKeys, listDemos, type DemoResp } from '@/lib/api/demo'
import type { PageResp } from '@/lib/api/types'

import { createDemoColumns, type DemoAction } from './demo-columns'
import { DemoDeleteDialog } from './demo-delete-dialog'
import { DemoFormDrawer } from './demo-form-drawer'

const DEFAULT_SORT = 'id,desc'

const SKELETON_WIDTHS = ['mx-auto w-6', 'w-40', 'w-16', 'w-24', 'w-28', 'mx-auto w-10']

export function DemoList() {
  const { hasPermission } = usePermission()

  const [searchInput, setSearchInput] = React.useState('')
  const [keyword, setKeyword] = React.useState('')

  // One state per overlay: a null value means it is closed, so "which record" and "is it open" can
  // never drift apart. The form drawer holds an object, because a null id there means 新增.
  const [formTarget, setFormTarget] = React.useState<{ demoId: string | number | null } | null>(null)
  const [deletingDemo, setDeletingDemo] = React.useState<DemoResp | null>(null)
  const fullscreen = useFullscreenTransition()

  const handleRowAction = React.useCallback((action: DemoAction, demo: DemoResp) => {
    if (action === 'edit') {
      setFormTarget({ demoId: demo.id })
      return
    }

    setDeletingDemo(demo)
  }, [])

  const buildQuery = React.useCallback(
    (pagination: PaginationState) => {
      const params = {
        page: pagination.pageIndex + 1,
        size: pagination.pageSize,
        name: keyword || undefined,
        sort: DEFAULT_SORT
      }
      return { queryKey: demoKeys.list(params), queryFn: (signal: AbortSignal) => listDemos(params, signal) }
    },
    [keyword]
  )

  const buildColumns = React.useCallback(
    (rowOffset: number) => createDemoColumns(handleRowAction, rowOffset),
    [handleRowAction]
  )

  const { query, table, resetPage, resetColumns } = useListTable<DemoResp, PageResp<DemoResp>>({
    buildQuery,
    buildColumns,
    getRowId: row => String(row.id)
  })

  const handleSearch = React.useCallback(
    (next: string) => {
      setKeyword(next)
      resetPage()
    },
    [resetPage]
  )

  const hasActiveFilters = keyword !== ''

  const resetFilters = () => {
    setSearchInput('')
    setKeyword('')
    resetPage()
  }

  const filters = (
    <>
      <TableSearch
        className="col-span-2 h-8 w-full sm:w-64"
        value={searchInput}
        onValueChange={setSearchInput}
        onSearch={handleSearch}
        placeholder="搜索示例名称"
      />
      {hasActiveFilters ? (
        <Button type="button" variant="ghost" onClick={resetFilters}>
          <RefreshCw />
          重置
        </Button>
      ) : null}
    </>
  )

  const actions = (
    <TableToolbar
      table={table}
      className="w-full xl:w-auto xl:justify-end"
      isRefreshing={query.isFetching && !query.isPending}
      isFullscreen={fullscreen.isFullscreen}
      onRefresh={() => void query.refetch()}
      onToggleFullscreen={fullscreen.toggleFullscreen}
      onResetColumns={resetColumns}
      actions={
        hasPermission('system:demo:create') && (
          <Button className="w-full sm:w-auto" onClick={() => setFormTarget({ demoId: null })}>
            <Plus />
            新增示例
          </Button>
        )
      }
    />
  )

  return (
    <>
      <ListPageCard
        title="示例管理"
        description="管理示例数据，用于验证模块脚手架的完整链路"
        filters={filters}
        actions={actions}
        fullscreen={fullscreen}
      >
        <TableFillArea fullscreen={fullscreen}>
          <DataTable
            table={table}
            isLoading={query.isPending}
            isError={query.isError}
            isShowingPreviousRows={query.isPlaceholderData}
            onRetry={() => void query.refetch()}
            fillHeight
            skeletonWidths={SKELETON_WIDTHS}
          />
        </TableFillArea>
      </ListPageCard>

      <DemoFormDrawer
        demoId={formTarget?.demoId ?? null}
        open={formTarget !== null}
        onClose={() => setFormTarget(null)}
      />
      <DemoDeleteDialog
        demo={deletingDemo}
        open={deletingDemo !== null}
        onClose={() => setDeletingDemo(null)}
      />
    </>
  )
}
