import { http, type Query } from '@/lib/http'

export type MenuType = 1 | 2 | 3

export const MENU_TYPE_MAP: Record<MenuType, string> = {
  1: '目录',
  2: '菜单',
  3: '按钮'
}

/** One row/node in `GET /system/menu/tree`. */
export interface MenuResp {
  id: string | number
  parentId: string | number | null
  title: string
  type: MenuType
  path: string | null
  name: string | null
  component: string | null
  redirect: string | null
  icon: string | null
  isExternal: boolean
  isCache: boolean
  isHidden: boolean
  permission: string | null
  sort: number
  status: 1 | 2
  createUserString?: string | null
  createTime?: string | null
  updateUserString?: string | null
  updateTime?: string | null
  children?: MenuResp[]
}

/** Request payload for creating or updating a menu. */
export interface MenuReq {
  type: MenuType
  title: string
  parentId?: string | number | null
  icon?: string | null
  sort: number
  status: 1 | 2
  permission?: string | null
  path?: string | null
  name?: string | null
  component?: string | null
  redirect?: string | null
  isExternal?: boolean
  isCache?: boolean
  isHidden?: boolean
}

/** Query parameters for `GET /system/menu/tree`. */
export interface MenuQuery {
  title?: string
  status?: number
}

/** Fetches the whole menu tree from `GET /system/menu/tree`. */
export function getMenuTree(query?: MenuQuery, signal?: AbortSignal) {
  return http.get<MenuResp[]>('/system/menu/tree', {
    query: query as Query,
    signal
  })
}

export function getMenu(id: string | number, signal?: AbortSignal) {
  return http.get<MenuResp>(`/system/menu/${id}`, { signal })
}

export function createMenu(body: MenuReq) {
  return http.post<{ id: string | number }>('/system/menu', body)
}

export function updateMenu(id: string | number, body: MenuReq) {
  return http.put<void>(`/system/menu/${id}`, body)
}

/** Batch deletes menus. Accepts `{ ids }`. */
export function deleteMenus(ids: Array<string | number>) {
  return http.del<void>('/system/menu', { body: { ids } })
}

export function deleteMenu(id: string | number) {
  return deleteMenus([id])
}

/** Clears Redis menu cache (`DELETE /system/menu/cache`). */
export function clearMenuCache() {
  return http.del<void>('/system/menu/cache')
}

export { menuKeys } from '@/lib/query-keys'
