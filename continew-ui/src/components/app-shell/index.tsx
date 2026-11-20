import { useEffect, useMemo, useState, type CSSProperties, type ReactNode } from 'react'

import { useNavigate, useRouterState } from '@tanstack/react-router'
import { cn } from 'cn'
import { useShallow } from 'zustand/react/shallow'

import { SidebarInset, SidebarProvider } from '@/components/ui/sidebar'
import { useMenuGroups } from '@/hooks/use-menu'
import { useIsMobile } from '@/hooks/use-mobile'
import type { LayoutMode } from '@/lib/preferences/layout'
import type { ShellPreferences } from '@/lib/shell-preferences'
import {
  firstMenuLeaf,
  isMenuItemActive,
  isMenuRootActive,
  toMenuRoots,
  type MenuGroup,
  type MenuRoot
} from '@/navigation/menu'
import { usePreferencesStore } from '@/stores/preferences/preferences-provider'
import { useShellStore } from '@/stores/shell'
import { setTabsPersistence } from '@/stores/tabs'

import { AdminHeader } from './header/admin-header'
import { SearchDialog } from './header/search-dialog'
import { GlobalShortcuts } from './global-shortcuts'
import { BreadcrumbView, useBreadcrumbTrail } from './app-breadcrumb'
import { DocumentTitle, UpdateChecker, Watermark } from './page-effects'
import { AppSidebar } from './sidebar/app-sidebar'
import { MixedSidebar, type MixedColumnEntry } from './sidebar/mixed-sidebar'
import { Tabbar } from './tabbar/tabbar'
import { LockScreenOverlay, LockScreenSetupDialog, useLockScreen } from './workspace-overlays'

type LayoutFlags = {
  sidebar: 'primary' | 'mixed' | null
  /** Header spans the full width above the sidebar instead of sitting beside it. */
  headerOnTop: boolean
  headerMenu: 'navigate' | 'select' | null
}

const LAYOUT_FLAGS: Record<LayoutMode, LayoutFlags> = {
  'sidebar-nav': { sidebar: 'primary', headerOnTop: false, headerMenu: null },
  'sidebar-mixed-nav': { sidebar: 'mixed', headerOnTop: false, headerMenu: null },
  'header-nav': { sidebar: null, headerOnTop: true, headerMenu: 'navigate' },
  'header-sidebar-nav': { sidebar: 'primary', headerOnTop: true, headerMenu: null },
  'mixed-nav': { sidebar: 'primary', headerOnTop: true, headerMenu: 'select' },
  'header-mixed-nav': { sidebar: 'mixed', headerOnTop: true, headerMenu: 'select' }
}

