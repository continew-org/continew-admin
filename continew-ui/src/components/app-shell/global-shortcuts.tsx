import * as React from 'react'

import { useShallow } from 'zustand/react/shallow'

import { useLogout } from '@/hooks/use-logout'
import { usePreferencesStore } from '@/stores/preferences/preferences-provider'
import { useShellStore } from '@/stores/shell'

/**
 * Global keyboard shortcuts, each gated by its own preference and the master switch:
 * Ctrl/⌘+J search, Alt+L lock screen, Alt+Q log out. Kept in one place so the triggers are
 * consistent with the hints shown in the header and user menu.
 */
export function GlobalShortcuts() {
  const prefs = usePreferencesStore(
    useShallow(state => ({
      enabled: state.values.shortcut_enable,
      search: state.values.shortcut_search,
      lock: state.values.shortcut_lock_screen && state.values.widget_lock_screen,
      logout: state.values.shortcut_logout
    }))
  )
  const logout = useLogout()

  React.useEffect(() => {
    if (!prefs.enabled) return

    const onKeyDown = (event: KeyboardEvent) => {
      if (event.repeat) return
      const mod = event.metaKey || event.ctrlKey
      const key = event.key.toLowerCase()

      if (prefs.search && mod && key === 'j') {
        event.preventDefault()
        const { searchOpen, setSearchOpen } = useShellStore.getState()
        setSearchOpen(!searchOpen)
        return
      }

      if (prefs.lock && event.altKey && key === 'l') {
        event.preventDefault()
        useShellStore.getState().setLockSetupOpen(true)
        return
      }

      if (prefs.logout && event.altKey && key === 'q') {
        event.preventDefault()
        void logout()
      }
    }

    window.addEventListener('keydown', onKeyDown)
    return () => window.removeEventListener('keydown', onKeyDown)
  }, [prefs.enabled, prefs.search, prefs.lock, prefs.logout, logout])

  return null
}
