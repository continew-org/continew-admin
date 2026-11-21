import * as React from 'react'

import { useQuery, useQueryClient } from '@tanstack/react-query'
import { type ColumnVisibilityState, type PaginationState } from '@tanstack/react-table'
import { useNavigate } from '@tanstack/react-router'

import { Plus } from 'lucide-react'

import { DataTable } from '@/components/data-table'
import { FilterSelect } from '@/components/filter-select'
import { ListPageCard } from '@/components/list-page-card'
import { TableFillArea } from '@/components/table-fill-area'
import { TableSearch } from '@/components/table-search'
import { TableToolbar } from '@/components/table-toolbar'
import { Button } from '@/components/ui/button'
import { toast } from '@/components/ui/toast'
import { useFullscreenTransition } from '@/hooks/use-fullscreen-transition'
import { useListTable } from '@/hooks/use-list-table'
import { usePermission } from '@/hooks/use-permission'
import {
  JOB_STATUS_OPTIONS,
  jobKeys,
  listJobGroups,
  listJobs,
  updateJobStatus,
  type JobQuery,
  type JobResp,
  type JobStatus
} from '@/lib/api/job'
import type { PageResp } from '@/lib/api/types'

import { createJobColumns, type JobAction } from './job-columns'
import { JobDeleteDialog } from './job-delete-dialog'
import { JobDetailDrawer } from './job-detail-drawer'
import { JobFormDrawer } from './job-form-drawer'
import { JobTriggerDialog } from './job-trigger-dialog'

const HIDDEN_BY_DEFAULT: ColumnVisibilityState = {
  updateDt: false
}

const SKELETON_WIDTHS: Record<string, string> = {
  index: 'mx-auto w-6',
  jobName: 'w-32',
  triggerType: 'w-32',
  taskType: 'w-28',
  jobStatus: 'mx-auto w-10 rounded-full',
  description: 'w-32',
  createDt: 'w-32',
  updateDt: 'w-32',
  actions: 'mx-auto w-8'
}

