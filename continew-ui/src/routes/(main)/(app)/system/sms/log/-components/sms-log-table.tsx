import type { ReactTable } from '@tanstack/react-table'

import { DataTable } from '@/components/data-table'
import type { SmsLogResp } from '@/lib/api/sms-log'
import type { DataTableFeatures } from '@/lib/data-table-features'

/** One skeleton width per column id, so hidden columns cannot shift the placeholder shapes. */
const SKELETON_WIDTHS: Record<string, string> = {
  index: 'mx-auto w-6',
  phone: 'w-24',
  params: 'w-40',
  status: 'mx-auto w-14 rounded-full',
  resMsg: 'w-48',
  createUserString: 'w-20',
  createTime: 'w-32',
  actions: 'mx-auto w-8'
}

interface SmsLogTableProps {
  readonly table: ReactTable<DataTableFeatures, SmsLogResp>
  readonly isLoading: boolean
  readonly isError: boolean
  readonly isShowingPreviousRows: boolean
  readonly onRetry?: () => void
  readonly fillHeight?: boolean
}

export function SmsLogTable({ table, ...rest }: SmsLogTableProps) {
  return (
    <DataTable
      table={table}
      {...rest}
      skeletonWidths={table.getVisibleLeafColumns().map(column => SKELETON_WIDTHS[column.id] ?? 'w-24')}
    />
  )
}
