import { useQuery } from '@tanstack/react-query'

import {
  dashboardKeys,
  getDashboardBrowser,
  getDashboardModule,
  getDashboardOs,
  getDashboardTimeslot
} from '@/lib/api/dashboard'

import { ChartCard, DistributionPieChart, RankBarChart } from './dashboard-charts'

export function DashboardTimeslot() {
  const query = useQuery({
    queryKey: dashboardKeys.timeslot(),
    queryFn: ({ signal }) => getDashboardTimeslot(signal)
  })

  const data = query.data ?? []

  return (
    <ChartCard
      title="访问时段"
      description="按两小时划分的访问分布"
      isPending={query.isPending}
      isError={query.isError}
      isEmpty={!data.some(item => item.value > 0)}
      onRetry={() => void query.refetch()}
    >
      <RankBarChart data={data} />
    </ChartCard>
  )
}

export function DashboardModule() {
  const query = useQuery({
    queryKey: dashboardKeys.module(),
    queryFn: ({ signal }) => getDashboardModule(signal)
  })

  const data = (query.data ?? []).map(item => ({
    ...item,
    name: item.name.length > 8 ? `${item.name.slice(0, 8)}…` : item.name
  }))

  return (
    <ChartCard
      title="模块访问 Top10"
      description="按访问量排序的功能模块"
      isPending={query.isPending}
      isError={query.isError}
      isEmpty={!data.some(item => item.value > 0)}
      onRetry={() => void query.refetch()}
    >
      <RankBarChart data={data} vertical />
    </ChartCard>
  )
}

export function DashboardOs() {
  const query = useQuery({
    queryKey: dashboardKeys.os(),
    queryFn: ({ signal }) => getDashboardOs(signal)
  })

  const data = query.data ?? []

  return (
    <ChartCard
      title="终端分布"
      description="访问终端操作系统的占比"
      isPending={query.isPending}
      isError={query.isError}
      isEmpty={!data.some(item => item.value > 0)}
      onRetry={() => void query.refetch()}
    >
      <DistributionPieChart data={data} />
    </ChartCard>
  )
}

export function DashboardBrowser() {
  const query = useQuery({
    queryKey: dashboardKeys.browser(),
    queryFn: ({ signal }) => getDashboardBrowser(signal)
  })

  const data = query.data ?? []

  return (
    <ChartCard
      title="浏览器分布"
      description="访问浏览器的占比"
      isPending={query.isPending}
      isError={query.isError}
      isEmpty={!data.some(item => item.value > 0)}
      onRetry={() => void query.refetch()}
    >
      <DistributionPieChart data={data} />
    </ChartCard>
  )
}
