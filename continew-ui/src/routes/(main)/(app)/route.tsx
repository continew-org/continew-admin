import { createFileRoute, Outlet } from '@tanstack/react-router'

import { BaseLayout, loadBaseLayout, PagePending } from '@/layouts'
import { QueryState } from '@/components/query-state'
import { ensureMenuLoaded } from '@/hooks/use-menu'
import { requireAuth } from '@/lib/auth-guard'
import { requireRouteAccess } from '@/lib/route-access'
import { NotFound } from '@/routes/-components/not-found'
import { useAuthStore } from '@/stores/auth/auth-provider'

export const Route = createFileRoute('/(main)/(app)')({
  beforeLoad: async ({ location, matches }) => {
    requireAuth({ location })

    // The menu decides which pages exist for this user, so it has to settle before the shell
    // renders; a session that expired during that request must not slip through.
    const routes = await ensureMenuLoaded()
    requireAuth({ location })
    requireRouteAccess(location.pathname, matches, routes)
  },
  loader: loadBaseLayout,
  component: AppLayout,
  notFoundComponent: NotFound
})

function AppLayout() {
  return (
    <BaseLayout {...Route.useLoaderData()}>
      <AuthenticatedOutlet />
    </BaseLayout>
  )
}

/**
 * Holds the page back until the signed-in user's profile has settled.
 *
 * A restored token still starts with an empty permission set, so painting the page
 * earlier would draw permission-gated actions out of nothing and then move them once
 * `GET /auth/user/info` lands.
 */
function AuthenticatedOutlet() {
  const status = useAuthStore(state => state.status)
  const isLoadingUser = useAuthStore(state => state.isLoadingUser)
  const hasUser = useAuthStore(state => state.user !== null)
  const loadUser = useAuthStore(state => state.loadUser)

  // Signing out drops the profile before the redirect lands; keep the shell empty.
  if (status === 'anonymous') {
    return null
  }

  // A profile refresh (avatar, nickname) must not unmount the page that triggered it.
  if (hasUser) {
    return <Outlet />
  }

  if (isLoadingUser) {
    return <PagePending />
  }

  return (
    <QueryState
      state="error"
      title="用户信息加载失败"
      className="min-h-[calc(100dvh-8rem)]"
      onRetry={() => void loadUser()}
    />
  )
}
