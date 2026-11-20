import { createFileRoute } from '@tanstack/react-router'

import { DeptList } from './-components/dept-list'

export const Route = createFileRoute('/(main)/(app)/system/dept')({
  component: DeptList
})
