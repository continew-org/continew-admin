import { PREFERENCE_REGISTRY, type PreferenceKey, type PreferenceValueMap } from './preferences-config'
import type { ResolvedThemeMode } from './theme'
import { applyThemeMode } from './theme-utils'

/**
 * Applies a preference to the DOM and returns the resolved theme when it changed the theme.
 * `theme_mode` toggles the `dark` class; everything else writes a `data-*` attribute or one or
 * more CSS custom properties.
 */
export function applyPreference<K extends PreferenceKey>(
  key: K,
  value: PreferenceValueMap[K]
): ResolvedThemeMode | undefined {
  if (key === 'theme_mode') {
    return applyThemeMode(value as PreferenceValueMap['theme_mode'])
  }

  const definition = PREFERENCE_REGISTRY[key]
  const cssValue = definition.type === 'number' && definition.unit ? `${value}${definition.unit}` : String(value)

  if (definition.cssVar) {
    const names = Array.isArray(definition.cssVar) ? definition.cssVar : [definition.cssVar]
    for (const name of names) document.documentElement.style.setProperty(name, cssValue)
    return undefined
  }

  if (definition.attribute) {
    document.documentElement.setAttribute(definition.attribute, String(value))
  }

  return undefined
}
