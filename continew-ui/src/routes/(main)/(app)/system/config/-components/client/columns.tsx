import type { ColumnDef } from '@tanstack/react-table'
import { Eye, PenLine, Trash2 } from 'lucide-react'

import { CopyButton } from '@/components/copy-button'
import { FlagBadge, TagList, TypeBadge, type TypeTone } from '@/components/data-badge'
import { RowActionsMenu } from '@/components/row-actions-menu'
import { StatusCell } from '@/components/status-cell'
import { TextCell } from '@/components/text-cell'
import { Button } from '@/components/ui/button'
import { DropdownMenuGroup, DropdownMenuItem, DropdownMenuSeparator } from '@/components/ui/dropdown-menu'
import { usePermission } from '@/hooks/use-permission'
import type { ClientResp } from '@/lib/api/client'
import { EMPTY_PLACEHOLDER } from '@/lib/constants'
import type { DataTableFeatures } from '@/lib/data-table-features'

import { toLabelMap } from '../use-dict-options'

export const AUTH_TYPE_OPTIONS = [
  { label: '账号密码', value: 'ACCOUNT' },
  { label: '短信验证码', value: 'SMS' },
  { label: '邮箱验证码', value: 'EMAIL' },
  { label: '三方授权', value: 'SOCIAL' }
]

export const REFRESH_TOKEN_MODE_OPTIONS = [
  { label: 'Cookie（Web）', value: 'COOKIE' },
  { label: '响应体（App / 小程序）', value: 'BODY' }
]

export const REFRESH_TOKEN_MODE_MAP = toLabelMap(REFRESH_TOKEN_MODE_OPTIONS)

export const REPLACED_RANGE_PRESETS = [
  { label: '当前客户端类型', value: 'CURR_DEVICE_TYPE' },
  { label: '所有客户端类型', value: 'ALL_DEVICE_TYPE' }
]

export const LOGOUT_MODE_PRESETS = [
  { label: '注销下线', value: 'LOGOUT' },
  { label: '踢人下线', value: 'KICKOUT' },
  { label: '顶人下线', value: 'REPLACED' }
]

const CLIENT_TYPE_TONES: Record<string, TypeTone> = {
  PC: 'blue',
  APP: 'teal',
  MINI: 'amber',
  WEB: 'violet'
}

export type ClientAction = 'detail' | 'edit' | 'delete'

function ClientIdCell({ clientId }: { clientId: string }) {
  return (
    <div className="flex items-center gap-1.5 text-foreground">
      <span className="truncate">{clientId}</span>
      <CopyButton
        value={clientId}
        label="复制客户端 ID"
        successMessage="已复制客户端 ID"
        className="size-6 text-muted-foreground transition-colors hover:text-foreground"
      />
    </div>
  )
}

function RowActions({
  client,
  onAction
}: {
  client: ClientResp
  onAction: (action: ClientAction, client: ClientResp) => void
}) {
  const { hasPermission } = usePermission()
  const isLocked = Boolean(client.disabled)
  const canDetail = hasPermission('system:client:get')
  const canUpdate = hasPermission('system:client:update')
  const canDelete = hasPermission('system:client:delete')

  if (!canDetail && !canUpdate && !canDelete) {
    return null
  }

  return (
    <div className="flex items-center justify-center">
      <RowActionsMenu
        showMenu={canUpdate || canDelete}
        primary={
          canDetail ? (
            <Button
              variant="ghost"
              size="icon-sm"
              aria-label="客户端详情"
              title="客户端详情"
              onClick={() => onAction('detail', client)}
            >
              <Eye />
            </Button>
          ) : undefined
        }
      >
        {canUpdate ? (
          <DropdownMenuGroup>
            <DropdownMenuItem disabled={isLocked} onClick={() => onAction('edit', client)}>
              <PenLine />
              修改客户端
            </DropdownMenuItem>
          </DropdownMenuGroup>
        ) : null}
        {canUpdate && canDelete ? <DropdownMenuSeparator /> : null}
        {canDelete ? (
          <DropdownMenuGroup>
            <DropdownMenuItem variant="destructive" disabled={isLocked} onClick={() => onAction('delete', client)}>
              <Trash2 />
              删除
            </DropdownMenuItem>
          </DropdownMenuGroup>
        ) : null}
      </RowActionsMenu>
    </div>
  )
}

