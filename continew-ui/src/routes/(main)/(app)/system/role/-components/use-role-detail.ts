import { useDetailQuery, type DetailQueryOptions } from '@/hooks/use-detail-query'
import { getRole, roleKeys } from '@/lib/api/role'

/** One role's detailed record (menuIds, deptIds and the two checkStrictly settings); a null id skips it. */
export function useRoleDetail(roleId: string | number | null, options: DetailQueryOptions = {}) {
  return useDetailQuery(roleKeys.detail(roleId), getRole, roleId, options)
}
