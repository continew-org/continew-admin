import { useMemo } from 'react'

import { hasPermission } from '@/lib/permission'
import { useAuthStore } from '@/stores/auth/auth-provider'

/**
 * Reads the permission codes returned by `GET /auth/user/info`.
 *
 * Call it once per screen and pass the results down: every call subscribes to the auth
 * store, so calling it inside a table row turns one subscription into one per row.
 *
 * Permission codes follow the backend convention, for example `system:user:create`
 * for `POST /system/user`. The returned functions keep their identity until the granted
 * codes change, so they are safe to use as `useMemo`/`useCallback` dependencies.
 */
export function usePermission() {
  const permissions = useAuthStore(state => state.permissions)

  return useMemo(
    () => ({
      permissions,
      hasPermission: (code?: string) => hasPermission(permissions, code),
      hasAnyPermission: (codes: readonly string[]) => codes.some(code => hasPermission(permissions, code))
    }),
    [permissions]
  )
}
