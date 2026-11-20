import { DeleteConfirmDialog } from '@/components/delete-confirm-dialog'
import { useLastRecord } from '@/hooks/use-last-record'
import { deleteSmsConfigs, type SmsConfigResp } from '@/lib/api/sms'
import { smsKeys } from '@/lib/query-keys'

interface SmsDeleteDialogProps {
  readonly sms: SmsConfigResp | null
  readonly open: boolean
  readonly onClose: () => void
}

export function SmsDeleteDialog({ sms, open, onClose }: SmsDeleteDialogProps) {
  // Keep the config named after the dialog starts closing and `sms` is already null.
  const shownSms = useLastRecord(sms)

  return (
    <DeleteConfirmDialog
      open={open}
      onClose={onClose}
      title="删除短信配置"
      description={`确定要删除短信配置「${shownSms?.name}」吗？操作不可撤销。`}
      disabled={!sms}
      action={async () => {
        if (sms) await deleteSmsConfigs([sms.id])
      }}
      invalidateKeys={[smsKeys.all]}
      successMessage="删除配置成功"
    />
  )
}
