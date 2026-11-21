import type { ReactNode } from 'react'

import { cn } from 'cn'
import { Star } from 'lucide-react'

import { Badge } from '@/components/ui/badge'

export type StatusTone = 'success' | 'warning' | 'caution' | 'danger' | 'info' | 'neutral' | 'primary'

const TONE_CLASSES: Record<StatusTone, { badge: string; dot: string }> = {
  success: {
    badge: 'border-status-success/20 bg-status-success/10 text-status-success-foreground',
    dot: 'bg-status-success'
  },
  warning: {
    badge: 'border-status-warning/20 bg-status-warning/10 text-status-warning-foreground',
    dot: 'bg-status-warning'
  },
  caution: {
    badge: 'border-status-caution/20 bg-status-caution/10 text-status-caution-foreground',
    dot: 'bg-status-caution'
  },
  danger: {
    badge: 'border-destructive/20 bg-destructive/10 text-destructive',
    dot: 'bg-destructive'
  },
  info: {
    badge: 'border-status-info/20 bg-status-info/10 text-status-info-foreground',
    dot: 'bg-status-info'
  },
  neutral: {
    badge: 'border-border bg-muted/50 text-muted-foreground',
    dot: 'bg-muted-foreground'
  },
  primary: {
    badge: 'border-primary/20 bg-primary/10 text-primary',
    dot: 'bg-primary'
  }
}

interface StatusBadgeProps {
  readonly tone: StatusTone
  readonly label: ReactNode
  readonly className?: string
}

export function StatusBadge({ tone, label, className }: StatusBadgeProps) {
  const classes = TONE_CLASSES[tone]

  return (
    <Badge variant="outline" className={cn('gap-1.5 border px-2 py-1 font-medium', classes.badge, className)}>
      <span className={cn('size-1.5 rounded-full', classes.dot)} />
      {label}
    </Badge>
  )
}

export function StatusCell({ status }: { status?: number | null }) {
  const isEnabled = status === 1

  return <StatusBadge tone={isEnabled ? 'success' : 'danger'} label={isEnabled ? '启用' : '禁用'} />
}

export function SuccessFailureCell({ status }: { status?: number | null }) {
  const isSuccess = status === 1

  return <StatusBadge tone={isSuccess ? 'success' : 'danger'} label={isSuccess ? '成功' : '失败'} />
}

/** The 默认 marker, drawn like the 内置 badge of the role screen: an outline badge with its icon. */
export function DefaultBadge() {
  return (
    <Badge variant="outline">
      <Star />
      默认
    </Badge>
  )
}
