import * as React from 'react'

import { keepPreviousData, useQuery, useQueryClient } from '@tanstack/react-query'
import { type PaginationState } from '@tanstack/react-table'

import { UserPlus, X } from 'lucide-react'

import { DeptTreeSelect } from '@/components/dept-tree-select'
import { EntityDrawer } from '@/components/entity-drawer'
import { PaginationArrow } from '@/components/pagination-arrow'
import { QueryState } from '@/components/query-state'
import { StatusCell } from '@/components/status-cell'
import { TableSearch } from '@/components/table-search'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Checkbox } from '@/components/ui/checkbox'
import { DrawerFooter } from '@/components/ui/drawer'
import { Pagination, PaginationContent, PaginationItem } from '@/components/ui/pagination'
import { Skeleton } from '@/components/ui/skeleton'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { toast } from '@/components/ui/toast'
import { useLastRecord } from '@/hooks/use-last-record'
import { assignRoleToUsers, listRoleUserIds, roleKeys, type RoleResp } from '@/lib/api/role'
import { listUsers, userKeys, type UserResp } from '@/lib/api/user'
import { EMPTY_PLACEHOLDER } from '@/lib/constants'
import { getInitials } from '@/lib/utils'

const PAGE_SIZE = 20
const DEFAULT_SORT = 't1.id,desc'

const EMPTY_USERS: UserResp[] = []

interface PaginationFooterProps {
  readonly total: number
  readonly pageIndex: number
  readonly pageCount: number
  readonly onPrevious: () => void
  readonly onNext: () => void
}

/** Compact footer shared by the two panels: total count plus previous/next and the page position. */
function PaginationFooter({ total, pageIndex, pageCount, onPrevious, onNext }: PaginationFooterProps) {
  const currentPage = Math.min(pageIndex + 1, pageCount)

  return (
    <div className="flex h-10 shrink-0 items-center justify-between gap-2 border-t px-3 text-xs text-muted-foreground">
      <span className="tabular-nums">共 {total} 条</span>
      <Pagination className="mx-0 w-auto">
        <PaginationContent>
          <PaginationItem>
            <PaginationArrow direction="previous" disabled={pageIndex <= 0} onClick={onPrevious} />
          </PaginationItem>
          <span className="tabular-nums">
            {currentPage} / {pageCount}
          </span>
          <PaginationItem>
            <PaginationArrow direction="next" disabled={currentPage >= pageCount} onClick={onNext} />
          </PaginationItem>
        </PaginationContent>
      </Pagination>
    </div>
  )
}

interface RoleUserAddDrawerProps {
  readonly role: RoleResp | null
  readonly open: boolean
  readonly onClose: () => void
  readonly onSuccess: () => void
}

/**
 * The "添加成员" nested drawer opened from the role member drawer. A transfer-style picker:
 * search and a department tree filter on top, the paged user table on the left and the chosen
 * users on the right. Selection is kept in a map keyed by user id, so it survives paging,
 * searching and department changes. On mobile the two panels stack vertically.
 */
