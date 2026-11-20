import * as React from 'react'

import { Cell, Pie, PieChart, ResponsiveContainer, Tooltip as RechartsTooltip } from 'recharts'

import { Card, CardAction, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { ToggleGroup, ToggleGroupItem } from '@/components/ui/toggle-group'
import type { FileFilterType, FileStatisticsResp } from '@/lib/api/file'

import { fileTypeLabel, formatFileSize } from './file-display'

type ChartMetric = 'size' | 'count'

/** The five file filters, in the same order as the sidebar. */
const CHART_TYPES: FileFilterType[] = [2, 3, 4, 5, 1]

interface FileTypeChartProps {
  readonly stats: FileStatisticsResp | undefined
  readonly isLoading: boolean
}

/**
 * A Recharts donut in the shadcn chart style. Recharts is already a project dependency, so this
 * stays a feature component instead of adding a ui/chart primitive only this screen would use.
 */
export function FileTypeChart({ stats, isLoading }: FileTypeChartProps) {
  const [metric, setMetric] = React.useState<ChartMetric>('size')

  const typeStats = new Map(
    (stats?.data ?? []).map(item => [Number(item.type), { size: item.size ?? 0, number: item.number ?? 0 }])
  )

  const data = CHART_TYPES.map((type, index) => {
    const stat = typeStats.get(type)
    const size = stat?.size ?? 0
    const number = stat?.number ?? 0

    return {
      type,
      name: fileTypeLabel(type),
      size,
      number,
      value: metric === 'size' ? size : number,
      color: `var(--chart-${index + 1})`
    }
  }).filter(item => item.value > 0)

  const total = data.reduce((sum, item) => sum + item.value, 0)

  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-base">分类占比</CardTitle>
        <CardDescription>{metric === 'size' ? '按文件大小统计' : '按文件数量统计'}</CardDescription>
        <CardAction>
          <ToggleGroup
            variant="outline"
            size="sm"
            spacing={0}
            value={[metric]}
            onValueChange={values => {
              const next = values[0]
              if (next === 'size' || next === 'count') {
                setMetric(next)
              }
            }}
          >
            <ToggleGroupItem value="size">大小</ToggleGroupItem>
            <ToggleGroupItem value="count">数量</ToggleGroupItem>
          </ToggleGroup>
        </CardAction>
      </CardHeader>

      <CardContent className="flex flex-col gap-3">
        {isLoading ? (
          <div className="flex flex-col gap-3">
            <Skeleton className="mx-auto size-36 rounded-full" />
            <Skeleton className="h-3 w-full" />
            <Skeleton className="h-3 w-2/3" />
          </div>
        ) : data.length === 0 ? (
          <div className="flex h-44 items-center justify-center text-xs text-muted-foreground">暂无统计数据</div>
        ) : (
          <>
            <div className="relative h-44 w-full">
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={data}
                    dataKey="value"
                    nameKey="name"
                    innerRadius="62%"
                    outerRadius="88%"
                    paddingAngle={2}
                    cornerRadius={3}
                    stroke="none"
                  >
                    {data.map(item => (
                      <Cell key={item.type} fill={item.color} />
                    ))}
                  </Pie>
                  <RechartsTooltip
                    cursor={false}
                    formatter={value => (metric === 'size' ? formatFileSize(Number(value)) : `${Number(value)} 个`)}
                    contentStyle={{
                      borderRadius: 8,
                      borderColor: 'var(--border)',
                      background: 'var(--background)',
                      fontSize: 12
                    }}
                  />
                </PieChart>
              </ResponsiveContainer>
              <div className="pointer-events-none absolute inset-0 flex flex-col items-center justify-center">
                <span className="text-xs text-muted-foreground">总计</span>
                <span className="text-lg font-semibold tabular-nums">
                  {metric === 'size' ? formatFileSize(total) : `${total} 个`}
                </span>
              </div>
            </div>

            <div className="flex flex-col gap-1.5">
              {data.map(item => (
                <div key={item.type} className="flex items-center justify-between gap-2 text-xs">
                  <span className="flex min-w-0 items-center gap-1.5">
                    <span className="size-2 shrink-0 rounded-full" style={{ backgroundColor: item.color }} />
                    <span className="truncate text-muted-foreground">{item.name}</span>
                  </span>
                  <span className="shrink-0 tabular-nums">
                    {metric === 'size' ? formatFileSize(item.size) : `${item.number} 个`}
                  </span>
                </div>
              ))}
            </div>
          </>
        )}
      </CardContent>
    </Card>
  )
}
