import type { ReactTable } from '@tanstack/react-table'

import { DataTable } from '@/components/data-table'
import type { LogResp } from '@/lib/api/log'
import type { DataTableFeatures } from '@/lib/data-table-features'

/** One skeleton width per column id, so hidden columns cannot shift the placeholder shapes. */
const SKELETON_WIDTHS: Record<string, string> = {
  index: 'mx-auto w-6',
  createTime: 'w-28',
  createUserString: 'w-20',
  description: 'w-32',
  module: 'mx-auto w-16',
  status: 'mx-auto w-14 rounded-full',
  ip: 'w-24',
  address: 'w-28',
  timeTaken: 'mx-auto w-12 rounded-full',
  browser: 'w-20',
  os: 'w-20'
}

interface LogTableProps {
  readonly table: ReactTable<DataTableFeatures, LogResp>
  readonly isLoading: boolean
  readonly isError: boolean
  readonly isShowingPreviousRows: boolean
  readonly onRetry?: () => void
  readonly fillHeight?: boolean
}

export function LogTable({ table, ...rest }: LogTableProps) {
  return (
    <DataTable
      table={table}
      {...rest}
      skeletonWidths={table.getVisibleLeafColumns().map(column => SKELETON_WIDTHS[column.id] ?? 'w-24')}
    />
  )
}
