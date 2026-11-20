import { createFileRoute, Outlet } from '@tanstack/react-router'
import { cn } from 'cn'

import { ThemeSwitcher } from '@/components/app-shell/header/theme-switcher'
import { Card, CardContent } from '@/components/ui/card'
import { Ripple } from '@/components/ui/ripple'
import { useSiteOptions } from '@/hooks/use-site-options'

import { AuthPending } from './-components/auth-pending'

export const Route = createFileRoute('/(main)/auth')({
  pendingComponent: AuthPending,
  component: AuthLayout
})

function AuthBrand({ logo, title, className }: { logo: string; title: string; className?: string }) {
  return (
    <div className={cn('flex items-center gap-2', className)}>
      <img src={logo} alt={title} className="w-6 md:w-8" />
      <div className="font-bold md:text-lg">{title}</div>
    </div>
  )
}

function AuthLayout() {
  const { title, logo, copyright, beian } = useSiteOptions()

  return (
    <main className="flex min-h-svh flex-col items-center justify-center p-6 md:p-10">
      <div className="w-full max-w-sm md:max-w-4xl">
        <header className="mb-8 flex items-center justify-between md:justify-end">
          <AuthBrand logo={logo} title={title} className="md:hidden" />
          <ThemeSwitcher showTooltip={false} />
        </header>

        <div className="flex flex-col gap-6">
          <Card className="gap-0 p-0">
            <CardContent className="grid p-0 md:grid-cols-2">
              <Outlet />

              <div className="relative hidden justify-end md:flex">
                <AuthBrand logo={logo} title={title} className="h-fit p-8" />
                <Ripple />
              </div>
            </CardContent>
          </Card>

          <footer className="px-6 text-center text-sm leading-normal text-muted-foreground">
            {copyright}
            {beian ? <span className="ml-2 text-xs">{beian}</span> : null}
          </footer>
        </div>
      </div>
    </main>
  )
}
