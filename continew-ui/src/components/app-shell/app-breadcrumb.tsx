import * as React from 'react'

import { Link, useRouter, useRouterState, type RegisteredRouter } from '@tanstack/react-router'
import { cn } from 'cn'
import { Home } from 'lucide-react'

import {
  Breadcrumb,
  BreadcrumbEllipsis,
  BreadcrumbItem,
  BreadcrumbLink,
  BreadcrumbList,
  BreadcrumbPage,
  BreadcrumbSeparator
} from '@/components/ui/breadcrumb'
import { Button } from '@/components/ui/button'
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from '@/components/ui/dropdown-menu'
import { useMenuRoutes } from '@/hooks/use-menu'
import { findMenuTrail, toPathname } from '@/navigation/menu'
import { formatSegmentTitle, STATIC_ROUTE_TITLES } from '@/navigation/route-titles'

export interface BreadcrumbItemData {
  readonly id: string
  readonly title: string
  readonly url?: string
}

export function useBreadcrumbTrail(): BreadcrumbItemData[] {
  const router = useRouter()
  const pathname = useRouterState({ select: s => s.location.pathname })
  const { data: menuRoutes } = useMenuRoutes()

  return React.useMemo(() => {
    const homeItem: BreadcrumbItemData = {
      id: 'app-home',
      title: '首页',
      url: '/dashboard'
    }

    // The dashboard is the home overview, so it has no trail.
    if (pathname === '/dashboard' || pathname === '/dashboard/') {
      return []
    }

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
          url
        }
      })

      return [homeItem, ...trailItems]
    }

    if (STATIC_ROUTE_TITLES[pathname]) {
      return [
        homeItem,
        {
          id: `static-${pathname}`,
          title: STATIC_ROUTE_TITLES[pathname]
        }
      ]
    }

    // Fall back to path segments for real routes only; unknown paths are 404s and get no trail.
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

interface BreadcrumbViewProps {
  readonly items: BreadcrumbItemData[]
  readonly className?: string
}

/** The home crumb: a link when it has a target, otherwise muted text. */
function HomeCrumb({ title, url }: { title: string; url?: string }) {
  const content = (
    <>
      <Home className="size-3.5" />
      <span>{title}</span>
    </>
  )

  if (!url) {
    return <span className="flex items-center gap-1.5 text-muted-foreground">{content}</span>
  }

  return (
    <BreadcrumbLink
      render={
        <Link<RegisteredRouter, string, string>
          href={url}
          className="flex items-center gap-1.5 transition-colors hover:text-foreground"
        />
      }
    >
      {content}
    </BreadcrumbLink>
  )
}

export function BreadcrumbView({ items, className }: BreadcrumbViewProps) {
  if (items.length === 0) {
    return null
  }

  const firstItem = items[0]
  const middleItems = items.slice(1, -1)
  const lastItem = items[items.length - 1]

  return (
    <div className={cn('px-4 pt-3.5 pb-2 md:px-6 md:pt-4', className)}>
      <Breadcrumb className="select-none">
        <BreadcrumbList>
          <BreadcrumbItem>
            <HomeCrumb title={firstItem.title} url={firstItem.url} />
          </BreadcrumbItem>

          {middleItems.length > 0 && (
            <>
              {middleItems.map(item => (
                <React.Fragment key={item.id}>
                  <BreadcrumbSeparator className="hidden md:inline-flex" />
                  <BreadcrumbItem className="hidden md:inline-flex">
                    {item.url ? (
                      <BreadcrumbLink
                        render={
                          <Link<RegisteredRouter, string, string>
                            href={item.url}
                            className="transition-colors hover:text-foreground"
                          />
                        }
                      >
                        {item.title}
                      </BreadcrumbLink>
                    ) : (
                      <span className="text-muted-foreground">{item.title}</span>
                    )}
                  </BreadcrumbItem>
                </React.Fragment>
              ))}

              {/* Intermediate items fold into a menu on mobile. */}
              <BreadcrumbSeparator className="md:hidden" />
              <BreadcrumbItem className="md:hidden">
                <DropdownMenu>
                  <DropdownMenuTrigger
                    render={
                      <Button
                        type="button"
                        variant="ghost"
                        size="icon-xs"
                        aria-label="展开路径"
                        className="size-5 rounded-xs"
                      />
                    }
                  >
                    <BreadcrumbEllipsis />
                  </DropdownMenuTrigger>
                  <DropdownMenuContent align="start" className="min-w-32">
                    {middleItems.map(item => (
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
            </>
          )}

          <BreadcrumbSeparator />
          <BreadcrumbItem>
            <BreadcrumbPage className="font-medium text-foreground">{lastItem.title}</BreadcrumbPage>
          </BreadcrumbItem>
        </BreadcrumbList>
      </Breadcrumb>
    </div>
  )
}

/** Breadcrumb bound to the current route; the trail is computed here and handed to the view. */
export function AppBreadcrumb({ className }: { className?: string }) {
  const items = useBreadcrumbTrail()

  return <BreadcrumbView items={items} className={className} />
}
