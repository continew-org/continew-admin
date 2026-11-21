import { CopyButton } from '@/components/copy-button'
import { Badge } from '@/components/ui/badge'
import type { AppResp } from '@/lib/api/app'

export function appLabel(app: Pick<AppResp, 'name'> | null | undefined) {
  return app?.name ?? ''
}

export function ExpireTimeCell({ expireTime }: { expireTime: string | null | undefined }) {
  if (!expireTime) {
    return <span className="text-sm text-muted-foreground">永久有效</span>
  }

  const isExpired = new Date(expireTime.replace(/-/g, '/')).getTime() < Date.now()

  if (isExpired) {
    return (
      <div className="flex items-center gap-1.5">
        <span className="text-sm text-destructive tabular-nums">{expireTime}</span>
        <Badge variant="destructive" className="px-1.5 py-0 text-xs font-normal">
          已过期
        </Badge>
      </div>
    )
  }

  return <span className="text-sm tabular-nums">{expireTime}</span>
}

export function AccessKeyCell({ accessKey }: { accessKey: string }) {
  return (
    <div className="flex items-center gap-1.5">
      <span className="truncate text-sm" title={accessKey}>
        {accessKey}
      </span>
      <CopyButton
        value={accessKey}
        label="复制 Access Key"
        successMessage="Access Key 已复制到剪贴板"
        size="icon-sm"
        className="size-6 shrink-0"
      />
    </div>
  )
}