/** Application shell shared by every authenticated section; the layout mode picks the frame. */
export function AppShell({ defaultOpen, variant, collapsible, children }: ShellPreferences & { children: ReactNode }) {
  const p = usePreferencesStore(
    useShallow(state => ({
      layout: state.values.layout,
      navigationSplit: state.values.navigation_split,
      autoActivate: state.values.sidebar_auto_activate_child,
      tabbarEnable: state.values.tabbar_enable,
      tabbarPersist: state.values.tabbar_persist
    }))
  )
  const isMobile = useIsMobile()
  const pathname = useRouterState({ select: state => state.location.pathname })
  const navigate = useNavigate()
  const { groups } = useMenuGroups()
  const roots = useMemo(() => toMenuRoots(groups), [groups])
  const [picked, setPicked] = useState<string>()
  const [mixedPick, setMixedPick] = useState<string>()
  const { locked, lock, unlock, clear } = useLockScreen()

  useEffect(() => {
    setTabsPersistence(p.tabbarPersist)
  }, [p.tabbarPersist])

  // A root the user clicked only stands in until the next navigation resolves the active one.
  useEffect(() => {
    setPicked(undefined)
    setMixedPick(undefined)
  }, [pathname])

  // Mobile always uses the vertical layout: horizontal menus do not fit.
  const layout: LayoutMode = isMobile ? 'sidebar-nav' : p.layout
  const flags = LAYOUT_FLAGS[layout]

  const activeId = roots.find(root => isMenuRootActive(root, pathname))?.id
  // A root the user clicked wins over the active one until the next navigation clears it.
  const selectedId = picked ?? activeId ?? roots[0]?.id
  const selectedRoot = roots.find(root => root.id === selectedId)

  const routerNavigate = (href: string, external?: boolean) => {
    if (external) window.open(href, '_blank', 'noopener,noreferrer')
    else void navigate({ href })
  }

  const openRoot = (root: MenuRoot) => {
    setPicked(root.id)
    if (root.link) {
      if (root.link.url !== pathname) routerNavigate(root.link.url, root.link.external)
      return
    }
    if (p.autoActivate) {
      const target = firstMenuLeaf(root.items)?.url
      if (target && target !== pathname) routerNavigate(target)
    }
  }

  const selectMixed = (entry: MixedColumnEntry) => {
    if (layout === 'header-mixed-nav') setMixedPick(entry.id)
    else setPicked(entry.id)

    if (entry.children.length === 0) {
      const item =
        layout === 'header-mixed-nav'
          ? selectedRoot?.items.find(i => i.id === entry.id)
          : roots.find(root => root.id === entry.id)?.link
      if (item) routerNavigate(item.url, item.external)
    } else if (p.autoActivate) {
      const target = firstMenuLeaf(entry.children)?.url
      if (target && target !== pathname) routerNavigate(target)
    }
  }

  const mixedEntries = useMemo<MixedColumnEntry[]>(() => {
    if (layout === 'header-mixed-nav') {
      return (selectedRoot?.items ?? []).map(item => ({
        id: item.id,
        title: item.title,
        icon: item.icon,
        children: item.subItems ?? [],
        isActive: isMenuItemActive(item, pathname)
      }))
    }
    return roots.map(root => ({
      id: root.id,
      title: root.title,
      icon: root.icon,
      children: root.link ? [] : root.items,
      isActive: isMenuRootActive(root, pathname)
    }))
  }, [layout, selectedRoot, roots, pathname])

  const mixedSelectedId =
    layout === 'header-mixed-nav'
      ? mixedPick && mixedEntries.some(e => e.id === mixedPick)
        ? mixedPick
        : mixedEntries.find(e => e.isActive)?.id
      : selectedId

  const splitRoot = layout === 'mixed-nav' && p.navigationSplit ? selectedRoot : undefined
  const sidebarGroups = useMemo<readonly MenuGroup[]>(
    () => (splitRoot && !splitRoot.link ? [{ id: splitRoot.id, items: [...splitRoot.items] }] : groups),
    [splitRoot, groups]
  )

  // In the split mixed layout, a leaf root (dashboard) has no sub-menu to list: hide the sidebar.
  const splitEmpty = layout === 'mixed-nav' && p.navigationSplit && (!selectedRoot || Boolean(selectedRoot.link))
  // A mixed sidebar with nothing to list (e.g. header-mixed on a leaf root) is pointless: hide it.
  const mixedEmpty = flags.sidebar === 'mixed' && mixedEntries.length === 0
  const sidebarKind = splitEmpty || mixedEmpty ? null : flags.sidebar

  const headerMenu = flags.headerMenu
    ? { roots, mode: flags.headerMenu, selectedId, onSelect: flags.headerMenu === 'select' ? openRoot : undefined }
    : undefined

  const header = (
    <AdminHeader showBrand={flags.headerOnTop} showSidebarToggle={sidebarKind === 'primary'} menu={headerMenu} />
  )

  const headerOffset = flags.headerOnTop ? '3rem' : '0px'

  return (
    <SidebarProvider
      defaultOpen={defaultOpen}
      className={flags.headerOnTop ? 'flex-col' : 'flex-row'}
      style={
        {
          '--sidebar-width': 'var(--app-sidebar-width)',
          '--sidebar-width-icon': '3rem',
          '--app-sidebar-width': '272px',
          '--app-sidebar-mixed-width': '56px',
          '--app-header-offset': headerOffset
        } as CSSProperties
      }
    >
      {flags.headerOnTop && header && (
        <div inert={locked} className="w-full shrink-0">
          {header}
        </div>
      )}
      <div
        inert={locked}
        className={cn(
          'flex min-h-0 w-full flex-1',
          // The shadcn sidebar is position:fixed; below a full-width header it starts lower.
          flags.headerOnTop &&
            '[&_[data-slot=sidebar-container]]:top-(--app-header-offset) [&_[data-slot=sidebar-container]]:h-[calc(100svh-var(--app-header-offset))] [&_[data-slot=sidebar-container]]:transition-[top,height,width,left]'
        )}
      >
        {sidebarKind === 'primary' && (
          <AppSidebar
            groups={sidebarGroups}
            showBrand={!flags.headerOnTop}
            variant={variant}
            collapsible={collapsible}
          />
        )}
        {sidebarKind === 'mixed' && (
          <MixedSidebar
            entries={mixedEntries}
            selectedId={mixedSelectedId}
            onSelect={selectMixed}
            topOffset={flags.headerOnTop}
            showBrand={!flags.headerOnTop}
          />
        )}
        <SidebarInset
          className={cn(
            '[html[data-content-layout=centered]_&>*]:mx-auto [html[data-content-layout=centered]_&>*]:w-full [html[data-content-layout=centered]_&>*]:max-w-screen-2xl',
            'peer-data-[variant=inset]:border',
            '[--dashboard-header-height:--spacing(12)]',
            'min-w-0 overflow-x-clip'
          )}
        >
          {!flags.headerOnTop && header}
          {p.tabbarEnable && <Tabbar />}
          <ShellContent>{children}</ShellContent>
        </SidebarInset>
      </div>

      <DocumentTitle />
      <Watermark />
      <UpdateChecker />
      <GlobalShortcuts />
      <SearchDialog />
      <LockScreenSetupDialog onLock={lock} />
      {locked && <LockScreenOverlay onUnlock={unlock} onClear={clear} onForget={clear} />}
    </SidebarProvider>
  )
}

