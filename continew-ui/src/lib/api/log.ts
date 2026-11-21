import { http, downloadFile } from '@/lib/http'
import type { PageResp } from '@/lib/api/types'

/** `LogStatusEnum`: 1 成功, 2 失败. */
export type LogStatus = 1 | 2

/**
 * One row of `GET /system/log`.
 *
 * Login rows carry the `登录` module and hold only the fields this list shows; operation
 * rows add the request duration. `createUserString` is the operator's nickname, joined
 * from `sys_user`, and is empty once that account is gone.
 */
export interface LogResp {
  id: string | number
  description: string
  module: string
  /** Milliseconds the request took. */
  timeTaken: number
  ip: string
  address: string
  browser: string
  os: string
  status: LogStatus
  /** The exception message of a failed operation. */
  errorMsg: string | null
  createUserString: string | null
  createTime: string
}

/** `GET /system/log/{id}` adds the captured HTTP exchange. */
export interface LogDetailResp extends LogResp {
  traceId: string
  requestUrl: string
  requestMethod: string
  requestHeaders: string | null
  requestBody: string | null
  statusCode: number | null
  responseHeaders: string | null
  responseBody: string | null
}

export type LogPageQuery = {
  page: number
  size: number
  /** `<field>,<asc|desc>`; the log query sorts on its `t1` table alias. */
  sort?: string
  /** Matched against both the description and the module. */
  description?: string
  /** Exact module name; the login tab sends `登录`. */
  module?: string
  /** Matched against both the IP and the address. */
  ip?: string
  /** Matched against the operator's username and nickname. */
  createUserString?: string
  /** `<start>,<end>`, both `yyyy-MM-dd HH:mm:ss`. */
  createTime?: string
  status?: LogStatus
}

/** The export endpoints take the same filters as the list, without the page. */
export type LogExportQuery = Omit<LogPageQuery, 'page' | 'size'>

/** One page of logs. */
export function listLogs(query: LogPageQuery, signal?: AbortSignal) {
  return http.get<PageResp<LogResp>>('/system/log', { query, signal })
}

/** One log's full record, including the request and response it captured. */
export function getLog(id: string | number, signal?: AbortSignal) {
  return http.get<LogDetailResp>(`/system/log/${id}`, { signal })
}

/** Every login log matching the filter, as a file download. */
export function exportLoginLogs(query: LogExportQuery) {
  return downloadFile('/system/log/export/login', query)
}

/** Every operation log matching the filter, as a file download. */
export function exportOperationLogs(query: LogExportQuery) {
  return downloadFile('/system/log/export/operation', query)
}

export { logKeys } from '@/lib/query-keys'
