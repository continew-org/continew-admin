import { describe, expect, it } from 'vitest'

import type { RouteResp } from '@/lib/api/types'
import { hasPermission } from '@/lib/permission'
import { isPathAllowed } from '@/navigation/menu'

const menu: RouteResp[] = [
  {
    id: 1,
    parentId: 0,
    title: '系统管理',
    type: 1,
    path: '/system',
    name: 'System',
    component: 'Layout',
    children: [
      {
        id: 2,
        parentId: 1,
        title: '用户管理',
        type: 2,
        path: '/system/user',
        name: 'SystemUser',
        component: 'system/user/index'
      },
      {
        id: 3,
        parentId: 1,
        title: '短信日志',
        type: 2,
        path: '/system/sms/log?tab=send',
        name: 'SystemSmsLog',
        component: 'system/sms/log/index'
      },
      {
        id: 4,
        parentId: 1,
        title: '隐藏页',
        type: 2,
        path: '/system/hidden',
        name: 'SystemHidden',
        component: 'system/hidden/index',
        isHidden: true
      }
    ]
  }
]

describe('hasPermission', () => {
  it('grants a listed code and rejects an unlisted one', () => {
    expect(hasPermission(['system:user:list'], 'system:user:list')).toBe(true)
    expect(hasPermission(['system:user:list'], 'system:user:delete')).toBe(false)
  })

  it('treats the wildcard as every permission', () => {
    expect(hasPermission(['*:*:*'], 'system:user:delete')).toBe(true)
  })

  it('treats an absent code as no requirement', () => {
    expect(hasPermission([], undefined)).toBe(true)
    expect(hasPermission([], '')).toBe(true)
  })

  it('denies everything when no code was granted', () => {
    expect(hasPermission([], 'system:user:list')).toBe(false)
  })
})

describe('isPathAllowed', () => {
  it('allows the app-owned static pages even with an empty menu', () => {
    expect(isPathAllowed([], '/dashboard')).toBe(true)
    expect(isPathAllowed([], '/dashboard/profile')).toBe(true)
  })

  it('allows a menu page, ignoring a trailing slash and the menu query string', () => {
    expect(isPathAllowed(menu, '/system/user/')).toBe(true)
    expect(isPathAllowed(menu, '/system/sms/log')).toBe(true)
  })

  it('allows a hidden menu page, which stays routable off the sidebar', () => {
    expect(isPathAllowed(menu, '/system/hidden')).toBe(true)
  })

  it('rejects a page missing from the menu', () => {
    expect(isPathAllowed(menu, '/system/role')).toBe(false)
    expect(isPathAllowed(menu, '/dashboard/unknown')).toBe(false)
  })
})
