import * as React from 'react'

import { type ColumnVisibilityState, type PaginationState } from '@tanstack/react-table'

import { Download, Plus } from 'lucide-react'

import { DataTable } from '@/components/data-table'
import { ListPageCard } from '@/components/list-page-card'
import { TableFillArea } from '@/components/table-fill-area'
import { TableSearch } from '@/components/table-search'
import { TableToolbar } from '@/components/table-toolbar'
import { Button } from '@/components/ui/button'
import { Spinner } from '@/components/ui/spinner'
import { useExport } from '@/hooks/use-export'
import { useFullscreenTransition } from '@/hooks/use-fullscreen-transition'
import { useListTable } from '@/hooks/use-list-table'
import { appKeys, type AppResp, exportApps, listApps } from '@/lib/api/app'
import type { PageResp } from '@/lib/api/types'

import { type AppAction, createAppColumns } from './app-columns'
import { AppDeleteDialog } from './app-delete-dialog'
import { AppDetailDrawer } from './app-detail-drawer'
import { AppFormDrawer } from './app-form-drawer'
import { AppResetSecretDialog } from './app-reset-secret-dialog'
import { AppSecretDialog } from './app-secret-dialog'

const HIDDEN_BY_DEFAULT: ColumnVisibilityState = {
  createUserString: false,
  updateUserString: false,
  updateTime: false
}

const DEFAULT_SORT = 'id,desc'

export function AppList() {
  const [keyword, setKeyword] = React.useState('')
  // One state per overlay: a null value means it is closed, so "which record" and "is it open" can
  // never drift apart. The form drawer holds an object, because a null id there means 新增.
  const [detailAppId, setDetailAppId] = React.useState<string | number | null>(null)
  const [formTarget, setFormTarget] = React.useState<{ appId: string | number | null } | null>(null)
  const [secretApp, setSecretApp] = React.useState<AppResp | null>(null)
  const [resetSecretApp, setResetSecretApp] = React.useState<AppResp | null>(null)
  const [deletingApp, setDeletingApp] = React.useState<AppResp | null>(null)
  const fullscreen = useFullscreenTransition()
  const { isExporting, exportData } = useExport(exportApps)

  const handleRowAction = React.useCallback((action: AppAction, app: AppResp) => {
    if (action === 'detail') {
      setDetailAppId(app.id)
      return
    }

    if (action === 'edit') {
      setFormTarget({ appId: app.id })
      return
    }

    if (action === 'secret') {
      setSecretApp(app)
      return
    }

    if (action === 'resetSecret') {
      setResetSecretApp(app)
      return
    }

    setDeletingApp(app)
  }, [])

  const buildQuery = React.useCallback(
    (pagination: PaginationState) => {
      const params = {
        page: pagination.pageIndex + 1,
        size: pagination.pageSize,
        description: keyword || undefined,
        sort: DEFAULT_SORT
      }
      return { queryKey: appKeys.list(params), queryFn: (signal: AbortSignal) => listApps(params, signal) }
    },
    [keyword]
  )

  const buildColumns = React.useCallback(
    (rowOffset: number) => createAppColumns(handleRowAction, rowOffset),
    [handleRowAction]
  )

  const { query, table, resetPage, resetColumns } = useListTable<AppResp, PageResp<AppResp>>({
    buildQuery,
    buildColumns,
    getRowId: row => String(row.id),
    initialColumnVisibility: HIDDEN_BY_DEFAULT
  })

  const handleSearch = (next: string) => {
    setKeyword(next)
    resetPage()
  }

  const queryFilters = { description: keyword || undefined, sort: DEFAULT_SORT }

  const filters = (
    <TableSearch
      className="col-span-2 h-8 w-full sm:w-64"
      placeholder="搜索应用名称、描述"
      clearable
      onSearch={handleSearch}
    />
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
        <>
          <Button className="w-full sm:w-auto" onClick={() => setFormTarget({ appId: null })}>
            <Plus />
            新增应用
          </Button>
          <Button variant="outline" disabled={isExporting} onClick={() => void exportData(queryFilters)}>
            {isExporting ? <Spinner /> : <Download />}
            导出
          </Button>
        </>
      }
    />
  )

  return (
    <>
      <ListPageCard
        title="应用管理"
        description="维护第三方接入应用及其访问凭证"
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
            skeletonWidths={['mx-auto w-4', 'mx-auto w-6', 'w-24', 'w-36', 'mx-auto w-12 rounded-full', 'w-24']}
          />
        </TableFillArea>
      </ListPageCard>

      <AppDetailDrawer appId={detailAppId} open={detailAppId !== null} onClose={() => setDetailAppId(null)} />
      <AppFormDrawer appId={formTarget?.appId ?? null} open={formTarget !== null} onClose={() => setFormTarget(null)} />
      <AppSecretDialog app={secretApp} open={secretApp !== null} onClose={() => setSecretApp(null)} />
      <AppResetSecretDialog
        app={resetSecretApp}
        open={resetSecretApp !== null}
        onClose={() => setResetSecretApp(null)}
        onSuccess={setSecretApp}
      />
      <AppDeleteDialog app={deletingApp} open={deletingApp !== null} onClose={() => setDeletingApp(null)} />
    </>
  )
}
