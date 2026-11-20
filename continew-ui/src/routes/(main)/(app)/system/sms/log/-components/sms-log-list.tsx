import * as React from 'react'

import { type PaginationState } from '@tanstack/react-table'
import { Download } from 'lucide-react'

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
  exportSmsLogs,
  listSmsLogs,
  smsLogKeys,
  type SmsLogPageQuery,
  type SmsLogQuery,
  type SmsLogResp,
  type SmsLogStatus
} from '@/lib/api/sms-log'
import type { PageResp } from '@/lib/api/types'

import { createSmsLogColumns, SMS_LOG_COLUMN_LABELS } from './sms-log-columns'
import { SmsLogDeleteDialog } from './sms-log-delete-dialog'
import { SmsLogTable } from './sms-log-table'

/** Newest first; the log table has no alias in its queries, so the plain column works. */
const DEFAULT_SORT = 'id,desc'

const STATUS_OPTIONS = [
  { value: 'all', label: '全部' },
  { value: '1', label: '成功' },
  { value: '2', label: '失败' }
]

export function SmsLogList() {
  const { hasPermission } = usePermission()
  const canDelete = hasPermission('system:smsLog:delete')
  const canExport = hasPermission('system:smsLog:export')
  const fullscreen = useFullscreenTransition()
  const { isExporting, exportData } = useExport<SmsLogQuery>(exportSmsLogs)

  const [configId, setConfigId] = React.useState('')
  const [phone, setPhone] = React.useState('')
  const [status, setStatus] = React.useState<SmsLogStatus | undefined>(undefined)
  const [deletingLog, setDeletingLog] = React.useState<SmsLogResp | null>(null)

  const handleDelete = React.useCallback((log: SmsLogResp) => {
    setDeletingLog(log)
  }, [])

  const buildQuery = React.useCallback(
    (pagination: PaginationState) => {
      // `configId` reaches the backend as a Long, so a box without digits must not be sent.
      const configIdFilter = /^\d+$/.test(configId) ? configId : undefined
      const params: SmsLogPageQuery = {
        page: pagination.pageIndex + 1,
        size: pagination.pageSize,
        sort: DEFAULT_SORT,
        configId: configIdFilter,
        phone: phone || undefined,
        status
      }
      return { queryKey: smsLogKeys.list(params), queryFn: (signal: AbortSignal) => listSmsLogs(params, signal) }
    },
    [configId, phone, status]
  )

  const buildColumns = React.useCallback(
    (rowOffset: number) => createSmsLogColumns({ rowOffset, canDelete, onDelete: handleDelete }),
    [canDelete, handleDelete]
  )

  const { query, table, resetPage, resetColumns } = useListTable<SmsLogResp, PageResp<SmsLogResp>>({
    buildQuery,
    buildColumns,
    getRowId: row => String(row.id)
  })

  const queryFilters: SmsLogQuery = {
    sort: DEFAULT_SORT,
    configId: /^\d+$/.test(configId) ? configId : undefined,
    phone: phone || undefined,
    status
  }

  const handleConfigIdSearch = (next: string) => {
    setConfigId(next)
    resetPage()
  }

  const handlePhoneSearch = (next: string) => {
    setPhone(next)
    resetPage()
  }

  const handleStatusChange = (value: string | null) => {
    setStatus(value === '1' ? 1 : value === '2' ? 2 : undefined)
    resetPage()
  }

  const filters = (
    <>
      <TableSearch
        className="col-span-2 h-8 w-full md:w-44"
        clearable
        placeholder="搜索配置 ID"
        onSearch={handleConfigIdSearch}
      />
      <TableSearch
        className="col-span-2 h-8 w-full md:w-48"
        clearable
        placeholder="搜索手机号"
        onSearch={handlePhoneSearch}
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
      columnLabels={SMS_LOG_COLUMN_LABELS}
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
      <ListPageCard
        title="短信日志"
        description="查看和管理短信发送记录"
        filters={filters}
        actions={actions}
        fullscreen={fullscreen}
      >
        <TableFillArea fullscreen={fullscreen}>
          <SmsLogTable
            table={table}
            isLoading={query.isPending}
            isError={query.isError}
            isShowingPreviousRows={query.isPlaceholderData}
            onRetry={() => void query.refetch()}
            fillHeight
          />
        </TableFillArea>
      </ListPageCard>

      <SmsLogDeleteDialog log={deletingLog} open={deletingLog !== null} onClose={() => setDeletingLog(null)} />
    </>
  )
}
