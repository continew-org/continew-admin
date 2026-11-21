import { DeleteConfirmDialog } from '@/components/delete-confirm-dialog'
import { useLastRecord } from '@/hooks/use-last-record'
import { deleteTenant, tenantKeys, type TenantResp } from '@/lib/api/tenant'

interface TenantDeleteDialogProps {
  /** The tenant being deleted, or `null` before the first one is picked. */
  readonly tenant: TenantResp | null
  readonly open: boolean
  readonly onClose: () => void
}

/** Confirms deleting one tenant; the request also clears every row it owns. */
export function TenantDeleteDialog({ tenant, open, onClose }: TenantDeleteDialogProps) {
  const shownTenant = useLastRecord(tenant)

  return (
    <DeleteConfirmDialog
      open={open}
      onClose={onClose}
      title="删除租户"
      description={
        shownTenant
          ? `是否确定删除租户「${shownTenant.name}（${shownTenant.code}）」？该租户的数据会一并清除，删除后不可恢复`
          : '是否确定删除该租户？删除后不可恢复'
      }
      disabled={!tenant}
      action={async () => {
        if (tenant) await deleteTenant(tenant.id)
      }}
      invalidateKeys={[tenantKeys.all]}
    />
  )
}
