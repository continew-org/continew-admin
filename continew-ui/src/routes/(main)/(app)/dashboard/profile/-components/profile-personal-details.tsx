import { LockKeyhole } from 'lucide-react'

import { Badge } from '@/components/ui/badge'

import type { ProfileRecord } from './profile-data'

export function PersonalDetails({ profile }: { profile: ProfileRecord }) {
  return (
    <>
      <div className="flex flex-col gap-2">
        <div className="flex items-center gap-2">
          <h2 className="font-heading text-base font-medium">个人信息</h2>
          <Badge className="rounded-sm" variant="outline">
            <LockKeyhole data-icon="inline-start" />
            Private
          </Badge>
        </div>
        <dl className="grid gap-8 sm:grid-cols-2 xl:grid-cols-3 xl:gap-12">
          <div className="flex flex-col gap-1">
            <dt className="text-xs text-muted-foreground">用户名</dt>
            <dd className="text-sm">{profile.preferredName}</dd>
          </div>
          <div className="flex flex-col gap-1">
            <dt className="text-xs text-muted-foreground">昵称</dt>
            <dd className="text-sm">{profile.legalName}</dd>
          </div>
          <div className="flex flex-col gap-1">
            <dt className="text-xs text-muted-foreground">个人邮箱</dt>
            <dd className="text-sm">{profile.personalEmail}</dd>
          </div>
          <div className="flex flex-col gap-1">
            <dt className="text-xs text-muted-foreground">性别</dt>
            <dd className="text-sm">{profile.pronouns}</dd>
          </div>
          <div className="flex flex-col gap-1">
            <dt className="text-xs text-muted-foreground">手机号码</dt>
            <dd className="text-sm">{profile.workPhone}</dd>
          </div>
        </dl>
      </div>
    </>
  )
}
