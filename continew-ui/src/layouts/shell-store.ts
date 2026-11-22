import { create } from 'zustand'

import { queryClient } from '@/lib/query-client'

/**
 * Transient shell state shared by the header, tabbar and overlays.
 *
 * Kept out of the preference store because none of it is saved, and kept in a store (not
 * React context) so each widget subscribes to the one field it needs.
 */
interface ShellState {
  /** Bumped by every refresh; the page surface is keyed on it so the page remounts. */
  refreshKey: number
  contentMaximized: boolean
  preferencesOpen: boolean
  searchOpen: boolean
  lockSetupOpen: boolean
  refresh: () => void
  setContentMaximized: (value: boolean) => void
  setPreferencesOpen: (value: boolean) => void
  setSearchOpen: (value: boolean) => void
  setLockSetupOpen: (value: boolean) => void
}

export const useShellStore = create<ShellState>()(set => ({
  refreshKey: 0,
  contentMaximized: false,
  preferencesOpen: false,
  searchOpen: false,
  lockSetupOpen: false,
  refresh: () => {
    // The remount alone is not enough: page data lives in TanStack Query, which has
    // refetchOnMount disabled, so without invalidation the fresh mount shows stale cache.
    void queryClient.invalidateQueries()
    set(state => ({ refreshKey: state.refreshKey + 1 }))
  },
  setContentMaximized: contentMaximized => set({ contentMaximized }),
  setPreferencesOpen: preferencesOpen => set({ preferencesOpen }),
  setSearchOpen: searchOpen => set({ searchOpen }),
  setLockSetupOpen: lockSetupOpen => set({ lockSetupOpen })
}))
