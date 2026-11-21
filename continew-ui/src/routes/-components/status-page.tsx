import type { ReactNode } from 'react'

import { cn } from 'cn'
import type { LucideIcon } from 'lucide-react'

interface StatusPageProps {
  icon: LucideIcon
  iconClassName?: string
  title: string
  description: string
  actions: ReactNode
  children?: ReactNode
}

/**
 * Full-page layout for the 404, error, and 403 screens.
 *
 * These screens render outside AppShell, so viewport height is the page height.
 */
export function StatusPage({ icon: Icon, iconClassName, title, description, actions, children }: StatusPageProps) {
  return (
    <main className="flex min-h-dvh flex-col items-center justify-center gap-6 px-4 text-center">
      <Icon className={cn('size-12', iconClassName ?? 'text-primary')} />
      <div className="flex max-w-md flex-col gap-2">
        <h1 className="text-3xl font-bold tracking-tight sm:text-4xl">{title}</h1>
        <p className="text-muted-foreground">{description}</p>
        {children}
      </div>
      <div className="flex flex-wrap justify-center gap-2">{actions}</div>
    </main>
  )
}
