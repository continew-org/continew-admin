import type { RouteResp } from './api/types'
import { getLocalStorageJson, removeLocalStorageValue, setLocalStorageJson } from './local-storage'

const MENU_CACHE_KEY = 'continew.menu-cache'

/** Reads the cached menu routes; missing or malformed entries yield null. */
export function getCachedMenu(): RouteResp[] | null {
  const parsed = getLocalStorageJson<unknown>(MENU_CACHE_KEY)
  return Array.isArray(parsed) ? (parsed as RouteResp[]) : null
}

export function setCachedMenu(routes: RouteResp[]) {
  setLocalStorageJson(MENU_CACHE_KEY, routes)
}

export function clearCachedMenu() {
  removeLocalStorageValue(MENU_CACHE_KEY)
}
