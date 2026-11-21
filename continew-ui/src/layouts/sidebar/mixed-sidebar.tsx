import * as React from 'react'

import { Highlight as SidebarHighlight } from '@animate-ui/react/primitives/sidebar'
import { cn } from 'cn'
import { Pin, PinOff } from 'lucide-react'
import { useShallow } from 'zustand/react/shallow'

import { Button } from '@/components/ui/button'
import { Tooltip, TooltipContent, TooltipTrigger } from '@/components/ui/tooltip'
import type { MenuGroup, MenuItem } from '@/navigation/menu'
import { usePreferencesStore } from '@/stores/preferences/preferences-provider'

import { SidebarBrand } from './admin-sidebar'
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
  onSelect: (entry: MixedColumnEntry, options?: { navigate?: boolean }) => void
  /** Top offset when a full-width header sits above the sidebar. */
  topOffset: boolean
  showBrand: boolean
}

/**
 * Two-column sidebar: a narrow column of roots and a second column with the selected root's
 * pages. Unpinned, the second column floats over the content while the pointer is on the
 * sidebar and folds away when it leaves.
 */
export function MixedSidebar({ entries, selectedId, onSelect, topOffset, showBrand }: MixedSidebarProps) {
  const { pinned, showPinButton, setPreference } = usePreferencesStore(
    useShallow(state => ({
      pinned: !state.values.sidebar_extra_collapsed,
      showPinButton: state.values.sidebar_fixed_button,
      setPreference: state.setPreference
    }))
  )
  const [hovering, setHovering] = React.useState(false)
  const [previewId, setPreviewId] = React.useState<string>()
  const leaveTimer = React.useRef<number>(undefined)

  React.useEffect(() => () => window.clearTimeout(leaveTimer.current), [])

  const displayedId = !pinned && hovering ? (previewId ?? selectedId) : selectedId
  const displayed = entries.find(entry => entry.id === displayedId)
  const showSub = Boolean(displayed?.children.length) && (pinned || hovering)
  const groups = React.useMemo<MenuGroup[]>(
    () => (displayed ? [{ id: displayed.id, items: [...displayed.children] }] : []),
    [displayed]
  )

  return (
    <aside
      onPointerEnter={() => window.clearTimeout(leaveTimer.current)}
      onPointerLeave={() => {
        leaveTimer.current = window.setTimeout(() => {
          setHovering(false)
          setPreviewId(undefined)
        }, 200)
      }}
      className={cn(
        // The unpinned second column floats over the content, so it must clear all of the content's
        // sticky chrome (header z-40, tabbar z-30, pinned table cells) yet stay under fullscreen
        // tables, dialogs and popovers (z-50).
        'sticky z-45 hidden shrink-0 md:flex',
        topOffset
          ? 'top-(--app-header-offset) h-[calc(100svh-var(--app-header-offset))] transition-[top,height] duration-200'
          : 'top-0 h-svh'
      )}
    >
      <nav
        data-slot="mixed-sidebar-root"
        aria-label="一级菜单"
        className="flex w-(--app-sidebar-mixed-width) scrollbar-none flex-col items-stretch gap-1 overflow-y-auto border-r bg-sidebar px-1.5 py-2 text-sidebar-foreground"
      >
        {showBrand && (
          <div className="mb-1 flex justify-center [&_[data-sidebar=menu-button]]:size-10 [&_[data-sidebar=menu-button]]:justify-center [&_[data-sidebar=menu-button]>span]:hidden [&_[data-sidebar=menu]]:items-center">
            <SidebarBrand />
          </div>
        )}
        {entries.map(entry => {
          const Icon = entry.icon
          const isSelected = entry.id === selectedId

          return (
            <button
              key={entry.id}
              type="button"
              onClick={() => {
                setPreviewId(undefined)
                onSelect(entry)
                if (entry.children.length) setHovering(true)
              }}
              onPointerEnter={() => {
                if (!pinned && entry.children.length) {
                  setPreviewId(entry.id)
                  setHovering(true)
                }
              }}
              aria-current={entry.isActive ? 'true' : undefined}
              className={cn(
                'flex flex-col items-center gap-1 rounded-md px-1 py-2 text-[11px] leading-tight transition-colors',
                isSelected
                  ? 'bg-sidebar-primary text-sidebar-primary-foreground'
                  : 'text-sidebar-foreground/80 hover:bg-sidebar-accent hover:text-sidebar-accent-foreground'
              )}
            >
              {Icon ? <Icon className="size-5" /> : <span className="text-base font-medium">{entry.title[0]}</span>}
              <span className="line-clamp-2 w-full text-center break-all">{entry.title}</span>
            </button>
          )
        })}
      </nav>

      <div
        data-slot="mixed-sidebar-sub"
        data-state={showSub ? 'open' : 'closed'}
        className={cn(
          'flex w-(--app-sidebar-width) flex-col overflow-hidden border-r bg-sidebar text-sidebar-foreground transition-[opacity,translate] duration-200 ease-out',
          // Unpinned, the column overlays the content instead of pushing it.
          !pinned && 'absolute inset-y-0 left-full shadow-xl',
          showSub ? 'translate-x-0 opacity-100' : 'pointer-events-none -translate-x-2 opacity-0',
          pinned && !showSub && 'hidden'
        )}
      >
        <div className="flex h-12 shrink-0 items-center justify-between gap-2 px-3">
          <span className="truncate text-sm font-semibold">{displayed?.title}</span>
          {showPinButton && (
            <Tooltip>
              <TooltipTrigger
                render={
                  <Button
                    variant="ghost"
                    size="icon-sm"
                    onClick={() => {
                      if (!pinned && displayed) onSelect(displayed, { navigate: false })
                      setPreviewId(undefined)
                      setPreference('sidebar_extra_collapsed', pinned)
                    }}
                  />
                }
              >
                {pinned ? <PinOff /> : <Pin />}
              </TooltipTrigger>
              <TooltipContent side="right">{pinned ? '取消固定' : '固定'}</TooltipContent>
            </Tooltip>
          )}
        </div>
        <SidebarHighlight className="min-h-0 flex-1 overflow-y-auto">
          <NavMain items={groups} />
        </SidebarHighlight>
      </div>
    </aside>
  )
}
