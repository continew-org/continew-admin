import { Building2 } from 'lucide-react'

import { CopyableText } from '@/components/copyable-text'
import { DetailField, DetailGrid } from '@/components/detail-field'
import { EntityDrawer } from '@/components/entity-drawer'
import { QueryState } from '@/components/query-state'
import { StatusCell } from '@/components/status-cell'
import { Skeleton } from '@/components/ui/skeleton'
import { useLastRecord } from '@/hooks/use-last-record'
import { EMPTY_PLACEHOLDER } from '@/lib/constants'

import { DomainCell, ExpireTimeCell } from './tenant-display'
import { useTenantDetail } from './use-tenant-detail'

function TenantDetailSkeleton() {
  return (
    <div className="grid grid-cols-1 gap-x-6 gap-y-4 sm:grid-cols-2">
      {Array.from({ length: 12 }, (_, index) => (
        <div key={index} className="flex flex-col gap-1">
          <Skeleton className="h-3.5 w-14" />
          <Skeleton className="h-5 w-28" />
        </div>
      ))}
    </div>
  )
}

function TextDetail({ label, value, className }: { label: string; value: string | null; className?: string }) {
  return (
    <DetailField label={label} className={className}>
      {value || EMPTY_PLACEHOLDER}
    </DetailField>
  )
}

interface TenantDetailDrawerProps {
  /** The tenant to show, or `null` before the first one is picked. */
  readonly tenantId: string | number | null
  readonly open: boolean
  readonly onClose: () => void
}

/** Read-only detail panel for one tenant, with the field order of the Vue client's drawer. */
export function TenantDetailDrawer({ tenantId, open, onClose }: TenantDetailDrawerProps) {
  const query = useTenantDetail(tenantId, { enabled: open })

  // Keep the last detail during the close animation; the query key becomes null first.
  const lastDetail = useLastRecord(query.data ?? null)
  const detail = query.data ?? (open ? null : lastDetail)

  return (
    <EntityDrawer open={open} onClose={onClose} title="租户详情" icon={<Building2 className="size-4" />} width={36}>
      <div className="min-h-0 flex-1 overflow-y-auto overscroll-contain p-4">
        {!detail && query.isPending ? (
          <TenantDetailSkeleton />
        ) : query.isError || !detail ? (
          <QueryState state="error" onRetry={() => void query.refetch()} />
        ) : (
          <DetailGrid>
            <DetailField label="ID">
              <CopyableText value={String(detail.id)} />
            </DetailField>
            <DetailField label="编码">
              <CopyableText value={detail.code} />
            </DetailField>
            <TextDetail label="名称" value={detail.name} />
            <TextDetail label="套餐" value={detail.packageName} />
            <DetailField label="域名">
              <DomainCell domain={detail.domain} />
            </DetailField>
            <DetailField label="过期时间">
              <ExpireTimeCell value={detail.expireTime} />
            </DetailField>
            <TextDetail label="管理员用户" value={detail.adminUsername} />
            <DetailField label="状态">
              <StatusCell status={detail.status} />
            </DetailField>
            <TextDetail label="创建人" value={detail.createUserString} />
            <TextDetail label="创建时间" value={detail.createTime} />
            <TextDetail label="修改人" value={detail.updateUserString} />
            <TextDetail label="修改时间" value={detail.updateTime} />
            <TextDetail label="描述" value={detail.description} className="sm:col-span-2" />
          </DetailGrid>
        )}
      </div>
    </EntityDrawer>
  )
}
