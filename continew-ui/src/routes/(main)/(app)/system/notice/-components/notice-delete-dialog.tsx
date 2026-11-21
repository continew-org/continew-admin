import { DeleteConfirmDialog } from '@/components/delete-confirm-dialog'
import { useLastRecord } from '@/hooks/use-last-record'
import { deleteNotice, noticeKeys, type NoticeResp } from '@/lib/api/notice'

interface NoticeDeleteDialogProps {
  /** The notice being deleted, or `null` before the first one is picked. */
  readonly notice: NoticeResp | null
  readonly open: boolean
  readonly onClose: () => void
}

export function NoticeDeleteDialog({ notice, open, onClose }: NoticeDeleteDialogProps) {
  const shownNotice = useLastRecord(notice)

  return (
    <DeleteConfirmDialog
      open={open}
      onClose={onClose}
      title="删除公告"
      description={
        shownNotice ? `是否确定删除公告「${shownNotice.title}」？删除后不可恢复` : '是否确定删除该公告？删除后不可恢复'
      }
      disabled={!notice}
      action={async () => {
        if (notice) await deleteNotice(notice.id)
      }}
      invalidateKeys={[noticeKeys.all]}
    />
  )
}
