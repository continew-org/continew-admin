import * as React from 'react'

import { useQueryClient } from '@tanstack/react-query'

import { LogOut } from 'lucide-react'

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
import { kickoutOnlineUser, onlineUserKeys, type OnlineUserResp } from '@/lib/api/online'

interface OnlineUserKickoutDialogProps {
  /** The session being kicked out, or `null` before the first one is picked. */
  readonly user: OnlineUserResp | null
  readonly open: boolean
  readonly onClose: () => void
}

export function OnlineUserKickoutDialog({ user, open, onClose }: OnlineUserKickoutDialogProps) {
  const queryClient = useQueryClient()
  const [isKicking, setIsKicking] = React.useState(false)
  const shownUser = useLastRecord(user)

  const sessionLabel = shownUser
    ? `${shownUser.nickname || shownUser.username}${shownUser.nickname ? `(${shownUser.username})` : ''}`
    : ''
  const shownTarget = shownUser?.ip ? `${sessionLabel}（IP ${shownUser.ip}）` : sessionLabel

  const handleKickout = async () => {
    if (user === null) {
      return
    }

    setIsKicking(true)

    try {
      await kickoutOnlineUser(user.sessionId)

      onClose()
      // The session disappears from every cached page, not just the current one.
      void queryClient.invalidateQueries({ queryKey: onlineUserKeys.all })
      toast.add({ title: '强退成功', type: 'success' })
    } catch {
      // The HTTP layer already reported the failure.
    } finally {
      setIsKicking(false)
    }
  }

  return (
    <AlertDialog
      open={open}
      onOpenChange={nextOpen => {
        // Closing mid-request would leave the call in flight with no way to report it.
        if (!nextOpen && !isKicking) {
          onClose()
        }
      }}
    >
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogMedia className="bg-destructive/10 text-destructive">
            <LogOut />
          </AlertDialogMedia>
          <AlertDialogTitle>强退在线用户</AlertDialogTitle>
          <AlertDialogDescription>
            {shownUser
              ? `是否确定强退「${shownTarget}」的登录会话？强退后该会话立即失效，用户需要重新登录。`
              : '是否确定强退该登录会话？强退后该会话立即失效，用户需要重新登录。'}
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel disabled={isKicking}>取消</AlertDialogCancel>
          <AlertDialogAction variant="destructive" disabled={isKicking} onClick={() => void handleKickout()}>
            {isKicking ? (
              <>
                <Spinner />
                强退中
              </>
            ) : (
              '强退'
            )}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  )
}
