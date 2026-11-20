import { http, type Query } from '@/lib/http'
import type { PageResp } from '@/lib/api/types'

/**
 * `DataScopeEnum`:
 * 1: 全部数据权限 (ALL)
 * 2: 本部门及以下数据权限 (DEPT_AND_CHILD)
 * 3: 本部门数据权限 (DEPT)
 * 4: 仅本人数据权限 (SELF)
 * 5: 自定义数据权限 (CUSTOM)
 */
export type DataScope = 1 | 2 | 3 | 4 | 5

export const DATA_SCOPE_OPTIONS = [
  { value: 1 as const, label: '全部数据权限' },
  { value: 2 as const, label: '本部门及以下数据权限' },
  { value: 3 as const, label: '本部门数据权限' },
  { value: 4 as const, label: '仅本人数据权限' },
  { value: 5 as const, label: '自定义数据权限' }
] as const

export const DATA_SCOPE_MAP: Record<DataScope, string> = {
  1: '全部数据',
  2: '本部门及以下',
  3: '本部门',
  4: '仅本人',
  5: '自定义部门'
}

/** One row of `GET /system/role` or `GET /system/role/list`. */
export interface RoleResp {
  id: string | number
  name: string
  code: string
  dataScope: DataScope
  sort: number
  isSystem: boolean
  description: string | null
  createUserString?: string | null
  createTime?: string | null
  updateUserString?: string | null
  updateTime?: string | null
  disabled?: boolean
}

/** Full record of `GET /system/role/{id}`. */
export interface RoleDetailResp extends RoleResp {
  menuCheckStrictly: boolean
  deptCheckStrictly: boolean
  menuIds: Array<string | number>
  deptIds: Array<string | number>
}

/** Query parameters for `GET /system/role/list`. */
export interface RoleQuery {
  sort?: string
  description?: string
  excludeRoleCodes?: string[]
}

export type RolePageQuery = RoleQuery

/** Payload for `POST /system/role` and `PUT /system/role/{id}`. */
export interface RoleReq {
  name: string
  code: string
  sort: number
  description?: string | null
  dataScope: DataScope
  deptIds?: Array<string | number>
  deptCheckStrictly?: boolean
}

/** Tree node of `GET /system/role/permission/tree`. */
export interface RolePermissionResp {
  id: string | number
  title: string
  parentId: string | number | null
  /** 1: directory, 2: menu, 3: button */
  type: number | string
  permission: string | null
  children?: RolePermissionResp[]
}

/** Payload for `PUT /system/role/{id}/permission`. */
export interface RolePermissionUpdateReq {
  roleId: string | number
  menuIds: Array<string | number>
  menuCheckStrictly: boolean
}

/** One row of `GET /system/role/{id}/user`. */
export interface RoleUserResp {
  /** User-role relation record ID (used when unassigning) */
  id: string | number
  roleId: string | number
  userId: string | number
  username: string
  nickname: string
  gender?: number
  status?: number
  isSystem?: boolean
  description?: string | null
  deptId?: string | number | null
  deptName?: string | null
  roleIds?: Array<string | number>
  roleNames?: string[]
}

/** Query parameters for `GET /system/role/{id}/user`. */
export interface RoleUserPageQuery {
  page?: number
  size?: number
  sort?: string
  description?: string
}

/** A role option from `GET /system/role/dict`. */
export interface RoleDictResp {
  label: string
  value: string | number
}

/** Every role from `GET /system/role/list`. */
export function listRoles(query?: RoleQuery, signal?: AbortSignal) {
  return http.get<RoleResp[]>('/system/role/list', {
    query: query as Query,
    signal
  })
}

/** Role picker options without requiring the role management list permission. */
export function listRoleDict(signal?: AbortSignal) {
  return http.get<RoleDictResp[]>('/system/role/dict', { signal })
}

/** Fetches a single role with its full menuIds and deptIds. */
export function getRole(id: string | number, signal?: AbortSignal) {
  return http.get<RoleDetailResp>(`/system/role/${id}`, { signal })
}

export function createRole(body: RoleReq) {
  return http.post<{ id: string | number }>('/system/role', body)
}

export function updateRole(id: string | number, body: RoleReq) {
  return http.put<void>(`/system/role/${id}`, body)
}

/** Batch deletes roles. The backend accepts `{ ids: List<Long> }`. */
export function deleteRoles(ids: Array<string | number>) {
  return http.del<void>('/system/role', { body: { ids } })
}

export function deleteRole(id: string | number) {
  return deleteRoles([id])
}

/** Fetches the full functional permissions tree for authorization. */
export function listRolePermissionTree(signal?: AbortSignal) {
  return http.get<RolePermissionResp[]>('/system/role/permission/tree', { signal })
}

/** Updates functional menu permissions for a role. */
export function updateRolePermission(id: string | number, body: RolePermissionUpdateReq) {
  return http.put<void>(`/system/role/${id}/permission`, body)
}

/** Paginated list of users assigned to this role. */
export function listRoleUsers(id: string | number, query?: RoleUserPageQuery, signal?: AbortSignal) {
  return http.get<PageResp<RoleUserResp>>(`/system/role/${id}/user`, {
    query: query as Query,
    signal
  })
}

/** User IDs already assigned to this role. */
export function listRoleUserIds(id: string | number, signal?: AbortSignal) {
  return http.get<Array<string | number>>(`/system/role/${id}/user/id`, { signal })
}

/** Assigns users to this role in bulk. */
export function assignRoleToUsers(id: string | number, userIds: Array<string | number>) {
  return http.post<void>(`/system/role/${id}/user`, userIds)
}

/** Unassigns users from this role in bulk by user_role relation IDs. */
export function unassignRoleFromUsers(userRoleIds: Array<string | number>) {
  return http.del<void>('/system/role/user', { body: userRoleIds })
}

export { roleKeys } from '@/lib/query-keys'