export function RoleUserAddDrawer({ role, open, onClose, onSuccess }: RoleUserAddDrawerProps) {
  const queryClient = useQueryClient()
  const shownRole = useLastRecord(role)
  const roleId = shownRole?.id ?? null

  const [keyword, setKeyword] = React.useState('')
  const [deptId, setDeptId] = React.useState<string | undefined>(undefined)
  const [pagination, setPagination] = React.useState<PaginationState>({ pageIndex: 0, pageSize: PAGE_SIZE })
  // Choosing across pages needs the user objects, not just ids, so the panel can name every pick.
  const [selected, setSelected] = React.useState<Map<string, UserResp>>(() => new Map())
  const [selectedPageIndex, setSelectedPageIndex] = React.useState(0)
  const [isSubmitting, setIsSubmitting] = React.useState(false)

  // Reset on close, so reopening never starts from a stale filter, page or selection.
  React.useEffect(() => {
    if (open) return

    setKeyword('')
    setDeptId(undefined)
    setPagination({ pageIndex: 0, pageSize: PAGE_SIZE })
    setSelected(new Map())
    setSelectedPageIndex(0)
  }, [open])

  // The ids already in the role, so the list greys them out instead of offering them again.
  const assignedQuery = useQuery({
    queryKey: roleKeys.userIds(roleId),
    queryFn: ({ signal }) => (roleId ? listRoleUserIds(roleId, signal) : Promise.resolve([])),
    enabled: open && roleId !== null,
    staleTime: 0
  })

  const assignedIds = React.useMemo(() => new Set((assignedQuery.data ?? []).map(String)), [assignedQuery.data])

  const params = {
    page: pagination.pageIndex + 1,
    size: pagination.pageSize,
    description: keyword || undefined,
    deptId,
    sort: DEFAULT_SORT
  }

  const query = useQuery({
    queryKey: userKeys.list(params),
    queryFn: ({ signal }) => listUsers(params, signal),
    enabled: open && roleId !== null,
    placeholderData: keepPreviousData
  })

  const users = query.data?.list ?? EMPTY_USERS
  const total = query.data?.total ?? 0
  const pageCount = Math.max(Math.ceil(total / pagination.pageSize), 1)

  const selectedUsers = Array.from(selected.values())
  const selectedPageCount = Math.max(1, Math.ceil(selectedUsers.length / PAGE_SIZE))
  const safeSelectedPageIndex = Math.min(selectedPageIndex, selectedPageCount - 1)
  const pagedSelectedUsers = selectedUsers.slice(
    safeSelectedPageIndex * PAGE_SIZE,
    safeSelectedPageIndex * PAGE_SIZE + PAGE_SIZE
  )

  const toggleUser = React.useCallback((user: UserResp) => {
    const id = String(user.id)

    setSelected(previous => {
      const next = new Map(previous)
      if (next.has(id)) {
        next.delete(id)
      } else {
        next.set(id, user)
      }
      return next
    })
  }, [])

  // Only rows outside the role can be picked, so the page-level "select all" ignores the rest.
  const selectableUsers = users.filter(user => !assignedIds.has(String(user.id)))
  const isAllPageSelected = selectableUsers.length > 0 && selectableUsers.every(user => selected.has(String(user.id)))
  const isSomePageSelected = selectableUsers.some(user => selected.has(String(user.id)))

  const togglePage = (checked: boolean) => {
    setSelected(previous => {
      const next = new Map(previous)
      for (const user of selectableUsers) {
        const id = String(user.id)
        if (checked) {
          next.set(id, user)
        } else {
          next.delete(id)
        }
      }
      return next
    })
  }

  const handleSearch = (next: string) => {
    setKeyword(next)
    setPagination(current => (current.pageIndex === 0 ? current : { ...current, pageIndex: 0 }))
  }

  const handleDeptChange = (value: string) => {
    setDeptId(value || undefined)
    setPagination(current => (current.pageIndex === 0 ? current : { ...current, pageIndex: 0 }))
  }

  const handleSubmit = async () => {
    if (roleId === null || selected.size === 0) return

    setIsSubmitting(true)
    try {
      await assignRoleToUsers(roleId, Array.from(selected.keys()))
      toast.add({ title: `已添加 ${selected.size} 名成员`, type: 'success' })
      void queryClient.invalidateQueries({ queryKey: roleKeys.userIds(roleId) })
      onSuccess()
      onClose()
    } catch {
      // The HTTP layer already reported the failure.
    } finally {
      setIsSubmitting(false)
    }
  }

  // A touch narrower than the parent member drawer (89% below desktop / 47.5rem on desktop) so the
  // parent keeps a small, even peek behind this nested panel.
  return (
    <EntityDrawer
      open={open}
      onClose={onClose}
      title="添加成员"
      icon={<UserPlus className="size-4" />}
      badge={shownRole ? <Badge variant="secondary">{shownRole.name}</Badge> : undefined}
      contentClassName="data-[swipe-axis=x]:[--drawer-content-width:89%] data-[swipe-axis=x]:sm:[--drawer-content-width:89%] data-[swipe-axis=x]:md:[--drawer-content-width:47.5rem]"
    >
      <div className="flex min-h-0 flex-1 flex-col gap-3 p-4">
        <div className="flex shrink-0 flex-col gap-2 sm:flex-row sm:items-center">
          <TableSearch className="h-8 w-full sm:w-64" placeholder="搜索用户名或昵称" onSearch={handleSearch} />
          <DeptTreeSelect
            triggerPrefix="部门"
            placeholder="全部部门"
            placeholderColor="foreground"
            showAllOption
            allOptionLabel="全部部门"
            value={deptId}
            onChange={handleDeptChange}
            className="w-full sm:w-44"
          />
        </div>

        <div className="flex min-h-0 flex-1 flex-col gap-3 overflow-y-auto md:flex-row md:overflow-hidden">
          {/* User list */}
          <div className="flex flex-none min-w-0 flex-col md:min-h-0 md:flex-1">
            <div className="flex min-w-0 flex-col rounded-md border *:data-[slot=table-container]:overflow-auto md:min-h-0 md:flex-1 md:*:data-[slot=table-container]:min-h-0 md:*:data-[slot=table-container]:flex-1">
              <Table>
                <TableHeader className="[&_th]:bg-muted">
                  <TableRow>
                    <TableHead className="w-9">
                      <Checkbox
                        checked={isAllPageSelected}
                        indeterminate={!isAllPageSelected && isSomePageSelected}
                        disabled={selectableUsers.length === 0}
                        onCheckedChange={value => togglePage(Boolean(value))}
                        aria-label="全选本页"
                      />
                    </TableHead>
                    <TableHead className="w-12">序号</TableHead>
                    <TableHead>昵称</TableHead>
                    <TableHead>用户名</TableHead>
                    <TableHead>状态</TableHead>
                    <TableHead>所属部门</TableHead>
                    <TableHead>描述</TableHead>
                  </TableRow>
                </TableHeader>

                <TableBody>
                  {query.isPending ? (
                    Array.from({ length: 5 }).map((_, index) => (
                      <TableRow key={index}>
                        <TableCell colSpan={7} className="py-3">
                          <Skeleton className="h-4 w-full" />
                        </TableCell>
                      </TableRow>
                    ))
                  ) : query.isError ? (
                    <TableRow>
                      <TableCell colSpan={7} className="h-32">
                        <QueryState state="error" title="用户列表加载失败" onRetry={() => void query.refetch()} />
                      </TableCell>
                    </TableRow>
                  ) : users.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={7} className="h-32">
                        <QueryState state="empty" title="未找到用户" />
                      </TableCell>
                    </TableRow>
                  ) : (
                    users.map((user, index) => {
                      const id = String(user.id)
                      const isAssigned = assignedIds.has(id)
                      const isSelected = selected.has(id)
                      const rowNumber = pagination.pageIndex * pagination.pageSize + index + 1
                      const rowClassName = isAssigned
                        ? 'cursor-not-allowed opacity-50'
                        : isSelected
                          ? 'cursor-pointer bg-muted/30'
                          : 'cursor-pointer hover:bg-muted/40'

                      return (
                        <TableRow
                          key={id}
                          className={rowClassName}
                          onClick={() => {
                            if (!isAssigned) toggleUser(user)
                          }}
                        >
                          <TableCell onClick={event => event.stopPropagation()}>
                            <Checkbox
                              checked={isSelected}
                              disabled={isAssigned}
                              aria-label={user.nickname || user.username}
                              onCheckedChange={() => {
                                if (!isAssigned) toggleUser(user)
                              }}
                            />
                          </TableCell>
                          <TableCell className="py-2.5 tabular-nums">{rowNumber}</TableCell>
                          <TableCell className="py-2.5">
                            <div className="flex items-center gap-2">
                              <span>{user.nickname || EMPTY_PLACEHOLDER}</span>
                              {isAssigned ? (
                                <Badge variant="outline" className="text-[10px] font-normal text-muted-foreground">
                                  已在角色中
                                </Badge>
                              ) : null}
                            </div>
                          </TableCell>
                          <TableCell className="py-2.5">{user.username}</TableCell>
                          <TableCell className="py-2.5">
                            <StatusCell status={user.status} />
                          </TableCell>
                          <TableCell className="py-2.5">{user.deptName || EMPTY_PLACEHOLDER}</TableCell>
                          <TableCell className="py-2.5">{user.description || EMPTY_PLACEHOLDER}</TableCell>
                        </TableRow>
                      )
                    })
                  )}
                </TableBody>
              </Table>

              {total > 0 ? (
                <PaginationFooter
                  total={total}
                  pageIndex={pagination.pageIndex}
                  pageCount={pageCount}
                  onPrevious={() =>
                    setPagination(current => ({ ...current, pageIndex: Math.max(0, current.pageIndex - 1) }))
                  }
                  onNext={() =>
                    setPagination(current => ({
                      ...current,
                      pageIndex: Math.min(pageCount - 1, current.pageIndex + 1)
                    }))
                  }
                />
              ) : null}
            </div>

          </div>

          {/* Chosen users */}
          <div className="flex flex-none flex-col rounded-md border md:min-h-0 md:w-64 md:shrink-0">
            <div className="flex h-10 shrink-0 items-center justify-between border-b bg-muted px-3">
              <span>已选 {selected.size} 人</span>
              {selected.size > 0 ? (
                <Button
                  variant="default"
                  onClick={() => {
                    setSelected(new Map())
                    setSelectedPageIndex(0)
                  }}
                >
                  清空
                </Button>
              ) : null}
            </div>

            <div className="p-2 md:min-h-0 md:flex-1 md:overflow-y-auto">
              {selected.size === 0 ? (
                <div className="flex h-24 items-center justify-center text-xs text-muted-foreground">未选择用户</div>
              ) : (
                <div className="flex flex-col gap-1">
                  {pagedSelectedUsers.map(user => {
                    const name = user.nickname || user.username

                    return (
                      <div
                        key={String(user.id)}
                        className="flex items-center gap-2 rounded-md px-1.5 py-1 hover:bg-muted/40"
                      >
                        <Avatar className="size-6">
                          <AvatarImage src={user.avatar ?? undefined} alt="" />
                          <AvatarFallback className="text-[10px]">{getInitials(name)}</AvatarFallback>
                        </Avatar>
                        <div className="flex min-w-0 flex-1 flex-col leading-tight">
                          <span className="truncate text-xs font-medium">{name}</span>
                          <span className="truncate text-[11px] text-muted-foreground">
                            {user.username}
                            {user.deptName ? ` · ${user.deptName}` : ''}
                          </span>
                        </div>
                        <Button
                          variant="ghost"
                          size="icon-xs"
                          aria-label={`移除 ${name}`}
                          onClick={() => toggleUser(user)}
                        >
                          <X />
                        </Button>
                      </div>
                    )
                  })}
                </div>
              )}
            </div>

            {selectedUsers.length > 0 ? (
              <PaginationFooter
                total={selectedUsers.length}
                pageIndex={safeSelectedPageIndex}
                pageCount={selectedPageCount}
                onPrevious={() => setSelectedPageIndex(Math.max(0, safeSelectedPageIndex - 1))}
                onNext={() => setSelectedPageIndex(Math.min(selectedPageCount - 1, safeSelectedPageIndex + 1))}
              />
            ) : null}
          </div>
        </div>
      </div>

      <DrawerFooter>
        <Button variant="outline" onClick={onClose} disabled={isSubmitting}>
          取消
        </Button>
        <Button onClick={() => void handleSubmit()} disabled={selected.size === 0 || isSubmitting}>
          {isSubmitting ? '添加中' : selected.size > 0 ? `确定添加 (${selected.size})` : '确定添加'}
        </Button>
      </DrawerFooter>
    </EntityDrawer>
  )
}
