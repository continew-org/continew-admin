import { createRootRoute, Outlet } from '@tanstack/react-router'

import { Toaster } from '@/components/ui/toast'
import { TooltipProvider } from '@/components/ui/tooltip'
import { AuthStoreProvider } from '@/stores/auth/auth-provider'
import { PreferencesStoreProvider } from '@/stores/preferences/preferences-provider'

import { NotFound } from './-components/not-found'
import { RootError } from './-components/root-error'

export const Route = createRootRoute({
  component: RootComponent,
  errorComponent: RootError,
  notFoundComponent: NotFound
})

function RootComponent() {
  return (
    <TooltipProvider>
      <AuthStoreProvider>
        <PreferencesStoreProvider>
          <Outlet />
          <Toaster />
        </PreferencesStoreProvider>
      </AuthStoreProvider>
    </TooltipProvider>
  )
}
