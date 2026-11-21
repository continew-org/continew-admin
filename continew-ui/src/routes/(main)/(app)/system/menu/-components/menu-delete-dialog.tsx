import { DeleteConfirmDialog } from '@/components/delete-confirm-dialog'
import { useLastRecord } from '@/hooks/use-last-record'
import { deleteMenu, menuKeys, type MenuResp } from '@/lib/api/menu'

interface MenuDeleteDialogProps {
  readonly menu: MenuResp | null
  readonly open: boolean
  readonly onClose: () => void
}

export function MenuDeleteDialog({ menu, open, onClose }: MenuDeleteDialogProps) {
  const shownMenu = useLastRecord(menu)

  const childCount = shownMenu?.children?.length ?? 0
  const hasChildren = childCount > 0

  const description = hasChildren ? (
    <span className="block space-y-1">
      <span className="font-medium text-destructive">
        菜单「{shownMenu?.title}」下包含 {childCount} 个子菜单或按钮，无法直接删除。
      </span>
      <span className="block text-xs text-muted-foreground">
        请先删除或转移其所有下级菜单和按钮节点后，再执行删除操作。
      </span>
    </span>
  ) : (
    `确定要删除菜单「${shownMenu?.title ?? ''}」吗？删除后配置将立即失效，此操作不可撤回。`
  )

  return (
    <DeleteConfirmDialog
      open={open}
      onClose={onClose}
      title="删除菜单"
      description={description}
      disabled={!menu}
      showConfirm={!hasChildren}
      action={async () => {
        if (menu && !hasChildren) await deleteMenu(menu.id)
      }}
      invalidateKeys={[menuKeys.trees()]}
      errorMessage="删除失败，请重试"
      successMessage={menu ? `菜单「${menu.title}」已删除` : null}
    />
  )
}
