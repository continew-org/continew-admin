import { createStore } from 'zustand/vanilla'

import {
  getUserInfo,
  login as loginRequest,
  logout as logoutRequest,
  socialLogin as socialLoginRequest
} from '@/lib/api/auth'
import type { UserInfoResp } from '@/lib/api/types'
import { clearSession, getAccessToken, getAuthGeneration, setSession } from '@/lib/auth-session'
import { encryptByRsa } from '@/lib/crypto'

export type AuthStatus = 'anonymous' | 'authenticated'

export type LoginParams = {
  username: string
  password: string
  captcha?: string
  uuid?: string
  persist?: boolean
  /** Tenant code for multi-tenant login; empty means the default tenant. */
  tenantCode?: string
}

/** The provider's callback parameters (`code`, `state`, ...) plus its source key. */
export type SocialLoginParams = Record<string, unknown> & { source: string }

export type AuthState = {
  status: AuthStatus
  user: UserInfoResp | null
  permissions: string[]
  roles: string[]
  /**
   * True until the profile of a restored session has been fetched, successfully or not.
   *
   * It starts true whenever a token survived a reload, because `user` and `permissions`
   * are empty at that point and callers must not read them as "no permissions".
   */
  isLoadingUser: boolean
  login: (params: LoginParams) => Promise<void>
  socialLogin: (params: SocialLoginParams) => Promise<void>
  logout: () => void
  clearLocalSession: () => void
  loadUser: () => Promise<void>
}

type LoadState = {
  generation: number
  promise: Promise<void>
}

const anonymousState = (): Pick<AuthState, 'status' | 'user' | 'permissions' | 'roles'> => ({
  status: 'anonymous',
  user: null,
  permissions: [],
  roles: []
})

export const createAuthStore = () => {
  let loadState: LoadState | null = null
  let loadToken = 0

  return createStore<AuthState>()((set, get) => {
    const establishSession = async (
      session: { accessToken: string; tenantId?: string | number | null },
      persist?: boolean
    ) => {
      setSession({ accessToken: session.accessToken, tenantId: session.tenantId, persist })

      // Flip to authenticated only once the profile loads; a failed profile fetch must not leave
      // a token behind that the shell would read as a signed-in session.
      try {
        await get().loadUser()
      } catch (error) {
        get().clearLocalSession()
        throw error
      }
    }

    return {
      status: getAccessToken() ? 'authenticated' : 'anonymous',
      user: null,
      permissions: [],
      roles: [],
      // A restored token means the profile is still unknown, so a fresh page load must
      // wait for `loadUser` before painting anything that depends on permissions.
      isLoadingUser: getAccessToken() !== null,

      login: async ({ username, password, captcha, uuid, persist, tenantCode }) => {
        const response = await loginRequest(
          {
            clientId: import.meta.env.VITE_CLIENT_ID,
            authType: 'ACCOUNT',
            username,
            password: encryptByRsa(password),
            captcha,
            uuid
          },
          tenantCode
        )

        await establishSession(response, persist)
      },

      socialLogin: async params => {
        const response = await socialLoginRequest({
          ...params,
          clientId: import.meta.env.VITE_CLIENT_ID,
          authType: 'SOCIAL'
        })

        // There is no "remember me" on this path, so the session always persists.
        await establishSession(response, true)
      },

      loadUser: () => {
        if (!getAccessToken()) {
          loadToken += 1
          loadState = null
          set({ ...anonymousState(), isLoadingUser: false })
          return Promise.resolve()
        }

        const generation = getAuthGeneration()

        if (loadState?.generation === generation) {
          return loadState.promise
        }

        const requestToken = ++loadToken
        set({ isLoadingUser: true })

        const promise = getUserInfo()
          .then(user => {
            if (generation !== getAuthGeneration()) {
              return
            }

            set({
              user,
              permissions: user.permissions ?? [],
              roles: user.roles ?? [],
              status: 'authenticated'
            })
          })
          .finally(() => {
            if (requestToken !== loadToken) {
              return
            }

            loadState = null
            set({ isLoadingUser: false })
          })

        loadState = { generation, promise }
        return promise
      },

      clearLocalSession: () => {
        clearSession()
        loadToken += 1
        loadState = null
        set({ ...anonymousState(), isLoadingUser: false })
      },

      logout: () => {
        void logoutRequest().catch(() => undefined)
        get().clearLocalSession()
      }
    }
  })
}
