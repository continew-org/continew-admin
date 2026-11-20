import type { ColumnDef } from '@tanstack/react-table'

import {
  ChevronRight,
  Database,
  ExternalLink,
  EyeOff,
  Folder,
  PenLine,
  Plus,
  Trash2
} from 'lucide-react'

import { BooleanCell, TypeBadge, type TypeTone } from '@/components/data-badge'
import { RowActionsMenu } from '@/components/row-actions-menu'
import { StatusCell } from '@/components/status-cell'
import { Badge } from '@/components/ui/badge'
import { DropdownMenuGroup, DropdownMenuItem, DropdownMenuSeparator } from '@/components/ui/dropdown-menu'
import { usePermission } from '@/hooks/use-permission'
import { MENU_TYPE_MAP, type MenuResp, type MenuType } from '@/lib/api/menu'
import { EMPTY_PLACEHOLDER } from '@/lib/constants'
import type { DataTableFeatures } from '@/lib/data-table-features'
import { resolveMenuIcon } from '@/navigation/menu-icons'
import { cn } from 'cn'

export type MenuAction = 'edit' | 'add-child' | 'delete'

const MENU_TYPE_TONES: Record<MenuType, TypeTone> = {
  1: 'blue',
  2: 'teal',
  3: 'violet'
}

function MenuTypeBadge({ type }: { type: MenuType }) {
  return <TypeBadge tone={MENU_TYPE_TONES[type]} label={MENU_TYPE_MAP[type]} />
}

type TextColumnKey = 'path' | 'name' | 'component'

function textColumn(key: TextColumnKey, header: string): ColumnDef<DataTableFeatures, MenuResp> {
  return {
    accessorKey: key,
    header,
    cell: ({ row }) => <span>{row.original[key] || EMPTY_PLACEHOLDER}</span>
  }
}

function RowActions({ menu, onAction }: { menu: MenuResp; onAction: (action: MenuAction, menu: MenuResp) => void }) {
  const { hasPermission } = usePermission()
  const canAddChild = menu.type === 1 || menu.type === 2

  return (
    <div className="flex items-center justify-center">
      <RowActionsMenu>
        <DropdownMenuGroup>
          {hasPermission('system:menu:update') && (
            <DropdownMenuItem onClick={() => onAction('edit', menu)}>
              <PenLine />
              修改菜单
            </DropdownMenuItem>
          )}
          {hasPermission('system:menu:create') && canAddChild && (
            <DropdownMenuItem onClick={() => onAction('add-child', menu)}>
              <Plus />
              添加下级
            </DropdownMenuItem>
          )}
        </DropdownMenuGroup>
        {hasPermission('system:menu:delete') && (
          <>
            <DropdownMenuSeparator />
            <DropdownMenuGroup>
              <DropdownMenuItem variant="destructive" onClick={() => onAction('delete', menu)}>
                <Trash2 />
                删除
              </DropdownMenuItem>
            </DropdownMenuGroup>
          </>
        )}
      </RowActionsMenu>
    </div>
  )
}

export function createMenuColumns(
  onAction: (action: MenuAction, menu: MenuResp) => void
): ColumnDef<DataTableFeatures, MenuResp>[] {
  return [
    {
      accessorKey: 'title',
      header: '菜单名称',
      enableHiding: false,
      cell: ({ row }) => {
        const menu = row.original
        const canExpand = row.getCanExpand()
        const isExpanded = row.getIsExpanded()
        const depth = row.depth
        const Icon = menu.icon ? resolveMenuIcon(menu.icon) : Folder

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

            {menu.type !== 3 && <Icon className="size-4 shrink-0 text-muted-foreground" />}

            <span
              className={cn(
                'truncate text-sm',
                menu.type === 1 && 'font-medium text-foreground',
                menu.type === 2 && 'text-foreground',
                menu.type === 3 && 'text-muted-foreground'
              )}
            >
              {menu.title}
            </span>
          </div>
        )
      }
    },
    {
      accessorKey: 'type',
      header: '类型',
      cell: ({ row }) => <MenuTypeBadge type={row.original.type} />
    },
    textColumn('path', '路由地址'),
    textColumn('name', '组件名称'),
    textColumn('component', '组件路径'),
    {
      accessorKey: 'permission',
      header: '权限标识',
      cell: ({ row }) =>
        row.original.permission ? (
          <Badge variant="secondary">{row.original.permission}</Badge>
        ) : (
          <span>{EMPTY_PLACEHOLDER}</span>
        )
    },
    {
      accessorKey: 'sort',
      header: '排序',
      cell: ({ row }) => <span>{row.original.sort}</span>
    },
    {
      accessorKey: 'status',
      header: '状态',
      cell: ({ row }) => <StatusCell status={row.original.status} />
    },
    {
      accessorKey: 'isHidden',
      header: '隐藏',
      cell: ({ row }) => <BooleanCell active={row.original.isHidden} label="隐藏" icon={<EyeOff />} />
    },
    {
      accessorKey: 'isExternal',
      header: '外链',
      cell: ({ row }) => <BooleanCell active={row.original.isExternal} label="外链" icon={<ExternalLink />} />
    },
    {
      accessorKey: 'isCache',
      header: '缓存',
      cell: ({ row }) => <BooleanCell active={row.original.isCache} label="缓存" icon={<Database />} />
    },
    {
      id: 'actions',
      header: () => <div className="text-center">操作</div>,
      enableHiding: false,
      cell: ({ row }) => <RowActions menu={row.original} onAction={onAction} />
    }
  ]
}
