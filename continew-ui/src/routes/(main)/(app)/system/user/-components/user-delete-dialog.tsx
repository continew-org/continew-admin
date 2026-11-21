import { DeleteConfirmDialog } from '@/components/delete-confirm-dialog'
import { useLastRecord } from '@/hooks/use-last-record'
import { deleteUser, userKeys, type UserResp } from '@/lib/api/user'

import { userLabel } from './user-display'

interface UserDeleteDialogProps {
  /** The user being deleted, or `null` before the first one is picked. */
  readonly user: UserResp | null
  readonly open: boolean
  readonly onClose: () => void
}

export function UserDeleteDialog({ user, open, onClose }: UserDeleteDialogProps) {
  const shownUser = useLastRecord(user)
  const shownLabel = userLabel(shownUser)

  return (
    <DeleteConfirmDialog
      open={open}
      onClose={onClose}
      title="删除用户"
      description={
        shownUser ? `是否确定删除用户「${shownLabel}」？删除后不可恢复` : '是否确定删除该用户？删除后不可恢复'
      }
      disabled={!user}
      action={async () => {
        if (user) await deleteUser(user.id)
      }}
      invalidateKeys={[userKeys.all]}
    />
  )
}