export function JobList() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const { hasPermission } = usePermission()
  const canCreate = hasPermission('schedule:job:create')
  const canGet = hasPermission('schedule:job:get')
  const canUpdate = hasPermission('schedule:job:update')
  const canDelete = hasPermission('schedule:job:delete')
  const canTrigger = hasPermission('schedule:job:trigger')
  const canViewLogs = hasPermission('schedule:log:list')
  const fullscreen = useFullscreenTransition()

  const [keyword, setKeyword] = React.useState('')
  const [jobStatus, setJobStatus] = React.useState<JobStatus | undefined>(undefined)
  const [groupName, setGroupName] = React.useState<string | undefined>(undefined)
  // The job query waits for the first group so the page does not briefly list every group.
  const [groupReady, setGroupReady] = React.useState(false)
  // A late group lookup must not override a group the user has already picked.
  const groupTouchedRef = React.useRef(false)
  // One state per overlay: a null value means it is closed. The form drawer holds an object,
  // because a null job there means 新增.
  const [detailJob, setDetailJob] = React.useState<JobResp | null>(null)
  const [formTarget, setFormTarget] = React.useState<{ job: JobResp | null } | null>(null)
  const [triggerJob, setTriggerJob] = React.useState<JobResp | null>(null)
  const [deletingJob, setDeletingJob] = React.useState<JobResp | null>(null)

  const groups = useQuery({
    queryKey: jobKeys.groups(),
    queryFn: ({ signal }) => listJobGroups(signal)
  })

  React.useEffect(() => {
    if (groups.isSuccess) {
      if (!groupTouchedRef.current) {
        setGroupName(current => current ?? groups.data[0])
      }
      setGroupReady(true)
      return
    }

    if (groups.isError) {
      // Let the list load without a group rather than wait forever on a failed lookup.
      setGroupReady(true)
    }
  }, [groups.isSuccess, groups.isError, groups.data])

  const groupItems = React.useMemo(
    () => [{ value: 'all', label: '全部' }, ...(groups.data ?? []).map(group => ({ value: group, label: group }))],
    [groups.data]
  )

  const handleToggleStatus = React.useCallback(
    async (job: JobResp, next: JobStatus) => {
      await updateJobStatus(job.id, { jobStatus: next })
      toast.add({ title: next === 1 ? '启用成功' : '禁用成功', type: 'success' })
      void queryClient.invalidateQueries({ queryKey: jobKeys.lists() })
    },
    [queryClient]
  )

  const handleAction = React.useCallback(
    (action: JobAction, job: JobResp) => {
      if (action === 'detail') {
        setDetailJob(job)
        return
      }

      if (action === 'trigger') {
        setTriggerJob(job)
        return
      }

      if (action === 'edit') {
        setFormTarget({ job })
        return
      }

      if (action === 'logs') {
        void navigate({
          to: '/schedule/log',
          search: { jobId: job.id, jobName: job.jobName, groupName: job.groupName }
        })
        return
      }

      setDeletingJob(job)
    },
    [navigate]
  )

  const buildQuery = React.useCallback(
    (pagination: PaginationState) => {
      const params: JobQuery = {
        page: pagination.pageIndex + 1,
        size: pagination.pageSize,
        groupName,
        jobName: keyword || undefined,
        jobStatus
      }
      return { queryKey: jobKeys.list(params), queryFn: (signal: AbortSignal) => listJobs(params, signal) }
    },
    [groupName, keyword, jobStatus]
  )

  const buildColumns = React.useCallback(
    (rowOffset: number) =>
      createJobColumns({
        rowOffset,
        canGet,
        canTrigger,
        canUpdate,
        canViewLogs,
        canDelete,
        onAction: handleAction,
        onToggleStatus: handleToggleStatus
      }),
    [canGet, canTrigger, canUpdate, canViewLogs, canDelete, handleAction, handleToggleStatus]
  )

  const { query, table, resetPage, resetColumns } = useListTable<JobResp, PageResp<JobResp>>({
    buildQuery,
    buildColumns,
    getRowId: row => String(row.id),
    initialColumnVisibility: HIDDEN_BY_DEFAULT,
    enabled: groupReady
  })

  // The API has no detail endpoint, so keep the drawer on the freshest copy of the row the
  // list currently holds and fall back to the clicked snapshot once it leaves the page.
  const liveDetailJob = React.useMemo(() => {
    if (!detailJob) {
      return null
    }

    return query.data?.list.find(job => String(job.id) === String(detailJob.id)) ?? detailJob
  }, [detailJob, query.data])

  const handleKeywordSearch = (next: string) => {
    setKeyword(next)
    resetPage()
  }

  const handleGroupChange = (value: string | null) => {
    groupTouchedRef.current = true
    setGroupName(!value || value === 'all' ? undefined : value)
    resetPage()
  }

  const handleStatusChange = (value: string | null) => {
    setJobStatus(!value || value === 'all' ? undefined : (Number(value) as JobStatus))
    resetPage()
  }

  const filters = (
    <>
      <TableSearch
        className="col-span-2 h-8 w-full sm:w-64"
        placeholder="搜索任务名称"
        clearable
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
        options={JOB_STATUS_OPTIONS}
        value={jobStatus === undefined ? 'all' : String(jobStatus)}
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
      columnLabels={{ jobStatus: '状态' }}
      actions={
        canCreate ? (
          <Button className="w-full sm:w-auto" onClick={() => setFormTarget({ job: null })}>
            <Plus />
            新增任务
          </Button>
        ) : null
      }
    />
  )

  return (
    <>
      <ListPageCard
        title="任务管理"
        description="管理定时任务、调度策略与执行状态"
        filters={filters}
        actions={actions}
        fullscreen={fullscreen}
      >
        <TableFillArea fullscreen={fullscreen}>
          <DataTable
            table={table}
            isLoading={query.isPending || !groupReady}
            isError={query.isError}
            isShowingPreviousRows={query.isPlaceholderData}
            onRetry={() => void query.refetch()}
            fillHeight
            skeletonWidths={table.getVisibleLeafColumns().map(column => SKELETON_WIDTHS[column.id] ?? 'w-24')}
          />
        </TableFillArea>
      </ListPageCard>

      <JobDetailDrawer job={liveDetailJob} open={detailJob !== null} onClose={() => setDetailJob(null)} />
      <JobFormDrawer
        job={formTarget?.job ?? null}
        open={formTarget !== null}
        onClose={() => setFormTarget(null)}
        defaultGroupName={groupName}
      />
      <JobTriggerDialog job={triggerJob} open={triggerJob !== null} onClose={() => setTriggerJob(null)} />
      <JobDeleteDialog job={deletingJob} open={deletingJob !== null} onClose={() => setDeletingJob(null)} />
    </>
  )
}
