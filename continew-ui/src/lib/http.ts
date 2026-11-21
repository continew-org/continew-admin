import { toast } from '@/components/ui/toast'

import type { ApiResponse, LoginResp } from './api/types'
import {
  clearSession,
  getAccessToken,
  getAuthGeneration,
  getTenantId,
  setAccessToken,
  setTenantId
} from './auth-session'

const BASE_URL = (import.meta.env.VITE_API_PREFIX || import.meta.env.VITE_API_BASE_URL || '').replace(/\/+$/, '')
const REQUEST_TIMEOUT_MS = 30_000

const LOGIN_PATH = '/auth/login'
const REFRESH_PATH = '/auth/refresh'
const LOGOUT_PATH = '/auth/logout'
const TENANT_FREE_PATHS = new Set([LOGIN_PATH, REFRESH_PATH, LOGOUT_PATH])

const SESSION_EXPIRED_MESSAGE = '登录状态已失效，请重新登录'
const SESSION_CHANGED_MESSAGE = '登录状态已变更，请重试'
const NETWORK_ERROR_MESSAGE = '网络连接失败，请检查您的网络'

const STATUS_MESSAGE: Partial<Record<number, string>> = {
  400: '请求错误（400）',
  401: SESSION_EXPIRED_MESSAGE,
  403: '拒绝访问（403）',
  404: '请求出错（404）',
  408: '请求超时（408）',
  500: '服务器错误（500）',
  502: '网络错误（502）',
  503: '服务不可用（503）',
  504: '网络超时（504）'
}

export type Query = Record<string, string | number | boolean | null | undefined>
type ErrorDetails = { code?: string; status?: number }
type MessagePayload = { msg?: string; message?: string }
type RefreshState = { generation: number; promise: Promise<string> }
type SessionExpiredHandler = (message: string) => void
type ForbiddenHandler = () => void

export class ApiError extends Error {
  readonly code?: string
  readonly status?: number
  notified = false

  constructor(message: string, options: ErrorDetails = {}) {
    super(message)
    this.name = 'ApiError'
    this.code = options.code
    this.status = options.status
  }
}

export interface RequestOptions extends Omit<RequestInit, 'body' | 'method'> {
  body?: unknown
  query?: Query
  skipAuthRefresh?: boolean
  silent?: boolean
}

let sessionExpiredHandler: SessionExpiredHandler | null = null
let forbiddenHandler: ForbiddenHandler | null = null
let sessionExpiredNotified = false
let refreshState: RefreshState | null = null

const timeoutSignal = () => AbortSignal.timeout(REQUEST_TIMEOUT_MS)

export function setSessionExpiredHandler(handler: SessionExpiredHandler | null) {
  sessionExpiredHandler = handler
}

export function setForbiddenHandler(handler: ForbiddenHandler | null) {
  forbiddenHandler = handler
}

function messageOf(payload: MessagePayload | null | undefined, fallback: string) {
  return payload?.msg || payload?.message || fallback
}

function fail(message: string, options: ErrorDetails = {}, silent = false) {
  const error = new ApiError(message, options)

  if (!silent) {
    error.notified = true
    toast.add({ title: message, type: 'error' })
  }

  return error
}

export function reportError(error: unknown, fallback = '操作失败，请重试') {
  if (error instanceof ApiError && error.notified) {
    return
  }

  toast.add({ title: error instanceof Error ? error.message : fallback, type: 'error' })
}

function sessionChangedError() {
  return new ApiError(SESSION_CHANGED_MESSAGE, { code: 'SESSION_CHANGED', status: 401 })
}

function assertGeneration(generation: number) {
  if (generation !== getAuthGeneration()) {
    throw sessionChangedError()
  }
}

function buildUrl(path: string, query?: Query) {
  const url = `${BASE_URL}${path}`

  if (!query) {
    return url
  }

  const search = new URLSearchParams()

  for (const [key, value] of Object.entries(query)) {
    if (value !== null && value !== undefined) {
      search.set(key, String(value))
    }
  }

  const queryString = search.toString()
  return queryString ? `${url}?${queryString}` : url
}

function buildHeaders(path: string, body?: unknown, extra?: HeadersInit) {
  const headers = new Headers(extra)

  if (body !== undefined && !(body instanceof FormData) && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }

  const accessToken = getAccessToken()
  if (accessToken) {
    headers.set('Authorization', `Bearer ${accessToken}`)
  }

  const tenantId = getTenantId()
  if (tenantId && !TENANT_FREE_PATHS.has(path)) {
    headers.set('X-Tenant-Id', tenantId)
  }

  return headers
}

async function readPayload<T>(response: Response) {
  return (await response.json().catch(() => null)) as ApiResponse<T> | null
}

function toRequestBody(body: unknown): BodyInit | undefined {
  if (body === undefined) {
    return undefined
  }

  return body instanceof FormData ? body : JSON.stringify(body)
}

async function performRefresh(generation: number): Promise<string> {
  const response = await fetch(buildUrl(REFRESH_PATH), {
    method: 'POST',
    credentials: 'include',
    headers: buildHeaders(REFRESH_PATH),
    signal: timeoutSignal()
  })
  const payload = await readPayload<LoginResp>(response)
  const accessToken = payload?.data?.accessToken

  if (!response.ok || !payload?.success || !accessToken) {
    throw new ApiError(messageOf(payload, SESSION_EXPIRED_MESSAGE), { status: response.status })
  }

  assertGeneration(generation)
  setAccessToken(accessToken)
  setTenantId(payload.data.tenantId ?? null)
  return accessToken
}

