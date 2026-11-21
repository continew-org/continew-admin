import * as React from 'react'

import { skipToken, useQuery } from '@tanstack/react-query'
import { Eye, EyeOff, KeyRound, ShieldAlert } from 'lucide-react'

import { CopyButton } from '@/components/copy-button'
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
import { Tooltip, TooltipContent, TooltipTrigger } from '@/components/ui/tooltip'
import { useLastRecord } from '@/hooks/use-last-record'
import { appKeys, type AppResp, getAppSecret } from '@/lib/api/app'

interface AppSecretDialogProps {
  readonly app: AppResp | null
  readonly open: boolean
  readonly onClose: () => void
}

function SecretField({ label, value, isMaskable = false }: { label: string; value: string; isMaskable?: boolean }) {
  const [show, setShow] = React.useState(!isMaskable)
  const displayValue = show ? value : value.replace(/./g, '•')

  return (
    <div className="flex flex-col gap-1.5">
      <span className="text-xs font-medium text-muted-foreground">{label}</span>
      <div className="flex items-center gap-2 rounded-md border bg-muted/30 px-3 py-2">
        <span className="flex-1 text-sm break-all select-all">{displayValue}</span>
        <div className="flex shrink-0 items-center gap-1">
          {isMaskable ? (
            <Tooltip>
              <TooltipTrigger
                render={
                  <Button variant="ghost" size="icon-sm" className="size-7" onClick={() => setShow(prev => !prev)} />
                }
              >
                {show ? <EyeOff className="size-3.5" /> : <Eye className="size-3.5" />}
              </TooltipTrigger>
              <TooltipContent side="top" sideOffset={8}>
                {show ? '隐藏私钥' : '显示私钥'}
              </TooltipContent>
            </Tooltip>
          ) : null}
          <CopyButton
            value={value}
            label={`复制 ${label}`}
            successMessage={`${label} 已复制到剪贴板`}
            size="icon-sm"
            className="size-7"
          />
        </div>
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
    enabled: open
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
