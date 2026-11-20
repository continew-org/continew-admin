import type { LucideIcon } from 'lucide-react'

import type { RouteResp } from '@/lib/api/types'

import { resolveMenuIcon } from './menu-icons'
import { STATIC_ROUTE_TITLES } from './route-titles'

export interface MenuItem {
  id: string
  title: string
  /** Backend path, used verbatim as the URL. */
  url: string
  icon?: LucideIcon
  /** External links open in a new tab instead of going through the router. */
  external?: boolean
  subItems?: MenuItem[]
}

export interface MenuGroup {
  id: string
  label?: string
  icon?: LucideIcon
  items: MenuItem[]
}

/**
 * A top-level navigation entry for the horizontal and two-column layouts: a labelled group
 * becomes one root with its items, and every loose item becomes a root of its own.
 */
export interface MenuRoot {
  id: string
  title: string
  icon?: LucideIcon
  /** Set when the root is a page itself instead of a container. */
  link?: MenuItem
  items: MenuItem[]
}

/** Backend menu type: 1 directory, 2 menu, 3 button. */
const TYPE_MENU = 2
const TYPE_BUTTON = 3

/** The backend stores paths with an optional query string, for example `?tab=site`. */
export function toPathname(path: string) {
  const queryIndex = path.indexOf('?')
  return queryIndex === -1 ? path : path.slice(0, queryIndex)
}

/** Buttons carry a permission code instead of a page, and hidden nodes are details. */
function isVisible(node: RouteResp) {
  return node.type !== TYPE_BUTTON && !node.isHidden
}

function toMenuItem(node: RouteResp): MenuItem {
  return {
    id: String(node.id),
    title: node.title,
    url: node.path,
    icon: resolveMenuIcon(node.icon),
    external: Boolean(node.isExternal)
  }
}

/** Nested directories are flattened: the sidebar renders a single sub-item level. */
function toMenuSubItems(nodes: readonly RouteResp[]): MenuItem[] {
  return nodes.filter(isVisible).flatMap(node => {
    if (node.type !== TYPE_MENU && node.children?.length) {
      return toMenuSubItems(node.children)
    }

    return [toMenuItem(node)]
  })
}

/**
 * Menu entries the app owns instead of the backend.
 *
 * `/dashboard` is a static route: it exists for every signed-in user, so it must not
 * depend on a `sys_menu` record or on the current user's permissions.
 */
export const STATIC_MENU_ITEMS: readonly MenuItem[] = [
  {
    id: 'app-dashboard',
    title: '仪表盘',
    url: '/dashboard',
    icon: resolveMenuIcon('dashboard')
  }
]

/**
 * Converts the backend menu tree into sidebar groups.
 *
 * A top-level directory becomes a labelled group, a menu becomes a link item, and a
 * directory nested inside a group becomes a collapsible parent item. Menus that sit
 * at the top level are collected into an unlabelled group in front of the rest, and
 * the app-owned static items are merged into that same group so they stay on top.
 */
export function toMenuGroups(nodes: readonly RouteResp[]): MenuGroup[] {
  const groups: MenuGroup[] = []
  const looseItems: MenuItem[] = [...STATIC_MENU_ITEMS]

  for (const node of nodes.filter(isVisible)) {
    if (node.type === TYPE_MENU || !node.children?.length) {
      looseItems.push(toMenuItem(node))
      continue
    }

    const items = node.children
      .filter(isVisible)
      .map(child =>
        child.type !== TYPE_MENU && child.children?.length
          ? { ...toMenuItem(child), subItems: toMenuSubItems(child.children) }
          : toMenuItem(child)
      )

    if (items.length > 0) {
      groups.push({ id: String(node.id), label: node.title, icon: resolveMenuIcon(node.icon), items })
    }
  }

  if (looseItems.length > 0) {
    groups.unshift({ id: 'root', items: looseItems })
  }

  return groups
}

/** Finds the full trail of menu entries leading to a pathname, for breadcrumb navigation. */
export function findMenuTrail(nodes: readonly RouteResp[], pathname: string): RouteResp[] {
  for (const node of nodes) {
    if (toPathname(node.path) === pathname) {
      return [node]
    }

    if (node.children?.length) {
      const match = findMenuTrail(node.children, pathname)
      if (match.length > 0) {
        return [node, ...match]
      }
    }
  }

  return []
}

/** Finds the menu entry behind a pathname, so placeholders can show its title. */
export function findMenuByPath(nodes: readonly RouteResp[], pathname: string) {
  return findMenuTrail(nodes, pathname).at(-1)
}

/** Normalizes a pathname for menu matching: no query string, no trailing slash. */
function normalizePathname(pathname: string) {
  const path = toPathname(pathname).replace(/\/+$/, '')
  return path || '/'
}

/**
 * Whether the current user may open `pathname`.
 *
 * App-owned static pages stay reachable for everyone; every other page has to be present in
 * the menu the backend already filtered by permission. The check walks the raw nodes, so a
 * menu hidden from the sidebar (but still routable) also counts as reachable.
 */
export function isPathAllowed(nodes: readonly RouteResp[], pathname: string) {
  const path = normalizePathname(pathname)
  return path in STATIC_ROUTE_TITLES || Boolean(findMenuByPath(nodes, path))
}

export function toMenuRoots(groups: readonly MenuGroup[]): MenuRoot[] {
  return groups.flatMap(group => {
    if (group.label) {
      return [{ id: group.id, title: group.label, icon: group.icon, items: group.items }]
    }

    return group.items.map(item =>
      item.subItems?.length
        ? { id: item.id, title: item.title, icon: item.icon, items: item.subItems }
        : { id: item.id, title: item.title, icon: item.icon, link: item, items: [] }
    )
  })
}

/** Whether a menu item, or one of its sub-items, is the page at `pathname`. */
export function isMenuItemActive(item: MenuItem, pathname: string): boolean {
  return toPathname(item.url) === pathname || Boolean(item.subItems?.some(sub => isMenuItemActive(sub, pathname)))
}

export function isMenuRootActive(root: MenuRoot, pathname: string) {
  return root.link ? isMenuItemActive(root.link, pathname) : root.items.some(item => isMenuItemActive(item, pathname))
}

/** The first page under a root or item, for "open the first child" navigation. */
export function firstMenuLeaf(items: readonly MenuItem[]): MenuItem | undefined {
  for (const item of items) {
    if (item.subItems?.length) {
      const leaf = firstMenuLeaf(item.subItems)
      if (leaf) return leaf
    } else {
      return item
    }
  }
  return undefined
}
