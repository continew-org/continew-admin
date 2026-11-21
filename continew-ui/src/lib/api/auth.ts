import { http } from '@/lib/http'

import type {
  AccountLoginReq,
  LoginResp,
  RouteResp,
  SocialAuthAuthorizeResp,
  SocialLoginReq,
  UserInfoResp
} from './types'

/**
 * Logs in with an account.
 *
 * The password must already be RSA encrypted (see `@/lib/crypto`), and the caller
 * handles failures so it can refresh the captcha.
 */
export function login(req: AccountLoginReq, tenantCode?: string) {
  return http.post<LoginResp>('/auth/login', req, {
    headers: tenantCode ? { 'X-Tenant-Code': tenantCode } : undefined,
    skipAuthRefresh: true,
    silent: true
  })
}

/** Returns the provider URL that starts a third-party login. */
export function socialAuthorize(source: string) {
  return http.get<SocialAuthAuthorizeResp>(`/auth/${source}`, {
    skipAuthRefresh: true,
    silent: true
  })
}

/** Completes a third-party login with the query the provider redirected back with. */
export function socialLogin(req: SocialLoginReq) {
  return http.post<LoginResp>('/auth/login', req, {
    skipAuthRefresh: true,
    silent: true
  })
}

/** Clears the server session and the HttpOnly refresh cookie. */
export function logout() {
  return http.post<void>('/auth/logout', undefined, { skipAuthRefresh: true, silent: true })
}

/** Returns the current user with its permissions, roles and department. */
export function getUserInfo() {
  return http.get<UserInfoResp>('/auth/user/info')
}

/** Returns the menu tree the current user is allowed to see. */
export function getUserRoutes(signal?: AbortSignal) {
  return http.get<RouteResp[]>('/auth/user/route', { signal })
}
