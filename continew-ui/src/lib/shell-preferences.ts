import { getClientCookie } from '@/lib/cookie'
import { parsePreference } from '@/lib/preferences/preferences-config'

/**
 * Sidebar preferences are read from cookies so the shell renders correctly on the first paint,
 * which is also what the boot script relies on.
 */
export function readShellPreferences() {
  return {
    defaultOpen: getClientCookie('sidebar_state') !== 'false',
    variant: parsePreference('sidebar_variant', getClientCookie('sidebar_variant')),
    collapsible: parsePreference('sidebar_collapsible', getClientCookie('sidebar_collapsible'))
  }
}

export type ShellPreferences = ReturnType<typeof readShellPreferences>

/**
 * Route loader for the shell layouts.
 *
 * The menu and the access checks run in `beforeLoad`, so this only reads the sidebar preferences
 * the first paint needs.
 */
export function loadAppShell(): ShellPreferences {
  return readShellPreferences()
}
