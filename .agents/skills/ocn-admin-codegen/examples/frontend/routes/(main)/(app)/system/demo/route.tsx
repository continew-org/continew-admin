import { createFileRoute } from '@tanstack/react-router'

import { DemoList } from './-components/demo-list'

export const Route = createFileRoute('/(main)/(app)/system/demo')({
  component: DemoList
})
