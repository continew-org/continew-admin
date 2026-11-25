import { describe, expect, it } from 'vitest'

import { isSidebarAdVisible, type SidebarAdState } from '@/layouts/sidebar/sidebar-ad-visibility'

const expandedDesktop: SidebarAdState = {
  unitId: '359',
  isMobile: false,
  state: 'expanded',
  collapsible: 'icon'
}

describe('isSidebarAdVisible', () => {
  it('shows the ad in the expanded desktop sidebar', () => {
    expect(isSidebarAdVisible(expandedDesktop)).toBe(true)
    expect(isSidebarAdVisible({ ...expandedDesktop, collapsible: 'offcanvas' })).toBe(true)
  })

  it('keeps builds without a unit id free of the vendor script', () => {
    expect(isSidebarAdVisible({ ...expandedDesktop, unitId: '' })).toBe(false)
    expect(isSidebarAdVisible({ ...expandedDesktop, unitId: undefined })).toBe(false)
  })

  it('opts out of the mobile drawer and the collapsed icon rail', () => {
    expect(isSidebarAdVisible({ ...expandedDesktop, isMobile: true })).toBe(false)
    expect(isSidebarAdVisible({ ...expandedDesktop, state: 'collapsed' })).toBe(false)
    expect(isSidebarAdVisible({ ...expandedDesktop, state: 'collapsed', collapsible: 'offcanvas' })).toBe(false)
  })

  it('treats the non-collapsible sidebar as expanded even when the open flag is off', () => {
    expect(isSidebarAdVisible({ ...expandedDesktop, state: 'collapsed', collapsible: 'none' })).toBe(true)
  })
})
