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

/** `GET /system/file/upload/config/default`：默认存储生效的上传配置，阈值与分片大小单位均为字节。 */
export interface FileUploadConfigResp {
  storageId: number
  storageName: string
  storageCode: string
  /** `StorageTypeEnum`：1 本地存储，2 对象存储 */
  storageType: number
  multipartUploadThreshold: number
  multipartUploadPartSize: number
  /** 仅本地存储返回，前端不使用 */
  multipartTempDir?: string | null
}

/** `POST /system/multipart-upload/init` 请求体。fileMd5 必填，用于断点续传会话匹配。 */
export interface MultipartUploadCreateReq {
  fileName: string
  fileSize: number
  fileMd5: string
  contentType?: string
  parentPath?: string
  metaData?: Record<string, string>
}

/** 分片初始化结果；`uploadedPartNumbers` 非空表示命中同 MD5 会话，可跳过已传分片。 */
export interface MultipartUploadCreateResp {
  fileId: string | null
  uploadId: string
  bucket: string | null
  platform: string | null
  fileName: string | null
  originalFileName: string | null
  fileMd5: string | null
  fileSize: number | null
  extension: string | null
  contentType: string | null
  type: string | null
  parentPath: string | null
  path: string
  partSize: number | null
  uploadedPartNumbers: number[] | null
}

export interface MultipartUploadPartResp {
  partNumber: number
  partETag: string | null
  partSize: number
  success: boolean
  errorMessage: string | null
}

export function listFiles(query: FilePageQuery, signal?: AbortSignal) {
  return http.get<PageResp<FileResp>>('/system/file', { query, signal })
}

export function uploadFile(file: File, parentPath: string, signal?: AbortSignal) {
  const body = new FormData()
  body.append('file', file)
  body.append('parentPath', parentPath || '/')
  return http.post<FileUploadResp>('/system/file/upload', body, { signal })
}

export function getDefaultUploadConfig(signal?: AbortSignal) {
  return http.get<FileUploadConfigResp>('/system/file/upload/config/default', { signal })
}

export function initMultipartUpload(body: MultipartUploadCreateReq, signal?: AbortSignal) {
  return http.post<MultipartUploadCreateResp>('/system/multipart-upload/init', body, { signal })
}

/** `path` 传 init 返回的存储文件路径；分片 body 只含该分片的字节。 */
export function uploadMultipartPart(
  uploadId: string,
  partNumber: number,
  path: string,
  chunk: Blob,
  signal?: AbortSignal
) {
  const body = new FormData()
  body.append('file', chunk)
  return http.post<MultipartUploadPartResp>('/system/multipart-upload/part', body, {
    query: { uploadId, partNumber, path },
    signal
  })
}

export function completeMultipartUpload(uploadId: string, signal?: AbortSignal) {
  return http.get<unknown>(`/system/multipart-upload/complete/${encodeURIComponent(uploadId)}`, { signal })
}

/** 中止分片会话并清理已传分片；cancel 请求自身失败时静默，不影响原错误向上抛。 */
export function cancelMultipartUpload(uploadId: string) {
  return http.get<unknown>(`/system/multipart-upload/cancel/${encodeURIComponent(uploadId)}`, {
    silent: true
  })
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
