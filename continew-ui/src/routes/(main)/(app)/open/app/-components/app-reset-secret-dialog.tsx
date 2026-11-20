import * as React from 'react'

import { useQueryClient } from '@tanstack/react-query'
import { RotateCcw } from 'lucide-react'

import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogMedia,
  AlertDialogTitle
} from '@/components/ui/alert-dialog'
import { Spinner } from '@/components/ui/spinner'
import { toast } from '@/components/ui/toast'
import { useLastRecord } from '@/hooks/use-last-record'
import { appKeys, type AppResp, resetAppSecret } from '@/lib/api/app'

import { appLabel } from './app-display'

interface AppResetSecretDialogProps {
  readonly app: AppResp | null
  readonly open: boolean
  readonly onClose: () => void
  readonly onSuccess?: (app: AppResp) => void
}

export function AppResetSecretDialog({ app, open, onClose, onSuccess }: AppResetSecretDialogProps) {
  const queryClient = useQueryClient()
  const [isResetting, setIsResetting] = React.useState(false)
  const shownApp = useLastRecord(app)
  const shownLabel = appLabel(shownApp)

  const handleReset = async () => {
    if (app === null) {
      return
    }

    setIsResetting(true)
    try {
      await resetAppSecret(app.id)

      await queryClient.invalidateQueries({ queryKey: appKeys.secret(app.id) })
      toast.add({ title: '重置密钥成功', type: 'success' })
      onClose()
      onSuccess?.(app)
    } catch {
      // The HTTP layer already reported the failure.
    } finally {
      setIsResetting(false)
    }
  }

  return (
    <AlertDialog
      open={open}
      onOpenChange={nextOpen => {
        if (!nextOpen && !isResetting) {
          onClose()
        }
      }}
    >
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogMedia className="bg-amber-500/10 text-amber-600 dark:text-amber-400">
            <RotateCcw />
          </AlertDialogMedia>
          <AlertDialogTitle>重置应用密钥</AlertDialogTitle>
          <AlertDialogDescription>
            {shownApp
              ? `确定重置应用「${shownLabel}」的密钥吗？重置后原 Secret Key 将立即作废，使用旧密钥的外部服务将无法正常调用接口。`
              : '重置密钥后，原 Secret Key 将立即作废，使用旧密钥的外部服务将无法正常调用接口。是否确定重置？'}
          </AlertDialogDescription>
        </AlertDialogHeader>

        <AlertDialogFooter>
          <AlertDialogCancel disabled={isResetting}>取消</AlertDialogCancel>
          <AlertDialogAction
            variant="destructive"
            disabled={isResetting}
            onClick={() => void handleReset()}
            className="bg-amber-600 text-white hover:bg-amber-700 dark:bg-amber-600 dark:hover:bg-amber-700"
          >
            {isResetting ? (
              <>
                <Spinner />
                重置中
              </>
            ) : (
              '确认重置'
            )}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  )
}
