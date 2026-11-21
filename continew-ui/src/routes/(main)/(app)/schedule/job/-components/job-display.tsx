import * as React from 'react'

import { Badge } from '@/components/ui/badge'
import { Switch } from '@/components/ui/switch'
import { Tooltip, TooltipContent, TooltipTrigger } from '@/components/ui/tooltip'
import { JOB_TASK_TYPE_LABELS, JOB_TRIGGER_TYPE_LABELS, type JobResp, type JobStatus } from '@/lib/api/job'
import { EMPTY_PLACEHOLDER } from '@/lib/constants'

/** The trigger type plus what it actually schedules: seconds or a Quartz expression. */
export function TriggerTypeCell({ job }: { job: JobResp }) {
  const label = JOB_TRIGGER_TYPE_LABELS[job.triggerType] ?? EMPTY_PLACEHOLDER

  return (
    <div className="flex items-center gap-1.5">
      <Badge variant="secondary">{label}</Badge>
      <span className="text-sm text-muted-foreground">：</span>
      {job.triggerType === 2 ? (
        <span className="text-sm">{job.triggerInterval} 秒</span>
      ) : (
        <Tooltip>
          <TooltipTrigger render={<span className="max-w-44 cursor-default truncate text-sm" />}>
            {job.triggerInterval}
          </TooltipTrigger>
          <TooltipContent side="top" className="max-w-64">
            <div className="flex flex-col gap-1">
              <span>{job.triggerInterval}</span>
              {job.nextTriggerAt ? <span className="text-muted-foreground">下次执行：{job.nextTriggerAt}</span> : null}
            </div>
          </TooltipContent>
        </Tooltip>
      )}
    </div>
  )
}

/** The task type and the executor that runs it. */
export function TaskTypeCell({ job }: { job: JobResp }) {
  const label = JOB_TASK_TYPE_LABELS[job.taskType] ?? EMPTY_PLACEHOLDER

  return (
    <div className="flex items-center gap-1.5">
      <Badge variant="secondary">{label}</Badge>
      <span className="text-sm">{job.executorInfo || EMPTY_PLACEHOLDER}</span>
    </div>
  )
}

interface JobStatusSwitchProps {
  readonly job: JobResp
  readonly canUpdate: boolean
  readonly onToggle: (job: JobResp, next: JobStatus) => Promise<void>
}

/**
 * The inline status switch of the job table.
 *
 * It keeps its own optimistic value so the knob follows the click immediately; a failed
 * request flips it back, and a successful one refreshes the row underneath.
 */
export function JobStatusSwitch({ job, canUpdate, onToggle }: JobStatusSwitchProps) {
  const [checked, setChecked] = React.useState(job.jobStatus === 1)
  const [isPending, setIsPending] = React.useState(false)

  React.useEffect(() => {
    setChecked(job.jobStatus === 1)
  }, [job.jobStatus])

  const handleChange = async (next: boolean) => {
    if (!canUpdate || isPending) {
      return
    }

    setChecked(next)
    setIsPending(true)

    try {
      await onToggle(job, next ? 1 : 0)
    } catch {
      setChecked(!next)
    } finally {
      setIsPending(false)
    }
  }

  return (
    <Switch checked={checked} disabled={!canUpdate || isPending} onCheckedChange={value => void handleChange(value)} />
  )
}
