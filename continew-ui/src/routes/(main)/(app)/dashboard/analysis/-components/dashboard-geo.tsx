import { useQuery } from '@tanstack/react-query'

import { dashboardKeys, getDashboardGeo } from '@/lib/api/dashboard'

import { ChartCard, RankBarChart } from './dashboard-charts'

export function DashboardGeo() {
  const query = useQuery({
    queryKey: dashboardKeys.geo(),
    queryFn: ({ signal }) => getDashboardGeo(signal)
  })

  const data = [...(query.data ?? [])]
    .filter(item => item.value > 0)
    .sort((a, b) => b.value - a.value)
    .slice(0, 10)

  return (
    <ChartCard
      title="地域分布"
      description="访问量最高的 10 个地区"
      isPending={query.isPending}
      isError={query.isError}
      isEmpty={data.length === 0}
      onRetry={() => void query.refetch()}
    >
      <RankBarChart data={data} vertical />
    </ChartCard>
  )
}
