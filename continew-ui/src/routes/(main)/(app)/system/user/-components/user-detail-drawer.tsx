import { Eye } from 'lucide-react'

import { CopyableText } from '@/components/copyable-text'
import { TagList } from '@/components/data-badge'
import { DetailField, DetailGrid } from '@/components/detail-field'
import { DetailGate } from '@/components/detail-gate'
import { EntityDrawer } from '@/components/entity-drawer'
import { StatusCell } from '@/components/status-cell'
import { Skeleton } from '@/components/ui/skeleton'
import { useLastRecord } from '@/hooks/use-last-record'
import { EMPTY_PLACEHOLDER } from '@/lib/constants'

import { genderLabel } from './user-display'
import { useUserDetail } from './use-user-detail'

function UserDetailSkeleton() {
  return (
    <DetailGrid>
      <div className="flex flex-col gap-1 sm:col-span-2">
        <Skeleton className="h-3.5 w-10" />
        <Skeleton className="h-5 w-48" />
      </div>
      <div className="flex flex-col gap-1">
        <Skeleton className="h-3.5 w-12" />
        <Skeleton className="h-5 w-28" />
      </div>
      <div className="flex flex-col gap-1">
        <Skeleton className="h-3.5 w-12" />
        <Skeleton className="h-5 w-24" />
      </div>
      <div className="flex flex-col gap-1">
        <Skeleton className="h-3.5 w-10" />
        <Skeleton className="h-5 w-16" />
      </div>
      <div className="flex flex-col gap-1">
        <Skeleton className="h-3.5 w-10" />
        <Skeleton className="h-5 w-16 rounded-full" />
      </div>
      <div className="flex flex-col gap-1">
        <Skeleton className="h-3.5 w-12" />
        <Skeleton className="h-5 w-32" />
      </div>
      <div className="flex flex-col gap-1">
        <Skeleton className="h-3.5 w-10" />
        <Skeleton className="h-5 w-36" />
      </div>
      <div className="flex flex-col gap-1">
        <Skeleton className="h-3.5 w-16" />
        <Skeleton className="h-5 w-28" />
      </div>
      <div className="flex flex-col gap-1">
        <Skeleton className="h-3.5 w-10" />
        <Skeleton className="h-5 w-20" />
      </div>
      <div className="flex flex-col gap-1 sm:col-span-2">
        <Skeleton className="h-3.5 w-10" />
        <Skeleton className="h-8 w-full" />
      </div>
      <div className="flex flex-col gap-1">
        <Skeleton className="h-3.5 w-14" />
        <Skeleton className="h-5 w-28" />
      </div>
      <div className="flex flex-col gap-1">
        <Skeleton className="h-3.5 w-16" />
        <Skeleton className="h-5 w-36" />
      </div>
      <div className="flex flex-col gap-1">
        <Skeleton className="h-3.5 w-14" />
        <Skeleton className="h-5 w-28" />
      </div>
      <div className="flex flex-col gap-1">
        <Skeleton className="h-3.5 w-16" />
        <Skeleton className="h-5 w-36" />
      </div>
    </DetailGrid>
  )
}

function TextDetail({ label, value, className }: { label: string; value: string | null; className?: string }) {
  return (
    <DetailField label={label} className={className}>
      {value || EMPTY_PLACEHOLDER}
    </DetailField>
  )
}

interface UserDetailDrawerProps {
  /** The user to show, or `null` before the first one is picked. */
  readonly userId: string | number | null
  readonly open: boolean
  readonly onClose: () => void
}

export function UserDetailDrawer({ userId, open, onClose }: UserDetailDrawerProps) {
  // The read-only drawer can reuse the short detail cache; edit and role flows force a fresh read.
  const query = useUserDetail(userId, { enabled: open })

  // Keep the last detail during the close animation; the query key becomes null first.
  const lastDetail = useLastRecord(query.data ?? null)
  const detail = query.data ?? (open ? null : lastDetail)

  return (
    <EntityDrawer open={open} onClose={onClose} title="用户详情" icon={<Eye className="size-4" />}>
      <div className="min-h-0 flex-1 overflow-y-auto overscroll-contain p-4">
        <DetailGate
          query={query}
          data={detail}
          requireFresh={false}
          errorTitle="用户加载失败"
          skeleton={<UserDetailSkeleton />}
        >
          {record => (
            <DetailGrid>
              <DetailField label="ID" className="sm:col-span-2">
                <CopyableText value={String(record.id)} label="复制 ID" />
              </DetailField>
              <DetailField label="用户名">{record.username}</DetailField>
              <DetailField label="昵称">{record.nickname}</DetailField>
              <DetailField label="性别">{genderLabel(record.gender)}</DetailField>
              <DetailField label="状态">
                <StatusCell status={record.status} />
              </DetailField>
              <TextDetail label="手机号" value={record.phone} />
              <TextDetail label="邮箱" value={record.email} />
              <TextDetail label="所属部门" value={record.deptName} />
              <DetailField label="角色">
                <TagList items={record.roleNames} />
              </DetailField>
              <TextDetail label="描述" value={record.description} className="sm:col-span-2" />
              <TextDetail label="创建人" value={record.createUserString} />
              <TextDetail label="修改人" value={record.updateUserString} />
              <TextDetail label="创建时间" value={record.createTime} />
              <TextDetail label="修改时间" value={record.updateTime} />
            </DetailGrid>
          )}
        </DetailGate>
      </div>
    </EntityDrawer>
  )
}
