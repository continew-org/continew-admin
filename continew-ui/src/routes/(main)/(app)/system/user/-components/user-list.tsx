import * as React from 'react'

import { type ColumnVisibilityState, type PaginationState } from '@tanstack/react-table'

import { Download, Plus, Upload } from 'lucide-react'

import { DataTable } from '@/components/data-table'
import { DeptTreeSelect } from '@/components/dept-tree-select'
import { FilterSelect } from '@/components/filter-select'
import { ListPageCard } from '@/components/list-page-card'
import { TableFillArea } from '@/components/table-fill-area'
import { TableSearch } from '@/components/table-search'
import { TableToolbar } from '@/components/table-toolbar'
import { Button } from '@/components/ui/button'
import { Spinner } from '@/components/ui/spinner'
import { useExport } from '@/hooks/use-export'
import { useFullscreenTransition } from '@/hooks/use-fullscreen-transition'
import { useListTable } from '@/hooks/use-list-table'
import { usePermission } from '@/hooks/use-permission'
import { exportUsers, listUsers, userKeys, type UserResp, type UserStatus } from '@/lib/api/user'
import type { PageResp } from '@/lib/api/types'

import { UserAssignRolesDialog } from './user-assign-roles-dialog'
import { createUserColumns, type UserAction } from './user-columns'
import { UserDeleteDialog } from './user-delete-dialog'
import { UserDetailDrawer } from './user-detail-drawer'
import { UserFormDrawer } from './user-form-drawer'
import { UserImportDrawer } from './user-import-drawer'
import { UserResetPasswordDialog } from './user-reset-password-dialog'

const HIDDEN_BY_DEFAULT: ColumnVisibilityState = {
  isSystem: false,
  createUserString: false,
  updateUserString: false,
  updateTime: false
}

/** The user query sorts on its own `t1` table alias. */
const DEFAULT_SORT = 't1.id,desc'

const STATUS_OPTIONS = [
  { value: 'all', label: '全部' },
  { value: '1', label: '启用' },
  { value: '2', label: '禁用' }
]

