import { http } from '@/lib/http'
import type { PageResp } from '@/lib/api/types'

/**
 * One row of `GET /monitor/online`.
 *
 * A row is one login session rather than one account: the same user signed in on two
 * devices appears twice, and `sessionId` is what the kick-out endpoint takes. The
 * backend sorts the list by login time, newest first.
 */
export interface OnlineUserResp {
  /** The user's id, not the session's. */
  id: string | number
  sessionId: string
  username: string
  nickname: string
  clientType: string
  clientId: string
  ip: string
  address: string
  browser: string
  os: string
  loginTime: string
  lastRefreshTime: string
}

export type OnlineUserPageQuery = {
  page: number
  size: number
  /** Matched against both the username and the nickname. */
  nickname?: string
  /**
   * `<start>,<end>`, both `yyyy-MM-dd HH:mm:ss`. The backend splits the value on the comma
   * into its `List<LocalDateTime>` filter.
   */
  loginTime?: string
  // The backend also filters by `clientId`; the screen follows the Vue client and offers
  // only the two filters above.
}

/** One page of login sessions. */
export function listOnlineUsers(query: OnlineUserPageQuery, signal?: AbortSignal) {
  return http.get<PageResp<OnlineUserResp>>('/monitor/online', { query, signal })
}

/** Revokes one login session; the backend rejects kicking out the current one. */
export function kickoutOnlineUser(sessionId: string) {
  return http.del<void>(`/monitor/online/${encodeURIComponent(sessionId)}`)
}

export { onlineUserKeys } from '@/lib/query-keys'
