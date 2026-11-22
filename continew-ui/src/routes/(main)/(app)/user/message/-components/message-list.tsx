import * as React from 'react'

import { useQueryClient } from '@tanstack/react-query'
import type { ColumnDef, PaginationState } from '@tanstack/react-table'
import { CheckCheck, Eye, MailCheck, Trash2 } from 'lucide-react'

import { DataTable } from '@/components/data-table'
import { DeleteConfirmDialog } from '@/components/delete-confirm-dialog'
import { FilterSelect } from '@/components/filter-select'
import { ListPageCard } from '@/components/list-page-card'
import { RowActionsMenu } from '@/components/row-actions-menu'
import { TableFillArea } from '@/components/table-fill-area'
import { TableSearch } from '@/components/table-search'
import { TableToolbar } from '@/components/table-toolbar'
import { Button } from '@/components/ui/button'
import { Checkbox } from '@/components/ui/checkbox'
import { DropdownMenuGroup, DropdownMenuItem, DropdownMenuSeparator } from '@/components/ui/dropdown-menu'
import { toast } from '@/components/ui/toast'
import { useFullscreenTransition } from '@/hooks/use-fullscreen-transition'
import { useListTable } from '@/hooks/use-list-table'
import {
  deleteMessages,
  listMessages,
  type MessagePageQuery,
  type MessageResp,
  type MessageType,
  readAllMessages,
  readMessages
} from '@/lib/api/message'
import type { PageResp } from '@/lib/api/types'
import type { DataTableFeatures } from '@/lib/data-table-features'
import { reportError } from '@/lib/http'
import { messageKeys } from '@/lib/query-keys'

import { MESSAGE_TYPE_LABELS, MessageTypeBadge, UnreadDot } from './message-display'
import { MessageDetailDrawer } from './message-detail-drawer'
import { useUnreadCounts } from './use-messages'

const DEFAULT_SORT = 'createTime,desc'

const TYPE_OPTIONS = [
  { value: 'all', label: '全部' },
  { value: '1', label: MESSAGE_TYPE_LABELS[1] },
  { value: '2', label: MESSAGE_TYPE_LABELS[2] }
]
const READ_OPTIONS = [
  { value: 'all', label: '全部' },
  { value: 'unread', label: '未读' },
  { value: 'read', label: '已读' }
]

type MessageColumn = ColumnDef<DataTableFeatures, MessageResp>

interface MessageColumnOptions {
  readonly selected: ReadonlySet<string>
  readonly pageIds: React.RefObject<string[]>
  readonly onSelect: (ids: string[], checked: boolean) => void
  readonly onView: (message: MessageResp) => void
  readonly onRead: (message: MessageResp) => void
  readonly onDelete: (message: MessageResp) => void
}

function SelectAll({ selected, pageIds, onSelect }: Pick<MessageColumnOptions, 'selected' | 'pageIds' | 'onSelect'>) {
  const ids = pageIds.current
  const count = ids.filter(id => selected.has(id)).length

  return (
    <div className="flex items-center justify-center">
      <Checkbox
        checked={ids.length > 0 && count === ids.length}
        indeterminate={count > 0 && count < ids.length}
        // Read the ids on click: this header can render before the first page has arrived.
        onCheckedChange={checked => onSelect(pageIds.current, Boolean(checked))}
      />
    </div>
  )
}

function createMessageColumns({
  selected,
  pageIds,
  onSelect,
  onView,
  onRead,
  onDelete
}: MessageColumnOptions): MessageColumn[] {
  return [
    {
      id: 'select',
      header: () => <SelectAll selected={selected} pageIds={pageIds} onSelect={onSelect} />,
      enableHiding: false,
      cell: ({ row }) => (
        <div className="flex items-center justify-center">
          <Checkbox
            checked={selected.has(String(row.original.id))}
            onCheckedChange={checked => onSelect([String(row.original.id)], Boolean(checked))}
          />
        </div>
      )
    },
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
        </button>
      )
    },
    {
      accessorKey: 'type',
      header: '类型',
      cell: ({ row }) => <MessageTypeBadge type={row.original.type} />
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
    { accessorKey: 'createTime', header: '时间' },
    {
      id: 'actions',
      header: () => <div className="text-center">操作</div>,
      enableHiding: false,
      cell: ({ row }) => (
        <div className="flex items-center justify-center">
          <RowActionsMenu>
            <DropdownMenuGroup>
              <DropdownMenuItem onClick={() => onView(row.original)}>
                <Eye />
                查看
              </DropdownMenuItem>
              <DropdownMenuItem disabled={row.original.isRead} onClick={() => onRead(row.original)}>
                <MailCheck />
                标记已读
              </DropdownMenuItem>
            </DropdownMenuGroup>
            <DropdownMenuSeparator />
            <DropdownMenuItem variant="destructive" onClick={() => onDelete(row.original)}>
              <Trash2 />
              删除
            </DropdownMenuItem>
          </RowActionsMenu>
        </div>
      )
    }
  ]
}

const COLUMN_LABELS: Record<string, string> = { select: '选择', actions: '操作' }

