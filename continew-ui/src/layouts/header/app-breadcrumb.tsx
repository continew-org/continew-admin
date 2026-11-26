import * as React from 'react'

import { Link, useRouter, useRouterState, type RegisteredRouter } from '@tanstack/react-router'
import { cn } from 'cn'
import { Home } from 'lucide-react'
import { useShallow } from 'zustand/react/shallow'

import {
  Breadcrumb,
  BreadcrumbEllipsis,
  BreadcrumbItem,
  BreadcrumbLink,
  BreadcrumbList,
  BreadcrumbPage,
  BreadcrumbSeparator
} from '@/components/ui/breadcrumb'
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from '@/components/ui/dropdown-menu'
import { useMenuRoutes } from '@/hooks/use-menu'
import { findMenuTrail, toPathname } from '@/navigation/menu'
import { resolveMenuIcon } from '@/navigation/menu-icons'
import { HOME_PATH, formatSegmentTitle, STATIC_ROUTE_ICONS, STATIC_ROUTE_TITLES } from '@/navigation/route-titles'
import { usePreferencesStore } from '@/stores/preferences/preferences-provider'

export interface BreadcrumbItemData {
  id: string
  title: string
  url?: string
  icon?: string | null
}

export function useBreadcrumbTrail(): BreadcrumbItemData[] {
  const router = useRouter()
  const pathname = useRouterState({ select: s => s.location.pathname })
  const { data: menuRoutes } = useMenuRoutes()

  return React.useMemo(() => {
    const homeItem: BreadcrumbItemData = {
      id: 'app-home',
      title: '首页',
      url: HOME_PATH
    }

    // The home page itself has no trail: its only crumb would link to itself.
    if (pathname === HOME_PATH || pathname === `${HOME_PATH}/`) {
      return []
    }

    // Dynamic backend menu matching
    const menuTrail = findMenuTrail(menuRoutes ?? [], pathname)
    if (menuTrail.length > 0) {
      const trailItems: BreadcrumbItemData[] = menuTrail.map((node, index) => {
        const isLast = index === menuTrail.length - 1
        const url = isLast
          ? undefined
          : node.redirect
            ? toPathname(node.redirect)
            : node.type === 2
              ? toPathname(node.path)
              : undefined

        return {
          id: `menu-${node.id}`,
          title: node.title,
          url,
          icon: node.icon
        }
      })

      return [homeItem, ...trailItems]
    }

    // Static route matching
    if (STATIC_ROUTE_TITLES[pathname]) {
      return [
        homeItem,
        {
          id: `static-${pathname}`,
          title: STATIC_ROUTE_TITLES[pathname],
          icon: STATIC_ROUTE_ICONS[pathname]
        }
      ]
    }

    // General fallback: split pathname segments for real routes only. Unknown paths are 404s,
    // so they must not produce a synthetic breadcrumb.
    if (!(pathname in router.routesByPath)) {
      return []
    }

    const segments = pathname.split('/').filter(Boolean)
    if (segments.length === 0) {
      return []
    }

    const dynamicItems: BreadcrumbItemData[] = segments.map((segment, index) => {
      const isLast = index === segments.length - 1
      const currentPath = `/${segments.slice(0, index + 1).join('/')}`
      return {
        id: `segment-${segment}-${index}`,
        title: STATIC_ROUTE_TITLES[currentPath] ?? formatSegmentTitle(segment),
        url: isLast ? undefined : currentPath
      }
    })

    return [homeItem, ...dynamicItems]
  }, [pathname, menuRoutes, router])
}

function CrumbIcon({ show, icon, isHome }: { show: boolean; icon?: string | null; isHome?: boolean }) {
  if (!show) return null
  if (isHome) return <Home className="size-3.5" />
  if (!icon) return null
  return React.createElement(resolveMenuIcon(icon), { className: 'size-3.5' })
}

/**
 * Breadcrumb for the header. Honours the breadcrumb preferences: show icons, show the home
 * item, hide a single-item trail, and the plain or pill-background style.
 */
export function AppBreadcrumb({ className }: { className?: string }) {
  const trail = useBreadcrumbTrail()
  const { showIcon, showHome, hideOnlyOne, style } = usePreferencesStore(
    useShallow(state => ({
      showIcon: state.values.breadcrumb_show_icon,
      showHome: state.values.breadcrumb_show_home,
      hideOnlyOne: state.values.breadcrumb_hide_only_one,
      style: state.values.breadcrumb_style
    }))
  )

  const items: BreadcrumbItemData[] = showHome ? trail : trail.filter(item => item.id !== 'app-home')

  if (items.length === 0 || (hideOnlyOne && items.length === 1)) {
    return null
  }

  const pill = style === 'background'
  const crumbClass = cn('flex items-center gap-1.5', pill && 'rounded-md bg-accent px-2 py-0.5')
  const lastItem = items[items.length - 1]
  const leading = items.slice(0, -1)
  // Mobile keeps the first and last crumbs and folds the rest into a menu.
  const folded = leading.slice(1)

  const renderCrumb = (item: BreadcrumbItemData) => {
    const isHome = item.id === 'app-home'
    const content = (
      <>
        <CrumbIcon show={showIcon} icon={item.icon} isHome={isHome} />
        <span>{item.title}</span>
      </>
    )

    return item.url ? (
      <BreadcrumbLink
        render={
          <Link<RegisteredRouter, string, string>
            href={item.url}
            className={cn(crumbClass, 'transition-colors hover:text-foreground')}
          />
        }
      >
        {content}
      </BreadcrumbLink>
    ) : (
      <span className={cn(crumbClass, 'text-muted-foreground')}>{content}</span>
    )
  }

  return (
    <Breadcrumb className={cn('min-w-0 select-none', className)}>
      <BreadcrumbList className="flex-nowrap">
        {leading.map((item, index) => (
          <React.Fragment key={item.id}>
            <BreadcrumbItem className={cn(index > 0 && 'hidden md:inline-flex')}>{renderCrumb(item)}</BreadcrumbItem>
            <BreadcrumbSeparator className={cn(index > 0 && 'hidden md:inline-flex')} />
          </React.Fragment>
        ))}

        {folded.length > 0 && (
          <>
            <BreadcrumbItem className="md:hidden">
              <DropdownMenu>
                <DropdownMenuTrigger
                  render={
                    <button
                      type="button"
                      aria-label="展开路径"
                      className="flex size-5 items-center justify-center rounded-xs transition-colors hover:bg-muted"
                    />
                  }
                >
                  <BreadcrumbEllipsis />
                </DropdownMenuTrigger>
                <DropdownMenuContent align="start" className="min-w-32">
                  {folded.map(item => (
                    <DropdownMenuItem
                      key={item.id}
                      render={
                        item.url ? (
                          <Link<RegisteredRouter, string, string>
                            href={item.url}
                            className="flex w-full items-center gap-2"
                          />
                        ) : undefined
                      }
                    >
                      <span>{item.title}</span>
                    </DropdownMenuItem>
                  ))}
                </DropdownMenuContent>
              </DropdownMenu>
            </BreadcrumbItem>
            <BreadcrumbSeparator className="md:hidden" />
          </>
        )}

        <BreadcrumbItem className="min-w-0">
          <BreadcrumbPage className={cn(crumbClass, 'min-w-0 font-medium text-foreground')}>
            <CrumbIcon show={showIcon} icon={lastItem.icon} isHome={lastItem.id === 'app-home'} />
            <span className="truncate">{lastItem.title}</span>
          </BreadcrumbPage>
        </BreadcrumbItem>
      </BreadcrumbList>
    </Breadcrumb>
  )
}
