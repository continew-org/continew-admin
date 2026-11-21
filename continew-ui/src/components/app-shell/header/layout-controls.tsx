import { useState } from 'react'

import { Tabs } from '@animate-ui/react/primitives/base-ui/tabs'
import { Settings } from 'lucide-react'
import { useShallow } from 'zustand/react/shallow'

import { EntityDrawer } from '@/components/entity-drawer'
import { Button } from '@/components/ui/button'
import { DrawerFooter } from '@/components/ui/drawer'
import { Label } from '@/components/ui/label'
import { Input } from '@/components/ui/input'
import { Kbd, KbdGroup } from '@/components/ui/kbd'
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Switch } from '@/components/ui/switch'
import { ToggleGroup, ToggleGroupItem } from '@/components/ui/toggle-group'
import { Tooltip, TooltipContent, TooltipTrigger } from '@/components/ui/tooltip'
import {
  type ContentLayout,
  type NavbarStyle,
  type Radius,
  RADIUS_OPTIONS,
  type SidebarCollapsible,
  type SidebarVariant,
  TABBAR_STYLE_OPTIONS,
  type TabbarStyle
} from '@/lib/preferences/layout'
import { THEME_COLOR_OPTIONS, type ThemeColor, type ThemeMode } from '@/lib/preferences/theme'
import { useLogout } from '@/hooks/use-logout'
import { usePreferencesStore } from '@/stores/preferences/preferences-provider'

import { LayoutModePicker } from './layout-mode-picker'

/** Discrete root font sizes offered in the panel; the underlying preference stays a number. */
const FONT_SIZE_OPTIONS = [
  { label: '小', value: '14' },
  { label: '标准', value: '16' },
  { label: '大', value: '18' }
] as const

/** Trigger styling shared by the preference tabs, mirroring the design-system ui/tabs look. */
const TAB_TRIGGER_CLASS =
  'relative inline-flex h-[calc(100%-1px)] items-center justify-center rounded-md px-1.5 py-0.5 text-sm font-medium whitespace-nowrap text-foreground/60 transition-colors hover:text-foreground data-active:bg-background data-active:text-foreground data-active:shadow-sm'

/** Platform modifier key for the search shortcut hint. */
const MODIFIER_KEY = /Mac|iPhone|iPad|iPod/.test(navigator.platform) ? '⌘' : 'Ctrl'

function PreferenceField({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div className="space-y-1.5">
      <Label>{label}</Label>
      {children}
    </div>
  )
}

function PreferenceSection({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <section className="space-y-3">
      <h4 className="leading-none font-bold">{title}</h4>
      {children}
    </section>
  )
}

function PreferenceToggle({
  label,
  checked,
  disabled,
  onChange
}: {
  label: string
  checked: boolean
  disabled?: boolean
  onChange: (checked: boolean) => void
}) {
  return (
    <div className="flex items-center justify-between gap-3">
      <Label>{label}</Label>
      <Switch checked={checked} disabled={disabled} onCheckedChange={onChange} />
    </div>
  )
}

function ShortcutRow({
  label,
  keys,
  checked,
  disabled,
  onChange
}: {
  label: string
  keys: readonly string[]
  checked: boolean
  disabled?: boolean
  onChange: (checked: boolean) => void
}) {
  return (
    <div className="flex items-center justify-between gap-3">
      <div className="flex items-center gap-2">
        <Label>{label}</Label>
        <KbdGroup>
          {keys.map(key => (
            <Kbd key={key}>{key}</Kbd>
          ))}
        </KbdGroup>
      </div>
      <Switch checked={checked} disabled={disabled} onCheckedChange={onChange} />
    </div>
  )
}

