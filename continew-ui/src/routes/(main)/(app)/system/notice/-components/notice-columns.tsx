import type { ColumnDef, ColumnVisibilityState } from '@tanstack/react-table'

import { Eye, FileText, Pen, Trash2 } from 'lucide-react'

import { FlagBadge } from '@/components/data-badge'
import { RowActionsMenu } from '@/components/row-actions-menu'
import { TextCell } from '@/components/text-cell'
import { Button } from '@/components/ui/button'
import { DropdownMenuGroup, DropdownMenuItem, DropdownMenuSeparator } from '@/components/ui/dropdown-menu'
import { usePermission } from '@/hooks/use-permission'
import type { NoticeResp } from '@/lib/api/notice'
import type { DataTableFeatures } from '@/lib/data-table-features'

import { NoticeMethodTags, NoticeStatusBadge, NoticeTypeBadge, noticeOptionLabel } from './notice-display'
import type { NoticeDicts } from './use-notice-dicts'

export type NoticeAction = 'detail' | 'view' | 'edit' | 'delete'

/** 置顶 is hidden by default, as it is in the Vue table. */
export const HIDDEN_BY_DEFAULT: ColumnVisibilityState = {
  isTop: false
}

/** Every code the row menu can show; the menu is dropped when the user has none of them. */
const ROW_ACTION_PERMISSIONS = [
  'system:notice:get',
  'system:notice:view',
  'system:notice:update',
  'system:notice:delete'
]

function yesNoColumn(key: 'isTiming' | 'isTop', header: string): ColumnDef<DataTableFeatures, NoticeResp> {
  return {
    accessorKey: key,
    header,
    cell: ({ row }) => <FlagBadge active={row.original[key]} activeLabel="是" inactiveLabel="否" />
  }
}

function RowActions({
  notice,
  onAction
}: {
  notice: NoticeResp
  onAction: (action: NoticeAction, notice: NoticeResp) => void
}) {
  const { hasPermission, hasAnyPermission } = usePermission()

  if (!hasAnyPermission(ROW_ACTION_PERMISSIONS)) {
    return null
  }

  const canDetail = hasPermission('system:notice:get')
  const canPreview = hasPermission('system:notice:view')
  const canEdit = hasPermission('system:notice:update')
  const canDelete = hasPermission('system:notice:delete')
  const hasGroup = canPreview || canEdit

  return (
    <div className="flex items-center justify-center">
      <RowActionsMenu
        showMenu={hasGroup || canDelete}
        primary={
          canDetail ? (
            <Button
              variant="ghost"
              size="icon-sm"
              aria-label="公告详情"
              title="公告详情"
              onClick={() => onAction('detail', notice)}
            >
              <Eye />
            </Button>
          ) : undefined
        }
      >
        {hasGroup ? (
          <DropdownMenuGroup>
            {canPreview && (
              <DropdownMenuItem onClick={() => onAction('view', notice)}>
                <FileText />
                预览公告
              </DropdownMenuItem>
            )}
            {canEdit && (
              <DropdownMenuItem onClick={() => onAction('edit', notice)}>
                <Pen />
                修改公告
              </DropdownMenuItem>
            )}
          </DropdownMenuGroup>
        ) : null}
        {hasGroup && canDelete ? <DropdownMenuSeparator /> : null}
        {canDelete ? (
          <DropdownMenuGroup>
            <DropdownMenuItem variant="destructive" onClick={() => onAction('delete', notice)}>
              <Trash2 />
              删除
            </DropdownMenuItem>
          </DropdownMenuGroup>
        ) : null}
      </RowActionsMenu>
    </div>
  )
}

/** Builds the notice table columns, matching the Vue order and the user table conventions. */
export function createNoticeColumns(
  dicts: NoticeDicts,
  onAction: (action: NoticeAction, notice: NoticeResp) => void,
  rowOffset: number
): ColumnDef<DataTableFeatures, NoticeResp>[] {
  return [
    {
      id: 'index',
      header: '序号',
      enableHiding: false,
      cell: ({ row }) => <div>{rowOffset + row.index + 1}</div>
    },
    {
      accessorKey: 'title',
      header: '公告标题',
      cell: ({ row }) => <TextCell value={row.original.title} className="max-w-72 font-medium" />
    },
    {
      accessorKey: 'createUserString',
      header: '发布人',
      cell: ({ row }) => <TextCell value={row.original.createUserString} />
    },
    {
      accessorKey: 'noticeScope',
      header: '通知范围',
      cell: ({ row }) => <div>{noticeOptionLabel(dicts.scopeOptions, row.original.noticeScope)}</div>
    },
    {
      accessorKey: 'noticeMethods',
      header: '通知方式',
      cell: ({ row }) => <NoticeMethodTags methods={row.original.noticeMethods} options={dicts.methodOptions} />
    },
    {
      accessorKey: 'type',
      header: '分类',
      cell: ({ row }) => <NoticeTypeBadge type={row.original.type} options={dicts.typeOptions} />
    },
    {
      accessorKey: 'status',
      header: '状态',
      cell: ({ row }) => <NoticeStatusBadge status={row.original.status} options={dicts.statusOptions} />
    },
    yesNoColumn('isTiming', '是否定时'),
    {
      accessorKey: 'publishTime',
      header: '发布时间',
      cell: ({ row }) => <TextCell value={row.original.publishTime} />
    },
    yesNoColumn('isTop', '是否置顶'),
    {
      id: 'actions',
      header: () => <div className="text-center">操作</div>,
      enableHiding: false,
      cell: ({ row }) => <RowActions notice={row.original} onAction={onAction} />
    }
  ]
}
