import { http } from '@/lib/http'

import type { PageResp } from './types'

export interface DemoResp {
  id: string | number
  name: string
  quantity: number
  onlineTime: string | null
  createUserString?: string | null
  createTime?: string | null
}

export interface DemoDetailResp extends DemoResp {
  extConfig: Record<string, unknown> | null
  updateUserString?: string | null
  updateTime?: string | null
}

export type DemoPageQuery = {
  page: number
  size: number
  name?: string
  sort?: string
}

export interface DemoReq {
  name: string
  quantity: number
  onlineTime?: string | null
  extConfig?: Record<string, unknown> | null
}

export function listDemos(query: DemoPageQuery, signal?: AbortSignal) {
  return http.get<PageResp<DemoResp>>('/system/demo', { query, signal })
}

export function getDemo(id: string | number, signal?: AbortSignal) {
  return http.get<DemoDetailResp>(`/system/demo/${id}`, { signal })
}

export function createDemo(body: DemoReq) {
  return http.post('/system/demo', body)
}

export function updateDemo(id: string | number, body: DemoReq) {
  return http.put(`/system/demo/${id}`, body)
}

export function deleteDemo(id: string | number) {
  return http.del('/system/demo', { body: { ids: [id] } })
}

export { demoKeys } from '@/lib/query-keys'
