import * as React from 'react'

import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { type ColumnVisibilityState, type ExpandedState, useTable } from '@tanstack/react-table'

import { ChevronsDownUp, ChevronsUpDown, Plus, Search, Trash2, X } from 'lucide-react'

import { DataTable } from '@/components/data-table'
import { ListPageCard } from '@/components/list-page-card'
import { TableToolbar } from '@/components/table-toolbar'
import { Button } from '@/components/ui/button'
import { InputGroup, InputGroupAddon, InputGroupInput } from '@/components/ui/input-group'
import { Spinner } from '@/components/ui/spinner'
import { toast } from '@/components/ui/toast'
import { useAvailableHeight } from '@/hooks/use-available-height'
import { useFullscreenTransition } from '@/hooks/use-fullscreen-transition'
import { usePermission } from '@/hooks/use-permission'
import { clearMenuCache, getMenuTree, menuKeys, type MenuResp } from '@/lib/api/menu'
import { dataTableFeatures } from '@/lib/data-table-features'
import { reportError } from '@/lib/http'
import { cn } from 'cn'

import { createMenuColumns, type MenuAction } from './menu-columns'
import { MenuDeleteDialog } from './menu-delete-dialog'
import { MenuFormDrawer } from './menu-form-drawer'

const DEFAULT_COLUMN_VISIBILITY: ColumnVisibilityState = {}

const EMPTY_TREE: MenuResp[] = []

/** Recursively filters tree nodes by title, path, or permission code, preserving parent paths. */
function filterMenuTree(nodes: MenuResp[], keyword: string): MenuResp[] {
  const q = keyword.toLowerCase().trim()
  if (!q) return nodes

  function filterNode(node: MenuResp): MenuResp | null {
    const matches =
      node.title.toLowerCase().includes(q) ||
      Boolean(node.path?.toLowerCase().includes(q)) ||
      Boolean(node.permission?.toLowerCase().includes(q))
    const children = (node.children ?? []).flatMap(child => filterNode(child) ?? [])

    return matches || children.length > 0 ? { ...node, children } : null
  }

  return nodes.flatMap(node => filterNode(node) ?? [])
}

interface FormTarget {
  readonly menuId: string | number | null
  readonly parentId: string | number | null
}

