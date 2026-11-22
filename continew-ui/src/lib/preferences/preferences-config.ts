/**
 * Preference registry: every preference the shell understands, its type, default, where it is
 * saved and how it reaches the DOM.
 *
 * Saving:
 * "client-cookie" → the original seven preferences; kept in cookies so existing settings survive.
 * "localStorage"  → everything added since. The inline boot script reads localStorage just as
 *                   synchronously as cookies, so these still apply before the first paint, and
 *                   they stay out of every API request's Cookie header.
 *
 * DOM:
 * `attribute` → written to `<html data-*>` for CSS selectors.
 * `cssVar`    → written to `<html style>` as custom properties (numbers get `unit`).
 * The boot script applies both before the first paint, so layout and theme never flicker.
 */

import {
  BREADCRUMB_STYLE_VALUES,
  CONTENT_LAYOUT_VALUES,
  HEADER_MENU_ALIGN_VALUES,
  LAYOUT_MODE_VALUES,
  NAVBAR_STYLE_VALUES,
  NAVIGATION_STYLE_VALUES,
  PREFERENCES_BUTTON_POSITION_VALUES,
  RADIUS_VALUES,
  SIDEBAR_COLLAPSIBLE_VALUES,
  SIDEBAR_VARIANT_VALUES,
  TABBAR_STYLE_VALUES,
  TRANSITION_NAME_VALUES
} from './layout'
import { THEME_COLOR_VALUES, THEME_MODE_VALUES } from './theme'

export type PreferencePersistence = 'client-cookie' | 'localStorage'

type DomTarget = {
  persistence: PreferencePersistence
  attribute?: `data-${string}`
  cssVar?: `--${string}` | readonly `--${string}`[]
}

type EnumDefinition<Values extends readonly string[]> = DomTarget & {
  type: 'enum'
  values: Values
  defaultValue: Values[number]
}

type BooleanDefinition = DomTarget & { type: 'boolean'; defaultValue: boolean }

type NumberDefinition = DomTarget & { type: 'number'; defaultValue: number; min: number; max: number; unit?: 'px' }

type TextDefinition = DomTarget & { type: 'text'; defaultValue: string; maxLength: number }

export type PreferenceDefinition =
  | EnumDefinition<readonly string[]>
  | BooleanDefinition
  | NumberDefinition
  | TextDefinition

const local = { persistence: 'localStorage' } as const

function enumPref<const Values extends readonly string[]>(
  values: Values,
  defaultValue: Values[number],
  target: DomTarget = local
): EnumDefinition<Values> {
  return { type: 'enum', values, defaultValue, ...target }
}

function boolPref(defaultValue: boolean, target: DomTarget = local): BooleanDefinition {
  return { type: 'boolean', defaultValue, ...target }
}

function numberPref(
  defaultValue: number,
  min: number,
  max: number,
  target: DomTarget & { unit?: 'px' } = local
): NumberDefinition {
  return { type: 'number', defaultValue, min, max, ...target }
}

function textPref(defaultValue: string, maxLength: number, target: DomTarget = local): TextDefinition {
  return { type: 'text', defaultValue, maxLength, ...target }
}

const cookie = (attribute: `data-${string}`) => ({ persistence: 'client-cookie', attribute }) as const

