import { useQuery, useQueryClient } from '@tanstack/react-query'

import { useDetailQuery, type DetailQueryOptions } from '@/hooks/use-detail-query'
import { countUnreadMessages, countUnreadNotices, getMessage, getUserNotice, messageKeys } from '@/lib/api/message'

/**
 * Unread badges for messages and notices. They go stale at once and refetch when the window
 * regains focus, so a dropped WebSocket never leaves the counts stuck.
 */
export function useUnreadCounts() {
  const messages = useQuery({
    queryKey: messageKeys.unreadMessages(),
    queryFn: ({ signal }) => countUnreadMessages(signal),
    staleTime: 0,
    refetchOnWindowFocus: true
  })
  const notices = useQuery({
    queryKey: messageKeys.unreadNotices(),
    queryFn: ({ signal }) => countUnreadNotices(signal),
    staleTime: 0,
    refetchOnWindowFocus: true
  })

  const messageCount = messages.data?.total ?? 0
  const noticeCount = notices.data?.total ?? 0

  return { messageCount, noticeCount, total: messageCount + noticeCount }
}

/** One message's detail. The backend marks it read on this request, so the badges and lists refresh. */
export function useMessageDetail(id: string | number | null, options: DetailQueryOptions = {}) {
  const queryClient = useQueryClient()

  return useDetailQuery(
    messageKeys.detail(id),
    async (messageId, signal) => {
      const detail = await getMessage(messageId, signal)
      void queryClient.invalidateQueries({ queryKey: messageKeys.unread() })
      void queryClient.invalidateQueries({ queryKey: messageKeys.lists() })
      return detail
    },
    id,
    options
  )
}

/** One notice read through the user endpoint, which needs no notice-management permission. */
export function useUserNoticeDetail(id: string | number | null, options: DetailQueryOptions = {}) {
  const queryClient = useQueryClient()

  return useDetailQuery(
    messageKeys.noticeDetail(id),
    async (noticeId, signal) => {
      const detail = await getUserNotice(noticeId, signal)
      void queryClient.invalidateQueries({ queryKey: messageKeys.unread() })
      void queryClient.invalidateQueries({ queryKey: messageKeys.noticeLists() })
      return detail
    },
    id,
    options
  )
}
