import { useDetailQuery, type DetailQueryOptions } from '@/hooks/use-detail-query'
import { getUser, userKeys } from '@/lib/api/user'

export function useUserDetail(userId: string | number | null, options: DetailQueryOptions = {}) {
  return useDetailQuery(userKeys.detail(userId), getUser, userId, options)
}
