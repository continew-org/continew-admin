import * as React from 'react'

import { useQueryClient } from '@tanstack/react-query'

import { RotateCcw, Square } from 'lucide-react'

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
import { jobLogKeys, retryJobLog, stopJobLog, type JobLogResp } from '@/lib/api/job-log'

export interface JobLogActionTarget {
  readonly type: 'stop' | 'retry'
  readonly log: JobLogResp
}

interface JobLogActionDialogProps {
  /** The action to confirm, or `null` before one is picked. */
  readonly action: JobLogActionTarget | null
  readonly open: boolean
  readonly onClose: () => void
}

/** Confirms stopping or retrying one job batch. */
export function JobLogActionDialog({ action, open, onClose }: JobLogActionDialogProps) {
  const queryClient = useQueryClient()
  const [isSubmitting, setIsSubmitting] = React.useState(false)
  const shownAction = useLastRecord(action)
  const isStop = shownAction?.type === 'stop'

  const handleConfirm = async () => {
    if (action === null) {
      return
    }

    setIsSubmitting(true)

    try {
      if (action.type === 'stop') {
        await stopJobLog(action.log.id)
      } else {
        await retryJobLog(action.log.id)
      }

      onClose()
      void queryClient.invalidateQueries({ queryKey: jobLogKeys.lists() })
      toast.add({ title: action.type === 'stop' ? '停止成功' : '重试成功', type: 'success' })
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
          <AlertDialogMedia className={isStop ? 'bg-destructive/10 text-destructive' : 'bg-primary/10 text-primary'}>
            {isStop ? <Square /> : <RotateCcw />}
          </AlertDialogMedia>
          <AlertDialogTitle>{isStop ? '停止任务' : '重试任务'}</AlertDialogTitle>
          <AlertDialogDescription>
            {shownAction
              ? isStop
                ? `是否确定停止任务「${shownAction.log.jobName}」的本次执行？`
                : `是否确定重试任务「${shownAction.log.jobName}」的本次执行？`
              : isStop
                ? '是否确定停止本次执行？'
                : '是否确定重试本次执行？'}
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel disabled={isSubmitting}>取消</AlertDialogCancel>
          <AlertDialogAction
            variant={isStop ? 'destructive' : 'default'}
            disabled={isSubmitting}
            onClick={() => void handleConfirm()}
          >
            {isSubmitting ? (
              <>
                <Spinner />
                {isStop ? '停止中' : '重试中'}
              </>
            ) : isStop ? (
              '停止'
            ) : (
              '重试'
            )}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  )
}
