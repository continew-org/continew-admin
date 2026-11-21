import { DeleteConfirmDialog } from '@/components/delete-confirm-dialog'
import { useLastRecord } from '@/hooks/use-last-record'
import { deleteFiles, type FileResp } from '@/lib/api/file'

interface FileDeleteDialogProps {
  readonly file: FileResp | null
  readonly open: boolean
  readonly onClose: () => void
  readonly onCompleted: () => void
}

/** Confirms deleting one file or folder. */
export function FileDeleteDialog({ file, open, onClose, onCompleted }: FileDeleteDialogProps) {
  const shownFile = useLastRecord(file)

  return (
    <DeleteConfirmDialog
      open={open}
      onClose={onClose}
      title={shownFile?.type === 0 ? '删除文件夹' : '删除文件'}
      description={
        shownFile
          ? `是否确定删除${shownFile.type === 0 ? '文件夹' : '文件'}「${shownFile.originalName}」？`
          : '是否确定删除该项？'
      }
      disabled={!file}
      action={async () => {
        if (file) await deleteFiles([file.id])
      }}
      onSuccess={onCompleted}
    />
  )
}
