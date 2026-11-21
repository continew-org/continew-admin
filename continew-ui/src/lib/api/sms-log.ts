import { http, downloadFile } from '@/lib/http'
import type { PageResp } from '@/lib/api/types'

/** `SuccessFailureStatusEnum`: 1 成功, 2 失败. */
export type SmsLogStatus = 1 | 2

/**
 * One row of `GET /system/sms/log`.
 *
 * `params` holds the template parameters the message was rendered with and `resMsg` the SMS
 * provider's response, so both can be long JSON strings. `disabled` marks the rows the
 * backend protects from deletion.
 */
export interface SmsLogResp {
  id: string | number
  configId: string | number
  phone: string
  params: string | null
  status: SmsLogStatus
  resMsg: string | null
  /** The operator's nickname; the backend joins it from `sys_user`. */
  createUserString: string | null
  createTime: string
  /** Present only when the backend protects the row from deletion. */
  disabled?: boolean | null
}

export type SmsLogPageQuery = {
  page: number
  size: number
  /** `<field>,<asc|desc>`; the log table has no alias in its queries. */
  sort?: string
  configId?: string | number
  /** Exact match; the backend compares with `=`. */
  phone?: string
  status?: SmsLogStatus
}

/** The export endpoint takes the same filters as the list, without the page. */
export type SmsLogQuery = Omit<SmsLogPageQuery, 'page' | 'size'>

/** One page of SMS logs. */
export function listSmsLogs(query: SmsLogPageQuery, signal?: AbortSignal) {
  return http.get<PageResp<SmsLogResp>>('/system/sms/log', { query, signal })
}

/**
 * Deletes one log.
 *
 * The backend only exposes the batch endpoint (`Api.BATCH_DELETE`), which takes `{ ids }`.
 */
export function deleteSmsLog(id: string | number) {
  return http.del<void>('/system/sms/log', { body: { ids: [id] } })
}

/** Every SMS log matching the filter, as a file download. */
export function exportSmsLogs(query: SmsLogQuery) {
  return downloadFile('/system/sms/log/export', query)
}

export { smsLogKeys } from '@/lib/query-keys'
