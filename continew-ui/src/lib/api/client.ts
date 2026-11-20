import { http, type Query } from '@/lib/http'
import type { PageResp } from '@/lib/api/types'

export interface ClientResp {
  id: string | number
  clientId: string
  clientKey?: string
  clientType: string
  authType: string[]
  activeTimeout: number
  timeout: number
  refreshTokenTimeout?: number
  refreshTokenMode?: string
  isConcurrent: boolean
  replacedRange?: string
  maxLoginCount?: number
  overflowLogoutMode?: string
  status: 1 | 2
  createUserString?: string
  createTime?: string
  updateUserString?: string
  updateTime?: string
  disabled?: boolean
}

export interface ClientQuery {
  clientType?: string
  status?: number | string
  page?: number
  size?: number
  sort?: string[]
}

export interface ClientReq {
  clientId?: string
  clientKey?: string
  clientType: string
  authType: string[]
  activeTimeout: number
  timeout: number
  refreshTokenTimeout?: number
  refreshTokenMode?: string
  isConcurrent: boolean
  replacedRange?: string
  maxLoginCount?: number
  overflowLogoutMode?: string
  status?: 1 | 2
}

/** Queries clients page: `GET /system/client` */
export function listClient(query?: ClientQuery, signal?: AbortSignal) {
  return http.get<PageResp<ClientResp>>('/system/client', {
    query: query as Query,
    signal
  })
}

/** Fetches single client detail: `GET /system/client/{id}` */
export function getClient(id: string | number, signal?: AbortSignal) {
  return http.get<ClientResp>(`/system/client/${id}`, { signal })
}

/** Creates new client: `POST /system/client` */
export function createClient(body: ClientReq) {
  return http.post<{ id: string | number }>('/system/client', body)
}

/** Updates client: `PUT /system/client/${id}` */
export function updateClient(id: string | number, body: ClientReq) {
  return http.put<void>(`/system/client/${id}`, body)
}

/** Batch deletes clients: `DELETE /system/client` */
export function deleteClients(ids: Array<string | number>) {
  return http.del<void>('/system/client', { body: { ids } })
}
