import { http } from '@/lib/http'

import type { UserPasswordUpdateReq } from './types'

export interface AvatarResp {
  avatar: string
}

export interface UserSocialBindResp {
  source: string
  description: string
}

/**
 * Changes the password of the signed-in user.
 *
 * The backend decrypts both fields with its RSA private key and enforces its own
 * policy (8-32 characters with at least one letter and one digit by default).
 */
export function updatePassword(req: UserPasswordUpdateReq) {
  return http.patch<void>('/user/profile/password', req, { silent: true })
}

/** Uploads a new avatar, answering with the URL the profile should show. */
export function updateAvatar(file: File) {
  const body = new FormData()
  body.append('avatarFile', file)
  return http.patch<AvatarResp>('/user/profile/avatar', body)
}

/** The third-party accounts bound to the signed-in user. */
export function listSocialBindings(signal?: AbortSignal) {
  return http.get<UserSocialBindResp[]>('/user/profile/social', { signal })
}

export { profileKeys } from '@/lib/query-keys'
