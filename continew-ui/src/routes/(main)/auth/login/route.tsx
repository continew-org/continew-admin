import { createFileRoute } from '@tanstack/react-router'

import { Separator } from '@/components/ui/separator'

import { AuthPending } from '../-components/auth-pending'
import { LoginForm } from '../-components/login-form'
import { OAuthButtons } from '../-components/social-auth/oauth-buttons'

export const Route = createFileRoute('/(main)/auth/login')({
  validateSearch: (search: Record<string, unknown>): { redirect?: string } => ({
    redirect: typeof search.redirect === 'string' ? search.redirect : undefined
  }),
  pendingComponent: AuthPending,
  component: LoginPage
})

function LoginPage() {
  const { redirect } = Route.useSearch()

  return (
    <div className="flex flex-col gap-6 p-6 md:p-8">
      <div className="flex flex-col">
        <h1 className="text-2xl font-bold">欢迎回来</h1>
        <p className="text-sm text-balance text-muted-foreground">请输入您的详细信息以登录</p>
      </div>
      <LoginForm redirectTo={redirect} />

      <div className="flex flex-col gap-3">
        <div className="flex items-center gap-3">
          <Separator className="flex-1" />
          <span className="text-xs text-muted-foreground">或使用以下方式登录</span>
          <Separator className="flex-1" />
        </div>
        <OAuthButtons />
      </div>
    </div>
  )
}
