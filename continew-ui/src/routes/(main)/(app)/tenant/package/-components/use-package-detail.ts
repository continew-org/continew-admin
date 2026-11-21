import { useDetailQuery, type DetailQueryOptions } from '@/hooks/use-detail-query'
import { getPackage, packageKeys } from '@/lib/api/package'

/**
 * One package's record, shared by the detail and edit drawers through one key. Read-only detail
 * uses the short cache; the edit drawer refreshes before initializing its form.
 */
export function usePackageDetail(packageId: string | number | null, options: DetailQueryOptions = {}) {
  return useDetailQuery(packageKeys.detail(packageId), getPackage, packageId, options)
}
