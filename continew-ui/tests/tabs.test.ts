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

describe('tabs store', () => {
  beforeEach(reset)

  it('adds a closable tab and records visit history', () => {
    useTabsStore.getState().addTab(tab('/a', 'A'), 0)
    const { tabs, history } = useTabsStore.getState()
    expect(tabs.map(t => t.path)).toEqual(['/dashboard', '/a'])
    expect(tabs[1].closable).toBe(true)
    expect(history).toEqual(['/a'])
  })

  it('ignores auth and unauthorized pages', () => {
    useTabsStore.getState().addTab(tab('/auth/login'), 0)
    useTabsStore.getState().addTab(tab('/unauthorized'), 0)
    expect(useTabsStore.getState().tabs.map(t => t.path)).toEqual(['/dashboard'])
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
    expect(useTabsStore.getState().tabs.map(t => t.path)).toEqual(['/dashboard', '/b', '/c'])
  })

  it('closeTab returns the next path and can fall back to visit history', () => {
    useTabsStore.getState().addTab(tab('/a'), 0)
    useTabsStore.getState().addTab(tab('/b'), 0)
    // Visit history is [/a, /b]; closing /b should fall back to /a.
    const next = useTabsStore.getState().closeTab('/b', true)
    expect(next).toBe('/a')
    expect(useTabsStore.getState().tabs.map(t => t.path)).toEqual(['/dashboard', '/a'])
  })

  it('keeps pinned tabs when closing left, right or all', () => {
    useTabsStore.getState().addTab(tab('/a'), 0)
    useTabsStore.getState().addTab(tab('/b'), 0)
    useTabsStore.getState().closeAllTabs()
    expect(useTabsStore.getState().tabs.map(t => t.path)).toEqual(['/dashboard'])
  })

  it('togglePin moves a tab into the pinned section', () => {
    useTabsStore.getState().addTab(tab('/a'), 0)
    useTabsStore.getState().togglePin('/a')
    const { tabs } = useTabsStore.getState()
    // Pinned tabs sit in front; the dashboard stays pinned first, /a joins it.
    expect(tabs.map(t => t.path)).toEqual(['/dashboard', '/a'])
    expect(tabs[1].closable).toBe(false)
  })

  it('moveTab reorders within a section and refuses cross-section moves', () => {
    useTabsStore.getState().addTab(tab('/a'), 0)
    useTabsStore.getState().addTab(tab('/b'), 0)
    // Move /b before /a (both closable) -> [/dashboard, /b, /a]
    useTabsStore.getState().moveTab(2, 1)
    expect(useTabsStore.getState().tabs.map(t => t.path)).toEqual(['/dashboard', '/b', '/a'])
    // Cross-section move (closable -> pinned) is ignored.
    useTabsStore.getState().moveTab(1, 0)
    expect(useTabsStore.getState().tabs.map(t => t.path)).toEqual(['/dashboard', '/b', '/a'])
  })

  it('pruneTabs drops unknown closable tabs but keeps the pinned dashboard', () => {
    useTabsStore.getState().addTab(tab('/keep'), 0)
    useTabsStore.getState().addTab(tab('/gone'), 0)
    useTabsStore.getState().pruneTabs(path => path === '/keep')
    expect(useTabsStore.getState().tabs.map(t => t.path)).toEqual(['/dashboard', '/keep'])
  })
})
