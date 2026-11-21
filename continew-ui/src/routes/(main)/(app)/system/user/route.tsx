import { createFileRoute } from '@tanstack/react-router'

import { UserList } from './-components/user-list'

export const Route = createFileRoute('/(main)/(app)/system/user')({
  component: UserList
})
