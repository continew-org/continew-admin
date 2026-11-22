import * as React from 'react'

import { Lock } from 'lucide-react'

import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle
} from '@/components/ui/dialog'
import { Input } from '@/components/ui/input'
import { useLogout } from '@/hooks/use-logout'
import { LOCK_SCREEN_KEY } from '@/lib/constants'
import { getInitials } from '@/lib/utils'
import { useAuthStore } from '@/stores/auth/auth-provider'

import { useShellStore } from './shell-store'

/**
 * Lock screen. The unlock password is a local one the user sets when locking, hashed with
 * SHA-256 into sessionStorage, so a reload keeps the screen locked but closing the browser tab
 * clears it. It guards an unattended screen; it is not an authentication factor.
 */

async function hash(text: string) {
  const digest = await crypto.subtle.digest('SHA-256', new TextEncoder().encode(text))
  return Array.from(new Uint8Array(digest), byte => byte.toString(16).padStart(2, '0')).join('')
}

function readLock() {
  try {
    return sessionStorage.getItem(LOCK_SCREEN_KEY)
  } catch {
    return null
  }
}

export function useLockScreen() {
  const [lockHash, setLockHash] = React.useState(readLock)

  const lock = React.useCallback(async (password: string) => {
    const value = await hash(password)
    sessionStorage.setItem(LOCK_SCREEN_KEY, value)
    setLockHash(value)
  }, [])

  const unlock = React.useCallback(
    async (password: string) => {
      if ((await hash(password)) !== lockHash) return false
      sessionStorage.removeItem(LOCK_SCREEN_KEY)
      setLockHash(null)
      return true
    },
    [lockHash]
  )

  const clear = React.useCallback(() => {
    sessionStorage.removeItem(LOCK_SCREEN_KEY)
    setLockHash(null)
  }, [])

  return { locked: lockHash !== null, lock, unlock, clear }
}

export function LockScreenSetupDialog({ onLock }: { onLock: (password: string) => Promise<void> }) {
  const open = useShellStore(state => state.lockSetupOpen)
  const setOpen = useShellStore(state => state.setLockSetupOpen)
  const [password, setPassword] = React.useState('')

  const submit = async (event: React.FormEvent) => {
    event.preventDefault()
    if (!password) return
    await onLock(password)
    setPassword('')
    setOpen(false)
  }

  return (
    <Dialog
      open={open}
      onOpenChange={next => {
        setOpen(next)
        if (!next) setPassword('')
      }}
    >
      <DialogContent className="sm:max-w-sm">
        <form onSubmit={submit} className="grid gap-4">
          <DialogHeader>
            <DialogTitle>锁定屏幕</DialogTitle>
            <DialogDescription>设置一个临时解锁密码，离开时保护当前页面。</DialogDescription>
          </DialogHeader>
          <Input
            type="password"
            autoFocus
            autoComplete="new-password"
            placeholder="请输入锁屏密码"
            value={password}
            onChange={event => setPassword(event.target.value)}
          />
          <DialogFooter>
            <Button type="submit" disabled={!password}>
              <Lock />
              立即锁定
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}

function useClock() {
  const [now, setNow] = React.useState(() => new Date())

  React.useEffect(() => {
    const timer = window.setInterval(() => setNow(new Date()), 1000)
    return () => window.clearInterval(timer)
  }, [])

  return now
}

export function LockScreenOverlay({
  onUnlock,
  onForget
}: {
  onUnlock: (password: string) => Promise<boolean>
  onForget: () => void
}) {
  const user = useAuthStore(state => state.user)
  const logout = useLogout()
  const now = useClock()
  const [password, setPassword] = React.useState('')
  const [error, setError] = React.useState(false)
  const name = user?.nickname || user?.username || ''

  const submit = async (event: React.FormEvent) => {
    event.preventDefault()
    const ok = await onUnlock(password)
    setError(!ok)
    if (ok) setPassword('')
  }

  return (
    <div
      role="dialog"
      aria-modal="true"
      aria-label="屏幕已锁定"
      className="fixed inset-0 z-[200] flex flex-col items-center justify-center gap-8 bg-background/80 backdrop-blur-xl motion-safe:animate-in motion-safe:fade-in"
    >
      <div className="text-center tabular-nums">
        <div className="text-6xl font-semibold tracking-tight">
          {now.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })}
        </div>
        <div className="mt-2 text-sm text-muted-foreground">
          {now.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' })}
        </div>
      </div>
      <form onSubmit={submit} className="flex w-72 flex-col items-center gap-3">
        <Avatar className="size-16">
          <AvatarImage src={user?.avatar ?? undefined} alt={name} />
          <AvatarFallback>{getInitials(name)}</AvatarFallback>
        </Avatar>
        <span className="font-medium">{name}</span>
        <Input
          type="password"
          autoFocus
          autoComplete="off"
          placeholder="输入锁屏密码解锁"
          aria-invalid={error || undefined}
          value={password}
          onChange={event => {
            setPassword(event.target.value)
            setError(false)
          }}
        />
        {error && <span className="text-xs text-destructive">密码错误</span>}
        <Button type="submit" className="w-full" disabled={!password}>
          解锁
        </Button>
        <Button
          type="button"
          variant="link"
          size="sm"
          className="text-muted-foreground"
          onClick={() => {
            onForget()
            void logout()
          }}
        >
          忘记密码？重新登录
        </Button>
      </form>
    </div>
  )
}
