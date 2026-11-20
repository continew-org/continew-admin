import { type MouseEvent, useState } from 'react'

import type { ReactTable, RowData } from '@tanstack/react-table'
import { Root as AnimatePagination } from '@animate-ui/react/primitives/pagination'

import { cn } from 'cn'

import { PaginationArrow } from '@/components/pagination-arrow'
import { Input } from '@/components/ui/input'
import { PaginationContent, PaginationEllipsis, PaginationItem, PaginationLink } from '@/components/ui/pagination'
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import type { DataTableFeatures } from '@/lib/data-table-features'

const PAGE_SIZE_OPTIONS = [10, 20, 30, 40, 50]

type PageItem = number | 'ellipsis-left' | 'ellipsis-right'

/**
 * The page numbers to show, with an ellipsis standing in for the runs left out. The first and the
 * last page are always listed, so a jump back to either end stays one click away.
 */
function getPageNumbers(currentPage: number, pageCount: number): PageItem[] {
  if (pageCount <= 7) {
    return Array.from({ length: pageCount }, (_, index) => index + 1)
  }

  if (currentPage <= 4) {
    return [1, 2, 3, 4, 5, 'ellipsis-right', pageCount]
  }

  if (currentPage >= pageCount - 3) {
    return [1, 'ellipsis-left', pageCount - 4, pageCount - 3, pageCount - 2, pageCount - 1, pageCount]
  }

  return [1, 'ellipsis-left', currentPage - 1, currentPage, currentPage + 1, 'ellipsis-right', pageCount]
}

function preventPaginationNavigation(event: MouseEvent<HTMLAnchorElement>) {
  event.preventDefault()
}

interface TablePaginationProps<TRow extends RowData> {
  readonly table: ReactTable<DataTableFeatures, TRow>
  /** A fill-height table keeps the last row's border, so the footer drops its own top border. */
  readonly showTopBorder?: boolean
}

/** The pagination footer shared by the table screens: page size, row count and the page links. */
export function TablePagination<TRow extends RowData>({ table, showTopBorder = true }: TablePaginationProps<TRow>) {
  const [jumpValue, setJumpValue] = useState('')
  const rowCount = table.getRowCount()

  if (rowCount === 0) {
    return null
  }

  const pageCount = Math.max(table.getPageCount(), 1)
  const currentPage = Math.min(table.state.pagination.pageIndex + 1, pageCount)
  const pageNumbers = getPageNumbers(currentPage, pageCount)

  // Type a page and press Enter (or blur): clamp to the real range, then clear the field.
  const commitJump = () => {
    const target = Number(jumpValue)
    if (Number.isFinite(target) && target >= 1) {
      table.setPageIndex(Math.min(Math.trunc(target), pageCount) - 1)
    }
    setJumpValue('')
  }

  return (
    <div className={cn('flex flex-wrap items-center justify-between gap-3 px-4 py-4', showTopBorder && 'border-t')}>
      <div className="flex flex-wrap items-center gap-x-4 gap-y-2 text-sm text-muted-foreground">
        <div className="flex shrink-0 items-center gap-2">
          <span>每页</span>
          <Select
            value={`${table.state.pagination.pageSize}`}
            onValueChange={value => table.setPageSize(Number(value))}
          >
            <SelectTrigger size="sm" className="w-20">
              <SelectValue />
            </SelectTrigger>
            <SelectContent side="top">
              <SelectGroup>
                {PAGE_SIZE_OPTIONS.map(pageSize => (
                  <SelectItem key={pageSize} value={`${pageSize}`}>
                    {pageSize}
                  </SelectItem>
                ))}
              </SelectGroup>
            </SelectContent>
          </Select>
        </div>
        <span className="shrink-0 tabular-nums">
          共 {rowCount} 条，第 {currentPage} / {pageCount} 页
        </span>
        <div className="flex shrink-0 items-center gap-2 whitespace-nowrap">
          <span>跳至</span>
          <Input
            value={jumpValue}
            onChange={event => setJumpValue(event.target.value.replace(/\D/g, ''))}
            onKeyDown={event => {
              if (event.key === 'Enter') {
                commitJump()
              }
            }}
            onBlur={commitJump}
            inputMode="numeric"
            aria-label="跳转到页码"
            className="h-7 w-14 px-2 text-center text-sm tabular-nums"
          />
          <span>页</span>
        </div>
      </div>

      <AnimatePagination
        role="navigation"
        aria-label="pagination"
        className="mx-0 flex w-auto justify-start md:justify-end"
      >
        <PaginationContent>
          <PaginationItem>
            <PaginationArrow
              direction="previous"
              disabled={!table.getCanPreviousPage()}
              onClick={() => table.previousPage()}
            />
          </PaginationItem>
          {pageNumbers.map(item =>
            typeof item === 'number' ? (
              <PaginationItem key={`page-${item}`}>
                <PaginationLink
                  href="#"
                  isActive={table.state.pagination.pageIndex === item - 1}
                  onClick={event => {
                    preventPaginationNavigation(event)
                    table.setPageIndex(item - 1)
                  }}
                >
                  {item}
                </PaginationLink>
              </PaginationItem>
            ) : (
              <PaginationItem key={item}>
                <PaginationEllipsis />
              </PaginationItem>
            )
          )}
          <PaginationItem>
            <PaginationArrow direction="next" disabled={!table.getCanNextPage()} onClick={() => table.nextPage()} />
          </PaginationItem>
        </PaginationContent>
      </AnimatePagination>
    </div>
  )
}
