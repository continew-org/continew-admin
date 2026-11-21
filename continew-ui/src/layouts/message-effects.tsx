import * as React from 'react'

import { useQuery, useQueryClient } from '@tanstack/react-query'

import { RichTextContent } from '@/components/rich-text-editor'
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle
} from '@/components/ui/dialog'
import { Skeleton } from '@/components/ui/skeleton'
import { listUnreadNoticeIds, messageKeys } from '@/lib/api/message'
import { getAccessToken } from '@/lib/auth-session'
import { API_BASE_URL } from '@/lib/http'
import { useUserNoticeDetail } from '@/routes/(main)/(app)/user/message/-components/use-messages'
import { useAuthStore } from '@/stores/auth/auth-provider'

const RECONNECT_DELAY_MS = 5_000

function webSocketUrl(token: string) {
  const url = new URL(`${API_BASE_URL}/websocket`, window.location.href)
  url.protocol = url.protocol === 'https:' ? 'wss:' : 'ws:'
  url.searchParams.set('token', token)
  return url.href
}

/**
 * Keeps the backend message socket (`continew-starter.messaging.websocket`) open while signed in.
 * A push is either `"1"` (something new arrived) or the new unread total after a read, so it is
 * only a signal: every push refetches the badges and lists instead of trusting the payload.
 */
export function MessageSocket() {
  const queryClient = useQueryClient()
  const signedIn = useAuthStore(state => state.status === 'authenticated')

  React.useEffect(() => {
    if (!signedIn) return

    let socket: WebSocket | null = null
    let timer: number | undefined
    let disposed = false

    const connect = () => {
      // Read the token on every attempt: a refreshed session invalidates the one used before.
      const token = getAccessToken()
      if (!token) return

      socket = new WebSocket(webSocketUrl(token))
      socket.onmessage = () => void queryClient.invalidateQueries({ queryKey: messageKeys.all })
      socket.onclose = () => {
        if (!disposed) timer = window.setTimeout(connect, RECONNECT_DELAY_MS)
      }
    }

    connect()
    return () => {
      disposed = true
      window.clearTimeout(timer)
      socket?.close()
    }
  }, [queryClient, signedIn])

  return null
}

/**
 * Login popup notices: the unread `POPUP` notices, asked once per session and paged in one dialog.
 * Opening a notice marks it read, so one closed before being reached pops up again next login.
 */
export function NoticePopup() {
  const signedIn = useAuthStore(state => state.status === 'authenticated')
  const idsQuery = useQuery({
    queryKey: messageKeys.popup(),
    queryFn: ({ signal }) => listUnreadNoticeIds('POPUP', signal),
    enabled: signedIn,
    staleTime: Infinity
  })
  const ids = idsQuery.data ?? []
  const [index, setIndex] = React.useState(0)
  const [dismissed, setDismissed] = React.useState(false)
  const open = signedIn && !dismissed && ids.length > 0
  const detailQuery = useUserNoticeDetail(ids[index] ?? null, { enabled: open })
  const detail = detailQuery.data
  const isLast = index >= ids.length - 1

  return (
    <Dialog open={open} onOpenChange={next => !next && setDismissed(true)}>
      <DialogContent className="sm:max-w-2xl">
        <DialogHeader>
          {detail ? (
            <>
              <DialogTitle>{detail.title}</DialogTitle>
              <DialogDescription>
                {[detail.createUserString, detail.publishTime ?? detail.createTime].filter(Boolean).join(' · ')}
              </DialogDescription>
            </>
          ) : (
            <>
              <DialogTitle>公告</DialogTitle>
              <DialogDescription>正在加载公告内容</DialogDescription>
            </>
          )}
        </DialogHeader>

        <div className="max-h-[60vh] min-h-24 overflow-y-auto overscroll-contain">
          {detail ? (
            <RichTextContent html={detail.content} />
          ) : detailQuery.isError ? (
            <p className="py-6 text-center text-sm text-muted-foreground">公告加载失败</p>
          ) : (
            <div className="flex flex-col gap-2">
              <Skeleton className="h-4 w-full" />
              <Skeleton className="h-4 w-full" />
              <Skeleton className="h-4 w-4/5" />
            </div>
          )}
        </div>

        <DialogFooter className="items-center sm:justify-between">
          <span className="text-xs text-muted-foreground tabular-nums">
            {ids.length > 1 ? `${index + 1} / ${ids.length}` : null}
          </span>
          <div className="flex gap-2">
            {ids.length > 1 && (
              <Button variant="outline" disabled={index === 0} onClick={() => setIndex(index - 1)}>
                上一条
              </Button>
            )}
            {isLast ? (
              <Button onClick={() => setDismissed(true)}>我知道了</Button>
            ) : (
              <Button onClick={() => setIndex(index + 1)}>下一条</Button>
            )}
          </div>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
