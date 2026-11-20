import { create } from 'zustand'
import { createJSONStorage, persist } from 'zustand/middleware'

export interface TabItem {
  id: string
  title: string
  path: string
  closable: boolean
  icon?: string | null
}

export const DEFAULT_PINNED_TAB: TabItem = {
  id: '/dashboard',
  title: '仪表盘',
  path: '/dashboard',
  closable: false,
  icon: 'dashboard'
}

const STORAGE_KEY = 'continew_in_header_tabs'

interface TabsState {
  tabs: TabItem[]
  /** Paths in the order they were last visited, newest last; used to pick the tab to fall back to. */
  history: string[]
  addTab: (tab: { title: string; path: string; closable?: boolean; icon?: string | null }, maxCount: number) => void
  /** Closes a tab and returns the path to show next when it was the active one. */
  closeTab: (path: string, useHistory: boolean) => string | undefined
  closeOtherTabs: (keepPath: string) => void
  closeLeftTabs: (path: string) => void
  closeRightTabs: (path: string) => void
  closeAllTabs: () => void
  togglePin: (path: string) => void
  moveTab: (fromIndex: number, toIndex: number) => void
  pruneTabs: (isKnown: (path: string) => boolean) => void
  resetTabs: () => void
}

const keep = (tab: TabItem, paths: Set<string>) => !tab.closable || paths.has(tab.path)

/** Pinned tabs always sit in front of the closable ones. */
const sortPinned = (tabs: TabItem[]) => [...tabs.filter(t => !t.closable), ...tabs.filter(t => t.closable)]

export const useTabsStore = create<TabsState>()(
  persist(
    (set, get) => ({
      tabs: [DEFAULT_PINNED_TAB],
      history: [],

      addTab: ({ title, path, closable = true, icon = null }, maxCount) => {
        if (!path || path.startsWith('/auth') || path === '/unauthorized') return

        const { tabs, history } = get()
        const nextHistory = history.at(-1) === path ? history : [...history.filter(p => p !== path), path].slice(-30)
        const existing = tabs.find(t => t.path === path)

        if (existing) {
          const merged = { ...existing, title: title || existing.title, icon: icon ?? existing.icon }
          const changed = merged.title !== existing.title || merged.icon !== existing.icon
          if (changed || nextHistory !== history) {
            set({ tabs: changed ? tabs.map(t => (t === existing ? merged : t)) : tabs, history: nextHistory })
          }
          return
        }

        let nextTabs: TabItem[] = [
          ...tabs,
          {
            id: path,
            title: title || '新标签页',
            path,
            closable: path === DEFAULT_PINNED_TAB.path ? false : closable,
            icon
          }
        ]

        // 0 means unlimited; otherwise drop the least recently visited closable tabs.
        const candidates = nextTabs.filter(t => t.closable && t.path !== path)
        const overflow = maxCount > 0 ? candidates.length + 1 - maxCount : 0
        if (overflow > 0) {
          const victims = new Set(
            candidates
              .sort((a, b) => nextHistory.indexOf(a.path) - nextHistory.indexOf(b.path))
              .slice(0, overflow)
              .map(t => t.path)
          )
          nextTabs = nextTabs.filter(t => !victims.has(t.path))
        }

        set({ tabs: nextTabs, history: nextHistory })
      },

      closeTab: (path, useHistory) => {
        const { tabs, history } = get()
        const index = tabs.findIndex(t => t.path === path)
        if (index === -1 || !tabs[index].closable) return undefined

        const nextTabs = tabs.filter(t => t.path !== path)
        const nextHistory = history.filter(p => p !== path)
        set({ tabs: nextTabs, history: nextHistory })

        const fromHistory = useHistory
          ? [...nextHistory].reverse().find(p => nextTabs.some(t => t.path === p))
          : undefined
        return fromHistory ?? (nextTabs[index] ?? nextTabs[index - 1] ?? DEFAULT_PINNED_TAB).path
      },

      closeOtherTabs: keepPath => set({ tabs: get().tabs.filter(t => keep(t, new Set([keepPath]))) }),

      closeLeftTabs: path => {
        const { tabs } = get()
        const index = tabs.findIndex(t => t.path === path)
        set({ tabs: tabs.filter((t, i) => i >= index || !t.closable) })
      },

      closeRightTabs: path => {
        const { tabs } = get()
        const index = tabs.findIndex(t => t.path === path)
        set({ tabs: tabs.filter((t, i) => i <= index || !t.closable) })
      },

      closeAllTabs: () => set({ tabs: get().tabs.filter(t => !t.closable) }),

      togglePin: path => {
        if (path === DEFAULT_PINNED_TAB.path) return
        set({ tabs: sortPinned(get().tabs.map(t => (t.path === path ? { ...t, closable: !t.closable } : t))) })
      },

      moveTab: (fromIndex, toIndex) => {
        const tabs = [...get().tabs]
        const moving = tabs[fromIndex]
        const target = tabs[toIndex]
        // Pinned and closable tabs reorder within their own section only.
        if (!moving || !target || moving.closable !== target.closable) return
        tabs.splice(fromIndex, 1)
        tabs.splice(toIndex, 0, moving)
        set({ tabs })
      },

      pruneTabs: isKnown => {
        const { tabs } = get()
        const nextTabs = tabs.filter(tab => tab.path === DEFAULT_PINNED_TAB.path || isKnown(tab.path))
        if (nextTabs.length !== tabs.length) set({ tabs: nextTabs })
      },

      resetTabs: () => set({ tabs: [DEFAULT_PINNED_TAB], history: [] })
    }),
    {
      name: STORAGE_KEY,
      storage: createJSONStorage(() => sessionStorage),
      partialize: state => ({ tabs: state.tabs })
    }
  )
)

/** `tabbar_persist` off: stop writing to sessionStorage and drop what is there. */
export function setTabsPersistence(enabled: boolean) {
  useTabsStore.persist.setOptions({
    storage: createJSONStorage(() =>
      enabled ? sessionStorage : { getItem: () => null, setItem: () => {}, removeItem: () => {} }
    )
  })
  if (enabled) {
    // Write the current tabs right away instead of waiting for the next change.
    useTabsStore.setState({ tabs: [...useTabsStore.getState().tabs] })
  } else {
    sessionStorage.removeItem(STORAGE_KEY)
  }
}
