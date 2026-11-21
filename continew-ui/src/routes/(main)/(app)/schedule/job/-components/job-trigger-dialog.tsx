import * as React from 'react'

import { useQueryClient } from '@tanstack/react-query'

import { Play } from 'lucide-react'

import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogMedia,
  AlertDialogTitle
} from '@/components/ui/alert-dialog'
import { Spinner } from '@/components/ui/spinner'
import { toast } from '@/components/ui/toast'
import { useLastRecord } from '@/hooks/use-last-record'
import { triggerJob, type JobResp } from '@/lib/api/job'
import { jobLogKeys } from '@/lib/query-keys'

interface JobTriggerDialogProps {
  /** The job to run once, or `null` before one is picked. */
  readonly job: JobResp | null
  readonly open: boolean
  readonly onClose: () => void
}

/** Confirms running a job outside its schedule. */
export function JobTriggerDialog({ job, open, onClose }: JobTriggerDialogProps) {
  const queryClient = useQueryClient()
  const [isSubmitting, setIsSubmitting] = React.useState(false)
  const shownJob = useLastRecord(job)

  const handleTrigger = async () => {
    if (job === null) {
      return
    }

    setIsSubmitting(true)

    try {
      await triggerJob(job.id)

      onClose()
      // A manual run creates a job batch, so a cached log list is stale.
      void queryClient.invalidateQueries({ queryKey: jobLogKeys.lists() })
      toast.add({ title: '执行请求已下发', type: 'success' })
    } catch {
      // The HTTP layer already reported the failure.
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <AlertDialog
      open={open}
      onOpenChange={nextOpen => {
        // Closing mid-request would leave the request in flight with no way to report it.
        if (!nextOpen && !isSubmitting) {
          onClose()
        }
      }}
    >
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogMedia className="bg-primary/10 text-primary">
            <Play />
          </AlertDialogMedia>
          <AlertDialogTitle>执行任务</AlertDialogTitle>
          <AlertDialogDescription>
            {shownJob ? `是否确定立即执行一次任务「${shownJob.jobName}」？` : '是否确定立即执行一次该任务？'}
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel disabled={isSubmitting}>取消</AlertDialogCancel>
          <AlertDialogAction disabled={isSubmitting} onClick={() => void handleTrigger()}>
            {isSubmitting ? (
              <>
                <Spinner />
                执行中
              </>
            ) : (
              '执行'
            )}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  )
}
