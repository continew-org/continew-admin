import * as React from 'react'

import { Link } from '@tanstack/react-router'
import { cn } from 'cn'

import { useSiteOptions } from '@/hooks/use-site-options'
import type { MenuGroup, MenuItem } from '@/navigation/menu'

import { NavMain } from './nav-main'

export interface MixedColumnEntry {
  id: string
  title: string
  icon?: MenuItem['icon']
  /** Entries without children are pages: clicking navigates instead of opening the sub column. */
  children: readonly MenuItem[]
  isActive: boolean
}

interface MixedSidebarProps {
  entries: readonly MixedColumnEntry[]
  selectedId: string | undefined
  onSelect: (entry: MixedColumnEntry) => void
  /** Top offset when a full-width header sits above the sidebar. */
  topOffset: boolean
  showBrand: boolean
}

/**
 * Two-column sidebar: a narrow column of roots and a second column with the selected root's
 * pages. Kept pinned (no hover-peek) for simplicity.
 */
export function MixedSidebar({ entries, selectedId, onSelect, topOffset, showBrand }: MixedSidebarProps) {
  const { title, logo } = useSiteOptions()

  const displayed = entries.find(entry => entry.id === selectedId) ?? entries.find(entry => entry.isActive)
  const groups = React.useMemo<MenuGroup[]>(
    () => (displayed ? [{ id: displayed.id, items: [...displayed.children] }] : []),
    [displayed]
  )

  return (
    <aside
      className={cn(
        'sticky z-20 hidden shrink-0 md:flex',
        topOffset ? 'top-(--app-header-offset) h-[calc(100svh-var(--app-header-offset))]' : 'top-0 h-svh'
      )}
    >
      <nav
        data-slot="mixed-sidebar-root"
        aria-label="一级菜单"
        className="flex w-(--app-sidebar-mixed-width) scrollbar-none flex-col items-stretch gap-1 overflow-y-auto border-r bg-sidebar px-1.5 py-2 text-sidebar-foreground"
      >
        {showBrand && (
          <Link to="/dashboard" aria-label={title || '返回仪表盘'} className="mb-1 flex justify-center py-1">
            <img src={logo} alt="" className="size-8 object-contain" />
          </Link>
        )}
        {entries.map(entry => {
          const Icon = entry.icon
          const isSelected = entry.id === selectedId

          return (
            <button
              key={entry.id}
              type="button"
              onClick={() => onSelect(entry)}
              aria-label={entry.title}
              title={entry.title}
              aria-current={entry.isActive ? 'true' : undefined}
              className={cn(
                'flex items-center justify-center rounded-md px-1 py-2.5 transition-colors',
                isSelected
                  ? 'bg-sidebar-primary text-sidebar-primary-foreground'
                  : 'text-sidebar-foreground/80 hover:bg-sidebar-accent hover:text-sidebar-accent-foreground'
              )}
            >
              {Icon ? <Icon className="size-5" /> : <span className="text-sm font-medium">{entry.title[0]}</span>}
            </button>
          )
        })}
      </nav>

      {displayed && displayed.children.length > 0 && (
        <div
          data-slot="mixed-sidebar-sub"
          className="flex w-(--app-sidebar-width) flex-col overflow-hidden border-r bg-sidebar text-sidebar-foreground"
        >
          <div className="flex h-12 shrink-0 items-center border-b px-3">
            <span className="truncate text-sm font-semibold">{displayed.title}</span>
          </div>
          <div className="min-h-0 flex-1 scroll-fade-y overflow-y-auto">
            <NavMain items={groups} />
          </div>
        </div>
      )}
    </aside>
  )
}
