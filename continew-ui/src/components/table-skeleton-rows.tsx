import type { ReactTable, RowData } from '@tanstack/react-table'

import { cn } from 'cn'

import { Skeleton } from '@/components/ui/skeleton'
import { TableCell, TableRow } from '@/components/ui/table'
import type { DataTableFeatures } from '@/lib/data-table-features'

const DEFAULT_ROW_COUNT = 5
const DEFAULT_WIDTH = 'w-24'

interface TableSkeletonRowsProps<TRow extends RowData> {
  readonly table: ReactTable<DataTableFeatures, TRow>
  /**
   * One width class per column, in order, so a screen can imitate its own cell shapes: a narrow
   * centered serial number, a wide name, a status pill, a small centered action. Columns past the
   * end of the list reuse the last entry, which stands in for the rest of the row.
   */
  readonly widths?: readonly string[]
  readonly rowCount?: number
}

/** The placeholder rows a table shows while its first page is loading. */
export function TableSkeletonRows<TRow extends RowData>({
  table,
  widths = [],
  rowCount = DEFAULT_ROW_COUNT
}: TableSkeletonRowsProps<TRow>) {
  const columnCount = table.getVisibleLeafColumns().length
  const fallbackWidth = widths[widths.length - 1] ?? DEFAULT_WIDTH

  return (
    <>
      {Array.from({ length: rowCount }).map((_, rowIndex) => (
        <TableRow key={rowIndex} className="hover:bg-transparent">
          {Array.from({ length: columnCount }).map((_, colIndex) => (
            <TableCell key={colIndex} className="py-3.5 align-middle">
              <Skeleton className={cn('h-4', widths[colIndex] ?? fallbackWidth)} />
            </TableCell>
          ))}
        </TableRow>
      ))}
    </>
  )
}
