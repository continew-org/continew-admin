import { QueryState } from '@/components/query-state'
import { Skeleton } from '@/components/ui/skeleton'

interface ConfigPanelSkeletonProps {
  fields?: number
  variant?: 'form' | 'card'
}

export function ConfigPanelSkeleton({ fields = 6, variant = 'form' }: ConfigPanelSkeletonProps) {
  if (variant === 'card') {
    return (
      <div className="grid grid-cols-1 gap-5 xl:grid-cols-2">
        <div className="flex flex-col gap-4 rounded-xl border bg-card p-5">
          <div className="flex items-center justify-between border-b pb-3">
            <Skeleton className="h-6 w-32" />
            <Skeleton className="h-8 w-24" />
          </div>
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <Skeleton className="h-16 w-16 rounded-lg" />
            <Skeleton className="h-16 w-16 rounded-lg" />
          </div>
          <Skeleton className="h-9 w-full" />
          <Skeleton className="h-9 w-full" />
          <Skeleton className="h-16 w-full" />
        </div>
        <div className="hidden flex-col rounded-xl border bg-card p-5 xl:flex">
          <Skeleton className="mb-3 h-6 w-28" />
          <Skeleton className="w-full flex-1 rounded-xl" />
        </div>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between border-b pb-4">
        <div className="space-y-1">
          <Skeleton className="h-6 w-32" />
          <Skeleton className="h-4 w-64" />
        </div>
      </div>
      <div className={fields > 1 ? 'grid grid-cols-1 gap-6 sm:grid-cols-2' : 'flex flex-col gap-6'}>
        {Array.from({ length: fields }, (_, index) => (
          <Skeleton key={index} className="h-20 w-full" />
        ))}
      </div>
    </div>
  )
}

interface ConfigPanelErrorProps {
  onRetry: () => void
}

export function ConfigPanelError({ onRetry }: ConfigPanelErrorProps) {
  return <QueryState state="error" className="rounded-xl border border-solid bg-card p-10" onRetry={onRetry} />
}
