import * as React from 'react'

import { useMutation, useQueryClient } from '@tanstack/react-query'
import { type ColumnVisibilityState, type PaginationState } from '@tanstack/react-table'

import { Plus, RefreshCw } from 'lucide-react'

import { DataTable } from '@/components/data-table'
import { FilterSelect } from '@/components/filter-select'
import { ListPageCard } from '@/components/list-page-card'
import { TableSearch } from '@/components/table-search'
import { TableToolbar } from '@/components/table-toolbar'
import { Button } from '@/components/ui/button'
import { toast } from '@/components/ui/toast'
import { useFullscreenTransition } from '@/hooks/use-fullscreen-transition'
import { useListTable } from '@/hooks/use-list-table'
import { usePermission } from '@/hooks/use-permission'
import { listSmsConfig, setDefaultSmsConfig, type SmsConfigResp } from '@/lib/api/sms'
import type { PageResp } from '@/lib/api/types'
import { reportError } from '@/lib/http'
import { smsKeys } from '@/lib/query-keys'

import { type DictOption, toLabelMap, useDictOptions } from '../use-dict-options'
import { createSmsColumns, type SmsAction } from './columns'
import { SmsDeleteDialog } from './delete-dialog'
import { SmsFormDrawer } from './form-drawer'

const SUPPLIER_PRESETS: DictOption[] = [
  { label: '阿里云', value: 'alibaba' },
  { label: '腾讯云', value: 'tencent' },
  { label: '华为云', value: 'huawei' },
  { label: '容联云', value: 'cloopen' },
  { label: '网易云信', value: 'netease' },
  { label: '亿美软通', value: 'emay' },
  { label: '助通科技', value: 'zhutong' },
  { label: '鼎佑科技', value: 'dingyou' },
  { label: '联麓信息', value: 'lianlu' },
  { label: '螺丝帽', value: 'luosimao' }
]

const HIDDEN_BY_DEFAULT: ColumnVisibilityState = {
  updateTime: false,
  createTime: false
}

export function ConfigSms() {
  const queryClient = useQueryClient()
  const { hasPermission } = usePermission()
  const fullscreen = useFullscreenTransition()

  const [searchInput, setSearchInput] = React.useState('')
  const [keyword, setKeyword] = React.useState('')
  const [supplier, setSupplier] = React.useState<string | undefined>(undefined)

  // One state per overlay: a null value means it is closed. The form drawer holds an object, because
  // a null id there means 新增.
  const [formTarget, setFormTarget] = React.useState<{ smsId: string | number | null } | null>(null)
  const [deleteTarget, setDeleteTarget] = React.useState<SmsConfigResp | null>(null)

  const supplierOptions = useDictOptions('sms_supplier', SUPPLIER_PRESETS)
  const supplierMap = React.useMemo(() => toLabelMap(supplierOptions), [supplierOptions])

  const setDefaultMutation = useMutation({
    mutationFn: (id: string | number) => setDefaultSmsConfig(id),
    onSuccess: () => {
      toast.add({ title: '设置默认成功', type: 'success' })
      void queryClient.invalidateQueries({ queryKey: smsKeys.all })
    },
    onError: err => reportError(err, '设置失败')
  })

  const handleRowAction = React.useCallback(
    (action: SmsAction, record: SmsConfigResp) => {
      if (action === 'edit') {
        setFormTarget({ smsId: record.id })
        return
      }

      if (action === 'setDefault') {
        setDefaultMutation.mutate(record.id)
        return
      }

      setDeleteTarget(record)
    },
    [setDefaultMutation]
  )

  const buildQuery = React.useCallback(
    (pagination: PaginationState) => {
      const params = { page: pagination.pageIndex + 1, size: pagination.pageSize, name: keyword || undefined, supplier }
      return { queryKey: smsKeys.list(params), queryFn: (signal: AbortSignal) => listSmsConfig(params, signal) }
    },
    [keyword, supplier]
  )

  const buildColumns = React.useCallback(
    (rowOffset: number) => createSmsColumns({ supplierMap, onAction: handleRowAction }, rowOffset),
    [supplierMap, handleRowAction]
  )

  const { query, table, resetPage, resetColumns } = useListTable<SmsConfigResp, PageResp<SmsConfigResp>>({
    buildQuery,
    buildColumns,
    getRowId: row => String(row.id),
    initialColumnVisibility: HIDDEN_BY_DEFAULT
  })

  const handleSearch = React.useCallback(
    (next: string) => {
      setKeyword(next)
      resetPage()
    },
    [resetPage]
  )

  const handleResetFilters = () => {
    setSearchInput('')
    setKeyword('')
    setSupplier(undefined)
    resetPage()
  }

  const filters = (
    <>
      <TableSearch
        className="col-span-2 h-8 w-full sm:w-60"
        value={searchInput}
        onValueChange={setSearchInput}
        onSearch={handleSearch}
        placeholder="搜索配置名称"
      />
      <FilterSelect
        label="厂商"
        options={[{ value: 'all', label: '全部' }, ...supplierOptions]}
        value={supplier ?? 'all'}
        onValueChange={value => {
          setSupplier(!value || value === 'all' ? undefined : value)
          resetPage()
        }}
        className="w-full sm:w-32"
      />
      {(keyword !== '' || supplier !== undefined) && (
        <Button type="button" variant="ghost" onClick={handleResetFilters}>
          <RefreshCw />
          重置
        </Button>
      )}
    </>
  )

  const actions = (
    <TableToolbar
      table={table}
      isRefreshing={query.isFetching && !query.isPending}
      isFullscreen={fullscreen.isFullscreen}
      onRefresh={() => void query.refetch()}
      onToggleFullscreen={fullscreen.toggleFullscreen}
      onResetColumns={resetColumns}
      actions={
        hasPermission('system:smsConfig:create') && (
          <Button className="w-full sm:w-auto" onClick={() => setFormTarget({ smsId: null })}>
            <Plus />
            新增配置
          </Button>
        )
      }
    />
  )

  return (
    <>
      <ListPageCard
        title="短信配置"
        description="维护短信服务厂商的接入凭据与默认发送通道"
        filters={filters}
        actions={actions}
        fullscreen={fullscreen}
      >
        <DataTable
          table={table}
          isLoading={query.isPending}
          isError={query.isError}
          isShowingPreviousRows={query.isPlaceholderData}
          onRetry={() => void query.refetch()}
          skeletonWidths={['mx-auto w-6', 'w-24', 'w-16', 'w-28', 'w-20']}
        />
      </ListPageCard>

      <SmsFormDrawer
        smsId={formTarget?.smsId ?? null}
        open={formTarget !== null}
        supplierOptions={supplierOptions}
        onClose={() => setFormTarget(null)}
      />
      <SmsDeleteDialog sms={deleteTarget} open={deleteTarget !== null} onClose={() => setDeleteTarget(null)} />
    </>
  )
}
