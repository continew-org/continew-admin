import { setClientCookie } from '../cookie'
import { setLocalStorageValue } from '../local-storage'
import {
  getPreferencePersistence,
  getPreferenceStorageKey,
  type PreferenceKey,
  type PreferencePersistence,
  type PreferenceValueMap
} from './preferences-config'

async function persistByMode(mode: PreferencePersistence, key: string, value: string): Promise<void> {
  switch (mode) {
    case 'none':
      return

    case 'client-cookie':
      setClientCookie(key, value)
      return

    case 'localStorage':
      setLocalStorageValue(key, value)
      return
  }
}

export function persistPreference<K extends PreferenceKey>(key: K, value: PreferenceValueMap[K]): Promise<void> {
  return persistByMode(getPreferencePersistence(key), getPreferenceStorageKey(key), String(value))
}
