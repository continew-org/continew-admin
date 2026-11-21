import type { ReactTable } from '@tanstack/react-table'

import { DataTable } from '@/components/data-table'
import type { PackageResp } from '@/lib/api/package'
import type { DataTableFeatures } from '@/lib/data-table-features'

/** One skeleton width per column id, so hidden columns cannot shift the placeholder shapes. */
const SKELETON_WIDTHS: Record<string, string> = {
  index: 'mx-auto w-6',
  name: 'w-32',
  status: 'mx-auto w-14 rounded-full',
  sort: 'mx-auto w-8',
  description: 'w-40',
  createUserString: 'w-20',
  createTime: 'w-32',
  updateUserString: 'w-20',
  updateTime: 'w-32',
  actions: 'mx-auto w-8'
}

interface PackageTableProps {
  readonly table: ReactTable<DataTableFeatures, PackageResp>
  readonly isLoading: boolean
  readonly isError: boolean
  readonly isShowingPreviousRows: boolean
  readonly onRetry?: () => void
  readonly fillHeight?: boolean
}

export function PackageTable({ table, ...rest }: PackageTableProps) {
  return (
    <DataTable
      table={table}
      {...rest}
      skeletonWidths={table.getVisibleLeafColumns().map(column => SKELETON_WIDTHS[column.id] ?? 'w-24')}
    />
  )
}
