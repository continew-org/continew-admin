import { DeleteConfirmDialog } from '@/components/delete-confirm-dialog'
import { useLastRecord } from '@/hooks/use-last-record'
import { deleteDict, deleteDictItem, dictKeys, type DictItemResp, type DictResp } from '@/lib/api/dict'

export type DictDeleteTarget = { type: 'dict'; dict: DictResp } | { type: 'item'; item: DictItemResp }

interface DictDeleteDialogProps {
  readonly target: DictDeleteTarget | null
  readonly open: boolean
  readonly onClose: () => void
}

/** Confirms deleting a dictionary (with its items) or one dictionary item. */
export function DictDeleteDialog({ target, open, onClose }: DictDeleteDialogProps) {
  // Keep the last target during the close animation, where `target` is already null.
  const shownTarget = useLastRecord(target)

  const description =
    shownTarget === null
      ? '是否确定删除该数据？删除后不可恢复'
      : shownTarget.type === 'dict'
        ? `是否确定删除字典「${shownTarget.dict.name}」？其下所有字典项也会一并删除`
        : `是否确定删除字典项「${shownTarget.item.label}」？`

  return (
    <DeleteConfirmDialog
      open={open}
      onClose={onClose}
      title={shownTarget?.type === 'dict' ? '删除字典' : '删除字典项'}
      description={description}
      disabled={!target}
      action={async () => {
        if (!target) return
        if (target.type === 'dict') {
          await deleteDict(target.dict.id)
        } else {
          await deleteDictItem(target.item.id)
        }
      }}
      // Deleting a dictionary only changes the dictionary list; its item page disappears with the
      // panel, so refetching `dictKeys.items()` there would hit a just-deleted dictionary for nothing.
      invalidateKeys={target?.type === 'item' ? [dictKeys.items()] : [dictKeys.lists()]}
    />
  )
}
