import { describe, expect, it } from 'vitest'

import type { RouteResp } from '@/lib/api/types'
import { firstMenuLeaf, isMenuRootActive, toMenuGroups, toMenuRoots, toMenuTree } from '@/navigation/menu'
import { ANALYSIS_PATH, HOME_PATH } from '@/navigation/route-titles'

function route(id: number, path: string, options: Partial<RouteResp> = {}): RouteResp {
  return { id, path, type: 2, parentId: 0, title: path, name: path, component: '', ...options }
}

const routes = [
  route(1, '/system', {
    type: 1,
    title: '系统管理',
    children: [
      route(2, '/system/user'),
      route(3, '/system/log', { type: 1, children: [route(4, '/system/log/sms?tab=send')] }),
      route(5, '/system/hidden', { isHidden: true }),
      route(6, '', { type: 3 })
    ]
  }),
  route(7, 'https://example.org', { isExternal: true })
]

const dashboardPages = [HOME_PATH, ANALYSIS_PATH]

describe('menu layout projections', () => {
  it('shows dashboard pages directly under a labelled group even without backend routes', () => {
    const groups = toMenuGroups([])
    expect(groups.map(group => group.label)).toEqual(['仪表盘'])
    expect(groups[0].items.map(item => item.url)).toEqual(dashboardPages)
  })

  it('keeps the dashboard first without regrouping loose pages or nested backend directories', () => {
    const groups = toMenuGroups(routes)
    expect(groups.map(group => group.label)).toEqual(['仪表盘', undefined, '系统管理'])
    expect(groups[1].items).toMatchObject([{ id: '7', external: true }])
    expect(groups[2].items).toMatchObject([{ id: '2' }, { id: '3', subItems: [{ id: '4' }] }])
  })

  it('turns both directories into non-link parents in the classic sidebar', () => {
    const [tree] = toMenuTree(toMenuGroups(routes))
    expect(tree.items.map(item => [item.id, item.subItems?.map(child => child.url)])).toEqual([
      ['app-dashboard', dashboardPages],
      ['7', undefined],
      ['1', ['/system/user', '/system/log/sms?tab=send']]
    ])
    expect(tree.items.filter(item => item.subItems).map(item => item.url)).toEqual(['', ''])
  })

  it('keeps horizontal and split-layout roots selectable with the same active and first pages', () => {
    const roots = toMenuRoots(toMenuGroups(routes))
    expect(roots.map(root => root.id)).toEqual(['app-dashboard', '7', '1'])
    expect(roots[0].items.map(item => item.url)).toEqual(dashboardPages)
    expect(firstMenuLeaf(roots[0].items)?.url).toBe(HOME_PATH)
    expect(roots[1].link?.external).toBe(true)
    expect(roots.filter(root => isMenuRootActive(root, ANALYSIS_PATH))).toEqual([roots[0]])
    expect(roots.filter(root => isMenuRootActive(root, '/system/log/sms'))).toEqual([roots[2]])
  })
})
