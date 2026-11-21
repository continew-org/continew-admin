import type { ColumnDef } from '@tanstack/react-table'

import { Pen, Trash2 } from 'lucide-react'

import { RowActionsMenu } from '@/components/row-actions-menu'
import { TextCell } from '@/components/text-cell'
import { DropdownMenuGroup, DropdownMenuItem, DropdownMenuSeparator } from '@/components/ui/dropdown-menu'
import { usePermission } from '@/hooks/use-permission'
import type { DemoResp } from '@/lib/api/demo'
import type { DataTableFeatures } from '@/lib/data-table-features'

export type DemoAction = 'edit' | 'delete'

/** Every code the row menu can show; the menu is dropped when the user has none of them. */
const ROW_ACTION_PERMISSIONS = ['system:demo:update', 'system:demo:delete']

function RowActions({
  demo,
  onAction
}: {
  demo: DemoResp
  onAction: (action: DemoAction, demo: DemoResp) => void
}) {
  const { hasPermission, hasAnyPermission } = usePermission()

  if (!hasAnyPermission(ROW_ACTION_PERMISSIONS)) {
    return null
  }

  const canEdit = hasPermission('system:demo:update')
  const canDelete = hasPermission('system:demo:delete')

  return (
    <div className="flex items-center justify-center">
      <RowActionsMenu showMenu={canEdit || canDelete}>
        {canEdit ? (
          <DropdownMenuGroup>
            <DropdownMenuItem onClick={() => onAction('edit', demo)}>
              <Pen />
              修改示例
            </DropdownMenuItem>
          </DropdownMenuGroup>
        ) : null}
        {canEdit && canDelete ? <DropdownMenuSeparator /> : null}
        {canDelete ? (
          <DropdownMenuGroup>
            <DropdownMenuItem variant="destructive" onClick={() => onAction('delete', demo)}>
              <Trash2 />
              删除
            </DropdownMenuItem>
          </DropdownMenuGroup>
        ) : null}
      </RowActionsMenu>
    </div>
  )
}

/** Builds the demo table columns; every business column is a plain value cell. */
export function createDemoColumns(
  onAction: (action: DemoAction, demo: DemoResp) => void,
  rowOffset: number
): ColumnDef<DataTableFeatures, DemoResp>[] {
  return [
    {
      id: 'index',
      header: '序号',
      enableHiding: false,
      cell: ({ row }) => <div>{rowOffset + row.index + 1}</div>
    },
    {
      accessorKey: 'name',
      header: '名称',
      cell: ({ row }) => <TextCell value={row.original.name} className="max-w-72 font-medium" />
    },
    {
      accessorKey: 'quantity',
      header: '数量',
      cell: ({ row }) => <div>{row.original.quantity}</div>
    },
    {
      accessorKey: 'onlineTime',
      header: '上线时间',
      cell: ({ row }) => <TextCell value={row.original.onlineTime} />
    },
    {
      accessorKey: 'createUserString',
      header: '创建人',
      cell: ({ row }) => <TextCell value={row.original.createUserString} />
    },
    {
      accessorKey: 'createTime',
      header: '创建时间',
      cell: ({ row }) => <TextCell value={row.original.createTime} />
    },
    {
      id: 'actions',
      header: () => <div className="text-center">操作</div>,
      enableHiding: false,
      cell: ({ row }) => <RowActions demo={row.original} onAction={onAction} />
    }
  ]
}
