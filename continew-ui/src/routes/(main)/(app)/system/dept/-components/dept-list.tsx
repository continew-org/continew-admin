import * as React from 'react'

import { useMutation, useQuery } from '@tanstack/react-query'
import { type ColumnVisibilityState, type ExpandedState, useTable } from '@tanstack/react-table'

import { ChevronsDownUp, ChevronsUpDown, Download, Plus, Search, X } from 'lucide-react'

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
import { deptKeys, exportDept, getDeptTree, type DeptResp } from '@/lib/api/dept'
import { dataTableFeatures } from '@/lib/data-table-features'
import { reportError } from '@/lib/http'
import { cn } from 'cn'

import { createDeptColumns, type DeptAction } from './dept-columns'
import { DeptDeleteDialog } from './dept-delete-dialog'
import { DeptFormDrawer } from './dept-form-drawer'

const DEFAULT_COLUMN_VISIBILITY: ColumnVisibilityState = {
  createUserString: false,
  updateUserString: false,
  updateTime: false
}

const EMPTY_TREE: DeptResp[] = []

/** Recursively filters tree nodes by name or description, preserving parent paths. */
function filterDeptTree(nodes: DeptResp[], keyword: string): DeptResp[] {
  const q = keyword.toLowerCase().trim()
  if (!q) return nodes

  function filterNode(node: DeptResp): DeptResp | null {
    const matches = node.name.toLowerCase().includes(q) || Boolean(node.description?.toLowerCase().includes(q))
    const children = (node.children ?? []).flatMap(child => filterNode(child) ?? [])

    return matches || children.length > 0 ? { ...node, children } : null
  }

  return nodes.flatMap(node => filterNode(node) ?? [])
}

interface FormTarget {
  readonly deptId: string | number | null
  readonly parentId: string | number | null
}

export function DeptList() {
  const { hasPermission } = usePermission()

  const hasCreatePermission = hasPermission('system:dept:create')
  const hasExportPermission = hasPermission('system:dept:export')

  const [searchInput, setSearchInput] = React.useState('')
  const keyword = searchInput.trim()

  // Root departments are expanded by default; deeper levels start collapsed.
  const [expanded, setExpanded] = React.useState<ExpandedState>({})
  const [columnVisibility, setColumnVisibility] = React.useState<ColumnVisibilityState>(DEFAULT_COLUMN_VISIBILITY)

  // One state per overlay: a null value means closed, so "which record" and "is it open" cannot drift apart.
  const [formTarget, setFormTarget] = React.useState<FormTarget | null>(null)
  const [deletingDept, setDeletingDept] = React.useState<DeptResp | null>(null)

  const fullscreen = useFullscreenTransition()

  const tableAreaRef = React.useRef<HTMLDivElement>(null)
  const remeasureTableArea = useAvailableHeight(tableAreaRef, '--table-available-height')

  const query = useQuery({
    queryKey: deptKeys.trees(),
    queryFn: ({ signal }) => getDeptTree(undefined, signal)
  })

  const rawTree = query.data ?? EMPTY_TREE

  const filteredTree = React.useMemo(() => filterDeptTree(rawTree, keyword), [rawTree, keyword])

  // Expand only the root departments by default once the tree arrives; deeper levels start collapsed.
  const rootsInitializedRef = React.useRef(false)
  React.useEffect(() => {
    if (rootsInitializedRef.current || !rawTree.length) {
      return
    }

    rootsInitializedRef.current = true
    setExpanded(Object.fromEntries(rawTree.map(node => [String(node.id), true])))
  }, [rawTree])

  React.useEffect(() => {
    if (keyword) {
      setExpanded(true)
    }
  }, [keyword])

  const handleRowAction = React.useCallback((action: DeptAction, dept: DeptResp) => {
    if (action === 'edit') {
      setFormTarget({ deptId: dept.id, parentId: null })
      return
    }

    if (action === 'add-child') {
      setFormTarget({ deptId: null, parentId: dept.id })
      return
    }

    if (action === 'delete') {
      setDeletingDept(dept)
    }
  }, [])

  const columns = React.useMemo(() => createDeptColumns(handleRowAction), [handleRowAction])

  const table = useTable({
    features: dataTableFeatures,
    data: filteredTree,
    columns,
    getSubRows: (row: DeptResp) => row.children,
    // Real IDs, so expanding or filtering keeps row identity and does not remount every row.
    getRowId: row => String(row.id),
    // The tree loads asynchronously; without this the post-load auto reset collapses the default
    // root-expanded state back to the empty default as soon as the data arrives.
    autoResetExpanded: false,
    state: {
      expanded,
      columnVisibility
    },
    manualPagination: true,
    paginateExpandedRows: false,
    onExpandedChange: setExpanded,
    onColumnVisibilityChange: setColumnVisibility
  })

  // Expansion, filtering and loading change the row count in the same commit that paints the new
  // rows. Measure before that paint, otherwise the page overflows for one frame (the observer-driven
  // correction is deferred to the next frame) and the whole shell shifts by a scrollbar width.
  const rowCount = table.getRowModel().rows.length
  React.useLayoutEffect(() => {
    remeasureTableArea()
  }, [remeasureTableArea, rowCount])

  const exportMutation = useMutation({
    mutationFn: () => exportDept({ description: keyword || undefined }),
    onSuccess: () => {
      toast.add({ title: '部门数据导出成功', type: 'success' })
    },
    onError: error => reportError(error, '导出失败，请重试')
  })

  const isAllExpanded = table.getIsAllRowsExpanded()
  const handleToggleExpandAll = () => setExpanded(isAllExpanded ? {} : true)

  const filters = (
    <InputGroup className="w-full sm:w-64">
      <InputGroupAddon>
        <Search className="size-4" />
      </InputGroupAddon>
      <InputGroupInput
        className="text-sm"
        placeholder="搜索部门名称或描述"
        value={searchInput}
        onChange={event => setSearchInput(event.target.value)}
      />
      {searchInput && (
        <InputGroupAddon align="inline-end">
          <Button type="button" variant="ghost" size="icon-xs" onClick={() => setSearchInput('')}>
            <X className="size-3.5" />
          </Button>
        </InputGroupAddon>
      )}
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
            <Button onClick={() => setFormTarget({ deptId: null, parentId: null })}>
              <Plus />
              新增部门
            </Button>
          )}

          {hasExportPermission && (
            <Button
              variant="outline"
              size="icon"
              disabled={exportMutation.isPending}
              onClick={() => exportMutation.mutate()}
            >
              {exportMutation.isPending ? <Spinner /> : <Download />}
            </Button>
          )}

          <Button variant="outline" size="icon" onClick={handleToggleExpandAll}>
            {isAllExpanded ? <ChevronsDownUp /> : <ChevronsUpDown />}
          </Button>
        </>
      }
    />
  )

  return (
    <>
      <ListPageCard
        title="部门管理"
        description="管理系统组织架构及部门层级"
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

      <DeptFormDrawer
        deptId={formTarget?.deptId ?? null}
        defaultParentId={formTarget?.parentId ?? null}
        open={formTarget !== null}
        deptTree={rawTree}
        onClose={() => setFormTarget(null)}
      />

      <DeptDeleteDialog dept={deletingDept} open={deletingDept !== null} onClose={() => setDeletingDept(null)} />
    </>
  )
}
