import { useTable } from '@tanstack/react-table'
import { createElement } from 'react'
import { renderToStaticMarkup } from 'react-dom/server'
import { describe, expect, it, vi } from 'vitest'

import type { OnlineUserResp } from '@/lib/api/online'
import { EMPTY_PLACEHOLDER } from '@/lib/constants'
import { dataTableFeatures } from '@/lib/data-table-features'
import { createOnlineUserColumns } from '@/routes/(main)/(app)/monitor/online/-components/online-user-columns'

const session: OnlineUserResp = {
  id: 1,
  sessionId: 'current',
  nickname: '张三',
  username: 'zhangsan',
  clientType: 'PC',
  clientId: 'web',
  ip: '127.0.0.1',
  address: '中国|北京|北京市',
  browser: 'Chrome',
  os: 'Windows',
  loginTime: '2026-11-25 08:09:10',
  lastActiveTime: '2026-11-25 09:10:11',
  lastRefreshTime: 'hidden-refresh',
  activeTimeExact: true,
  online: true,
  idleExpired: false,
  idleExpireTime: null,
  sessionExpireTime: '2026-12-25 08:09:10'
}

function renderRow(overrides: Partial<OnlineUserResp> = {}, canKickout = true) {
  function SessionRow() {
    const table = useTable({
      features: dataTableFeatures,
      data: [{ ...session, ...overrides }],
      columns: createOnlineUserColumns({ canKickout, isCurrentSession: id => id === 'current', onKickout: vi.fn() })
    })
    const headers = table
      .getHeaderGroups()[0]
      .headers.map(header => createElement('th', { key: header.id }, createElement(table.FlexRender, { header })))
    const cells = table
      .getRowModel()
      .rows[0].getVisibleCells()
      .map(cell => createElement('td', { key: cell.id }, createElement(table.FlexRender, { cell })))
    return createElement(
      'table',
      null,
      createElement('thead', null, createElement('tr', null, headers)),
      createElement('tbody', null, createElement('tr', null, cells))
    )
  }
  return renderToStaticMarkup(createElement(SessionRow))
}

describe('online session columns', () => {
  it('shows eight compact columns in order and leaves the address intact', () => {
    const html = renderRow()
    expect([...html.matchAll(/<th>(.*?)<\/th>/g)].map(match => match[1].replace(/<[^>]+>/g, ''))).toEqual([
      '用户',
      '登录 IP',
      'IP 归属地',
      '浏览器 / 系统',
      '登录时间',
      '最近活跃',
      '状态',
      '操作'
    ])
    for (const value of [
      '张三 zhangsan',
      session.address,
      'Chrome / Windows',
      '>11-25 08:09<',
      'title="2026-11-25 08:09:10"',
      '>11-25 09:10<',
      '在线',
      '绝对到期：2026-12-25 08:09:10'
    ])
      expect(html).toContain(value)
    expect(html).not.toContain('hidden-refresh')
  })
  it('marks estimated activity and explains idle expiry and timeout', () => {
    const idle = { activeTimeExact: false, online: false, idleExpireTime: '2026-11-25 10:10:11' }
    const html = renderRow(idle)
    for (const value of [
      '>约 11-25 09:10<',
      'title="约 2026-11-25 09:10:11"',
      '>闲置<',
      '闲置到期：2026-11-25 10:10:11'
    ])
      expect(html).toContain(value)
    expect(renderRow({ ...idle, online: true, idleExpired: true })).toContain('>闲置超时<')
  })
  it('does not invent missing values, separators or an idle status', () => {
    const html = renderRow({
      nickname: '',
      browser: '',
      os: '',
      address: '',
      lastActiveTime: null,
      activeTimeExact: false,
      online: null
    })
    expect(html).toContain('>zhangsan<')
    expect(html.split(`>${EMPTY_PLACEHOLDER}<`)).toHaveLength(5)
    expect(html).not.toContain('约 ')
  })
  it('only offers kickout for other sessions and when permitted', () => {
    expect(renderRow()).toContain('当前会话')
    expect(renderRow()).not.toContain('aria-label="强退"')
    expect(renderRow({ sessionId: 'other' })).toContain('aria-label="强退"')
    expect(renderRow({ sessionId: 'other' }, false)).not.toContain('操作')
  })
})
