import type { ColumnDef } from '@tanstack/react-table'

import { Eye, KeyRound, Pen, RotateCcw, Trash2 } from 'lucide-react'

import { RowActionsMenu } from '@/components/row-actions-menu'
import { StatusCell } from '@/components/status-cell'
import { TextCell } from '@/components/text-cell'
import { Button } from '@/components/ui/button'
import { DropdownMenuGroup, DropdownMenuItem, DropdownMenuSeparator } from '@/components/ui/dropdown-menu'
import type { AppResp } from '@/lib/api/app'
import type { DataTableFeatures } from '@/lib/data-table-features'

import { AccessKeyCell, ExpireTimeCell } from './app-display'

/** The row actions the table raises. */
export type AppAction = 'detail' | 'edit' | 'secret' | 'resetSecret' | 'delete'

type TextColumnKey = 'description' | 'createUserString' | 'createTime' | 'updateUserString' | 'updateTime'

function textColumn(key: TextColumnKey, header: string): ColumnDef<DataTableFeatures, AppResp> {
  return {
    accessorKey: key,
    header,
    cell: ({ row }) => <TextCell value={row.original[key]} />
  }
}

function RowActions({ app, onAction }: { app: AppResp; onAction: (action: AppAction, app: AppResp) => void }) {
  const isLocked = Boolean(app.disabled)

  return (
    <div className="flex items-center">
      <RowActionsMenu
        primary={
          <Button
            variant="ghost"
            size="icon-sm"
            aria-label="应用详情"
            title="应用详情"
            onClick={() => onAction('detail', app)}
          >
            <Eye />
          </Button>
        }
      >
        <DropdownMenuGroup>
          <DropdownMenuItem disabled={isLocked} onClick={() => onAction('edit', app)}>
            <Pen />
            修改应用
          </DropdownMenuItem>
          <DropdownMenuItem onClick={() => onAction('secret', app)}>
            <KeyRound />
            查看密钥
          </DropdownMenuItem>
          <DropdownMenuItem disabled={isLocked} onClick={() => onAction('resetSecret', app)}>
            <RotateCcw />
            重置密钥
          </DropdownMenuItem>
        </DropdownMenuGroup>
        <DropdownMenuSeparator />
        <DropdownMenuItem variant="destructive" disabled={isLocked} onClick={() => onAction('delete', app)}>
          <Trash2 />
          删除
        </DropdownMenuItem>
      </RowActionsMenu>
    </div>
  )
}

/**
 * Builds the app table columns; `rowOffset` keeps the serial number continuous across pages.
 */
export function createAppColumns(onAction: (action: AppAction, app: AppResp) => void, rowOffset: number) {
  const columns: ColumnDef<DataTableFeatures, AppResp>[] = [
    {
      id: 'index',
      header: '序号',
      enableHiding: false,
      cell: ({ row }) => <div className="tabular-nums">{rowOffset + row.index + 1}</div>
    },
    {
      accessorKey: 'name',
      header: '应用名称',
      cell: ({ row }) => <span className="font-medium text-foreground">{row.original.name}</span>
    },
    {
      accessorKey: 'accessKey',
      header: 'Access Key',
      cell: ({ row }) => <AccessKeyCell accessKey={row.original.accessKey} />
    },
    {
      accessorKey: 'status',
      header: '状态',
      cell: ({ row }) => <StatusCell status={row.original.status} />
    },
    {
      accessorKey: 'expireTime',
      header: '失效时间',
      cell: ({ row }) => <ExpireTimeCell expireTime={row.original.expireTime} />
    },
    textColumn('description', '描述'),
    textColumn('createUserString', '创建人'),
    textColumn('createTime', '创建时间'),
    textColumn('updateUserString', '修改人'),
    textColumn('updateTime', '修改时间'),
    {
      id: 'actions',
      header: '操作',
      enableHiding: false,
      cell: ({ row }) => <RowActions app={row.original} onAction={onAction} />
    }
  ]

  return columns
}
