import { createFileRoute } from '@tanstack/react-router'

import { TenantList } from './-components/tenant-list'

export const Route = createFileRoute('/(main)/(app)/tenant/management')({
  component: TenantList
})
