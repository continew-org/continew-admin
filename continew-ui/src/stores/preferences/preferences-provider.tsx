import { createContext, use, useEffect, useState } from 'react'

import { type StoreApi, useStore } from 'zustand'

import { getClientCookie } from '@/lib/cookie'
import { getLocalStorageValue } from '@/lib/local-storage'
import {
  PREFERENCE_DEFAULTS,
  PREFERENCE_KEYS,
  PREFERENCE_REGISTRY,
  type PreferenceKey,
  type PreferenceValueMap,
  getPreferenceStorageKey,
  parsePreference
} from '@/lib/preferences/preferences-config'
import { applyThemeMode, subscribeToSystemTheme } from '@/lib/preferences/theme-utils'

import { createPreferencesStore, type PreferencesState } from './preferences-store'

const PreferencesStoreContext = createContext<StoreApi<PreferencesState> | null>(null)

function readDomPreference<K extends PreferenceKey>(key: K): PreferenceValueMap[K] {
  const definition = PREFERENCE_REGISTRY[key]
  const root = document.documentElement

  if (definition.attribute) {
    return parsePreference(key, root.getAttribute(definition.attribute))
  }

  if (definition.cssVar) {
    const name = Array.isArray(definition.cssVar) ? definition.cssVar[0] : definition.cssVar
    return parsePreference(key, window.getComputedStyle(root).getPropertyValue(name))
  }

  // JS-only preferences (boolean/text) have no DOM target, so read the stored value directly.
  const storageKey = getPreferenceStorageKey(key)
  const raw =
    definition.persistence === 'localStorage'
      ? getLocalStorageValue(storageKey)
      : definition.persistence === 'client-cookie'
        ? getClientCookie(storageKey)
        : null

  return parsePreference(key, raw)
}

function readDomPreferences(): PreferenceValueMap {
  const values = { ...PREFERENCE_DEFAULTS }

  function assignPreference<K extends PreferenceKey>(key: K) {
    values[key] = readDomPreference(key)
  }

  for (const key of PREFERENCE_KEYS) assignPreference(key)
  return values
}

export function PreferencesStoreProvider({ children }: { children: React.ReactNode }) {
  const [store] = useState<StoreApi<PreferencesState>>(() => createPreferencesStore())

  useEffect(() => {
    store.setState({
      values: readDomPreferences(),
      resolvedThemeMode: document.documentElement.classList.contains('dark') ? 'dark' : 'light',
      isSynced: true
    })
  }, [store])

  useEffect(() => {
    let unsubscribeMedia: (() => void) | undefined

    const subscribeForMode = (mode: PreferenceValueMap['theme_mode']) => {
      unsubscribeMedia?.()
      unsubscribeMedia = undefined

      if (mode === 'system') {
        unsubscribeMedia = subscribeToSystemTheme(() => {
          store.setState({ resolvedThemeMode: applyThemeMode('system') })
        })
      }
    }

    subscribeForMode(store.getState().values.theme_mode)

    const unsubscribeStore = store.subscribe((state, previousState) => {
      if (state.values.theme_mode !== previousState.values.theme_mode) {
        subscribeForMode(state.values.theme_mode)
      }
    })

    return () => {
      unsubscribeMedia?.()
      unsubscribeStore()
    }
  }, [store])

  return <PreferencesStoreContext.Provider value={store}>{children}</PreferencesStoreContext.Provider>
}

export function usePreferencesStore<T>(selector: (state: PreferencesState) => T): T {
  const store = use(PreferencesStoreContext)
  if (!store) throw new Error('Missing PreferencesStoreProvider')
  return useStore(store, selector)
}
