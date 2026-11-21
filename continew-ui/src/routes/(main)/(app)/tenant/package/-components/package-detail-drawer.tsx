import { FolderTree } from 'lucide-react'

import { CopyableText } from '@/components/copyable-text'
import { DetailField, DetailGrid } from '@/components/detail-field'
import { EntityDrawer } from '@/components/entity-drawer'
import { QueryState } from '@/components/query-state'
import { StatusCell } from '@/components/status-cell'
import { Skeleton } from '@/components/ui/skeleton'
import { Tree } from '@/components/ui/tree'
import { EMPTY_PLACEHOLDER } from '@/lib/constants'

import { toMenuTreeNodes, usePackageMenuTree } from './use-package-menu-tree'
import { usePackageDetail } from './use-package-detail'

function PackageDetailSkeleton() {
  return (
    <div className="flex flex-col gap-6">
      <div className="grid grid-cols-1 gap-x-6 gap-y-4 sm:grid-cols-2">
        {Array.from({ length: 10 }, (_, index) => (
          <div key={index} className="flex flex-col gap-1">
            <Skeleton className="h-3.5 w-14" />
            <Skeleton className="h-5 w-28" />
          </div>
        ))}
      </div>
      <Skeleton className="h-48 w-full" />
    </div>
  )
}

interface PackageDetailDrawerProps {
  /** The package to show, or `null` before the first one is picked. */
  readonly packageId: string | number | null
  readonly open: boolean
  readonly onClose: () => void
}

/** Read-only detail panel for one package, with its menu grants shown as a checked tree. */
export function PackageDetailDrawer({ packageId, open, onClose }: PackageDetailDrawerProps) {
  const detail = usePackageDetail(packageId, { enabled: open })
  const menuTree = usePackageMenuTree(open)

  const treeNodes = toMenuTreeNodes(menuTree.data ?? [])
  const pkg = detail.data
  const isLoading = detail.isPending || menuTree.isPending

  return (
    <EntityDrawer open={open} onClose={onClose} title="套餐详情" icon={<FolderTree className="size-4" />} width={40}>
      <div className="min-h-0 flex-1 overflow-y-auto overscroll-contain p-4">
        {isLoading ? (
          <PackageDetailSkeleton />
        ) : detail.isError || !pkg ? (
          <QueryState state="error" onRetry={() => void detail.refetch()} />
        ) : (
          <div className="flex flex-col gap-6">
            <section>
              <h3 className="mb-3 text-sm font-medium text-foreground">基础信息</h3>
              <DetailGrid>
                <DetailField label="ID" className="sm:col-span-2">
                  <CopyableText value={String(pkg.id)} />
                </DetailField>
                <DetailField label="名称">{pkg.name || EMPTY_PLACEHOLDER}</DetailField>
                <DetailField label="排序">{pkg.sort ?? EMPTY_PLACEHOLDER}</DetailField>
                <DetailField label="状态">
                  <StatusCell status={pkg.status} />
                </DetailField>
                <div />
                <DetailField label="创建人">{pkg.createUserString || EMPTY_PLACEHOLDER}</DetailField>
                <DetailField label="创建时间">{pkg.createTime || EMPTY_PLACEHOLDER}</DetailField>
                <DetailField label="修改人">{pkg.updateUserString || EMPTY_PLACEHOLDER}</DetailField>
                <DetailField label="修改时间">{pkg.updateTime || EMPTY_PLACEHOLDER}</DetailField>
                <DetailField label="描述" className="sm:col-span-2">
                  {pkg.description || EMPTY_PLACEHOLDER}
                </DetailField>
              </DetailGrid>
            </section>

            <section>
              <h3 className="mb-3 text-sm font-medium text-foreground">关联菜单</h3>
              <div className="max-h-96 overflow-auto overscroll-contain rounded-md border bg-background/50 p-1.5">
                <Tree
                  data={treeNodes}
                  checkable
                  checkedKeys={(pkg.menuIds ?? []).map(String)}
                  defaultExpandAll
                  emptyText="该套餐尚未关联菜单"
                />
              </div>
            </section>
          </div>
        )}
      </div>
    </EntityDrawer>
  )
}
