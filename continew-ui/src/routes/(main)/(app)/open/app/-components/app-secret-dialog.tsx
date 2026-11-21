import { skipToken, useQuery } from '@tanstack/react-query'
import { KeyRound, ShieldAlert } from 'lucide-react'

import { CopyButton } from '@/components/copy-button'
import { SensitiveText } from '@/components/sensitive-text'
import { QueryState } from '@/components/query-state'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogClose,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle
} from '@/components/ui/dialog'
import { Skeleton } from '@/components/ui/skeleton'
import { useLastRecord } from '@/hooks/use-last-record'
import { appKeys, type AppResp, getAppSecret } from '@/lib/api/app'

interface AppSecretDialogProps {
  readonly app: AppResp | null
  readonly open: boolean
  readonly onClose: () => void
}

function SecretField({ label, value, isMaskable = false }: { label: string; value: string; isMaskable?: boolean }) {
  return (
    <div className="flex flex-col gap-1.5">
      <span className="text-xs font-medium text-muted-foreground">{label}</span>
      <div className="flex items-center gap-2 rounded-md border bg-muted/30 px-3 py-2">
        <SensitiveText
          value={value}
          type="password"
          defaultRevealed={!isMaskable}
          className="flex-1"
          textClassName="flex-1 text-sm break-all select-all whitespace-normal"
        />
        <CopyButton
          value={value}
          label={`复制 ${label}`}
          successMessage={`${label} 已复制到剪贴板`}
          size="icon-sm"
          className="size-7 shrink-0"
        />
      </div>
    </div>
  )
}

export function AppSecretDialog({ app, open, onClose }: AppSecretDialogProps) {
  const appId = app?.id ?? null
  const shownApp = useLastRecord(app)

  const query = useQuery({
    queryKey: appKeys.secret(appId),
    queryFn: appId === null ? skipToken : ({ signal }) => getAppSecret(appId, signal),
    enabled: open,
    // The secret is dropped from the cache once the dialog closes instead of lingering in memory.
    gcTime: 0
  })

  return (
    <Dialog open={open} onOpenChange={nextOpen => !nextOpen && onClose()}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <div className="flex items-center gap-2">
            <KeyRound className="size-4" />
            <DialogTitle>应用密钥</DialogTitle>
          </div>
          <DialogDescription>
            {shownApp ? `应用「${shownApp.name}」的接口调用认证凭证` : '查看应用密钥'}
          </DialogDescription>
        </DialogHeader>

        <div className="flex flex-col gap-4 py-2">
          {query.isLoading ? (
            <div className="flex flex-col gap-3">
              <Skeleton className="h-14 w-full" />
              <Skeleton className="h-14 w-full" />
            </div>
          ) : query.isError ? (
            <QueryState state="error" className="p-6" onRetry={() => void query.refetch()} />
          ) : query.data ? (
            <>
              <SecretField label="Access Key (访问密钥)" value={query.data.accessKey} />
              <SecretField label="Secret Key (私有密钥)" value={query.data.secretKey} isMaskable />

              <Alert className="border-amber-500/30 bg-amber-500/10 text-xs text-amber-800 dark:text-amber-200">
                <ShieldAlert className="size-4 text-amber-600 dark:text-amber-400" />
                <AlertTitle>安全提示</AlertTitle>
                <AlertDescription className="text-amber-700/90 dark:text-amber-300/90">
                  Secret Key 是调用 Open API 的签名密钥，切勿在前端、公开代码库或不安全网络环境中泄露。
                </AlertDescription>
              </Alert>
            </>
          ) : null}
        </div>

        <DialogFooter>
          <DialogClose render={<Button variant="outline" />}>关闭</DialogClose>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
