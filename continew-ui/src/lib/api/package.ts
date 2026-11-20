import { http } from '@/lib/http'
import type { PageResp } from '@/lib/api/types'

/** `DisEnableStatusEnum`: 1 启用, 2 禁用. */
export type PackageStatus = 1 | 2

/** One row of `GET /tenant/package`. */
export interface PackageResp {
  id: string | number
  name: string
  sort: number
  /** True when selecting a menu also selects its parent and child nodes. */
  menuCheckStrictly: boolean
  description: string | null
  status: PackageStatus
  createUserString: string | null
  createTime: string
  updateUserString: string | null
  updateTime: string | null
  disabled?: boolean | null
}

/** `GET /tenant/package/{id}` adds the menu ids the package grants. */
export interface PackageDetailResp extends PackageResp {
  menuIds: (string | number)[]
}

/** A package option from `GET /tenant/package/dict`. */
export interface PackageDictResp {
  label: string
  value: string | number
}

/**
 * A node of `GET /tenant/package/menu/tree`; the backend builds a simple Hutool tree.
 *
 * The starter maps the menu title onto the tree node through `@TreeField`'s default
 * `nameKey = "title"`, so the label lives in `title`, not `name`. Hutool serializes the
 * node primary key as `key`; the menu id is there, not in an `id` field.
 */
export interface PackageMenuNode {
  key: string | number
  parentId: string | number | null
  title: string
  children?: PackageMenuNode[]
}

export type PackagePageQuery = {
  page: number
  size: number
  /** `<field>,<asc|desc>`; the package table has no alias in its queries. */
  sort?: string
  /** Matched against both the name and the description. */
  description?: string
  status?: PackageStatus
}

/** The body of `POST /tenant/package` and `PUT /tenant/package/{id}`. */
export type PackageReq = {
  name: string
  sort?: number
  menuCheckStrictly?: boolean
  description?: string
  status?: PackageStatus
  /** The tenant cannot be created with an empty menu set. */
  menuIds: (string | number)[]
}

/** One page of packages. */
export function listPackages(query: PackagePageQuery, signal?: AbortSignal) {
  return http.get<PageResp<PackageResp>>('/tenant/package', { query, signal })
}

/** One package's record, including its menu ids. */
export function getPackage(id: string | number, signal?: AbortSignal) {
  return http.get<PackageDetailResp>(`/tenant/package/${id}`, { signal })
}

/** Creates a package, answering with its new id. */
export function createPackage(body: PackageReq) {
  return http.post<{ id: string | number }>('/tenant/package', body)
}

export function updatePackage(id: string | number, body: PackageReq) {
  return http.put<void>(`/tenant/package/${id}`, body)
}

/** Deletes one package; the backend rejects packages that still have tenants. */
export function deletePackage(id: string | number) {
  return http.del<void>(`/tenant/package/${id}`)
}

/** Every package as a label/value option. */
export function listPackageDict(signal?: AbortSignal) {
  return http.get<PackageDictResp[]>('/tenant/package/dict', { signal })
}

/** The menu tree a package may grant, already filtered for tenant use. */
export function getPackageMenuTree(signal?: AbortSignal) {
  return http.get<PackageMenuNode[]>('/tenant/package/menu/tree', { signal })
}

export { packageKeys } from '@/lib/query-keys'
