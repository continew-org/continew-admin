import type { ReactNode } from 'react'

import { ListChecks } from 'lucide-react'

import { CopyableText } from '@/components/copyable-text'
import { DetailField, DetailGrid } from '@/components/detail-field'
import { EntityDrawer } from '@/components/entity-drawer'
import { StatusCell } from '@/components/status-cell'
import { Badge } from '@/components/ui/badge'
import { useLastRecord } from '@/hooks/use-last-record'
import {
  JOB_BLOCK_STRATEGY_LABELS,
  JOB_ROUTE_KEY_LABELS,
  JOB_TASK_TYPE_LABELS,
  JOB_TRIGGER_TYPE_LABELS,
  type JobResp
} from '@/lib/api/job'
import { EMPTY_PLACEHOLDER } from '@/lib/constants'

function LabelBadge({ children }: { children: ReactNode }) {
  return <Badge variant="secondary">{children}</Badge>
}

interface JobDetailDrawerProps {
  /** The job to show; the list row is the record, since the API has no detail endpoint. */
  readonly job: JobResp | null
  readonly open: boolean
  readonly onClose: () => void
}

/** Read-only detail panel for one job, with the field order of the Vue client's drawer. */
export function JobDetailDrawer({ job, open, onClose }: JobDetailDrawerProps) {
  const shownJob = useLastRecord(job)

  return (
    <EntityDrawer open={open} onClose={onClose} title="任务详情" icon={<ListChecks className="size-4" />} width={36}>
      <div className="min-h-0 flex-1 overflow-y-auto overscroll-contain p-4">
        {shownJob ? (
          <DetailGrid>
            <DetailField label="ID" className="sm:col-span-2">
              <CopyableText value={String(shownJob.id)} />
            </DetailField>
            <DetailField label="任务组">{shownJob.groupName || EMPTY_PLACEHOLDER}</DetailField>
            <DetailField label="任务名称">{shownJob.jobName || EMPTY_PLACEHOLDER}</DetailField>
            <DetailField label="触发类型">
              <LabelBadge>{JOB_TRIGGER_TYPE_LABELS[shownJob.triggerType] ?? EMPTY_PLACEHOLDER}</LabelBadge>
            </DetailField>
            {shownJob.triggerType === 3 ? (
              <DetailField label="Cron">{shownJob.triggerInterval || EMPTY_PLACEHOLDER}</DetailField>
            ) : shownJob.triggerType === 2 ? (
              <DetailField label="间隔时长">
                {shownJob.triggerInterval ? `${shownJob.triggerInterval} 秒` : EMPTY_PLACEHOLDER}
              </DetailField>
            ) : (
              <div />
            )}
            <DetailField label="任务类型">
              <LabelBadge>{JOB_TASK_TYPE_LABELS[shownJob.taskType] ?? EMPTY_PLACEHOLDER}</LabelBadge>
            </DetailField>
            <DetailField label="执行器名称">{shownJob.executorInfo || EMPTY_PLACEHOLDER}</DetailField>
            <DetailField label="任务参数">{shownJob.argsStr || EMPTY_PLACEHOLDER}</DetailField>
            <DetailField label="路由策略">
              <LabelBadge>{JOB_ROUTE_KEY_LABELS[shownJob.routeKey] ?? EMPTY_PLACEHOLDER}</LabelBadge>
            </DetailField>
            <DetailField label="阻塞策略">
              <LabelBadge>{JOB_BLOCK_STRATEGY_LABELS[shownJob.blockStrategy] ?? EMPTY_PLACEHOLDER}</LabelBadge>
            </DetailField>
            <DetailField label="超时时间">
              {shownJob.executorTimeout === null || shownJob.executorTimeout === undefined
                ? EMPTY_PLACEHOLDER
                : `${shownJob.executorTimeout} 秒`}
            </DetailField>
            <DetailField label="最大重试次数">{shownJob.maxRetryTimes ?? EMPTY_PLACEHOLDER}</DetailField>
            <DetailField label="重试间隔">
              {shownJob.retryInterval === null || shownJob.retryInterval === undefined
                ? EMPTY_PLACEHOLDER
                : `${shownJob.retryInterval} 秒`}
            </DetailField>
            <DetailField label="并行数">{shownJob.parallelNum ?? EMPTY_PLACEHOLDER}</DetailField>
            <DetailField label="任务状态">
              <StatusCell status={shownJob.jobStatus === 1 ? 1 : 2} />
            </DetailField>
            <div />
            <DetailField label="描述" className="sm:col-span-2">
              {shownJob.description || EMPTY_PLACEHOLDER}
            </DetailField>
          </DetailGrid>
        ) : null}
      </div>
    </EntityDrawer>
  )
}
