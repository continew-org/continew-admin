import { http, downloadFile } from '@/lib/http'

/** `DisEnableStatusEnum`: 1 启用, 2 禁用. */
export type UserStatus = 1 | 2

/** `GenderEnum`: 0 未知, 1 男, 2 女. */
export type UserGender = 0 | 1 | 2

/**
 * One row of `GET /system/user`.
 *
 * The backend registers `BaseEnumSerializer`, so `status` and `gender` arrive as the
 * enum's numeric `value` rather than its name. `big-number-serialize-mode: FLEXIBLE`
 * turns ids above the JS safe integer range into strings, and `email`/`phone` are masked
 * before they leave the server.
 */
export interface UserResp {
  id: string | number
  username: string
  nickname: string
  gender: UserGender
  avatar: string | null
  email: string | null
  phone: string | null
  status: UserStatus
  isSystem: boolean
  description: string | null
  deptId: string | number | null
  deptName: string | null
  roleIds: (string | number)[]
  roleNames: string[]
  createUserString: string | null
  createTime: string
  updateUserString: string | null
  updateTime: string | null
  /** True for built-in users and for the signed-in user; the backend rejects edits. */
  disabled: boolean | null
}

/** The backend's `PageResp`: the current page slice plus the total row count. */
export interface PageResp<T> {
  list: T[]
  total: number
}

export type UserPageQuery = {
  page: number
  size: number
  /** Keyword matched against username, nickname and description. */
  description?: string
  status?: UserStatus
  /** Filter by department ID. */
  deptId?: string | number
  /** `<field>,<asc|desc>`; the user query sorts on its `t1` table alias. */
  sort?: string
}

/** One page of users. */
export function listUsers(query: UserPageQuery, signal?: AbortSignal) {
  return http.get<PageResp<UserResp>>('/system/user', { query, signal })
}

/** Every user matching the filter, as a file download. */
export function exportUsers(query: Omit<UserPageQuery, 'page' | 'size'>) {
  return downloadFile('/system/user/export', query)
}

/**
 * `GET /system/user/{id}`.
 *
 * The detail response extends the list response, so it only adds the fields the list omits.
 */
export interface UserDetailResp extends UserResp {
  /** Last password change time. */
  pwdResetTime: string | null
}

/** One user's full record. */
export function getUser(id: string | number, signal?: AbortSignal) {
  return http.get<UserDetailResp>(`/system/user/${id}`, { signal })
}

/**
 * The body of `PUT /system/user/{id}`.
 *
 * `password` is deliberately absent: the backend only requires it in its create validation
 * group, and an existing password is changed through `PATCH /system/user/{id}/password`.
 * Ids stay in whatever form the API handed out, so a snowflake id is never narrowed into a
 * `number` and silently loses precision.
 */
export type UserReq = {
  username: string
  nickname: string
  gender: UserGender
  deptId: string | number
  roleIds: (string | number)[]
  status: UserStatus
  email?: string
  phone?: string
  description?: string
}

export function updateUser(id: string | number, body: UserReq) {
  return http.put<void>(`/system/user/${id}`, body)
}

/**
 * The body of `POST /system/user`.
 *
 * Extends the update body with `password`, which the backend only requires in its create
 * validation group (`CrudValidationGroup.Create`). It must already be RSA encrypted (see
 * `@/lib/crypto`); `UserServiceImpl.beforeCreate` decrypts it with the private key.
 */
export type UserCreateReq = UserReq & {
  password: string
}

/** Creates a user, answering with its new id. */
export function createUser(body: UserCreateReq) {
  return http.post<{ id: string | number }>('/system/user', body)
}

/**
 * Deletes one user.
 *
 * The backend only exposes the batch endpoint (`Api.BATCH_DELETE`), which takes `{ ids }`, so a
 * single deletion sends a one-id list. This mirrors the Vue client's `deleteUser`.
 */
export function deleteUser(id: string | number) {
  return http.del<void>('/system/user', { body: { ids: [id] } })
}

/**
 * The body of `PATCH /system/user/{id}/password`.
 *
 * `newPassword` must already be RSA encrypted (see `@/lib/crypto`); the backend decrypts it
 * with its private key and stores the result.
 */
export type UserPasswordResetReq = {
  newPassword: string
}

/**
 * Resets one user's password.
 *
 * The backend also invalidates that account's long-lived credentials, so every device it is
 * signed in on has to sign in again.
 */
export function resetUserPassword(id: string | number, body: UserPasswordResetReq) {
  return http.patch<void>(`/system/user/${id}/password`, body)
}

/** The body of `PATCH /system/user/{id}/role`; the backend requires at least one role. */
export type UserRoleUpdateReq = {
  roleIds: (string | number)[]
}

/** Replaces one user's role set. */
export function assignUserRoles(id: string | number, body: UserRoleUpdateReq) {
  return http.patch<void>(`/system/user/${id}/role`, body)
}

/** `ImportPolicyEnum`: 1 跳过该行, 2 修改数据, 3 停止导入. */
export type UserImportPolicy = 1 | 2 | 3

/**
 * The result of `POST /system/user/import/parse`.
 *
 * The backend keeps the valid rows under `importKey` in Redis for 10 minutes; only counts come
 * back, so the UI can show how many rows were accepted or collided, not per-row errors.
 */
export interface UserImportParseResp {
  importKey: string
  totalRows: number
  validRows: number
  duplicateUserRows: number
  duplicateEmailRows: number
  duplicatePhoneRows: number
}

/** The body of `POST /system/user/import`. */
export interface UserImportReq {
  importKey: string
  duplicateUser: UserImportPolicy
  duplicateEmail: UserImportPolicy
  duplicatePhone: UserImportPolicy
  /** `DisEnableStatusEnum`: 1 启用, 2 禁用. */
  defaultStatus?: UserStatus
}

/** The result of `POST /system/user/import`. */
export interface UserImportResp {
  totalRows: number
  insertRows: number
  updateRows: number
}

/** Downloads the user import template (an xlsx file). */
export function downloadUserImportTemplate() {
  return downloadFile('/system/user/import/template')
}

/** Uploads one xlsx for parsing; the valid rows stay under `importKey` for 10 minutes. */
export function parseUserImport(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return http.post<UserImportParseResp>('/system/user/import/parse', formData)
}

/** Commits a parsed import with the duplicate policies and default status. */
export function importUsers(body: UserImportReq) {
  return http.post<UserImportResp>('/system/user/import', body)
}

export { userKeys } from '@/lib/query-keys'
