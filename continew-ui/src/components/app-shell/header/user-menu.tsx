import { Link } from '@tanstack/react-router'

import { CircleUser, Lock, LogOut } from 'lucide-react'
import { useShallow } from 'zustand/react/shallow'

import { TagList } from '@/components/data-badge'
import { Avatar, AvatarBadge, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { Button } from '@/components/ui/button'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger
} from '@/components/ui/dropdown-menu'
import { Kbd, KbdGroup } from '@/components/ui/kbd'
import { useLogout } from '@/hooks/use-logout'
import { getInitials } from '@/lib/utils'
import { useAuthStore } from '@/stores/auth/auth-provider'
import { usePreferencesStore } from '@/stores/preferences/preferences-provider'
import { useShellStore } from '@/stores/shell'

/** Current user menu: identity, department, roles and logout. */
export function UserMenu() {
  const user = useAuthStore(state => state.user)
  const logout = useLogout()
  const setLockSetupOpen = useShellStore(state => state.setLockSetupOpen)
  const { lockScreen, shortcutLock, shortcutLogout } = usePreferencesStore(
    useShallow(state => ({
      lockScreen: state.values.widget_lock_screen,
      shortcutLock: state.values.shortcut_enable && state.values.shortcut_lock_screen,
      shortcutLogout: state.values.shortcut_enable && state.values.shortcut_logout
    }))
  )

  const name = user?.nickname || user?.username || ''
  const email = user?.email ?? ''
  const initials = getInitials(name)

  return (
    <DropdownMenu>
      <DropdownMenuTrigger render={<Button variant="ghost" size="icon" />}>
        <Avatar>
          {user?.avatar ? <AvatarImage src={user.avatar} alt={name} /> : null}
          <AvatarFallback>{initials}</AvatarFallback>
          <AvatarBadge className="bg-green-500" />
        </Avatar>
      </DropdownMenuTrigger>
      <DropdownMenuContent className="min-w-56 space-y-1 rounded-lg" side="bottom" align="end" sideOffset={4}>
        <div className="flex items-center gap-2 px-1 py-1.5">
          <Avatar className="size-9 rounded-lg">
            {user?.avatar ? <AvatarImage src={user.avatar} alt={name} /> : null}
            <AvatarFallback>{initials}</AvatarFallback>
          </Avatar>
          <div className="grid min-w-0 flex-1 text-left text-sm leading-tight">
            <span className="truncate font-semibold">{name}</span>
            <span className="truncate text-xs text-muted-foreground">{email}</span>
          </div>
        </div>
        {user?.deptName || user?.roleNames?.length ? (
          <div className="flex flex-col gap-1 px-2 pb-1 text-xs text-muted-foreground">
            {user.deptName ? (
              <span className="max-w-full truncate" title={user.deptName}>
                {user.deptName}
              </span>
            ) : null}
            {user.roleNames?.length ? <TagList items={user.roleNames} max={1} /> : null}
          </div>
        ) : null}
        <DropdownMenuSeparator />
        <DropdownMenuGroup>
          <DropdownMenuItem render={<Link to="/dashboard/profile" />}>
            <CircleUser />
            个人信息
          </DropdownMenuItem>
          {lockScreen && (
            <DropdownMenuItem onClick={() => setLockSetupOpen(true)}>
              <Lock />
              锁定屏幕
              {shortcutLock && (
                <KbdGroup className="ml-auto">
                  <Kbd>Alt</Kbd>
                  <Kbd>L</Kbd>
                </KbdGroup>
              )}
            </DropdownMenuItem>
          )}
        </DropdownMenuGroup>
        <DropdownMenuSeparator />
        <DropdownMenuItem onClick={() => void logout()}>
          <LogOut />
          登出系统
          {shortcutLogout && (
            <KbdGroup className="ml-auto">
              <Kbd>Alt</Kbd>
              <Kbd>Q</Kbd>
            </KbdGroup>
          )}
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  )
}
