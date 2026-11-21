import { useAutoAnimate } from '@formkit/auto-animate/react'
import { Link, useRouterState, type RegisteredRouter } from '@tanstack/react-router'

import { ChevronRight } from 'lucide-react'

import { Collapsible, CollapsibleContent, CollapsibleTrigger } from '@/components/ui/collapsible'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuItem,
  DropdownMenuTrigger
} from '@/components/ui/dropdown-menu'
import {
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarMenuSub,
  SidebarMenuSubButton,
  SidebarMenuSubItem,
  useSidebar
} from '@/components/ui/sidebar'
import { toPathname, type MenuGroup, type MenuItem } from '@/navigation/menu'

interface NavMainProps {
  readonly items: readonly MenuGroup[]
}

interface NavItemProps {
  readonly item: MenuItem
  readonly isItemActive: (item: MenuItem) => boolean
  readonly isSubItemActive: (url: string) => boolean
  readonly isSubmenuOpen: (item: MenuItem) => boolean
}

interface NavLinkItemProps {
  readonly item: MenuItem
  readonly isActive: boolean
  readonly showIconFallback: boolean
  readonly tooltip: string | undefined
}

interface NavDropdownItemProps {
  readonly item: MenuItem
  readonly isActive: boolean
  readonly isSubItemActive: (url: string) => boolean
}

interface NavCollapsibleItemProps {
  readonly item: MenuItem
  readonly isActive: boolean
  readonly defaultOpen: boolean
  readonly isSubItemActive: (url: string) => boolean
}

function CollapsedIconFallback({ title }: { title: string }) {
  return (
    <span className="flex size-4 shrink-0 items-center justify-center rounded-xs text-[10px] font-medium outline">
      {title.slice(0, 1)}
    </span>
  )
}

function hasSubItems(item: MenuItem): item is MenuItem & { subItems: MenuItem[] } {
  return Boolean(item.subItems?.length)
}

interface RenderItemLinkOptions {
  className?: string
  'aria-current'?: 'page' | undefined
}

/**
 * Menu paths come from the backend, so links use the raw `href` instead of a typed
 * route path: a path without a page is served by the placeholder route, and `href` is
 * what splits a query string off the path.
 *
 * External links open in a new tab; internal links use `Link`.
 * `Link` only makes `to` optional once its `TTo` parameter is `string`, so the type
 * arguments are pinned; without them every `href`-only link is a type error.
 */
export function renderItemLink(item: MenuItem, options?: RenderItemLinkOptions) {
  if (item.external) {
    return <a href={item.url} target="_blank" rel="noreferrer" {...options} />
  }

  return <Link<RegisteredRouter, string, string> href={item.url} {...options} />
}

export function NavMain({ items }: NavMainProps) {
  const path = useRouterState({ select: state => state.location.pathname })

  const isItemActive = (item: MenuItem) => {
    if (hasSubItems(item)) {
      return item.subItems.some(subItem => toPathname(subItem.url) === path)
    }

    return toPathname(item.url) === path
  }

  const isSubItemActive = (url: string) => toPathname(url) === path

  const isSubmenuOpen = (item: MenuItem) => {
    return Boolean(item.subItems?.some(subItem => toPathname(subItem.url) === path))
  }

  return (
    <>
      {items.map(group => (
        <NavGroupSection
          key={group.id}
          group={group}
          isItemActive={isItemActive}
          isSubItemActive={isSubItemActive}
          isSubmenuOpen={isSubmenuOpen}
        />
      ))}
    </>
  )
}

function NavGroupSection({
  group,
  isItemActive,
  isSubItemActive,
  isSubmenuOpen
}: {
  group: MenuGroup
  isItemActive: (item: MenuItem) => boolean
  isSubItemActive: (url: string) => boolean
  isSubmenuOpen: (item: MenuItem) => boolean
}) {
  const [menuRef] = useAutoAnimate<HTMLUListElement>({ duration: 250, easing: 'ease-out' })

  return (
    <SidebarGroup>
      {group.label && (
        <SidebarGroupLabel className="group-data-[collapsible=icon]:pointer-events-none">
          {group.label}
        </SidebarGroupLabel>
      )}
      <SidebarGroupContent>
        <SidebarMenu ref={menuRef} className="gap-1">
          {group.items.map(item => (
            <NavItem
              key={item.id}
              item={item}
              isItemActive={isItemActive}
              isSubItemActive={isSubItemActive}
              isSubmenuOpen={isSubmenuOpen}
            />
          ))}
        </SidebarMenu>
      </SidebarGroupContent>
    </SidebarGroup>
  )
}

