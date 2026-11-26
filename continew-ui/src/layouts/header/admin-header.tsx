import * as React from 'react'

import { Link } from '@tanstack/react-router'
import { cn } from 'cn'
import { useShallow } from 'zustand/react/shallow'

import { SidebarTrigger, useSidebar } from '@/components/ui/sidebar'
import { useSiteOptions } from '@/hooks/use-site-options'
import type { MenuRoot } from '@/navigation/menu'
import { usePreferencesStore } from '@/stores/preferences/preferences-provider'

import { AppBreadcrumb } from './app-breadcrumb'
import { FullscreenToggle } from './fullscreen-toggle'
import { GithubHelp } from './github-help'
import { HeaderMenu } from './header-menu'
import { LockScreenButton, NotificationButton, PreferencesButton, RefreshButton, UserMenu } from './header-widgets'
import { SearchTrigger } from './search-dialog'
import { ThemeSwitcher } from './theme-switcher'
import { HOME_PATH } from '@/navigation/route-titles'

function HeaderBrand({ wide }: { wide: boolean }) {
  const { title, logo } = useSiteOptions()
  const { state } = useSidebar()
  const { variant, collapsible } = usePreferencesStore(
    useShallow(s => ({ variant: s.values.sidebar_variant, collapsible: s.values.sidebar_collapsible }))
  )
  // The box tracks the sidebar under it, so the toggle stays on the content edge. An offcanvas
  // sidebar leaves nothing to align with once collapsed. The plain `sidebar` variant has no
  // inner padding, so the box starts half a padding further left to line the logo up with the icons.
  const aligned = wide && !(state === 'collapsed' && collapsible === 'offcanvas')
  const rail = aligned && state === 'collapsed' && collapsible === 'icon'
  const plain = variant === 'sidebar'

  return (
    <Link
      to={HOME_PATH}
      aria-label={title || '返回仪表盘'}
      className={cn(
        'flex shrink-0 items-center gap-2 overflow-hidden font-semibold',
        aligned && 'transition-[width] duration-200 ease-linear motion-reduce:transition-none',
        aligned && plain && 'md:-ml-2',
        aligned &&
          !rail &&
          (plain ? 'md:w-[calc(var(--app-sidebar-width)-0.5rem)]' : 'md:w-[calc(var(--app-sidebar-width)-1rem)]'),
        rail && (plain ? 'md:w-[calc(var(--sidebar-width-icon)-0.5rem)]' : 'md:w-(--sidebar-width-icon)')
      )}
    >
      <img
        src={logo}
        alt=""
        className={cn(
          'size-8 shrink-0 object-contain',
          rail && 'md:ml-[max(0px,calc(var(--sidebar-width-icon)/2-1.5rem))]'
        )}
      />
      <span className={cn('hidden truncate sm:inline', rail && 'md:hidden')}>{title}</span>
    </Link>
  )
}

export interface AdminHeaderProps {
  /** Full-width header layouts show the brand in the header instead of the sidebar. */
  showBrand: boolean
  /** Brand box as wide as the sidebar under it (header-sidebar layout). */
  brandAlignsSidebar: boolean
  showSidebarToggle: boolean
  menu?: {
    roots: readonly MenuRoot[]
    mode: 'navigate' | 'select'
    selectedId?: string
    onSelect?: (root: MenuRoot) => void
  }
  showPreferences: boolean
  /** Scroll-driven hide for the `auto` and `auto-scroll` navbar styles. */
  hidden: boolean
}

export const AdminHeader = React.memo(function AdminHeader({
  showBrand,
  brandAlignsSidebar,
  showSidebarToggle,
  menu,
  showPreferences,
  hidden
}: AdminHeaderProps) {
  const w = usePreferencesStore(
    useShallow(state => ({
      search: state.values.widget_global_search,
      notification: state.values.widget_notification,
      refresh: state.values.widget_refresh,
      fullscreen: state.values.widget_fullscreen,
      theme: state.values.widget_theme_toggle,
      lock: state.values.widget_lock_screen,
      breadcrumb: state.values.breadcrumb_enable,
      menuAlign: state.values.header_menu_align,
      navbarStyle: state.values.navbar_style
    }))
  )
  const showBreadcrumb = w.breadcrumb && !menu

  return (
    <header
      data-slot="app-header"
      data-hidden={hidden || undefined}
      className={cn(
        'z-40 flex h-(--app-header-height) shrink-0 items-center gap-2 border-b bg-background px-3 transition-transform duration-200 lg:px-4',
        w.navbarStyle !== 'scroll' && 'sticky top-0 bg-background/80 backdrop-blur-md',
        hidden && '-translate-y-full'
      )}
    >
      <div className="flex shrink-0 items-center gap-1 lg:gap-2">
        {showBrand && <HeaderBrand wide={brandAlignsSidebar} />}
        {showSidebarToggle && <SidebarTrigger className="-ml-1" />}
        {showBreadcrumb && <AppBreadcrumb className="hidden md:flex" />}
      </div>

      <div
        className={cn(
          'flex min-w-0 flex-1 items-center',
          w.menuAlign === 'center' && 'justify-center',
          w.menuAlign === 'end' && 'justify-end'
        )}
      >
        {menu && <HeaderMenu {...menu} />}
      </div>

      <div className="flex shrink-0 items-center gap-1.5">
        {w.search && <SearchTrigger />}
        <GithubHelp />
        {w.refresh && <RefreshButton />}
        {w.notification && <NotificationButton />}
        {showPreferences && <PreferencesButton />}
        {w.lock && <LockScreenButton />}
        {w.theme && <ThemeSwitcher />}
        {w.fullscreen && <FullscreenToggle />}
        <UserMenu showPreferences={!showPreferences} />
      </div>
    </header>
  )
})