export const PREFERENCE_REGISTRY = {
  // Appearance
  theme_mode: enumPref(THEME_MODE_VALUES, 'system', cookie('data-theme-mode')),
  theme_color: enumPref(THEME_COLOR_VALUES, 'blue', cookie('data-theme-color')),
  theme_color_primary: textPref('', 32, {
    ...local,
    cssVar: ['--primary', '--ring', '--sidebar-primary', '--sidebar-ring']
  }),
  theme_color_success: textPref('', 32, { ...local, cssVar: '--status-success' }),
  theme_color_warning: textPref('', 32, { ...local, cssVar: '--status-warning' }),
  theme_color_destructive: textPref('', 32, { ...local, cssVar: '--destructive' }),
  radius: enumPref(RADIUS_VALUES, '0.5', cookie('data-radius')),
  font_size: numberPref(16, 14, 20, { ...local, cssVar: '--app-font-size', unit: 'px' }),
  semi_dark_header: boolPref(false, { ...local, attribute: 'data-semi-dark-header' }),
  semi_dark_sidebar: boolPref(false, { ...local, attribute: 'data-semi-dark-sidebar' }),
  semi_dark_sidebar_sub: boolPref(false, { ...local, attribute: 'data-semi-dark-sidebar-sub' }),
  color_gray_mode: boolPref(false, { ...local, attribute: 'data-color-gray' }),
  color_weak_mode: boolPref(false, { ...local, attribute: 'data-color-weak' }),

  // Layout
  layout: enumPref(LAYOUT_MODE_VALUES, 'header-sidebar-nav', { ...local, attribute: 'data-layout' }),
  content_layout: enumPref(CONTENT_LAYOUT_VALUES, 'full-width', cookie('data-content-layout')),
  content_compact_width: numberPref(1536, 960, 1680, { ...local, cssVar: '--app-content-width', unit: 'px' }),
  content_padding: numberPref(24, 0, 48, { ...local, cssVar: '--app-content-padding', unit: 'px' }),

  // Header
  header_visible: boolPref(true),
  navbar_style: enumPref(NAVBAR_STYLE_VALUES, 'sticky', cookie('data-navbar-style')),
  header_height: numberPref(48, 40, 80, { ...local, cssVar: '--app-header-height', unit: 'px' }),
  header_menu_align: enumPref(HEADER_MENU_ALIGN_VALUES, 'start'),

  // Breadcrumb
  breadcrumb_enable: boolPref(true),
  breadcrumb_show_icon: boolPref(true),
  breadcrumb_show_home: boolPref(true),
  breadcrumb_hide_only_one: boolPref(false),
  breadcrumb_style: enumPref(BREADCRUMB_STYLE_VALUES, 'normal'),

  // Sidebar & navigation
  sidebar_variant: enumPref(SIDEBAR_VARIANT_VALUES, 'sidebar', cookie('data-sidebar-variant')),
  sidebar_collapsible: enumPref(SIDEBAR_COLLAPSIBLE_VALUES, 'icon', cookie('data-sidebar-collapsible')),
  sidebar_enable: boolPref(true),
  sidebar_hidden: boolPref(false),
  sidebar_width: numberPref(200, 160, 320, { ...local, cssVar: '--app-sidebar-width', unit: 'px' }),
  sidebar_mixed_width: numberPref(80, 60, 120, { ...local, cssVar: '--app-sidebar-mixed-width', unit: 'px' }),
  sidebar_draggable: boolPref(true),
  sidebar_expand_on_hover: boolPref(false),
  sidebar_collapsed_show_title: boolPref(true, { ...local, attribute: 'data-sidebar-show-title' }),
  sidebar_collapsed_button: boolPref(true),
  sidebar_fixed_button: boolPref(true),
  sidebar_extra_collapsed: boolPref(false),
  sidebar_auto_activate_child: boolPref(false),
  navigation_accordion: boolPref(true),
  navigation_style: enumPref(NAVIGATION_STYLE_VALUES, 'rounded', { ...local, attribute: 'data-navigation-style' }),
  navigation_split: boolPref(true),

  // Tabbar
  tabbar_enable: boolPref(true),
  tabbar_style: enumPref(TABBAR_STYLE_VALUES, 'card'),
  tabbar_height: numberPref(38, 28, 56, { ...local, cssVar: '--app-tabbar-height', unit: 'px' }),
  tabbar_max_count: numberPref(0, 0, 50),
  tabbar_persist: boolPref(true),
  tabbar_draggable: boolPref(true),
  tabbar_wheelable: boolPref(true),
  tabbar_middle_click_close: boolPref(true),
  tabbar_visit_history: boolPref(true),
  tabbar_show_icon: boolPref(true),
  tabbar_show_maximize: boolPref(true),
  tabbar_show_more: boolPref(true),
  tabbar_show_refresh: boolPref(true),

  // Animation
  animation_enable: boolPref(true, { ...local, attribute: 'data-animation' }),
  transition_enable: boolPref(true),
  transition_name: enumPref(TRANSITION_NAME_VALUES, 'fade-slide'),
  transition_progress: boolPref(true),
  transition_loading: boolPref(true),

  // Footer (content comes from the backend site options)
  footer_enable: boolPref(true),
  footer_fixed: boolPref(true),

  // General
  app_dynamic_title: boolPref(true),
  app_watermark: boolPref(true),
  app_watermark_content: textPref('', 64),
  app_preferences_button_position: enumPref(PREFERENCES_BUTTON_POSITION_VALUES, 'auto'),
  app_enable_copy_preferences: boolPref(true),
  app_enable_sticky_preferences_nav: boolPref(true),
  app_enable_check_updates: boolPref(true),

  // Shortcuts
  shortcut_enable: boolPref(true),
  shortcut_search: boolPref(true),
  shortcut_logout: boolPref(true),
  shortcut_lock_screen: boolPref(true),
  shortcut_escape: boolPref(true),

  // Header widgets
  widget_global_search: boolPref(true),
  widget_notification: boolPref(true),
  widget_refresh: boolPref(true),
  widget_fullscreen: boolPref(true),
  widget_theme_toggle: boolPref(true),
  widget_lock_screen: boolPref(true),
  widget_sidebar_toggle: boolPref(true)
} as const satisfies Record<string, PreferenceDefinition>

