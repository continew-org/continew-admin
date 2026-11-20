import { http, downloadFile } from '@/lib/http'
import type { PageResp } from './types'

/** `DisEnableStatusEnum`: 1 启用, 2 禁用. */
export type AppStatus = 1 | 2

/**
 * One row of `GET /open/app`.
 */
export interface AppResp {
  id: string | number
  name: string
  accessKey: string
  expireTime: string | null
  status: AppStatus
  description: string | null
  createUserString: string | null
  createTime: string
  updateUserString: string | null
  updateTime: string | null
  disabled: boolean | null
}

export type AppPageQuery = {
  page: number
  size: number
  /** Keyword matched against name and description. */
  description?: string
  status?: AppStatus
  sort?: string
}

/** One page of apps. */
export function listApps(query: AppPageQuery, signal?: AbortSignal) {
  return http.get<PageResp<AppResp>>('/open/app', { query, signal })
}

/** Every app matching the filter, as a file download. */
export function exportApps(query: Omit<AppPageQuery, 'page' | 'size'>) {
  return downloadFile('/open/app/export', query)
}

/** One app's full record. */
export type AppDetailResp = AppResp

export function getApp(id: string | number, signal?: AbortSignal) {
  return http.get<AppDetailResp>(`/open/app/${id}`, { signal })
}

/**
 * The body of `POST /open/app` and `PUT /open/app/{id}`.
 */
export type AppReq = {
  name: string
  expireTime?: string | null
  status: AppStatus
  description?: string
}

export function updateApp(id: string | number, body: AppReq) {
  return http.put<void>(`/open/app/${id}`, body)
}

/** Creates an app, answering with its new id. */
export function createApp(body: AppReq) {
  return http.post<{ id: string | number }>('/open/app', body)
}

/**
 * Deletes one app.
 */
export function deleteApp(id: string | number) {
  return http.del<void>('/open/app', { body: { ids: [id] } })
}

/**
 * Batch deletes multiple apps.
 */
export function deleteApps(ids: (string | number)[]) {
  return http.del<void>('/open/app', { body: { ids } })
}

/**
 * `GET /open/app/{id}/secret` response.
 */
export interface AppSecretResp {
  accessKey: string
  secretKey: string
}

/** Fetches one app's secret keys. */
export function getAppSecret(id: string | number, signal?: AbortSignal) {
  return http.get<AppSecretResp>(`/open/app/${id}/secret`, { signal })
}

/** Resets one app's secret key. */
export function resetAppSecret(id: string | number) {
  return http.patch<void>(`/open/app/${id}/secret`)
}

export { appKeys } from '@/lib/query-keys'
