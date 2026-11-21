/**
 * Preference registry: every shell preference, its type, default, how it is saved and how it
 * reaches the DOM.
 *
 * Saving:
 * "client-cookie" → read by the inline boot script before the first paint (layout-critical).
 * "localStorage"  → everything else; stored under a `pref_` prefix and read synchronously too.
 * "none"          → not saved, resets on reload.
 *
 * DOM:
 * `attribute` → written to `<html data-*>` for CSS selectors.
 * `cssVar`    → written to `<html style>` as custom properties (numbers get `unit`).
 * `boolean`/`text` preferences carry neither and are consumed by JS only.
 * The boot script applies both DOM kinds before the first paint, so nothing flickers.
 */

import {
  CONTENT_LAYOUT_VALUES,
  HEADER_MENU_ALIGN_VALUES,
  LAYOUT_MODE_VALUES,
  NAVBAR_STYLE_VALUES,
  RADIUS_VALUES,
  SIDEBAR_COLLAPSIBLE_VALUES,
  SIDEBAR_VARIANT_VALUES,
  TABBAR_STYLE_VALUES
} from './layout'
import { THEME_COLOR_VALUES, THEME_MODE_VALUES } from './theme'

export type PreferencePersistence = 'none' | 'client-cookie' | 'localStorage'

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

type NumberDefinition = DomTarget & {
  type: 'number'
  defaultValue: number
  min: number
  max: number
  unit?: 'px'
}

type BooleanDefinition = DomTarget & { type: 'boolean'; defaultValue: boolean }

type TextDefinition = DomTarget & { type: 'text'; defaultValue: string; maxLength: number }

export type PreferenceDefinition =
  | EnumDefinition<readonly string[]>
  | NumberDefinition
  | BooleanDefinition
  | TextDefinition

const cookie = (attribute: `data-${string}`): DomTarget => ({ persistence: 'client-cookie', attribute })
const local: DomTarget = { persistence: 'localStorage' }

function enumPref<const Values extends readonly string[]>(
  values: Values,
  defaultValue: Values[number],
  target: DomTarget
): EnumDefinition<Values> {
  return { type: 'enum', values, defaultValue, ...target }
}

function numberPref(
  defaultValue: number,
  min: number,
  max: number,
  target: DomTarget & { unit?: 'px' }
): NumberDefinition {
  return { type: 'number', defaultValue, min, max, ...target }
}

function boolPref(defaultValue: boolean, target: DomTarget = local): BooleanDefinition {
  return { type: 'boolean', defaultValue, ...target }
}

function textPref(defaultValue: string, maxLength: number, target: DomTarget = local): TextDefinition {
  return { type: 'text', defaultValue, maxLength, ...target }
}

export const PREFERENCE_REGISTRY = {
  theme_mode: enumPref(THEME_MODE_VALUES, 'light', cookie('data-theme-mode')),
  theme_color: enumPref(THEME_COLOR_VALUES, 'blue', cookie('data-theme-color')),
  color_gray_mode: boolPref(false, { ...local, attribute: 'data-color-gray' }),
  color_weak_mode: boolPref(false, { ...local, attribute: 'data-color-weak' }),
  content_layout: enumPref(CONTENT_LAYOUT_VALUES, 'full-width', cookie('data-content-layout')),
  navbar_style: enumPref(NAVBAR_STYLE_VALUES, 'sticky', cookie('data-navbar-style')),
  sidebar_variant: enumPref(SIDEBAR_VARIANT_VALUES, 'inset', cookie('data-sidebar-variant')),
  sidebar_collapsible: enumPref(SIDEBAR_COLLAPSIBLE_VALUES, 'icon', cookie('data-sidebar-collapsible')),
  radius: enumPref(RADIUS_VALUES, '0.5', cookie('data-radius')),
  // Root font size, in px: drives the whole rem-based type scale.
  font_size: numberPref(16, 14, 18, { ...local, cssVar: '--app-font-size', unit: 'px' }),

  // Layout
  layout: enumPref(LAYOUT_MODE_VALUES, 'sidebar-nav', { ...local, attribute: 'data-layout' }),
  header_menu_align: enumPref(HEADER_MENU_ALIGN_VALUES, 'start', local),
  navigation_split: boolPref(true),
  sidebar_auto_activate_child: boolPref(false),

  // Tabbar
  tabbar_enable: boolPref(true),
  tabbar_style: enumPref(TABBAR_STYLE_VALUES, 'plain', local),
  tabbar_height: numberPref(38, 28, 56, { ...local, cssVar: '--app-tabbar-height', unit: 'px' }),
  tabbar_max_count: numberPref(0, 0, 50, local),
  tabbar_persist: boolPref(true),
  tabbar_draggable: boolPref(true),
  tabbar_wheelable: boolPref(true),
  tabbar_middle_click_close: boolPref(false),
  tabbar_visit_history: boolPref(true),
  tabbar_show_icon: boolPref(true),
  tabbar_show_more: boolPref(true),

  // Widgets
  widget_notification: boolPref(true),
  widget_lock_screen: boolPref(true),

  // Shortcuts
  shortcut_enable: boolPref(true),
  shortcut_search: boolPref(true),
  shortcut_lock_screen: boolPref(true),
  shortcut_logout: boolPref(true),

  // General
  app_dynamic_title: boolPref(true),
  app_watermark: boolPref(false),
  app_watermark_content: textPref('', 64),
  app_enable_check_updates: boolPref(true)
} as const satisfies Record<string, PreferenceDefinition>

export type PreferenceKey = keyof typeof PREFERENCE_REGISTRY

type ValueOf<D> = D extends { type: 'enum'; values: readonly (infer V)[] }
  ? V
  : D extends { type: 'number' }
    ? number
    : D extends { type: 'boolean' }
      ? boolean
      : D extends { type: 'text' }
        ? string
        : never

export type PreferenceValueMap = {
  [K in PreferenceKey]: ValueOf<(typeof PREFERENCE_REGISTRY)[K]>
}

export const PREFERENCE_KEYS = Object.freeze(Object.keys(PREFERENCE_REGISTRY) as PreferenceKey[])

export const PREFERENCE_DEFAULTS = Object.fromEntries(
  PREFERENCE_KEYS.map(key => [key, PREFERENCE_REGISTRY[key].defaultValue])
) as PreferenceValueMap

export function getPreferencePersistence(key: PreferenceKey): PreferencePersistence {
  return PREFERENCE_REGISTRY[key].persistence
}

/** localStorage entries are namespaced with a `pref_` prefix; cookies keep their original names. */
export function getPreferenceStorageKey(key: PreferenceKey): string {
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

  if (definition.type === 'enum') {
    return (definition.values.includes(rawValue) ? rawValue : fallback) as PreferenceValueMap[K]
  }

  if (definition.type === 'boolean') {
    return (rawValue === 'true' ? true : rawValue === 'false' ? false : fallback) as PreferenceValueMap[K]
  }

  if (definition.type === 'text') {
    return rawValue.slice(0, definition.maxLength) as PreferenceValueMap[K]
  }

  // Numbers may arrive with a unit when read back from a CSS custom property (e.g. "16px").
  if (rawValue === '') return fallback
  const value = Number(String(rawValue).replace(/[^0-9.+-]/g, ''))
  if (!Number.isFinite(value)) return fallback
  return Math.min(Math.max(Math.round(value), definition.min), definition.max) as PreferenceValueMap[K]
}
