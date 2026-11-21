import * as React from 'react'

import { useQuery, useQueryClient } from '@tanstack/react-query'
import { type ColumnDef, type PaginationState, type RowSelectionState, useTable } from '@tanstack/react-table'

import { Plus, Trash2, Users } from 'lucide-react'

import { EntityDrawer } from '@/components/entity-drawer'
import { QueryState } from '@/components/query-state'
import { StatusCell } from '@/components/status-cell'
import { TablePagination } from '@/components/table-pagination'
import { TableSearch } from '@/components/table-search'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Checkbox } from '@/components/ui/checkbox'
import { DrawerFooter } from '@/components/ui/drawer'
import { Skeleton } from '@/components/ui/skeleton'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { toast } from '@/components/ui/toast'
import { useLastRecord } from '@/hooks/use-last-record'
import { listRoleUsers, roleKeys, type RoleResp, type RoleUserResp, unassignRoleFromUsers } from '@/lib/api/role'
import { EMPTY_PLACEHOLDER } from '@/lib/constants'
import { dataTableFeatures, type DataTableFeatures } from '@/lib/data-table-features'

import { RoleUserAddDrawer } from './role-user-add-drawer'

const EMPTY_MEMBERS: RoleUserResp[] = []

interface RoleUserDrawerProps {
  readonly role: RoleResp | null
  readonly open: boolean
  readonly onClose: () => void
}

interface MemberColumnOptions {
  readonly isUnassigning: boolean
  readonly onUnassign: (member: RoleUserResp) => void
  /** Rows already shown on earlier pages, so the serial number keeps counting across pages. */
  readonly rowOffset: number
}

function createMemberColumns({
  isUnassigning,
  onUnassign,
  rowOffset
}: MemberColumnOptions): ColumnDef<DataTableFeatures, RoleUserResp>[] {
  return [
    {
      id: 'select',
      header: ({ table }) => {
        const isAll = table.getIsAllPageRowsSelected()
        const isSome = table.getIsSomePageRowsSelected()

        return (
          <div className="flex items-center justify-center">
            <Checkbox
              checked={isAll}
              indeterminate={!isAll && isSome}
              onCheckedChange={value => table.toggleAllPageRowsSelected(Boolean(value))}
            />
          </div>
        )
      },
      cell: ({ row }) => (
        <div className="flex items-center justify-center">
          <Checkbox checked={row.getIsSelected()} onCheckedChange={value => row.toggleSelected(Boolean(value))} />
        </div>
      ),
      enableSorting: false,
      size: 36
    },
    {
      id: 'index',
      header: '序号',
      enableSorting: false,
      cell: ({ row }) => <span className="tabular-nums">{rowOffset + row.index + 1}</span>
    },
    {
      accessorKey: 'nickname',
      header: '昵称',
      cell: ({ row }) => <span>{row.original.nickname || EMPTY_PLACEHOLDER}</span>
    },
    {
      accessorKey: 'username',
      header: '用户名',
      cell: ({ row }) => <span>{row.original.username || EMPTY_PLACEHOLDER}</span>
    },
    {
      accessorKey: 'deptName',
      header: '所属部门',
      cell: ({ row }) => <span>{row.original.deptName || EMPTY_PLACEHOLDER}</span>
    },
    {
      accessorKey: 'status',
      header: '状态',
      cell: ({ row }) => <StatusCell status={row.original.status} />
    },
    {
      id: 'actions',
      header: () => <div className="text-center">操作</div>,
      cell: ({ row }) => (
        <div className="flex items-center justify-center">
          <Button variant="destructive" disabled={isUnassigning} onClick={() => onUnassign(row.original)}>
            取消授权
          </Button>
        </div>
      )
    }
  ]
}

