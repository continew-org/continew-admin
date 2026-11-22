import { createStore } from 'zustand/vanilla'

import { applyPreference } from '@/lib/preferences/preference-runtime'
import {
  PREFERENCE_DEFAULTS,
  PREFERENCE_KEYS,
  type PreferenceKey,
  type PreferenceValueMap
} from '@/lib/preferences/preferences-config'
import { persistPreference, readStoredPreferences } from '@/lib/preferences/preferences-storage'
import type { ResolvedThemeMode } from '@/lib/preferences/theme'

export type PreferencesState = {
  values: PreferenceValueMap
  resolvedThemeMode: ResolvedThemeMode
  setPreference: <K extends PreferenceKey>(key: K, value: PreferenceValueMap[K]) => void
  /** Applies several values at once, so subscribers re-render a single time. */
  setPreferences: (patch: Partial<PreferenceValueMap>) => void
  resetPreferences: () => void
}

function applyAndPersist<K extends PreferenceKey>(key: K, value: PreferenceValueMap[K]) {
  const resolved = applyPreference(key, value)
  persistPreference(key, value)
  return resolved
}

export const createPreferencesStore = () => {
  const values = readStoredPreferences()

  // The boot script normally writes these to <html> before the first paint. If it failed (it
  // always sets data-theme-mode), apply everything once here so the session still matches.
  if (!document.documentElement.hasAttribute('data-theme-mode')) {
    for (const key of PREFERENCE_KEYS) applyPreference(key, values[key])
  }

  return createStore<PreferencesState>()((set, get) => ({
    values,
    resolvedThemeMode: document.documentElement.classList.contains('dark') ? 'dark' : 'light',

    setPreference: (key, value) => {
      if (get().values[key] === value) return
      get().setPreferences({ [key]: value } as Partial<PreferenceValueMap>)
    },

    setPreferences: patch => {
      const current = get().values
      let resolvedThemeMode: ResolvedThemeMode | undefined

      function update<K extends PreferenceKey>(key: K) {
        const value = patch[key] as PreferenceValueMap[K]
        if (value === undefined || value === current[key]) return
        resolvedThemeMode = applyAndPersist(key, value) ?? resolvedThemeMode
      }

      for (const key of Object.keys(patch) as PreferenceKey[]) update(key)

      set({
        values: { ...current, ...patch },
        ...(resolvedThemeMode ? { resolvedThemeMode } : {})
      })
    },

    resetPreferences: () => {
      get().setPreferences(
        Object.fromEntries(PREFERENCE_KEYS.map(key => [key, PREFERENCE_DEFAULTS[key]])) as PreferenceValueMap
      )
    }
  }))
}
