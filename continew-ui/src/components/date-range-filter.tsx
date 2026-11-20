import * as React from 'react'

import { cn } from 'cn'
import { format } from 'date-fns'
import { CalendarDays } from 'lucide-react'
import type { DateRange } from 'react-day-picker'

import { Button } from '@/components/ui/button'
import { Calendar } from '@/components/ui/calendar'
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover'
import { useIsMobile } from '@/hooks/use-mobile'

interface DateRangeFilterProps {
  readonly value: DateRange | undefined
  readonly onChange: (value: DateRange | undefined) => void
  /** Shown while no range is picked. */
  readonly placeholder?: string
  /** Extra classes for the trigger, e.g. to span both columns of the mobile filter grid. */
  readonly className?: string
}

/**
 * A controlled day-range filter.
 *
 * Unlike `DateRangePicker`, it starts empty when the caller passes no value, so a screen
 * can decide whether a range applies by default.
 */
export function DateRangeFilter({ value, onChange, placeholder = '选择日期', className }: DateRangeFilterProps) {
  const [open, setOpen] = React.useState(false)
  const isMobile = useIsMobile()
  let label = placeholder

  if (value?.from) {
    const from = format(value.from, 'yyyy-MM-dd')
    label = value.to ? `${from} - ${format(value.to, 'yyyy-MM-dd')}` : from
  }

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger
        render={
          <Button
            variant="outline"
            className={cn(
              'min-w-0 max-w-full justify-start overflow-hidden border-input bg-transparent font-normal',
              className
            )}
          />
        }
      >
        <CalendarDays />
        <span className="min-w-0 truncate">{label}</span>
      </PopoverTrigger>
      <PopoverContent className="w-auto max-w-[calc(100vw-1rem)] overflow-hidden p-0" align="start">
        <Calendar
          mode="range"
          defaultMonth={value?.from}
          selected={value}
          onSelect={onChange}
          numberOfMonths={isMobile ? 1 : 2}
        />
      </PopoverContent>
    </Popover>
  )
}
