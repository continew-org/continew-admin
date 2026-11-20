import * as React from 'react'

import { useScrollFade } from '@animate-ui/react/primitives/effects/scroll-fade'
import { useRouterState } from '@tanstack/react-router'
import { cn } from 'cn'
import { ChevronDown } from 'lucide-react'

import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSub,
  DropdownMenuSubContent,
  DropdownMenuSubTrigger,
  DropdownMenuTrigger
} from '@/components/ui/dropdown-menu'
import { isMenuItemActive, isMenuRootActive, toPathname, type MenuItem, type MenuRoot } from '@/navigation/menu'

import { renderItemLink } from '../sidebar/nav-main'

const itemClass =
  'flex h-9 shrink-0 items-center gap-1.5 rounded-md px-3 text-sm whitespace-nowrap transition-colors outline-none focus-visible:ring-2 focus-visible:ring-ring/50 [&_svg]:size-4'

function itemState(active: boolean) {
  return active
    ? 'bg-primary/10 font-medium text-primary'
    : 'text-muted-foreground hover:bg-accent hover:text-foreground'
}

function MenuEntries({ items, pathname }: { items: readonly MenuItem[]; pathname: string }) {
  return items.map(item =>
    item.subItems?.length ? (
      <DropdownMenuSub key={item.id}>
        <DropdownMenuSubTrigger className={cn(isMenuItemActive(item, pathname) && 'text-primary')}>
          {item.icon && <item.icon />}
          {item.title}
        </DropdownMenuSubTrigger>
        <DropdownMenuSubContent className="min-w-44">
          <MenuEntries items={item.subItems} pathname={pathname} />
        </DropdownMenuSubContent>
      </DropdownMenuSub>
    ) : (
      <DropdownMenuItem
        key={item.id}
        render={renderItemLink(item)}
        className={cn(toPathname(item.url) === pathname && 'bg-accent text-primary')}
      >
        {item.icon && <item.icon />}
        {item.title}
      </DropdownMenuItem>
    )
  )
}

interface HeaderMenuProps {
  roots: readonly MenuRoot[]
  /** `navigate`: roots open their pages in a dropdown. `select`: a click picks the root for the sidebar. */
  mode: 'navigate' | 'select'
  selectedId?: string
  onSelect?: (root: MenuRoot) => void
}

/** Horizontal menu in the header for the header and mixed layouts. */
export const HeaderMenu = React.memo(function HeaderMenu({ roots, mode, selectedId, onSelect }: HeaderMenuProps) {
  const pathname = useRouterState({ select: state => state.location.pathname })
  const navRef = React.useRef<HTMLElement>(null)

  useScrollFade(navRef, { orientation: 'horizontal', blur: 4 })

  // A vertical wheel over the strip scrolls it sideways; native listener so it can be non-passive.
  React.useEffect(() => {
    const node = navRef.current
    if (!node) return

    const onWheel = (event: WheelEvent) => {
      if (Math.abs(event.deltaY) <= Math.abs(event.deltaX)) return
      event.preventDefault()
      node.scrollLeft += event.deltaY
    }

    node.addEventListener('wheel', onWheel, { passive: false })
    return () => node.removeEventListener('wheel', onWheel)
  }, [])

  return (
    <nav
      ref={navRef}
      aria-label="顶部菜单"
      className="flex w-full min-w-0 scrollbar-none items-center gap-1 overflow-x-auto"
    >
      {roots.map(root => {
        const Icon = root.icon
        const active = mode === 'select' ? root.id === selectedId : isMenuRootActive(root, pathname)
        const label = (
          <>
            {Icon && <Icon />}
            <span>{root.title}</span>
          </>
        )

        if (root.link) {
          return (
            <React.Fragment key={root.id}>
              {React.cloneElement(renderItemLink(root.link, { className: cn(itemClass, itemState(active)) }), {
                children: label,
                onClick: () => onSelect?.(root)
              })}
            </React.Fragment>
          )
        }

        if (mode === 'select') {
          return (
            <button
              key={root.id}
              type="button"
              onClick={() => onSelect?.(root)}
              className={cn(itemClass, itemState(active))}
            >
              {label}
            </button>
          )
        }

        return (
          <DropdownMenu key={root.id}>
            <DropdownMenuTrigger render={<button type="button" className={cn(itemClass, itemState(active))} />}>
              {label}
              <ChevronDown className="size-3.5! opacity-60" />
            </DropdownMenuTrigger>
            <DropdownMenuContent align="start" className="min-w-44">
              <MenuEntries items={root.items} pathname={pathname} />
            </DropdownMenuContent>
          </DropdownMenu>
        )
      })}
    </nav>
  )
})
