import { cn } from 'cn'

import { Button } from '@/components/ui/button'
import { Empty, EmptyHeader, EmptyTitle } from '@/components/ui/empty'

interface QueryStateProps {
  readonly state: 'empty' | 'error'
  /** Defaults to 「数据加载失败」 or 「暂无数据」. */
  readonly title?: string
  readonly onRetry?: () => void
  readonly className?: string
}

/** The shared empty/error block used by tables, panels and drawers. */
export function QueryState({ state, title, onRetry, className }: QueryStateProps) {
  const isError = state === 'error'

  return (
    <Empty className={cn('gap-2 p-4', className)}>
      <EmptyHeader>
        <EmptyTitle>{title ?? (isError ? '数据加载失败' : '暂无数据')}</EmptyTitle>
      </EmptyHeader>
      {isError && onRetry ? (
        <Button type="button" variant="outline" size="sm" onClick={onRetry}>
          重新加载
        </Button>
      ) : null}
    </Empty>
  )
}
