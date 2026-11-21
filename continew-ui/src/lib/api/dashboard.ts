import { http } from '@/lib/http'

/** One label/value pair used by the dashboard charts. */
export interface DashboardChartItem {
  name: string
  value: number
}

/** PV / IP overview; `growth` is the percentage change against yesterday. */
export interface DashboardOverviewResp {
  total: number
  today: number
  growth: number
  dataList: DashboardChartItem[]
}

/** One day of the access trend. */
export interface DashboardAccessTrendResp {
  date: string
  pvCount: number
  ipCount: number
}

/** One published notice shown on the dashboard. */
export interface DashboardNoticeResp {
  id: string | number
  title: string
  type: string
  isTop: boolean
}

export type DashboardRange = 7 | 30

export function getDashboardNotices(signal?: AbortSignal) {
  return http.get<DashboardNoticeResp[]>('/dashboard/notice', { signal })
}

export function getDashboardPvOverview(signal?: AbortSignal) {
  return http.get<DashboardOverviewResp>('/dashboard/analysis/overview/pv', { signal })
}

export function getDashboardIpOverview(signal?: AbortSignal) {
  return http.get<DashboardOverviewResp>('/dashboard/analysis/overview/ip', { signal })
}

export function getDashboardAccessTrend(days: DashboardRange, signal?: AbortSignal) {
  return http.get<DashboardAccessTrendResp[]>(`/dashboard/access/trend/${days}`, { signal })
}

export function getDashboardGeo(signal?: AbortSignal) {
  return http.get<DashboardChartItem[]>('/dashboard/analysis/geo', { signal })
}

export function getDashboardTimeslot(signal?: AbortSignal) {
  return http.get<DashboardChartItem[]>('/dashboard/analysis/timeslot', { signal })
}

export function getDashboardModule(signal?: AbortSignal) {
  return http.get<DashboardChartItem[]>('/dashboard/analysis/module', { signal })
}

export function getDashboardOs(signal?: AbortSignal) {
  return http.get<DashboardChartItem[]>('/dashboard/analysis/os', { signal })
}

export function getDashboardBrowser(signal?: AbortSignal) {
  return http.get<DashboardChartItem[]>('/dashboard/analysis/browser', { signal })
}

export { dashboardKeys } from '@/lib/query-keys'
