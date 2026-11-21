import * as React from 'react'

import { useNavigate } from '@tanstack/react-router'
import { ArrowRight, Mail } from 'lucide-react'

import { EntityDrawer } from '@/components/entity-drawer'
import { QueryState } from '@/components/query-state'
import { RichTextContent } from '@/components/rich-text-editor'
import { Button } from '@/components/ui/button'
import { DrawerFooter } from '@/components/ui/drawer'
import { Skeleton } from '@/components/ui/skeleton'
import { NoticeViewDrawer } from '@/routes/(main)/(app)/system/notice/-components/notice-view-drawer'

import { MessageTypeBadge } from './message-display'
import { useMessageDetail } from './use-messages'

/**
 * The backend message templates (`MessageTemplateEnum`) link to the Vue admin's routes: its
 * notice viewer becomes the notice drawer here and its profile page lives under the dashboard.
 * Any other in-app path is followed as is; other origins are never offered.
 */
function resolveTarget(path: string | null | undefined): { noticeId: string } | { href: string } | null {
  const noticeId = path?.match(/^\/user\/notice\?id=([^&]+)$/)?.[1]
  if (noticeId) return { noticeId }
  if (path === '/user/profile') return { href: '/dashboard/profile' }
  return path?.startsWith('/') && !path.startsWith('//') ? { href: path } : null
}

interface MessageDetailDrawerProps {
  readonly messageId: string | number | null
  readonly open: boolean
  readonly onClose: () => void
}

/** One message; opening it marks it read. A message with a `path` offers to go there. */
export function MessageDetailDrawer({ messageId, open, onClose }: MessageDetailDrawerProps) {
  const navigate = useNavigate()
  const query = useMessageDetail(messageId, { enabled: open })
  const detail = query.data
  const target = resolveTarget(detail?.path)
  const [noticeId, setNoticeId] = React.useState<string | null>(null)

  return (
    <>
      <EntityDrawer open={open} onClose={onClose} title="消息详情" icon={<Mail className="size-4" />} width={40}>
        <div className="min-h-0 flex-1 overflow-y-auto overscroll-contain p-5">
          {query.isPending ? (
            <div className="flex flex-col gap-3">
              <Skeleton className="h-6 w-2/3" />
              <Skeleton className="h-3 w-40" />
              <Skeleton className="h-px w-full" />
              <Skeleton className="h-4 w-full" />
              <Skeleton className="h-4 w-4/5" />
            </div>
          ) : query.isError || !detail ? (
            <QueryState state="error" onRetry={() => void query.refetch()} />
          ) : (
            <article className="flex flex-col gap-4">
              <header className="flex flex-col gap-2 border-b pb-4">
                <h2 className="text-lg font-semibold">{detail.title}</h2>
                <div className="flex items-center gap-2 text-xs text-muted-foreground">
                  <MessageTypeBadge type={detail.type} />
                  <span className="tabular-nums">{detail.createTime}</span>
                </div>
              </header>
              <RichTextContent html={detail.content} />
            </article>
          )}
        </div>

        <DrawerFooter>
          {target && (
            <Button
              onClick={() => {
                onClose()
                if ('noticeId' in target) setNoticeId(target.noticeId)
                else void navigate({ href: target.href })
              }}
            >
              {'noticeId' in target ? '查看公告' : '前往查看'}
              <ArrowRight />
            </Button>
          )}
          <Button variant="outline" onClick={onClose}>
            关闭
          </Button>
        </DrawerFooter>
      </EntityDrawer>

      <NoticeViewDrawer noticeId={noticeId} open={noticeId !== null} onClose={() => setNoticeId(null)} source="self" />
    </>
  )
}
