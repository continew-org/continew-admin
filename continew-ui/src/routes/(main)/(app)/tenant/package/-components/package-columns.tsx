import type { ColumnDef } from '@tanstack/react-table'
import { Eye, Pen, Trash2 } from 'lucide-react'

import { RowActionsMenu } from '@/components/row-actions-menu'
import { StatusCell } from '@/components/status-cell'
import { TextCell } from '@/components/text-cell'
import { Button } from '@/components/ui/button'
import { DropdownMenuGroup, DropdownMenuItem, DropdownMenuSeparator } from '@/components/ui/dropdown-menu'
import type { PackageResp } from '@/lib/api/package'
import type { DataTableFeatures } from '@/lib/data-table-features'

type PackageColumn = ColumnDef<DataTableFeatures, PackageResp>
type TextColumnKey = 'name' | 'description' | 'createUserString' | 'createTime' | 'updateUserString' | 'updateTime'

/** The row actions the table raises. */
export type PackageAction = 'detail' | 'edit' | 'delete'

/** Labels for the columns whose header is a node, which the hide-columns menu cannot read. */
export const PACKAGE_COLUMN_LABELS: Record<string, string> = {
  status: '状态',
  sort: '排序'
}

interface PackageColumnOptions {
  /** The index of the first row on the current page, so 序号 stays continuous. */
  readonly rowOffset: number
  readonly canGet: boolean
  readonly canUpdate: boolean
  readonly canDelete: boolean
  readonly onAction: (action: PackageAction, pkg: PackageResp) => void
}

function textColumn(key: TextColumnKey, header: string): PackageColumn {
  return {
    accessorKey: key,
    header,
    cell: ({ row }) => <TextCell value={row.original[key]} />
  }
}

function RowActions({
  pkg,
  canGet,
  canUpdate,
  canDelete,
  onAction
}: {
  pkg: PackageResp
  canGet: boolean
  canUpdate: boolean
  canDelete: boolean
  onAction: (action: PackageAction, pkg: PackageResp) => void
}) {
  const hasGroup = canUpdate

  return (
    <div className="flex items-center justify-center">
      <RowActionsMenu
        showMenu={hasGroup || canDelete}
        primary={
          canGet ? (
            <Button
              variant="ghost"
              size="icon-sm"
              aria-label="套餐详情"
              title="套餐详情"
              onClick={() => onAction('detail', pkg)}
            >
              <Eye />
            </Button>
          ) : undefined
        }
      >
        {hasGroup ? (
          <DropdownMenuGroup>
            <DropdownMenuItem onClick={() => onAction('edit', pkg)}>
              <Pen />
              修改套餐
            </DropdownMenuItem>
          </DropdownMenuGroup>
        ) : null}
        {hasGroup && canDelete ? <DropdownMenuSeparator /> : null}
        {canDelete ? (
          <DropdownMenuGroup>
            <DropdownMenuItem
              variant="destructive"
              disabled={Boolean(pkg.disabled)}
              onClick={() => onAction('delete', pkg)}
            >
              <Trash2 />
              删除
            </DropdownMenuItem>
          </DropdownMenuGroup>
        ) : null}
      </RowActionsMenu>
    </div>
  )
}

export function createPackageColumns({ rowOffset, canGet, canUpdate, canDelete, onAction }: PackageColumnOptions) {
  const columns: PackageColumn[] = [
    {
      id: 'index',
      header: '序号',
      enableHiding: false,
      cell: ({ row }) => (
        <div className="text-center text-sm text-muted-foreground tabular-nums">{rowOffset + row.index + 1}</div>
      )
    },
    textColumn('name', '名称'),
    {
      accessorKey: 'status',
      header: () => <div className="text-center">状态</div>,
      cell: ({ row }) => (
        <div className="flex justify-center">
          <StatusCell status={row.original.status} />
        </div>
      )
    },
    {
      accessorKey: 'sort',
      header: () => <div className="text-center">排序</div>,
      cell: ({ row }) => <div className="text-center text-sm tabular-nums">{row.original.sort}</div>
    },
    textColumn('description', '描述'),
    textColumn('createUserString', '创建人'),
    textColumn('createTime', '创建时间'),
    textColumn('updateUserString', '修改人'),
    textColumn('updateTime', '修改时间')
  ]

  if (canGet || canUpdate || canDelete) {
    columns.push({
      id: 'actions',
      header: () => <div className="text-center">操作</div>,
      enableHiding: false,
      cell: ({ row }) => (
        <RowActions
          pkg={row.original}
          canGet={canGet}
          canUpdate={canUpdate}
          canDelete={canDelete}
          onAction={onAction}
        />
      )
    })
  }

  return columns
}
