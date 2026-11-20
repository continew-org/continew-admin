import * as React from 'react'

import { useQuery } from '@tanstack/react-query'
import { type PaginationState } from '@tanstack/react-table'
import { useNavigate } from '@tanstack/react-router'

import type { DateRange } from 'react-day-picker'

import { DataTable } from '@/components/data-table'
import { DateRangeFilter } from '@/components/date-range-filter'
import { FilterSelect } from '@/components/filter-select'
import { ListPageCard } from '@/components/list-page-card'
import { TableFillArea } from '@/components/table-fill-area'
import { TableSearch } from '@/components/table-search'
import { TableToolbar } from '@/components/table-toolbar'
import { useFullscreenTransition } from '@/hooks/use-fullscreen-transition'
import { useListTable } from '@/hooks/use-list-table'
import { usePermission } from '@/hooks/use-permission'
import { jobKeys, listJobGroups } from '@/lib/api/job'
import {
  JOB_EXECUTE_STATUS_OPTIONS,
  jobLogKeys,
  listJobLogs,
  type JobExecuteStatus,
  type JobLogQuery,
  type JobLogResp
} from '@/lib/api/job-log'
import type { PageResp } from '@/lib/api/types'
import { createRecentDateRange, toDateTimeRangeQuery } from '@/lib/date-range'

import { JobLogActionDialog, type JobLogActionTarget } from './job-log-action-dialog'
import { createJobLogColumns, type JobLogAction } from './job-log-columns'

const SKELETON_WIDTHS: Record<string, string> = {
  index: 'mx-auto w-6',
  groupName: 'w-28',
  jobName: 'w-32',
  createDt: 'w-32',
  taskBatchStatus: 'mx-auto w-16 rounded-full',
  operationReason: 'w-28',
  executionAt: 'w-32',
  actions: 'mx-auto w-8'
}

interface JobLogListProps {
  /** A job carried over from the job page's 查看日志 action. */
  readonly initialJobId?: number
  readonly initialJobName?: string
  readonly initialGroupName?: string
}

export function JobLogList({ initialJobId, initialJobName, initialGroupName }: JobLogListProps) {
  const navigate = useNavigate()
  const { hasPermission } = usePermission()
  const canStop = hasPermission('schedule:log:stop')
  const canRetry = hasPermission('schedule:log:retry')
  // `/schedule/job/group` needs the job list permission; a log-only role can still use the page.
  const canListJobs = hasPermission('schedule:job:list')
  const fullscreen = useFullscreenTransition()

  const [jobId, setJobId] = React.useState<number | undefined>(initialJobId)
  const [groupName, setGroupName] = React.useState<string | undefined>(initialGroupName)
  const [keyword, setKeyword] = React.useState(initialJobName ?? '')
  const [searchText, setSearchText] = React.useState(initialJobName ?? '')
  const [status, setStatus] = React.useState<JobExecuteStatus | undefined>(undefined)
  const [dateRange, setDateRange] = React.useState<DateRange | undefined>(() => createRecentDateRange())
  const [actionTarget, setActionTarget] = React.useState<JobLogActionTarget | null>(null)

  const groups = useQuery({
    queryKey: jobKeys.groups(),
    queryFn: ({ signal }) => listJobGroups(signal),
    enabled: canListJobs
  })

  const groupOptions = React.useMemo(() => {
    const list = groups.data ?? []
    return groupName && !list.includes(groupName) ? [groupName, ...list] : list
  }, [groups.data, groupName])

  const groupItems = React.useMemo(
    () => [{ value: 'all', label: '全部' }, ...groupOptions.map(group => ({ value: group, label: group }))],
    [groupOptions]
  )

  /** Clearing the URL search keeps the hidden `jobId` from outliving the visible filters. */
  const clearRouteSearch = () => {
    void navigate({ to: '/schedule/log', search: {}, replace: true })
  }

  const releaseJobFilter = () => {
    if (jobId === undefined) {
      return
    }

    setJobId(undefined)
    clearRouteSearch()
  }

  const handleAction = React.useCallback((type: JobLogAction, log: JobLogResp) => {
    setActionTarget({ type, log })
  }, [])

  const buildQuery = React.useCallback(
    (pagination: PaginationState) => {
      const params: JobLogQuery = {
        page: pagination.pageIndex + 1,
        size: pagination.pageSize,
        jobId,
        groupName,
        jobName: keyword || undefined,
        taskBatchStatus: status,
        datetimeRange: toDateTimeRangeQuery(dateRange)
      }
      return { queryKey: jobLogKeys.list(params), queryFn: (signal: AbortSignal) => listJobLogs(params, signal) }
    },
    [jobId, groupName, keyword, status, dateRange]
  )

  const buildColumns = React.useCallback(
    (rowOffset: number) => createJobLogColumns({ rowOffset, canStop, canRetry, onAction: handleAction }),
    [canStop, canRetry, handleAction]
  )

  const { query, table, resetPage, resetColumns } = useListTable<JobLogResp, PageResp<JobLogResp>>({
    buildQuery,
    buildColumns,
    getRowId: row => String(row.id)
  })

  const handleKeywordSearch = (next: string) => {
    setKeyword(next)
    releaseJobFilter()
    resetPage()
  }

  const handleGroupChange = (value: string | null) => {
    setGroupName(!value || value === 'all' ? undefined : value)
    releaseJobFilter()
    resetPage()
  }

  const handleStatusChange = (value: string | null) => {
    setStatus(!value || value === 'all' ? undefined : (Number(value) as JobExecuteStatus))
    releaseJobFilter()
    resetPage()
  }

  const handleDateRangeChange = (range: DateRange | undefined) => {
    setDateRange(range)
    releaseJobFilter()
    resetPage()
  }

  const filters = (
    <>
      <TableSearch
        className="col-span-2 h-8 w-full sm:w-64"
        placeholder="搜索任务名称"
        clearable
        value={searchText}
        onValueChange={setSearchText}
        onSearch={handleKeywordSearch}
      />
      <FilterSelect
        label="任务组"
        options={groupItems}
        value={groupName ?? 'all'}
        onValueChange={handleGroupChange}
        className="w-full sm:w-40"
      />
      <FilterSelect
        label="状态"
        options={JOB_EXECUTE_STATUS_OPTIONS}
        value={status === undefined ? 'all' : String(status)}
        onValueChange={handleStatusChange}
        className="w-full sm:w-32"
      />
      <DateRangeFilter value={dateRange} onChange={handleDateRangeChange} className="col-span-2" />
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
      columnLabels={{ taskBatchStatus: '执行状态', operationReason: '执行备注' }}
    />
  )

  return (
    <>
      <ListPageCard
        title="任务日志"
        description="查看任务调度记录、执行状态与执行结果"
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
            skeletonWidths={table.getVisibleLeafColumns().map(column => SKELETON_WIDTHS[column.id] ?? 'w-24')}
          />
        </TableFillArea>
      </ListPageCard>

      <JobLogActionDialog action={actionTarget} open={actionTarget !== null} onClose={() => setActionTarget(null)} />
    </>
  )
}
