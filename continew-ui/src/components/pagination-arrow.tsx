import type { MouseEvent } from 'react'
import { ChevronLeft, ChevronRight } from 'lucide-react'

import { PaginationLink, PaginationNext, PaginationPrevious } from '@/components/ui/pagination'

interface PaginationArrowProps {
  readonly direction: 'previous' | 'next'
  readonly disabled: boolean
  readonly onClick: () => void
  /** Visible label for standalone paginators; the table footer omits it and gets a square icon. */
  readonly text?: string
}

/**
 * The previous/next arrow shared by the paginated lists. At either end the link keeps its slot,
 * but it leaves the tab order, reports `aria-disabled` and ignores activation.
 */
export function PaginationArrow({ direction, disabled, onClick, text }: PaginationArrowProps) {
  const isPrevious = direction === 'previous'
  const label = isPrevious ? '上一页' : '下一页'

  const linkProps = {
    href: '#',
    'aria-label': label,
    'aria-disabled': disabled || undefined,
    tabIndex: disabled ? -1 : undefined,
    className: disabled ? 'pointer-events-none opacity-50' : undefined,
    onClick: (event: MouseEvent<HTMLAnchorElement>) => {
      event.preventDefault()
      if (!disabled) {
        onClick()
      }
    }
  }

  if (text) {
    return isPrevious ? (
      <PaginationPrevious {...linkProps} text={text} />
    ) : (
      <PaginationNext {...linkProps} text={text} />
    )
  }

  return (
    <PaginationLink {...linkProps} size="icon">
      {isPrevious ? <ChevronLeft /> : <ChevronRight />}
    </PaginationLink>
  )
}
