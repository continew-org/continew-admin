import * as React from 'react'

import type { UseQueryResult } from '@tanstack/react-query'
import { RotateCcw } from 'lucide-react'

import { QueryState } from '@/components/query-state'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { ToggleGroup, ToggleGroupItem } from '@/components/ui/toggle-group'
import { cn } from 'cn'
import {
  type HubResponse,
  type WorkbenchCommitsResp,
  type WorkbenchAuthor,
  type WorkbenchRange,
  WORKBENCH_HUB_BASE
} from '@/lib/api/workbench'

import {
  aggregateAuthors,
  authorInitial,
  authorShades,
  barAxis,
  githubProfileUrl,
  labelledBuckets,
  resolveAvatarUrl,
  toCommitBars,
  toLeaderboard,
  UNLINKED_AUTHOR_ID
} from './workbench-data'

const RANGE_ITEMS = [
  { value: '30d', label: '近 30 天' },
  { value: '6m', label: '近 6 个月' }
] as const

function WorkbenchRangeSwitch({
  range,
  disabled,
  onChange
}: {
  readonly range: WorkbenchRange
  readonly disabled: boolean
  readonly onChange: (range: WorkbenchRange) => void
}) {
  return (
    <ToggleGroup
      aria-label="统计周期"
      variant="outline"
      size="sm"
      spacing={0}
      disabled={disabled}
      value={[range]}
      onValueChange={([next]) => {
        if (next === '30d' || next === '6m') {
          onChange(next)
        }
      }}
    >
      {RANGE_ITEMS.map(item => (
        <ToggleGroupItem
          key={item.value}
          value={item.value}
          className="aria-pressed:bg-primary aria-pressed:text-primary-foreground"
        >
          {item.label}
        </ToggleGroupItem>
      ))}
    </ToggleGroup>
  )
}

function AuthorRow({ author }: { author: WorkbenchAuthor }) {
  const avatar = resolveAvatarUrl(author.avatarUrl, WORKBENCH_HUB_BASE)
  const unlinked = author.authorId === UNLINKED_AUTHOR_ID
  const profile = githubProfileUrl(author.authorId)
  const identity = (
    <>
      <Avatar size="sm" aria-hidden className="after:hidden">
        {avatar && !unlinked ? <AvatarImage src={avatar} alt="" /> : null}
        <AvatarFallback className="bg-transparent">{authorInitial(author.displayName)}</AvatarFallback>
      </Avatar>
      <span className={cn('min-w-0 flex-1 truncate text-sm', unlinked && 'text-muted-foreground')}>
        {author.displayName}
      </span>
    </>
  )

  return (
    <li className="flex min-h-8 items-center gap-2 py-0.5">
      {profile ? (
        <a
          href={profile}
          target="_blank"
          rel="noopener noreferrer"
          className="flex min-w-0 flex-1 items-center gap-2 rounded-sm underline-offset-2 hover:text-primary hover:underline focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-inset"
        >
          {identity}
        </a>
      ) : (
        identity
      )}
      <span className="shrink-0 text-sm font-medium tabular-nums">
        {author.count}
        <span className="ml-0.5 text-xs font-normal text-muted-foreground">次</span>
      </span>
    </li>
  )
}

interface ActivityProps {
  readonly range: WorkbenchRange
  readonly query: UseQueryResult<HubResponse<WorkbenchCommitsResp>>
}

/**
 * Commit chart with the leaderboard beside it. A click on a bar narrows the leaderboard to that
 * bucket without another request: the bucket already carries its authors, keyed by `authorId`.
 */
export function WorkbenchActivity({
  range,
  query,
  onRangeChange
}: ActivityProps & { onRangeChange: (range: WorkbenchRange) => void }) {
  return (
    <Card role="region" aria-labelledby="workbench-activity" className="@container/activity">
      <CardHeader className="flex flex-wrap items-start justify-between gap-2">
        <div className="space-y-1">
          <CardTitle>
            <h2 id="workbench-activity">近期提交</h2>
          </CardTitle>
          <CardDescription>Open ContiNew 仓库更新热度</CardDescription>
        </div>
        <WorkbenchRangeSwitch range={range} disabled={!query.isEnabled} onChange={onRangeChange} />
      </CardHeader>
      <CardContent
        key={range}
        className="grid grid-cols-1 grid-rows-[repeat(2,minmax(16rem,auto))] gap-4 @[35rem]/activity:grid-cols-[minmax(0,2.2fr)_minmax(14.5rem,1fr)] @[35rem]/activity:grid-rows-[minmax(16rem,auto)] @[35rem]/activity:gap-5"
      >
        <ActivityBody range={range} query={query} />
      </CardContent>
    </Card>
  )
}

