import * as React from 'react'

import { useNavigate } from '@tanstack/react-router'
import { cn } from 'cn'
import { Settings } from 'lucide-react'
import { useShallow } from 'zustand/react/shallow'

import { Button } from '@/components/ui/button'
import { SidebarInset, SidebarProvider } from '@/components/ui/sidebar'
import { useMenuGroups } from '@/hooks/use-menu'
import { useIsMobile } from '@/hooks/use-mobile'
import { useSiteOptions } from '@/hooks/use-site-options'
import { getClientCookie } from '@/lib/cookie'
import type { LayoutMode } from '@/lib/preferences/layout'
import {
  firstMenuLeaf,
  isMenuItemActive,
  isMenuRootActive,
  toMenuRoots,
  toMenuTree,
  type MenuGroup,
  type MenuRoot
} from '@/navigation/menu'
import { usePreferencesStore } from '@/stores/preferences/preferences-provider'
import { setTabsPersistence } from '@/stores/tabs'

import { AdminHeader } from './header/admin-header'
import { AppBreadcrumb } from './header/app-breadcrumb'
import { SearchDialog } from './header/search-dialog'
import { MessageSocket, NoticePopup } from './message-effects'
import { DocumentTitle, GlobalShortcuts, RouteProgress, UpdateChecker, Watermark } from './page-effects'
import { PageSurface } from './page-surface'
import { usePathname } from './route-info'
import { useShellStore } from './shell-store'
import { AdminSidebar } from './sidebar/admin-sidebar'
import { MixedSidebar, type MixedColumnEntry } from './sidebar/mixed-sidebar'
import { Tabbar } from './tabbar/tabbar'
import { LockScreenOverlay, LockScreenSetupDialog, useLockScreen } from './workspace-overlays'

// The preferences sheet is only needed once the user opens it; keep it out of the first bundle.
const loadPreferencesSheet = () => import('./preferences/preferences-sheet')
const PreferencesSheet = React.lazy(() => loadPreferencesSheet().then(m => ({ default: m.PreferencesSheet })))

/** Items of the selected root, or undefined when the root is a single page with nothing to list. */
function selectedRootItems(roots: readonly MenuRoot[], selectedId: string | undefined) {
  const root = roots.find(r => r.id === selectedId)
  return root && !root.link && root.items.length ? root.items : undefined
}

/** A callback whose identity never changes but always runs the latest closure, so memoised children stay put. */
function useStableCallback<A extends unknown[]>(fn: (...args: A) => void) {
  const ref = React.useRef(fn)
  React.useLayoutEffect(() => {
    ref.current = fn
  })
  return React.useCallback((...args: A) => ref.current(...args), [])
}

/** Route loader: the sidebar open state is read from its cookie so the first paint matches. */
export function loadBaseLayout() {
  return { defaultOpen: getClientCookie('sidebar_state') !== 'false' }
}

type LayoutFlags = {
  sidebar: 'primary' | 'mixed' | null
  /** Header spans the full width above the sidebar instead of sitting beside it. */
  headerOnTop: boolean
  headerMenu: 'navigate' | 'select' | null
}

const LAYOUT_FLAGS: Record<LayoutMode, LayoutFlags> = {
  'sidebar-nav': { sidebar: 'primary', headerOnTop: false, headerMenu: null },
  'sidebar-tree-nav': { sidebar: 'primary', headerOnTop: false, headerMenu: null },
  'sidebar-mixed-nav': { sidebar: 'mixed', headerOnTop: false, headerMenu: null },
  'header-nav': { sidebar: null, headerOnTop: true, headerMenu: 'navigate' },
  'header-sidebar-nav': { sidebar: 'primary', headerOnTop: true, headerMenu: null },
  'mixed-nav': { sidebar: 'primary', headerOnTop: true, headerMenu: 'select' },
  'header-mixed-nav': { sidebar: 'mixed', headerOnTop: true, headerMenu: 'select' },
  'full-content': { sidebar: null, headerOnTop: false, headerMenu: null }
}

/**
 * Picks the root shown by the mixed layouts. The root that holds the current page wins; a
 * root the user clicked overrides it until the next navigation.
 */
function useSelectedRoot(roots: readonly MenuRoot[], pathname: string) {
  const activeId = roots.find(root => isMenuRootActive(root, pathname))?.id
  const [picked, setPicked] = React.useState<{ id: string; path: string } | null>(null)
  if (picked && picked.path !== pathname) setPicked(null)
  const selectedId = picked?.path === pathname ? picked.id : (activeId ?? roots[0]?.id)

  return { selectedId, pick: (id: string) => setPicked({ id, path: pathname }) }
}

