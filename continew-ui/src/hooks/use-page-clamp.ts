import * as React from 'react'

import type { PaginationState } from '@tanstack/react-table'

/** Moves the page index back when the current page no longer has rows. */
export function usePageClamp(
  total: number | undefined,
  pagination: PaginationState,
  setPagination: React.Dispatch<React.SetStateAction<PaginationState>>
) {
  React.useEffect(() => {
    // `undefined` means the total is still unknown (a fetch is pending): do not clamp on it, or the
    // 0 stand-in would reset the page mid-load. A real 0 is a confirmed empty result and must fall
    // back to the first page.
    if (total === undefined) {
      return
    }

    if (pagination.pageIndex * pagination.pageSize >= total) {
      const maxPageIndex = Math.max(0, Math.ceil(total / pagination.pageSize) - 1)
      setPagination(current => (current.pageIndex === maxPageIndex ? current : { ...current, pageIndex: maxPageIndex }))
    }
  }, [total, pagination.pageIndex, pagination.pageSize, setPagination])
}
