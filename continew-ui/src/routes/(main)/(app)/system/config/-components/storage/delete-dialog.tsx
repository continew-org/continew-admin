import { DeleteConfirmDialog } from '@/components/delete-confirm-dialog'
import { useLastRecord } from '@/hooks/use-last-record'
import { deleteStorages, type StorageResp } from '@/lib/api/storage'
import { storageKeys } from '@/lib/query-keys'

interface StorageDeleteDialogProps {
  /** The storage being deleted, or `null` before the first one is picked. */
  readonly storage: StorageResp | null
  readonly open: boolean
  readonly onClose: () => void
}

/**
 * Confirms deleting one storage. The backend refuses to delete a storage that still has files
 * associated with it, or that is the default one, and reports why.
 */
export function StorageDeleteDialog({ storage, open, onClose }: StorageDeleteDialogProps) {
  // Keep the storage named after the dialog starts closing and `storage` is already null.
  const shownStorage = useLastRecord(storage)

  return (
    <DeleteConfirmDialog
      open={open}
      onClose={onClose}
      title="删除存储"
      description={
        shownStorage === null
          ? '是否确定删除该存储配置？删除后不可恢复。'
          : `是否确定删除存储配置「${shownStorage.name}(${shownStorage.code})」？删除后不可恢复，已上传至该存储的文件将无法访问。`
      }
      disabled={!storage}
      action={async () => {
        if (storage) await deleteStorages([storage.id])
      }}
      invalidateKeys={[storageKeys.all]}
    />
  )
}
