import { endOfDay, format, startOfDay, subDays } from 'date-fns'
import type { DateRange } from 'react-day-picker'

const DATE_TIME_FORMAT = 'yyyy-MM-dd HH:mm:ss'

/** Default range for the log and monitor lists: the last `days` days, including today. */
export function createRecentDateRange(days = 7): DateRange {
  const to = new Date()
  return { from: subDays(to, days - 1), to }
}

/** Formats a picked range as the backend's `start,end` `LocalDateTime` filter, widened to whole days. */
export function toDateTimeRangeQuery(range: DateRange | undefined) {
  if (!range?.from) {
    return undefined
  }

  const from = format(startOfDay(range.from), DATE_TIME_FORMAT)
  const to = format(endOfDay(range.to ?? range.from), DATE_TIME_FORMAT)
  return `${from},${to}`
}
