import { useRouter, useRouterState, type RegisteredRouter } from '@tanstack/react-router'

import { useMenuRoutes } from '@/hooks/use-menu'
import type { RouteResp } from '@/lib/api/types'
import { findMenuByPath } from '@/navigation/menu'
import { formatSegmentTitle, STATIC_ROUTE_ICONS, STATIC_ROUTE_TITLES } from '@/navigation/route-titles'

export interface RouteInfo {
  title: string
  icon: string
}

/**
 * Title and icon for a pathname: static routes first, then the backend menu. Real routes outside
 * the menu fall back to a title generated from the last segment; unknown paths (404s) return null.
 */
export function resolveRouteInfo(pathname: string, menuRoutes: readonly RouteResp[], router: RegisteredRouter) {
  if (pathname === '/dashboard' || pathname === '/dashboard/') {
    return { title: '仪表盘', icon: 'dashboard' }
  }

  if (STATIC_ROUTE_TITLES[pathname]) {
    return { title: STATIC_ROUTE_TITLES[pathname], icon: STATIC_ROUTE_ICONS[pathname] ?? 'common' }
  }

  const entry = findMenuByPath(menuRoutes, pathname)
  if (entry?.title) {
    return { title: entry.title, icon: entry.icon ?? 'common' }
  }

  if (!(pathname in router.routesByPath)) {
    return null
  }

  const segment = pathname.split('/').filter(Boolean).pop()
  return { title: segment ? formatSegmentTitle(segment) : '新标签页', icon: 'common' }
}

export function usePathname() {
  return useRouterState({ select: state => state.location.pathname })
}

/** Route info for the current page; re-renders only when the pathname or the menu changes. */
export function useCurrentRouteInfo(): RouteInfo | null {
  const router = useRouter()
  const pathname = usePathname()
  const { data: menuRoutes } = useMenuRoutes()

  return resolveRouteInfo(pathname, menuRoutes ?? [], router)
}
