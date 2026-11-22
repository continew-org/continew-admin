import * as React from 'react'

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

import { usePathname } from '../route-info'
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
  const pathname = usePathname()

  return (
    <nav aria-label="顶部菜单" className="flex min-w-0 scrollbar-none items-center gap-1 overflow-x-auto">
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