export function UserList() {
  const [deptId, setDeptId] = React.useState<string | number | undefined>(undefined)
  const [status, setStatus] = React.useState<UserStatus | undefined>(undefined)
  const [keyword, setKeyword] = React.useState('')
  // One state per overlay: a null value means it is closed, so "which record" and "is it open" can
  // never drift apart. The form drawer holds an object, because a null id there means 新增.
  const [detailUserId, setDetailUserId] = React.useState<string | number | null>(null)
  const [formTarget, setFormTarget] = React.useState<{ userId: string | number | null } | null>(null)
  const [resetUser, setResetUser] = React.useState<UserResp | null>(null)
  const [roleUser, setRoleUser] = React.useState<UserResp | null>(null)
  const [deletingUser, setDeletingUser] = React.useState<UserResp | null>(null)
  const [importOpen, setImportOpen] = React.useState(false)
  const fullscreen = useFullscreenTransition()
  const { isExporting, exportData } = useExport(exportUsers)

  // One store subscription for the page; every row only receives the granted codes and
  // compares against them, so a list of N users never becomes N subscriptions.
  const { permissions, hasPermission } = usePermission()

  const handleRowAction = React.useCallback((action: UserAction, user: UserResp) => {
    if (action === 'detail') {
      setDetailUserId(user.id)
      return
    }

    if (action === 'edit') {
      setFormTarget({ userId: user.id })
      return
    }

    if (action === 'resetPassword') {
      setResetUser(user)
      return
    }

    if (action === 'assignRoles') {
      setRoleUser(user)
      return
    }

    if (action === 'delete') {
      setDeletingUser(user)
    }
  }, [])

  // Shared by the list request and the export request, so the two can never drift apart.
  const queryFilters = React.useMemo(
    () => ({ status, deptId, description: keyword || undefined, sort: DEFAULT_SORT }),
    [status, deptId, keyword]
  )

  const buildQuery = React.useCallback(
    (pagination: PaginationState) => {
      const params = { page: pagination.pageIndex + 1, size: pagination.pageSize, ...queryFilters }
      return { queryKey: userKeys.list(params), queryFn: (signal: AbortSignal) => listUsers(params, signal) }
    },
    [queryFilters]
  )

  const buildColumns = React.useCallback(
    (rowOffset: number) => createUserColumns(handleRowAction, rowOffset, permissions),
    [handleRowAction, permissions]
  )

  const { query, table, resetPage, resetColumns } = useListTable<UserResp, PageResp<UserResp>>({
    buildQuery,
    buildColumns,
    getRowId: row => String(row.id),
    initialColumnVisibility: HIDDEN_BY_DEFAULT
  })

  // HIDDEN_BY_DEFAULT leaves 12 columns visible; only widen when more are enabled.
  const hasManyColumns = table.getVisibleLeafColumns().length > 12

  const handleSearch = React.useCallback(
    (next: string) => {
      setKeyword(next)
      resetPage()
    },
    [resetPage]
  )

  const handleDeptChange = (value: string) => {
    setDeptId(value || undefined)
    resetPage()
  }

  const handleStatusChange = (value: string | null) => {
    setStatus(!value || value === 'all' ? undefined : (Number(value) as UserStatus))
    resetPage()
  }

  const filters = (
    <>
      <TableSearch className="col-span-2 sm:w-64" placeholder="搜索用户名、昵称、描述" onSearch={handleSearch} />
      <DeptTreeSelect
        triggerPrefix="部门"
        placeholder="全部部门"
        placeholderColor="foreground"
        showAllOption
        allOptionLabel="全部部门"
        value={deptId}
        onChange={handleDeptChange}
        className="sm:w-44"
      />
      <FilterSelect
        label="状态"
        options={STATUS_OPTIONS}
        value={status === undefined ? 'all' : String(status)}
        onValueChange={handleStatusChange}
        className="sm:w-32"
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
      actions={
        <>
          {hasPermission('system:user:create') && (
            <Button onClick={() => setFormTarget({ userId: null })}>
              <Plus />
              新增用户
            </Button>
          )}
          {hasPermission('system:user:import') && (
            <Button size="icon" variant="outline" aria-label="导入用户" title="导入用户" onClick={() => setImportOpen(true)}>
              <Upload />
            </Button>
          )}
          {hasPermission('system:user:export') && (
            <Button size="icon" variant="outline" disabled={isExporting} onClick={() => void exportData(queryFilters)}>
              {isExporting ? <Spinner /> : <Download />}
            </Button>
          )}
        </>
      }
    />
  )

  return (
    <>
      <ListPageCard
        title="用户管理"
        description="对当前系统用户的基本信息进行管理"
        filters={filters}
        actions={actions}
        fullscreen={fullscreen}
      >
        <TableFillArea fullscreen={fullscreen}>
          <DataTable
            table={table}
            className={hasManyColumns ? 'min-w-[1600px]' : undefined}
            isLoading={query.isPending}
            isError={query.isError}
            isShowingPreviousRows={query.isPlaceholderData}
            onRetry={() => void query.refetch()}
            fillHeight
            skeletonWidths={['mx-auto w-6', 'w-24', 'w-20', 'mx-auto w-12 rounded-full', 'mx-auto w-8', 'w-12']}
          />
        </TableFillArea>
      </ListPageCard>

      <UserDetailDrawer userId={detailUserId} open={detailUserId !== null} onClose={() => setDetailUserId(null)} />
      <UserImportDrawer open={importOpen} onClose={() => setImportOpen(false)} />
      <UserFormDrawer
        userId={formTarget?.userId ?? null}
        open={formTarget !== null}
        onClose={() => setFormTarget(null)}
      />
      <UserResetPasswordDialog user={resetUser} open={resetUser !== null} onClose={() => setResetUser(null)} />
      <UserAssignRolesDialog user={roleUser} open={roleUser !== null} onClose={() => setRoleUser(null)} />
      <UserDeleteDialog user={deletingUser} open={deletingUser !== null} onClose={() => setDeletingUser(null)} />
    </>
  )
}
