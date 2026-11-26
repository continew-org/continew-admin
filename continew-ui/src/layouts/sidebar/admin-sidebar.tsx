import * as React from 'react'

import { Link } from '@tanstack/react-router'
import { useShallow } from 'zustand/react/shallow'

import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  useSidebar
} from '@/components/ui/sidebar'
import { useSiteOptions } from '@/hooks/use-site-options'
import type { MenuGroup } from '@/navigation/menu'
import { usePreferencesStore, usePreferencesStoreApi } from '@/stores/preferences/preferences-provider'

import { NavMain } from './nav-main'
import { SidebarAd } from './sidebar-ad'
import { HOME_PATH } from '@/navigation/route-titles'

export function SidebarBrand() {
  const { title, logo } = useSiteOptions()

  return (
    <SidebarMenu>
      <SidebarMenuItem>
        <SidebarMenuButton
          className="h-10 p-1 group-data-[collapsible=icon]:mx-auto group-data-[collapsible=icon]:p-0! hover:bg-transparent active:bg-transparent"
          render={<Link to={HOME_PATH} aria-label={title || '返回仪表盘'} />}
        >
          <img src={logo} alt="" className="size-8 shrink-0 object-contain" />
          <span className="text-base font-semibold group-data-[collapsible=icon]:hidden">{title}</span>
        </SidebarMenuButton>
      </SidebarMenuItem>
    </SidebarMenu>
  )
}

/**
 * Drag handle on the sidebar edge. The width is written straight to the CSS variable while
 * dragging, so the drag costs no React renders; the preference is saved once on release.
 */
function SidebarResizeHandle() {
  const store = usePreferencesStoreApi()
  const { state } = useSidebar()

  if (state === 'collapsed') return null

  const onPointerDown = (event: React.PointerEvent<HTMLDivElement>) => {
    event.preventDefault()
    const root = document.documentElement
    const startX = event.clientX
    const startWidth = store.getState().values.sidebar_width
    let width = startWidth

    root.classList.add('disable-transitions')
    document.body.style.cursor = 'col-resize'
    document.body.style.userSelect = 'none'

    const onMove = (move: PointerEvent) => {
      width = Math.min(Math.max(Math.round(startWidth + move.clientX - startX), 160), 320)
      root.style.setProperty('--app-sidebar-width', `${width}px`)
    }

    const onUp = () => {
      window.removeEventListener('pointermove', onMove)
      window.removeEventListener('pointerup', onUp)
      root.classList.remove('disable-transitions')
      document.body.style.cursor = ''
      document.body.style.userSelect = ''
      store.getState().setPreference('sidebar_width', width)
    }

    window.addEventListener('pointermove', onMove)
    window.addEventListener('pointerup', onUp)
  }

  return (
    <div
      role="separator"
      aria-orientation="vertical"
      aria-label="调整侧边栏宽度"
      onPointerDown={onPointerDown}
      className="absolute inset-y-0 -right-1 z-20 hidden w-2 cursor-col-resize after:absolute after:inset-y-0 after:left-1/2 after:w-px after:transition-colors hover:after:bg-primary/60 md:block"
    />
  )
}

/**
 * Hover-to-expand for the collapsed icon rail: the rail widens as an overlay (the content does
 * not move) after a short delay, and folds back when the pointer leaves.
 */
function useHoverExpand(enabled: boolean) {
  const { state, isMobile } = useSidebar()
  const [hovered, setHovered] = React.useState(false)
  const timer = React.useRef<number>(undefined)
  const active = enabled && state === 'collapsed' && !isMobile

  React.useEffect(() => () => window.clearTimeout(timer.current), [])

  // Expanding with Ctrl+B mid-peek must not leave a stale peek for the next collapse.
  if (!active && hovered) setHovered(false)
  if (!active) return { expanded: false, handlers: {} }

  return {
    expanded: hovered,
    handlers: {
      onPointerEnter: () => {
        window.clearTimeout(timer.current)
        timer.current = window.setTimeout(() => setHovered(true), 150)
      },
      onPointerLeave: () => {
        window.clearTimeout(timer.current)
        setHovered(false)
      }
    }
  }
}

/** Vertical sidebar used by the sidebar, header-sidebar and mixed layouts. */
export const AdminSidebar = React.memo(function AdminSidebar({
  groups,
  showBrand = true,
  className
}: {
  groups: readonly MenuGroup[]
  showBrand?: boolean
  className?: string
}) {
  const { variant, collapsible, draggable, expandOnHover } = usePreferencesStore(
    useShallow(state => ({
      variant: state.values.sidebar_variant,
      collapsible: state.values.sidebar_collapsible,
      draggable: state.values.sidebar_draggable,
      expandOnHover: state.values.sidebar_expand_on_hover
    }))
  )
  const { expanded, handlers } = useHoverExpand(expandOnHover && collapsible === 'icon')

  return (
    <Sidebar variant={variant} collapsible={collapsible} peek={expanded} className={className} {...handlers}>
      {showBrand && (
        <SidebarHeader>
          <SidebarBrand />
        </SidebarHeader>
      )}
      <SidebarContent className="scroll-fade-y">
        <NavMain items={groups} forceExpanded={expanded} />
      </SidebarContent>
      <SidebarFooter className="shrink-0 p-0">
        <SidebarAd collapsible={collapsible} />
      </SidebarFooter>
      {draggable && <SidebarResizeHandle />}
    </Sidebar>
  )
})
