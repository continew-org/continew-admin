import { createFileRoute } from '@tanstack/react-router'

import { useAuthStore } from '@/stores/auth/auth-provider'

import { DashboardAccessTrend } from './-components/dashboard-access-trend'
import {
  DashboardBrowser,
  DashboardModule,
  DashboardOs,
  DashboardTimeslot
} from './-components/dashboard-distributions'
import { DashboardGeo } from './-components/dashboard-geo'
import { DashboardMetrics } from './-components/dashboard-metrics'
import { DashboardNotices } from './-components/dashboard-notices'

/**
 * The dashboard is a static route: it is not part of the backend menu, so `/dashboard`
 * stays reachable for every signed-in user and is where login lands.
 */
export const Route = createFileRoute('/(main)/(app)/dashboard/')({
  component: Page
})

function Page() {
  const user = useAuthStore(state => state.user)
  const displayName = user?.nickname || user?.username || '朋友'

  return (
    <div className="@container/main flex flex-col gap-4 md:gap-6">
      <div className="space-y-1">
        <h1 className="text-3xl tracking-tight">你好，{displayName}</h1>
        <p className="text-sm text-muted-foreground">这里汇总系统访问趋势与运行概况。</p>
      </div>

      <DashboardMetrics />

      <div className="grid grid-cols-1 items-stretch gap-4 xl:grid-cols-3">
        <div className="xl:col-span-2">
          <DashboardAccessTrend />
        </div>
        <DashboardTimeslot />
      </div>

      <div className="grid grid-cols-1 items-stretch gap-4 md:grid-cols-2 xl:grid-cols-3">
        <DashboardModule />
        <DashboardOs />
        <DashboardBrowser />
      </div>

      <div className="grid grid-cols-1 items-stretch gap-4 xl:grid-cols-3">
        <div className="xl:col-span-2">
          <DashboardGeo />
        </div>
        <DashboardNotices />
      </div>
    </div>
  )
}
