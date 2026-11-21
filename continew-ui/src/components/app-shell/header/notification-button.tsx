import * as React from 'react'

import { useQuery, useQueryClient } from '@tanstack/react-query'
import { Link } from '@tanstack/react-router'
import { Bell, CheckCheck } from 'lucide-react'

import { cn } from 'cn'

import { Button } from '@/components/ui/button'
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Tooltip, TooltipContent, TooltipTrigger } from '@/components/ui/tooltip'
import { listMessages, listUserNotices, readAllMessages } from '@/lib/api/message'
import { reportError } from '@/lib/http'
import { messageKeys } from '@/lib/query-keys'
import { NoticeViewDrawer } from '@/routes/(main)/(app)/system/notice/-components/notice-view-drawer'
import { MessageDetailDrawer } from '@/routes/(main)/(app)/user/message/-components/message-detail-drawer'
import { MessageTypeBadge, UnreadDot } from '@/routes/(main)/(app)/user/message/-components/message-display'
import { useUnreadCounts } from '@/routes/(main)/(app)/user/message/-components/use-messages'

const RECENT_SIZE = 5

function RecentRow({
  unread,
  title,
  meta,
  time,
  onClick
}: {
  unread: boolean
  title: string
  meta: React.ReactNode
  time?: string | null
  onClick: () => void
}) {
  return (
    <button
      type="button"
      onClick={onClick}
      className="flex w-full items-center gap-2 rounded-md px-3 py-2 text-left text-sm hover:bg-accent"
    >
      <UnreadDot unread={unread} />
      <span className={cn('min-w-0 flex-1 truncate', unread ? 'font-medium' : 'text-muted-foreground')}>{title}</span>
      {meta}
      {/* `2026-11-20 14:02:33` → `11-20 14:02`: enough for a row, and no time-zone parsing. */}
      <span className="shrink-0 text-xs text-muted-foreground tabular-nums">{time?.slice(5, 16)}</span>
    </button>
  )
}

function RecentState({
  isPending,
  isError,
  isEmpty,
  emptyText
}: Record<'isPending' | 'isError' | 'isEmpty', boolean> & { emptyText: string }) {
  const text = isPending ? '加载中…' : isError ? '加载失败' : isEmpty ? emptyText : null
  return text ? <p className="px-3 py-6 text-center text-xs text-muted-foreground">{text}</p> : null
}

/**
 * Notification bell: unread messages plus unread notices on the badge, and the latest of each in
 * the popover. The lists only load while it is open; the badge follows the message socket.
 */
export function NotificationButton() {
  const queryClient = useQueryClient()
  const [open, setOpen] = React.useState(false)
  const [tab, setTab] = React.useState<'message' | 'notice'>('message')
  const [messageId, setMessageId] = React.useState<string | number | null>(null)
  const [noticeId, setNoticeId] = React.useState<string | number | null>(null)
  const [isReading, setIsReading] = React.useState(false)
  const { messageCount, noticeCount, total } = useUnreadCounts()
  const messages = useQuery({
    queryKey: messageKeys.recent(),
    queryFn: ({ signal }) => listMessages({ page: 1, size: RECENT_SIZE, sort: 'createTime,desc' }, signal),
    enabled: open && tab === 'message'
  })
  const notices = useQuery({
    queryKey: messageKeys.recentNotices(),
    queryFn: ({ signal }) => listUserNotices({ page: 1, size: RECENT_SIZE, sort: 'publishTime,desc' }, signal),
    enabled: open && tab === 'notice'
  })
  const label = total > 0 ? `通知（${total} 条未读）` : '通知'

  const readAll = async () => {
    setIsReading(true)
    try {
      await readAllMessages()
      void queryClient.invalidateQueries({ queryKey: messageKeys.unread() })
      void queryClient.invalidateQueries({ queryKey: messageKeys.lists() })
    } catch (error) {
      reportError(error)
    } finally {
      setIsReading(false)
    }
  }

  return (
    <>
      <Popover open={open} onOpenChange={setOpen}>
        <Tooltip>
          <TooltipTrigger
            render={
              <PopoverTrigger
                render={<Button variant="ghost" size="icon-sm" aria-label={label} className="relative" />}
              />
            }
          >
            <Bell />
            {total > 0 && (
              <span className="absolute top-0.5 right-0.5 flex h-3.5 min-w-3.5 items-center justify-center rounded-full bg-destructive px-1 text-[10px] leading-none font-medium text-white tabular-nums">
                {total > 99 ? '99+' : total}
              </span>
            )}
          </TooltipTrigger>
          <TooltipContent side="bottom" sideOffset={8}>
            {label}
          </TooltipContent>
        </Tooltip>
        <PopoverContent align="end" className="w-96 p-0">
          <Tabs value={tab} onValueChange={value => setTab(value as 'message' | 'notice')} className="gap-0">
            <TabsList variant="line" className="w-full justify-start gap-4 border-b px-3">
              <TabsTrigger value="message" className="flex-none">
                消息{messageCount > 0 ? ` ${messageCount}` : ''}
              </TabsTrigger>
              <TabsTrigger value="notice" className="flex-none">
                公告{noticeCount > 0 ? ` ${noticeCount}` : ''}
              </TabsTrigger>
            </TabsList>
            <TabsContent value="message" className="max-h-80 overflow-y-auto p-1">
              <RecentState
                isPending={messages.isPending}
                isError={messages.isError}
                isEmpty={messages.data?.list.length === 0}
                emptyText="暂无消息"
              />
              {messages.data?.list.map(message => (
                <RecentRow
                  key={message.id}
                  unread={!message.isRead}
                  title={message.title}
                  meta={<MessageTypeBadge type={message.type} />}
                  time={message.createTime}
                  onClick={() => {
                    setOpen(false)
                    setMessageId(message.id)
                  }}
                />
              ))}
            </TabsContent>
            <TabsContent value="notice" className="max-h-80 overflow-y-auto p-1">
              <RecentState
                isPending={notices.isPending}
                isError={notices.isError}
                isEmpty={notices.data?.list.length === 0}
                emptyText="暂无公告"
              />
              {notices.data?.list.map(notice => (
                <RecentRow
                  key={notice.id}
                  unread={!notice.isRead}
                  title={notice.title}
                  meta={notice.isTop ? <span className="shrink-0 text-xs text-primary">置顶</span> : null}
                  time={notice.publishTime}
                  onClick={() => {
                    setOpen(false)
                    setNoticeId(notice.id)
                  }}
                />
              ))}
            </TabsContent>
          </Tabs>
          <div className="flex items-center justify-between border-t px-3 py-2">
            {tab === 'message' ? (
              <Button
                variant="ghost"
                size="sm"
                disabled={messageCount === 0 || isReading}
                onClick={() => void readAll()}
              >
                <CheckCheck />
                全部已读
              </Button>
            ) : (
              <span />
            )}
            <Button
              variant="ghost"
              size="sm"
              render={<Link to="/user/message" search={{ tab }} />}
              onClick={() => setOpen(false)}
            >
              查看全部
            </Button>
          </div>
        </PopoverContent>
      </Popover>
      <MessageDetailDrawer messageId={messageId} open={messageId !== null} onClose={() => setMessageId(null)} />
      <NoticeViewDrawer noticeId={noticeId} open={noticeId !== null} onClose={() => setNoticeId(null)} source="self" />
    </>
  )
}
