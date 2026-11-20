import { useDetailQuery, type DetailQueryOptions } from '@/hooks/use-detail-query'
import { getTenant, tenantKeys } from '@/lib/api/tenant'

/** One tenant's record shared by the detail drawer and the edit form; a null id skips the query. */
export function useTenantDetail(tenantId: string | number | null, options: DetailQueryOptions = {}) {
  return useDetailQuery(tenantKeys.detail(tenantId), getTenant, tenantId, options)
}
