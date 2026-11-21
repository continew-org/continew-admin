import { clearCachedMenu } from './menu-cache'

const ACCESS_TOKEN_KEY = 'continew.access-token'
const TENANT_ID_KEY = 'continew.tenant-id'

let accessToken: string | null = null
let tenantId: string | null = null
let authGeneration = 0

const isPersisted = (key: string) => window.localStorage.getItem(key) !== null

const toTenantId = (value: string | number | null | undefined) =>
  value === null || value === undefined ? null : String(value)

function readStored(key: string): string | null {
  try {
    return window.localStorage.getItem(key) ?? window.sessionStorage.getItem(key)
  } catch {
    return null
  }
}

function writeStored(key: string, value: string | null, persist?: boolean) {
  try {
    if (value === null) {
      window.localStorage.removeItem(key)
      window.sessionStorage.removeItem(key)
      return
    }

    const target = persist ? window.localStorage : window.sessionStorage
    const other = persist ? window.sessionStorage : window.localStorage
    target.setItem(key, value)
    other.removeItem(key)
  } catch (error) {
    if (import.meta.env.DEV) {
      console.error('[auth-session] Failed to persist session value:', error)
    }
  }
}

export function hydrateSession() {
  accessToken = readStored(ACCESS_TOKEN_KEY)
  tenantId = readStored(TENANT_ID_KEY)
}

export function getAccessToken(): string | null {
  return accessToken
}

export function getTenantId(): string | null {
  return tenantId
}

export function getSessionId(): string | null {
  const payload = accessToken?.split('.')[1]

  if (!payload) {
    return null
  }

  try {
    const claims = JSON.parse(atob(payload.replace(/-/g, '+').replace(/_/g, '/'))) as { sid?: unknown }
    return typeof claims.sid === 'string' ? claims.sid : null
  } catch {
    return null
  }
}

/** Increments whenever the session is replaced or cleared. */
export function getAuthGeneration(): number {
  return authGeneration
}

export function setAccessToken(token: string) {
  accessToken = token
  writeStored(ACCESS_TOKEN_KEY, token, isPersisted(ACCESS_TOKEN_KEY))
}

export function setTenantId(value: string | number | null) {
  tenantId = toTenantId(value)
  writeStored(TENANT_ID_KEY, tenantId, isPersisted(TENANT_ID_KEY))
}

export function setSession(next: { accessToken: string; tenantId?: string | number | null; persist?: boolean }) {
  const persist = next.persist ?? true
  authGeneration += 1
  accessToken = next.accessToken
  tenantId = toTenantId(next.tenantId)
  writeStored(ACCESS_TOKEN_KEY, accessToken, persist)
  writeStored(TENANT_ID_KEY, tenantId, persist)
}

export function clearSession() {
  accessToken = null
  tenantId = null
  authGeneration += 1
  writeStored(ACCESS_TOKEN_KEY, null)
  writeStored(TENANT_ID_KEY, null)
  clearCachedMenu()
}
