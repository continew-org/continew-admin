import * as React from 'react'

import { Link } from '@tanstack/react-router'
import { cn } from 'cn'
import { useShallow } from 'zustand/react/shallow'

import { Separator } from '@/components/ui/separator'
import { SidebarTrigger } from '@/components/ui/sidebar'
import { useSiteOptions } from '@/hooks/use-site-options'
import type { MenuRoot } from '@/navigation/menu'
import { usePreferencesStore } from '@/stores/preferences/preferences-provider'

import { FullscreenToggle } from './fullscreen-toggle'
import { GithubHelp } from './github-help'
import { HeaderMenu } from './header-menu'
import { LayoutControls } from './layout-controls'
import { NotificationButton } from './notification-button'
import { SearchTrigger } from './search-dialog'
import { ThemeSwitcher } from './theme-switcher'
import { UserMenu } from './user-menu'

function HeaderBrand() {
  const { title, logo } = useSiteOptions()

  return (
    <Link to="/dashboard" aria-label={title || '返回仪表盘'} className="flex shrink-0 items-center gap-2 font-semibold">
      <img src={logo} alt="" className="size-8 shrink-0 object-contain" />
      <span className="hidden truncate sm:inline">{title}</span>
    </Link>
  )
}

export interface AdminHeaderProps {
  /** Full-width / no-sidebar layouts show the brand in the header. */
  showBrand: boolean
  showSidebarToggle: boolean
  menu?: {
    roots: readonly MenuRoot[]
    mode: 'navigate' | 'select'
    selectedId?: string
    onSelect?: (root: MenuRoot) => void
  }
}

/** The app header. Composes the shared header widgets; the horizontal menu is optional. */
export const AdminHeader = React.memo(function AdminHeader({ showBrand, showSidebarToggle, menu }: AdminHeaderProps) {
  const { menuAlign, notification } = usePreferencesStore(
    useShallow(state => ({
      menuAlign: state.values.header_menu_align,
      notification: state.values.widget_notification
    }))
  )

  return (
    <header
      className={cn(
        'flex h-12 shrink-0 items-center gap-2 border-b px-4 transition-[width,height] ease-linear lg:px-6',
        // Sticky navbar styling; keeps the glass/border consistent across sidebar variants.
        '[html[data-navbar-style=sticky]_&]:sticky [html[data-navbar-style=sticky]_&]:top-0 [html[data-navbar-style=sticky]_&]:z-50 [html[data-navbar-style=sticky]_&]:overflow-hidden [html[data-navbar-style=sticky]_&]:rounded-t-[inherit] [html[data-navbar-style=sticky]_&]:bg-background/50 [html[data-navbar-style=sticky]_&]:backdrop-blur-md'
      )}
    >
      <div className="flex shrink-0 items-center gap-1 lg:gap-2">
        {showBrand && <HeaderBrand />}
        {showSidebarToggle && <SidebarTrigger className="-ml-1" />}
        {(showBrand || showSidebarToggle) && (
          <Separator
            orientation="vertical"
            className="mx-1 data-[orientation=vertical]:h-4 data-[orientation=vertical]:self-center lg:mx-2"
          />
        )}
        <SearchTrigger />
      </div>

      <div
        className={cn(
          'flex min-w-0 flex-1 items-center',
          menuAlign === 'center' && 'justify-center',
          menuAlign === 'end' && 'justify-end'
        )}
      >
        {menu && <HeaderMenu {...menu} />}
      </div>

      <div className="flex shrink-0 items-center gap-3">
        <LayoutControls />
        {notification && <NotificationButton />}
        <div className="hidden md:contents">
          <GithubHelp />
        </div>
        <ThemeSwitcher />
        <div className="hidden md:contents">
          <FullscreenToggle />
        </div>
        <UserMenu />
      </div>
    </header>
  )
})
