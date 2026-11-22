// Sidebar Variant
const SIDEBAR_VARIANT_OPTIONS = [
  { label: 'Sidebar', value: 'sidebar' },
  { label: 'Inset', value: 'inset' },
  { label: 'Floating', value: 'floating' }
] as const
export const SIDEBAR_VARIANT_VALUES = SIDEBAR_VARIANT_OPTIONS.map(v => v.value)
export type SidebarVariant = (typeof SIDEBAR_VARIANT_VALUES)[number]

// Sidebar Collapsible
const SIDEBAR_COLLAPSIBLE_OPTIONS = [
  { label: 'Icon', value: 'icon' },
  { label: 'Offcanvas', value: 'offcanvas' }
] as const
export const SIDEBAR_COLLAPSIBLE_VALUES = SIDEBAR_COLLAPSIBLE_OPTIONS.map(v => v.value)
export type SidebarCollapsible = (typeof SIDEBAR_COLLAPSIBLE_VALUES)[number]

// Content Layout
const CONTENT_LAYOUT_OPTIONS = [
  { label: 'Centered', value: 'centered' },
  { label: 'Full Width', value: 'full-width' }
] as const
export const CONTENT_LAYOUT_VALUES = CONTENT_LAYOUT_OPTIONS.map(v => v.value)
export type ContentLayout = (typeof CONTENT_LAYOUT_VALUES)[number]

// Navbar Style: sticky, scroll, auto (hide once scrolled past the header), auto-scroll (hide while scrolling down)
export const NAVBAR_STYLE_OPTIONS = [
  { label: '吸顶', value: 'sticky' },
  { label: '跟随滚动', value: 'scroll' },
  { label: '自动隐藏', value: 'auto' },
  { label: '下滑隐藏', value: 'auto-scroll' }
] as const
export const NAVBAR_STYLE_VALUES = NAVBAR_STYLE_OPTIONS.map(v => v.value)
export type NavbarStyle = (typeof NAVBAR_STYLE_VALUES)[number]

// Radius, as a share of the default corner token: 0 squares everything, 1 keeps the design default.
export const RADIUS_OPTIONS = [
  { label: '0', value: '0' },
  { label: '25%', value: '0.25' },
  { label: '50%', value: '0.5' },
  { label: '75%', value: '0.75' },
  { label: '100%', value: '1' }
] as const
export const RADIUS_VALUES = RADIUS_OPTIONS.map(v => v.value)
export type Radius = (typeof RADIUS_VALUES)[number]

// Layout mode
export const LAYOUT_MODE_OPTIONS = [
  { label: '经典', value: 'sidebar-tree-nav', description: '传统左侧折叠菜单，一级目录可展开收起' },
  { label: '垂直', value: 'sidebar-nav', description: '侧边菜单布局' },
  { label: '双列菜单', value: 'sidebar-mixed-nav', description: '垂直双列菜单模式' },
  { label: '水平', value: 'header-nav', description: '水平菜单模式，菜单全部显示在顶部' },
  { label: '侧边导航', value: 'header-sidebar-nav', description: '顶部通栏，侧边导航' },
  { label: '混合垂直', value: 'mixed-nav', description: '垂直水平菜单共存' },
  { label: '混合双列', value: 'header-mixed-nav', description: '双列、水平菜单共存模式' },
  { label: '内容全屏', value: 'full-content', description: '不显示任何菜单，只显示内容主体' }
] as const
export const LAYOUT_MODE_VALUES = LAYOUT_MODE_OPTIONS.map(v => v.value)
export type LayoutMode = (typeof LAYOUT_MODE_VALUES)[number]

export const HEADER_MENU_ALIGN_OPTIONS = [
  { label: '左侧', value: 'start' },
  { label: '居中', value: 'center' },
  { label: '右侧', value: 'end' }
] as const
export const HEADER_MENU_ALIGN_VALUES = HEADER_MENU_ALIGN_OPTIONS.map(v => v.value)

export const BREADCRUMB_STYLE_OPTIONS = [
  { label: '常规', value: 'normal' },
  { label: '背景', value: 'background' }
] as const
export const BREADCRUMB_STYLE_VALUES = BREADCRUMB_STYLE_OPTIONS.map(v => v.value)

export const NAVIGATION_STYLE_OPTIONS = [
  { label: '圆润', value: 'rounded' },
  { label: '朴素', value: 'plain' }
] as const
export const NAVIGATION_STYLE_VALUES = NAVIGATION_STYLE_OPTIONS.map(v => v.value)

export const TABBAR_STYLE_OPTIONS = [
  { label: '谷歌', value: 'chrome' },
  { label: '朴素', value: 'plain' },
  { label: '卡片', value: 'card' },
  { label: '轻快', value: 'brisk' }
] as const
export const TABBAR_STYLE_VALUES = TABBAR_STYLE_OPTIONS.map(v => v.value)
export type TabbarStyle = (typeof TABBAR_STYLE_VALUES)[number]

export const TRANSITION_NAME_OPTIONS = [
  { label: '渐隐', value: 'fade' },
  { label: '滑动', value: 'fade-slide' },
  { label: '上浮', value: 'fade-up' },
  { label: '下沉', value: 'fade-down' },
  { label: '抖动', value: 'fade-shake' }
] as const
export const TRANSITION_NAME_VALUES = TRANSITION_NAME_OPTIONS.map(v => v.value)
export type TransitionName = (typeof TRANSITION_NAME_VALUES)[number]

export const PREFERENCES_BUTTON_POSITION_OPTIONS = [
  { label: '自动', value: 'auto' },
  { label: '顶栏', value: 'header' },
  { label: '固定', value: 'fixed' },
  { label: '用户菜单', value: 'user-dropdown' }
] as const
export const PREFERENCES_BUTTON_POSITION_VALUES = PREFERENCES_BUTTON_POSITION_OPTIONS.map(v => v.value)
