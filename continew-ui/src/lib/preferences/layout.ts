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

// Navbar Style
const NAVBAR_STYLE_OPTIONS = [
  { label: 'Sticky', value: 'sticky' },
  { label: 'Scroll', value: 'scroll' }
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

// Tabbar style
export const TABBAR_STYLE_OPTIONS = [
  { label: '朴素', value: 'plain' },
  { label: '卡片', value: 'card' },
  { label: '轻快', value: 'brisk' }
] as const
export const TABBAR_STYLE_VALUES = TABBAR_STYLE_OPTIONS.map(v => v.value)
export type TabbarStyle = (typeof TABBAR_STYLE_VALUES)[number]

// Layout mode
export const LAYOUT_MODE_OPTIONS = [
  { label: '垂直', value: 'sidebar-nav', description: '侧边菜单布局' },
  { label: '双列菜单', value: 'sidebar-mixed-nav', description: '垂直双列菜单模式' },
  { label: '水平', value: 'header-nav', description: '菜单全部显示在顶部' },
  { label: '侧边导航', value: 'header-sidebar-nav', description: '顶部通栏，侧边导航' },
  { label: '混合垂直', value: 'mixed-nav', description: '垂直水平菜单共存' },
  { label: '混合双列', value: 'header-mixed-nav', description: '双列、水平菜单共存' }
] as const
export const LAYOUT_MODE_VALUES = LAYOUT_MODE_OPTIONS.map(v => v.value)
export type LayoutMode = (typeof LAYOUT_MODE_VALUES)[number]

export const HEADER_MENU_ALIGN_OPTIONS = [
  { label: '左侧', value: 'start' },
  { label: '居中', value: 'center' },
  { label: '右侧', value: 'end' }
] as const
export const HEADER_MENU_ALIGN_VALUES = HEADER_MENU_ALIGN_OPTIONS.map(v => v.value)
export type HeaderMenuAlign = (typeof HEADER_MENU_ALIGN_VALUES)[number]
