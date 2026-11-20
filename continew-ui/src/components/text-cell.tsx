import { cn } from 'cn'

import { EMPTY_PLACEHOLDER } from '@/lib/constants'

interface TextCellProps {
  readonly value?: string | number | null
  /** Extra classes for the value, e.g. `font-medium` or a wider truncation cap. */
  readonly className?: string
  /** Caps the truncated width; defaults to `max-w-56`. */
  readonly maxWidth?: string
  /** When false, long text wraps inside `maxWidth` instead of truncating to one line. */
  readonly truncate?: boolean
}

/** A compact table cell: empty values become the shared placeholder, full value stays on title. */
export function TextCell({ value, className, maxWidth = 'max-w-56', truncate = true }: TextCellProps) {
  if (value === null || value === undefined || value === '') {
    return <span>{EMPTY_PLACEHOLDER}</span>
  }

  return <span className={cn(truncate && 'truncate', maxWidth, className)}>{value}</span>
}
