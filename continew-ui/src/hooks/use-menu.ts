import { useMemo } from 'react'

import { CancelledError, queryOptions, useQuery } from '@tanstack/react-query'

import { getUserRoutes } from '@/lib/api/auth'
import type { RouteResp } from '@/lib/api/types'
import { ApiError } from '@/lib/http'
import { getAccessToken } from '@/lib/auth-session'
import { getCachedMenu, setCachedMenu } from '@/lib/menu-cache'
import { queryClient } from '@/lib/query-client'
import { toMenuGroups } from '@/navigation/menu'
import { useAuthStore } from '@/stores/auth/auth-provider'

/** The menu only changes when the signed-in user or its roles change. */
export const MENU_QUERY_KEY = ['auth', 'menu'] as const

export const menuQueryOptions = queryOptions({
  queryKey: MENU_QUERY_KEY,
  queryFn: async ({ signal }) => {
    const routes = await getUserRoutes(signal)
    setCachedMenu(routes)
    return routes
  },
  initialData: () => getCachedMenu() ?? undefined
})

export function useMenuRoutes() {
  const status = useAuthStore(state => state.status)
  return useQuery({
    ...menuQueryOptions,
    enabled: status === 'authenticated' && Boolean(getAccessToken())
  })
}

/**
 * Ensures menu data is present before the shell renders and returns it.
 *
 * If cached menu data already exists (from memory or localStorage), this resolves
 * immediately (0ms) so the shell mounts without delay. In the background, it
 * triggers a revalidation to keep data fresh without holding up the UI.
 *
 * Only when there is no cached data at all (first login/cold start) will it await
 * the network request to prevent flashing empty static menus.
 */
export async function ensureMenuLoaded(): Promise<RouteResp[]> {
  const cached = queryClient.getQueryData<RouteResp[]>(MENU_QUERY_KEY) ?? getCachedMenu()
  if (cached) {
    if (!queryClient.getQueryData(MENU_QUERY_KEY)) {
      queryClient.setQueryData(MENU_QUERY_KEY, cached)
    }
    void queryClient.query({ ...menuQueryOptions, staleTime: 0 }).catch(() => {})
    return cached
  }

  try {
    return await queryClient.query({ ...menuQueryOptions, staleTime: 'static' })
  } catch (error) {
    if (!(error instanceof ApiError) && !(error instanceof CancelledError)) {
      throw error
    }
    return queryClient.getQueryData<RouteResp[]>(MENU_QUERY_KEY) ?? []
  }
}

/**
 * Sidebar groups: the app's own static routes first, then the menu the backend allows
 * this user to see. The static entries do not wait for the request, so `/dashboard`
 * stays reachable even when the menu call fails or the user has no roles.
 */
export function useMenuGroups() {
  const { data, isPending } = useMenuRoutes()

  return {
    groups: useMemo(() => toMenuGroups(data ?? []), [data]),
    isPending
  }
}
