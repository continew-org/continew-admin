import { useTable } from '@tanstack/react-table'
import { createElement } from 'react'
import { renderToStaticMarkup } from 'react-dom/server'
import { describe, expect, it, vi } from 'vitest'

import type { LogResp } from '@/lib/api/log'
import { EMPTY_PLACEHOLDER } from '@/lib/constants'
import { dataTableFeatures } from '@/lib/data-table-features'
import {
  createLoginLogColumns,
  createOperationLogColumns
} from '@/routes/(main)/(app)/monitor/log/-components/log-columns'

const log: LogResp = {
  id: 1,
  description: '日志记录',
  module: '登录',
  timeTaken: 12,
  ip: '127.0.0.1',
  address: '中国|北京|北京市',
  browser: 'Chrome',
  os: 'Windows',
  status: 1,
  errorMsg: null,
  createUserString: '张三',
  username: 'zhangsan',
  createTime: '2026-11-25 08:09:10'
}

function renderRow(login: boolean, overrides: Partial<LogResp> = {}, canViewDetail = false) {
  function LogRow() {
    const table = useTable({
      features: dataTableFeatures,
      data: [{ ...log, ...overrides }],
      columns: login
        ? createLoginLogColumns(0)
        : createOperationLogColumns({ rowOffset: 0, canViewDetail, onDetail: vi.fn() })
    })
    return table
      .getRowModel()
      .rows[0].getVisibleCells()
      .map(cell =>
        createElement('div', { key: cell.id, 'data-column': cell.column.id }, createElement(table.FlexRender, { cell }))
      )
  }
  return renderToStaticMarkup(createElement(LogRow))
}

describe.each([true, false])('log columns (login=%s)', login => {
  it('shows the joined identity, unmodified address and device', () => {
    const html = renderRow(login)
    for (const value of ['>张三 zhangsan<', `>${log.address}<`, '>Chrome / Windows<']) expect(html).toContain(value)
    expect(html.match(/data-column=/g)).toHaveLength(login ? 8 : 10)
  })

  it('keeps either side without a dangling separator', () => {
    const left = renderRow(login, { username: null, os: '' })
    expect(left).toContain('>张三<')
    expect(left).toContain('>Chrome<')
    const right = renderRow(login, { createUserString: null, browser: '' })
    expect(right).toContain('>zhangsan<')
    expect(right).toContain('>Windows<')
  })

  it('does not invent history for missing identity, address or device', () => {
    const html = renderRow(login, { username: null, createUserString: null, address: '', browser: '', os: '' })
    for (const id of ['user', 'address', 'device'])
      expect(html).toContain(`data-column="${id}"><span>${EMPTY_PLACEHOLDER}</span>`)
  })

  it('only links operation times with detail permission', () => {
    expect(renderRow(login)).not.toContain('<button')
    expect(renderRow(login, {}, true).includes('<button')).toBe(!login)
  })
})