/** The last page visited under each root, so returning to a root restores where the user was. */
function useLastVisited(roots: readonly MenuRoot[], pathname: string) {
  const ref = React.useRef(new Map<string, string>())
  const root = roots.find(r => isMenuRootActive(r, pathname))
  if (root) ref.current.set(root.id, pathname)
  return ref.current
}

/**
 * Auto-hiding header: `auto` hides past the header height, `auto-scroll` while scrolling down.
 * A passive listener batched to one read per frame; React only re-renders when the boolean flips.
 */
function useHeaderHidden(enabled: boolean, mode: string, threshold: number) {
  const [hidden, setHidden] = React.useState(false)

  React.useEffect(() => {
    if (!enabled) {
      setHidden(false)
      return
    }

    let last = window.scrollY
    let frame = 0
    const onScroll = () => {
      if (frame) return
      frame = requestAnimationFrame(() => {
        frame = 0
        const top = window.scrollY
        setHidden(mode === 'auto' ? top > threshold : top > threshold && top > last)
        last = top
      })
    }

    window.addEventListener('scroll', onScroll, { passive: true })
    return () => {
      window.removeEventListener('scroll', onScroll)
      cancelAnimationFrame(frame)
    }
  }, [enabled, mode, threshold])

  return enabled && hidden
}

function useApplyTabsPersistence() {
  const persist = usePreferencesStore(state => state.values.tabbar_persist)
  const first = React.useRef(true)

  React.useEffect(() => {
    // The store is already set up for persisting; only switching it off at boot needs work.
    if (first.current && persist) {
      first.current = false
      return
    }
    first.current = false
    setTabsPersistence(persist)
  }, [persist])
}

function Footer({ fixed }: { fixed: boolean }) {
  const { copyright, beian } = useSiteOptions()
  if (!copyright && !beian) return null

  return (
    <footer
      data-slot="app-footer"
      className={cn(
        'flex h-9 shrink-0 items-center justify-center gap-3 border-t bg-background px-4 text-xs text-muted-foreground',
        fixed && 'sticky bottom-0 z-10'
      )}
    >
      {copyright && <span>{copyright}</span>}
      {beian && (
        <a href="https://beian.miit.gov.cn" target="_blank" rel="noreferrer" className="hover:text-foreground">
          {beian}
        </a>
      )}
    </footer>
  )
}

