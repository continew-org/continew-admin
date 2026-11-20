import { createFileRoute } from '@tanstack/react-router'

import { Separator } from '@/components/ui/separator'
import { Tabs, TabsContent, TabsIndicator, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { useAuthStore } from '@/stores/auth/auth-provider'

import { PasswordForm } from './-components/password-form'
import { ProfileHeader } from './-components/profile-header'
import { ProfileOverview } from './-components/profile-overview'
import { PersonalDetails } from './-components/profile-personal-details'
import { toProfileRecord } from './-components/profile-record'
import { ProfileStatusSidebar } from './-components/profile-status-sidebar'

export const Route = createFileRoute('/(main)/(app)/dashboard/profile')({
  component: Page
})

function Page() {
  const user = useAuthStore(state => state.user)
  const profile = toProfileRecord(user)

  return (
    <div className="flex flex-col gap-4 pb-4" data-content-padding="false">
      <ProfileHeader profile={profile} />

      <Tabs className="min-h-0 flex-1 gap-0" defaultValue="overview">
        <div className="touch-pan-x scrollbar-none overflow-x-auto overscroll-x-contain border-y">
          <TabsList
            className="w-max min-w-full justify-start gap-4 px-4 *:data-[slot=tabs-trigger]:flex-none"
            variant="line"
          >
            <TabsTrigger value="overview" className="gap-2 after:hidden">
              总览
            </TabsTrigger>
            <TabsTrigger value="personal" className="gap-2 after:hidden">
              个人信息
            </TabsTrigger>
            <TabsTrigger value="security" className="gap-2 after:hidden">
              安全
            </TabsTrigger>
            <TabsIndicator />
          </TabsList>
        </div>

        <div className="px-4 md:px-6">
          <TabsContent value="overview">
            <div className="grid lg:grid-cols-[minmax(0,1fr)_auto_18rem]">
              <div className="py-4 lg:pr-6">
                <ProfileOverview profile={profile} />
              </div>
              <Separator className="hidden lg:block" orientation="vertical" />
              <div className="py-4 lg:pl-6">
                <ProfileStatusSidebar />
              </div>
            </div>
          </TabsContent>

          <TabsContent className="py-4" value="personal">
            <PersonalDetails profile={profile} />
          </TabsContent>

          <TabsContent className="py-4" value="security">
            <div className="max-w-md">
              <h2 className="font-heading text-base font-medium">修改密码</h2>
              <p className="mt-1 mb-4 text-sm text-muted-foreground">
                当前密码和新密码会在本地加密后提交，修改成功后所有设备都会退出登录。
              </p>
              <PasswordForm />
            </div>
          </TabsContent>
        </div>
      </Tabs>
    </div>
  )
}
