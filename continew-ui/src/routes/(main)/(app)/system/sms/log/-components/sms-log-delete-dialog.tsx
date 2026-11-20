import { DeleteConfirmDialog } from '@/components/delete-confirm-dialog'
import { useLastRecord } from '@/hooks/use-last-record'
import { deleteSmsLog, smsLogKeys, type SmsLogResp } from '@/lib/api/sms-log'

interface SmsLogDeleteDialogProps {
  /** The log being deleted, or `null` before the first one is picked. */
  readonly log: SmsLogResp | null
  readonly open: boolean
  readonly onClose: () => void
}

/**
 * Confirms deleting one SMS log; the rows the backend protects never reach the dialog.
 */
export function SmsLogDeleteDialog({ log, open, onClose }: SmsLogDeleteDialogProps) {
  const shownLog = useLastRecord(log)

  return (
    <DeleteConfirmDialog
      open={open}
      onClose={onClose}
      title="删除短信日志"
      description={
        shownLog?.phone
          ? `是否确定删除手机号为「${shownLog.phone}」的短信日志？删除后不可恢复`
          : '是否确定删除该条短信日志？删除后不可恢复'
      }
      disabled={!log}
      action={async () => {
        if (log) await deleteSmsLog(log.id)
      }}
      invalidateKeys={[smsLogKeys.all]}
    />
  )
}
