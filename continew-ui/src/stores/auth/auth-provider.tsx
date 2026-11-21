import { createContext, use, useEffect, useRef, useState } from 'react'

import { useQueryClient } from '@tanstack/react-query'
import { useNavigate, useRouter } from '@tanstack/react-router'
import { type StoreApi, useStore } from 'zustand'

import { toast } from '@/components/ui/toast'
import { getAccessToken } from '@/lib/auth-session'
import { endSession } from '@/lib/end-session'
import { setForbiddenHandler, setSessionExpiredHandler } from '@/lib/http'
import { useTabsStore } from '@/stores/tabs'

import { createAuthStore, type AuthState } from './auth-store'

const AuthStoreContext = createContext<StoreApi<AuthState> | null>(null)

export function AuthStoreProvider({ children }: { children: React.ReactNode }) {
  const [store] = useState(() => createAuthStore())
  const navigate = useNavigate()
  const router = useRouter()
  const queryClient = useQueryClient()
  const resetTabs = useTabsStore(state => state.resetTabs)
  const passwordExpiryNotified = useRef(false)

  useEffect(() => {
    if (getAccessToken()) {
      void store
        .getState()
        .loadUser()
        .catch(() => undefined)
    }
  }, [store])

  useEffect(() => {
    return store.subscribe(state => {
      if (!state.user?.pwdExpired || passwordExpiryNotified.current) {
        return
      }

      passwordExpiryNotified.current = true
      toast.add({ title: '密码已过期，请先修改密码。', type: 'warning' })
      void navigate({ to: '/dashboard/profile', replace: true })
    })
  }, [navigate, store])

  useEffect(() => {
    setSessionExpiredHandler(message => {
      toast.add({ title: message, type: 'error' })
      void endSession({
        clearAuthSession: store.getState().clearLocalSession,
        queryClient,
        resetTabs,
        navigate,
        redirect: router.state.location.href
      }).catch(() => undefined)
    })

    setForbiddenHandler(() => {
      if (router.state.location.pathname === '/unauthorized') {
        return
      }

      void navigate({ to: '/unauthorized', replace: true })
    })

    return () => {
      setSessionExpiredHandler(null)
      setForbiddenHandler(null)
    }
  }, [navigate, queryClient, resetTabs, router, store])

  return <AuthStoreContext.Provider value={store}>{children}</AuthStoreContext.Provider>
}

export function useAuthStore<T>(selector: (state: AuthState) => T): T {
  const store = use(AuthStoreContext)
  if (!store) throw new Error('Missing AuthStoreProvider')
  return useStore(store, selector)
}
