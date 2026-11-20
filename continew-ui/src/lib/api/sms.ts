import { http, type Query } from '@/lib/http'
import type { PageResp } from '@/lib/api/types'

export interface SmsConfigResp {
  id: string | number
  name: string
  supplier: string
  accessKey: string
  secretKey?: string
  signature?: string
  templateId: string
  weight?: number
  retryInterval?: number
  maxRetries?: number
  maximum?: number
  supplierConfig?: string
  isDefault: boolean
  status: 1 | 2
  createUserString?: string
  createTime?: string
  updateUserString?: string
  updateTime?: string
}

export interface SmsConfigQuery {
  name?: string
  supplier?: string
  accessKey?: string
  status?: number
  page?: number
  size?: number
  sort?: string[]
}

export interface SmsConfigReq {
  name: string
  supplier: string
  accessKey: string
  secretKey: string
  signature?: string
  templateId: string
  weight?: number
  retryInterval?: number
  maxRetries?: number
  maximum?: number
  supplierConfig?: string
  status?: 1 | 2
}

/** Queries SMS configurations page: `GET /system/sms/config` */
export function listSmsConfig(query?: SmsConfigQuery, signal?: AbortSignal) {
  return http.get<PageResp<SmsConfigResp>>('/system/sms/config', {
    query: query as Query,
    signal
  })
}

/** Fetches single SMS config detail: `GET /system/sms/config/{id}` */
export function getSmsConfig(id: string | number, signal?: AbortSignal) {
  return http.get<SmsConfigResp>(`/system/sms/config/${id}`, { signal })
}

/** Creates new SMS config: `POST /system/sms/config` */
export function createSmsConfig(body: SmsConfigReq) {
  return http.post<{ id: string | number }>('/system/sms/config', body)
}

/** Updates existing SMS config: `PUT /system/sms/config/{id}` */
export function updateSmsConfig(id: string | number, body: SmsConfigReq) {
  return http.put<void>(`/system/sms/config/${id}`, body)
}

/** Batch deletes SMS configs: `DELETE /system/sms/config` */
export function deleteSmsConfigs(ids: Array<string | number>) {
  return http.del<void>('/system/sms/config', { body: { ids } })
}

/** Sets an SMS config as default: `PUT /system/sms/config/{id}/default` */
export function setDefaultSmsConfig(id: string | number) {
  return http.put<void>(`/system/sms/config/${id}/default`)
}
