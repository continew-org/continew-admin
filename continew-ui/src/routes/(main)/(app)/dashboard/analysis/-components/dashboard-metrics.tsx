import { useQuery } from '@tanstack/react-query'
import { Activity, CalendarCheck, Globe2, TrendingDown, TrendingUp, UsersRound } from 'lucide-react'

import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardDescription, CardHeader } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { dashboardKeys, getDashboardIpOverview, getDashboardPvOverview } from '@/lib/api/dashboard'

function formatNumber(value: number | undefined) {
  return value === undefined ? '—' : value.toLocaleString('zh-CN')
}

function MetricValue({ value, isPending }: { value: number | undefined; isPending: boolean }) {
  if (isPending) {
    return <Skeleton className="h-8 w-24" />
  }

  return <div className="text-3xl leading-none font-medium tracking-tight tabular-nums">{formatNumber(value)}</div>
}

function GrowthBadge({ growth }: { growth: number | undefined }) {
  if (growth === undefined || Number.isNaN(growth)) {
    return null
  }

  const isPositive = growth >= 0
  const Icon = isPositive ? TrendingUp : TrendingDown

  return (
    <Badge variant={isPositive ? 'secondary' : 'destructive'}>
      <Icon className="size-3" />
      {isPositive ? '+' : ''}
      {growth}%
    </Badge>
  )
}

export function DashboardMetrics() {
  const pv = useQuery({
    queryKey: dashboardKeys.overviewPv(),
    queryFn: ({ signal }) => getDashboardPvOverview(signal)
  })

  const ip = useQuery({
    queryKey: dashboardKeys.overviewIp(),
    queryFn: ({ signal }) => getDashboardIpOverview(signal)
  })

  return (
    <div className="grid grid-cols-1 gap-4 *:data-[slot=card]:bg-linear-to-t *:data-[slot=card]:from-primary/5 *:data-[slot=card]:to-card *:data-[slot=card]:shadow-xs sm:grid-cols-2 xl:grid-cols-4 dark:*:data-[slot=card]:bg-card">
      <Card>
        <CardHeader>
          <div className="flex items-center gap-2">
            <Activity className="size-4 text-muted-foreground" />
            <CardDescription>累计访问量</CardDescription>
          </div>
        </CardHeader>
        <CardContent className="flex flex-col gap-1">
          <MetricValue value={pv.data?.total} isPending={pv.isPending} />
          <p className="text-sm text-muted-foreground">系统累计访问次数（PV）</p>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <div className="flex items-center gap-2">
            <CalendarCheck className="size-4 text-muted-foreground" />
            <CardDescription>今日访问量</CardDescription>
          </div>
        </CardHeader>
        <CardContent className="flex flex-col gap-1">
          <div className="flex flex-wrap items-center gap-2">
            <MetricValue value={pv.data?.today} isPending={pv.isPending} />
            <GrowthBadge growth={pv.data?.growth} />
          </div>
          <p className="text-sm text-muted-foreground">较昨日访问量变化</p>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <div className="flex items-center gap-2">
            <Globe2 className="size-4 text-muted-foreground" />
            <CardDescription>累计独立 IP</CardDescription>
          </div>
        </CardHeader>
        <CardContent className="flex flex-col gap-1">
          <MetricValue value={ip.data?.total} isPending={ip.isPending} />
          <p className="text-sm text-muted-foreground">系统累计访问 IP 数</p>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <div className="flex items-center gap-2">
            <UsersRound className="size-4 text-muted-foreground" />
            <CardDescription>今日独立 IP</CardDescription>
          </div>
        </CardHeader>
        <CardContent className="flex flex-col gap-1">
          <div className="flex flex-wrap items-center gap-2">
            <MetricValue value={ip.data?.today} isPending={ip.isPending} />
            <GrowthBadge growth={ip.data?.growth} />
          </div>
          <p className="text-sm text-muted-foreground">较昨日独立 IP 变化</p>
        </CardContent>
      </Card>
    </div>
  )
}
