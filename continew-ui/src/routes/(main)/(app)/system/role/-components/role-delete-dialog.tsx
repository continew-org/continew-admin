import { DeleteConfirmDialog } from '@/components/delete-confirm-dialog'
import { useLastRecord } from '@/hooks/use-last-record'
import { deleteRoles, roleKeys, type RoleResp } from '@/lib/api/role'

interface RoleDeleteDialogProps {
  /** The role being deleted, or `null` before the first one is picked. */
  readonly role: RoleResp | null
  readonly open: boolean
  readonly onClose: () => void
}

export function RoleDeleteDialog({ role, open, onClose }: RoleDeleteDialogProps) {
  const shownRole = useLastRecord(role)

  return (
    <DeleteConfirmDialog
      open={open}
      onClose={onClose}
      title="删除角色"
      description={
        shownRole
          ? `确定要删除角色「${shownRole.name}」吗？删除后该角色下关联的用户权限将失效，操作不可撤回。`
          : '确定要删除该角色吗？删除后不可撤回。'
      }
      disabled={!role}
      action={async () => {
        if (role) await deleteRoles([role.id])
      }}
      invalidateKeys={[roleKeys.all]}
      successMessage="删除角色成功"
    />
  )
}
