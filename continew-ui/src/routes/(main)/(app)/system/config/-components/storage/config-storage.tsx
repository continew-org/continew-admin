import * as React from 'react'

import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'

import { Cloud, HardDrive, RefreshCw } from 'lucide-react'

import { FilterSelect } from '@/components/filter-select'
import { ListPageCard } from '@/components/list-page-card'
import { TableSearch } from '@/components/table-search'
import { Button } from '@/components/ui/button'
import { toast } from '@/components/ui/toast'
import { useFullscreenTransition } from '@/hooks/use-fullscreen-transition'
import { usePermission } from '@/hooks/use-permission'
import {
  listStorage,
  setDefaultStorage,
  type StorageResp,
  type StorageStatus,
  type StorageType,
  updateStorageStatus
} from '@/lib/api/storage'
import { storageKeys } from '@/lib/query-keys'

import {
  createLocalStorageColumns,
  createOssStorageColumns,
  LOCAL_TABLE_MIN_WIDTH,
  OSS_TABLE_MIN_WIDTH,
  type StorageAction
} from './columns'
import { StorageDeleteDialog } from './delete-dialog'
import { StorageFormDrawer } from './form-drawer'
import { StorageGroupPanel } from './group-panel'
import { StorageToolbar } from './toolbar'

/** The list endpoint is unpaged, so the backend orders it by the storage's own 排序 column. */
const DEFAULT_SORT = ['sort,asc']

/** Stable empty array, so a failed query does not rebuild the group rows on every render. */
const EMPTY_STORAGES: StorageResp[] = []

const TYPE_OPTIONS = [
  { value: 'all', label: '全部' },
  { value: '1', label: '本地存储' },
  { value: '2', label: '对象存储' }
]

const STATUS_OPTIONS = [
  { value: 'all', label: '全部' },
  { value: '1', label: '启用' },
  { value: '2', label: '禁用' }
]

interface StorageFormTarget {
  readonly type: StorageType
  /** `null` creates a storage of that type; an id edits it. */
  readonly storageId: string | number | null
}

export function ConfigStorage() {
  const queryClient = useQueryClient()
  const { hasPermission } = usePermission()
  const fullscreen = useFullscreenTransition()

  const [searchInput, setSearchInput] = React.useState('')
  const [keyword, setKeyword] = React.useState('')
  const [type, setType] = React.useState<StorageType | undefined>(undefined)
  const [status, setStatus] = React.useState<StorageStatus | undefined>(undefined)

  // One state per overlay: a null value means it is closed, and a null id inside the form target
  // means 新增 (of that type).
  const [formTarget, setFormTarget] = React.useState<StorageFormTarget | null>(null)
  const [deleteTarget, setDeleteTarget] = React.useState<StorageResp | null>(null)

  const query = useQuery({
    queryKey: storageKeys.list({ description: keyword || undefined, type, status, sort: DEFAULT_SORT }),
    queryFn: ({ signal }) =>
      listStorage({ description: keyword || undefined, type, status, sort: DEFAULT_SORT }, signal),
    placeholderData: keepPreviousData
  })

  const storages = query.data ?? EMPTY_STORAGES
  const localStorages = React.useMemo(() => storages.filter(storage => storage.type === 1), [storages])
  const ossStorages = React.useMemo(() => storages.filter(storage => storage.type === 2), [storages])

  const setDefaultMutation = useMutation({
    mutationFn: (id: string | number) => setDefaultStorage(id),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: storageKeys.all })
      toast.add({ title: '设置默认存储成功', type: 'success' })
    }
  })

  const statusMutation = useMutation({
    mutationFn: ({ id, nextStatus }: { id: string | number; nextStatus: StorageStatus }) =>
      updateStorageStatus(id, nextStatus),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: storageKeys.all })
      toast.add({ title: '状态更新成功', type: 'success' })
    }
  })

  const handleRowAction = React.useCallback(
    (action: StorageAction, storage: StorageResp) => {
      if (action === 'edit') {
        setFormTarget({ type: storage.type, storageId: storage.id })
        return
      }

      if (action === 'setDefault') {
        setDefaultMutation.mutate(storage.id)
        return
      }

      if (action === 'toggleStatus') {
        statusMutation.mutate({ id: storage.id, nextStatus: storage.status === 1 ? 2 : 1 })
        return
      }

      setDeleteTarget(storage)
    },
    [setDefaultMutation, statusMutation]
  )

  const localColumns = React.useMemo(() => createLocalStorageColumns({ onAction: handleRowAction }), [handleRowAction])
  const ossColumns = React.useMemo(() => createOssStorageColumns({ onAction: handleRowAction }), [handleRowAction])

  const hasActiveFilters = keyword !== '' || type !== undefined || status !== undefined

  const handleResetFilters = () => {
    setSearchInput('')
    setKeyword('')
    setType(undefined)
    setStatus(undefined)
  }

  const filters = (
    <>
      <TableSearch
        className="col-span-2 h-8 w-full sm:w-64"
        value={searchInput}
        onValueChange={setSearchInput}
        onSearch={setKeyword}
        placeholder="搜索名称、编码、描述"
      />
      <FilterSelect
        label="类型"
        options={TYPE_OPTIONS}
        value={type === undefined ? 'all' : String(type)}
        onValueChange={value => setType(!value || value === 'all' ? undefined : (Number(value) as StorageType))}
        className="w-full sm:w-32"
      />
      <FilterSelect
        label="状态"
        options={STATUS_OPTIONS}
        value={status === undefined ? 'all' : String(status)}
        onValueChange={value => setStatus(!value || value === 'all' ? undefined : (Number(value) as StorageStatus))}
        className="w-full sm:w-32"
      />
      {hasActiveFilters && (
        <Button type="button" variant="ghost" onClick={handleResetFilters}>
          <RefreshCw />
          重置
        </Button>
      )}
    </>
  )

  const actions = (
    <StorageToolbar
      canCreate={hasPermission('system:storage:create')}
      isRefreshing={query.isFetching && !query.isPending}
      isFullscreen={fullscreen.isFullscreen}
      onCreate={type => setFormTarget({ type, storageId: null })}
      onRefresh={() => void query.refetch()}
      onToggleFullscreen={fullscreen.toggleFullscreen}
    />
  )

  return (
    <>
      <ListPageCard
        title="存储配置"
        description="维护本地与对象存储，文件上传与下载都经由默认存储"
        filters={filters}
        actions={actions}
        fullscreen={fullscreen}
      >
        <div className="flex flex-col">
          {type !== 2 && (
            <StorageGroupPanel
              title="本地存储"
              icon={<HardDrive className="size-4 text-muted-foreground" />}
              minWidth={LOCAL_TABLE_MIN_WIDTH}
              items={localStorages}
              columns={localColumns}
              isLoading={query.isPending}
            />
          )}

          {type !== 1 && (
            <StorageGroupPanel
              title="对象存储"
              icon={<Cloud className="size-4 text-muted-foreground" />}
              minWidth={OSS_TABLE_MIN_WIDTH}
              items={ossStorages}
              columns={ossColumns}
              isLoading={query.isPending}
            />
          )}
        </div>
      </ListPageCard>

      <StorageFormDrawer
        storageId={formTarget?.storageId ?? null}
        storageType={formTarget?.type ?? 1}
        open={formTarget !== null}
        onClose={() => setFormTarget(null)}
      />
      <StorageDeleteDialog storage={deleteTarget} open={deleteTarget !== null} onClose={() => setDeleteTarget(null)} />
    </>
  )
}
