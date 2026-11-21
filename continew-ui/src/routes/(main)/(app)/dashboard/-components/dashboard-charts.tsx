import type { ReactNode } from 'react'

import { cn } from 'cn'
import { Bar, BarChart, CartesianGrid, Cell, Pie, PieChart, XAxis, YAxis } from 'recharts'

import { QueryState } from '@/components/query-state'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { type ChartConfig, ChartContainer, ChartTooltip, ChartTooltipContent } from '@/components/ui/chart'
import { Skeleton } from '@/components/ui/skeleton'
import type { DashboardChartItem } from '@/lib/api/dashboard'

const PIE_COLORS = ['var(--chart-1)', 'var(--chart-2)', 'var(--chart-3)', 'var(--chart-4)', 'var(--chart-5)']

const valueConfig = {
  value: {
    label: '访问量',
    color: 'var(--chart-1)'
  }
} satisfies ChartConfig

interface ChartCardProps {
  readonly title: string
  readonly description: string
  readonly isPending: boolean
  readonly isError: boolean
  readonly isEmpty: boolean
  readonly onRetry: () => void
  readonly children: ReactNode
  readonly className?: string
}

export function ChartCard({
  title,
  description,
  isPending,
  isError,
  isEmpty,
  onRetry,
  children,
  className
}: ChartCardProps) {
  return (
    <Card className={className}>
      <CardHeader>
        <CardTitle>{title}</CardTitle>
        <CardDescription>{description}</CardDescription>
      </CardHeader>
      <CardContent>
        {isPending ? (
          <Skeleton className="h-60 w-full" />
        ) : isError ? (
          <QueryState state="error" className="h-60" onRetry={onRetry} />
        ) : isEmpty ? (
          <QueryState state="empty" className="h-60" title="暂无数据" />
        ) : (
          children
        )}
      </CardContent>
    </Card>
  )
}

interface RankBarChartProps {
  readonly data: DashboardChartItem[]
  readonly vertical?: boolean
  readonly className?: string
}

export function RankBarChart({ data, vertical = false, className }: RankBarChartProps) {
  const radius: [number, number, number, number] = vertical ? [0, 4, 4, 0] : [4, 4, 0, 0]

  return (
    <ChartContainer config={valueConfig} className={cn('aspect-auto h-60 w-full', className)}>
      <BarChart
        accessibilityLayer
        data={data}
        layout={vertical ? 'vertical' : 'horizontal'}
        margin={{ left: 4, right: 12, top: 8, bottom: 0 }}
      >
        <CartesianGrid horizontal={!vertical} vertical={vertical} strokeOpacity={0.5} />
        {vertical ? (
          <>
            <XAxis type="number" hide />
            <YAxis
              type="category"
              dataKey="name"
              width={112}
              tickLine={false}
              axisLine={false}
              tickMargin={4}
              tick={{ fontSize: 12 }}
            />
          </>
        ) : (
          <>
            <XAxis dataKey="name" tickLine={false} axisLine={false} tickMargin={8} />
            <YAxis type="number" hide />
          </>
        )}
        <ChartTooltip cursor={false} content={<ChartTooltipContent indicator="line" />} />
        <Bar dataKey="value" fill="var(--color-value)" radius={radius} barSize={vertical ? 14 : 28} />
      </BarChart>
    </ChartContainer>
  )
}

export function DistributionPieChart({ data, className }: { data: DashboardChartItem[]; className?: string }) {
  return (
    <div className={cn('flex flex-col', className)}>
      <ChartContainer config={valueConfig} className="aspect-auto h-48 w-full">
        <PieChart>
          <ChartTooltip content={<ChartTooltipContent nameKey="name" />} />
          <Pie data={data} dataKey="value" nameKey="name" innerRadius={48} outerRadius={80} paddingAngle={2}>
            {data.map((item, index) => (
              <Cell key={item.name} fill={PIE_COLORS[index % PIE_COLORS.length]} />
            ))}
          </Pie>
        </PieChart>
      </ChartContainer>
      <div className="mt-2 flex flex-wrap justify-center gap-x-3 gap-y-1">
        {data.map((item, index) => (
          <div key={item.name} className="flex items-center gap-1.5 text-xs text-muted-foreground">
            <span
              className="size-2 shrink-0 rounded-[2px]"
              style={{ backgroundColor: PIE_COLORS[index % PIE_COLORS.length] }}
            />
            <span className="max-w-24 truncate">{item.name}</span>
          </div>
        ))}
      </div>
    </div>
  )
}
