import * as React from 'react'

import { createFileRoute, useNavigate } from '@tanstack/react-router'
import { Globe, HardDrive, Lock, Mail, MessageSquare, ShieldCheck, Smartphone } from 'lucide-react'
import { z } from 'zod'

import { Tabs, TabsContent, TabsIndicator, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { usePermission } from '@/hooks/use-permission'

import { ConfigClient } from './-components/client/config-client'
import { ConfigLogin } from './-components/config-login'
import { ConfigMail } from './-components/config-mail'
import { ConfigSecurity } from './-components/config-security'
import { ConfigSite } from './-components/config-site'
import { ConfigSms } from './-components/sms/config-sms'
import { ConfigStorage } from './-components/storage/config-storage'

const searchSchema = z.object({
  tab: z.string().optional()
})

export const Route = createFileRoute('/(main)/(app)/system/config')({
  validateSearch: searchSchema,
  component: SystemConfigPage
})

interface ConfigTabItem {
  key: string
  name: string
  icon: React.ReactNode
  permission: string
  component: React.ComponentType
}

const ALL_TABS: ConfigTabItem[] = [
  {
    key: 'site',
    name: '网站配置',
    icon: <Globe className="size-4" />,
    permission: 'system:siteConfig:get',
    component: ConfigSite
  },
  {
    key: 'security',
    name: '安全配置',
    icon: <ShieldCheck className="size-4" />,
    permission: 'system:securityConfig:get',
    component: ConfigSecurity
  },
  {
    key: 'login',
    name: '登录配置',
    icon: <Lock className="size-4" />,
    permission: 'system:loginConfig:get',
    component: ConfigLogin
  },
  {
    key: 'mail',
    name: '邮件配置',
    icon: <Mail className="size-4" />,
    permission: 'system:mailConfig:get',
    component: ConfigMail
  },
  {
    key: 'sms',
    name: '短信配置',
    icon: <MessageSquare className="size-4" />,
    permission: 'system:smsConfig:list',
    component: ConfigSms
  },
  {
    key: 'storage',
    name: '存储配置',
    icon: <HardDrive className="size-4" />,
    permission: 'system:storage:list',
    component: ConfigStorage
  },
  {
    key: 'client',
    name: '客户端配置',
    icon: <Smartphone className="size-4" />,
    permission: 'system:client:list',
    component: ConfigClient
  }
]

function SystemConfigPage() {
  const { tab: queryTab } = Route.useSearch()
  const navigate = useNavigate({ from: Route.fullPath })
  const { hasPermission } = usePermission()

  const visibleTabs = React.useMemo(
    () => ALL_TABS.filter(tab => hasPermission(tab.permission)),
    [hasPermission]
  )

  const activeTab = visibleTabs.some(tab => tab.key === queryTab) ? queryTab : visibleTabs[0]?.key

  const handleTabChange = (key: string) => {
    navigate({
      search: (prev: Record<string, unknown>) => ({ ...prev, tab: key })
    })
  }

  if (activeTab === undefined) {
    return (
      <div className="flex h-64 items-center justify-center text-sm text-muted-foreground">暂无系统配置访问权限</div>
    )
  }

  return (
    <div className="flex flex-col gap-4 pb-4" data-content-padding="false">
      <div className="px-4 pt-4 md:px-6">
        <h1 className="text-xl font-semibold tracking-tight">系统配置</h1>
        <p className="mt-1 text-sm text-muted-foreground">管理系统基础信息和默认配置</p>
      </div>

      <Tabs value={activeTab} onValueChange={handleTabChange} className="min-h-0 flex-1 gap-0">
        <div className="touch-pan-x scrollbar-none overflow-x-auto overscroll-x-contain border-y">
          <TabsList
            className="w-max min-w-full justify-start gap-4 px-4 *:data-[slot=tabs-trigger]:flex-none"
            variant="line"
          >
            {visibleTabs.map(tab => (
              <TabsTrigger key={tab.key} value={tab.key} className="gap-2 after:hidden">
                {tab.icon}
                {tab.name}
              </TabsTrigger>
            ))}
            <TabsIndicator />
          </TabsList>
        </div>

        <div className="px-4 md:px-6">
          {visibleTabs.map(tab => {
            const Component = tab.component
            return (
              <TabsContent key={tab.key} value={tab.key} className="py-4">
                <Component />
              </TabsContent>
            )
          })}
        </div>
      </Tabs>
    </div>
  )
}
