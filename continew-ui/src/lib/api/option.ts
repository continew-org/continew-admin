import { http, type Query } from '@/lib/http'

export interface OptionResp {
  id: number
  name: string
  code: string
  value: string
  defaultValue?: string
  description?: string
}

export interface OptionQuery {
  category?: 'SITE' | 'PASSWORD' | 'MAIL' | 'LOGIN' | string
  code?: string[]
}

export interface OptionReq {
  id: number
  code: string
  value: string
}

export interface OptionValueResetReq {
  category: string
  code?: string[]
}

/** Queries options list: `GET /system/option` */
export function listOption(query: OptionQuery, signal?: AbortSignal) {
  return http.get<OptionResp[]>('/system/option', {
    query: query as Query,
    signal
  })
}

/** Updates options in batch: `PUT /system/option` */
export function updateOption(body: OptionReq[]) {
  return http.put<void>('/system/option', body)
}

/** Resets options to default values: `PATCH /system/option/value` */
export function resetOptionValue(body: OptionValueResetReq) {
  return http.patch<void>('/system/option/value', body)
}
