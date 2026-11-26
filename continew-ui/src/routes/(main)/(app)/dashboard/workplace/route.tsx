import * as React from 'react'

import { useQuery } from '@tanstack/react-query'
import { createFileRoute } from '@tanstack/react-router'

import { ExternalLink, Info } from 'lucide-react'

import { Button } from '@/components/ui/button'
import { isWorkbenchEnabled, type WorkbenchRange } from '@/lib/api/workbench'
import { PROJECT_LINKS } from '@/navigation/route-titles'

import { WorkbenchActivity } from './-components/workbench-activity'
import { WorkbenchNotices } from './-components/workbench-notices'
import { commitsQueryOptions, releasesQueryOptions, starsQueryOptions } from './-components/workbench-queries'
import { WorkbenchReleases } from './-components/workbench-releases'
import { WorkbenchSponsors } from './-components/workbench-sponsors'
import { WorkbenchStars } from './-components/workbench-stars'
import { WorkbenchWelcome } from './-components/workbench-welcome'

/**
 * The workbench: the landing page after sign-in. The Hub panels need `VITE_WORKBENCH_HUB_URL`;
 * the notice panel and the sponsor carousel come from the Admin API and continew.top, so they
 * keep working when the Hub is not configured.
 */
export const Route = createFileRoute('/(main)/(app)/dashboard/workplace')({
  component: Page
})

function Page() {
  return (
    <div className="@container/main flex flex-col gap-4 md:gap-6">
      <WorkbenchWelcome />

      {isWorkbenchEnabled ? (
        <HubPanels />
      ) : (
        <>
          <div className="flex flex-wrap items-center justify-between gap-x-4 gap-y-1 rounded-lg border bg-muted/30 px-3 py-2">
            <p className="flex items-center gap-2 text-sm text-muted-foreground">
              <Info aria-hidden className="size-4 shrink-0" />
              此工作台未启用仓库统计与更新日志。
            </p>
            <Button
              variant="link"
              size="sm"
              nativeButton={false}
              render={<a href={PROJECT_LINKS.releases} target="_blank" rel="noreferrer" />}
            >
              查看发布记录 <ExternalLink />
            </Button>
          </div>
          <div className="grid min-w-0 items-start gap-4 @[44rem]/main:grid-cols-[minmax(16rem,20rem)_minmax(0,1fr)]">
            <WorkbenchSponsors />
            <WorkbenchNotices />
          </div>
        </>
      )}
    </div>
  )
}

function HubPanels() {
  const [range, setRange] = React.useState<WorkbenchRange>('30d')
  const commits = useQuery(commitsQueryOptions(range))
  const stars = useQuery(starsQueryOptions('6m'))
  const releases = useQuery(releasesQueryOptions())
  const samples = [
    commits.data?.source === 'mock' && '近期提交',
    stars.data?.source === 'mock' && 'Star 趋势',
    releases.data?.source === 'mock' && '更新日志'
  ].filter(Boolean)
  // A stale snapshot is still shown, but its age must be visible instead of reading as current data.
  const staleAt = [
    commits.data?.data.freshness === 'stale' ? commits.data.data.updatedAt : null,
    stars.data?.data.freshness === 'stale' ? stars.data.data.updatedAt : null,
    releases.data?.data.freshness === 'stale' ? releases.data.data.updatedAt : null
  ].find(Boolean)

  return (
    <div className="grid min-w-0 items-start gap-4 @[56rem]/main:grid-cols-[minmax(0,1fr)_clamp(20rem,32%,23rem)]">
      <div className="flex min-w-0 flex-col gap-4">
        <WorkbenchActivity range={range} onRangeChange={setRange} query={commits} />
        <WorkbenchStars query={stars} />
      </div>
      <div className="grid min-w-0 items-start gap-4">
        <WorkbenchReleases query={releases} />
        <WorkbenchSponsors />
        <WorkbenchNotices />
      </div>
      {samples.length > 0 && (
        <p className="col-span-full text-xs text-muted-foreground">示例数据：{samples.join('、')}</p>
      )}
      {staleAt && <p className="col-span-full text-xs text-muted-foreground">部分数据为旧快照：更新于 {staleAt}</p>}
    </div>
  )
}
