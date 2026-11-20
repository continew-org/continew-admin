import * as React from 'react'

import { type PaginationState } from '@tanstack/react-table'

import { Plus, RefreshCw } from 'lucide-react'

import { DataTable } from '@/components/data-table'
import { FilterSelect } from '@/components/filter-select'
import { ListPageCard } from '@/components/list-page-card'
import { TableFillArea } from '@/components/table-fill-area'
import { TableSearch } from '@/components/table-search'
import { TableToolbar } from '@/components/table-toolbar'
import { Button } from '@/components/ui/button'
import { useFullscreenTransition } from '@/hooks/use-fullscreen-transition'
import { useListTable } from '@/hooks/use-list-table'
import { usePermission } from '@/hooks/use-permission'
import { listNotices, noticeKeys, type NoticeResp } from '@/lib/api/notice'
import type { PageResp } from '@/lib/api/types'

import { createNoticeColumns, HIDDEN_BY_DEFAULT, type NoticeAction } from './notice-columns'
import { NoticeDeleteDialog } from './notice-delete-dialog'
import { NoticeDetailDrawer } from './notice-detail-drawer'
import { NoticeFormDrawer } from './notice-form-drawer'
import { NoticeViewDrawer } from './notice-view-drawer'
import { useNoticeDicts } from './use-notice-dicts'

const DEFAULT_SORT = 'id,desc'

const SKELETON_WIDTHS = [
  'mx-auto w-6',
  'w-40',
  'w-16',
  'mx-auto w-16',
  'w-24',
  'mx-auto w-12 rounded-full',
  'mx-auto w-10',
  'mx-auto w-20',
  'w-28'
]

const ALL_OPTION = { value: 'all', label: '全部' }

export function NoticeList() {
  const { hasPermission } = usePermission()
  const dicts = useNoticeDicts()

  const [searchInput, setSearchInput] = React.useState('')
  const [keyword, setKeyword] = React.useState('')
  const [type, setType] = React.useState<string | undefined>(undefined)

  // One state per overlay: a null value means it is closed, so "which record" and "is it open" can
  // never drift apart. The form drawer holds an object, because a null id there means 新增.
  const [detailId, setDetailId] = React.useState<string | number | null>(null)
  const [viewId, setViewId] = React.useState<string | number | null>(null)
  const [formTarget, setFormTarget] = React.useState<{ noticeId: string | number | null } | null>(null)
  const [deletingNotice, setDeletingNotice] = React.useState<NoticeResp | null>(null)
  const fullscreen = useFullscreenTransition()

  const handleRowAction = React.useCallback((action: NoticeAction, notice: NoticeResp) => {
    if (action === 'detail') {
      setDetailId(notice.id)
      return
    }

    if (action === 'view') {
      setViewId(notice.id)
      return
    }

    if (action === 'edit') {
      setFormTarget({ noticeId: notice.id })
      return
    }

    setDeletingNotice(notice)
  }, [])

  const buildQuery = React.useCallback(
    (pagination: PaginationState) => {
      const params = {
        page: pagination.pageIndex + 1,
        size: pagination.pageSize,
        title: keyword || undefined,
        type,
        sort: DEFAULT_SORT
      }
      return { queryKey: noticeKeys.list(params), queryFn: (signal: AbortSignal) => listNotices(params, signal) }
    },
    [keyword, type]
  )

  const buildColumns = React.useCallback(
    (rowOffset: number) => createNoticeColumns(dicts, handleRowAction, rowOffset),
    [dicts, handleRowAction]
  )

  const { query, table, resetPage, resetColumns } = useListTable<NoticeResp, PageResp<NoticeResp>>({
    buildQuery,
    buildColumns,
    getRowId: row => String(row.id),
    initialColumnVisibility: HIDDEN_BY_DEFAULT
  })

  const handleSearch = React.useCallback(
    (next: string) => {
      setKeyword(next)
      resetPage()
    },
    [resetPage]
  )

  const hasActiveFilters = keyword !== '' || type !== undefined

  const handleTypeChange = (value: string | null) => {
    setType(!value || value === 'all' ? undefined : value)
    resetPage()
  }

  const resetFilters = () => {
    setSearchInput('')
    setKeyword('')
    setType(undefined)
    resetPage()
  }

  const filters = (
    <>
      <TableSearch
        className="col-span-2 h-8 w-full sm:w-64"
        value={searchInput}
        onValueChange={setSearchInput}
        onSearch={handleSearch}
        placeholder="搜索公告标题"
      />
      <FilterSelect
        label="分类"
        options={[ALL_OPTION, ...dicts.typeOptions]}
        value={type ?? 'all'}
        onValueChange={handleTypeChange}
        className="w-full sm:w-32"
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
        hasPermission('system:notice:create') && (
          <Button className="w-full sm:w-auto" onClick={() => setFormTarget({ noticeId: null })}>
            <Plus />
            新增公告
          </Button>
        )
      }
    />
  )

  return (
    <>
      <ListPageCard
        title="通知公告"
        description="管理系统内部的通知与公告"
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

      <NoticeDetailDrawer noticeId={detailId} open={detailId !== null} onClose={() => setDetailId(null)} />
      <NoticeViewDrawer noticeId={viewId} open={viewId !== null} onClose={() => setViewId(null)} />
      <NoticeFormDrawer
        noticeId={formTarget?.noticeId ?? null}
        open={formTarget !== null}
        onClose={() => setFormTarget(null)}
      />
      <NoticeDeleteDialog
        notice={deletingNotice}
        open={deletingNotice !== null}
        onClose={() => setDeletingNotice(null)}
      />
    </>
  )
}