function NavItem({ item, isItemActive, isSubItemActive, isSubmenuOpen }: NavItemProps) {
  const { state, isMobile } = useSidebar()
  const isCollapsedDesktop = state === 'collapsed' && !isMobile

  if (!hasSubItems(item)) {
    return (
      <NavLinkItem
        item={item}
        isActive={isItemActive(item)}
        showIconFallback={isCollapsedDesktop}
        tooltip={isCollapsedDesktop ? item.title : undefined}
      />
    )
  }

  if (isCollapsedDesktop) {
    return <NavDropdownItem item={item} isActive={isItemActive(item)} isSubItemActive={isSubItemActive} />
  }

  return (
    <NavCollapsibleItem
      item={item}
      isActive={isItemActive(item)}
      defaultOpen={isSubmenuOpen(item)}
      isSubItemActive={isSubItemActive}
    />
  )
}

function NavLinkItem({ item, isActive, showIconFallback, tooltip }: NavLinkItemProps) {
  return (
    <SidebarMenuItem>
      <SidebarMenuButton render={renderItemLink(item)} tooltip={tooltip} isActive={isActive}>
        <NavLinkIcon item={item} showFallback={showIconFallback} />
        <span>{item.title}</span>
      </SidebarMenuButton>
    </SidebarMenuItem>
  )
}

function NavLinkIcon({ item, showFallback }: { item: MenuItem; showFallback: boolean }) {
  const Icon = item.icon

  if (Icon) {
    return <Icon />
  }

  if (showFallback) {
    return <CollapsedIconFallback title={item.title} />
  }

  return null
}

function NavDropdownItem({ item, isActive, isSubItemActive }: NavDropdownItemProps) {
  const Icon = item.icon

  return (
    <SidebarMenuItem>
      <DropdownMenu>
        <DropdownMenuTrigger render={<SidebarMenuButton tooltip={item.title} isActive={isActive} />}>
          {Icon ? <Icon /> : <CollapsedIconFallback title={item.title} />}
          <span>{item.title}</span>
        </DropdownMenuTrigger>

        <DropdownMenuContent side="right" align="start" sideOffset={12} className="w-48">
          <DropdownMenuGroup>
            {item.subItems?.map(subItem => {
              const SubIcon = subItem.icon

              return (
                <DropdownMenuItem
                  key={subItem.id}
                  render={renderItemLink(subItem, {
                    'aria-current': isSubItemActive(subItem.url) ? 'page' : undefined,
                    className: 'flex items-center gap-2'
                  })}
                >
                  {SubIcon && <SubIcon />}
                  <span>{subItem.title}</span>
                </DropdownMenuItem>
              )
            })}
          </DropdownMenuGroup>
        </DropdownMenuContent>
      </DropdownMenu>
    </SidebarMenuItem>
  )
}

function NavCollapsibleItem({ item, isActive, defaultOpen, isSubItemActive }: NavCollapsibleItemProps) {
  const Icon = item.icon
  const [subRef] = useAutoAnimate<HTMLUListElement>({ duration: 250, easing: 'ease-out' })

  return (
    <Collapsible
      render={<li data-slot="sidebar-menu-item" data-sidebar="menu-item" className="group/menu-item relative" />}
      defaultOpen={defaultOpen}
      className="group/collapsible"
    >
      <CollapsibleTrigger render={<SidebarMenuButton isActive={isActive} />}>
        {Icon && <Icon />}
        <span>{item.title}</span>
        <ChevronRight className="ml-auto transition-transform duration-200 group-data-panel-open/menu-button:rotate-90" />
      </CollapsibleTrigger>

      <CollapsibleContent>
        <SidebarMenuSub ref={subRef}>
          {item.subItems?.map(subItem => {
            const SubIcon = subItem.icon

            return (
              <SidebarMenuSubItem key={subItem.id}>
                <SidebarMenuSubButton render={renderItemLink(subItem)} isActive={isSubItemActive(subItem.url)}>
                  {SubIcon && <SubIcon />}
                  <span>{subItem.title}</span>
                </SidebarMenuSubButton>
              </SidebarMenuSubItem>
            )
          })}
        </SidebarMenuSub>
      </CollapsibleContent>
    </Collapsible>
  )
}
