import { type ColumnDef } from '@tanstack/react-table'
import { Eye, Pen, Play, ScrollText, Trash2 } from 'lucide-react'

import { RowActionsMenu } from '@/components/row-actions-menu'
import { TextCell } from '@/components/text-cell'
import { Button } from '@/components/ui/button'
import { DropdownMenuGroup, DropdownMenuItem, DropdownMenuSeparator } from '@/components/ui/dropdown-menu'
import type { JobResp, JobStatus } from '@/lib/api/job'
import type { DataTableFeatures } from '@/lib/data-table-features'

import { JobStatusSwitch, TaskTypeCell, TriggerTypeCell } from './job-display'

/** The row actions the table raises. */
export type JobAction = 'detail' | 'trigger' | 'edit' | 'logs' | 'delete'

interface JobColumnOptions {
  /** The index of the first row on the current page, so 序号 stays continuous. */
  readonly rowOffset: number
  readonly canGet: boolean
  readonly canTrigger: boolean
  readonly canUpdate: boolean
  readonly canViewLogs: boolean
  readonly canDelete: boolean
  readonly onAction: (action: JobAction, job: JobResp) => void
  readonly onToggleStatus: (job: JobResp, next: JobStatus) => Promise<void>
}

function RowActions({
  job,
  canGet,
  canTrigger,
  canUpdate,
  canViewLogs,
  canDelete,
  onAction
}: {
  job: JobResp
  canGet: boolean
  canTrigger: boolean
  canUpdate: boolean
  canViewLogs: boolean
  canDelete: boolean
  onAction: (action: JobAction, job: JobResp) => void
}) {
  const hasGroup = canTrigger || canUpdate || canViewLogs
  const hasSecondary = hasGroup || canDelete

  return (
    <div className="flex items-center justify-center">
      <RowActionsMenu
        showMenu={hasSecondary}
        primary={
          canGet ? (
            <Button
              variant="ghost"
              size="icon-sm"
              aria-label="任务详情"
              title="任务详情"
              onClick={() => onAction('detail', job)}
            >
              <Eye />
            </Button>
          ) : undefined
        }
      >
        {hasGroup ? (
          <DropdownMenuGroup>
            {canTrigger ? (
              <DropdownMenuItem onClick={() => onAction('trigger', job)}>
                <Play />
                执行
              </DropdownMenuItem>
            ) : null}
            {canUpdate ? (
              <DropdownMenuItem onClick={() => onAction('edit', job)}>
                <Pen />
                修改
              </DropdownMenuItem>
            ) : null}
            {canViewLogs ? (
              <DropdownMenuItem onClick={() => onAction('logs', job)}>
                <ScrollText />
                查看日志
              </DropdownMenuItem>
            ) : null}
          </DropdownMenuGroup>
        ) : null}
        {hasGroup && canDelete ? <DropdownMenuSeparator /> : null}
        {canDelete ? (
          <DropdownMenuItem variant="destructive" onClick={() => onAction('delete', job)}>
            <Trash2 />
            删除
          </DropdownMenuItem>
        ) : null}
      </RowActionsMenu>
    </div>
  )
}

/** Builds the column set of the job table, in the order of the Vue client's page. */
export function createJobColumns({
  rowOffset,
  canGet,
  canTrigger,
  canUpdate,
  canViewLogs,
  canDelete,
  onAction,
  onToggleStatus
}: JobColumnOptions) {
  const columns: ColumnDef<DataTableFeatures, JobResp>[] = [
    {
      id: 'index',
      header: '序号',
      enableHiding: false,
      cell: ({ row }) => (
        <div className="text-center text-sm text-muted-foreground tabular-nums">{rowOffset + row.index + 1}</div>
      )
    },
    {
      accessorKey: 'jobName',
      header: '任务名称',
      cell: ({ row }) => <TextCell value={row.original.jobName} />
    },
    {
      accessorKey: 'triggerType',
      header: '调度类型',
      cell: ({ row }) => <TriggerTypeCell job={row.original} />
    },
    {
      accessorKey: 'taskType',
      header: '任务类型',
      cell: ({ row }) => <TaskTypeCell job={row.original} />
    },
    {
      accessorKey: 'jobStatus',
      header: () => <div className="text-center">状态</div>,
      cell: ({ row }) => (
        <div className="flex justify-center">
          <JobStatusSwitch job={row.original} canUpdate={canUpdate} onToggle={onToggleStatus} />
        </div>
      )
    },
    {
      accessorKey: 'description',
      header: '描述',
      cell: ({ row }) => <TextCell value={row.original.description} />
    },
    {
      accessorKey: 'createDt',
      header: '创建时间',
      cell: ({ row }) => <TextCell value={row.original.createDt} />
    },
    {
      accessorKey: 'updateDt',
      header: '修改时间',
      cell: ({ row }) => <TextCell value={row.original.updateDt} />
    }
  ]

  if (canGet || canTrigger || canUpdate || canViewLogs || canDelete) {
    columns.push({
      id: 'actions',
      header: () => <div className="text-center">操作</div>,
      enableHiding: false,
      cell: ({ row }) => (
        <RowActions
          job={row.original}
          canGet={canGet}
          canTrigger={canTrigger}
          canUpdate={canUpdate}
          canViewLogs={canViewLogs}
          canDelete={canDelete}
          onAction={onAction}
        />
      )
    })
  }

  return columns
}
