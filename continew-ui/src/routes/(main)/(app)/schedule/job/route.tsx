import { createFileRoute } from '@tanstack/react-router'

import { JobList } from './-components/job-list'

export const Route = createFileRoute('/(main)/(app)/schedule/job')({
  component: JobList
})
