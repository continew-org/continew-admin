import * as React from 'react'

import { createFileRoute, useNavigate } from '@tanstack/react-router'

import { Loader2Icon } from 'lucide-react'

import { toast } from '@/components/ui/toast'
import { useAuthStore } from '@/stores/auth/auth-provider'

export const Route = createFileRoute('/(main)/social/callback')({
  validateSearch: (search: Record<string, unknown>) => search,
  component: SocialLoginCallback
})

/**
 * Landing page for a third-party login. The provider redirects here with `source` plus its own
 * callback parameters; they are forwarded to `POST /auth/login` as an `SOCIAL` login.
 */
function SocialLoginCallback() {
  const navigate = useNavigate()
  const socialLogin = useAuthStore(state => state.socialLogin)
  const search = Route.useSearch()
  const startedRef = React.useRef(false)

  React.useEffect(() => {
    if (startedRef.current) {
      return
    }

    startedRef.current = true

    const { source, redirect, ...rest } = search

    if (typeof source !== 'string' || source === '') {
      void navigate({ to: '/auth/login', replace: true })
      return
    }

    socialLogin({ ...rest, source })
      .then(() => {
        toast.add({ title: '登录成功', type: 'success' })
        const target = typeof redirect === 'string' && redirect.startsWith('/') ? redirect : '/dashboard'
        return navigate({ href: target, replace: true })
      })
      .catch(error => {
        // The login endpoints are silent, so this page owns the failure message.
        toast.add({ title: error instanceof Error ? error.message : '第三方登录失败', type: 'error' })
        void navigate({ to: '/auth/login', replace: true })
      })
  }, [navigate, search, socialLogin])

  return (
    <div className="flex min-h-svh items-center justify-center">
      <Loader2Icon className="size-6 animate-spin text-muted-foreground" />
      <span className="sr-only">正在登录，请稍候</span>
    </div>
  )
}
