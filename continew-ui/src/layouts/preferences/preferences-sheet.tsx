import { useEffect, useState } from 'react'

import { cn } from 'cn'
import { Copy, Pin, PinOff, RotateCcw } from 'lucide-react'
import { useShallow } from 'zustand/react/shallow'

import { Tabs } from '@base-ui/react/tabs'

import { Button } from '@/components/ui/button'
import { Sheet, SheetContent, SheetDescription, SheetHeader, SheetTitle } from '@/components/ui/sheet'
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

/**
 * Plain CSS motion: the active pill slides with a no-overshoot ease-out, and the incoming panel
 * fades up 4px. No height or blur morph, so the sheet never wobbles. `[data-animation='false']`
 * zeroes both durations.
 */
const TAB_TRIGGER_CLASS =
  'relative z-10 inline-flex h-full items-center justify-center rounded-md px-1.5 text-sm font-medium whitespace-nowrap text-foreground/60 transition-colors duration-200 hover:text-foreground focus-visible:ring-[3px] focus-visible:ring-ring/50 focus-visible:outline-none data-active:text-foreground'

const TAB_INDICATOR_CLASS =
  'absolute top-(--active-tab-top) left-(--active-tab-left) h-(--active-tab-height) w-(--active-tab-width) rounded-md bg-background shadow-sm transition-[left,width] duration-250 ease-[cubic-bezier(0.32,0.72,0,1)] dark:bg-input/30'

const TAB_PANEL_CLASS =
  'px-3 pb-6 outline-none motion-safe:animate-in motion-safe:fade-in-0 motion-safe:slide-in-from-bottom-1 motion-safe:duration-200 motion-safe:ease-out'

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

        <Tabs.Root
          value={tab}
          onValueChange={value => setTab(String(value))}
          className="flex min-h-0 flex-1 flex-col gap-0"
        >
          <div className="min-h-0 flex-1 overflow-y-auto overscroll-contain">
            <div className={cn('bg-popover px-4 pt-3 pb-2', sticky && 'sticky top-0 z-10')}>
              <Tabs.List className="relative grid h-8 w-full grid-cols-4 items-center rounded-lg bg-muted p-[3px] text-sm text-muted-foreground">
                {TABS.map(tab => (
                  <Tabs.Tab key={tab.value} value={tab.value} className={TAB_TRIGGER_CLASS}>
                    {tab.label}
                  </Tabs.Tab>
                ))}
                <Tabs.Indicator className={TAB_INDICATOR_CLASS} />
              </Tabs.List>
            </div>
            {TABS.map(({ value, Content }) => (
              <Tabs.Panel key={value} value={value} className={TAB_PANEL_CLASS}>
                <Content />
              </Tabs.Panel>
            ))}
          </div>
        </Tabs.Root>

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
