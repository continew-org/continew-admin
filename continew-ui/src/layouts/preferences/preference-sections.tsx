import { useShallow } from 'zustand/react/shallow'

import {
  BREADCRUMB_STYLE_OPTIONS,
  HEADER_MENU_ALIGN_OPTIONS,
  NAVBAR_STYLE_OPTIONS,
  NAVIGATION_STYLE_OPTIONS,
  PREFERENCES_BUTTON_POSITION_OPTIONS,
  SIDEBAR_COLLAPSIBLE_VALUES,
  SIDEBAR_VARIANT_VALUES,
  TABBAR_STYLE_OPTIONS
} from '@/lib/preferences/layout'
import { usePreferencesStore } from '@/stores/preferences/preferences-provider'

import {
  PreferenceBlock,
  PreferenceNumber,
  PreferenceSegmented,
  PreferenceSwitch,
  PreferenceText
} from './preference-controls'
import {
  ContentModePicker,
  LayoutModePicker,
  RadiusPicker,
  SemanticColorFields,
  ThemeColorGrid,
  ThemeModePicker,
  TransitionPicker
} from './preference-pickers'

const SIDEBAR_VARIANT_LABELS = { inset: '内嵌', sidebar: '侧边', floating: '悬浮' } as const
const SIDEBAR_COLLAPSIBLE_LABELS = { icon: '图标', offcanvas: '抽屉' } as const

export function AppearancePreferences() {
  const { dark, layout, semiDarkSidebar } = usePreferencesStore(
    useShallow(state => ({
      dark: state.resolvedThemeMode === 'dark',
      layout: state.values.layout,
      semiDarkSidebar: state.values.semi_dark_sidebar
    }))
  )
  const twoColumn = layout === 'sidebar-mixed-nav' || layout === 'header-mixed-nav'

  return (
    <>
      <PreferenceBlock title="主题">
        <ThemeModePicker />
        <PreferenceSwitch
          field="semi_dark_sidebar"
          label="深色侧边栏"
          disabled={dark || layout === 'header-nav' || layout === 'full-content'}
        />
        <PreferenceSwitch
          field="semi_dark_sidebar_sub"
          label="深色二级侧边栏"
          disabled={dark || !twoColumn || !semiDarkSidebar}
        />
        <PreferenceSwitch field="semi_dark_header" label="深色顶栏" disabled={dark} />
      </PreferenceBlock>
      <PreferenceBlock title="内置主题">
        <ThemeColorGrid />
      </PreferenceBlock>
      <PreferenceBlock title="自定义颜色">
        <SemanticColorFields />
      </PreferenceBlock>
      <PreferenceBlock title="圆角">
        <RadiusPicker />
      </PreferenceBlock>
      <PreferenceBlock title="字体">
        <PreferenceNumber field="font_size" label="基础字号" />
      </PreferenceBlock>
      <PreferenceBlock title="其它">
        <PreferenceSwitch field="color_weak_mode" label="色弱模式" />
        <PreferenceSwitch field="color_gray_mode" label="灰色模式" />
      </PreferenceBlock>
    </>
  )
}

