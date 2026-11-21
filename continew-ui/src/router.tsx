import { createRouter } from '@tanstack/react-router'

import { PagePending } from '@/components/app-shell/page-pending'

import { routeTree } from './routeTree.gen'

export function getRouter() {
  return createRouter({
    routeTree,
    // Mirrors Vite's `base` so router links keep the deploy sub-path (GitHub Pages project sites).
    basepath: import.meta.env.BASE_URL,
    scrollRestoration: true,
    defaultPreload: 'intent',
    defaultPendingComponent: PagePending,
    defaultPendingMs: 150,
    defaultPendingMinMs: 300
  })
}

declare module '@tanstack/react-router' {
  interface Register {
    router: ReturnType<typeof getRouter>
  }
}
