import type { ReactNode } from 'react'

import { cn } from 'cn'

interface DetailFieldProps {
  readonly label: string
  readonly className?: string
  readonly children: ReactNode
}

/** One label/value pair inside a `DetailGrid`. */
export function DetailField({ label, className, children }: DetailFieldProps) {
  return (
    <div className={cn('flex flex-col gap-1', className)}>
      <dt className="text-muted-foreground">{label}</dt>
      <dd className="wrap-break-word">{children}</dd>
    </div>
  )
}

interface DetailGridProps {
  readonly className?: string
  readonly children: ReactNode
}

/** The two-column description list the detail drawers share. */
export function DetailGrid({ className, children }: DetailGridProps) {
  return <dl className={cn('grid grid-cols-1 gap-x-6 gap-y-4 sm:grid-cols-2', className)}>{children}</dl>
}
