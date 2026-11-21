import {
  PREFERENCE_REGISTRY,
  type PreferenceDefinition,
  type PreferenceKey,
  type PreferenceValueMap
} from './preferences-config'
import type { ResolvedThemeMode } from './theme'
import { applyThemeMode } from './theme-utils'

/** Writes one preference to `<html>`: a data attribute, CSS custom properties, or the theme class. */
export function applyPreference<K extends PreferenceKey>(
  key: K,
  value: PreferenceValueMap[K]
): ResolvedThemeMode | undefined {
  if (key === 'theme_mode') {
    return applyThemeMode(value as PreferenceValueMap['theme_mode'])
  }

  const definition: PreferenceDefinition = PREFERENCE_REGISTRY[key]
  const root = document.documentElement

  if (definition.attribute) {
    root.setAttribute(definition.attribute, String(value))
  }

  if (definition.cssVar) {
    const cssValue = definition.type === 'number' && definition.unit ? `${value}${definition.unit}` : String(value)
    const names = typeof definition.cssVar === 'string' ? [definition.cssVar] : definition.cssVar

    for (const name of names) {
      // An empty value (an unset custom colour) falls back to the stylesheet.
      if (cssValue) root.style.setProperty(name, cssValue)
      else root.style.removeProperty(name)
    }
  }

  return undefined
}
