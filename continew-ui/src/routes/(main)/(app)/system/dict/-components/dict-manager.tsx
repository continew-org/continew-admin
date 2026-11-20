import * as React from 'react'

import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'

import { ScrollFade } from '@animate-ui/react/primitives/effects/scroll-fade'
import { Bookmark, MoreHorizontal, Pencil, Plus, Trash2 } from 'lucide-react'

import { cn } from 'cn'

import { ListPageCard } from '@/components/list-page-card'
import { QueryState } from '@/components/query-state'
import { TableSearch } from '@/components/table-search'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger
} from '@/components/ui/dropdown-menu'
import { Empty, EmptyDescription, EmptyHeader, EmptyMedia, EmptyTitle } from '@/components/ui/empty'
import { Skeleton } from '@/components/ui/skeleton'
import { toast } from '@/components/ui/toast'
import { useAvailableHeight } from '@/hooks/use-available-height'
import { useFullscreenTransition } from '@/hooks/use-fullscreen-transition'
import { usePermission } from '@/hooks/use-permission'
import { clearDictCache, dictKeys, listDicts, type DictItemResp, type DictResp } from '@/lib/api/dict'

import { DictDeleteDialog, type DictDeleteTarget } from './dict-delete-dialog'
import { DictFormDrawer } from './dict-form-drawer'
import { DictItemFormDrawer } from './dict-item-form-drawer'
import { DictItemPanel } from './dict-item-panel'
import { DictToolbar } from './dict-toolbar'

interface ItemFormTarget {
  readonly dict: DictResp
  readonly item: DictItemResp | null
}

interface DictFormTarget {
  /** The dictionary being edited, or `null` to create one. */
  readonly dict: DictResp | null
}

/** Stable empty array, so the filter memo does not re-evaluate while the query is pending. */
const EMPTY_DICTS: DictResp[] = []

function DictListSkeleton() {
  return (
    <div className="flex flex-col">
      {Array.from({ length: 6 }, (_, index) => (
        <div key={index} className="flex flex-col gap-2 border-t px-4 py-3">
          <Skeleton className="h-4 w-32" />
          <Skeleton className="h-3 w-20" />
        </div>
      ))}
    </div>
  )
}

/**
 * Dictionary management in a master-detail shape: a searchable list of dictionaries on the left and
 * the selected dictionary's paginated items on the right. Only the selected dictionary requests its
 * items, so opening the page costs one list call plus one item page instead of one per dictionary.
 */
