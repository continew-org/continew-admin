import type { ColumnDef } from '@tanstack/react-table'
import { LogOut } from 'lucide-react'

import { TextCell } from '@/components/text-cell'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import type { OnlineUserResp } from '@/lib/api/online'
import { EMPTY_PLACEHOLDER } from '@/lib/constants'
import type { DataTableFeatures } from '@/lib/data-table-features'

import { DeviceCell, UserCell } from '../../-components/monitor-cells'

type OnlineUserColumn = ColumnDef<DataTableFeatures, OnlineUserResp>
type TextColumnKey = 'ip' | 'address'

interface OnlineUserColumnOptions {
  /** The signed-in account may not hold `monitor:online:kickout`; the column is dropped then. */
  readonly canKickout: boolean
  /** The backend rejects kicking out the session that sends the request. */
  readonly isCurrentSession: (sessionId: string) => boolean
  readonly onKickout: (user: OnlineUserResp) => void
}

function textColumn(key: TextColumnKey, header: string): OnlineUserColumn {
  return {
    accessorKey: key,
    header,
    cell: ({ row }) => <TextCell value={row.original[key]} />
  }
}

function TimeCell({ value, estimated = false }: { value: string | null; estimated?: boolean }) {
  if (!value) return <span>{EMPTY_PLACEHOLDER}</span>
  const prefix = estimated ? '约 ' : ''
  const compact = value.replace(/^\d{4}-(\d{2}-\d{2})[T ](\d{2}:\d{2}):\d{2}$/, '$1 $2')
  return (
    <span className="whitespace-nowrap tabular-nums" title={prefix + value}>
      {prefix + compact}
    </span>
  )
}

function StatusCell({ user }: { user: OnlineUserResp }) {
  const { idleExpired, online, idleExpireTime, sessionExpireTime } = user
  const expiry = [
    idleExpireTime && `闲置到期：${idleExpireTime}`,
    sessionExpireTime && `绝对到期：${sessionExpireTime}`
  ]
    .filter(Boolean)
    .join('；')
  if (idleExpired) {
    return (
      <Badge variant="destructive" title={['已闲置超时，待失效', expiry].filter(Boolean).join('；')}>
        闲置超时
      </Badge>
    )
  }
  if (online === null || online === undefined) return <span>{EMPTY_PLACEHOLDER}</span>
  return (
    <Badge variant={online ? 'default' : 'secondary'} title={expiry || undefined}>
      {online ? '在线' : '闲置'}
    </Badge>
  )
}

function KickoutCell({
  user,
  isCurrentSession,
  onKickout
}: {
  user: OnlineUserResp
  isCurrentSession: (sessionId: string) => boolean
  onKickout: (user: OnlineUserResp) => void
}) {
  return (
    <div className="flex items-center justify-center">
      {isCurrentSession(user.sessionId) ? (
        <Badge variant="secondary">当前会话</Badge>
      ) : (
        <Button
          variant="ghost"
          size="icon-sm"
          className="text-destructive hover:bg-destructive/10 hover:text-destructive"
          aria-label="强退"
          title="强退"
          onClick={() => onKickout(user)}
        >
          <LogOut />
        </Button>
      )}
    </div>
  )
}

export function createOnlineUserColumns({ canKickout, isCurrentSession, onKickout }: OnlineUserColumnOptions) {
  const columns: OnlineUserColumn[] = [
    {
      id: 'user',
      header: '用户',
      cell: ({ row: { original: user } }) => <UserCell nickname={user.nickname} username={user.username} />
    },
    textColumn('ip', '登录 IP'),
    textColumn('address', 'IP 归属地'),
    {
      id: 'device',
      header: '浏览器 / 系统',
      cell: ({ row: { original: user } }) => <DeviceCell browser={user.browser} os={user.os} />
    },
    {
      accessorKey: 'loginTime',
      header: '登录时间',
      cell: ({ row }) => <TimeCell value={row.original.loginTime} />
    },
    {
      accessorKey: 'lastActiveTime',
      header: '最近活跃',
      cell: ({ row }) => (
        <TimeCell value={row.original.lastActiveTime} estimated={row.original.activeTimeExact === false} />
      )
    },
    {
      id: 'status',
      header: '状态',
      cell: ({ row }) => <StatusCell user={row.original} />
    }
  ]

  if (canKickout) {
    columns.push({
      id: 'actions',
      header: () => <div className="text-center">操作</div>,
      enableHiding: false,
      cell: ({ row }) => <KickoutCell user={row.original} isCurrentSession={isCurrentSession} onKickout={onKickout} />
    })
  }

  return columns
}
