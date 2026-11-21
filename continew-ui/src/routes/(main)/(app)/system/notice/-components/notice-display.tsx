import { StatusBadge, type StatusTone } from '@/components/status-cell'
import { Badge } from '@/components/ui/badge'
import type { NoticeStatus } from '@/lib/api/notice'
import { EMPTY_PLACEHOLDER } from '@/lib/constants'

import type { NoticeDictOption } from './use-notice-dicts'

const NOTICE_STATUS_TONES: Record<NoticeStatus, StatusTone> = {
  1: 'warning',
  2: 'info',
  3: 'success'
}

export function noticeOptionLabel(options: NoticeDictOption[], value?: string | number | null) {
  if (value === null || value === undefined || value === '') {
    return EMPTY_PLACEHOLDER
  }

  return options.find(item => item.value === String(value))?.label ?? String(value)
}

export function NoticeStatusBadge({ status, options }: { status: NoticeStatus; options: NoticeDictOption[] }) {
  return <StatusBadge tone={NOTICE_STATUS_TONES[status] ?? 'neutral'} label={noticeOptionLabel(options, status)} />
}

export function NoticeMethodTags({
  methods,
  options
}: {
  methods: number[] | null | undefined
  options: NoticeDictOption[]
}) {
  if (!methods || methods.length === 0) {
    return <span className="text-sm text-muted-foreground">无</span>
  }

  return (
    <div className="flex flex-wrap gap-1">
      {methods.map(method => (
        <Badge key={method} variant="outline" className="font-normal">
          {noticeOptionLabel(options, method)}
        </Badge>
      ))}
    </div>
  )
}

export function NoticeTypeBadge({ type, options }: { type?: string | null; options: NoticeDictOption[] }) {
  if (!type) {
    return <span className="text-sm text-muted-foreground">{EMPTY_PLACEHOLDER}</span>
  }

  return (
    <Badge variant="secondary" className="font-normal">
      {noticeOptionLabel(options, type)}
    </Badge>
  )
}