/** Application shell shared by every authenticated section. */
export function BaseLayout({ defaultOpen, children }: { defaultOpen: boolean; children: React.ReactNode }) {
  const p = usePreferencesStore(
    useShallow(state => ({
      layout: state.values.layout,
      headerVisible: state.values.header_visible,
      navbarStyle: state.values.navbar_style,
      headerHeight: state.values.header_height,
      sidebarEnable: state.values.sidebar_enable,
      sidebarHidden: state.values.sidebar_hidden,
      sidebarToggle: state.values.widget_sidebar_toggle && state.values.sidebar_collapsed_button,
      autoActivateChild: state.values.sidebar_auto_activate_child,
      navigationSplit: state.values.navigation_split,
      tabbarEnable: state.values.tabbar_enable,
      breadcrumbEnable: state.values.breadcrumb_enable,
      footerEnable: state.values.footer_enable,
      footerFixed: state.values.footer_fixed,
      transitionEnable: state.values.transition_enable,
      transitionName: state.values.transition_name,
      transitionProgress: state.values.transition_progress,
      transitionLoading: state.values.transition_loading,
      contentLayout: state.values.content_layout,
      buttonPosition: state.values.app_preferences_button_position,
      collapsedShowTitle: state.values.sidebar_collapsed_show_title
    }))
  )

  const isMobile = useIsMobile()
  const pathname = usePathname()
  const { groups } = useMenuGroups()
  const roots = React.useMemo(() => toMenuRoots(groups), [groups])
  const contentMaximized = useShellStore(state => state.contentMaximized)
  const refreshKey = useShellStore(state => state.refreshKey)
  const preferencesOpen = useShellStore(state => state.preferencesOpen)
  const setPreferencesOpen = useShellStore(state => state.setPreferencesOpen)
  const { locked, lock, unlock, clear } = useLockScreen()
  const lastVisited = useLastVisited(roots, pathname)
  const { selectedId, pick } = useSelectedRoot(roots, pathname)
  const [mixedPick, setMixedPick] = React.useState<string>()
  const navigate = useNavigate()
  const routerNavigate = (href: string, external?: boolean) => {
    if (external) window.open(href, '_blank', 'noopener,noreferrer')
    else void navigate({ href })
  }
  const [preferencesMounted, setPreferencesMounted] = React.useState(false)
  if (preferencesOpen && !preferencesMounted) setPreferencesMounted(true)

  useApplyTabsPersistence()

  // Warm the preferences chunk while idle, so the first open is instant.
  React.useEffect(() => {
    const id =
      window.requestIdleCallback?.(() => void loadPreferencesSheet().then(() => setPreferencesMounted(true))) ??
      window.setTimeout(() => void loadPreferencesSheet().then(() => setPreferencesMounted(true)), 2000)
    return () => (window.cancelIdleCallback ? window.cancelIdleCallback(id) : window.clearTimeout(id))
  }, [])

  // Mobile always uses the vertical layout: horizontal menus do not fit.
  const layout: LayoutMode =
    isMobile && p.layout !== 'full-content' && p.layout !== 'sidebar-tree-nav' ? 'sidebar-nav' : p.layout
  const flags = LAYOUT_FLAGS[layout]
  const splitEmpty = layout === 'mixed-nav' && p.navigationSplit && !selectedRootItems(roots, selectedId)
  const sidebarKind = contentMaximized || !p.sidebarEnable || p.sidebarHidden || splitEmpty ? null : flags.sidebar
  const headerShown = !contentMaximized && layout !== 'full-content' && p.headerVisible
  const tabbarShown = layout !== 'full-content' && p.tabbarEnable
  const headerHidden = useHeaderHidden(
    headerShown && (p.navbarStyle === 'auto' || p.navbarStyle === 'auto-scroll'),
    p.navbarStyle,
    p.headerHeight
  )

  const selectedRoot = roots.find(root => root.id === selectedId)
  // Height the fixed sidebar must clear under a top header. The `scroll` style scrolls the
  // header away, so there the sidebar starts at the top and the header overlaps it while visible.
  const headerOffset =
    flags.headerOnTop && headerShown && p.navbarStyle !== 'scroll' && !headerHidden ? 'var(--app-header-height)' : '0px'

  const openRootImpl = (root: MenuRoot) => {
    pick(root.id)
    if (root.link) return
    if (p.autoActivateChild) {
      const target = lastVisited.get(root.id) ?? firstMenuLeaf(root.items)?.url
      if (target && target !== pathname) routerNavigate(target)
    }
  }

  // Sidebar content per layout; memoised so the sidebar skips renders that do not change it.
  const splitRoot = layout === 'mixed-nav' && p.navigationSplit ? selectedRoot : undefined
  const sidebarGroups = React.useMemo<readonly MenuGroup[]>(
    () =>
      splitRoot
        ? splitRoot.link
          ? []
          : [{ id: splitRoot.id, items: [...splitRoot.items] }]
        : layout === 'sidebar-tree-nav'
          ? toMenuTree(groups)
          : groups,
    [splitRoot, groups, layout]
  )

  // Two-column layouts: roots in the first column. In header-mixed the header carries the
  // roots, and the first column lists the selected root's items.
  const mixedEntries: MixedColumnEntry[] =
    layout === 'header-mixed-nav'
      ? (selectedRoot?.items ?? []).map(item => ({
          id: item.id,
          title: item.title,
          icon: item.icon,
          children: item.subItems ?? [],
          isActive: isMenuItemActive(item, pathname)
        }))
      : roots.map(root => ({
          id: root.id,
          title: root.title,
          icon: root.icon,
          children: root.link ? [] : root.items,
          isActive: isMenuRootActive(root, pathname)
        }))
  const mixedSelectedId =
    layout === 'header-mixed-nav'
      ? mixedPick && mixedEntries.some(e => e.id === mixedPick)
        ? mixedPick
        : mixedEntries.find(e => e.isActive)?.id
      : selectedId

  const selectMixedImpl = (entry: MixedColumnEntry, options?: { navigate?: boolean }) => {
    if (layout === 'header-mixed-nav') setMixedPick(entry.id)
    else pick(entry.id)
    if (options?.navigate === false) return

    if (entry.children.length === 0) {
      const item =
        layout === 'header-mixed-nav'
          ? selectedRoot?.items.find(i => i.id === entry.id)
          : roots.find(r => r.id === entry.id)?.link
      if (item) routerNavigate(item.url, item.external)
    } else if (p.autoActivateChild) {
      const target = firstMenuLeaf(entry.children)?.url
      if (target && target !== pathname) routerNavigate(target)
    }
  }

  const openRoot = useStableCallback(openRootImpl)
  const selectMixed = useStableCallback(selectMixedImpl)

  const headerMenu = React.useMemo(
    () =>
      flags.headerMenu
        ? { roots, mode: flags.headerMenu, selectedId, onSelect: flags.headerMenu === 'select' ? openRoot : undefined }
        : undefined,
    [flags.headerMenu, roots, selectedId, openRoot]
  )

  // Every non-fixed position lives in the header, so a hidden header always falls back to the
  // floating button; otherwise the preferences could become unreachable.
  const showPreferencesInHeader =
    headerShown && (p.buttonPosition === 'header' || (p.buttonPosition === 'auto' && !isMobile))
  const showFixedPreferences = p.buttonPosition === 'fixed' || !headerShown || (p.buttonPosition === 'auto' && isMobile)

  const header = headerShown && (
    <AdminHeader
      showBrand={flags.headerOnTop || !sidebarKind}
      brandAlignsSidebar={layout === 'header-sidebar-nav' && sidebarKind === 'primary'}
      showSidebarToggle={Boolean(sidebarKind === 'primary') && p.sidebarToggle}
      menu={headerMenu}
      showPreferences={showPreferencesInHeader}
      hidden={headerHidden}
    />
  )

  const content = (
    <>
      {!flags.headerOnTop && header}
      {tabbarShown && (
        // Pinned right under the header so they scroll as one block; the `scroll` navbar style
        // lets both scroll away, and an auto-hidden header lets the tabbar slide up to the top.
        <div
          className={cn(
            'z-30 bg-background',
            p.navbarStyle !== 'scroll' && 'sticky transition-[top] duration-200',
            headerShown && !headerHidden ? 'top-(--app-header-height)' : 'top-0'
          )}
        >
          <Tabbar />
        </div>
      )}
      {/* Layouts with a horizontal menu have no room for the breadcrumb in the header. Mixed
          two-column skips it: header menu, first column and tabbar already show the location. */}
      {headerShown && flags.headerMenu && layout !== 'header-mixed-nav' && p.breadcrumbEnable && (
        <AppBreadcrumb className="px-(--app-content-padding) pt-3" />
      )}
      <div
        data-slot="app-content"
        data-transition={p.transitionEnable && p.transitionName !== 'fade-shake' ? p.transitionName : undefined}
        className={cn(
          'relative flex min-h-0 min-w-0 flex-1 flex-col p-(--app-content-padding) has-data-[content-padding=false]:p-0',
          p.contentLayout === 'centered' &&
            '[&>[data-slot=page-surface]]:mx-auto [&>[data-slot=page-surface]]:max-w-(--app-content-width)'
        )}
      >
        <PageSurface
          key={`${pathname}:${refreshKey}`}
          showLoading={p.transitionLoading}
          transition={p.transitionEnable ? p.transitionName : undefined}
        >
          {children}
        </PageSurface>
      </div>
      {p.footerEnable && layout !== 'full-content' && <Footer fixed={p.footerFixed} />}
    </>
  )

  return (
    <SidebarProvider
      defaultOpen={defaultOpen}
      className={flags.headerOnTop ? 'flex-col' : 'flex-row'}
      style={
        {
          '--sidebar-width': 'var(--app-sidebar-width)',
          '--app-header-offset': headerOffset,
          '--sidebar-width-icon': p.collapsedShowTitle ? '4.5rem' : '3rem'
        } as React.CSSProperties
      }
    >
      {flags.headerOnTop && (
        <div inert={locked} className="contents">
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
        {sidebarKind === 'primary' && <AdminSidebar groups={sidebarGroups} showBrand={!flags.headerOnTop} />}
        {sidebarKind === 'mixed' && (
          <MixedSidebar
            entries={mixedEntries}
            selectedId={mixedSelectedId}
            onSelect={selectMixed}
            topOffset={flags.headerOnTop}
            showBrand={!flags.headerOnTop}
          />
        )}
        <SidebarInset className="min-w-0 overflow-x-clip peer-data-[variant=inset]:border">{content}</SidebarInset>
      </div>

      {p.transitionProgress && <RouteProgress />}
      <DocumentTitle />
      <Watermark />
      {!locked && <GlobalShortcuts />}
      <UpdateChecker />
      <MessageSocket />
      <NoticePopup />
      <SearchDialog />
      <LockScreenSetupDialog onLock={lock} />
      {locked && <LockScreenOverlay onUnlock={unlock} onForget={clear} />}
      {showFixedPreferences && (
        <Button
          aria-label="偏好设置"
          size="icon"
          onClick={() => setPreferencesOpen(true)}
          className="fixed top-1/2 right-0 z-50 -translate-y-1/2 rounded-r-none shadow-lg"
        >
          <Settings />
        </Button>
      )}
      {preferencesMounted && (
        <React.Suspense fallback={null}>
          <PreferencesSheet />
        </React.Suspense>
      )}
    </SidebarProvider>
  )
}
