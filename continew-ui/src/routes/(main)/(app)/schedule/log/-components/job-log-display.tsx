import { StatusBadge, type StatusTone } from '@/components/status-cell'
import { JOB_EXECUTE_REASON_LABELS, JOB_EXECUTE_STATUS_LABELS, type JobExecuteStatus } from '@/lib/api/job-log'
import { EMPTY_PLACEHOLDER } from '@/lib/constants'

const STATUS_TONES: Record<JobExecuteStatus, StatusTone> = {
  1: 'info',
  2: 'warning',
  3: 'success',
  4: 'danger',
  5: 'danger',
  6: 'neutral'
}

/** The execution status as the shared status badge. */
export function JobExecuteStatusBadge({ status }: { status: JobExecuteStatus }) {
  const label = JOB_EXECUTE_STATUS_LABELS[status] ?? EMPTY_PLACEHOLDER

  return <StatusBadge tone={STATUS_TONES[status] ?? 'neutral'} label={label} />
}

/** The execution note; the full label stays available through the native title. */
export function OperationReasonCell({ reason }: { reason: number | null | undefined }) {
  const label = reason === null || reason === undefined ? '无' : (JOB_EXECUTE_REASON_LABELS[reason] ?? String(reason))

  if (label === '无') {
    return <span className="text-sm text-muted-foreground">无</span>
  }

  return (
    <span className="block max-w-56 truncate text-sm" title={label}>
      {label}
    </span>
  )
}
