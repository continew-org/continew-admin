import { Badge } from '@/components/ui/badge'
import { Separator } from '@/components/ui/separator'

import type { ProfileRecord } from './profile-data'
import { ProfileSocialAccounts } from './profile-social-accounts'

interface ProfileOverviewProps {
  profile: ProfileRecord
}

export function ProfileOverview({ profile }: ProfileOverviewProps) {
  return (
    <>
      <div className="flex flex-col gap-2">
        <h2 className="font-heading text-base font-medium">描述</h2>
        <p className="text-sm text-muted-foreground">{profile.bio}</p>
      </div>

      <Separator className="my-4" />

      <div className="flex flex-col gap-2">
        <h2 className="font-heading text-base font-medium">账号详情</h2>
        <div className="grid gap-x-8 gap-y-4 sm:grid-cols-2">
          <div className="flex flex-col gap-1">
            <span className="text-xs text-muted-foreground">用户 ID</span>
            <span className="text-sm">{profile.contractorId}</span>
          </div>
          <div className="flex flex-col gap-1">
            <span className="text-xs text-muted-foreground">部门</span>
            <span className="text-sm">{profile.department}</span>
          </div>
          <div className="flex flex-col gap-1">
            <span className="text-xs text-muted-foreground">创建时间</span>
            <span className="text-sm">{profile.startDate}</span>
          </div>
          <div className="flex flex-col gap-1">
            <span className="text-xs text-muted-foreground">账户活跃时间</span>
            <span className="text-sm">{profile.activeDuration}</span>
          </div>
        </div>
      </div>

      <Separator className="my-4" />

      <div className="grid gap-8 sm:grid-cols-2 xl:gap-12">
        <div className="flex flex-col gap-2">
          <h2 className="font-heading text-base font-medium">角色信息</h2>
          {profile.roleNames.length ? (
            <div className="flex flex-wrap gap-2">
              {profile.roleNames.map(role => (
                <Badge key={role} className="rounded-sm" variant="secondary">
                  {role}
                </Badge>
              ))}
            </div>
          ) : (
            <p className="text-sm text-muted-foreground">暂无角色</p>
          )}
        </div>

        <ProfileSocialAccounts />
      </div>
    </>
  )
}
