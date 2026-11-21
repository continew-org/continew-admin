import { useDetailQuery, type DetailQueryOptions } from '@/hooks/use-detail-query'
import { appKeys, getApp } from '@/lib/api/app'

/** One app's record, shared by the detail and edit drawers; a null id skips the query. */
export function useAppDetail(appId: string | number | null, options: DetailQueryOptions = {}) {
  return useDetailQuery(appKeys.detail(appId), getApp, appId, options)
}