function refreshAccessToken(generation: number): Promise<string> {
  if (generation !== getAuthGeneration()) {
    return Promise.reject(sessionChangedError())
  }

  if (refreshState?.generation === generation) {
    return refreshState.promise
  }

  const promise = performRefresh(generation).finally(() => {
    if (refreshState?.promise === promise) {
      refreshState = null
    }
  })
  refreshState = { generation, promise }
  return promise
}

function handleSessionExpired(generation: number, message: string) {
  if (generation !== getAuthGeneration()) {
    throw sessionChangedError()
  }

  if (sessionExpiredNotified) {
    return
  }

  sessionExpiredNotified = true
  clearSession()

  void fetch(buildUrl(LOGOUT_PATH), {
    method: 'POST',
    credentials: 'include',
    headers: buildHeaders(LOGOUT_PATH),
    signal: timeoutSignal(),
    keepalive: true
  }).catch(() => undefined)

  sessionExpiredHandler?.(message)
}

async function renewSession(generation: number) {
  assertGeneration(generation)

  try {
    await refreshAccessToken(generation)
  } catch (error) {
    assertGeneration(generation)

    const message = error instanceof ApiError ? error.message : SESSION_EXPIRED_MESSAGE
    handleSessionExpired(generation, message)
    throw error
  }

  assertGeneration(generation)
}

async function throwSessionExpired(response: Response, generation: number, silent = false): Promise<never> {
  assertGeneration(generation)

  const payload = await readPayload<unknown>(response)
  const message = messageOf(payload, SESSION_EXPIRED_MESSAGE)

  handleSessionExpired(generation, message)
  throw fail(message, { status: 401 }, silent)
}

async function send<T>(
  method: string,
  path: string,
  options: RequestOptions = {},
  isRetry = false,
  generation = getAuthGeneration()
): Promise<T> {
  assertGeneration(generation)

  const { body, query, skipAuthRefresh, silent, headers, ...init } = options
  const timeout = timeoutSignal()
  // A caller-provided signal cancels the request too, but the request timeout still applies.
  const signal = init.signal ? AbortSignal.any([init.signal, timeout]) : timeout

  let response: Response
  try {
    response = await fetch(buildUrl(path, query), {
      ...init,
      method,
      credentials: 'include',
      headers: buildHeaders(path, body, headers),
      body: toRequestBody(body),
      signal
    })
  } catch (error) {
    // React Query aborts the previous request when its key changes or the view unmounts. That is
    // not a network failure, so it must not surface an error toast.
    if (init.signal?.aborted) {
      throw error
    }

    throw fail(NETWORK_ERROR_MESSAGE, {}, silent)
  }

  if (response.status === 401) {
    if (skipAuthRefresh || path === REFRESH_PATH || isRetry || !getAccessToken()) {
      return throwSessionExpired(response, generation, silent)
    }

    await renewSession(generation)
    return send<T>(method, path, options, true, generation)
  }

  if (response.status === 403) {
    forbiddenHandler?.()
  }

  const payload = await readPayload<T>(response)
  const message = messageOf(payload, STATUS_MESSAGE[response.status] ?? `请求失败（${response.status}）`)

  if (!payload) {
    throw fail(message, { status: response.status }, silent)
  }

  if (!payload.success) {
    throw fail(message, { code: payload.code, status: response.status }, silent)
  }

  if (path === LOGIN_PATH) {
    sessionExpiredNotified = false
  }

  return payload.data
}

function readDownloadFilename(header: string | null) {
  if (!header) {
    return null
  }

  const raw = /filename\*=UTF-8''([^;]+)/i.exec(header)?.[1] ?? /filename="?([^";]+)"?/i.exec(header)?.[1]

  if (!raw) {
    return null
  }

  try {
    return decodeURIComponent(raw)
  } catch {
    return raw
  }
}

async function fetchDownload(path: string, query?: Query) {
  try {
    return await fetch(buildUrl(path, query), {
      credentials: 'include',
      headers: buildHeaders(path),
      signal: timeoutSignal()
    })
  } catch {
    throw fail(NETWORK_ERROR_MESSAGE)
  }
}

export async function downloadFile(path: string, query?: Query) {
  const generation = getAuthGeneration()
  let response = await fetchDownload(path, query)

  if (response.status === 401) {
    await renewSession(generation)
    response = await fetchDownload(path, query)

    if (response.status === 401) {
      return throwSessionExpired(response, generation)
    }
  }

  if (response.status === 403) {
    forbiddenHandler?.()
  }

  if (!response.ok) {
    throw fail(STATUS_MESSAGE[response.status] ?? `下载失败（${response.status}）`, { status: response.status })
  }

  if (response.headers.get('Content-Type')?.includes('application/json')) {
    const payload = await readPayload<unknown>(response)
    throw fail(messageOf(payload, '导出失败'), { code: payload?.code, status: response.status })
  }

  const url = URL.createObjectURL(await response.blob())
  const anchor = document.createElement('a')
  anchor.href = url
  anchor.download = readDownloadFilename(response.headers.get('Content-Disposition')) ?? 'download'
  document.body.append(anchor)
  anchor.click()
  anchor.remove()
  setTimeout(() => URL.revokeObjectURL(url), 0)
}

export const http = {
  get: <T>(path: string, options?: RequestOptions) => send<T>('GET', path, options),
  post: <T>(path: string, body?: unknown, options?: RequestOptions) => send<T>('POST', path, { ...options, body }),
  put: <T>(path: string, body?: unknown, options?: RequestOptions) => send<T>('PUT', path, { ...options, body }),
  patch: <T>(path: string, body?: unknown, options?: RequestOptions) => send<T>('PATCH', path, { ...options, body }),
  del: <T>(path: string, options?: RequestOptions) => send<T>('DELETE', path, options)
}
