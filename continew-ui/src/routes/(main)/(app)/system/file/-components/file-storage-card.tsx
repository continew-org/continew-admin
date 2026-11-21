import { cn } from 'cn'

import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Separator } from '@/components/ui/separator'
import { Skeleton } from '@/components/ui/skeleton'
import type { FileFilterType, FileStatisticsResp } from '@/lib/api/file'

import { FILE_TYPE_FILTERS, formatFileSize } from './file-display'

interface FileStorageCardProps {
  readonly activeType: 'all' | FileFilterType
  readonly stats: FileStatisticsResp | undefined
  readonly isLoading: boolean
  readonly isError: boolean
  readonly onRetry: () => void
  readonly onTypeChange: (type: 'all' | FileFilterType) => void
}

/** Storage totals plus the file-type navigation, the sidebar equivalent of the Vue file page. */
export function FileStorageCard({
  activeType,
  stats,
  isLoading,
  isError,
  onRetry,
  onTypeChange
}: FileStorageCardProps) {
  const typeStats = new Map(
    (stats?.data ?? []).map(item => [Number(item.type), { number: item.number ?? 0, size: item.size ?? 0 }])
  )

  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-base">存储空间</CardTitle>
        <CardDescription>当前默认存储的文件统计</CardDescription>
      </CardHeader>
      <CardContent className="flex flex-col gap-4">
        <div className="grid grid-cols-2 gap-2">
          <div className="rounded-lg border bg-muted/30 p-3">
            <p className="text-xs text-muted-foreground">文件数量</p>
            {isLoading ? (
              <Skeleton className="mt-1.5 h-6 w-12" />
            ) : (
              <p className="mt-1 text-lg font-semibold tabular-nums">{stats?.number ?? 0}</p>
            )}
          </div>
          <div className="rounded-lg border bg-muted/30 p-3">
            <p className="text-xs text-muted-foreground">已用空间</p>
            {isLoading ? (
              <Skeleton className="mt-1.5 h-6 w-16" />
            ) : (
              <p className="mt-1 text-lg font-semibold tabular-nums">{formatFileSize(stats?.size)}</p>
            )}
          </div>
        </div>

        {isError ? (
          <div className="flex items-center justify-between gap-2 rounded-lg border border-dashed px-3 py-2">
            <span className="text-xs text-muted-foreground">统计加载失败</span>
            <Button type="button" variant="link" size="sm" onClick={onRetry}>
              重试
            </Button>
          </div>
        ) : null}

        <Separator className="hidden xl:block" />

        <nav className="hidden flex-col gap-1 xl:flex">
          {FILE_TYPE_FILTERS.map(item => {
            const Icon = item.icon
            const active = activeType === item.value
            const count = item.value === 'all' ? (stats?.number ?? 0) : (typeStats.get(item.value)?.number ?? 0)

            return (
              <button
                key={item.value}
                type="button"
                className={cn(
                  'flex w-full items-center gap-2 rounded-md px-2 py-1.5 text-sm transition-colors hover:bg-muted',
                  active && 'bg-muted font-medium'
                )}
                onClick={() => onTypeChange(item.value)}
              >
                <Icon className="size-4 text-muted-foreground" />
                <span className="flex-1 text-left">{item.label}</span>
                <span className="text-xs text-muted-foreground tabular-nums">{isLoading ? '—' : count}</span>
              </button>
            )
          })}
        </nav>
      </CardContent>
    </Card>
  )
}
