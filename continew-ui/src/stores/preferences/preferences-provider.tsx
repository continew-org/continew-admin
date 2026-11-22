import { createContext, use, useEffect, useState } from 'react'

import { AnimateUIProvider } from '@animate-ui/react/provider'

import { type StoreApi, useStore } from 'zustand'

import type { PreferenceValueMap } from '@/lib/preferences/preferences-config'
import { applyThemeMode, subscribeToSystemTheme } from '@/lib/preferences/theme-utils'

import { createPreferencesStore, type PreferencesState } from './preferences-store'

const PreferencesStoreContext = createContext<StoreApi<PreferencesState> | null>(null)

export function PreferencesStoreProvider({ children }: { children: React.ReactNode }) {
  const [store] = useState<StoreApi<PreferencesState>>(() => createPreferencesStore())
  const animationEnable = useStore(store, state => state.values.animation_enable)

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

  return (
    <PreferencesStoreContext.Provider value={store}>
      <AnimateUIProvider reducedMotion={animationEnable ? 'user' : 'always'}>{children}</AnimateUIProvider>
    </PreferencesStoreContext.Provider>
  )
}

/** The store itself, for event handlers that read the latest values without subscribing. */
export function usePreferencesStoreApi() {
  const store = use(PreferencesStoreContext)
  if (!store) throw new Error('Missing PreferencesStoreProvider')
  return store
}

/** Subscribes to one preference; unrelated preference changes do not re-render the caller. */
export function usePreference<K extends keyof PreferenceValueMap>(key: K): PreferenceValueMap[K] {
  return usePreferencesStore(state => state.values[key])
}

export function usePreferencesStore<T>(selector: (state: PreferencesState) => T): T {
  const store = use(PreferencesStoreContext)
  if (!store) throw new Error('Missing PreferencesStoreProvider')
  return useStore(store, selector)
}