export function MessageList() {
  const queryClient = useQueryClient()
  const { messageCount } = useUnreadCounts()
  const fullscreen = useFullscreenTransition()
  const pageIds = React.useRef<string[]>([])
  const [selected, setSelected] = React.useState<ReadonlySet<string>>(() => new Set())
  const [viewId, setViewId] = React.useState<string | number | null>(null)
  const [deleteIds, setDeleteIds] = React.useState<string[] | null>(null)
  const [isReading, setIsReading] = React.useState(false)

  const [title, setTitle] = React.useState('')
  const [type, setType] = React.useState<MessageType | undefined>(undefined)
  const [isRead, setIsRead] = React.useState<boolean | undefined>(undefined)

  const buildQuery = React.useCallback(
    (pagination: PaginationState) => {
      const params: MessagePageQuery = {
        page: pagination.pageIndex + 1,
        size: pagination.pageSize,
        sort: DEFAULT_SORT,
        title: title || undefined,
        type,
        isRead
      }
      return { queryKey: messageKeys.list(params), queryFn: (signal: AbortSignal) => listMessages(params, signal) }
    },
    [title, type, isRead]
  )

  const refresh = React.useCallback(() => {
    void queryClient.invalidateQueries({ queryKey: messageKeys.unread() })
    void queryClient.invalidateQueries({ queryKey: messageKeys.lists() })
  }, [queryClient])

  const markRead = React.useCallback(
    async (ids: string[] | null) => {
      setIsReading(true)
      try {
        await (ids ? readMessages(ids) : readAllMessages())
        toast.add({ title: ids ? '已标记为已读' : '全部消息已读', type: 'success' })
        setSelected(new Set())
        refresh()
      } catch (error) {
        reportError(error)
      } finally {
        setIsReading(false)
      }
    },
    [refresh, setIsReading, setSelected]
  )

  const onSelect = React.useCallback(
    (ids: string[], checked: boolean) => {
      setSelected(previous => {
        const next = new Set(previous)
        for (const id of ids) {
          if (checked) next.add(id)
          else next.delete(id)
        }
        return next
      })
    },
    [setSelected]
  )

  const buildColumns = React.useCallback(
    () =>
      createMessageColumns({
        selected,
        pageIds,
        onSelect,
        onView: message => setViewId(message.id),
        onRead: message => void markRead([String(message.id)]),
        onDelete: message => setDeleteIds([String(message.id)])
      }),
    [selected, onSelect, markRead, setViewId, setDeleteIds]
  )

  const { query, table, resetPage, resetColumns } = useListTable<MessageResp, PageResp<MessageResp>>({
    buildQuery,
    buildColumns,
    getRowId: row => String(row.id)
  })

  // Track the current page's ids so select-all only ever acts on visible rows.
  React.useEffect(() => {
    pageIds.current = (query.data?.list ?? []).map(row => String(row.id))
  }, [query.data])

  const handleSearch = (next: string) => {
    setTitle(next)
    resetPage()
  }

  const handleTypeChange = (value: string | null) => {
    setType(value === '1' ? 1 : value === '2' ? 2 : undefined)
    resetPage()
  }

  const handleReadChange = (value: string | null) => {
    setIsRead(value === 'read' ? true : value === 'unread' ? false : undefined)
    resetPage()
  }

  const filters = (
    <>
      <TableSearch className="col-span-2 h-8 w-full md:w-56" clearable placeholder="搜索标题" onSearch={handleSearch} />
      <FilterSelect
        label="类型"
        options={TYPE_OPTIONS}
        value={type === undefined ? 'all' : String(type)}
        onValueChange={handleTypeChange}
        className="w-full sm:w-36"
      />
      <FilterSelect
        label="状态"
        options={READ_OPTIONS}
        value={isRead === true ? 'read' : isRead === false ? 'unread' : 'all'}
        onValueChange={handleReadChange}
        className="w-full sm:w-32"
      />
    </>
  )

  const ids = pageIds.current.filter(id => selected.has(id))
  const suffix = ids.length > 0 ? ` (${ids.length})` : ''

  const actions = (
    <TableToolbar
      table={table}
      className="w-full xl:w-auto xl:justify-end"
      isRefreshing={query.isFetching && !query.isPending}
      isFullscreen={fullscreen.isFullscreen}
      onRefresh={() => void query.refetch()}
      onToggleFullscreen={fullscreen.toggleFullscreen}
      onResetColumns={resetColumns}
      columnLabels={COLUMN_LABELS}
      actions={
        <>
          <Button variant="outline" disabled={ids.length === 0 || isReading} onClick={() => void markRead(ids)}>
            <MailCheck />
            标记已读{suffix}
          </Button>
          <Button variant="outline" disabled={ids.length === 0} onClick={() => setDeleteIds(ids)}>
            <Trash2 />
            删除{suffix}
          </Button>
          <Button disabled={messageCount === 0 || isReading} onClick={() => void markRead(null)}>
            <CheckCheck />
            全部已读
          </Button>
        </>
      }
    />
  )

  return (
    <>
      <ListPageCard
        title="我的消息"
        description="系统推送的消息，阅读即标记为已读"
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
            emptyTitle="暂无消息"
            fillHeight
          />
        </TableFillArea>
      </ListPageCard>

      <MessageDetailDrawer messageId={viewId} open={viewId !== null} onClose={() => setViewId(null)} />
      <DeleteConfirmDialog
        open={deleteIds !== null}
        onClose={() => setDeleteIds(null)}
        title="删除消息"
        description={
          deleteIds && deleteIds.length > 1 ? `确定删除选中的 ${deleteIds.length} 条消息吗？` : '确定删除这条消息吗？'
        }
        action={async () => {
          await deleteMessages(deleteIds ?? [])
          setSelected(new Set())
        }}
        invalidateKeys={[messageKeys.unread(), messageKeys.lists()]}
      />
    </>
  )
}
