import type { ReactNode } from 'react'

import { cn } from 'cn'
import { ChevronDown } from 'lucide-react'

import { QueryState } from '@/components/query-state'
import { Button } from '@/components/ui/button'
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from '@/components/ui/collapsible'
import { Skeleton } from '@/components/ui/skeleton'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import type { StorageResp } from '@/lib/api/storage'

import type { StorageColumn } from './columns'

interface StorageGroupPanelProps {
  readonly title: string
  readonly icon: ReactNode
  readonly minWidth: string
  readonly items: StorageResp[]
  readonly columns: StorageColumn[]
  readonly isLoading: boolean
}

/**
 * One storage type as a collapsible group panel holding its table, sharing the dictionary screen's
 * section shape: a top-separated section whose header carries the group name and row count.
 */
export function StorageGroupPanel({ title, icon, minWidth, items, columns, isLoading }: StorageGroupPanelProps) {
  return (
    <Collapsible defaultOpen className="flex flex-col border-t">
      <div className="flex flex-col gap-2 px-5 py-3 sm:flex-row sm:items-center">
        <CollapsibleTrigger
          render={
            <Button
              variant="ghost"
              className="group -ml-2 h-auto w-full justify-start gap-2 px-2 py-1 hover:bg-transparent aria-expanded:bg-transparent sm:flex-1"
            />
          }
        >
          <ChevronDown className="group-data-panel-open:rotate-180" />
          <span className="flex min-w-0 items-center gap-1.5 text-left">
            {icon}
            <span className="shrink-0 leading-none font-medium">{title}</span>
            <span className="text-sm text-muted-foreground">({items.length})</span>
          </span>
        </CollapsibleTrigger>
      </div>

      <CollapsibleContent>
        {items.length > 0 ? (
          <StorageTable items={items} columns={columns} minWidth={minWidth} />
        ) : isLoading ? (
          <StorageTableSkeleton columns={columns} minWidth={minWidth} />
        ) : (
          <QueryState
            state="empty"
            title={`暂无${title}配置`}
            className="min-h-24 rounded-none border-t bg-muted/50 p-4"
          />
        )}
      </CollapsibleContent>
    </Collapsible>
  )
}

function StorageTable({
  items,
  columns,
  minWidth
}: {
  items: StorageResp[]
  columns: StorageColumn[]
  minWidth: string
}) {
  return (
    <div className="scrollbar-thin [scrollbar-color:var(--border)_transparent] overflow-x-auto **:data-[slot=table-container]:overflow-visible [&::-webkit-scrollbar]:h-1 [&::-webkit-scrollbar-thumb]:rounded-full [&::-webkit-scrollbar-thumb]:bg-border [&::-webkit-scrollbar-track]:bg-transparent">
      <Table className={cn("table-fixed **:data-[slot='table-cell']:px-5 **:data-[slot='table-head']:px-5", minWidth)}>
        <StorageColGroup columns={columns} />

        <TableHeader className="bg-muted/50 [&_tr]:border-y">
          <TableRow>
            {columns.map(column => (
              <TableHead key={column.id} className={cn('font-medium', column.headerClassName)}>
                {column.header}
              </TableHead>
            ))}
          </TableRow>
        </TableHeader>

        <TableBody className="**:data-[slot='table-row']:hover:bg-transparent">
          {items.map(item => (
            <TableRow key={String(item.id)}>
              {columns.map(column => (
                <TableCell key={column.id}>{column.cell(item)}</TableCell>
              ))}
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  )
}

function StorageTableSkeleton({ columns, minWidth }: { columns: StorageColumn[]; minWidth: string }) {
  return (
    <div className="scrollbar-thin [scrollbar-color:var(--border)_transparent] overflow-x-auto **:data-[slot=table-container]:overflow-visible [&::-webkit-scrollbar]:h-1 [&::-webkit-scrollbar-thumb]:rounded-full [&::-webkit-scrollbar-thumb]:bg-border [&::-webkit-scrollbar-track]:bg-transparent">
      <Table className={cn("table-fixed **:data-[slot='table-cell']:px-5 **:data-[slot='table-head']:px-5", minWidth)}>
        <StorageColGroup columns={columns} />

        <TableHeader className="bg-muted/50 [&_tr]:border-y">
          <TableRow>
            {columns.map(column => (
              <TableHead key={column.id} className={cn('font-medium', column.headerClassName)}>
                {column.header}
              </TableHead>
            ))}
          </TableRow>
        </TableHeader>

        <TableBody className="**:data-[slot='table-row']:hover:bg-transparent">
          {Array.from({ length: 2 }).map((_, rowIndex) => (
            <TableRow key={rowIndex}>
              {columns.map(column => (
                <TableCell key={column.id}>
                  <Skeleton className={cn('h-4', column.id === 'actions' ? 'ml-auto w-8' : 'w-24')} />
                </TableCell>
              ))}
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  )
}

function StorageColGroup({ columns }: { columns: StorageColumn[] }) {
  return (
    <colgroup>
      {columns.map(column => (
        <col key={column.id} className={column.width} />
      ))}
    </colgroup>
  )
}
