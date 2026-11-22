import * as React from 'react'

import { cn } from 'cn'
import { Eye, EyeOff } from 'lucide-react'

import { Button } from '@/components/ui/button'
import { Tooltip, TooltipContent, TooltipTrigger } from '@/components/ui/tooltip'
import { maskText, type MaskRule, type MaskType } from '@/lib/mask'

export interface SensitiveTextProps {
  readonly value: string | null | undefined
  /** Named rule from `MASK_RULES`, or a custom `{ pattern, mask }` rule. */
  readonly type?: MaskType | MaskRule
  /** Starts unmasked when true; the eye button still toggles. */
  readonly defaultRevealed?: boolean
  readonly className?: string
  /** Classes for the text node, e.g. `break-all select-all` for keys. */
  readonly textClassName?: string
}

/**
 * Sensitive value shown masked with an eye button to reveal it, for phones,
 * id cards, emails, keys and similar fields in tables and drawers.
 *
 * A custom rule follows `String.replace` semantics: `pattern` captures what
 * stays visible and `mask` is the replacement (`$1` keeps a capture).
 */
export function SensitiveText({
  value,
  type = 'password',
  defaultRevealed = false,
  className,
  textClassName
}: SensitiveTextProps) {
  const [revealed, setRevealed] = React.useState(defaultRevealed)
  // A reused instance (drawer switching records) must not show the next value in plain text.
  const [shownValue, setShownValue] = React.useState(value)
  if (value !== shownValue) {
    setShownValue(value)
    setRevealed(defaultRevealed)
  }

  if (!value) {
    return null
  }

  const text = revealed ? value : maskText(value, type)

  return (
    <span className={cn('inline-flex items-center gap-1', className)}>
      <span className={cn('min-w-0 truncate', textClassName)}>{text}</span>
      <Tooltip>
        <TooltipTrigger
          render={
            <Button
              type="button"
              variant="ghost"
              size="icon-xs"
              aria-label={revealed ? '隐藏敏感信息' : '显示敏感信息'}
              onClick={event => {
                // Sensitive text often sits inside clickable rows.
                event.stopPropagation()
                setRevealed(v => !v)
              }}
            />
          }
        >
          {revealed ? <EyeOff className="size-3.5" /> : <Eye className="size-3.5" />}
        </TooltipTrigger>
        <TooltipContent side="top">{revealed ? '隐藏' : '显示'}</TooltipContent>
      </Tooltip>
    </span>
  )
}
