import type { ColumnDef } from '@tanstack/react-table'
import { Eye, KeyRound, Pen, ShieldCheck, Trash2 } from 'lucide-react'

import { FlagBadge, TagList } from '@/components/data-badge'
import { RowActionsMenu } from '@/components/row-actions-menu'
import { StatusCell } from '@/components/status-cell'
import { TextCell } from '@/components/text-cell'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { Button } from '@/components/ui/button'
import { DropdownMenuGroup, DropdownMenuItem, DropdownMenuSeparator } from '@/components/ui/dropdown-menu'
import type { UserResp } from '@/lib/api/user'
import type { DataTableFeatures } from '@/lib/data-table-features'
import { hasPermission } from '@/lib/permission'
import { getInitials } from '@/lib/utils'

import { genderLabel } from './user-display'

export type UserAction = 'detail' | 'edit' | 'resetPassword' | 'assignRoles' | 'delete'

type TextColumnKey =
  | 'deptName'
  | 'phone'
  | 'email'
  | 'description'
  | 'createUserString'
  | 'createTime'
  | 'updateUserString'
  | 'updateTime'

function textColumn(key: TextColumnKey, header: string): ColumnDef<DataTableFeatures, UserResp> {
  return {
    accessorKey: key,
    header,
    cell: ({ row }) => <TextCell value={row.original[key]} />
  }
}

function NicknameCell({ user }: { user: UserResp }) {
  const name = user.nickname || user.username

  return (
    <div className="flex items-center gap-2">
      <Avatar>
        <AvatarImage src={user.avatar ?? undefined} />
        <AvatarFallback>{getInitials(name)}</AvatarFallback>
      </Avatar>
      <span className="max-w-56 min-w-0 truncate" title={name}>
        {name}
      </span>
    </div>
  )
}

function RowActions({
  user,
  onAction,
  permissions
}: {
  user: UserResp
  onAction: (action: UserAction, user: UserResp) => void
  permissions: readonly string[]
}) {
  const isLocked = Boolean(user.disabled)
  const canUpdate = hasPermission(permissions, 'system:user:update')
  const canResetPassword = hasPermission(permissions, 'system:user:resetPwd')
  const canAssignRoles = hasPermission(permissions, 'system:user:updateRole')
  const canDelete = hasPermission(permissions, 'system:user:delete')
  const hasSecondaryActions = canUpdate || canResetPassword || canAssignRoles || canDelete

  return (
    <RowActionsMenu
      showMenu={hasSecondaryActions}
      primary={
        <Button
          variant="ghost"
          size="icon-sm"
          aria-label="用户详情"
          title="用户详情"
          onClick={() => onAction('detail', user)}
        >
          <Eye />
        </Button>
      }
    >
      {canUpdate && (
        <DropdownMenuGroup>
          <DropdownMenuItem disabled={isLocked} onClick={() => onAction('edit', user)}>
            <Pen />
            修改用户
          </DropdownMenuItem>
        </DropdownMenuGroup>
      )}
      {(canResetPassword || canAssignRoles) && (
        <>
          <DropdownMenuSeparator />
          <DropdownMenuGroup>
            {canResetPassword && (
              <DropdownMenuItem disabled={isLocked} onClick={() => onAction('resetPassword', user)}>
                <KeyRound />
                重置密码
              </DropdownMenuItem>
            )}
            {canAssignRoles && (
              <DropdownMenuItem disabled={isLocked} onClick={() => onAction('assignRoles', user)}>
                <ShieldCheck />
                分配角色
              </DropdownMenuItem>
            )}
          </DropdownMenuGroup>
        </>
      )}
      {canDelete && (
        <>
          <DropdownMenuSeparator />
          <DropdownMenuItem variant="destructive" disabled={isLocked} onClick={() => onAction('delete', user)}>
            <Trash2 />
            删除
          </DropdownMenuItem>
        </>
      )}
    </RowActionsMenu>
  )
}

export function createUserColumns(
  onAction: (action: UserAction, user: UserResp) => void,
  rowOffset: number,
  permissions: readonly string[]
) {
  const columns: ColumnDef<DataTableFeatures, UserResp>[] = [
    {
      id: 'index',
      header: '序号',
      enableHiding: false,
      cell: ({ row }) => <div className="tabular-nums">{rowOffset + row.index + 1}</div>
    },
    {
      accessorKey: 'nickname',
      header: '昵称',
      cell: ({ row }) => <NicknameCell user={row.original} />
    },
    {
      accessorKey: 'username',
      header: '用户名',
      cell: ({ row }) => row.original.username
    },
    {
      accessorKey: 'status',
      header: '状态',
      cell: ({ row }) => <StatusCell status={row.original.status} />
    },
    {
      accessorKey: 'gender',
      header: '性别',
      cell: ({ row }) => genderLabel(row.original.gender)
    },
    textColumn('deptName', '所属部门'),
    {
      accessorKey: 'roleNames',
      header: '角色',
      cell: ({ row }) => <TagList items={row.original.roleNames} max={1} />
    },
    textColumn('phone', '手机号'),
    textColumn('email', '邮箱'),
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
      header: '操作',
      enableHiding: false,
      cell: ({ row }) => <RowActions user={row.original} onAction={onAction} permissions={permissions} />
    }
  ]

  return columns
}
