import type { LabelValueResp } from './api/common'
import { getLocalStorageJson, setLocalStorageJson } from './local-storage'

const SITE_OPTION_CACHE_KEY = 'continew.site-options'

/** Reads the cached site options used to render the shell before the first query resolves. */
export function getCachedSiteOptions(): LabelValueResp<string>[] | null {
  const parsed = getLocalStorageJson<unknown>(SITE_OPTION_CACHE_KEY)
  return Array.isArray(parsed) ? (parsed as LabelValueResp<string>[]) : null
}

export function setCachedSiteOptions(options: LabelValueResp<string>[]) {
  setLocalStorageJson(SITE_OPTION_CACHE_KEY, options)
}
