import type { ReactTable } from '@tanstack/react-table'

import { DataTable } from '@/components/data-table'
import type { OnlineUserResp } from '@/lib/api/online'
import type { DataTableFeatures } from '@/lib/data-table-features'

/** One skeleton width per column id, so hidden columns cannot shift the placeholder shapes. */
const SKELETON_WIDTHS: Record<string, string> = {
  user: 'w-28',
  ip: 'w-24',
  address: 'w-32',
  device: 'w-32',
  loginTime: 'w-32',
  lastActiveTime: 'w-32',
  status: 'w-12',
  actions: 'mx-auto w-12'
}

interface OnlineUserTableProps {
  readonly table: ReactTable<DataTableFeatures, OnlineUserResp>
  readonly isLoading: boolean
  readonly isError: boolean
  readonly isShowingPreviousRows: boolean
  readonly onRetry?: () => void
  readonly fillHeight?: boolean
}

export function OnlineUserTable({ table, ...rest }: OnlineUserTableProps) {
  return (
    <DataTable
      table={table}
      {...rest}
      skeletonWidths={table.getVisibleLeafColumns().map(column => SKELETON_WIDTHS[column.id] ?? 'w-24')}
    />
  )
}
