import { createFileRoute } from '@tanstack/react-router'

import { SmsLogList } from './-components/sms-log-list'

/**
 * The backend menu points at `/system/sms/log` while naming its component
 * `monitor/sms/log/index`. File-based routes take the URL from the path, so the screen has
 * to live here for the sidebar link to resolve.
 */
export const Route = createFileRoute('/(main)/(app)/system/sms/log')({
  component: SmsLogList
})
