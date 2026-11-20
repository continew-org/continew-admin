import { Eye } from 'lucide-react'

import { EntityDrawer } from '@/components/entity-drawer'
import { QueryState } from '@/components/query-state'
import { RichTextContent } from '@/components/rich-text-editor'
import { Badge } from '@/components/ui/badge'
import { Separator } from '@/components/ui/separator'
import { Skeleton } from '@/components/ui/skeleton'
import { EMPTY_PLACEHOLDER } from '@/lib/constants'

import { useNoticeDetail } from './use-notice-detail'

interface NoticeViewDrawerProps {
  readonly noticeId: string | number | null
  readonly open: boolean
  readonly onClose: () => void
}

/** The reader view: title, publisher, publish time, then the body. */
export function NoticeViewDrawer({ noticeId, open, onClose }: NoticeViewDrawerProps) {
  const detailQuery = useNoticeDetail(noticeId, { enabled: open })
  const detail = detailQuery.data

  return (
    <EntityDrawer open={open} onClose={onClose} title="查看公告" icon={<Eye className="size-4" />} width={44}>
      <div className="min-h-0 flex-1 overflow-y-auto overscroll-contain p-5">
        {detailQuery.isPending ? (
          <div className="flex flex-col gap-3">
            <Skeleton className="h-7 w-2/3" />
            <Skeleton className="h-3 w-28" />
            <Skeleton className="h-3 w-32" />
            <Skeleton className="h-px w-full" />
            <Skeleton className="h-4 w-full" />
            <Skeleton className="h-4 w-full" />
            <Skeleton className="h-4 w-4/5" />
          </div>
        ) : detailQuery.isError || !detail ? (
          <QueryState state="error" onRetry={() => void detailQuery.refetch()} />
        ) : (
          <article className="flex flex-col gap-4">
            <div className="flex flex-col gap-1.5">
              <div className="flex items-start gap-2">
                <h2 className="min-w-0 flex-1 text-xl leading-snug font-semibold">{detail.title}</h2>
                {detail.isTop ? (
                  <Badge variant="secondary" className="shrink-0 font-normal">
                    置顶
                  </Badge>
                ) : null}
              </div>
              <p className="text-xs text-muted-foreground">{detail.createUserString || EMPTY_PLACEHOLDER}</p>
              <p className="text-xs text-muted-foreground">
                {detail.updateTime || detail.createTime || EMPTY_PLACEHOLDER}
              </p>
            </div>

            <Separator />

            <RichTextContent html={detail.content} />
          </article>
        )}
      </div>
    </EntityDrawer>
  )
}
