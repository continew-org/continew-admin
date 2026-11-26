import * as React from 'react'

import { useQuery } from '@tanstack/react-query'
import { Bell } from 'lucide-react'

import { QueryState } from '@/components/query-state'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { dashboardKeys, getDashboardNotices } from '@/lib/api/dashboard'
import { NoticeViewDrawer } from '@/routes/(main)/(app)/system/notice/-components/notice-view-drawer'

export function WorkbenchNotices() {
  const query = useQuery({
    queryKey: dashboardKeys.notices(),
    queryFn: ({ signal }) => getDashboardNotices(signal)
  })

  const notices = query.data ?? []

  const [viewId, setViewId] = React.useState<string | number | null>(null)

  return (
    <>
      <Card>
        <CardHeader>
          <CardTitle>
            <h2>最新公告</h2>
          </CardTitle>
          <CardDescription>查看最新发布的系统公告</CardDescription>
        </CardHeader>
        <CardContent>
          {query.isPending ? (
            <div className="flex flex-col gap-3">
              <Skeleton className="h-4 w-3/4" />
              <Skeleton className="h-4 w-1/2" />
            </div>
          ) : query.isError ? (
            <QueryState state="error" onRetry={() => void query.refetch()} />
          ) : !notices.length ? (
            <QueryState state="empty" title="暂无公告" />
          ) : (
            <ul className="flex flex-col">
              {notices.map(notice => (
                <li key={notice.id} className="border-b last:border-b-0">
                  <button
                    type="button"
                    onClick={() => setViewId(notice.id)}
                    className="-mx-2 flex w-[calc(100%+1rem)] items-center gap-2 rounded-md px-2 py-3 text-left transition-colors hover:bg-muted focus-visible:bg-muted focus-visible:outline-none"
                  >
                    <Bell className="size-3.5 shrink-0 text-muted-foreground" />
                    <span className="min-w-0 flex-1 truncate text-sm" title={notice.title}>
                      {notice.title}
                    </span>
                    {notice.isTop ? (
                      <Badge variant="secondary" className="shrink-0">
                        置顶
                      </Badge>
                    ) : null}
                  </button>
                </li>
              ))}
            </ul>
          )}
        </CardContent>
      </Card>

      <NoticeViewDrawer source="self" noticeId={viewId} open={viewId !== null} onClose={() => setViewId(null)} />
    </>
  )
}
