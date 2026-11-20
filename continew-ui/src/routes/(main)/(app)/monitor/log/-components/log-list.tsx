import * as React from 'react'

import { type PaginationState } from '@tanstack/react-table'
import { Download } from 'lucide-react'
import type { DateRange } from 'react-day-picker'

import { DateRangeFilter } from '@/components/date-range-filter'
import { FilterSelect } from '@/components/filter-select'
import { ListPageCard } from '@/components/list-page-card'
import { TableFillArea } from '@/components/table-fill-area'
import { TableSearch } from '@/components/table-search'
import { TableToolbar } from '@/components/table-toolbar'
import { Button } from '@/components/ui/button'
import { Spinner } from '@/components/ui/spinner'
import { useExport } from '@/hooks/use-export'
import { useFullscreenTransition } from '@/hooks/use-fullscreen-transition'
import { useListTable } from '@/hooks/use-list-table'
import { usePermission } from '@/hooks/use-permission'
import {
  exportLoginLogs,
  exportOperationLogs,
  listLogs,
  logKeys,
  type LogExportQuery,
  type LogPageQuery,
  type LogResp,
  type LogStatus
} from '@/lib/api/log'
import type { PageResp } from '@/lib/api/types'
import { createRecentDateRange, toDateTimeRangeQuery } from '@/lib/date-range'

import { createLoginLogColumns, createOperationLogColumns, LOG_COLUMN_LABELS } from './log-columns'
import { LogDetailDrawer } from './log-detail-drawer'
import { LogTable } from './log-table'

type LogMode = 'login' | 'operation'

/** The log query sorts on its own `t1` table alias, by the SQL column the mapper selects. */
const DEFAULT_SORT = 't1.create_time,desc'

const STATUS_OPTIONS = [
  { value: 'all', label: '全部' },
  { value: '1', label: '成功' },
  { value: '2', label: '失败' }
]

const PLACEHOLDERS: Record<LogMode, { operator: string; ip: string; date: string }> = {
  login: { operator: '搜索登录用户', ip: '搜索登录 IP 或地点', date: '登录时间' },
  operation: { operator: '搜索操作人', ip: '搜索操作 IP 或地点', date: '操作时间' }
}

interface LogListProps {
  readonly mode: LogMode
}

/** One log tab: its filters, its table and, for operation rows, the detail drawer. */
export function LogList({ mode }: LogListProps) {
  const isLogin = mode === 'login'
  const placeholders = PLACEHOLDERS[mode]
  const fullscreen = useFullscreenTransition()
  const { hasPermission } = usePermission()
  const canExport = hasPermission('monitor:log:export')
  const canViewDetail = hasPermission('monitor:log:get')
  const { isExporting, exportData } = useExport<LogExportQuery>(isLogin ? exportLoginLogs : exportOperationLogs)

  const [operator, setOperator] = React.useState('')
  const [ip, setIp] = React.useState('')
  const [status, setStatus] = React.useState<LogStatus | undefined>(undefined)
  const [createTime, setCreateTime] = React.useState<DateRange | undefined>(createRecentDateRange)
  // `id` and `open` live in one atomic state so they cannot drift apart. Closing only flips
  // `open` and keeps `id`: the drawer still renders the last log while its exit animation runs,
  // so blanking `id` here would flash a skeleton during the slide-out.
  const [detailView, setDetailView] = React.useState<{ readonly id: string | number | null; readonly open: boolean }>({
    id: null,
    open: false
  })

  const handleDetail = React.useCallback((log: LogResp) => {
    setDetailView({ id: log.id, open: true })
  }, [])

  const closeDetail = React.useCallback(() => {
    setDetailView(prev => ({ ...prev, open: false }))
  }, [])

  const buildQuery = React.useCallback(
    (pagination: PaginationState) => {
      const params: LogPageQuery = {
        page: pagination.pageIndex + 1,
        size: pagination.pageSize,
        sort: DEFAULT_SORT,
        module: isLogin ? '登录' : undefined,
        createUserString: operator || undefined,
        ip: ip || undefined,
        createTime: toDateTimeRangeQuery(createTime),
        status
      }
      return { queryKey: logKeys.list(params), queryFn: (signal: AbortSignal) => listLogs(params, signal) }
    },
    [isLogin, operator, ip, createTime, status]
  )

  const buildColumns = React.useCallback(
    (rowOffset: number) =>
      isLogin
        ? createLoginLogColumns(rowOffset)
        : createOperationLogColumns({ rowOffset, canViewDetail, onDetail: handleDetail }),
    [isLogin, canViewDetail, handleDetail]
  )

  const { query, table, resetPage, resetColumns } = useListTable<LogResp, PageResp<LogResp>>({
    buildQuery,
    buildColumns,
    getRowId: row => String(row.id)
  })

  const queryFilters: LogExportQuery = {
    sort: DEFAULT_SORT,
    module: isLogin ? '登录' : undefined,
    createUserString: operator || undefined,
    ip: ip || undefined,
    createTime: toDateTimeRangeQuery(createTime),
    status
  }

  const handleOperatorSearch = (next: string) => {
    setOperator(next)
    resetPage()
  }

  const handleIpSearch = (next: string) => {
    setIp(next)
    resetPage()
  }

  const handleCreateTimeChange = (next: DateRange | undefined) => {
    setCreateTime(next)
    resetPage()
  }

  const handleStatusChange = (value: string | null) => {
    setStatus(value === '1' ? 1 : value === '2' ? 2 : undefined)
    resetPage()
  }

  const filters = (
    <>
      <TableSearch
        className="col-span-2 h-8 w-full md:w-52"
        clearable
        placeholder={placeholders.operator}
        onSearch={handleOperatorSearch}
      />
      <TableSearch
        className="col-span-2 h-8 w-full md:w-56"
        clearable
        placeholder={placeholders.ip}
        onSearch={handleIpSearch}
      />
      <DateRangeFilter
        value={createTime}
        onChange={handleCreateTimeChange}
        placeholder={placeholders.date}
        className="col-span-2"
      />
      <FilterSelect
        label="状态"
        options={STATUS_OPTIONS}
        value={status === undefined ? 'all' : String(status)}
        onValueChange={handleStatusChange}
        className="w-full sm:w-32"
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
      columnLabels={LOG_COLUMN_LABELS}
      actions={
        canExport ? (
          <Button variant="outline" disabled={isExporting} onClick={() => void exportData(queryFilters)}>
            {isExporting ? <Spinner /> : <Download />}
            导出
          </Button>
        ) : null
      }
    />
  )

  return (
    <>
      <ListPageCard filters={filters} actions={actions} fullscreen={fullscreen}>
        <TableFillArea fullscreen={fullscreen}>
          <LogTable
            table={table}
            isLoading={query.isPending}
            isError={query.isError}
            isShowingPreviousRows={query.isPlaceholderData}
            onRetry={() => void query.refetch()}
            fillHeight
          />
        </TableFillArea>
      </ListPageCard>

      {!isLogin && canViewDetail ? (
        <LogDetailDrawer logId={detailView.id} open={detailView.open} onClose={closeDetail} />
      ) : null}
    </>
  )
}
