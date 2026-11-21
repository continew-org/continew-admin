import * as React from 'react'

import { useQueries, useQuery } from '@tanstack/react-query'
import { Bell } from 'lucide-react'

import { QueryState } from '@/components/query-state'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { dashboardKeys, getDashboardNotices } from '@/lib/api/dashboard'
import { getNotice, noticeKeys } from '@/lib/api/notice'
import { DETAIL_STALE_TIME_MS } from '@/lib/query-client'
import { NoticeViewDrawer } from '@/routes/(main)/(app)/system/notice/-components/notice-view-drawer'

/** The dashboard notice only carries the title; the date comes from the notice detail. */
function publishDate(publishTime: string | null | undefined) {
  return publishTime ? publishTime.slice(0, 10) : undefined
}

export function DashboardNotices() {
  const query = useQuery({
    queryKey: dashboardKeys.notices(),
    queryFn: ({ signal }) => getDashboardNotices(signal)
  })

  const notices = query.data ?? []

  // The dashboard notice payload has no publish time, so fill it from each notice's detail. The
  // same query key is reused by the preview drawer, so opening a row hits the warmed cache.
  const details = useQueries({
    queries: notices.map(notice => ({
      queryKey: noticeKeys.detail(notice.id),
      queryFn: ({ signal }: { signal: AbortSignal }) => getNotice(notice.id, signal),
      staleTime: DETAIL_STALE_TIME_MS,
      refetchOnMount: 'always' as const
    }))
  })

  const [viewId, setViewId] = React.useState<string | number | null>(null)

  return (
    <>
      <Card className="h-full">
        <CardHeader>
          <CardTitle>最新公告</CardTitle>
          <CardDescription>系统已发布的最新公告</CardDescription>
        </CardHeader>
        <CardContent>
          {query.isPending ? (
            <div className="flex flex-col gap-3">
              {Array.from({ length: 4 }).map((_, index) => (
                <Skeleton key={index} className="h-10 w-full" />
              ))}
            </div>
          ) : query.isError ? (
            <QueryState state="error" className="h-60" onRetry={() => void query.refetch()} />
          ) : !notices.length ? (
            <QueryState state="empty" className="h-60" title="暂无公告" />
          ) : (
            <ul className="flex flex-col">
              {notices.map((notice, index) => {
                const detail = details[index]
                const date = publishDate(detail?.data?.publishTime)

                return (
                  <li key={notice.id} className="border-b last:border-b-0">
                    <button
                      type="button"
                      onClick={() => setViewId(notice.id)}
                      className="-mx-2 flex w-[calc(100%+1rem)] items-center gap-2 rounded-md px-2 py-3 text-left transition-colors hover:bg-muted focus-visible:bg-muted focus-visible:outline-none"
                    >
                      <Bell className="size-3.5 shrink-0 text-muted-foreground" />
                      <span className="min-w-0 flex-1 truncate text-sm">{notice.title}</span>
                      {date ? (
                        <span className="shrink-0 text-xs text-muted-foreground tabular-nums">{date}</span>
                      ) : detail?.isPending ? (
                        <Skeleton className="h-3 w-16 shrink-0" />
                      ) : null}
                      {notice.isTop ? (
                        <Badge variant="secondary" className="shrink-0">
                          置顶
                        </Badge>
                      ) : null}
                    </button>
                  </li>
                )
              })}
            </ul>
          )}
        </CardContent>
      </Card>

      <NoticeViewDrawer noticeId={viewId} open={viewId !== null} onClose={() => setViewId(null)} />
    </>
  )
}
