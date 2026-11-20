import { createFileRoute } from '@tanstack/react-router'

import { OnlineUserList } from './-components/online-user-list'

export const Route = createFileRoute('/(main)/(app)/monitor/online')({
  component: OnlineUserList
})
