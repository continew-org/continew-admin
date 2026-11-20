import { redirect } from '@tanstack/react-router'

import { getAccessToken } from './auth-session'

const PUBLIC_PATHS = ['/auth', '/unauthorized']

function isPublicPath(pathname: string) {
  return PUBLIC_PATHS.some(path => pathname === path || pathname.startsWith(`${path}/`))
}

/** Runs in `beforeLoad`, outside React, so it reads the module session instead of the store. */
export function requireAuth({ location }: { location: { href: string; pathname: string } }) {
  if (isPublicPath(location.pathname) || getAccessToken()) {
    return
  }

  throw redirect({ to: '/auth/login', search: { redirect: location.href } })
}
