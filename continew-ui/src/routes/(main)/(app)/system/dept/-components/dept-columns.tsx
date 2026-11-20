import type { ColumnDef } from '@tanstack/react-table'

import { Building2, ChevronRight, PenLine, Plus, Trash2 } from 'lucide-react'

import { FlagBadge } from '@/components/data-badge'
import { RowActionsMenu } from '@/components/row-actions-menu'
import { StatusCell } from '@/components/status-cell'
import { DropdownMenuGroup, DropdownMenuItem, DropdownMenuSeparator } from '@/components/ui/dropdown-menu'
import { usePermission } from '@/hooks/use-permission'
import type { DeptResp } from '@/lib/api/dept'
import { EMPTY_PLACEHOLDER } from '@/lib/constants'
import type { DataTableFeatures } from '@/lib/data-table-features'
import { cn } from 'cn'

export type DeptAction = 'edit' | 'add-child' | 'delete'

type TextColumnKey = 'description' | 'createUserString' | 'createTime' | 'updateUserString' | 'updateTime'

function textColumn(key: TextColumnKey, header: string): ColumnDef<DataTableFeatures, DeptResp> {
  return {
    accessorKey: key,
    header,
    cell: ({ row }) => <span>{row.original[key] || EMPTY_PLACEHOLDER}</span>
  }
}

function RowActions({ dept, onAction }: { dept: DeptResp; onAction: (action: DeptAction, dept: DeptResp) => void }) {
  const { hasPermission } = usePermission()

  return (
    <div className="flex items-center justify-center">
      <RowActionsMenu>
        <DropdownMenuGroup>
          {hasPermission('system:dept:update') && (
            <DropdownMenuItem onClick={() => onAction('edit', dept)}>
              <PenLine />
              修改部门
            </DropdownMenuItem>
          )}
          {hasPermission('system:dept:create') && (
            <DropdownMenuItem onClick={() => onAction('add-child', dept)}>
              <Plus />
              添加下级
            </DropdownMenuItem>
          )}
        </DropdownMenuGroup>
        {hasPermission('system:dept:delete') && (
          <>
            <DropdownMenuSeparator />
            <DropdownMenuGroup>
              <DropdownMenuItem
                variant="destructive"
                disabled={dept.isSystem}
                onClick={() => onAction('delete', dept)}
              >
                <Trash2 />
                {dept.isSystem ? '内置部门' : '删除'}
              </DropdownMenuItem>
            </DropdownMenuGroup>
          </>
        )}
      </RowActionsMenu>
    </div>
  )
}

export function createDeptColumns(
  onAction: (action: DeptAction, dept: DeptResp) => void
): ColumnDef<DataTableFeatures, DeptResp>[] {
  return [
    {
      accessorKey: 'name',
      header: '部门名称',
      enableHiding: false,
      cell: ({ row }) => {
        const dept = row.original
        const canExpand = row.getCanExpand()
        const isExpanded = row.getIsExpanded()
        const depth = row.depth

        return (
          <div className="flex items-center gap-2" style={{ paddingLeft: `${depth * 24}px` }}>
            {canExpand ? (
              <button
                type="button"
                onClick={e => {
                  e.stopPropagation()
                  row.toggleExpanded()
                }}
                className="flex size-5 shrink-0 items-center justify-center rounded-xs text-muted-foreground transition-colors hover:bg-muted hover:text-foreground"
              >
                <ChevronRight
                  className={cn('size-4 transition-transform duration-200', isExpanded && 'rotate-90 text-foreground')}
                />
              </button>
            ) : (
              <span className="size-5 shrink-0" />
            )}

            <Building2 className="size-4 shrink-0 text-muted-foreground" />

            <span className={cn('truncate text-sm', depth === 0 ? 'font-medium text-foreground' : 'text-foreground')}>
              {dept.name}
            </span>
          </div>
        )
      }
    },
    {
      accessorKey: 'status',
      header: '状态',
      cell: ({ row }) => <StatusCell status={row.original.status} />
    },
    {
      accessorKey: 'sort',
      header: '排序',
      cell: ({ row }) => <span>{row.original.sort}</span>
    },
    {
      accessorKey: 'isSystem',
      header: '系统内置',
      cell: ({ row }) => <FlagBadge active={row.original.isSystem} activeLabel="内置" inactiveLabel="自定义" />
    },
    textColumn('description', '描述'),
    textColumn('createUserString', '创建人'),
    textColumn('createTime', '创建时间'),
    textColumn('updateUserString', '修改人'),
    textColumn('updateTime', '修改时间'),
    {
      id: 'actions',
      header: () => <div className="text-center">操作</div>,
      enableHiding: false,
      cell: ({ row }) => <RowActions dept={row.original} onAction={onAction} />
    }
  ]
}
