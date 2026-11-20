import { FileText } from 'lucide-react'

import { DetailField, DetailGrid } from '@/components/detail-field'
import { EntityDrawer } from '@/components/entity-drawer'
import { QueryState } from '@/components/query-state'
import { Skeleton } from '@/components/ui/skeleton'
import { EMPTY_PLACEHOLDER } from '@/lib/constants'

import { NoticeMethodTags, NoticeStatusBadge, NoticeTypeBadge, noticeOptionLabel } from './notice-display'
import { useNoticeDetail } from './use-notice-detail'
import { useNoticeDicts } from './use-notice-dicts'

function NoticeDetailSkeleton() {
  return (
    <DetailGrid>
      {Array.from({ length: 13 }, (_, index) => (
        <div key={index} className="flex flex-col gap-1">
          <Skeleton className="h-3.5 w-12" />
          <Skeleton className="h-5 w-24" />
        </div>
      ))}
    </DetailGrid>
  )
}

function TextDetail({ label, value, className }: { label: string; value?: string | null; className?: string }) {
  return (
    <DetailField label={label} className={className}>
      {value || EMPTY_PLACEHOLDER}
    </DetailField>
  )
}

interface NoticeDetailDrawerProps {
  /** The notice to show, or `null` before the first one is picked. */
  readonly noticeId: string | number | null
  readonly open: boolean
  readonly onClose: () => void
}

/** The Vue DetailDrawer's fields, laid out as a two-column drawer. */
export function NoticeDetailDrawer({ noticeId, open, onClose }: NoticeDetailDrawerProps) {
  const dicts = useNoticeDicts()
  const query = useNoticeDetail(noticeId, { enabled: open })
  const detail = query.data

  return (
    <EntityDrawer open={open} onClose={onClose} title="公告详情" icon={<FileText className="size-4" />} width={40}>
      <div className="min-h-0 flex-1 overflow-y-auto overscroll-contain p-5">
        {query.isPending ? (
          <NoticeDetailSkeleton />
        ) : query.isError || !detail ? (
          <QueryState state="error" onRetry={() => void query.refetch()} />
        ) : (
          <DetailGrid>
            <DetailField label="ID" className="sm:col-span-2">
              {detail.id}
            </DetailField>
            <TextDetail label="标题" value={detail.title} />
            <DetailField label="分类">
              <NoticeTypeBadge type={detail.type} options={dicts.typeOptions} />
            </DetailField>
            <DetailField label="通知范围">{noticeOptionLabel(dicts.scopeOptions, detail.noticeScope)}</DetailField>
            <DetailField label="通知方式">
              <NoticeMethodTags methods={detail.noticeMethods} options={dicts.methodOptions} />
            </DetailField>
            <DetailField label="是否定时">{detail.isTiming ? '是' : '否'}</DetailField>
            <TextDetail label="发布时间" value={detail.publishTime} />
            <DetailField label="是否置顶">{detail.isTop ? '是' : '否'}</DetailField>
            <DetailField label="状态">
              <NoticeStatusBadge status={detail.status} options={dicts.statusOptions} />
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
