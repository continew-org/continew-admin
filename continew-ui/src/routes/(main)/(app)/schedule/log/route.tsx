import { createFileRoute } from '@tanstack/react-router'
import { z } from 'zod'

import { JobLogList } from './-components/job-log-list'

const searchSchema = z.object({
  /** Carried from the job page's 查看日志 action. */
  jobId: z
    .preprocess(
      value => (value === '' || value === null ? undefined : value),
      z.coerce.number().int().positive().optional()
    )
    .catch(undefined),
  jobName: z.string().optional(),
  groupName: z.string().optional()
})

export const Route = createFileRoute('/(main)/(app)/schedule/log')({
  validateSearch: searchSchema,
  component: ScheduleLogPage
})

function ScheduleLogPage() {
  const search = Route.useSearch()

  return <JobLogList initialJobId={search.jobId} initialJobName={search.jobName} initialGroupName={search.groupName} />
}
