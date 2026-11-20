import type { ReactNode } from 'react'

import type { UseQueryResult } from '@tanstack/react-query'

import { QueryState } from '@/components/query-state'

interface DetailGateProps<T> {
  /** The detail query the gate renders. */
  readonly query: UseQueryResult<T>
  /** Overrides the query data, such as a record kept for a closing animation. `null` means "no data". */
  readonly data?: T | null
  /**
   * When true (the default) a background refetch of already-loaded data still counts as loading,
   * so an edit form never seeds from a stale base. Read-only views can pass false to keep showing
   * the previous record while it revalidates.
   */
  readonly requireFresh?: boolean
  readonly errorTitle?: string
  /** Extra classes for the error block, such as a drawer's tighter padding. */
  readonly errorClassName?: string
  /** Rendered under the error block, such as a dialog's cancel button. */
  readonly errorFooter?: ReactNode
  readonly skeleton: ReactNode
  readonly children: (data: T) => ReactNode
}

/**
 * The shared three-state gate for detail queries — skeleton while loading, a retryable error block
 * on failure, and the record once it is ready. Keeping the conditions here means every drawer and
 * dialog that loads a record behaves the same, and none of them renders a form against missing or
 * stale data that a later save would overwrite.
 */
export function DetailGate<T>({
  query,
  data: dataOverride,
  requireFresh = true,
  errorTitle,
  errorClassName,
  errorFooter,
  skeleton,
  children
}: DetailGateProps<T>) {
  const data = dataOverride ?? query.data

  // `fetchStatus !== 'idle'` covers both a first load and a background refetch, keeping one notion
  // of "still settling" for every caller.
  const isSettling = query.fetchStatus !== 'idle'
  const isWaitingForData = data === undefined && (query.isPending || isSettling)
  const isStale = requireFresh && data !== undefined && isSettling

  if (isWaitingForData || isStale) {
    return <>{skeleton}</>
  }

  if (query.isError || data === undefined) {
    return (
      <div className="flex flex-col gap-4">
        <QueryState
          state="error"
          title={errorTitle}
          onRetry={() => void query.refetch()}
          className={errorClassName}
        />
        {errorFooter}
      </div>
    )
  }

  return <>{children(data)}</>
}
