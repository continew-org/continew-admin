import { createFileRoute } from '@tanstack/react-router'

import { AppList } from './-components/app-list'

export const Route = createFileRoute('/(main)/(app)/open/app')({
  component: AppList
})
