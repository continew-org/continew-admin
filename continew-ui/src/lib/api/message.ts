import { http } from '@/lib/http'

import type { NoticeDetailResp, NoticeResp } from './notice'
import type { PageResp } from './types'

/** The backend `MessageTypeEnum`: 1 系统消息, 2 安全消息. */
export type MessageType = 1 | 2

export interface MessageResp {
  id: string | number
  title: string
  type: MessageType
  /** In-app path the message points at, if any. */
  path: string | null
  isRead: boolean
  readTime: string | null
  createTime: string
}

export interface MessageDetailResp extends Omit<MessageResp, 'readTime'> {
  content: string
}

export interface MessageUnreadResp {
  total: number
}

export type MessagePageQuery = {
  page: number
  size: number
  title?: string
  type?: MessageType
  isRead?: boolean
  sort?: string
}

export type UserNoticePageQuery = {
  page: number
  size: number
  title?: string
  type?: string
  sort?: string
}

/** The backend `NoticeMethodEnum` names accepted by `/notice/unread/{method}`. */
export type NoticeMethod = 'SYSTEM_MESSAGE' | 'POPUP'

/** Background badge counts: failures stay quiet instead of toasting on every refresh. */
export function countUnreadMessages(signal?: AbortSignal) {
  return http.get<MessageUnreadResp>('/user/message/unread', { signal, silent: true })
}

export function listMessages(query: MessagePageQuery, signal?: AbortSignal) {
  return http.get<PageResp<MessageResp>>('/user/message', { query, signal })
}

/** Reading a message also marks it read on the backend. */
export function getMessage(id: string | number, signal?: AbortSignal) {
  return http.get<MessageDetailResp>(`/user/message/${id}`, { signal })
}

export function readMessages(ids: (string | number)[]) {
  return http.patch<void>('/user/message/read', { ids })
}

export function readAllMessages() {
  return http.patch<void>('/user/message/readAll')
}

export function deleteMessages(ids: (string | number)[]) {
  return http.del<void>('/user/message', { body: { ids } })
}

export function countUnreadNotices(signal?: AbortSignal) {
  return http.get<{ total: number }>('/user/message/notice/unread', { signal, silent: true })
}

export function listUnreadNoticeIds(method: NoticeMethod, signal?: AbortSignal) {
  return http.get<(string | number)[]>(`/user/message/notice/unread/${method}`, { signal })
}

/** Notices visible to the current user, each with its own `isRead`. */
export function listUserNotices(query: UserNoticePageQuery, signal?: AbortSignal) {
  return http.get<PageResp<NoticeResp>>('/user/message/notice', { query, signal })
}

/** Reading a notice also marks it read on the backend. */
export function getUserNotice(id: string | number, signal?: AbortSignal) {
  return http.get<NoticeDetailResp>(`/user/message/notice/${id}`, { signal })
}

export { messageKeys } from '@/lib/query-keys'
