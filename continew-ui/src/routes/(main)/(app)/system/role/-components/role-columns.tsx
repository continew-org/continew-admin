import type { ColumnDef } from '@tanstack/react-table'

import { Eye, KeyRound, PenLine, ShieldCheck, Trash2, Users } from 'lucide-react'

import { FlagBadge, TypeBadge, type TypeTone } from '@/components/data-badge'
import { RowActionsMenu } from '@/components/row-actions-menu'
import { Button } from '@/components/ui/button'
import { DropdownMenuGroup, DropdownMenuItem, DropdownMenuSeparator } from '@/components/ui/dropdown-menu'
import { DATA_SCOPE_MAP, type DataScope, type RoleResp } from '@/lib/api/role'
import { EMPTY_PLACEHOLDER } from '@/lib/constants'
import type { DataTableFeatures } from '@/lib/data-table-features'

/** The row actions the table raises. */
export type RoleAction = 'detail' | 'edit' | 'permission' | 'members' | 'delete'

const DATA_SCOPE_TONES: Record<DataScope, TypeTone> = {
  1: 'blue',
  2: 'teal',
  3: 'teal',
  4: 'neutral',
  5: 'violet'
}

export function DataScopeBadge({ scope }: { scope: DataScope }) {
  return <TypeBadge tone={DATA_SCOPE_TONES[scope]} label={DATA_SCOPE_MAP[scope]} />
}

type TextColumnKey = 'description' | 'createUserString' | 'createTime' | 'updateUserString' | 'updateTime'

function textColumn(key: TextColumnKey, header: string, className?: string): ColumnDef<DataTableFeatures, RoleResp> {
  return {
    accessorKey: key,
    header,
    cell: ({ row }) => <span className={className}>{row.original[key] || EMPTY_PLACEHOLDER}</span>
  }
}

function RowActions({ role, onAction }: { role: RoleResp; onAction: (action: RoleAction, role: RoleResp) => void }) {
  const isSystem = Boolean(role.isSystem)

  return (
    <RowActionsMenu
      primary={
        <Button
          variant="ghost"
          size="icon-sm"
          aria-label="角色详情"
          title="角色详情"
          onClick={() => onAction('detail', role)}
        >
          <Eye />
        </Button>
      }
    >
      <DropdownMenuGroup>
        <DropdownMenuItem disabled={isSystem} onClick={() => onAction('edit', role)}>
          <PenLine />
          修改角色
        </DropdownMenuItem>
        <DropdownMenuItem disabled={isSystem} onClick={() => onAction('permission', role)}>
          <KeyRound />
          功能权限
        </DropdownMenuItem>
        <DropdownMenuItem disabled={isSystem} onClick={() => onAction('members', role)}>
          <Users />
          关联用户
        </DropdownMenuItem>
      </DropdownMenuGroup>
      <DropdownMenuSeparator />
      <DropdownMenuItem variant="destructive" disabled={isSystem} onClick={() => onAction('delete', role)}>
        <Trash2 />
        删除
      </DropdownMenuItem>
    </RowActionsMenu>
  )
}

/** Builds the role table columns; the serial number follows the row's index in the full data set. */
export function createRoleColumns(
  onAction: (action: RoleAction, role: RoleResp) => void
): ColumnDef<DataTableFeatures, RoleResp>[] {
  return [
    {
      id: 'index',
      header: '序号',
      enableHiding: false,
      cell: ({ row }) => <span className="tabular-nums">{row.index + 1}</span>
    },
    {
      accessorKey: 'name',
      header: '角色名称',
      cell: ({ row }) => (
        <div className="flex items-center gap-2">
          <span>{row.original.name}</span>
          <FlagBadge active={row.original.isSystem} activeLabel="内置" icon={<ShieldCheck />} />
        </div>
      )
    },
    {
      accessorKey: 'code',
      header: '角色编码',
      cell: ({ row }) => <span>{row.original.code}</span>
    },
    {
      accessorKey: 'dataScope',
      header: '数据权限',
      cell: ({ row }) => <DataScopeBadge scope={row.original.dataScope} />
    },
    {
      accessorKey: 'sort',
      header: '排序',
      cell: ({ row }) => <span className="tabular-nums">{row.original.sort}</span>
    },
    textColumn('description', '描述', 'inline-block max-w-50 truncate'),
    textColumn('createUserString', '创建人'),
    textColumn('createTime', '创建时间', 'tabular-nums'),
    textColumn('updateUserString', '修改人'),
    textColumn('updateTime', '修改时间', 'tabular-nums'),
    {
      id: 'actions',
      header: '操作',
      enableHiding: false,
      cell: ({ row }) => <RowActions role={row.original} onAction={onAction} />
    }
  ]
}
