import { create } from 'zustand'

/**
 * Transient shell state shared by the tabbar, the lock screen, the search palette and the page
 * surface. None of it is persisted, so it lives in its own store rather than the preference store.
 */
interface ShellState {
  /** Bumped by a per-tab refresh; the page surface is keyed on it so the page remounts. */
  refreshKey: number
  /** Whether the "set a lock-screen password" dialog is open. */
  lockSetupOpen: boolean
  /** Whether the command palette (global search) is open. */
  searchOpen: boolean
  refresh: () => void
  setLockSetupOpen: (value: boolean) => void
  setSearchOpen: (value: boolean) => void
}

export const useShellStore = create<ShellState>()(set => ({
  refreshKey: 0,
  lockSetupOpen: false,
  searchOpen: false,
  refresh: () => set(state => ({ refreshKey: state.refreshKey + 1 })),
  setLockSetupOpen: lockSetupOpen => set({ lockSetupOpen }),
  setSearchOpen: searchOpen => set({ searchOpen })
}))