export interface ClientColumnOptions {
  readonly clientTypeMap: Record<string, string>
  readonly authTypeMap: Record<string, string>
  readonly replacedRangeMap: Record<string, string>
  readonly logoutModeMap: Record<string, string>
  readonly onAction: (action: ClientAction, client: ClientResp) => void
}

export function createClientColumns(
  options: ClientColumnOptions,
  rowOffset: number
): ColumnDef<DataTableFeatures, ClientResp>[] {
  const { clientTypeMap, authTypeMap, replacedRangeMap, logoutModeMap, onAction } = options

  return [
    {
      id: 'index',
      header: '序号',
      enableHiding: false,
      cell: ({ row }) => <div>{rowOffset + row.index + 1}</div>
    },
    {
      accessorKey: 'clientId',
      header: '客户端 ID',
      enableHiding: false,
      cell: ({ row }) => <ClientIdCell clientId={row.original.clientId} />
    },
    {
      accessorKey: 'clientType',
      header: '客户端类型',
      cell: ({ row }) => (
        <TypeBadge
          tone={CLIENT_TYPE_TONES[row.original.clientType] ?? 'neutral'}
          label={clientTypeMap[row.original.clientType] || row.original.clientType}
        />
      )
    },
    {
      accessorKey: 'authType',
      header: '认证类型',
      cell: ({ row }) => (
        <TagList items={(row.original.authType ?? []).map(authType => authTypeMap[authType] || authType)} max={1} />
      )
    },
    {
      accessorKey: 'status',
      header: '状态',
      cell: ({ row }) => <StatusCell status={row.original.status} />
    },
    {
      accessorKey: 'activeTimeout',
      header: 'Token 最低活跃频率',
      cell: ({ row }) => {
        const val = row.original.activeTimeout
        return <span>{val === -1 ? '不限制' : `${val} 秒`}</span>
      }
    },
    {
      accessorKey: 'timeout',
      header: 'Token 有效期',
      cell: ({ row }) => {
        const val = row.original.timeout
        return <span>{val === -1 ? '永不过期' : `${val} 秒`}</span>
      }
    },
    {
      accessorKey: 'refreshTokenTimeout',
      header: 'Refresh Token 有效期',
      cell: ({ row }) => {
        const val = row.original.refreshTokenTimeout
        return <span>{val ? `${val} 秒` : EMPTY_PLACEHOLDER}</span>
      }
    },
    {
      accessorKey: 'refreshTokenMode',
      header: 'Refresh Token 传输模式',
      cell: ({ row }) => {
        const mode = row.original.refreshTokenMode
        const text = mode ? REFRESH_TOKEN_MODE_MAP[mode] || mode : EMPTY_PLACEHOLDER
        return <span className="text-sm">{text}</span>
      }
    },
    {
      accessorKey: 'isConcurrent',
      header: '多地登录',
      cell: ({ row }) => <FlagBadge active={row.original.isConcurrent} activeLabel="允许" inactiveLabel="互斥" />
    },
    {
      accessorKey: 'replacedRange',
      header: '下线范围',
      cell: ({ row }) => {
        const val = row.original.replacedRange
        const text = val ? replacedRangeMap[val] || val : EMPTY_PLACEHOLDER
        return <span className="text-sm">{text}</span>
      }
    },
    {
      accessorKey: 'maxLoginCount',
      header: '登录数量',
      cell: ({ row }) => {
        const val = row.original.maxLoginCount
        if (val === -1) return <span className="text-sm">不限制</span>
        if (val !== undefined && val !== null) return <span>{val} 个</span>
        return <span className="text-sm">{EMPTY_PLACEHOLDER}</span>
      }
    },
    {
      accessorKey: 'overflowLogoutMode',
      header: '溢出处理',
      cell: ({ row }) => {
        const val = row.original.overflowLogoutMode
        const text = val ? logoutModeMap[val] || val : EMPTY_PLACEHOLDER
        return <span className="text-sm">{text}</span>
      }
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
      accessorKey: 'updateUserString',
      header: '修改人',
      cell: ({ row }) => <TextCell value={row.original.updateUserString} />
    },
    {
      accessorKey: 'updateTime',
      header: '修改时间',
      cell: ({ row }) => <TextCell value={row.original.updateTime} />
    },
    {
      id: 'actions',
      header: () => <div className="text-center">操作</div>,
      enableHiding: false,
      cell: ({ row }) => <RowActions client={row.original} onAction={onAction} />
    }
  ]
}
