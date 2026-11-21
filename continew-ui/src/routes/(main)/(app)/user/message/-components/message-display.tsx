import { TypeBadge } from '@/components/data-badge'
import type { MessageType } from '@/lib/api/message'

export const MESSAGE_TYPE_LABELS: Record<MessageType, string> = { 1: '系统消息', 2: '安全消息' }

/** Matches the backend colours: system messages use the primary tone, security ones a warning. */
export function MessageTypeBadge({ type }: { type: MessageType }) {
  return <TypeBadge tone={type === 2 ? 'amber' : 'blue'} label={MESSAGE_TYPE_LABELS[type]} />
}

/** The dot in front of an unread title; read rows keep the gap so titles stay aligned. */
export function UnreadDot({ unread }: { unread: boolean }) {
  return <span className={unread ? 'size-1.5 shrink-0 rounded-full bg-destructive' : 'size-1.5 shrink-0'} />
}