export function LayoutPreferences() {
  const v = usePreferencesStore(
    useShallow(state => ({
      layout: state.values.layout,
      contentLayout: state.values.content_layout,
      sidebarEnable: state.values.sidebar_enable,
      headerVisible: state.values.header_visible,
      tabbarEnable: state.values.tabbar_enable,
      breadcrumbEnable: state.values.breadcrumb_enable,
      footerEnable: state.values.footer_enable,
      transitionEnable: state.values.transition_enable,
      collapsible: state.values.sidebar_collapsible
    }))
  )
  const full = v.layout === 'full-content'
  const hasSidebar = !full && v.layout !== 'header-nav'
  const twoColumn = v.layout === 'sidebar-mixed-nav' || v.layout === 'header-mixed-nav'
  const hasHeaderMenu = ['header-nav', 'mixed-nav', 'header-mixed-nav'].includes(v.layout)
  const sidebarOff = !hasSidebar || !v.sidebarEnable
  // Mixed two-column never renders a breadcrumb (see base-layout).
  const noBreadcrumb = v.layout === 'header-mixed-nav'
  const breadcrumbOff = noBreadcrumb || !v.breadcrumbEnable

  return (
    <>
      <PreferenceBlock title="布局">
        <LayoutModePicker />
      </PreferenceBlock>
      <PreferenceBlock title="内容">
        <ContentModePicker />
        <PreferenceNumber field="content_compact_width" label="定宽宽度" disabled={v.contentLayout !== 'centered'} />
        <PreferenceNumber field="content_padding" label="内容边距" />
      </PreferenceBlock>
      <PreferenceBlock title="侧边栏">
        <PreferenceSwitch field="sidebar_enable" label="显示侧边栏" disabled={!hasSidebar} />
        <PreferenceSegmented
          field="sidebar_variant"
          label="样式"
          disabled={sidebarOff || twoColumn}
          options={SIDEBAR_VARIANT_VALUES.map(value => ({ value, label: SIDEBAR_VARIANT_LABELS[value] }))}
        />
        <PreferenceSegmented
          field="sidebar_collapsible"
          label="折叠方式"
          disabled={sidebarOff || twoColumn}
          options={SIDEBAR_COLLAPSIBLE_VALUES.map(value => ({ value, label: SIDEBAR_COLLAPSIBLE_LABELS[value] }))}
        />
        <PreferenceSwitch field="sidebar_collapsed_button" label="显示折叠按钮" disabled={sidebarOff || twoColumn} />
        <PreferenceSwitch
          field="sidebar_expand_on_hover"
          label="鼠标悬停展开"
          hint="折叠为图标时，悬停临时展开"
          disabled={sidebarOff || twoColumn || v.collapsible !== 'icon'}
        />
        <PreferenceSwitch
          field="sidebar_collapsed_show_title"
          label="折叠显示菜单名"
          disabled={sidebarOff || twoColumn || v.collapsible !== 'icon'}
        />
        <PreferenceSwitch field="sidebar_draggable" label="拖拽调整宽度" disabled={sidebarOff || twoColumn} />
        <PreferenceSwitch field="sidebar_fixed_button" label="显示固定按钮" disabled={sidebarOff || !twoColumn} />
        <PreferenceSwitch
          field="sidebar_auto_activate_child"
          label="自动激活子菜单"
          hint="点击一级菜单时打开其第一个页面"
          disabled={!twoColumn && v.layout !== 'mixed-nav'}
        />
        <PreferenceNumber field="sidebar_width" label="宽度" disabled={sidebarOff} />
        <PreferenceNumber field="sidebar_mixed_width" label="一级栏宽度" disabled={sidebarOff || !twoColumn} />
      </PreferenceBlock>
      <PreferenceBlock title="顶栏">
        <PreferenceSwitch field="header_visible" label="显示顶栏" disabled={full} />
        <PreferenceSegmented
          field="navbar_style"
          label="模式"
          options={NAVBAR_STYLE_OPTIONS}
          disabled={full || !v.headerVisible}
        />
        <PreferenceSegmented
          field="header_menu_align"
          label="菜单位置"
          options={HEADER_MENU_ALIGN_OPTIONS}
          disabled={!hasHeaderMenu || !v.headerVisible}
        />
        <PreferenceNumber field="header_height" label="高度" disabled={full || !v.headerVisible} />
      </PreferenceBlock>
      <PreferenceBlock title="导航菜单">
        <PreferenceSegmented
          field="navigation_style"
          label="导航菜单风格"
          options={NAVIGATION_STYLE_OPTIONS}
          disabled={full}
        />
        <PreferenceSwitch
          field="navigation_split"
          label="导航菜单分离"
          hint="顶部显示一级菜单，侧边显示其子菜单"
          disabled={v.layout !== 'mixed-nav'}
        />
        <PreferenceSwitch field="navigation_accordion" label="侧边导航手风琴" disabled={!hasSidebar} />
      </PreferenceBlock>
      <PreferenceBlock title="面包屑">
        <PreferenceSwitch
          field="breadcrumb_enable"
          label="开启面包屑"
          disabled={full || noBreadcrumb || !v.headerVisible}
        />
        <PreferenceSwitch field="breadcrumb_hide_only_one" label="仅一级时隐藏" disabled={breadcrumbOff} />
        <PreferenceSwitch field="breadcrumb_show_icon" label="显示面包屑图标" disabled={breadcrumbOff} />
        <PreferenceSwitch field="breadcrumb_show_home" label="显示首页按钮" disabled={breadcrumbOff} />
        <PreferenceSegmented
          field="breadcrumb_style"
          label="面包屑风格"
          options={BREADCRUMB_STYLE_OPTIONS}
          disabled={breadcrumbOff}
        />
      </PreferenceBlock>
      <PreferenceBlock title="标签栏">
        <PreferenceSwitch field="tabbar_enable" label="启用标签栏" disabled={full} />
        <PreferenceSwitch field="tabbar_persist" label="持久化标签页" disabled={!v.tabbarEnable} />
        <PreferenceSwitch
          field="tabbar_visit_history"
          label="访问历史记录"
          hint="关闭标签时回到上一个访问的页面"
          disabled={!v.tabbarEnable}
        />
        <PreferenceNumber field="tabbar_max_count" label="最大标签数" suffix="个" disabled={!v.tabbarEnable} />
        <PreferenceSwitch field="tabbar_draggable" label="启动拖拽排序" disabled={!v.tabbarEnable} />
        <PreferenceSwitch field="tabbar_wheelable" label="启用纵向滚轮响应" disabled={!v.tabbarEnable} />
        <PreferenceSwitch field="tabbar_middle_click_close" label="点击鼠标中键关闭标签页" disabled={!v.tabbarEnable} />
        <PreferenceSwitch field="tabbar_show_icon" label="显示标签页图标" disabled={!v.tabbarEnable} />
        <PreferenceSwitch field="tabbar_show_more" label="显示更多按钮" disabled={!v.tabbarEnable} />
        <PreferenceSwitch field="tabbar_show_refresh" label="显示刷新按钮" disabled={!v.tabbarEnable} />
        <PreferenceSwitch field="tabbar_show_maximize" label="显示最大化按钮" disabled={!v.tabbarEnable} />
        <PreferenceSegmented
          field="tabbar_style"
          label="标签页风格"
          options={TABBAR_STYLE_OPTIONS}
          disabled={!v.tabbarEnable}
        />
        <PreferenceNumber field="tabbar_height" label="高度" disabled={!v.tabbarEnable} />
      </PreferenceBlock>
      <PreferenceBlock title="小部件">
        <PreferenceSwitch field="widget_global_search" label="启用全局搜索" />
        <PreferenceSwitch field="widget_theme_toggle" label="启用主题切换" />
        <PreferenceSwitch field="widget_fullscreen" label="启用全屏" />
        <PreferenceSwitch field="widget_notification" label="启用通知" />
        <PreferenceSwitch field="widget_lock_screen" label="启用锁屏" />
        <PreferenceSwitch field="widget_sidebar_toggle" label="启用侧边栏切换" />
        <PreferenceSwitch field="widget_refresh" label="启用刷新" />
        <PreferenceSegmented
          field="app_preferences_button_position"
          label="偏好设置位置"
          options={PREFERENCES_BUTTON_POSITION_OPTIONS}
        />
      </PreferenceBlock>
      <PreferenceBlock title="页脚">
        <PreferenceSwitch
          field="footer_enable"
          label="显示页脚"
          hint="内容取自系统配置的版权与备案信息"
          disabled={full}
        />
        <PreferenceSwitch field="footer_fixed" label="固定在底部" disabled={full || !v.footerEnable} />
      </PreferenceBlock>
      <PreferenceBlock title="动画">
        <PreferenceSwitch field="animation_enable" label="启用动画" />
        <PreferenceSwitch field="transition_progress" label="页面切换进度条" />
        <PreferenceSwitch field="transition_loading" label="页面切换 Loading" />
        <PreferenceSwitch field="transition_enable" label="页面切换动画" />
        {v.transitionEnable && <TransitionPicker />}
      </PreferenceBlock>
    </>
  )
}

