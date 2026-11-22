import * as React from 'react'

import type { ColumnDef, PaginationState } from '@tanstack/react-table'

import { DataTable } from '@/components/data-table'
import { FilterSelect } from '@/components/filter-select'
import { ListPageCard } from '@/components/list-page-card'
import { TableFillArea } from '@/components/table-fill-area'
import { TableSearch } from '@/components/table-search'
import { TableToolbar } from '@/components/table-toolbar'
import { useFullscreenTransition } from '@/hooks/use-fullscreen-transition'
import { useListTable } from '@/hooks/use-list-table'
import { listUserNotices, type UserNoticePageQuery } from '@/lib/api/message'
import type { NoticeResp } from '@/lib/api/notice'
import type { PageResp } from '@/lib/api/types'
import type { DataTableFeatures } from '@/lib/data-table-features'
import { messageKeys } from '@/lib/query-keys'
import { NoticeTypeBadge } from '@/routes/(main)/(app)/system/notice/-components/notice-display'
import { useNoticeDicts } from '@/routes/(main)/(app)/system/notice/-components/use-notice-dicts'
import { NoticeViewDrawer } from '@/routes/(main)/(app)/system/notice/-components/notice-view-drawer'

import { UnreadDot } from './message-display'

const DEFAULT_SORT = 'publishTime,desc'

type NoticeColumn = ColumnDef<DataTableFeatures, NoticeResp>

function createNoticeColumns(
  typeOptions: { value: string; label: string }[],
  onView: (notice: NoticeResp) => void
): NoticeColumn[] {
  return [
    {
      accessorKey: 'title',
      header: '标题',
      cell: ({ row }) => (
        <button
          type="button"
          className="flex max-w-96 min-w-0 items-center gap-2 text-left hover:text-primary"
          onClick={() => onView(row.original)}
        >
          <UnreadDot unread={!row.original.isRead} />
          <span className={row.original.isRead ? 'truncate text-muted-foreground' : 'truncate font-medium'}>
            {row.original.title}
          </span>
          {row.original.isTop && <span className="shrink-0 text-xs text-primary">置顶</span>}
        </button>
      )
    },
    {
      accessorKey: 'type',
      header: '分类',
      cell: ({ row }) => <NoticeTypeBadge type={row.original.type} options={typeOptions} />
    },
    {
      accessorKey: 'isRead',
      header: '状态',
      cell: ({ row }) => (
        <span className={row.original.isRead ? 'text-muted-foreground' : 'text-destructive'}>
          {row.original.isRead ? '已读' : '未读'}
        </span>
      )
    },
    { accessorKey: 'createUserString', header: '发布人' },
    { accessorKey: 'publishTime', header: '发布时间' }
  ]
}

export function NoticeList() {
  const { typeOptions } = useNoticeDicts()
  const fullscreen = useFullscreenTransition()
  const [viewId, setViewId] = React.useState<string | number | null>(null)
  const [title, setTitle] = React.useState('')
  const [type, setType] = React.useState<string | undefined>(undefined)

  const buildQuery = React.useCallback(
    (pagination: PaginationState) => {
      const params: UserNoticePageQuery = {
        page: pagination.pageIndex + 1,
        size: pagination.pageSize,
        sort: DEFAULT_SORT,
        title: title || undefined,
        type
      }
      return {
        queryKey: messageKeys.noticeList(params),
        queryFn: (signal: AbortSignal) => listUserNotices(params, signal)
      }
    },
    [title, type]
  )

  const buildColumns = React.useCallback(
    () => createNoticeColumns(typeOptions, notice => setViewId(notice.id)),
    [typeOptions]
  )

  const { query, table, resetPage, resetColumns } = useListTable<NoticeResp, PageResp<NoticeResp>>({
    buildQuery,
    buildColumns,
    getRowId: row => String(row.id)
  })

  const handleSearch = (next: string) => {
    setTitle(next)
    resetPage()
  }

  const handleTypeChange = (value: string | null) => {
    setType(value === 'all' ? undefined : (value ?? undefined))
    resetPage()
  }

  const filters = (
    <>
      <TableSearch className="col-span-2 h-8 w-full md:w-56" clearable placeholder="搜索标题" onSearch={handleSearch} />
      <FilterSelect
        label="分类"
        options={[{ value: 'all', label: '全部' }, ...typeOptions]}
        value={type ?? 'all'}
        onValueChange={handleTypeChange}
        className="w-full sm:w-36"
      />
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
    />
  )

  return (
    <>
      <ListPageCard
        title="我的公告"
        description="收到的系统公告，查看后自动标记为已读"
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
            emptyTitle="暂无公告"
            fillHeight
          />
        </TableFillArea>
      </ListPageCard>

      <NoticeViewDrawer noticeId={viewId} open={viewId !== null} onClose={() => setViewId(null)} source="self" />
    </>
  )
}
