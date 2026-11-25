export type SidebarCollapsible = 'icon' | 'offcanvas' | 'none'

export interface SidebarAdState {
  /** Build-time ad unit id; empty in builds that do not run ads. */
  unitId: string | undefined
  isMobile: boolean
  state: 'expanded' | 'collapsed'
  collapsible: SidebarCollapsible
}

/** True only for the desktop sidebar that stays expanded; the mobile drawer, icon rail and peek opt out. */
export function isSidebarAdVisible({ unitId, isMobile, state, collapsible }: SidebarAdState): boolean {
  if (!unitId || isMobile) return false
  return collapsible === 'none' || state === 'expanded'
}
