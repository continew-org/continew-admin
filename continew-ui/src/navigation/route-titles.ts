/** Titles for the app's static dashboard routes, shared by the header tabs and the breadcrumb. */
export const STATIC_ROUTE_TITLES: Record<string, string> = {
  '/dashboard': '仪表盘',
  '/dashboard/profile': '个人中心'
}

/** Menu icon names for the same static routes, used by the header tabs. */
export const STATIC_ROUTE_ICONS: Record<string, string> = {
  '/dashboard': 'dashboard',
  '/dashboard/profile': 'user'
}

/** `user-management` becomes `User Management`, for routes with no static or backend title. */
export function formatSegmentTitle(segment: string) {
  return segment
    .split(/[-_]/)
    .map(word => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ')
}
