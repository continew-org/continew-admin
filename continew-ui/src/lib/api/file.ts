import { http } from '@/lib/http'

import type { PageResp } from './types'

/** 0 为目录，其余与后端 `FileTypeEnum` 一致。 */
export type FileType = 0 | 1 | 2 | 3 | 4 | 5

/** 左侧按类型筛选时不包含“目录”。 */
export type FileFilterType = Exclude<FileType, 0>

export const FILE_TYPE_LABELS: Record<FileType, string> = {
  0: '文件夹',
  1: '其他',
  2: '图片',
  3: '文档',
  4: '视频',
  5: '音频'
}

export interface FileResp {
  id: string | number
  name: string
  originalName: string
  size: number
  url: string
  parentPath: string
  path: string
  extension: string | null
  contentType: string | null
  type: FileType
  sha256: string | null
  metadata: string | null
  thumbnailName: string | null
  thumbnailSize: number | null
  thumbnailMetadata: string | null
  thumbnailUrl: string | null
  storageId: number
  storageName: string
  createTime: string
  updateTime: string
}

export type FilePageQuery = {
  originalName?: string
  parentPath?: string
  type?: FileFilterType
  sort?: string
  page: number
  size: number
}

export interface FileReq {
  originalName: string
  parentPath?: string | null
}

export interface FileUploadResp {
  id: string
  url: string
  thUrl: string | null
  metadata: Record<string, string> | null
}

export interface FileStatisticsResp {
  type?: FileType | null
  size?: number
  number?: number
  data?: FileStatisticsResp[] | null
}

export function listFiles(query: FilePageQuery, signal?: AbortSignal) {
  return http.get<PageResp<FileResp>>('/system/file', { query, signal })
}

export function uploadFile(file: File, parentPath: string) {
  const body = new FormData()
  body.append('file', file)
  body.append('parentPath', parentPath || '/')
  return http.post<FileUploadResp>('/system/file/upload', body)
}

export function updateFile(id: string | number, body: FileReq) {
  return http.put(`/system/file/${id}`, body)
}

/** 后端批量删除接口，一次删除一个文件时也传数组。 */
export function deleteFiles(ids: (string | number)[]) {
  return http.del('/system/file', { body: { ids } })
}

export function createDir(parentPath: string, originalName: string) {
  return http.post<{ id: string | number }>('/system/file/dir', { parentPath, originalName })
}

export function getFileStatistics(signal?: AbortSignal) {
  return http.get<FileStatisticsResp>('/system/file/statistics', { signal })
}

export function listRecycleFiles(query: FilePageQuery, signal?: AbortSignal) {
  return http.get<PageResp<FileResp>>('/system/file/recycle', { query, signal })
}

export function restoreRecycleFile(id: string | number) {
  return http.put(`/system/file/recycle/restore/${id}`)
}

export function deleteRecycleFile(id: string | number) {
  return http.del(`/system/file/recycle/${id}`)
}

export function cleanRecycleBin() {
  return http.del('/system/file/recycle/clean')
}

export { fileKeys } from '@/lib/query-keys'
