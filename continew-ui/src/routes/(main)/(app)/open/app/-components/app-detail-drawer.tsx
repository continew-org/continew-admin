import { LayoutGrid } from 'lucide-react'

import { CopyableText } from '@/components/copyable-text'
import { DetailField, DetailGrid } from '@/components/detail-field'
import { EntityDrawer } from '@/components/entity-drawer'
import { QueryState } from '@/components/query-state'
import { StatusCell } from '@/components/status-cell'
import { Skeleton } from '@/components/ui/skeleton'
import { useLastRecord } from '@/hooks/use-last-record'
import { EMPTY_PLACEHOLDER } from '@/lib/constants'

import { ExpireTimeCell } from './app-display'
import { useAppDetail } from './use-app-detail'

function AppDetailSkeleton() {
  return (
    <div className="grid grid-cols-1 gap-x-6 gap-y-4 sm:grid-cols-2">
      <div className="flex flex-col gap-1 sm:col-span-2">
        <Skeleton className="h-3.5 w-10" />
        <Skeleton className="h-5 w-48" />
      </div>
      <div className="flex flex-col gap-1 sm:col-span-2">
        <Skeleton className="h-3.5 w-16" />
        <Skeleton className="h-5 w-40" />
      </div>
      <div className="flex flex-col gap-1 sm:col-span-2">
        <Skeleton className="h-3.5 w-20" />
        <Skeleton className="h-5 w-60" />
      </div>
      <div className="flex flex-col gap-1">
        <Skeleton className="h-3.5 w-10" />
        <Skeleton className="h-5 w-16 rounded-full" />
      </div>
      <div className="flex flex-col gap-1">
        <Skeleton className="h-3.5 w-14" />
        <Skeleton className="h-5 w-28" />
      </div>
      <div className="flex flex-col gap-1 sm:col-span-2">
        <Skeleton className="h-3.5 w-10" />
        <Skeleton className="h-12 w-full" />
      </div>
      <div className="flex flex-col gap-1">
        <Skeleton className="h-3.5 w-14" />
        <Skeleton className="h-5 w-24" />
      </div>
      <div className="flex flex-col gap-1">
        <Skeleton className="h-3.5 w-16" />
        <Skeleton className="h-5 w-36" />
      </div>
      <div className="flex flex-col gap-1">
        <Skeleton className="h-3.5 w-14" />
        <Skeleton className="h-5 w-24" />
      </div>
      <div className="flex flex-col gap-1">
        <Skeleton className="h-3.5 w-16" />
        <Skeleton className="h-5 w-36" />
      </div>
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

interface AppDetailDrawerProps {
  /** The app to show, or `null` before the first one is picked. */
  readonly appId: string | number | null
  readonly open: boolean
  readonly onClose: () => void
}

/** Read-only app detail panel. */
export function AppDetailDrawer({ appId, open, onClose }: AppDetailDrawerProps) {
  const query = useAppDetail(appId, { enabled: open })

  // Keep the last detail during the close animation; the query key becomes null first.
  const lastDetail = useLastRecord(query.data ?? null)
  const detail = query.data ?? (open ? null : lastDetail)

  return (
    <EntityDrawer open={open} onClose={onClose} title="应用详情" icon={<LayoutGrid className="size-4" />} width={32}>
      <div className="min-h-0 flex-1 overflow-y-auto overscroll-contain p-4">
        {!detail && query.isPending ? (
          <AppDetailSkeleton />
        ) : query.isError || !detail ? (
          <QueryState state="error" onRetry={() => void query.refetch()} />
        ) : (
          <DetailGrid>
            <DetailField label="应用 ID" className="sm:col-span-2">
              <CopyableText value={String(detail.id)} label="复制 ID" className="break-all" />
            </DetailField>

            <DetailField label="应用名称" className="sm:col-span-2">
              <span className="font-medium text-foreground">{detail.name}</span>
            </DetailField>

            <DetailField label="Access Key" className="sm:col-span-2">
              <CopyableText
                value={detail.accessKey}
                label="复制 Access Key"
                successMessage="Access Key 已复制到剪贴板"
                className="break-all"
              />
            </DetailField>

            <DetailField label="状态">
              <StatusCell status={detail.status} />
            </DetailField>

            <DetailField label="失效时间">
              <ExpireTimeCell expireTime={detail.expireTime} />
            </DetailField>

            <DetailField label="应用描述" className="sm:col-span-2">
              <span className="text-sm whitespace-pre-wrap text-muted-foreground">
                {detail.description || EMPTY_PLACEHOLDER}
              </span>
            </DetailField>

            <TextDetail label="创建人" value={detail.createUserString} />
            <TextDetail label="创建时间" value={detail.createTime} />
            <TextDetail label="修改人" value={detail.updateUserString} />
            <TextDetail label="修改时间" value={detail.updateTime} />
          </DetailGrid>
        )}
      </div>
    </EntityDrawer>
  )
}