export function DictManager() {
  const queryClient = useQueryClient()
  const { hasPermission } = usePermission()
  const canCreateDict = hasPermission('system:dict:create')
  const canUpdateDict = hasPermission('system:dict:update')
  const canDeleteDict = hasPermission('system:dict:delete')
  const canClearCache = hasPermission('system:dict:clearCache')
  const canCreateItem = hasPermission('system:dictItem:create')
  const canUpdateItem = hasPermission('system:dictItem:update')
  const canDeleteItem = hasPermission('system:dictItem:delete')
  const fullscreen = useFullscreenTransition()
  const areaRef = React.useRef<HTMLDivElement>(null)
  useAvailableHeight(areaRef, '--dict-available-height')

  const [keyword, setKeyword] = React.useState('')
  const [selectedDictId, setSelectedDictId] = React.useState<string | number | null>(null)

  // One state per overlay: a null value means closed, and a null `dict` inside the form target
  // means 新增.
  const [dictFormTarget, setDictFormTarget] = React.useState<DictFormTarget | null>(null)
  const [itemFormTarget, setItemFormTarget] = React.useState<ItemFormTarget | null>(null)
  const [deleteTarget, setDeleteTarget] = React.useState<DictDeleteTarget | null>(null)

  const query = useQuery({
    queryKey: dictKeys.list(),
    queryFn: ({ signal }) => listDicts(undefined, signal)
  })

  const dicts = query.data ?? EMPTY_DICTS

  // The backend clears one dictionary code per call, so a global clear loops over every code.
  const clearCacheMutation = useMutation({
    mutationFn: (codes: string[]) => Promise.all(codes.map(code => clearDictCache(code))),
    onSuccess: () => {
      toast.add({ title: '字典缓存已成功清除', type: 'success' })
      void queryClient.invalidateQueries({ queryKey: dictKeys.all })
    }
  })

  const filteredDicts = React.useMemo(() => {
    const needle = keyword.trim().toLowerCase()
    if (!needle) {
      return dicts
    }

    return dicts.filter(dict =>
      [dict.name, dict.code, dict.description ?? ''].some(value => value.toLowerCase().includes(needle))
    )
  }, [dicts, keyword])

  // Keep a valid selection: default to the first dictionary, and fall back to it when the selected
  // one is filtered out or deleted.
  React.useEffect(() => {
    if (filteredDicts.length === 0) {
      if (selectedDictId !== null) {
        setSelectedDictId(null)
      }
      return
    }

    const stillVisible = filteredDicts.some(dict => String(dict.id) === String(selectedDictId))
    if (!stillVisible) {
      setSelectedDictId(filteredDicts[0].id)
    }
  }, [filteredDicts, selectedDictId])

  // Fall back to the first dictionary in the same render, so the right pane never flashes empty
  // while the selection effect above catches up.
  const selectedDict = React.useMemo(
    () => filteredDicts.find(dict => String(dict.id) === String(selectedDictId)) ?? filteredDicts[0] ?? null,
    [filteredDicts, selectedDictId]
  )

  const handleRefresh = React.useCallback(() => {
    void query.refetch()
    void queryClient.invalidateQueries({ queryKey: dictKeys.items() })
  }, [query, queryClient])

  const actions = (
    <DictToolbar
      canClearCache={canClearCache && dicts.length > 0}
      isClearingCache={clearCacheMutation.isPending}
      isRefreshing={query.isFetching && !query.isPending}
      isFullscreen={fullscreen.isFullscreen}
      onClearCache={() => clearCacheMutation.mutate(dicts.map(dict => dict.code))}
      onRefresh={handleRefresh}
      onToggleFullscreen={fullscreen.toggleFullscreen}
    />
  )

  const body = query.isPending ? (
    <DictListSkeleton />
  ) : query.isError ? (
    <QueryState
      state="error"
      title="字典加载失败"
      onRetry={() => void query.refetch()}
      className="min-h-40 rounded-none"
    />
  ) : dicts.length === 0 ? (
    <Empty className="min-h-[24rem] rounded-none">
      <EmptyHeader>
        <EmptyMedia variant="icon">
          <Bookmark />
        </EmptyMedia>
        <EmptyTitle>暂无字典</EmptyTitle>
        <EmptyDescription>新增字典后即可维护字典项</EmptyDescription>
      </EmptyHeader>
    </Empty>
  ) : (
    <div
      className={cn(
        'grid min-h-0 grid-cols-1 border-t md:grid-cols-[18rem_minmax(0,1fr)]',
        fullscreen.isFullscreen ? 'h-[calc(100vh-6.5rem)]' : 'md:h-[var(--dict-available-height)]'
      )}
    >
      <aside className="flex min-h-0 flex-col border-b md:border-r md:border-b-0">
        <div className="flex h-14 shrink-0 items-center gap-2 px-3">
          <TableSearch className="h-8 w-full md:w-full" placeholder="搜索字典" onSearch={setKeyword} clearable />
          {canCreateDict && (
            <Button
              variant="outline"
              size="icon"
              className="shrink-0"
              onClick={() => setDictFormTarget({ dict: null })}
            >
              <Plus />
              <span className="sr-only">新增字典</span>
            </Button>
          )}
        </div>

        {filteredDicts.length === 0 ? (
          <p className="px-4 py-6 text-center text-sm text-muted-foreground">没有匹配的字典</p>
        ) : (
          <ScrollFade className="min-h-0 flex-1" shrink={0} blur={4}>
            <ul className="flex flex-col gap-0.5 p-1.5">
              {filteredDicts.map(dict => {
                const isSelected = String(dict.id) === String(selectedDict?.id)

                return (
                  <li key={String(dict.id)} className="group/dict relative">
                    <button
                      type="button"
                      onClick={() => setSelectedDictId(dict.id)}
                      aria-current={isSelected ? 'true' : undefined}
                      className={cn(
                        'flex w-full flex-col gap-0.5 rounded-md px-2.5 py-2 pr-9 text-left transition-colors',
                        isSelected ? 'bg-accent text-accent-foreground' : 'hover:bg-accent/60'
                      )}
                    >
                      <span className="flex min-w-0 items-center gap-1.5">
                        <span className={cn('min-w-0 truncate text-sm', isSelected && 'font-medium')}>{dict.name}</span>
                        {dict.isSystem && (
                          <Badge variant="outline" className="shrink-0 px-1 py-0 text-[10px] font-normal">
                            内置
                          </Badge>
                        )}
                      </span>
                      <span className="truncate text-xs text-muted-foreground">{dict.code}</span>
                    </button>

                    {(canUpdateDict || (canDeleteDict && !dict.isSystem)) && (
                      <div className="pointer-events-none absolute top-1/2 right-1 -translate-y-1/2 opacity-0 transition-opacity group-hover/dict:pointer-events-auto group-hover/dict:opacity-100 focus-within:pointer-events-auto focus-within:opacity-100">
                        <DropdownMenu>
                          <DropdownMenuTrigger render={<Button variant="ghost" size="icon-sm" />}>
                            <MoreHorizontal className="size-4" />
                          </DropdownMenuTrigger>
                          <DropdownMenuContent align="end" className="w-36">
                            {canUpdateDict && (
                              <DropdownMenuItem onClick={() => setDictFormTarget({ dict })}>
                                <Pencil />
                                修改字典
                              </DropdownMenuItem>
                            )}
                            {canUpdateDict && canDeleteDict && !dict.isSystem && <DropdownMenuSeparator />}
                            {canDeleteDict && !dict.isSystem && (
                              <DropdownMenuItem
                                variant="destructive"
                                onClick={() => setDeleteTarget({ type: 'dict', dict })}
                              >
                                <Trash2 />
                                删除字典
                              </DropdownMenuItem>
                            )}
                          </DropdownMenuContent>
                        </DropdownMenu>
                      </div>
                    )}
                  </li>
                )
              })}
            </ul>
          </ScrollFade>
        )}
      </aside>

      <section className="flex min-h-0 flex-col">
        {selectedDict ? (
          <DictItemPanel
            key={String(selectedDict.id)}
            dict={selectedDict}
            canCreateItem={canCreateItem}
            canUpdateItem={canUpdateItem}
            canDeleteItem={canDeleteItem}
            canDeleteDict={canDeleteDict}
            onCreateItem={target => setItemFormTarget({ dict: target, item: null })}
            onEditItem={(target, item) => setItemFormTarget({ dict: target, item })}
            onDeleteItem={item => setDeleteTarget({ type: 'item', item })}
            onDeleteDict={target => setDeleteTarget({ type: 'dict', dict: target })}
          />
        ) : (
          <Empty className="min-h-[24rem] rounded-none">
            <EmptyHeader>
              <EmptyMedia variant="icon">
                <Bookmark />
              </EmptyMedia>
              <EmptyTitle>请选择字典</EmptyTitle>
              <EmptyDescription>从左侧选择一个字典查看其字典项</EmptyDescription>
            </EmptyHeader>
          </Empty>
        )}
      </section>
    </div>
  )

  return (
    <>
      <ListPageCard title="字典管理" description="维护和管理系统字典和字典项" actions={actions} fullscreen={fullscreen}>
        <div ref={areaRef} className="flex min-h-0 flex-1 flex-col">
          {body}
        </div>
      </ListPageCard>

      <DictFormDrawer
        dict={dictFormTarget?.dict ?? null}
        open={dictFormTarget !== null}
        onClose={() => setDictFormTarget(null)}
      />
      <DictItemFormDrawer
        item={itemFormTarget?.item ?? null}
        dictId={itemFormTarget?.dict.id ?? ''}
        open={itemFormTarget !== null}
        onClose={() => setItemFormTarget(null)}
      />
      <DictDeleteDialog target={deleteTarget} open={deleteTarget !== null} onClose={() => setDeleteTarget(null)} />
    </>
  )
}
