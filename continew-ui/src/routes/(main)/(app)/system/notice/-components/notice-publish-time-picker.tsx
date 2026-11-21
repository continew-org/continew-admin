import * as React from 'react'

import { format, parse } from 'date-fns'

import { CalendarIcon } from 'lucide-react'

import { cn } from 'cn'

import { Button } from '@/components/ui/button'
import { Calendar } from '@/components/ui/calendar'
import { Input } from '@/components/ui/input'
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover'

interface NoticePublishTimePickerProps {
  readonly value: string | null
  readonly onChange: (value: string | null) => void
  readonly disabled?: boolean
  readonly invalid?: boolean
}

const VALUE_FORMAT = 'yyyy-MM-dd HH:mm:ss'
const DISPLAY_FORMAT = 'yyyy-MM-dd HH:mm'

function parseValue(value: string | null) {
  if (!value) {
    return null
  }

  const parsed = parse(value, VALUE_FORMAT, new Date())
  return Number.isNaN(parsed.getTime()) ? null : parsed
}

/** A date plus time picker; the backend accepts `yyyy-MM-dd HH:mm:ss`. */
export function NoticePublishTimePicker({
  value,
  onChange,
  disabled = false,
  invalid = false
}: NoticePublishTimePickerProps) {
  const parsed = parseValue(value)
  const [date, setDate] = React.useState<Date | undefined>(parsed ?? undefined)
  const [time, setTime] = React.useState(parsed ? format(parsed, 'HH:mm') : '09:00')

  React.useEffect(() => {
    const next = parseValue(value)
    setDate(next ?? undefined)
    setTime(next ? format(next, 'HH:mm') : '09:00')
  }, [value])

  const emit = (nextDate: Date | undefined, nextTime: string) => {
    if (!nextDate) {
      onChange(null)
      return
    }

    onChange(`${format(nextDate, 'yyyy-MM-dd')} ${nextTime || '00:00'}:00`)
  }

  return (
    <Popover>
      <PopoverTrigger
        render={
          <Button
            type="button"
            variant="outline"
            disabled={disabled}
            aria-invalid={invalid}
            className={cn('w-full justify-start font-normal', !value && 'text-muted-foreground')}
          />
        }
      >
        <CalendarIcon />
        {parsed ? format(parsed, DISPLAY_FORMAT) : '选择发布时间'}
      </PopoverTrigger>
      <PopoverContent align="start" className="w-auto p-3">
        <Calendar
          mode="single"
          selected={date}
          onSelect={nextDate => {
            setDate(nextDate)
            emit(nextDate, time)
          }}
        />
        <div className="mt-2 flex items-center gap-2 border-t pt-2">
          <span className="text-xs text-muted-foreground">时间</span>
          <Input
            type="time"
            step={60}
            value={time}
            disabled={disabled}
            className="h-8 w-28"
            onChange={event => {
              setTime(event.target.value)
              emit(date, event.target.value)
            }}
          />
        </div>
      </PopoverContent>
    </Popover>
  )
}
