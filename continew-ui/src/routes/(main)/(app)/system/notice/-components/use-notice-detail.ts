import { useDetailQuery, type DetailQueryOptions } from '@/hooks/use-detail-query'
import { getNotice, noticeKeys } from '@/lib/api/notice'

/** One notice's record shared by the form, detail and view drawers; a null id skips the query. */
export function useNoticeDetail(noticeId: string | number | null, options: DetailQueryOptions = {}) {
  return useDetailQuery(noticeKeys.detail(noticeId), getNotice, noticeId, options)
}
