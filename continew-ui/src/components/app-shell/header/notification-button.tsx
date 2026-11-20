import * as React from 'react'

import { useQuery } from '@tanstack/react-query'
import { Bell } from 'lucide-react'

import { Button } from '@/components/ui/button'
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover'
import { Tooltip, TooltipContent, TooltipTrigger } from '@/components/ui/tooltip'
import { dashboardKeys, getDashboardNotices } from '@/lib/api/dashboard'

/** Ids the user has already seen, so a red dot can mark notices that arrived since. */
const SEEN_STORAGE_KEY = 'continew_seen_notices'

function readSeen(): string[] {
  try {
    const raw = localStorage.getItem(SEEN_STORAGE_KEY)
    return raw ? (JSON.parse(raw) as string[]) : []
  } catch {
    return []
  }
}

/**
 * Notification bell. The dashboard notice endpoint has no read flag, so "new" is decided by
 * comparing the current notice ids against the ones recorded here; opening the popover marks
 * everything current as seen and clears the dot.
 */
export function NotificationButton() {
  const [open, setOpen] = React.useState(false)
  const [seen, setSeen] = React.useState<ReadonlySet<string>>(() => new Set(readSeen()))

  const { data } = useQuery({
    queryKey: dashboardKeys.notices(),
    queryFn: ({ signal }) => getDashboardNotices(signal)
  })
  const notices = data ?? []
  const hasUnseen = notices.some(notice => !seen.has(String(notice.id)))

  const handleOpenChange = (next: boolean) => {
    setOpen(next)
    if (!next || notices.length === 0) return

    const ids = notices.map(notice => String(notice.id))
    setSeen(new Set(ids))
    try {
      localStorage.setItem(SEEN_STORAGE_KEY, JSON.stringify(ids))
    } catch {
      // Storage may be unavailable; the dot simply reappears next load.
    }
  }

  return (
    <Popover open={open} onOpenChange={handleOpenChange}>
      <Tooltip>
        <TooltipTrigger
          render={
            <PopoverTrigger
              render={<Button variant="outline" size="icon-sm" aria-label="通知" className="relative" />}
            />
          }
        >
          <Bell />
          {hasUnseen && (
            <span
              aria-hidden
              className="absolute -top-0.5 -right-0.5 size-2 rounded-full bg-destructive ring-2 ring-background"
            />
          )}
        </TooltipTrigger>
        <TooltipContent side="bottom" sideOffset={8}>
          通知
        </TooltipContent>
      </Tooltip>
      <PopoverContent align="end" className="w-80 p-0">
        <div className="flex items-center justify-between border-b px-4 py-3">
          <span className="text-sm font-medium">最新公告</span>
        </div>
        <div className="max-h-80 overflow-y-auto p-1">
          {notices.length === 0 ? (
            <p className="px-3 py-6 text-center text-xs text-muted-foreground">暂无公告</p>
          ) : (
            notices.map(notice => (
              <div key={notice.id} className="flex items-center gap-2 rounded-md px-3 py-2 text-sm">
                <Bell className="size-3.5 shrink-0 text-muted-foreground" />
                <span className="min-w-0 flex-1 truncate">{notice.title}</span>
                {notice.isTop && <span className="shrink-0 text-xs text-primary">置顶</span>}
              </div>
            ))
          )}
        </div>
      </PopoverContent>
    </Popover>
  )
}
