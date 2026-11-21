export const THEME_MODE_VALUES = ['light', 'dark', 'system'] as const

export type ThemeMode = (typeof THEME_MODE_VALUES)[number]
export type ResolvedThemeMode = 'light' | 'dark'

export const THEME_COLOR_OPTIONS = [
  {
    value: 'blue',
    label: '霁蓝',
    primary: { light: 'oklch(0.6231 0.188 259.8145)', dark: 'oklch(0.6231 0.188 259.8145)' }
  },
  {
    value: 'default',
    label: '墨染',
    primary: { light: 'oklch(0.205 0 0)', dark: 'oklch(0.922 0 0)' }
  },
  {
    value: 'cyan',
    label: '天青',
    primary: { light: 'oklch(0.5624 0.0947 203.2755)', dark: 'oklch(0.8520 0.1269 195.0354)' }
  },
  {
    value: 'emerald',
    label: '竹青',
    primary: { light: 'oklch(0.7227 0.1920 149.5793)', dark: 'oklch(0.7729 0.1535 163.2231)' }
  },
  {
    value: 'amber',
    label: '琥珀',
    primary: { light: 'oklch(0.7686 0.1647 70.0804)', dark: 'oklch(0.7686 0.1647 70.0804)' }
  },
  {
    value: 'orange',
    label: '丹霞',
    primary: { light: 'oklch(0.7357 0.1641 34.7091)', dark: 'oklch(0.7357 0.1641 34.7091)' }
  },
  {
    value: 'rose',
    label: '朱砂',
    primary: { light: 'oklch(0.5778 0.2282 26.5713)', dark: 'oklch(0.5778 0.2282 26.5713)' }
  },
  {
    value: 'fuchsia',
    label: '霓虹',
    primary: { light: 'oklch(0.6726 0.2904 341.4084)', dark: 'oklch(0.6726 0.2904 341.4084)' }
  },
  {
    value: 'violet',
    label: '暮紫',
    primary: { light: 'oklch(0.6056 0.2189 292.7172)', dark: 'oklch(0.6056 0.2189 292.7172)' }
  },
  {
    value: 'slate',
    label: '石板',
    primary: { light: 'oklch(0.5854 0.2041 277.1173)', dark: 'oklch(0.6801 0.1583 276.9349)' }
  }
] as const

export const THEME_COLOR_VALUES = THEME_COLOR_OPTIONS.map(color => color.value)

export type ThemeColor = (typeof THEME_COLOR_OPTIONS)[number]['value']
