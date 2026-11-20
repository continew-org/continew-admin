import type { MouseEvent } from 'react'

import { Check, Pencil, RefreshCw, RotateCcw, Undo2 } from 'lucide-react'

import { Button } from '@/components/ui/button'
import { Spinner } from '@/components/ui/spinner'
import { cn } from 'cn'

interface ConfigPanelHeaderProps {
  title: string
  description: string
  canUpdate: boolean
  isEdit: boolean
  isSubmitting: boolean
  onEdit: () => void
  /** Opens the reset-to-default confirmation. */
  onResetDefault: () => void
  /** Discards the edits, back to the values the server holds. */
  onResetForm: () => void
  onCancel: () => void
  /** `card` matches the site panel, whose header sits inside its own card. */
  variant?: 'panel' | 'card'
}

export function ConfigPanelHeader({
  title,
  description,
  canUpdate,
  isEdit,
  isSubmitting,
  onEdit,
  onResetDefault,
  onResetForm,
  onCancel,
  variant = 'panel'
}: ConfigPanelHeaderProps) {
  // React reuses this same DOM button as the submit button on the next render; without this the
  // browser can still submit the form after the click handler.
  const clickWithoutSubmit = (handler: () => void) => (event: MouseEvent<HTMLButtonElement>) => {
    event.preventDefault()
    handler()
  }

  return (
    <div
      className={cn(
        'flex flex-wrap items-center justify-between border-b',
        variant === 'card' ? 'gap-3 pb-3' : 'gap-4 pb-4'
      )}
    >
      <div>
        <h2 className={cn('text-base font-semibold tracking-tight', variant === 'panel' && 'font-heading')}>{title}</h2>
        <p className={variant === 'panel' ? 'mt-0.5 text-sm text-muted-foreground' : 'text-muted-foreground'}>
          {description}
        </p>
      </div>
      <div className="flex items-center gap-2">
        {isEdit ? (
          <>
            <Button type="submit" size="sm" disabled={isSubmitting}>
              {isSubmitting ? <Spinner /> : <Check />}
              保存
            </Button>
            <Button type="button" size="sm" variant="outline" onClick={onResetForm} disabled={isSubmitting}>
              <RefreshCw />
              重置
            </Button>
            <Button type="button" size="sm" variant="ghost" onClick={onCancel} disabled={isSubmitting}>
              <Undo2 />
              取消
            </Button>
          </>
        ) : (
          canUpdate && (
            <>
              <Button type="button" size="sm" onClick={clickWithoutSubmit(onEdit)}>
                <Pencil />
                修改
              </Button>
              <Button type="button" size="sm" variant="outline" onClick={clickWithoutSubmit(onResetDefault)}>
                <RotateCcw />
                恢复默认
              </Button>
            </>
          )
        )}
      </div>
    </div>
  )
}
