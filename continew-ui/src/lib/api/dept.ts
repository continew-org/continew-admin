import { downloadFile, http, type Query } from '@/lib/http'

/** Response model for a department node in `GET /system/dept/tree` or `GET /system/dept/{id}`. */
export interface DeptResp {
  id: string | number
  parentId: string | number | null
  name: string
  status: 1 | 2
  sort: number
  isSystem: boolean
  description: string | null
  createUserString?: string | null
  createTime?: string | null
  updateUserString?: string | null
  updateTime?: string | null
  children?: DeptResp[]
}

/** One flattened department, tagged with its depth for indented display. */
export interface DeptOption {
  id: string | number
  name: string
  depth: number
}

/** Query parameters for `GET /system/dept/tree`. */
export interface DeptQuery {
  description?: string
  status?: number
}

/** Request payload for creating or updating a department. */
export interface DeptReq {
  parentId: string | number
  name: string
  sort?: number
  status?: 1 | 2
  description?: string | null
}

/** Fetches the whole department tree from `GET /system/dept/tree`. */
export function getDeptTree(query?: DeptQuery, signal?: AbortSignal) {
  return http.get<DeptResp[]>('/system/dept/tree', { query: query as Query, signal })
}

export function getDept(id: string | number, signal?: AbortSignal) {
  return http.get<DeptResp>(`/system/dept/${id}`, { signal })
}

export function createDept(body: DeptReq) {
  return http.post<{ id: string | number }>('/system/dept', body)
}

export function updateDept(id: string | number, body: DeptReq) {
  return http.put<void>(`/system/dept/${id}`, body)
}

/** Batch deletes departments. Accepts `{ ids }`. */
export function deleteDepts(ids: Array<string | number>) {
  return http.del<void>('/system/dept', { body: { ids } })
}

export function deleteDept(id: string | number) {
  return deleteDepts([id])
}

/** Exports department data to Excel. */
export function exportDept(query?: DeptQuery) {
  return downloadFile('/system/dept/export', query as Query)
}

/** Flattens the tree in display order, parents before their children. */
export function flattenDepts(nodes: DeptResp[], depth = 0): DeptOption[] {
  return nodes.flatMap(node => [
    { id: node.id, name: node.name, depth },
    ...flattenDepts(node.children ?? [], depth + 1)
  ])
}

export { deptKeys } from '@/lib/query-keys'
