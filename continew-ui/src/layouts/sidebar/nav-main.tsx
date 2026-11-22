import * as React from 'react'

import { Link, type RegisteredRouter } from '@tanstack/react-router'
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
import { isMenuItemActive, toPathname, type MenuGroup, type MenuItem } from '@/navigation/menu'
import { usePreferencesStore } from '@/stores/preferences/preferences-provider'

import { usePathname } from '../route-info'

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

/**
 * Menu paths come from the backend, so links use the raw `href`. External links open in a
 * new tab. `Link`'s type arguments are pinned so an `href`-only link type-checks.
 */
export function renderItemLink(item: MenuItem, options?: { className?: string; 'aria-current'?: 'page' }) {
  if (item.external) {
    return <a href={item.url} target="_blank" rel="noreferrer" {...options} />
  }

  return <Link<RegisteredRouter, string, string> href={item.url} {...options} />
}

/**
 * Accordion state: with `navigation_accordion` on, opening one parent closes its siblings.
 * The parent that holds the current page opens on every navigation.
 */
function useOpenParents(groups: readonly MenuGroup[], pathname: string) {
  const accordion = usePreferencesStore(state => state.values.navigation_accordion)
  const activeId = React.useMemo(() => {
    for (const group of groups) {
      for (const item of group.items) {
        if (hasSubItems(item) && isMenuItemActive(item, pathname)) return item.id
      }
    }
    return undefined
  }, [groups, pathname])

  const [open, setOpen] = React.useState<ReadonlySet<string>>(() => new Set(activeId ? [activeId] : []))

  // Adjusting state during render (instead of an effect) avoids a frame with the old parent open.
  const [lastActiveId, setLastActiveId] = React.useState(activeId)
  if (activeId !== lastActiveId) {
    setLastActiveId(activeId)
    if (activeId && !open.has(activeId)) {
      setOpen(accordion ? new Set([activeId]) : new Set([...open, activeId]))
    }
  }

  const toggle = React.useCallback(
    (id: string, next: boolean) => {
      setOpen(current => {
        if (!next) {
          const copy = new Set(current)
          copy.delete(id)
          return copy
        }
        return accordion ? new Set([id]) : new Set([...current, id])
      })
    },
    [accordion]
  )

  return { open, toggle }
}

/**
 * `forceExpanded`: the collapsed rail is peeking at full width, so items render as in the
 * expanded sidebar (labels, collapsible parents, no tooltips).
 */
export function NavMain({
  items,
  forceExpanded = false
}: {
  readonly items: readonly MenuGroup[]
  readonly forceExpanded?: boolean
}) {
  const pathname = usePathname()
  const { open, toggle } = useOpenParents(items, pathname)

  return items.map(group => (
    <SidebarGroup key={group.id}>
      {group.label && (
        <SidebarGroupLabel className="group-data-[collapsible=icon]:pointer-events-none">
          {group.label}
        </SidebarGroupLabel>
      )}
      <SidebarGroupContent>
        <SidebarMenu className="gap-1">
          {group.items.map(item => (
            <NavItem
              key={item.id}
              item={item}
              pathname={pathname}
              open={open.has(item.id)}
              onToggle={toggle}
              forceExpanded={forceExpanded}
            />
          ))}
        </SidebarMenu>
      </SidebarGroupContent>
    </SidebarGroup>
  ))
}

interface NavItemProps {
  item: MenuItem
  pathname: string
  open: boolean
  onToggle: (id: string, open: boolean) => void
  forceExpanded: boolean
}

const NavItem = React.memo(function NavItem({ item, pathname, open, onToggle, forceExpanded }: NavItemProps) {
  const { state, isMobile } = useSidebar()
  const isCollapsedDesktop = state === 'collapsed' && !isMobile && !forceExpanded
  // Tooltips only make sense on the bare icon rail.
  const tooltip = { children: item.title, hidden: !isCollapsedDesktop }
  const isActive = isMenuItemActive(item, pathname)

  if (!hasSubItems(item)) {
    return (
      <SidebarMenuItem>
        <SidebarMenuButton render={renderItemLink(item)} tooltip={tooltip} isActive={isActive}>
          {item.icon ? <item.icon /> : isCollapsedDesktop && <CollapsedIconFallback title={item.title} />}
          <span>{item.title}</span>
        </SidebarMenuButton>
      </SidebarMenuItem>
    )
  }

  if (isCollapsedDesktop) {
    return <NavDropdownItem item={item} isActive={isActive} pathname={pathname} />
  }

  const Icon = item.icon

  return (
    <Collapsible
      render={<li data-slot="sidebar-menu-item" data-sidebar="menu-item" className="group/menu-item relative" />}
      open={open}
      onOpenChange={next => onToggle(item.id, next)}
      className="group/collapsible"
    >
      <CollapsibleTrigger render={<SidebarMenuButton tooltip={tooltip} isActive={isActive && !open} />}>
        {Icon && <Icon />}
        <span>{item.title}</span>
        <ChevronRight className="ml-auto transition-transform duration-200 group-data-panel-open/menu-button:rotate-90" />
      </CollapsibleTrigger>

      <CollapsibleContent className="h-(--collapsible-panel-height) overflow-hidden transition-[height] duration-200 ease-out data-ending-style:h-0 data-starting-style:h-0">
        <SidebarMenuSub>
          {item.subItems.map(subItem => (
            <SidebarMenuSubItem key={subItem.id}>
              <SidebarMenuSubButton render={renderItemLink(subItem)} isActive={toPathname(subItem.url) === pathname}>
                {subItem.icon && <subItem.icon />}
                <span>{subItem.title}</span>
              </SidebarMenuSubButton>
            </SidebarMenuSubItem>
          ))}
        </SidebarMenuSub>
      </CollapsibleContent>
    </Collapsible>
  )
})

function NavDropdownItem({ item, isActive, pathname }: { item: MenuItem; isActive: boolean; pathname: string }) {
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
            {item.subItems?.map(subItem => (
              <DropdownMenuItem
                key={subItem.id}
                render={renderItemLink(subItem, {
                  'aria-current': toPathname(subItem.url) === pathname ? 'page' : undefined,
                  className: 'flex items-center gap-2'
                })}
              >
                {subItem.icon && <subItem.icon />}
                <span>{subItem.title}</span>
              </DropdownMenuItem>
            ))}
          </DropdownMenuGroup>
        </DropdownMenuContent>
      </DropdownMenu>
    </SidebarMenuItem>
  )
}
