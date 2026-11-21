/** The backend grants every permission with this code (SystemConstants.ALL_PERMISSION). */
const ALL_PERMISSION = '*:*:*'

/**
 * Whether `permissions` grants `code`.
 *
 * An absent code means the action needs no permission, which callers rely on for menu
 * entries that carry no `permission` field.
 */
export function hasPermission(permissions: readonly string[], code?: string) {
  if (!code) {
    return true
  }

  return permissions.includes(ALL_PERMISSION) || permissions.includes(code)
}
