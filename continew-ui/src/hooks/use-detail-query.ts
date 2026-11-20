import { skipToken, useQuery, type QueryKey } from '@tanstack/react-query'

import { DETAIL_STALE_TIME_MS } from '@/lib/query-client'

export interface DetailQueryOptions {
  /** Whether the caller is showing the record; closed drawers stay idle. */
  readonly enabled?: boolean
}

/**
 * One record's detail query shared by detail, edit and view drawers: a null id skips the
 * fetch, closed drawers stay idle, and every open loads a fresh record.
 */
export function useDetailQuery<TRow>(
  queryKey: QueryKey,
  fetchRecord: (id: string | number, signal: AbortSignal) => Promise<TRow>,
  id: string | number | null,
  { enabled = true }: DetailQueryOptions = {}
) {
  return useQuery({
    queryKey,
    queryFn: id === null ? skipToken : ({ signal }) => fetchRecord(id, signal),
    enabled,
    staleTime: DETAIL_STALE_TIME_MS,
    refetchOnMount: 'always'
  })
}
