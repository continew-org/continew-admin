import * as React from 'react'

import { keepPreviousData, useQuery } from '@tanstack/react-query'
import { type ColumnVisibilityState, type PaginationState, useTable } from '@tanstack/react-table'

import { Plus } from 'lucide-react'

import { DataTable } from '@/components/data-table'
import { ListPageCard } from '@/components/list-page-card'
import { TableFillArea } from '@/components/table-fill-area'
import { TableSearch } from '@/components/table-search'
import { TableToolbar } from '@/components/table-toolbar'
import { Button } from '@/components/ui/button'
import { useFullscreenTransition } from '@/hooks/use-fullscreen-transition'
import { usePageClamp } from '@/hooks/use-page-clamp'
import { listRoles, roleKeys, type RoleResp } from '@/lib/api/role'
import { dataTableFeatures } from '@/lib/data-table-features'

import { createRoleColumns, type RoleAction } from './role-columns'
import { RoleDeleteDialog } from './role-delete-dialog'
import { RoleDetailDrawer } from './role-detail-drawer'
import { RoleFormDrawer } from './role-form-drawer'
import { RolePermissionDrawer } from './role-permission-drawer'
import { RoleUserDrawer } from './role-user-drawer'

const HIDDEN_BY_DEFAULT: ColumnVisibilityState = {
  createUserString: false,
  updateUserString: false,
  updateTime: false
}

const DEFAULT_SORT = 'sort,asc,id,desc'
const EMPTY_ROWS: RoleResp[] = []

export function RoleList() {
  const [pagination, setPagination] = React.useState<PaginationState>({ pageIndex: 0, pageSize: 10 })
  const [columnVisibility, setColumnVisibility] = React.useState<ColumnVisibilityState>(HIDDEN_BY_DEFAULT)
  const [keyword, setKeyword] = React.useState('')

  // One state per overlay: a null value means it is closed, so "which record" and "is it open" can
  // never drift apart. The form drawer holds an object, because a null id there means 新增.
  const [detailRoleId, setDetailRoleId] = React.useState<string | number | null>(null)
  const [formTarget, setFormTarget] = React.useState<{ roleId: string | number | null } | null>(null)
  const [permissionRole, setPermissionRole] = React.useState<RoleResp | null>(null)
  const [userRole, setUserRole] = React.useState<RoleResp | null>(null)
  const [deletingRole, setDeletingRole] = React.useState<RoleResp | null>(null)

  const fullscreen = useFullscreenTransition()

  const queryParams = { description: keyword || undefined, sort: DEFAULT_SORT }

  const query = useQuery({
    queryKey: roleKeys.list(queryParams),
    queryFn: ({ signal }) => listRoles(queryParams, signal),
    placeholderData: keepPreviousData
  })

  const roles = query.data ?? EMPTY_ROWS

  usePageClamp(roles.length, pagination, setPagination)

  const handleSearch = (next: string) => {
    setKeyword(next)
    setPagination(current => (current.pageIndex === 0 ? current : { ...current, pageIndex: 0 }))
  }

  const handleRowAction = React.useCallback((action: RoleAction, role: RoleResp) => {
    if (action === 'detail') {
      setDetailRoleId(role.id)
      return
    }

    if (action === 'edit') {
      setFormTarget({ roleId: role.id })
      return
    }

    if (action === 'permission') {
      setPermissionRole(role)
      return
    }

    if (action === 'members') {
      setUserRole(role)
      return
    }

    if (action === 'delete') {
      setDeletingRole(role)
    }
  }, [])

  const columns = React.useMemo(() => createRoleColumns(handleRowAction), [handleRowAction])

  const table = useTable({
    features: dataTableFeatures,
    data: roles,
    columns,
    state: { columnVisibility, pagination },
    autoResetPageIndex: false,
    onColumnVisibilityChange: setColumnVisibility,
    onPaginationChange: setPagination,
    getRowId: row => String(row.id)
  })

  const filters = <TableSearch className="h-8" placeholder="搜索角色名称、编码" onSearch={handleSearch} />

  const actions = (
    <TableToolbar
      table={table}
      isRefreshing={query.isFetching && !query.isPending}
      isFullscreen={fullscreen.isFullscreen}
      onRefresh={() => void query.refetch()}
      onToggleFullscreen={fullscreen.toggleFullscreen}
      onResetColumns={() => setColumnVisibility(HIDDEN_BY_DEFAULT)}
      actions={
        <Button onClick={() => setFormTarget({ roleId: null })}>
          <Plus />
          新增角色
        </Button>
      }
    />
  )

  return (
    <>
      <ListPageCard
        title="角色管理"
        description="管理系统角色、数据权限范围及关联角色"
        filters={filters}
        actions={actions}
        fullscreen={fullscreen}
      >
        <TableFillArea fullscreen={fullscreen}>
          <DataTable
            table={table}
            isLoading={query.isPending}
            isError={query.isError}
            isShowingPreviousRows={query.isPlaceholderData}
            onRetry={() => void query.refetch()}
            fillHeight
            skeletonWidths={['mx-auto w-4', 'mx-auto w-6', 'w-24', 'w-20', 'mx-auto w-16 rounded-full', 'w-20']}
          />
        </TableFillArea>
      </ListPageCard>

      <RoleDetailDrawer
        roleId={detailRoleId}
        open={detailRoleId !== null}
        onClose={() => setDetailRoleId(null)}
      />
      <RoleFormDrawer
        roleId={formTarget?.roleId ?? null}
        open={formTarget !== null}
        onClose={() => setFormTarget(null)}
      />
      <RolePermissionDrawer
        role={permissionRole}
        open={permissionRole !== null}
        onClose={() => setPermissionRole(null)}
      />
      <RoleUserDrawer role={userRole} open={userRole !== null} onClose={() => setUserRole(null)} />
      <RoleDeleteDialog role={deletingRole} open={deletingRole !== null} onClose={() => setDeletingRole(null)} />
    </>
  )
}
