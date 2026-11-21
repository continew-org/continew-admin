import type { ReactNode } from 'react'

import { cn } from 'cn'

import { Badge } from '@/components/ui/badge'
import { EMPTY_PLACEHOLDER } from '@/lib/constants'

export type TypeTone = 'blue' | 'violet' | 'teal' | 'amber' | 'neutral'

const TYPE_CLASSES: Record<TypeTone, string> = {
  blue: 'border-type-blue/20 bg-type-blue/10 text-type-blue-foreground',
  violet: 'border-type-violet/20 bg-type-violet/10 text-type-violet-foreground',
  teal: 'border-type-teal/20 bg-type-teal/10 text-type-teal-foreground',
  amber: 'border-type-amber/20 bg-type-amber/10 text-type-amber-foreground',
  neutral: 'border-border bg-muted/50 text-muted-foreground'
}

interface TypeBadgeProps {
  readonly tone: TypeTone
  readonly label: string
  readonly className?: string
}

export function TypeBadge({ tone, label, className }: TypeBadgeProps) {
  return (
    <Badge variant="outline" className={cn('gap-1.5 border px-2 py-0.5 font-normal', TYPE_CLASSES[tone], className)}>
      {label}
    </Badge>
  )
}

interface FlagBadgeProps {
  readonly active?: boolean | null
  readonly activeLabel: string
  readonly inactiveLabel?: string
  readonly icon?: ReactNode
}

export function FlagBadge({ active, activeLabel, inactiveLabel, icon }: FlagBadgeProps) {
  const isActive = Boolean(active)

  if (!isActive && !inactiveLabel) {
    return null
  }

  return (
    <Badge variant="outline">
      {isActive ? icon : null}
      {isActive ? activeLabel : inactiveLabel}
    </Badge>
  )
}

interface BooleanCellProps {
  readonly active?: boolean | null
  readonly label: string
  readonly icon?: ReactNode
  readonly className?: string
}

export function BooleanCell({ active, label, icon, className }: BooleanCellProps) {
  if (!active) {
    return <span className="text-sm text-muted-foreground">—</span>
  }

  return (
    <Badge
      variant="outline"
      className={cn('gap-1 border-border/60 bg-muted/30 px-1.5 py-0 font-normal text-muted-foreground', className)}
    >
      {icon}
      {label}
    </Badge>
  )
}

interface TagListProps {
  readonly items: readonly string[]
  readonly max?: number
  readonly className?: string
}

export function TagList({ items, max, className }: TagListProps) {
  if (items.length === 0) {
    return <span className="text-sm">{EMPTY_PLACEHOLDER}</span>
  }

  const visibleItems = max === undefined ? items : items.slice(0, max)
  const hiddenCount = items.length - visibleItems.length

  return (
    <div className={cn('flex flex-wrap gap-1', className)}>
      {visibleItems.map(item => (
        <Badge key={item} variant="secondary">
          {item}
        </Badge>
      ))}
      {hiddenCount > 0 ? <Badge variant="secondary">+{hiddenCount}</Badge> : null}
    </div>
  )
}
