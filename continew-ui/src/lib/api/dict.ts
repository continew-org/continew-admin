import { http } from '@/lib/http'

import type { PageResp } from './types'

export type DisEnableStatus = 1 | 2

export type DictQuery = {
  description?: string
}

export interface DictResp {
  id: string | number
  name: string
  code: string
  description: string | null
  isSystem: boolean
  createUserString?: string | null
  createTime?: string | null
  updateUserString?: string | null
  updateTime?: string | null
}

export interface DictReq {
  name: string
  code: string
  description?: string
}

export type DictItemQuery = {
  dictId?: string | number
  description?: string
  status?: DisEnableStatus
  sort?: string
  page: number
  size: number
}

export interface DictItemResp {
  id: string | number
  label: string
  value: string
  color: string | null
  status: DisEnableStatus
  sort: number
  description: string | null
  dictId: string | number
  createUserString?: string | null
  createTime?: string | null
  updateUserString?: string | null
  updateTime?: string | null
}

export interface DictItemReq {
  label: string
  value: string
  color?: string
  sort: number
  description?: string
  status: DisEnableStatus
  dictId: string | number
}

export function listDicts(query?: DictQuery, signal?: AbortSignal) {
  return http.get<DictResp[]>('/system/dict/list', { query, signal })
}

export function getDict(id: string | number, signal?: AbortSignal) {
  return http.get<DictResp>(`/system/dict/${id}`, { signal })
}

export function createDict(body: DictReq) {
  return http.post('/system/dict', body)
}

export function updateDict(id: string | number, body: DictReq) {
  return http.put(`/system/dict/${id}`, body)
}

export function deleteDict(id: string | number) {
  return http.del('/system/dict', { body: { ids: [id] } })
}

export function clearDictCache(code: string) {
  return http.del(`/system/dict/cache/${code}`)
}

export function listDictItems(query: DictItemQuery, signal?: AbortSignal) {
  return http.get<PageResp<DictItemResp>>('/system/dict/item', { query, signal })
}

export function getDictItem(id: string | number, signal?: AbortSignal) {
  return http.get<DictItemResp>(`/system/dict/item/${id}`, { signal })
}

export function createDictItem(body: DictItemReq) {
  return http.post('/system/dict/item', body)
}

export function updateDictItem(id: string | number, body: DictItemReq) {
  return http.put(`/system/dict/item/${id}`, body)
}

export function deleteDictItem(id: string | number) {
  return http.del('/system/dict/item', { body: { ids: [id] } })
}

export { dictKeys } from '@/lib/query-keys'
