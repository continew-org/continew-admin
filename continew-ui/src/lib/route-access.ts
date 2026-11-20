import { redirect } from '@tanstack/react-router'

import type { RouteResp } from '@/lib/api/types'
import { isPathAllowed } from '@/navigation/menu'

/** The shape the guard needs from a route match; the router passes a richer object. */
interface RouteMatchLike {
  readonly routeId: string
}

/**
 * Guards a page the current user's menu does not contain.
 *
 * The menu comes from `GET /auth/user/route`, which the backend already filters by
 * permission, so a missing entry means "no access" rather than "hidden". Two cases pass
 * deliberately: unknown paths, which the `$` routes answer with their own 404, and an empty
 * menu, because locking every user out over a failed menu request is worse than a page whose
 * own requests answer 403.
 */
export function requireRouteAccess(
  pathname: string,
  matches: readonly RouteMatchLike[],
  routes: readonly RouteResp[]
) {
  if (matches.at(-1)?.routeId.endsWith('/$') || routes.length === 0) {
    return
  }

  if (!isPathAllowed(routes, pathname)) {
    throw redirect({ to: '/unauthorized' })
  }
}