function ActivityBody({ range, query }: ActivityProps) {
  // The selection is a `periodStart`, not an index: the two ranges have different bucket lists.
  const [selectedStart, setSelectedStart] = React.useState<string | null>(null)

  const meta = query.data?.data
  // The projection is derived from the response, so it only changes when the response does.
  const bars = React.useMemo(() => (meta ? toCommitBars(meta, meta.buckets) : []), [meta])
  const selected = bars.find(bar => bar.periodStart === selectedStart)
  const shades = React.useMemo(() => authorShades(bars.map(bar => bar.authors)), [bars])

  const axis = barAxis(bars.map(bar => bar.total))
  const labels = labelledBuckets(bars, range)

  // The chart total always describes the whole window; the leaderboard follows the selection.
  const windowAuthors = aggregateAuthors(bars.map(bar => bar.authors))
  const windowTotal = windowAuthors.reduce((sum, author) => sum + author.count, 0)
  const leaderboard = toLeaderboard(selected ? selected.authors : windowAuthors)
  const windowPartial = bars.some(bar => bar.partial)
  const windowHasData = bars.some(bar => !bar.partial || bar.authors.length > 0)
  const partial = selected?.partial ?? windowPartial
  const hasDetail = selected ? !selected.partial || selected.authors.length > 0 : windowHasData

  const selectBar = (periodStart: string) => setSelectedStart(current => (current === periodStart ? null : periodStart))
  const selectHint = range === '6m' ? '选择月份，查看当月贡献榜。' : '选择日期，查看当天贡献榜。'

  if (!query.isEnabled) {
    return <QueryState state="empty" className="col-span-full row-span-full" title="暂无提交统计" />
  }
  if (query.isPending) {
    return <Skeleton className="col-span-full row-span-full" />
  }
  if (query.isError) {
    return <QueryState state="error" className="col-span-full row-span-full" onRetry={() => void query.refetch()} />
  }
  if (bars.length === 0) {
    return <QueryState state="empty" className="col-span-full row-span-full" title="暂无提交数据" />
  }

  return (
    <>
      <div className="flex min-w-0 flex-col">
        <div className="flex items-baseline justify-between gap-2">
          <h3 className="text-sm font-medium">{range === '6m' ? '每月提交次数' : '每日提交次数'}</h3>
          <span className="text-xs text-muted-foreground tabular-nums">
            {windowHasData ? `${windowPartial ? '已知小计' : '合计'} ${windowTotal} 次` : '暂无可用提交明细'}
          </span>
        </div>

        <div className="flex gap-1.5 pt-4 @[35rem]/activity:mt-auto">
          <div className="relative h-40 w-7 shrink-0 text-xs text-muted-foreground tabular-nums">
            {/* Both gridline values sit on their lines, so the top one never leans into the heading. */}
            <span className="absolute top-0 right-0 -translate-y-1/2">{axis.max}</span>
            <span className="absolute top-1/2 right-0 -translate-y-1/2">{axis.half}</span>
            <span className="absolute top-full right-0 -translate-y-1/2">0</span>
          </div>

          <div className="@container/plot min-w-0 flex-1">
            <div className="relative h-40 border-b">
              <div className="absolute inset-x-0 top-1/2 border-t border-dashed opacity-60" />
              <div
                className="grid h-full grid-rows-1"
                style={{ gridTemplateColumns: `repeat(${bars.length}, minmax(0, 1fr))` }}
              >
                {bars.map(bar => {
                  const active = bar.periodStart === selectedStart
                  const height = (bar.total / axis.max) * 100
                  const missing = bar.partial && bar.authors.length === 0
                  const summary = missing ? '暂无可用提交明细' : `${bar.partial ? '已知小计 ' : ''}${bar.total} 次提交`

                  return (
                    <button
                      key={bar.periodStart}
                      type="button"
                      aria-pressed={active}
                      onClick={() => selectBar(bar.periodStart)}
                      title={`${bar.periodStart}｜${summary}`}
                      className={cn(
                        'group relative flex h-full min-w-0 flex-col justify-end px-0.5 transition-colors focus-visible:ring-2 focus-visible:ring-ring focus-visible:outline-none',
                        range === '6m' && 'items-center *:max-w-[min(60%,2rem)]',
                        active ? 'bg-primary/10' : 'hover:bg-muted/60'
                      )}
                    >
                      {missing && (
                        <span aria-hidden className="mb-0.5 w-full border-b-2 border-dashed border-muted-foreground" />
                      )}
                      <span
                        className={cn(
                          'flex w-full flex-col-reverse overflow-hidden rounded-t-xs',
                          bar.partial && 'opacity-50'
                        )}
                        style={{ height: `${height}%` }}
                      >
                        {bar.authors.map(author => (
                          <span
                            key={author.authorId}
                            className="w-full"
                            // A flex-grow share of the bar's height, so segments split by count.
                            style={{
                              flexGrow: author.count,
                              flexBasis: 0,
                              backgroundColor: shades.get(author.authorId)
                            }}
                          />
                        ))}
                      </span>
                    </button>
                  )
                })}
              </div>
            </div>

            <div
              className="mt-1 grid text-xs whitespace-nowrap text-muted-foreground"
              style={{ gridTemplateColumns: `repeat(${bars.length}, minmax(0, 1fr))` }}
            >
              {bars.map((bar, index) => (
                <span
                  key={bar.periodStart}
                  className={cn(
                    'w-max justify-self-center',
                    range === '30d' && index % 14 !== 0 && index !== bars.length - 1 && '@max-[16rem]/plot:invisible',
                    range === '30d' && index === 0 && 'justify-self-start',
                    range === '30d' && index === bars.length - 1 && 'justify-self-end'
                  )}
                >
                  {labels.has(bar.periodStart) ? bar.label : null}
                </span>
              ))}
            </div>
          </div>
        </div>

        <p className="mt-2 text-xs text-muted-foreground">
          {windowPartial ? '部分提交数据不完整，合计仅含已记录的提交。' : selectHint}
        </p>
      </div>

      <section
        aria-label="贡献榜"
        className="flex h-64 min-w-0 flex-col border-t pt-3 @[35rem]/activity:border-t-0 @[35rem]/activity:border-l @[35rem]/activity:pt-0 @[35rem]/activity:pl-5"
      >
        <div className="flex min-h-6 items-baseline justify-between gap-1">
          <h3 className="shrink-0 -translate-x-px text-sm font-medium">贡献榜</h3>
          {hasDetail && (
            <p className="min-w-0 text-right text-xs wrap-anywhere text-muted-foreground tabular-nums">
              {`${partial ? '已知 ' : ''}${leaderboard.total} 次提交 · ${leaderboard.contributors} 位贡献者`}
            </p>
          )}
        </div>
        {(selected || partial) && (
          <div className="mt-1 flex min-h-6 items-center gap-1 text-xs text-muted-foreground">
            {selected && <span className="truncate">{selected.label}</span>}
            {partial && <Badge variant="secondary">部分数据</Badge>}
            {selected && (
              <Button
                variant="ghost"
                size="xs"
                className="ml-auto text-muted-foreground"
                onClick={() => setSelectedStart(null)}
              >
                <RotateCcw />
                重置筛选
              </Button>
            )}
          </div>
        )}

        <div className="mt-2 min-h-0 flex-1 [scrollbar-gutter:stable] overflow-y-auto">
          {leaderboard.rows.length === 0 ? (
            <p className="flex min-h-8 items-center text-xs text-muted-foreground">
              {partial ? '该周期暂无可用提交明细' : '该周期没有提交'}
            </p>
          ) : (
            <ol>
              {leaderboard.rows.map(author => (
                <AuthorRow key={author.authorId} author={author} />
              ))}
            </ol>
          )}
        </div>
      </section>
    </>
  )
}
