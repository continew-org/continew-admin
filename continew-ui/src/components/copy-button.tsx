import * as React from 'react'

import { Check, Copy } from 'lucide-react'

import { Button } from '@/components/ui/button'
import { toast } from '@/components/ui/toast'
import { Tooltip, TooltipContent, TooltipTrigger } from '@/components/ui/tooltip'

const COPIED_FEEDBACK_MS = 1500

interface CopyButtonProps {
  readonly value: string
  /** Tooltip text while not copied, such as `复制 ID`. */
  readonly label?: string
  /** Shows a success toast, for values whose copy click is not otherwise obvious. */
  readonly successMessage?: string
  readonly size?: 'icon-xs' | 'icon-sm'
  readonly className?: string
}

/** Copies a value and shows a short check mark; shared by ids, keys and credentials. */
export function CopyButton({ value, label = '复制', successMessage, size = 'icon-xs', className }: CopyButtonProps) {
  const [copied, setCopied] = React.useState(false)
  const timer = React.useRef<ReturnType<typeof setTimeout>>(undefined)

  React.useEffect(() => () => clearTimeout(timer.current), [])

  const copy = async () => {
    try {
      await navigator.clipboard.writeText(value)
      setCopied(true)
      clearTimeout(timer.current)
      timer.current = setTimeout(() => setCopied(false), COPIED_FEEDBACK_MS)
      if (successMessage) {
        toast.add({ title: successMessage, type: 'success' })
      }
    } catch {
      // `navigator.clipboard` only exists in a secure context, so this can fail outright.
      toast.add({ title: '复制失败，请手动选择', type: 'error' })
    }
  }

  return (
    <Tooltip>
      <TooltipTrigger
        render={
          <Button
            type="button"
            variant="ghost"
            size={size}
            className={className}
            onClick={event => {
              // Copy buttons often sit inside clickable rows; keep the row from reacting.
              event.stopPropagation()
              void copy()
            }}
          />
        }
      >
        {copied ? <Check className="size-3.5 text-green-600" /> : <Copy className="size-3.5" />}
      </TooltipTrigger>
      <TooltipContent side="top">{copied ? '已复制' : label}</TooltipContent>
    </Tooltip>
  )
}
