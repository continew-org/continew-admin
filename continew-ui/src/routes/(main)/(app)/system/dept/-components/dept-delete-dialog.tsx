import { DeleteConfirmDialog } from '@/components/delete-confirm-dialog'
import { useLastRecord } from '@/hooks/use-last-record'
import { deleteDept, deptKeys, type DeptResp } from '@/lib/api/dept'

interface DeptDeleteDialogProps {
  readonly dept: DeptResp | null
  readonly open: boolean
  readonly onClose: () => void
}

export function DeptDeleteDialog({ dept, open, onClose }: DeptDeleteDialogProps) {
  const shownDept = useLastRecord(dept)

  const childCount = shownDept?.children?.length ?? 0
  const hasChildren = childCount > 0
  const isSystem = Boolean(shownDept?.isSystem)
  const cannotDelete = hasChildren || isSystem

  const description = isSystem ? (
    <span className="block space-y-1">
      <span className="font-medium text-destructive">部门「{shownDept?.name}」是系统内置部门，无法删除。</span>
      <span className="block text-xs text-muted-foreground">内置组织机构用于保障系统基础业务逻辑运转。</span>
    </span>
  ) : hasChildren ? (
    <span className="block space-y-1">
      <span className="font-medium text-destructive">
        部门「{shownDept?.name}」下包含 {childCount} 个下级部门，无法直接删除。
      </span>
      <span className="block text-xs text-muted-foreground">请先删除或转移其所有下级部门节点后，再执行删除操作。</span>
    </span>
  ) : (
    `确定要删除部门「${shownDept?.name ?? ''}」吗？删除后该部门将从组织架构中移除，此操作不可撤回。`
  )

  return (
    <DeleteConfirmDialog
      open={open}
      onClose={onClose}
      title="删除部门"
      description={description}
      disabled={!dept}
      showConfirm={!cannotDelete}
      action={async () => {
        if (dept && !cannotDelete) await deleteDept(dept.id)
      }}
      invalidateKeys={[deptKeys.all]}
      errorMessage="删除失败，请重试"
      successMessage={dept ? `部门「${dept.name}」已删除` : null}
    />
  )
}
