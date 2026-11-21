import { DeleteConfirmDialog } from '@/components/delete-confirm-dialog'
import { useLastRecord } from '@/hooks/use-last-record'
import { deletePackage, packageKeys, type PackageResp } from '@/lib/api/package'
import { tenantKeys } from '@/lib/api/tenant'

interface PackageDeleteDialogProps {
  /** The package being deleted, or `null` before the first one is picked. */
  readonly pkg: PackageResp | null
  readonly open: boolean
  readonly onClose: () => void
}

/** Confirms deleting one package; the backend rejects one that still has tenants. */
export function PackageDeleteDialog({ pkg, open, onClose }: PackageDeleteDialogProps) {
  const shownPackage = useLastRecord(pkg)

  return (
    <DeleteConfirmDialog
      open={open}
      onClose={onClose}
      title="删除套餐"
      description={
        shownPackage ? `是否确定删除套餐「${shownPackage.name}」？删除后不可恢复` : '是否确定删除该套餐？删除后不可恢复'
      }
      disabled={!pkg}
      action={async () => {
        if (pkg) await deletePackage(pkg.id)
      }}
      // The tenant list shows the package name, so it goes stale with the package.
      invalidateKeys={[packageKeys.all, tenantKeys.all]}
    />
  )
}