export function RoleUserDrawer({ role, open, onClose }: RoleUserDrawerProps) {
  const queryClient = useQueryClient()
  const shownRole = useLastRecord(role)
  const roleId = shownRole?.id ?? null

  const [pagination, setPagination] = React.useState<PaginationState>({ pageIndex: 0, pageSize: 10 })
  const [rowSelection, setRowSelection] = React.useState<RowSelectionState>({})
  const [keyword, setKeyword] = React.useState('')
  const [isAddOpen, setIsAddOpen] = React.useState(false)
  const [isUnassigning, setIsUnassigning] = React.useState(false)

  // Reset on close, so reopening never starts from a stale keyword, page or selection.
  React.useEffect(() => {
    if (open) return

    setPagination({ pageIndex: 0, pageSize: 10 })
    setRowSelection({})
    setKeyword('')
    setIsAddOpen(false)
  }, [open])

  const memberParams = {
    page: pagination.pageIndex + 1,
    size: pagination.pageSize,
    description: keyword || undefined
  }

  const query = useQuery({
    queryKey: roleKeys.users(roleId, memberParams),
    queryFn: ({ signal }) => {
      if (!roleId) return Promise.resolve({ list: [], total: 0 })
      return listRoleUsers(roleId, memberParams, signal)
    },
    enabled: open && roleId !== null
  })

  const total = query.data?.total ?? 0
  const pageCount = Math.max(Math.ceil(total / pagination.pageSize), 1)

  /** One refresh covers the member list, the cached ids and the "already in the role" marks. */
  const refreshMembers = React.useCallback(() => {
    void queryClient.invalidateQueries({ queryKey: roleKeys.usersRoot(roleId) })
    void queryClient.invalidateQueries({ queryKey: roleKeys.userIds(roleId) })
  }, [queryClient, roleId])

  const handleUnassignSingle = React.useCallback(
    async (member: RoleUserResp) => {
      setIsUnassigning(true)
      try {
        await unassignRoleFromUsers([member.id])
        toast.add({ title: '已取消成员角色授权', type: 'success' })
        refreshMembers()
      } catch {
        // The HTTP layer already reported the failure.
      } finally {
        setIsUnassigning(false)
      }
    },
    [refreshMembers]
  )

  const handleUnassignBatch = async () => {
    const selectedIds = Object.keys(rowSelection)
    if (selectedIds.length === 0) return

    setIsUnassigning(true)
    try {
      await unassignRoleFromUsers(selectedIds)
      toast.add({ title: `已批量取消 ${selectedIds.length} 名成员授权`, type: 'success' })
      setRowSelection({})
      refreshMembers()
    } catch {
      // The HTTP layer already reported the failure.
    } finally {
      setIsUnassigning(false)
    }
  }

  const handleSearch = (next: string) => {
    setKeyword(next)
    setPagination(current => (current.pageIndex === 0 ? current : { ...current, pageIndex: 0 }))
  }

  const columns = React.useMemo(
    () =>
      createMemberColumns({
        isUnassigning,
        onUnassign: handleUnassignSingle,
        rowOffset: pagination.pageIndex * pagination.pageSize
      }),
    [isUnassigning, handleUnassignSingle, pagination.pageIndex, pagination.pageSize]
  )

  const table = useTable({
    features: dataTableFeatures,
    data: query.data?.list ?? EMPTY_MEMBERS,
    columns,
    state: { pagination, rowSelection },
    manualPagination: true,
    rowCount: total,
    pageCount,
    autoResetPageIndex: false,
    enableRowSelection: true,
    onPaginationChange: setPagination,
    onRowSelectionChange: setRowSelection,
    getRowId: row => String(row.id)
  })

  const selectedCount = Object.keys(rowSelection).length

  // Panel widths come from custom CSS vars: 90% below desktop and 48rem on desktop. The nested add
  // drawer uses 89% / 47.5rem, so the two panels keep a small, even stack peek at every breakpoint
  // rather than the nested one covering this drawer.
  return (
    <>
      <EntityDrawer
        open={open}
        onClose={onClose}
        title="角色成员管理"
        icon={<Users className="size-4" />}
        badge={shownRole ? <Badge variant="secondary">{shownRole.name}</Badge> : undefined}
        contentClassName="data-[swipe-axis=x]:[--drawer-content-width:90%] data-[swipe-axis=x]:sm:[--drawer-content-width:90%] data-[swipe-axis=x]:md:[--drawer-content-width:48rem]"
      >
        <div className="flex min-h-0 flex-1 flex-col gap-3 p-4">
          <div className="flex flex-wrap items-center justify-between gap-2">
            <div className="w-56">
              <TableSearch className="h-8 md:w-full" placeholder="搜索用户名" onSearch={handleSearch} />
            </div>

            <div className="flex items-center gap-2">
              <Button onClick={() => setIsAddOpen(true)}>
                <Plus />
                添加成员
              </Button>
              <Button
                variant="destructive"
                disabled={selectedCount === 0 || isUnassigning}
                onClick={handleUnassignBatch}
              >
                <Trash2 />
                {selectedCount > 0 ? `批量取消 ${selectedCount} 个授权` : '批量取消授权'}
              </Button>
            </div>
          </div>

          <div className="flex min-h-0 flex-1 flex-col rounded-md border *:data-[slot=table-container]:min-h-0 *:data-[slot=table-container]:flex-1 *:data-[slot=table-container]:overflow-auto">
            <Table>
              <TableHeader>
                {table.getHeaderGroups().map(headerGroup => (
                  <TableRow key={headerGroup.id}>
                    {headerGroup.headers.map(header => (
                      <TableHead key={header.id}>
                        {header.isPlaceholder ? null : <table.FlexRender header={header} />}
                      </TableHead>
                    ))}
                  </TableRow>
                ))}
              </TableHeader>

              <TableBody>
                {table.getRowModel().rows.length ? (
                  table.getRowModel().rows.map(row => (
                    <TableRow key={row.id} className={row.getIsSelected() ? 'bg-muted/30' : undefined}>
                      {row.getVisibleCells().map(cell => (
                        <TableCell key={cell.id}>
                          <table.FlexRender cell={cell} />
                        </TableCell>
                      ))}
                    </TableRow>
                  ))
                ) : query.isPending ? (
                  Array.from({ length: 4 }).map((_, index) => (
                    <TableRow key={index}>
                      <TableCell colSpan={columns.length} className="py-3">
                        <Skeleton className="h-4 w-full" />
                      </TableCell>
                    </TableRow>
                  ))
                ) : query.isError ? (
                  <TableRow>
                    <TableCell colSpan={columns.length} className="h-32">
                      <QueryState state="error" onRetry={() => void query.refetch()} />
                    </TableCell>
                  </TableRow>
                ) : (
                  <TableRow>
                    <TableCell colSpan={columns.length} className="h-32">
                      <QueryState state="empty" title="暂无成员数据" />
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          </div>

          <TablePagination table={table} showTopBorder={false} />
        </div>

        <DrawerFooter>
          <Button variant="outline" onClick={onClose}>
            关闭
          </Button>
        </DrawerFooter>

        <RoleUserAddDrawer
          role={role}
          open={isAddOpen}
          onClose={() => setIsAddOpen(false)}
          onSuccess={refreshMembers}
        />
      </EntityDrawer>
    </>
  )
}
