import { getClientCookie, setClientCookie } from '../cookie'
import { getLocalStorageValue, removeLocalStorageValue, setLocalStorageValue } from '../local-storage'
import {
  getPreferenceStorageKey,
  PREFERENCE_DEFAULTS,
  PREFERENCE_KEYS,
  PREFERENCE_REGISTRY,
  type PreferenceKey,
  type PreferenceValueMap,
  parsePreference
} from './preferences-config'

function readRaw(key: PreferenceKey) {
  const storageKey = getPreferenceStorageKey(key)
  return PREFERENCE_REGISTRY[key].persistence === 'client-cookie'
    ? getClientCookie(storageKey)
    : getLocalStorageValue(storageKey)
}

/** Reads every saved preference synchronously; the SPA has no server render to hydrate against. */
export function readStoredPreferences(): PreferenceValueMap {
  const values = { ...PREFERENCE_DEFAULTS }

  function assign<K extends PreferenceKey>(key: K) {
    values[key] = parsePreference(key, readRaw(key))
  }

  for (const key of PREFERENCE_KEYS) assign(key)
  return values
}

/** Only values that differ from the default are kept in localStorage. */
export function persistPreference<K extends PreferenceKey>(key: K, value: PreferenceValueMap[K]) {
  const storageKey = getPreferenceStorageKey(key)

  if (PREFERENCE_REGISTRY[key].persistence === 'client-cookie') {
    setClientCookie(storageKey, String(value))
    return
  }

  if (value === PREFERENCE_DEFAULTS[key]) removeLocalStorageValue(storageKey)
  else setLocalStorageValue(storageKey, String(value))
}