export function ShortcutPreferences() {
  const enabled = usePreferencesStore(state => state.values.shortcut_enable)

  return (
    <PreferenceBlock title="全局">
      <PreferenceSwitch field="shortcut_enable" label="快捷键" />
      <PreferenceSwitch field="shortcut_search" label="全局搜索" hint="Ctrl / ⌘ + J" disabled={!enabled} />
      <PreferenceSwitch field="shortcut_logout" label="退出登录" hint="Alt + Q" disabled={!enabled} />
      <PreferenceSwitch field="shortcut_lock_screen" label="锁定屏幕" hint="Alt + L" disabled={!enabled} />
      <PreferenceSwitch field="shortcut_escape" label="Esc 退出最大化" hint="Esc" disabled={!enabled} />
    </PreferenceBlock>
  )
}

export function GeneralPreferences() {
  const watermark = usePreferencesStore(state => state.values.app_watermark)

  return (
    <PreferenceBlock title="通用">
      <PreferenceSwitch field="app_dynamic_title" label="动态标题" hint="浏览器标题跟随当前页面" />
      <PreferenceSwitch field="app_watermark" label="水印" />
      <PreferenceText
        field="app_watermark_content"
        label="水印文案"
        placeholder="默认使用用户名"
        disabled={!watermark}
      />
      <PreferenceSwitch field="app_enable_check_updates" label="定时检查更新" />
      <PreferenceSwitch field="app_enable_copy_preferences" label="显示复制按钮" />
    </PreferenceBlock>
  )
}
