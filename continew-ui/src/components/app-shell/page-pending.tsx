import { Spinner } from '@/components/ui/spinner'

export function PagePending() {
  return (
    <div className="flex min-h-svh items-center justify-center">
      <Spinner className="size-4" />
    </div>
  )
}
