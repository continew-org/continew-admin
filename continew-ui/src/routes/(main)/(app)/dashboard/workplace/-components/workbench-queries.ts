import { queryOptions } from '@tanstack/react-query'

import {
  getWorkbenchCommits,
  getWorkbenchReleases,
  getWorkbenchStars,
  isWorkbenchEnabled,
  type WorkbenchRange
} from '@/lib/api/workbench'
import { workbenchKeys } from '@/lib/query-keys'

/**
 * Each chart is cached by its own range. Switching the commit range reuses the six-month Star
 * trend and the range-independent release list.
 */
export const commitsQueryOptions = (range: WorkbenchRange) =>
  queryOptions({
    queryKey: workbenchKeys.commits(range),
    queryFn: ({ signal }) => getWorkbenchCommits(range, signal),
    enabled: isWorkbenchEnabled
  })

export const starsQueryOptions = (range: WorkbenchRange) =>
  queryOptions({
    queryKey: workbenchKeys.stars(range),
    queryFn: ({ signal }) => getWorkbenchStars(range, signal),
    enabled: isWorkbenchEnabled
  })

export const releasesQueryOptions = () =>
  queryOptions({
    queryKey: workbenchKeys.releases(),
    queryFn: ({ signal }) => getWorkbenchReleases(signal),
    enabled: isWorkbenchEnabled
  })