export function MenuList() {
  const queryClient = useQueryClient()
  const { hasPermission } = usePermission()

  const hasCreatePermission = hasPermission('system:menu:create')
  const hasClearCachePermission = hasPermission('system:menu:clearCache')

  const [searchInput, setSearchInput] = React.useState('')
  const keyword = searchInput.trim()

  const [expanded, setExpanded] = React.useState<ExpandedState>({})
  const [columnVisibility, setColumnVisibility] = React.useState<ColumnVisibilityState>(DEFAULT_COLUMN_VISIBILITY)

  // One state per overlay: a null value means closed, so "which record" and "is it open" cannot drift apart.
  const [formTarget, setFormTarget] = React.useState<FormTarget | null>(null)
  const [deletingMenu, setDeletingMenu] = React.useState<MenuResp | null>(null)

  const fullscreen = useFullscreenTransition()

  const tableAreaRef = React.useRef<HTMLDivElement>(null)
  const remeasureTableArea = useAvailableHeight(tableAreaRef, '--table-available-height')

  const query = useQuery({
    queryKey: menuKeys.trees(),
    queryFn: ({ signal }) => getMenuTree(undefined, signal)
  })

  const rawTree = query.data ?? EMPTY_TREE

  const filteredTree = React.useMemo(() => filterMenuTree(rawTree, keyword), [rawTree, keyword])

  React.useEffect(() => {
    if (keyword) {
      setExpanded(true)
    }
  }, [keyword])

  const handleRowAction = React.useCallback((action: MenuAction, menu: MenuResp) => {
    if (action === 'edit') {
      setFormTarget({ menuId: menu.id, parentId: null })
      return
    }

    if (action === 'add-child') {
      setFormTarget({ menuId: null, parentId: menu.id })
      return
    }

    if (action === 'delete') {
      setDeletingMenu(menu)
    }
  }, [])

  const columns = React.useMemo(() => createMenuColumns(handleRowAction), [handleRowAction])

  const table = useTable({
    features: dataTableFeatures,
    data: filteredTree,
    columns,
    getSubRows: (row: MenuResp) => row.children,
    // Real IDs, so expanding or filtering keeps row identity and does not remount every row.
    getRowId: row => String(row.id),
    state: {
      expanded,
      columnVisibility
    },
    manualPagination: true,
    paginateExpandedRows: false,
    onExpandedChange: setExpanded,
    onColumnVisibilityChange: setColumnVisibility
  })

  // Same as the department tree: expansion and filtering change the row count in the commit that
  // paints the new rows, so measure before paint instead of letting the page overflow for one frame.
  const rowCount = table.getRowModel().rows.length
  React.useLayoutEffect(() => {
    remeasureTableArea()
  }, [remeasureTableArea, rowCount])

  const clearCacheMutation = useMutation({
    mutationFn: () => clearMenuCache(),
    onSuccess: () => {
      toast.add({ title: '菜单缓存已成功清除', type: 'success' })
      void queryClient.invalidateQueries({ queryKey: menuKeys.trees() })
    },
    onError: error => reportError(error, '清除缓存失败，请重试')
  })

  const isAllExpanded = expanded === true || (typeof expanded === 'object' && Object.keys(expanded).length > 0)
  const handleToggleExpandAll = () => setExpanded(isAllExpanded ? {} : true)

  const filters = (
    <InputGroup className="w-full md:w-64">
      <InputGroupAddon align="inline-start">
        <Search className="size-3.5" />
      </InputGroupAddon>
      <InputGroupInput
        className="text-sm"
        placeholder="搜索名称、路由、权限"
        value={searchInput}
        onChange={event => setSearchInput(event.target.value)}
      />
      {searchInput ? (
        <InputGroupAddon align="inline-end">
          <Button type="button" variant="ghost" size="icon-xs" onClick={() => setSearchInput('')}>
            <X className="size-3" />
          </Button>
        </InputGroupAddon>
      ) : null}
    </InputGroup>
  )

  const actions = (
    <TableToolbar
      table={table}
      isRefreshing={query.isFetching && !query.isPending}
      isFullscreen={fullscreen.isFullscreen}
      onRefresh={() => void query.refetch()}
      onToggleFullscreen={fullscreen.toggleFullscreen}
      onResetColumns={() => setColumnVisibility(DEFAULT_COLUMN_VISIBILITY)}
      actions={
        <>
          {hasCreatePermission && (
            <Button onClick={() => setFormTarget({ menuId: null, parentId: null })}>
              <Plus />
              新增菜单
            </Button>
          )}

          {hasClearCachePermission && (
            <Button
              variant="outline"
              disabled={clearCacheMutation.isPending}
              onClick={() => clearCacheMutation.mutate()}
              className="text-amber-600 hover:text-amber-700 dark:text-amber-400 dark:hover:text-amber-300"
            >
              {clearCacheMutation.isPending ? <Spinner /> : <Trash2 />}
              清除缓存
            </Button>
          )}

          <Button variant="outline" onClick={handleToggleExpandAll} size="icon">
            {isAllExpanded ? <ChevronsDownUp /> : <ChevronsUpDown />}
          </Button>
        </>
      }
    />
  )

  return (
    <>
      <ListPageCard
        title="菜单管理"
        description="配置系统目录、路由页面与按钮权限"
        filters={filters}
        actions={actions}
        fullscreen={fullscreen}
      >
        <div ref={tableAreaRef} className="flex min-h-0 flex-1 flex-col">
          <DataTable
            table={table}
            isLoading={query.isPending}
            isError={query.isError}
            isShowingPreviousRows={query.isPlaceholderData}
            onRetry={() => void query.refetch()}
            stickyHeader
            showPagination={false}
            skeletonWidths={['w-48', 'w-12', 'w-20']}
            containerClassName={cn(
              // The table fills the measured height, so it does not need the footer stand-in padding.
              'pb-0',
              fullscreen.isFullscreen
                ? '*:data-[slot=table-container]:h-[calc(100vh-6.5rem)] *:data-[slot=table-container]:max-h-[calc(100vh-6.5rem)]'
                : '*:data-[slot=table-container]:max-h-[var(--table-available-height)]'
            )}
          />
        </div>
      </ListPageCard>

      <MenuFormDrawer
        menuId={formTarget?.menuId ?? null}
        defaultParentId={formTarget?.parentId ?? null}
        open={formTarget !== null}
        onClose={() => setFormTarget(null)}
        menuTree={rawTree}
      />

      <MenuDeleteDialog menu={deletingMenu} open={deletingMenu !== null} onClose={() => setDeletingMenu(null)} />
    </>
  )
}
