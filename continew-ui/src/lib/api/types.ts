/**
 * Response and payload types for the continew-admin API.
 *
 * `code` is a string in the backend's `R` wrapper and business failures are
 * returned with HTTP 200 plus `success: false`, while an expired access token
 * returns HTTP 401.
 */
export interface ApiResponse<T> {
  success: boolean
  code: string
  msg: string
  timestamp?: number
  data: T
}

export interface PageResp<T> {
  list: T[]
  total: number
}

export type AuthType = 'ACCOUNT' | 'PHONE' | 'EMAIL' | 'SOCIAL'

/** Account login payload; `password` must be RSA encrypted. */
export interface AccountLoginReq {
  clientId: string
  authType: 'ACCOUNT'
  username: string
  password: string
  captcha?: string
  uuid?: string
}

export interface LoginResp {
  accessToken: string
  tokenType: 'Bearer'
  expiresIn: number
  refreshExpiresIn: number
  /** Only returned in BODY mode; browsers receive it as an HttpOnly cookie. */
  refreshToken?: string
  tenantId?: string | number | null
}

/** Authorize target for a third-party login, returned by `GET /auth/{source}`. */
export interface SocialAuthAuthorizeResp {
  authorizeUrl: string
}

/** The provider redirects back with its own parameters (`code`, `state`, ...). */
export type SocialLoginReq = {
  source: string
  clientId: string
  authType: 'SOCIAL'
} & Record<string, unknown>

export interface CaptchaResp {
  uuid: string
  /** Data URI, for example `data:image/png;base64,...`. */
  img: string
  /** Omitted when the captcha is disabled. */
  expireTime?: number
  isEnabled: boolean
}

export interface UserInfoResp {
  id: number
  username: string
  nickname: string
  gender: number
  /** Masked by the backend, for example `c*******@126.com`. */
  email: string
  /** Masked by the backend. */
  phone: string
  avatar: string | null
  description: string
  pwdResetTime: string
  pwdExpired: boolean
  createTime: string
  registrationDate: string
  deptId: number
  deptName: string
  permissions: string[]
  roles: string[]
  roleNames: string[]
}

export type RouteType = 1 | 2 | 3

/** Both password fields must be RSA encrypted before they are sent. */
export interface UserPasswordUpdateReq {
  /** Empty for third-party accounts that are setting an initial password. */
  oldPassword?: string
  newPassword: string
}

/** Menu tree node returned by `GET /auth/user/route`. */
export interface RouteResp {
  id: number
  parentId: number
  title: string
  /** 1: directory, 2: menu, 3: button. */
  type: RouteType
  path: string
  name: string
  component: string
  redirect?: string | null
  icon?: string | null
  isExternal?: boolean
  isCache?: boolean
  isHidden?: boolean
  permission?: string | null
  sort?: number
  children?: RouteResp[]
}
