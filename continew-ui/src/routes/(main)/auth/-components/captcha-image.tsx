import { cn } from 'cn'
import { Loader2Icon } from 'lucide-react'

interface CaptchaImageProps {
  readonly img?: string
  readonly isExpired: boolean
  readonly isLoading: boolean
  readonly onRefresh: () => void
}

export function CaptchaImage({ img, isExpired, isLoading, onRefresh }: CaptchaImageProps) {
  return (
    <div
      className={cn(
        'relative h-8 w-28 shrink-0 cursor-pointer overflow-hidden rounded-lg border bg-muted/40 transition-colors hover:bg-muted',
        isLoading && 'opacity-60'
      )}
      onClick={onRefresh}
    >
      <img src={img} alt="" className={cn('block h-full w-full object-fill', isExpired && 'opacity-0')} />
      {isLoading ? <Loader2Icon className="absolute inset-0 m-auto size-4 animate-spin text-muted-foreground" /> : null}
      {isExpired && !isLoading ? (
        <span className="absolute inset-0 flex items-center justify-center text-xs font-medium text-status-warning-foreground">
          验证码已过期
        </span>
      ) : null}
    </div>
  )
}
