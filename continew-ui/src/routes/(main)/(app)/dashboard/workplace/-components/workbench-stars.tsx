import type { UseQueryResult } from '@tanstack/react-query'

import { Area, AreaChart, CartesianGrid, ReferenceDot, XAxis, YAxis } from 'recharts'

import { QueryState } from '@/components/query-state'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { type ChartConfig, ChartContainer, ChartTooltip, ChartTooltipContent } from '@/components/ui/chart'
import { Skeleton } from '@/components/ui/skeleton'
import type { HubResponse, WorkbenchStarsResp } from '@/lib/api/workbench'

import { monthLabel, starDomain, starMonthTicks, starTimeline, toStarPoints } from './workbench-data'

const compactCount = new Intl.NumberFormat('en-US', { notation: 'compact', maximumFractionDigits: 1 })

const chartConfig = {
  totalStars: {
    label: 'Star 总数',
    color: 'var(--chart-1)'
  }
} satisfies ChartConfig

/**
 * Star trend of all public repositories. `totalStars` is each repository's end-of-period count
 * added up, not a daily increment; a null bucket breaks the line instead of interpolating.
 */
export function WorkbenchStars({ query }: { readonly query: UseQueryResult<HubResponse<WorkbenchStarsResp>> }) {
  const meta = query.data?.data

  const points = meta ? toStarPoints(meta, meta.buckets) : []
  const measured = points.filter(point => point.totalStars !== null)
  const latest = meta?.latest
  const timeline = meta ? starTimeline(meta, points) : []
  const observation = timeline.find(row => row.latestStars !== null)
  const observationLabel =
    observation && latest
      ? `最新实测：${observation.label} · ${latest.totalStars.toLocaleString('zh-CN')} Star`
      : undefined
  const domain = starDomain([...points.map(point => point.totalStars), observation?.latestStars ?? null])
  const monthStarts = new Set(starMonthTicks(points))
  const ticks = new Map(
    timeline
      .filter(row => monthStarts.has(row.periodStart) && (row.latestStars === null || row.totalStars !== null))
      .map(row => [row.time, monthLabel(row.periodStart)])
  )
  const gaps = points.filter(point => point.totalStars === null)
  // A gap the Hub forgot to list is still incomplete data, so the badge must not depend on the list alone.
  const partial = measured.length > 0 && (gaps.length > 0 || measured.some(point => point.partial))

  return (
    <Card className="@container/stars">
      <CardHeader>
        <div className="flex flex-wrap items-center gap-2">
          <CardTitle>
            <h2>Star 趋势</h2>
          </CardTitle>
          {partial && <Badge variant="secondary">部分数据</Badge>}
        </div>
        <div className="flex flex-wrap items-baseline justify-between gap-x-4">
          <CardDescription>Open ContiNew 仓库 Star 总数</CardDescription>
          <div className="flex items-baseline gap-2">
            <span className="text-xs text-muted-foreground">近 6 个月</span>
            <span
              className="text-lg font-semibold tabular-nums"
              title={latest ? `${latest.totalStars.toLocaleString('zh-CN')}｜采样时间：${latest.sampledAt}` : undefined}
            >
              {latest ? compactCount.format(latest.totalStars) : '—'}
            </span>
          </div>
        </div>
      </CardHeader>

      <CardContent>
        {query.isPending ? (
          <Skeleton className="h-40 w-full" />
        ) : query.isError ? (
          <QueryState state="error" className="h-40" onRetry={() => void query.refetch()} />
        ) : measured.length === 0 && !observation ? (
          <QueryState state="empty" className="h-40" title="这段时间暂无 Star 记录" />
        ) : (
          <ChartContainer config={chartConfig} debounce={220} className="aspect-auto h-40 w-full">
            <AreaChart accessibilityLayer data={timeline} margin={{ top: 8, right: 22, bottom: 0, left: 0 }}>
              <defs>
                <linearGradient id="fillWorkbenchStars" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="var(--color-totalStars)" stopOpacity={0.32} />
                  <stop offset="95%" stopColor="var(--color-totalStars)" stopOpacity={0.04} />
                </linearGradient>
              </defs>
              <CartesianGrid vertical={false} strokeOpacity={0.5} />
              <XAxis
                dataKey="time"
                type="number"
                domain={['dataMin', 'dataMax']}
                tickLine={false}
                axisLine={false}
                tickMargin={6}
                interval="preserveStartEnd"
                ticks={[...ticks.keys()]}
                tickFormatter={time => ticks.get(time) ?? ''}
              />
              <YAxis
                allowDataOverflow
                width="auto"
                tickLine={false}
                axisLine={false}
                domain={domain ? [domain[0], domain[1]] : ['auto', 'auto']}
              />
              <ChartTooltip
                cursor={false}
                content={
                  <ChartTooltipContent
                    indicator="line"
                    labelFormatter={(_value, payload) => {
                      const point = payload?.[0]?.payload as { label?: string } | undefined
                      return point?.label ? `${point.label}｜Star 总数` : 'Star 总数'
                    }}
                  />
                }
              />
              <Area
                baseValue="dataMin"
                fill="url(#fillWorkbenchStars)"
                fillOpacity={1}
                dataKey="totalStars"
                type="monotone"
                stroke="var(--color-totalStars)"
                strokeWidth={1.25}
                dot={false}
                activeDot={{ r: 3 }}
                // A bucket without a snapshot keeps its slot but must not be bridged.
                connectNulls={false}
              />
              {observation && latest && (
                <ReferenceDot
                  x={observation.time}
                  y={latest.totalStars}
                  zIndex={1000}
                  shape={({ cx, cy }) => (
                    <g role="img" aria-label={observationLabel} transform={`translate(${cx},${cy})`}>
                      <title>{observationLabel}</title>
                      <circle r={11} fill="transparent" />
                      <circle r={4} fill="var(--color-totalStars)" stroke="var(--card)" strokeWidth={2} />
                    </g>
                  )}
                />
              )}
            </AreaChart>
          </ChartContainer>
        )}

        {observationLabel && measured.length === 0 && (
          <p className="mt-2 text-xs text-muted-foreground">{observationLabel}｜暂无历史日末记录</p>
        )}
        {measured.length > 0 && gaps.length > 0 ? (
          <p className="mt-2 text-xs text-muted-foreground">
            暂无记录：
            {gaps
              .map(point => point.label)
              .slice(0, 6)
              .join('、')}
            {gaps.length > 6 ? ' 等' : ''}
          </p>
        ) : null}
      </CardContent>
    </Card>
  )
}
