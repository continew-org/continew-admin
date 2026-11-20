import * as React from 'react'

import { Camera } from 'lucide-react'

import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { Input } from '@/components/ui/input'
import { Spinner } from '@/components/ui/spinner'
import { toast } from '@/components/ui/toast'
import { cn } from 'cn'
import { updateAvatar } from '@/lib/api/profile'
import { useAuthStore } from '@/stores/auth/auth-provider'

import type { ProfileRecord } from './profile-data'

interface ProfileHeaderProps {
  profile: ProfileRecord
}

export function ProfileHeader({ profile }: ProfileHeaderProps) {
  const loadUser = useAuthStore(state => state.loadUser)
  const fileRef = React.useRef<HTMLInputElement>(null)
  const [isUploading, setIsUploading] = React.useState(false)

  const handleAvatarChange = async (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0]
    event.target.value = ''

    if (!file) {
      return
    }

    if (!file.type.startsWith('image/')) {
      toast.add({ title: '请选择有效的图片文件', type: 'error' })
      return
    }

    setIsUploading(true)
    try {
      await updateAvatar(file)
      await loadUser()
      toast.add({ title: '头像已更新', type: 'success' })
    } catch {
      // The HTTP layer already reported the failure.
    } finally {
      setIsUploading(false)
    }
  }

  return (
    <div className="flex flex-col gap-5 px-4 lg:flex-row lg:items-end lg:justify-between">
      <div className="flex min-w-0 items-center gap-4">
        <div className="group relative size-16 shrink-0 sm:size-20">
          <Input
            ref={fileRef}
            type="file"
            accept="image/*"
            className="hidden"
            disabled={isUploading}
            onChange={event => void handleAvatarChange(event)}
          />
          <button
            type="button"
            className={cn(
              'relative block size-full overflow-hidden rounded-full',
              'focus-visible:ring-[3px] focus-visible:ring-ring/50 focus-visible:outline-1 focus-visible:outline-ring',
              'disabled:opacity-70'
            )}
            disabled={isUploading}
            onClick={() => fileRef.current?.click()}
          >
            <Avatar className="size-full">
              {profile.avatar ? <AvatarImage alt={profile.name} src={profile.avatar} /> : null}
              <AvatarFallback>{profile.initials}</AvatarFallback>
            </Avatar>
            <span
              className={cn(
                'absolute inset-0 flex flex-col items-center justify-center gap-1 bg-black/45 text-white',
                'opacity-0 transition-opacity group-hover:opacity-100 group-focus-visible:opacity-100'
              )}
            >
              {isUploading ? <Spinner /> : <Camera className="size-4" />}
              <span className="text-[10px] leading-none">更换头像</span>
            </span>
          </button>
        </div>

        <div className="flex min-w-0 flex-col gap-2">
          <div className="flex flex-col gap-0.5">
            <h1 className="truncate font-heading text-xl leading-6 font-semibold tracking-tight sm:text-2xl sm:leading-7">
              {profile.name}
            </h1>
            <p className="truncate text-sm leading-5 text-muted-foreground">{profile.workEmail}</p>
          </div>
        </div>
      </div>
    </div>
  )
}
