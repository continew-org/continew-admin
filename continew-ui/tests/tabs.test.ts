import { beforeEach, describe, expect, it } from 'vitest'

// The store persists to sessionStorage; provide one so it can be imported under the node test env.
const memory = new Map<string, string>()
const fakeStorage = {
  getItem: (key: string) => memory.get(key) ?? null,
  setItem: (key: string, value: string) => void memory.set(key, String(value)),
  removeItem: (key: string) => void memory.delete(key),
  clear: () => memory.clear(),
  key: () => null,
  length: 0
}
;(globalThis as unknown as { sessionStorage: Storage }).sessionStorage = fakeStorage as unknown as Storage

const { DEFAULT_PINNED_TAB, useTabsStore } = await import('@/stores/tabs')

const tab = (path: string, title = path) => ({ path, title })

function reset() {
  memory.clear()
  useTabsStore.setState({ tabs: [{ ...DEFAULT_PINNED_TAB }], history: [] })
}

describe('tabs persistence migration', () => {
  it('replaces the old pinned dashboard tab and keeps the other tabs, ordered and de-duplicated', () => {
    // A session from the previous build: the pinned tab is `/dashboard`, two user tabs are open.
    memory.set(
      'continew_in_header_tabs',
      JSON.stringify({
        state: {
          tabs: [
            { id: '/dashboard', title: '仪表盘', path: '/dashboard', closable: false, icon: 'dashboard' },
            { id: '/dashboard/', title: '仪表盘', path: '/dashboard/', closable: true, icon: 'dashboard' },
            { id: '/system/user', title: '用户管理', path: '/system/user', closable: true, icon: 'user' },
            { id: '/dashboard', title: '仪表盘', path: '/dashboard', closable: true, icon: 'dashboard' }
          ]
        },
        version: 0
      })
    )

    // The store already hydrated at import time; rehydrate against the freshly written entry.
    return useTabsStore.persist.rehydrate().then(() => {
      expect(useTabsStore.getState().tabs.map(t => t.path)).toEqual([DEFAULT_PINNED_TAB.path, '/system/user'])
      expect(useTabsStore.getState().tabs[0].closable).toBe(false)
    })
  })

  it('drops unusable entries and pins the landing tab once, even when the new path was already closable', () => {
    memory.set(
      'continew_in_header_tabs',
      JSON.stringify({
        state: {
          tabs: [
            { id: '/dashboard/workplace', title: '工作台', path: '/dashboard/workplace', closable: true, icon: null },
            null,
            { id: 'x', title: '无路径', closable: true },
            { id: 'bad-icon', title: '坏图标', path: '/bad-icon', closable: true, icon: 123 },
            { id: '/system/user', title: '用户管理', path: '/system/user', closable: true, icon: 'user' }
          ]
        },
        version: 0
      })
    )

    return useTabsStore.persist.rehydrate().then(() => {
      const { tabs } = useTabsStore.getState()
      expect(tabs.map(t => t.path)).toEqual([DEFAULT_PINNED_TAB.path, '/system/user'])
      // The pinned entry is the default one, not the closable copy that was stored.
      expect(tabs[0]).toEqual(DEFAULT_PINNED_TAB)
    })
  })
})

describe('tabs store', () => {
  beforeEach(reset)

  it('adds a closable tab and records visit history', () => {
    useTabsStore.getState().addTab(tab('/a', 'A'), 0)
    const { tabs, history } = useTabsStore.getState()
    expect(tabs.map(t => t.path)).toEqual([DEFAULT_PINNED_TAB.path, '/a'])
    expect(tabs[1].closable).toBe(true)
    expect(history).toEqual(['/a'])
  })

  it('ignores auth and unauthorized pages', () => {
    useTabsStore.getState().addTab(tab('/auth/login'), 0)
    useTabsStore.getState().addTab(tab('/unauthorized'), 0)
    expect(useTabsStore.getState().tabs.map(t => t.path)).toEqual([DEFAULT_PINNED_TAB.path])
  })

  it('merges title and icon when the same path is added again', () => {
    useTabsStore.getState().addTab(tab('/a', 'Old'), 0)
    useTabsStore.getState().addTab({ path: '/a', title: 'New', icon: 'x' }, 0)
    const entry = useTabsStore.getState().tabs.find(t => t.path === '/a')
    expect(entry?.title).toBe('New')
    expect(entry?.icon).toBe('x')
  })

  it('evicts the least recently visited closable tab past maxCount', () => {
    useTabsStore.getState().addTab(tab('/a'), 2)
    useTabsStore.getState().addTab(tab('/b'), 2)
    useTabsStore.getState().addTab(tab('/c'), 2)
    expect(useTabsStore.getState().tabs.map(t => t.path)).toEqual([DEFAULT_PINNED_TAB.path, '/b', '/c'])
  })

  it('closeTab returns the next path and can fall back to visit history', () => {
    useTabsStore.getState().addTab(tab('/a'), 0)
    useTabsStore.getState().addTab(tab('/b'), 0)
    // Visit history is [/a, /b]; closing /b should fall back to /a.
    const next = useTabsStore.getState().closeTab('/b', true)
    expect(next).toBe('/a')
    expect(useTabsStore.getState().tabs.map(t => t.path)).toEqual([DEFAULT_PINNED_TAB.path, '/a'])
  })

  it('keeps pinned tabs when closing left, right or all', () => {
    useTabsStore.getState().addTab(tab('/a'), 0)
    useTabsStore.getState().addTab(tab('/b'), 0)
    useTabsStore.getState().closeAllTabs()
    expect(useTabsStore.getState().tabs.map(t => t.path)).toEqual([DEFAULT_PINNED_TAB.path])
  })

  it('togglePin moves a tab into the pinned section', () => {
    useTabsStore.getState().addTab(tab('/a'), 0)
    useTabsStore.getState().togglePin('/a')
    const { tabs } = useTabsStore.getState()
    // Pinned tabs sit in front; the landing page stays pinned first, /a joins it.
    expect(tabs.map(t => t.path)).toEqual([DEFAULT_PINNED_TAB.path, '/a'])
    expect(tabs[1].closable).toBe(false)
  })

  it('moveTab reorders within a section and refuses cross-section moves', () => {
    useTabsStore.getState().addTab(tab('/a'), 0)
    useTabsStore.getState().addTab(tab('/b'), 0)
    // Move /b before /a (both closable) -> [/dashboard, /b, /a]
    useTabsStore.getState().moveTab(2, 1)
    expect(useTabsStore.getState().tabs.map(t => t.path)).toEqual([DEFAULT_PINNED_TAB.path, '/b', '/a'])
    // Cross-section move (closable -> pinned) is ignored.
    useTabsStore.getState().moveTab(1, 0)
    expect(useTabsStore.getState().tabs.map(t => t.path)).toEqual([DEFAULT_PINNED_TAB.path, '/b', '/a'])
  })

  it('pruneTabs drops unknown closable tabs but keeps the pinned landing tab', () => {
    useTabsStore.getState().addTab(tab('/keep'), 0)
    useTabsStore.getState().addTab(tab('/gone'), 0)
    useTabsStore.getState().pruneTabs(path => path === '/keep')
    expect(useTabsStore.getState().tabs.map(t => t.path)).toEqual([DEFAULT_PINNED_TAB.path, '/keep'])
  })
})
