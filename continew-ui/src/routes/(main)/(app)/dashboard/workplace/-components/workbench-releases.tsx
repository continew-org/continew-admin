import type { UseQueryResult } from '@tanstack/react-query'
import { ExternalLink, Tag } from 'lucide-react'

import { QueryState } from '@/components/query-state'
import { Button } from '@/components/ui/button'
import { Card, CardAction, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import type { HubResponse, WorkbenchReleasesResp } from '@/lib/api/workbench'
import { PROJECT_LINKS } from '@/navigation/route-titles'

import { releaseDate, releaseTitle, safeHttpUrl } from './workbench-data'

/**
 * The latest formal releases, straight from the Hub; the news does not follow the chart period.
 *
 * `htmlUrl` comes from a remote payload, so it is checked before it becomes an href: a malformed
 * or non-http(s) value links to the releases page instead of wherever it points.
 */
export function WorkbenchReleases({ query }: { readonly query: UseQueryResult<HubResponse<WorkbenchReleasesResp>> }) {
  const items = query.data?.data.items ?? null

  return (
    <Card>
      <CardHeader>
        <CardTitle>
          <h2>更新日志</h2>
        </CardTitle>
        <CardDescription>ContiNew Admin 版本更新</CardDescription>
        <CardAction>
          <Button
            variant="ghost"
            size="sm"
            className="text-muted-foreground"
            nativeButton={false}
            render={<a href={PROJECT_LINKS.releases} target="_blank" rel="noreferrer" />}
          >
            全部版本
            <ExternalLink />
          </Button>
        </CardAction>
      </CardHeader>

      <CardContent>
        {query.isPending ? (
          <div className="flex flex-col gap-2">
            {Array.from({ length: 3 }).map((_, index) => (
              <Skeleton key={index} className="h-9 w-full" />
            ))}
          </div>
        ) : query.isError ? (
          <QueryState state="error" className="h-32" onRetry={() => void query.refetch()} />
        ) : !items || items.length === 0 ? (
          <QueryState state="empty" className="h-32" title={items === null ? '发布记录暂不可用' : '暂无正式版本'} />
        ) : (
          <ul className="flex flex-col">
            {items.map(item => {
              const { version, rest } = releaseTitle(item.tagName, item.title)

              return (
                <li key={item.tagName} className="border-b last:border-b-0">
                  <a
                    href={safeHttpUrl(item.htmlUrl, PROJECT_LINKS.releases)}
                    target="_blank"
                    rel="noreferrer"
                    className="-mx-2 flex items-start gap-2 rounded-md px-2 py-2.5 transition-colors hover:bg-muted focus-visible:bg-muted focus-visible:outline-none"
                  >
                    <Tag className="mt-0.5 size-3.5 shrink-0 text-muted-foreground" />
                    <span className="min-w-0 flex-1 truncate text-sm" title={rest ? `${version}｜${rest}` : version}>
                      <span className="font-medium">{version}</span>
                      {rest ? <span className="text-muted-foreground">{`｜${rest}`}</span> : null}
                    </span>
                    <span className="shrink-0 text-xs text-muted-foreground tabular-nums">
                      {releaseDate(item.publishedAt)}
                    </span>
                  </a>
                </li>
              )
            })}
          </ul>
        )}
      </CardContent>
    </Card>
  )
}
