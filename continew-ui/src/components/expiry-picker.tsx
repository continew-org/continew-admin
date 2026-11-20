import * as React from 'react'

import { addDays, addYears, format } from 'date-fns'

import { Calendar as CalendarIcon, X } from 'lucide-react'

import { cn } from 'cn'

import { Button } from '@/components/ui/button'
import { Calendar } from '@/components/ui/calendar'
import { Input } from '@/components/ui/input'
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover'

interface ExpiryPickerProps {
  /** The stored `yyyy-MM-dd HH:mm:ss` value, or an empty string for one that never expires. */
  readonly value: string
  readonly onChange: (value: string) => void
  /** What an empty value means on this screen, used for the preset and the clear button. */
  readonly neverLabel?: string
  /** The trigger text while the value is empty. */
  readonly placeholder?: string
  readonly invalid?: boolean
  readonly disabled?: boolean
  readonly id?: string
  readonly onBlur?: () => void
}

/** The backend sends `yyyy-MM-dd HH:mm:ss`; Safari needs the dashes replaced to parse it. */
function toDate(value: string) {
  if (!value) {
    return undefined
  }

  const date = new Date(value.replace(/-/g, '/'))
  return Number.isNaN(date.getTime()) ? undefined : date
}

/**
 * An expiry picker: quick presets, a single-day calendar and hour/minute/second inputs.
 * A picked day defaults to the end of that day; the time can then be adjusted to the second.
 */
export function ExpiryPicker({
  value,
  onChange,
  neverLabel = '永不过期',
  placeholder = '永不过期（点击设置过期时间）',
  invalid,
  disabled,
  id,
  onBlur
}: ExpiryPickerProps) {
  const [open, setOpen] = React.useState(false)

  const time = value.length >= 19 ? value.slice(11, 19) : '23:59:59'
  const [hour, minute, second] = time.split(':')

  const applyDate = (date: Date, close = true) => {
    onChange(`${format(date, 'yyyy-MM-dd')} ${time}`)
    if (close) {
      setOpen(false)
    }
  }

  const applyTime = (index: 0 | 1 | 2, raw: string) => {
    const date = toDate(value)
    if (!date) {
      return
    }

    const parts = [hour, minute, second]
    const limit = index === 0 ? 23 : 59
    parts[index] = String(Math.min(limit, Math.max(0, Number(raw) || 0))).padStart(2, '0')
    onChange(`${format(date, 'yyyy-MM-dd')} ${parts.join(':')}`)
  }

  const clear = () => {
    onChange('')
    setOpen(false)
  }

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <div className="relative flex items-center">
        <PopoverTrigger
          render={
            <Button
              type="button"
              id={id}
              variant="outline"
              disabled={disabled}
              className={cn('w-full justify-between font-normal', value ? 'pr-8' : 'pr-2.5')}
              aria-invalid={invalid}
              onBlur={onBlur}
            />
          }
        >
          <span className={value ? 'text-foreground' : 'text-muted-foreground'}>{value || placeholder}</span>
          <CalendarIcon className="size-4 text-muted-foreground" />
        </PopoverTrigger>

        {value ? (
          <button
            type="button"
            onClick={event => {
              event.stopPropagation()
              clear()
            }}
            disabled={disabled}
            className="absolute right-2.5 z-10 text-muted-foreground transition-colors hover:text-foreground"
          >
            <X className="size-3.5" />
          </button>
        ) : null}
      </div>

      <PopoverContent className="w-auto p-3" align="start">
        {/* One `inert` covers the presets, the calendar and the time inputs while saving. */}
        <div className="flex flex-col gap-3" inert={disabled}>
          <div className="flex flex-wrap items-center gap-1.5 border-b pb-2">
            <Button type="button" size="xs" variant="outline" onClick={() => applyDate(addDays(new Date(), 30))}>
              30天
            </Button>
            <Button type="button" size="xs" variant="outline" onClick={() => applyDate(addDays(new Date(), 90))}>
              90天
            </Button>
            <Button type="button" size="xs" variant="outline" onClick={() => applyDate(addYears(new Date(), 1))}>
              1年
            </Button>
            <Button type="button" size="xs" variant="outline" onClick={() => applyDate(addYears(new Date(), 3))}>
              3年
            </Button>
            <Button type="button" size="xs" variant="ghost" onClick={clear}>
              {neverLabel}
            </Button>
          </div>

          <Calendar
            mode="single"
            selected={toDate(value)}
            onSelect={date => (date ? applyDate(date, false) : clear())}
            disabled={date => date.getTime() < Date.now() - 24 * 60 * 60 * 1000}
          />

          <div className="flex items-center gap-1 border-t pt-2">
            <span className="mr-1 text-xs text-muted-foreground">时间</span>
            <Input
              type="number"
              min={0}
              max={23}
              value={hour}
              disabled={!value}
              onChange={event => applyTime(0, event.target.value)}
              className="h-7 w-14 px-1 text-center"
            />
            <span className="text-muted-foreground">:</span>
            <Input
              type="number"
              min={0}
              max={59}
              value={minute}
              disabled={!value}
              onChange={event => applyTime(1, event.target.value)}
              className="h-7 w-14 px-1 text-center"
            />
            <span className="text-muted-foreground">:</span>
            <Input
              type="number"
              min={0}
              max={59}
              value={second}
              disabled={!value}
              onChange={event => applyTime(2, event.target.value)}
              className="h-7 w-14 px-1 text-center"
            />
          </div>
        </div>
      </PopoverContent>
    </Popover>
  )
}
