import { createFileRoute } from '@tanstack/react-router'

import { DashboardAccessTrend } from './-components/dashboard-access-trend'
import {
  DashboardBrowser,
  DashboardModule,
  DashboardOs,
  DashboardTimeslot
} from './-components/dashboard-distributions'
import { DashboardGeo } from './-components/dashboard-geo'
import { DashboardMetrics } from './-components/dashboard-metrics'

/** The analysis page: access trends and running overview of this Admin instance. */
export const Route = createFileRoute('/(main)/(app)/dashboard/analysis')({
  component: Page
})

function Page() {
  return (
    <div className="@container/main flex flex-col gap-4 md:gap-6">
      <h1 className="sr-only">分析页</h1>
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

      <DashboardGeo />
    </div>
  )
}
