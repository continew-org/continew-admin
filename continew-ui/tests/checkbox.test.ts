import { createElement } from 'react'
import { renderToStaticMarkup } from 'react-dom/server'
import { describe, expect, it } from 'vitest'

import { Checkbox } from '@/components/ui/checkbox'

describe('Checkbox', () => {
  it('omits the indicator when unchecked', () => {
    const html = renderToStaticMarkup(createElement(Checkbox, { checked: false }))

    expect(html).toContain('data-unchecked=""')
    expect(html).not.toContain('data-slot="checkbox-indicator"')
  })

  it('renders a minus indicator and indeterminate state when indeterminate', () => {
    const html = renderToStaticMarkup(createElement(Checkbox, { checked: false, indeterminate: true }))

    expect(html).toContain('data-indeterminate=""')
    expect(html).toContain('aria-checked="mixed"')
    expect(html).toContain('data-slot="checkbox-indicator"')
    expect(html).toContain('lucide-minus')
    expect(html).not.toContain('lucide-check')
  })

  it('renders a check indicator when checked', () => {
    const html = renderToStaticMarkup(createElement(Checkbox, { checked: true }))

    expect(html).toContain('data-checked=""')
    expect(html).toContain('aria-checked="true"')
    expect(html).toContain('lucide-check')
    expect(html).not.toContain('lucide-minus')
  })
})
