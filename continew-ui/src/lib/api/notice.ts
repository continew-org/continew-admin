import { http } from '@/lib/http'

import type { PageResp } from './types'

export type NoticeScope = 1 | 2
export type NoticeStatus = 1 | 2 | 3

export interface NoticeResp {
  id: string | number
  title: string
  type: string
  noticeScope: NoticeScope
  noticeMethods: number[] | null
  isTiming: boolean
  publishTime: string | null
  isTop: boolean
  status: NoticeStatus
  isRead: boolean
  createUserString?: string | null
  createTime?: string | null
}

export interface NoticeDetailResp extends NoticeResp {
  content: string
  noticeUsers: string[] | null
  updateUserString?: string | null
  updateTime?: string | null
}

export type NoticePageQuery = {
  page: number
  size: number
  title?: string
  type?: string
  sort?: string
}

export interface NoticeReq {
  title: string
  content: string
  type: string
  noticeScope: NoticeScope
  noticeUsers?: string[] | null
  noticeMethods?: number[] | null
  isTiming: boolean
  publishTime?: string | null
  isTop: boolean
  status: NoticeStatus
}

export function listNotices(query: NoticePageQuery, signal?: AbortSignal) {
  return http.get<PageResp<NoticeResp>>('/system/notice', { query, signal })
}

export function getNotice(id: string | number, signal?: AbortSignal) {
  return http.get<NoticeDetailResp>(`/system/notice/${id}`, { signal })
}

export function createNotice(body: NoticeReq) {
  return http.post('/system/notice', body)
}

export function updateNotice(id: string | number, body: NoticeReq) {
  return http.put(`/system/notice/${id}`, body)
}

export function deleteNotice(id: string | number) {
  return http.del('/system/notice', { body: { ids: [id] } })
}

export { noticeKeys } from '@/lib/query-keys'
