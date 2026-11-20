import { createFileRoute } from '@tanstack/react-router'

import { NoticeList } from './-components/notice-list'

export const Route = createFileRoute('/(main)/(app)/system/notice')({
  component: NoticeList
})
