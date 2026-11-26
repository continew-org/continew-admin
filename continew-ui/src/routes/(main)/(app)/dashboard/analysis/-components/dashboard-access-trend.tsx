import * as React from 'react'

import { useQuery } from '@tanstack/react-query'
import { format, parseISO } from 'date-fns'
import { Area, CartesianGrid, ComposedChart, Line, XAxis } from 'recharts'

import { QueryState } from '@/components/query-state'
import { Card, CardAction, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import {
  type ChartConfig,
  ChartContainer,
  ChartLegend,
  ChartLegendContent,
  ChartTooltip,
  ChartTooltipContent
} from '@/components/ui/chart'
import { Skeleton } from '@/components/ui/skeleton'
import { ToggleGroup, ToggleGroupItem } from '@/components/ui/toggle-group'
import {
  dashboardKeys,
  getDashboardAccessTrend,
  type DashboardRange,
  type DashboardAccessTrendResp
} from '@/lib/api/dashboard'

import { dashboardChartColor } from '../../-chart-color'

const RANGE_ITEMS: readonly { value: DashboardRange; label: string }[] = [
  { value: 7, label: '近 7 天' },
  { value: 30, label: '近 30 天' }
]

const chartConfig = {
  pvCount: {
    label: '访问量',
    color: 'var(--chart-1)'
  },
  ipCount: {
    label: '独立 IP',
    color: dashboardChartColor(1)
  }
} satisfies ChartConfig

export function DashboardAccessTrend() {
  const [range, setRange] = React.useState<DashboardRange>(7)

  const query = useQuery({
    queryKey: dashboardKeys.accessTrend(range),
    queryFn: ({ signal }) => getDashboardAccessTrend(range, signal)
  })

  const data = query.data ?? []

  return (
    <Card className="@container/card">
      <CardHeader>
        <CardTitle>
          <h2>访问趋势</h2>
        </CardTitle>
        <CardDescription>
          <span className="hidden @[540px]/card:block">访问量与独立 IP 的按日走势</span>
          <span className="@[540px]/card:hidden">按日访问走势</span>
        </CardDescription>
        <CardAction>
          <ToggleGroup
            aria-label="访问趋势统计周期"
            variant="outline"
            size="sm"
            spacing={0}
            value={[String(range)]}
            onValueChange={([next]) => {
              const match = RANGE_ITEMS.find(item => String(item.value) === next)
              if (match) {
                setRange(match.value)
              }
            }}
          >
            {RANGE_ITEMS.map(item => (
              <ToggleGroupItem
                key={item.value}
                value={String(item.value)}
                className="aria-pressed:bg-primary aria-pressed:text-primary-foreground"
              >
                {item.label}
              </ToggleGroupItem>
            ))}
          </ToggleGroup>
        </CardAction>
      </CardHeader>

      <CardContent>
        {query.isPending ? (
          <Skeleton className="h-72 w-full" />
        ) : query.isError ? (
          <QueryState state="error" className="h-72" onRetry={() => void query.refetch()} />
        ) : !data.some(point => point.pvCount > 0 || point.ipCount > 0) ? (
          <QueryState state="empty" className="h-72" title="暂无访问数据" />
        ) : (
          <ChartContainer config={chartConfig} debounce={220} className="aspect-auto h-72 w-full">
            <ComposedChart data={data} margin={{ top: 0 }}>
              <defs>
                <linearGradient id="fillPvCount" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="var(--color-pvCount)" stopOpacity={0.32} />
                  <stop offset="95%" stopColor="var(--color-pvCount)" stopOpacity={0.04} />
                </linearGradient>
              </defs>
              <CartesianGrid vertical={false} strokeOpacity={0.5} />
              <XAxis
                dataKey="date"
                tickLine={false}
                axisLine={false}
                tickMargin={8}
                minTickGap={32}
                tickFormatter={(value: DashboardAccessTrendResp['date']) => format(parseISO(value), 'MM-dd')}
              />
              <ChartTooltip
                cursor={false}
                content={<ChartTooltipContent indicator="line" labelFormatter={value => String(value)} />}
              />
              <ChartLegend verticalAlign="top" content={<ChartLegendContent className="mb-4 justify-end" />} />
              <Area
                dataKey="pvCount"
                type="natural"
                fill="url(#fillPvCount)"
                stroke="var(--color-pvCount)"
                strokeWidth={1.25}
                dot={false}
              />
              <Line dataKey="ipCount" type="natural" stroke="var(--color-ipCount)" strokeWidth={1.4} dot={false} />
            </ComposedChart>
          </ChartContainer>
        )}
      </CardContent>
    </Card>
  )
}