export type PreferenceKey = keyof typeof PREFERENCE_REGISTRY

type ValueOf<D> = D extends { type: 'enum'; values: readonly (infer V)[] }
  ? V
  : D extends { type: 'boolean' }
    ? boolean
    : D extends { type: 'number' }
      ? number
      : string

export type PreferenceValueMap = {
  [K in PreferenceKey]: ValueOf<(typeof PREFERENCE_REGISTRY)[K]>
}

export const PREFERENCE_KEYS = Object.freeze(Object.keys(PREFERENCE_REGISTRY) as PreferenceKey[])

export const PREFERENCE_DEFAULTS = Object.fromEntries(
  PREFERENCE_KEYS.map(key => [key, PREFERENCE_REGISTRY[key].defaultValue])
) as PreferenceValueMap

/** Storage key: cookies keep their original names, localStorage entries get a prefix. */
export function getPreferenceStorageKey(key: PreferenceKey) {
  return PREFERENCE_REGISTRY[key].persistence === 'localStorage' ? `pref_${key}` : key
}

/**
 * Parses a stored string into a valid value; anything invalid falls back to the default.
 * The boot script repeats this logic in plain JS, keep the two in step.
 */
export function parsePreference<K extends PreferenceKey>(
  key: K,
  rawValue: string | null | undefined
): PreferenceValueMap[K] {
  const definition: PreferenceDefinition = PREFERENCE_REGISTRY[key]
  const fallback = definition.defaultValue as PreferenceValueMap[K]

  if (rawValue == null) return fallback

  switch (definition.type) {
    case 'enum':
      return (definition.values.includes(rawValue) ? rawValue : fallback) as PreferenceValueMap[K]
    case 'boolean':
      return (rawValue === 'true' ? true : rawValue === 'false' ? false : fallback) as PreferenceValueMap[K]
    case 'number': {
      const value = Number(rawValue)
      if (rawValue === '' || !Number.isFinite(value)) return fallback
      return Math.min(Math.max(Math.round(value), definition.min), definition.max) as PreferenceValueMap[K]
    }
    case 'text':
      return rawValue.slice(0, definition.maxLength) as PreferenceValueMap[K]
  }
}
