import * as React from 'react'

import { createFileRoute, useNavigate } from '@tanstack/react-router'
import { Bell, Mail } from 'lucide-react'
import { z } from 'zod'

import { cn } from 'cn'

import { Badge } from '@/components/ui/badge'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { useIsMobile } from '@/hooks/use-mobile'

import { MessageList } from './-components/message-list'
import { NoticeList } from './-components/notice-list'
import { useUnreadCounts } from './-components/use-messages'

const searchSchema = z.object({
  tab: z.enum(['message', 'notice']).optional()
})

export const Route = createFileRoute('/(main)/(app)/user/message')({
  validateSearch: searchSchema,
  component: MessageCenterPage
})

function MessageCenterPage() {
  const { tab = 'message' } = Route.useSearch()
  const navigate = useNavigate({ from: Route.fullPath })
  const isMobile = useIsMobile()
  const { messageCount, noticeCount } = useUnreadCounts()
  const tabs = [
    { value: 'message', label: '我的消息', icon: <Mail />, count: messageCount },
    { value: 'notice', label: '我的公告', icon: <Bell />, count: noticeCount }
  ] as const

  // Keep a visited tab mounted so switching back keeps its filters and page.
  const [visited, setVisited] = React.useState<ReadonlySet<string>>(() => new Set([tab]))
  if (!visited.has(tab)) setVisited(new Set(visited).add(tab))

  return (
    <div className="flex flex-col gap-4 pb-4" data-content-padding="false">
      <div className="px-4 pt-4 md:px-6">
        <h1 className="text-xl font-semibold tracking-tight">消息中心</h1>
        <p className="mt-1 text-sm text-muted-foreground">查看系统消息与公告，未读内容会实时更新</p>
      </div>

      <Tabs
        value={tab}
        onValueChange={value => navigate({ search: previous => ({ ...previous, tab: value }) })}
        orientation={isMobile ? 'horizontal' : 'vertical'}
        className="gap-4 px-4 md:flex-row md:px-6"
      >
        <TabsList variant="line" className="shrink-0 md:w-44 md:items-stretch">
          {tabs.map(item => (
            <TabsTrigger key={item.value} value={item.value} className="gap-2 md:py-2">
              {item.icon}
              {item.label}
              {item.count > 0 && (
                <Badge variant="destructive" className="ml-auto h-5 min-w-5 px-1.5 tabular-nums">
                  {item.count > 99 ? '99+' : item.count}
                </Badge>
              )}
            </TabsTrigger>
          ))}
        </TabsList>

        {tabs.map(item => (
          <TabsContent
            key={item.value}
            value={item.value}
            keepMounted
            className={cn('min-w-0 flex-1', item.value !== tab && 'hidden')}
          >
            {visited.has(item.value) ? item.value === 'message' ? <MessageList /> : <NoticeList /> : null}
          </TabsContent>
        ))}
      </Tabs>
    </div>
  )
}
