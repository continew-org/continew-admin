import { cn } from 'cn'

import { CopyButton } from '@/components/copy-button'

/** A value that is only useful next to a copy button, such as a generated id or code. */
export function CopyableText({
  value,
  className,
  label,
  successMessage
}: {
  value: string | null | undefined
  className?: string
  label?: string
  successMessage?: string
}) {
  if (!value) {
    return null
  }

  return (
    <div className="flex items-center gap-1">
      <span className={cn('text-sm', className)}>{value}</span>
      <CopyButton value={value} label={label} successMessage={successMessage} />
    </div>
  )
}
