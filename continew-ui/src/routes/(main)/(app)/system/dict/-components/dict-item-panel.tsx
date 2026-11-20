import * as React from 'react'

import type { PaginationState } from '@tanstack/react-table'

import { Plus, Trash2 } from 'lucide-react'

import { DataTable } from '@/components/data-table'
import { RowActionsMenu } from '@/components/row-actions-menu'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { DropdownMenuItem } from '@/components/ui/dropdown-menu'
import { useListTable } from '@/hooks/use-list-table'
import { dictKeys, listDictItems, type DictItemQuery, type DictItemResp, type DictResp } from '@/lib/api/dict'
import type { PageResp } from '@/lib/api/types'

import { createDictItemColumns, type DictItemAction } from './dict-columns'

const DEFAULT_SORT = 'sort,asc'

interface DictItemPanelProps {
  readonly dict: DictResp
  readonly canCreateItem: boolean
  readonly canUpdateItem: boolean
  readonly canDeleteItem: boolean
  readonly canDeleteDict: boolean
  readonly onCreateItem: (dict: DictResp) => void
  readonly onEditItem: (dict: DictResp, item: DictItemResp) => void
  readonly onDeleteItem: (item: DictItemResp) => void
  readonly onDeleteDict: (dict: DictResp) => void
}

/** The right pane of the dictionary screen: the selected dictionary and its paginated items. */
export function DictItemPanel({
  dict,
  canCreateItem,
  canUpdateItem,
  canDeleteItem,
  canDeleteDict,
  onCreateItem,
  onEditItem,
  onDeleteItem,
  onDeleteDict
}: DictItemPanelProps) {
  const handleAction = React.useCallback(
    (action: DictItemAction, item: DictItemResp) => {
      if (action === 'update') {
        onEditItem(dict, item)
        return
      }

      onDeleteItem(item)
    },
    [dict, onEditItem, onDeleteItem]
  )

  const buildQuery = React.useCallback(
    (pagination: PaginationState) => {
      const params: DictItemQuery = {
        dictId: dict.id,
        page: pagination.pageIndex + 1,
        size: pagination.pageSize,
        sort: DEFAULT_SORT
      }
      return { queryKey: dictKeys.itemList(params), queryFn: (signal: AbortSignal) => listDictItems(params, signal) }
    },
    [dict.id]
  )

  const buildColumns = React.useCallback(
    (rowOffset: number) =>
      createDictItemColumns({ canUpdate: canUpdateItem, canDelete: canDeleteItem, onAction: handleAction }, rowOffset),
    [canUpdateItem, canDeleteItem, handleAction]
  )

  const { query, table } = useListTable<DictItemResp, PageResp<DictItemResp>>({
    buildQuery,
    buildColumns,
    getRowId: row => String(row.id)
  })

  const canDeleteThisDict = canDeleteDict && !dict.isSystem

  return (
    <div className="flex min-h-0 flex-1 flex-col">
      <div className="flex shrink-0 flex-col gap-2 px-4 py-2 md:h-14 md:flex-row md:items-center md:justify-between md:gap-3 md:py-0">
        <div className="flex min-w-0 flex-wrap items-center gap-x-2 gap-y-1">
          <h3 className="truncate text-base font-medium">{dict.name}</h3>
          <span className="shrink-0 text-sm text-muted-foreground">({dict.code})</span>
          {dict.isSystem && (
            <Badge variant="outline" className="shrink-0 px-1 py-0 text-[10px] font-normal">
              内置
            </Badge>
          )}
          {dict.description ? (
            <span className="hidden min-w-0 truncate text-xs text-muted-foreground sm:inline">{dict.description}</span>
          ) : null}
        </div>

        <div className="flex items-center gap-2">
          {canCreateItem && (
            <Button onClick={() => onCreateItem(dict)}>
              <Plus />
              新增字典项
            </Button>
          )}

          {canDeleteThisDict && (
            <RowActionsMenu triggerVariant="outline" triggerSize="icon">
              <DropdownMenuItem variant="destructive" onClick={() => onDeleteDict(dict)}>
                <Trash2 />
                删除字典
              </DropdownMenuItem>
            </RowActionsMenu>
          )}
        </div>
      </div>

      <DataTable
        table={table}
        isLoading={query.isPending}
        isError={query.isError}
        isShowingPreviousRows={query.isPlaceholderData}
        onRetry={() => void query.refetch()}
        emptyTitle="暂无字典项"
        fillHeight
        skeletonWidths={[
          'mx-auto w-6',
          'w-16',
          'w-20',
          'mx-auto w-12 rounded-full',
          'mx-auto w-8',
          'w-32',
          'w-32',
          'ml-auto w-8'
        ]}
      />
    </div>
  )
}