/**
 * The page body's entrance animation, isolated so only this subtree re-renders when the pathname
 * or refresh counter changes; the frame around it stays put.
 */
function PageFrame({ children }: { children: ReactNode }) {
  const pathname = useRouterState({ select: state => state.location.pathname })
  const refreshKey = useShellStore(state => state.refreshKey)

  return (
    <div
      key={`${pathname}:${refreshKey}`}
      className="motion-safe:animate-in motion-safe:duration-150 motion-safe:fade-in motion-safe:slide-in-from-bottom-1"
    >
      {children}
    </div>
  )
}

/**
 * Breadcrumb plus the padded content area. Owns the breadcrumb pathname subscription so `AppShell`
 * itself takes no router state and does not re-render on navigation.
 */
function ShellContent({ children }: { children: ReactNode }) {
  const breadcrumbItems = useBreadcrumbTrail()
  const hasBreadcrumb = breadcrumbItems.length > 0

  return (
    <div className="flex min-h-0 min-w-0 flex-1 flex-col overflow-x-hidden">
      <BreadcrumbView items={breadcrumbItems} />
      <div
        className={cn(
          'min-h-0 min-w-0 flex-1 p-4 has-data-[content-padding=false]:p-0 md:p-6 md:has-data-[content-padding=false]:p-0',
          hasBreadcrumb ? 'pt-1.5 md:pt-2' : undefined
        )}
      >
        <PageFrame>{children}</PageFrame>
      </div>
    </div>
  )
}
