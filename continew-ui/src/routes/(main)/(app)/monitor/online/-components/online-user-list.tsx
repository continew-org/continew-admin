import * as React from 'react'

import { type PaginationState } from '@tanstack/react-table'
import type { DateRange } from 'react-day-picker'

import { DateRangeFilter } from '@/components/date-range-filter'
import { ListPageCard } from '@/components/list-page-card'
import { TableFillArea } from '@/components/table-fill-area'
import { TableSearch } from '@/components/table-search'
import { TableToolbar } from '@/components/table-toolbar'
import { useFullscreenTransition } from '@/hooks/use-fullscreen-transition'
import { useListTable } from '@/hooks/use-list-table'
import { usePermission } from '@/hooks/use-permission'
import { listOnlineUsers, onlineUserKeys, type OnlineUserPageQuery, type OnlineUserResp } from '@/lib/api/online'
import type { PageResp } from '@/lib/api/types'
import { getSessionId } from '@/lib/auth-session'
import { createRecentDateRange, toDateTimeRangeQuery } from '@/lib/date-range'

import { createOnlineUserColumns } from './online-user-columns'
import { OnlineUserKickoutDialog } from './online-user-kickout-dialog'
import { OnlineUserTable } from './online-user-table'

export function OnlineUserList() {
  const [keyword, setKeyword] = React.useState('')
  const [loginTime, setLoginTime] = React.useState<DateRange | undefined>(createRecentDateRange)
  const [kickingUser, setKickingUser] = React.useState<OnlineUserResp | null>(null)
  const { hasPermission } = usePermission()
  const fullscreen = useFullscreenTransition()

  // Without the permission the backend drops the action as well; hiding the column avoids a
  // button that can only fail.
  const canKickout = hasPermission('monitor:online:kickout')
  const currentSessionId = getSessionId()
  const isCurrentSession = React.useCallback(
    (sessionId: string) => currentSessionId !== null && sessionId === currentSessionId,
    [currentSessionId]
  )

  const handleKickout = React.useCallback((user: OnlineUserResp) => {
    setKickingUser(user)
  }, [])

  const buildQuery = React.useCallback(
    (pagination: PaginationState) => {
      const params: OnlineUserPageQuery = {
        page: pagination.pageIndex + 1,
        size: pagination.pageSize,
        nickname: keyword || undefined,
        loginTime: toDateTimeRangeQuery(loginTime)
      }
      return {
        queryKey: onlineUserKeys.list(params),
        queryFn: (signal: AbortSignal) => listOnlineUsers(params, signal)
      }
    },
    [keyword, loginTime]
  )

  const buildColumns = React.useCallback(
    (rowOffset: number) =>
      createOnlineUserColumns({ rowOffset, canKickout, isCurrentSession, onKickout: handleKickout }),
    [canKickout, isCurrentSession, handleKickout]
  )

  const { query, table, resetPage, resetColumns } = useListTable<OnlineUserResp, PageResp<OnlineUserResp>>({
    buildQuery,
    buildColumns,
    getRowId: row => row.sessionId
  })

  const handleSearch = (next: string) => {
    setKeyword(next)
    resetPage()
  }

  const handleLoginTimeChange = (next: DateRange | undefined) => {
    setLoginTime(next)
    resetPage()
  }

  const filters = (
    <>
      <TableSearch
        className="col-span-2 h-8 w-full sm:w-64"
        clearable
        placeholder="搜索用户名、昵称"
        onSearch={handleSearch}
      />
      <DateRangeFilter
        value={loginTime}
        onChange={handleLoginTimeChange}
        placeholder="登录时间"
        className="col-span-2"
      />
    </>
  )

  const actions = (
    <TableToolbar
      table={table}
      className="w-full xl:w-auto xl:justify-end"
      isRefreshing={query.isFetching && !query.isPending}
      isFullscreen={fullscreen.isFullscreen}
      onRefresh={() => void query.refetch()}
      onToggleFullscreen={fullscreen.toggleFullscreen}
      onResetColumns={resetColumns}
    />
  )

  return (
    <>
      <ListPageCard
        title="在线用户"
        description="管理当前系统登录的会话"
        filters={filters}
        actions={actions}
        fullscreen={fullscreen}
      >
        <TableFillArea fullscreen={fullscreen}>
          <OnlineUserTable
            table={table}
            isLoading={query.isPending}
            isError={query.isError}
            isShowingPreviousRows={query.isPlaceholderData}
            onRetry={() => void query.refetch()}
            fillHeight
          />
        </TableFillArea>
      </ListPageCard>

      <OnlineUserKickoutDialog user={kickingUser} open={kickingUser !== null} onClose={() => setKickingUser(null)} />
    </>
  )
}
