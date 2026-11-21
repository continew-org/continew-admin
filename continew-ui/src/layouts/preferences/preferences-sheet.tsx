import { useEffect, useState } from 'react'

import { cn } from 'cn'
import { Copy, Pin, PinOff, RotateCcw } from 'lucide-react'
import { useShallow } from 'zustand/react/shallow'

import { Button } from '@/components/ui/button'
import { Sheet, SheetContent, SheetDescription, SheetHeader, SheetTitle } from '@/components/ui/sheet'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { toast } from '@/components/ui/toast'
import { Tooltip, TooltipContent, TooltipTrigger } from '@/components/ui/tooltip'
import { useLogout } from '@/hooks/use-logout'
import { PREFERENCE_DEFAULTS, PREFERENCE_KEYS } from '@/lib/preferences/preferences-config'
import { usePreferencesStore, usePreferencesStoreApi } from '@/stores/preferences/preferences-provider'

import { useShellStore } from '../shell-store'
import {
  AppearancePreferences,
  GeneralPreferences,
  LayoutPreferences,
  ShortcutPreferences
} from './preference-sections'

const TABS = [
  { value: 'appearance', label: '外观', Content: AppearancePreferences },
  { value: 'layout', label: '布局', Content: LayoutPreferences },
  { value: 'shortcut', label: '快捷键', Content: ShortcutPreferences },
  { value: 'general', label: '通用', Content: GeneralPreferences }
] as const

/** True while any preference differs from its default; recomputed only when the values change. */
function useHasChanges() {
  return usePreferencesStore(state => PREFERENCE_KEYS.some(key => state.values[key] !== PREFERENCE_DEFAULTS[key]))
}

export function PreferencesSheet() {
  const open = useShellStore(state => state.preferencesOpen)
  const setOpen = useShellStore(state => state.setPreferencesOpen)
  const [mounted, setMounted] = useState(false)
  // Lives outside the sheet content, so the chosen tab survives closing and reopening the drawer.
  const [tab, setTab] = useState<string>('appearance')
  const store = usePreferencesStoreApi()
  const logout = useLogout()
  const hasChanges = useHasChanges()
  const { sticky, canCopy, setPreference, resetPreferences } = usePreferencesStore(
    useShallow(state => ({
      sticky: state.values.app_enable_sticky_preferences_nav,
      canCopy: state.values.app_enable_copy_preferences,
      setPreference: state.setPreference,
      resetPreferences: state.resetPreferences
    }))
  )

  const copy = async () => {
    const { values } = store.getState()
    const diff = Object.fromEntries(
      PREFERENCE_KEYS.filter(key => values[key] !== PREFERENCE_DEFAULTS[key]).map(key => [key, values[key]])
    )
    try {
      await navigator.clipboard.writeText(JSON.stringify(diff, null, 2))
      toast.add({ title: '已复制偏好配置', description: '可粘贴到 preferences-config.ts 作为默认值', type: 'success' })
    } catch {
      toast.add({ title: '复制失败，请检查浏览器剪贴板权限', type: 'error' })
    }
  }

  const clearAndLogout = () => {
    resetPreferences()
    setOpen(false)
    void logout()
  }

  useEffect(() => {
    setMounted(true)
  }, [])

  return (
    <Sheet open={open && mounted} onOpenChange={setOpen}>
      <SheetContent className="w-[400px] gap-0 sm:max-w-[400px]" showCloseButton>
        <SheetHeader className="border-b pr-24">
          <SheetTitle>偏好设置</SheetTitle>
          <SheetDescription>自定义偏好设置，实时预览</SheetDescription>
          <div className="absolute top-3 right-11 flex items-center gap-0.5">
            <Tooltip>
              <TooltipTrigger
                render={
                  <Button
                    variant="ghost"
                    size="icon-sm"
                    aria-label="恢复默认"
                    disabled={!hasChanges}
                    onClick={resetPreferences}
                    className="relative"
                  />
                }
              >
                {hasChanges && <span className="absolute top-1 right-1 size-1.5 rounded-full bg-primary" />}
                <RotateCcw />
              </TooltipTrigger>
              <TooltipContent>恢复默认</TooltipContent>
            </Tooltip>
            <Tooltip>
              <TooltipTrigger
                render={
                  <Button
                    variant="ghost"
                    size="icon-sm"
                    aria-label={sticky ? '取消固定导航' : '固定导航'}
                    onClick={() => setPreference('app_enable_sticky_preferences_nav', !sticky)}
                  />
                }
              >
                {sticky ? <PinOff /> : <Pin />}
              </TooltipTrigger>
              <TooltipContent>{sticky ? '取消固定导航' : '固定导航'}</TooltipContent>
            </Tooltip>
          </div>
        </SheetHeader>

        <Tabs value={tab} onValueChange={value => setTab(String(value))} className="min-h-0 flex-1 gap-0">
          <div className="min-h-0 flex-1 overflow-y-auto overscroll-contain">
            <div className={cn('bg-popover px-4 pt-3 pb-2', sticky && 'sticky top-0 z-10')}>
              <TabsList className="grid w-full grid-cols-4">
                {TABS.map(tab => (
                  <TabsTrigger key={tab.value} value={tab.value}>
                    {tab.label}
                  </TabsTrigger>
                ))}
              </TabsList>
            </div>
            {TABS.map(({ value, Content }) => (
              <TabsContent key={value} value={value} className="px-3 pb-6">
                <Content />
              </TabsContent>
            ))}
          </div>
        </Tabs>

        <div className={cn('grid gap-3 border-t p-4', canCopy ? 'grid-cols-2' : 'grid-cols-1')}>
          {canCopy && (
            <Button disabled={!hasChanges} onClick={() => void copy()}>
              <Copy />
              复制偏好设置
            </Button>
          )}
          <Button variant="ghost" onClick={clearAndLogout}>
            清空缓存 &amp; 退出登录
          </Button>
        </div>
      </SheetContent>
    </Sheet>
  )
}
