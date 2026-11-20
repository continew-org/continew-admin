import { createFileRoute } from '@tanstack/react-router'

import { RoleList } from './-components/role-list'

export const Route = createFileRoute('/(main)/(app)/system/role')({
  component: RoleList
})
