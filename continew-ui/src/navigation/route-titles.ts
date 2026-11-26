/** Where signing in lands, and the tab the shell keeps pinned. */
export const HOME_PATH = '/dashboard/workplace'

/** The analysis page, beside the home path so the menu and the lookups cannot drift apart. */
export const ANALYSIS_PATH = '/dashboard/analysis'

/** The dashboard root redirects to the workbench. */
export const DASHBOARD_PATH = '/dashboard'

/**
 * Project links shown by the header help card and the workbench welcome band.
 *
 * They live here rather than in either component so a documentation version bump cannot update
 * one entry point and leave the other on the previous release.
 */
export const PROJECT_LINKS = {
  repository: 'https://github.com/continew-org/continew-admin',
  starterRepository: 'https://github.com/continew-org/continew-starter',
  docs: 'https://continew.top/docs/admin/5.x',
  website: 'https://continew.top',
  releases: 'https://github.com/continew-org/continew-admin/releases',
  faq: 'https://continew.top/docs/admin/5.x/reference/faq',
  whyChooseUs: 'https://continew.top/docs/admin/5.x/why-choose-us',
  sponsor: 'https://continew.top/sponsor'
} as const

/** Titles for the app's static dashboard routes, shared by the header tabs and the breadcrumb. */
export const STATIC_ROUTE_TITLES: Record<string, string> = {
  [DASHBOARD_PATH]: '仪表盘',
  [HOME_PATH]: '工作台',
  [ANALYSIS_PATH]: '分析页',
  '/dashboard/profile': '个人中心',
  '/user/message': '消息中心'
}

/** Menu icon names for the same static routes, used by the header tabs. */
export const STATIC_ROUTE_ICONS: Record<string, string> = {
  [DASHBOARD_PATH]: 'dashboard',
  [HOME_PATH]: 'desktop',
  [ANALYSIS_PATH]: 'chart',
  '/dashboard/profile': 'user',
  '/user/message': 'bell'
}

/** `user-management` becomes `User Management`, for routes with no static or backend title. */
export function formatSegmentTitle(segment: string) {
  return segment
    .split(/[-_]/)
    .map(word => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ')
}