export function LayoutControls() {
  const { values, resolvedThemeMode, setPreference, resetPreferences } = usePreferencesStore(
    useShallow(state => ({
      values: state.values,
      resolvedThemeMode: state.resolvedThemeMode,
      setPreference: state.setPreference,
      resetPreferences: state.resetPreferences
    }))
  )

  const {
    theme_mode: themeMode,
    theme_color: themeColor,
    color_weak_mode: colorWeak,
    color_gray_mode: colorGray,
    layout: layoutMode,
    content_layout: contentLayout,
    navbar_style: navbarStyle,
    sidebar_variant: variant,
    sidebar_collapsible: collapsible,
    radius,
    font_size: fontSize,
    tabbar_enable: tabbarEnable,
    tabbar_style: tabbarStyle,
    tabbar_height: tabbarHeight,
    tabbar_max_count: tabbarMaxCount,
    tabbar_persist: tabbarPersist,
    tabbar_draggable: tabbarDraggable,
    tabbar_wheelable: tabbarWheelable,
    tabbar_middle_click_close: tabbarMiddleClick,
    tabbar_visit_history: tabbarVisitHistory,
    tabbar_show_icon: tabbarShowIcon,
    tabbar_show_more: tabbarShowMore,
    widget_notification: widgetNotification,
    widget_lock_screen: widgetLockScreen,
    shortcut_enable: shortcutEnabled,
    shortcut_search: shortcutSearch,
    shortcut_lock_screen: shortcutLock,
    shortcut_logout: shortcutLogout,
    app_dynamic_title: dynamicTitle,
    app_watermark: watermarkEnabled,
    app_watermark_content: watermarkContent,
    app_enable_check_updates: checkUpdates
  } = values

  const logout = useLogout()
  const [open, setOpen] = useState(false)
  const clearAndLogout = () => {
    resetPreferences()
    void logout()
  }

  const themeColorItems = THEME_COLOR_OPTIONS.map(color => ({
    value: color.value,
    label: (
      <span className="flex items-center gap-1.5">
        <span
          className="size-2.5 rounded-full"
          style={{ backgroundColor: resolvedThemeMode === 'dark' ? color.primary.dark : color.primary.light }}
        />
        {color.label}
      </span>
    )
  }))

  return (
    <>
      <Tooltip>
        <TooltipTrigger
          render={<Button size="icon-sm" variant="outline" aria-label="偏好设置" onClick={() => setOpen(true)} />}
        >
          <Settings />
        </TooltipTrigger>
        <TooltipContent side="bottom" sideOffset={8}>
          偏好设置
        </TooltipContent>
      </Tooltip>
      <EntityDrawer
        open={open}
        onClose={() => setOpen(false)}
        title="偏好设置"
        icon={<Settings className="size-4" />}
        description="自定义仪表盘的外观与布局"
        width={30}
      >
        <Tabs.Root defaultValue="appearance" className="flex min-h-0 flex-1 flex-col gap-0">
          <div className="px-4 pt-3 pb-1">
            <Tabs.List className="grid h-8 w-full grid-cols-4 items-center rounded-lg bg-muted p-[3px] text-sm text-muted-foreground">
              <Tabs.Tab value="appearance" className={TAB_TRIGGER_CLASS}>
                外观
              </Tabs.Tab>
              <Tabs.Tab value="layout" className={TAB_TRIGGER_CLASS}>
                布局
              </Tabs.Tab>
              <Tabs.Tab value="tabbar" className={TAB_TRIGGER_CLASS}>
                标签栏
              </Tabs.Tab>
              <Tabs.Tab value="general" className={TAB_TRIGGER_CLASS}>
                通用
              </Tabs.Tab>
            </Tabs.List>
          </div>
          <div className="min-h-0 flex-1 overflow-y-auto overscroll-contain px-4 pb-4">
            <Tabs.Panel value="appearance" className="space-y-6 py-3">
              <PreferenceSection title="主题">
                <PreferenceField label="主题模式">
                  <ToggleGroup
                    size="sm"
                    spacing={0}
                    variant="outline"
                    className="w-full **:data-[slot=toggle-group-item]:flex-1 **:data-[slot=toggle-group-item]:text-xs"
                    value={[themeMode]}
                    onValueChange={([mode]) => {
                      if (!mode) return
                      setPreference('theme_mode', mode as ThemeMode)
                    }}
                  >
                    <ToggleGroupItem value="light" aria-label="切换浅色">
                      浅色
                    </ToggleGroupItem>
                    <ToggleGroupItem value="dark" aria-label="切换深色">
                      深色
                    </ToggleGroupItem>
                    <ToggleGroupItem value="system" aria-label="跟随系统">
                      跟随系统
                    </ToggleGroupItem>
                  </ToggleGroup>
                </PreferenceField>

                <PreferenceField label="主题色">
                  <Select
                    items={themeColorItems}
                    value={themeColor}
                    onValueChange={value => {
                      if (!value) return
                      setPreference('theme_color', value as ThemeColor)
                    }}
                  >
                    <SelectTrigger size="sm" className="w-full text-xs">
                      <SelectValue className="items-center" placeholder="选择颜色" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectGroup>
                        {THEME_COLOR_OPTIONS.map(color => (
                          <SelectItem key={color.value} className="text-xs" value={color.value}>
                            <span className="flex items-center gap-2">
                              <span
                                className="size-2.5 rounded-full"
                                style={{
                                  backgroundColor:
                                    resolvedThemeMode === 'dark' ? color.primary.dark : color.primary.light
                                }}
                              />
                              {color.label}
                            </span>
                          </SelectItem>
                        ))}
                      </SelectGroup>
                    </SelectContent>
                  </Select>
                </PreferenceField>

                <PreferenceField label="字号">
                  <ToggleGroup
                    size="sm"
                    spacing={0}
                    variant="outline"
                    className="w-full **:data-[slot=toggle-group-item]:flex-1 **:data-[slot=toggle-group-item]:text-xs"
                    value={[String(fontSize)]}
                    onValueChange={([size]) => {
                      if (!size) return
                      setPreference('font_size', Number(size))
                    }}
                  >
                    {FONT_SIZE_OPTIONS.map(option => (
                      <ToggleGroupItem key={option.value} value={option.value}>
                        {option.label}
                      </ToggleGroupItem>
                    ))}
                  </ToggleGroup>
                </PreferenceField>

                <PreferenceField label="圆角">
                  <ToggleGroup
                    size="sm"
                    spacing={0}
                    variant="outline"
                    className="w-full **:data-[slot=toggle-group-item]:flex-1 **:data-[slot=toggle-group-item]:text-xs"
                    value={[radius]}
                    onValueChange={([nextRadius]) => {
                      if (!nextRadius) return
                      setPreference('radius', nextRadius as Radius)
                    }}
                  >
                    {RADIUS_OPTIONS.map(option => (
                      <ToggleGroupItem key={option.value} value={option.value}>
                        {option.label}
                      </ToggleGroupItem>
                    ))}
                  </ToggleGroup>
                </PreferenceField>
              </PreferenceSection>

              <PreferenceSection title="其他">
                <PreferenceToggle
                  label="色弱模式"
                  checked={colorWeak}
                  onChange={checked => setPreference('color_weak_mode', checked)}
                />
                <PreferenceToggle
                  label="灰色模式"
                  checked={colorGray}
                  onChange={checked => setPreference('color_gray_mode', checked)}
                />
              </PreferenceSection>
            </Tabs.Panel>

            <Tabs.Panel value="layout" className="py-3">
              <PreferenceSection title="布局">
                <div className="space-y-1.5">
                  <Label className="text-xs font-medium">布局模式</Label>
                  <LayoutModePicker value={layoutMode} onChange={mode => setPreference('layout', mode)} />
                </div>
                <PreferenceField label="页面布局">
                  <ToggleGroup
                    size="sm"
                    spacing={0}
                    variant="outline"
                    className="w-full **:data-[slot=toggle-group-item]:flex-1 **:data-[slot=toggle-group-item]:text-xs"
                    value={[contentLayout]}
                    onValueChange={([layout]) => {
                      if (!layout) return
                      setPreference('content_layout', layout as ContentLayout)
                    }}
                  >
                    <ToggleGroupItem value="centered" aria-label="居中布局">
                      居中
                    </ToggleGroupItem>
                    <ToggleGroupItem value="full-width" aria-label="全宽布局">
                      全宽
                    </ToggleGroupItem>
                  </ToggleGroup>
                </PreferenceField>

                <PreferenceField label="导航栏行为">
                  <ToggleGroup
                    size="sm"
                    spacing={0}
                    variant="outline"
                    className="w-full **:data-[slot=toggle-group-item]:flex-1 **:data-[slot=toggle-group-item]:text-xs"
                    value={[navbarStyle]}
                    onValueChange={([style]) => {
                      if (!style) return
                      setPreference('navbar_style', style as NavbarStyle)
                    }}
                  >
                    <ToggleGroupItem value="sticky" aria-label="吸顶导航">
                      吸顶
                    </ToggleGroupItem>
                    <ToggleGroupItem value="scroll" aria-label="滚动导航">
                      跟随滚动
                    </ToggleGroupItem>
                  </ToggleGroup>
                </PreferenceField>

                <PreferenceField label="侧边栏样式">
                  <ToggleGroup
                    size="sm"
                    spacing={0}
                    variant="outline"
                    className="w-full **:data-[slot=toggle-group-item]:flex-1 **:data-[slot=toggle-group-item]:text-xs"
                    value={[variant]}
                    onValueChange={([nextVariant]) => {
                      if (!nextVariant) return
                      setPreference('sidebar_variant', nextVariant as SidebarVariant)
                    }}
                  >
                    <ToggleGroupItem value="inset" aria-label="内嵌侧边栏">
                      内嵌
                    </ToggleGroupItem>
                    <ToggleGroupItem value="sidebar" aria-label="侧边栏">
                      侧边
                    </ToggleGroupItem>
                    <ToggleGroupItem value="floating" aria-label="悬浮侧边栏">
                      悬浮
                    </ToggleGroupItem>
                  </ToggleGroup>
                </PreferenceField>

                <PreferenceField label="侧边栏折叠方式">
                  <ToggleGroup
                    size="sm"
                    spacing={0}
                    variant="outline"
                    className="w-full **:data-[slot=toggle-group-item]:flex-1 **:data-[slot=toggle-group-item]:text-xs"
                    value={[collapsible]}
                    onValueChange={([nextCollapsible]) => {
                      if (!nextCollapsible) return
                      setPreference('sidebar_collapsible', nextCollapsible as SidebarCollapsible)
                    }}
                  >
                    <ToggleGroupItem value="icon" aria-label="图标折叠">
                      图标
                    </ToggleGroupItem>
                    <ToggleGroupItem value="offcanvas" aria-label="抽屉折叠">
                      抽屉
                    </ToggleGroupItem>
                  </ToggleGroup>
                </PreferenceField>
              </PreferenceSection>
            </Tabs.Panel>

            <Tabs.Panel value="tabbar" className="py-3">
              <PreferenceSection title="标签栏">
                <PreferenceToggle
                  label="启用标签栏"
                  checked={tabbarEnable}
                  onChange={checked => setPreference('tabbar_enable', checked)}
                />
                <PreferenceToggle
                  label="持久化标签页"
                  checked={tabbarPersist}
                  disabled={!tabbarEnable}
                  onChange={checked => setPreference('tabbar_persist', checked)}
                />
                <PreferenceToggle
                  label="访问历史记录"
                  checked={tabbarVisitHistory}
                  disabled={!tabbarEnable}
                  onChange={checked => setPreference('tabbar_visit_history', checked)}
                />
                <PreferenceField label="标签风格">
                  <ToggleGroup
                    size="sm"
                    spacing={0}
                    variant="outline"
                    className="w-full **:data-[slot=toggle-group-item]:flex-1 **:data-[slot=toggle-group-item]:text-xs"
                    value={[tabbarStyle]}
                    onValueChange={([style]) => {
                      if (!style) return
                      setPreference('tabbar_style', style as TabbarStyle)
                    }}
                  >
                    {TABBAR_STYLE_OPTIONS.map(option => (
                      <ToggleGroupItem key={option.value} value={option.value}>
                        {option.label}
                      </ToggleGroupItem>
                    ))}
                  </ToggleGroup>
                </PreferenceField>
                <div className="grid grid-cols-2 gap-3">
                  <PreferenceField label="最大标签数">
                    <Input
                      type="number"
                      min={0}
                      max={50}
                      value={tabbarMaxCount}
                      disabled={!tabbarEnable}
                      className="h-8 text-xs"
                      onChange={event => {
                        const value = Number(event.target.value)
                        setPreference(
                          'tabbar_max_count',
                          Number.isFinite(value) ? Math.min(Math.max(Math.round(value), 0), 50) : 0
                        )
                      }}
                    />
                  </PreferenceField>
                  <PreferenceField label="高度">
                    <Input
                      type="number"
                      min={28}
                      max={56}
                      value={tabbarHeight}
                      disabled={!tabbarEnable}
                      className="h-8 text-xs"
                      onChange={event => {
                        const value = Number(event.target.value)
                        setPreference(
                          'tabbar_height',
                          Number.isFinite(value) ? Math.min(Math.max(Math.round(value), 28), 56) : 38
                        )
                      }}
                    />
                  </PreferenceField>
                </div>
                <PreferenceToggle
                  label="拖拽排序"
                  checked={tabbarDraggable}
                  disabled={!tabbarEnable}
                  onChange={checked => setPreference('tabbar_draggable', checked)}
                />
                <PreferenceToggle
                  label="纵向滚轮横滚"
                  checked={tabbarWheelable}
                  disabled={!tabbarEnable}
                  onChange={checked => setPreference('tabbar_wheelable', checked)}
                />
                <PreferenceToggle
                  label="中键关闭标签"
                  checked={tabbarMiddleClick}
                  disabled={!tabbarEnable}
                  onChange={checked => setPreference('tabbar_middle_click_close', checked)}
                />
                <PreferenceToggle
                  label="显示图标"
                  checked={tabbarShowIcon}
                  disabled={!tabbarEnable}
                  onChange={checked => setPreference('tabbar_show_icon', checked)}
                />
                <PreferenceToggle
                  label="显示更多按钮"
                  checked={tabbarShowMore}
                  disabled={!tabbarEnable}
                  onChange={checked => setPreference('tabbar_show_more', checked)}
                />
              </PreferenceSection>
            </Tabs.Panel>

            <Tabs.Panel value="general" className="space-y-6 py-3">
              <PreferenceSection title="通用">
                <PreferenceToggle
                  label="动态标题"
                  checked={dynamicTitle}
                  onChange={checked => setPreference('app_dynamic_title', checked)}
                />
                <PreferenceToggle
                  label="水印"
                  checked={watermarkEnabled}
                  onChange={checked => setPreference('app_watermark', checked)}
                />
                <PreferenceField label="水印文案">
                  <Input
                    value={watermarkContent}
                    disabled={!watermarkEnabled}
                    maxLength={64}
                    placeholder="默认使用用户名"
                    className="h-8 text-xs"
                    onChange={event => setPreference('app_watermark_content', event.target.value)}
                  />
                </PreferenceField>
                <PreferenceToggle
                  label="定时检查更新"
                  checked={checkUpdates}
                  onChange={checked => setPreference('app_enable_check_updates', checked)}
                />
              </PreferenceSection>

              <PreferenceSection title="小部件">
                <PreferenceToggle
                  label="显示通知"
                  checked={widgetNotification}
                  onChange={checked => setPreference('widget_notification', checked)}
                />
                <PreferenceToggle
                  label="启用锁屏"
                  checked={widgetLockScreen}
                  onChange={checked => setPreference('widget_lock_screen', checked)}
                />
              </PreferenceSection>

              <PreferenceSection title="快捷键">
                <PreferenceToggle
                  label="启用快捷键"
                  checked={shortcutEnabled}
                  onChange={checked => setPreference('shortcut_enable', checked)}
                />
                <ShortcutRow
                  label="全局搜索"
                  keys={[MODIFIER_KEY, 'J']}
                  checked={shortcutSearch}
                  disabled={!shortcutEnabled}
                  onChange={checked => setPreference('shortcut_search', checked)}
                />
                <ShortcutRow
                  label="锁定屏幕"
                  keys={['Alt', 'L']}
                  checked={shortcutLock}
                  disabled={!shortcutEnabled}
                  onChange={checked => setPreference('shortcut_lock_screen', checked)}
                />
                <ShortcutRow
                  label="退出登录"
                  keys={['Alt', 'Q']}
                  checked={shortcutLogout}
                  disabled={!shortcutEnabled}
                  onChange={checked => setPreference('shortcut_logout', checked)}
                />
              </PreferenceSection>
            </Tabs.Panel>
          </div>
        </Tabs.Root>

        <DrawerFooter className="grid grid-cols-2 gap-3">
          <Button type="button" variant="default" onClick={resetPreferences}>
            恢复默认值
          </Button>
          <Button type="button" variant="outline" onClick={clearAndLogout}>
            清空缓存 &amp; 退出登录
          </Button>
        </DrawerFooter>
      </EntityDrawer>
    </>
  )
}
