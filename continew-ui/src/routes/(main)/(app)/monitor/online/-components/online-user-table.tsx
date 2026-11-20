import type { ReactTable } from '@tanstack/react-table'

import { DataTable } from '@/components/data-table'
import type { OnlineUserResp } from '@/lib/api/online'
import type { DataTableFeatures } from '@/lib/data-table-features'

/** One skeleton width per column id, so hidden columns cannot shift the placeholder shapes. */
const SKELETON_WIDTHS: Record<string, string> = {
  index: 'mx-auto w-6',
  nickname: 'w-24',
  username: 'w-24',
  ip: 'w-24',
  address: 'w-32',
  browser: 'w-20',
  os: 'w-20',
  loginTime: 'w-32',
  lastRefreshTime: 'w-32',
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
