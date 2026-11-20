import { http, type Query } from '@/lib/http'

/** `StorageTypeEnum`: 1 本地存储, 2 对象存储. */
export type StorageType = 1 | 2

/** `DisEnableStatusEnum`: 1 启用, 2 禁用. */
export type StorageStatus = 1 | 2

export interface StorageResp {
  id: string | number
  name: string
  code: string
  status: StorageStatus
  type: StorageType
  accessKey?: string | null
  endpoint?: string | null
  /** Bucket/存储路径 */
  bucketName: string
  /** 域名/访问路径 */
  domain?: string | null
  /** 分片上传阈值（字节） */
  multipartUploadThreshold?: number | null
  /** 分片上传大小（字节） */
  multipartUploadPartSize?: number | null
  /** 本地分片临时目录 */
  multipartTempDir?: string | null
  recycleBinEnabled?: boolean | null
  recycleBinPath?: string | null
  description?: string | null
  isDefault: boolean
  sort?: number | null
  /** The backend mirrors `isDefault` here: the default storage may be neither disabled nor deleted. */
  disabled?: boolean
  createUserString?: string
  createTime?: string
  updateUserString?: string
  updateTime?: string
}

export interface StorageQuery {
  /** Keyword; the backend matches it against name, code and description. */
  description?: string
  status?: StorageStatus
  type?: StorageType
  /** `<field>,<asc|desc>` */
  sort?: string[]
}

export interface StorageReq {
  name: string
  code: string
  type: StorageType
  accessKey?: string | null
  /**
   * Encrypted with the backend RSA public key.
   *
   * Omitted on update, which leaves the stored value untouched, and unused by a local storage.
   */
  secretKey?: string | null
  endpoint?: string | null
  bucketName: string
  domain?: string | null
  multipartUploadThreshold?: number | null
  multipartUploadPartSize?: number | null
  multipartTempDir?: string | null
  recycleBinEnabled: boolean
  recycleBinPath?: string | null
  sort?: number
  description?: string
  status?: StorageStatus
}

/** Queries every storage: `GET /system/storage/list` (unpaged, `Api.LIST`). */
export function listStorage(query?: StorageQuery, signal?: AbortSignal) {
  return http.get<StorageResp[]>('/system/storage/list', {
    query: query as Query,
    signal
  })
}

/** Fetches single storage detail: `GET /system/storage/{id}` */
export function getStorage(id: string | number, signal?: AbortSignal) {
  return http.get<StorageResp>(`/system/storage/${id}`, { signal })
}

/** Creates new storage: `POST /system/storage` */
export function createStorage(body: StorageReq) {
  return http.post<{ id: string | number }>('/system/storage', body)
}

/** Updates storage: `PUT /system/storage/${id}` */
export function updateStorage(id: string | number, body: StorageReq) {
  return http.put<void>(`/system/storage/${id}`, body)
}

/** Batch deletes storages: `DELETE /system/storage` */
export function deleteStorages(ids: Array<string | number>) {
  return http.del<void>('/system/storage', { body: { ids } })
}

/** Sets storage as default: `PUT /system/storage/{id}/default` */
export function setDefaultStorage(id: string | number) {
  return http.put<void>(`/system/storage/${id}/default`)
}

/** Updates storage status: `PUT /system/storage/{id}/status` */
export function updateStorageStatus(id: string | number, status: StorageStatus) {
  return http.put<void>(`/system/storage/${id}/status`, { status })
}
