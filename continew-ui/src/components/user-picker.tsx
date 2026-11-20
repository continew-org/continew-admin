import * as React from 'react'

import { useInfiniteQuery } from '@tanstack/react-query'

import { Search } from 'lucide-react'

import { QueryState } from '@/components/query-state'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Checkbox } from '@/components/ui/checkbox'
import { InputGroup, InputGroupAddon, InputGroupInput } from '@/components/ui/input-group'
import { Skeleton } from '@/components/ui/skeleton'
import { useDebouncedValue } from '@/hooks/use-debounced-value'
import { listUsers, userKeys, type UserResp } from '@/lib/api/user'
import { getInitials } from '@/lib/utils'

const PAGE_SIZE = 100

/** Server-side user search that pages through matches, shared by every user picker. */
export function useUserPickerQuery(search: string, enabled: boolean) {
  const keyword = useDebouncedValue(search).trim()

  const query = useInfiniteQuery({
    queryKey: userKeys.picker({ page: 1, size: PAGE_SIZE, description: keyword || undefined }),
    queryFn: ({ pageParam, signal }) =>
      listUsers({ page: pageParam, size: PAGE_SIZE, description: keyword || undefined }, signal),
    initialPageParam: 1,
    getNextPageParam: (lastPage, allPages) => {
      const loaded = allPages.reduce((count, page) => count + page.list.length, 0)
      return loaded < lastPage.total ? allPages.length + 1 : undefined
    },
    enabled
  })

  const users = React.useMemo(() => query.data?.pages.flatMap(page => page.list) ?? [], [query.data])

  return { query, users }
}

function UserPickerSkeleton() {
  return (
    <div className="flex flex-col gap-2 p-2">
      {Array.from({ length: 5 }, (_, index) => (
        <div key={index} className="flex items-center gap-3 py-1">
          <Skeleton className="size-8 rounded-full" />
          <div className="flex flex-1 flex-col gap-1">
            <Skeleton className="h-4 w-28" />
            <Skeleton className="h-3 w-20" />
          </div>
        </div>
      ))}
    </div>
  )
}

interface UserPickerProps {
  readonly open: boolean
  /** Selected user IDs, keyed as strings so both pickers share one contract. */
  readonly selectedIds: ReadonlySet<string>
  readonly onToggle: (id: string) => void
  /** Rows that cannot be picked, with an explanatory badge (for example, already in the role). */
  readonly isDisabled?: (user: UserResp) => boolean
  readonly disabledBadge?: string
  readonly searchPlaceholder?: string
  readonly emptyText?: string
}

/**
 * The searchable, paged user list both dialogs render. Selection and the paged query contract live
 * here so the notice picker and the role member dialog stay identical; the dialog keeps the header,
 * footer and the business action.
 */
export function UserPicker({
  open,
  selectedIds,
  onToggle,
  isDisabled,
  disabledBadge = '已选择',
  searchPlaceholder = '搜索用户名、昵称或部门',
  emptyText = '未找到用户'
}: UserPickerProps) {
  const [search, setSearch] = React.useState('')
  const { query, users } = useUserPickerQuery(search, open)

  React.useEffect(() => {
    if (!open) {
      setSearch('')
    }
  }, [open])

  return (
    <div className="flex flex-col gap-3">
      <InputGroup>
        <InputGroupAddon>
          <Search className="size-3.5 text-muted-foreground" />
        </InputGroupAddon>
        <InputGroupInput
          placeholder={searchPlaceholder}
          value={search}
          onChange={event => setSearch(event.target.value)}
        />
      </InputGroup>

      <div className="max-h-72 min-h-48 overflow-y-auto overscroll-contain rounded-md border p-1 select-none">
        {query.isPending ? (
          <UserPickerSkeleton />
        ) : query.isError ? (
          <QueryState state="error" title="用户列表加载失败" onRetry={() => void query.refetch()} className="h-36" />
        ) : users.length === 0 ? (
          <div className="flex h-36 items-center justify-center text-xs text-muted-foreground">{emptyText}</div>
        ) : (
          <div className="flex flex-col gap-0.5">
            {users.map(user => {
              const id = String(user.id)
              const disabled = isDisabled?.(user) ?? false
              const selected = selectedIds.has(id)
              const name = user.nickname || user.username
              const rowClassName = disabled
                ? 'cursor-not-allowed bg-muted/20 opacity-50'
                : selected
                  ? 'cursor-pointer bg-accent text-accent-foreground'
                  : 'cursor-pointer hover:bg-accent/60'

              return (
                <div
                  key={id}
                  className={`flex items-center justify-between rounded-md px-2 py-1.5 transition-colors ${rowClassName}`}
                  onClick={() => {
                    if (!disabled) onToggle(id)
                  }}
                >
                  <div className="flex items-center gap-2.5">
                    <Checkbox
                      checked={disabled || selected}
                      disabled={disabled}
                      aria-label={name}
                      onCheckedChange={() => {
                        if (!disabled) onToggle(id)
                      }}
                      onClick={event => event.stopPropagation()}
                      className="size-4"
                    />
                    <Avatar className="size-7">
                      <AvatarImage src={user.avatar ?? undefined} alt="" />
                      <AvatarFallback className="text-xs">{getInitials(name)}</AvatarFallback>
                    </Avatar>
                    <div className="flex flex-col leading-tight">
                      <span className="text-xs font-medium">{name}</span>
                      <span className="text-[11px] font-normal text-muted-foreground">
                        {user.username} {user.deptName ? `· ${user.deptName}` : ''}
                      </span>
                    </div>
                  </div>

                  {disabled && disabledBadge ? (
                    <Badge variant="outline" className="text-[10px] font-normal text-muted-foreground">
                      {disabledBadge}
                    </Badge>
                  ) : null}
                </div>
              )
            })}
            {query.hasNextPage ? (
              <div className="flex justify-center py-2">
                <Button
                  type="button"
                  variant="ghost"
                  size="xs"
                  disabled={query.isFetchingNextPage}
                  onClick={() => void query.fetchNextPage()}
                >
                  {query.isFetchingNextPage ? '加载中…' : '加载更多'}
                </Button>
              </div>
            ) : null}
          </div>
        )}
      </div>
    </div>
  )
}
