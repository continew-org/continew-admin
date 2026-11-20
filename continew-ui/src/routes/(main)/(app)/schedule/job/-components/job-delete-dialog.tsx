import { DeleteConfirmDialog } from '@/components/delete-confirm-dialog'
import { useLastRecord } from '@/hooks/use-last-record'
import { deleteJob, jobKeys, type JobResp } from '@/lib/api/job'

interface JobDeleteDialogProps {
  /** The job being deleted, or `null` before one is picked. */
  readonly job: JobResp | null
  readonly open: boolean
  readonly onClose: () => void
}

/** Confirms deleting one job. */
export function JobDeleteDialog({ job, open, onClose }: JobDeleteDialogProps) {
  const shownJob = useLastRecord(job)

  return (
    <DeleteConfirmDialog
      open={open}
      onClose={onClose}
      title="删除任务"
      description={
        shownJob ? `是否确定删除任务「${shownJob.jobName}」？删除后不可恢复` : '是否确定删除该任务？删除后不可恢复'
      }
      disabled={!job}
      action={async () => {
        if (job) await deleteJob(job.id)
      }}
      invalidateKeys={[jobKeys.lists()]}
    />
  )
}
