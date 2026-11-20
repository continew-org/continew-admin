import type { ColumnDef } from '@tanstack/react-table'
import { LogOut } from 'lucide-react'

import { TextCell } from '@/components/text-cell'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import type { OnlineUserResp } from '@/lib/api/online'
import { EMPTY_PLACEHOLDER } from '@/lib/constants'
import type { DataTableFeatures } from '@/lib/data-table-features'

type OnlineUserColumn = ColumnDef<DataTableFeatures, OnlineUserResp>
type TextColumnKey = 'ip' | 'address' | 'browser' | 'os' | 'loginTime' | 'lastRefreshTime'

interface OnlineUserColumnOptions {
  /** The index of the first row on the current page, so 序号 stays continuous. */
  readonly rowOffset: number
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

export function createOnlineUserColumns({
  rowOffset,
  canKickout,
  isCurrentSession,
  onKickout
}: OnlineUserColumnOptions) {
  const columns: OnlineUserColumn[] = [
    {
      id: 'index',
      header: '序号',
      enableHiding: false,
      cell: ({ row }) => <div>{rowOffset + row.index + 1}</div>
    },
    {
      accessorKey: 'nickname',
      header: '昵称',
      cell: ({ row }) => <span className="truncate">{row.original.nickname || EMPTY_PLACEHOLDER}</span>
    },
    {
      accessorKey: 'username',
      header: '用户名',
      cell: ({ row }) => <span className="truncate">{row.original.username || EMPTY_PLACEHOLDER}</span>
    },
    textColumn('ip', '登录 IP'),
    textColumn('address', '登录地点'),
    textColumn('browser', '浏览器'),
    textColumn('os', '终端系统'),
    textColumn('loginTime', '登录时间'),
    textColumn('lastRefreshTime', '最后刷新时间')
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
