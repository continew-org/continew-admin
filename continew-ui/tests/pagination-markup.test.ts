import { createElement } from 'react'
import { renderToStaticMarkup } from 'react-dom/server'
import { describe, expect, it } from 'vitest'

import { PaginationArrow } from '@/components/pagination-arrow'
import { PaginationEllipsis, PaginationLink } from '@/components/ui/pagination'

describe('pagination markup', () => {
  it('keeps the attributes the animate-ui indicator looks for', () => {
    const link = renderToStaticMarkup(createElement(PaginationLink, { href: '#', isActive: true }, '2'))

    expect(link.startsWith('<a')).toBe(true)
    expect(link).toContain('data-slot="pagination-link"')
    expect(link).toContain('aria-current="page"')

    const ellipsis = renderToStaticMarkup(createElement(PaginationEllipsis))
    expect(ellipsis).toContain('data-slot="pagination-ellipsis"')
  })

  it('renders the arrows as page-sized links without an active state', () => {
    const previous = renderToStaticMarkup(
      createElement(PaginationArrow, { direction: 'previous', disabled: false, onClick: () => {} })
    )

    expect(previous).toContain('aria-label="上一页"')
    expect(previous).toContain('data-slot="pagination-link"')
    expect(previous).not.toContain('aria-current')

    const disabled = renderToStaticMarkup(
      createElement(PaginationArrow, { direction: 'next', disabled: true, onClick: () => {} })
    )

    expect(disabled).toContain('aria-label="下一页"')
    expect(disabled).toContain('aria-disabled="true"')
  })
})

