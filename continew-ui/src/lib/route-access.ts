import { redirect } from '@tanstack/react-router'

import type { RouteResp } from '@/lib/api/types'
import { isPathAllowed } from '@/navigation/menu'
import { HOME_PATH } from '@/navigation/route-titles'

/**
 * Keeps a caller-supplied redirect target on this site.
 *
 * A bare `startsWith('/')` also accepts `//evil.example`, which the router treats as a different
 * origin. Resolve against the current origin and keep only same-origin paths (with their query
 * and fragment); anything else falls back to the workbench.
 */
export function safeLocalHref(target: unknown, fallback: string = HOME_PATH, base?: string): string {
  if (typeof target !== 'string' || !target.startsWith('/') || target.startsWith('//')) {
    return fallback
  }

  try {
    const current = new URL(base ?? window.location.href)
    const resolved = new URL(target, current)
    return resolved.origin === current.origin ? `${resolved.pathname}${resolved.search}${resolved.hash}` : fallback
  } catch {
    return fallback
  }
}

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
export function requireRouteAccess(pathname: string, matches: readonly RouteMatchLike[], routes: readonly RouteResp[]) {
  if (matches.at(-1)?.routeId.endsWith('/$') || routes.length === 0) {
    return
  }

  if (!isPathAllowed(routes, pathname)) {
    throw redirect({ to: '/unauthorized' })
  }
}
