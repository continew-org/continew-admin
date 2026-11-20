import * as React from 'react'

import { type ColumnVisibilityState, type PaginationState } from '@tanstack/react-table'

import { Plus } from 'lucide-react'

import { DataTable } from '@/components/data-table'
import { FilterSelect } from '@/components/filter-select'
import { ListPageCard } from '@/components/list-page-card'
import { TableToolbar } from '@/components/table-toolbar'
import { Button } from '@/components/ui/button'
import { useFullscreenTransition } from '@/hooks/use-fullscreen-transition'
import { useListTable } from '@/hooks/use-list-table'
import { usePermission } from '@/hooks/use-permission'
import { listClient, type ClientResp } from '@/lib/api/client'
import type { PageResp } from '@/lib/api/types'
import { clientKeys } from '@/lib/query-keys'

import { type DictOption, toLabelMap, useDictOptions } from '../use-dict-options'
import {
  AUTH_TYPE_OPTIONS,
  createClientColumns,
  LOGOUT_MODE_PRESETS,
  REPLACED_RANGE_PRESETS,
  type ClientAction
} from './columns'
import { ClientDeleteDialog } from './delete-dialog'
import { ClientDetailDrawer } from './detail-drawer'
import { ClientFormDrawer } from './form-drawer'

const CLIENT_TYPE_PRESETS: DictOption[] = [
  { label: 'PC网页端', value: 'PC' },
  { label: '移动App端', value: 'APP' },
  { label: '微信小程序', value: 'MINI' },
  { label: '三方Web服务', value: 'WEB' }
]

const HIDDEN_BY_DEFAULT: ColumnVisibilityState = {
  refreshTokenTimeout: false,
  refreshTokenMode: false,
  replacedRange: false,
  maxLoginCount: false,
  overflowLogoutMode: false,
  createUserString: false,
  updateUserString: false,
  updateTime: false
}

const STATUS_OPTIONS = [
  { value: 'all', label: '全部' },
  { value: '1', label: '启用' },
  { value: '2', label: '禁用' }
]

export function ConfigClient() {
  const { hasPermission } = usePermission()
  const fullscreen = useFullscreenTransition()

  const [clientType, setClientType] = React.useState<string | undefined>(undefined)
  const [status, setStatus] = React.useState<number | undefined>(undefined)

  // One state per overlay: a null value means it is closed. The form drawer holds an object, because
  // a null id there means 新增.
  const [formTarget, setFormTarget] = React.useState<{ clientId: string | number | null } | null>(null)
  const [detailTarget, setDetailTarget] = React.useState<ClientResp | null>(null)
  const [deleteTarget, setDeleteTarget] = React.useState<ClientResp | null>(null)

  const clientTypeOptions = useDictOptions('client_type', CLIENT_TYPE_PRESETS)
  const authTypeOptions = useDictOptions('auth_type_enum', AUTH_TYPE_OPTIONS)
  const replacedRangeOptions = useDictOptions('replaced_range_enum', REPLACED_RANGE_PRESETS)
  const logoutModeOptions = useDictOptions('logout_mode_enum', LOGOUT_MODE_PRESETS)

  const clientTypeMap = React.useMemo(() => toLabelMap(clientTypeOptions), [clientTypeOptions])
  const authTypeMap = React.useMemo(() => toLabelMap(authTypeOptions), [authTypeOptions])
  const replacedRangeMap = React.useMemo(() => toLabelMap(replacedRangeOptions), [replacedRangeOptions])
  const logoutModeMap = React.useMemo(() => toLabelMap(logoutModeOptions), [logoutModeOptions])

  const handleRowAction = React.useCallback((action: ClientAction, record: ClientResp) => {
    if (action === 'detail') {
      setDetailTarget(record)
      return
    }

    if (action === 'edit') {
      setFormTarget({ clientId: record.id })
      return
    }

    setDeleteTarget(record)
  }, [])

  const buildQuery = React.useCallback(
    (pagination: PaginationState) => {
      const params = { page: pagination.pageIndex + 1, size: pagination.pageSize, clientType, status }
      return { queryKey: clientKeys.list(params), queryFn: (signal: AbortSignal) => listClient(params, signal) }
    },
    [clientType, status]
  )

  const buildColumns = React.useCallback(
    (rowOffset: number) =>
      createClientColumns(
        { clientTypeMap, authTypeMap, replacedRangeMap, logoutModeMap, onAction: handleRowAction },
        rowOffset
      ),
    [clientTypeMap, authTypeMap, replacedRangeMap, logoutModeMap, handleRowAction]
  )

  const { query, table, resetPage, resetColumns } = useListTable<ClientResp, PageResp<ClientResp>>({
    buildQuery,
    buildColumns,
    getRowId: row => String(row.id),
    initialColumnVisibility: HIDDEN_BY_DEFAULT
  })

  const filters = (
    <>
      <FilterSelect
        label="客户端类型"
        options={[{ value: 'all', label: '全部' }, ...clientTypeOptions]}
        value={clientType ?? 'all'}
        onValueChange={value => {
          setClientType(!value || value === 'all' ? undefined : value)
          resetPage()
        }}
        className="w-full sm:w-40"
      />
      <FilterSelect
        label="状态"
        options={STATUS_OPTIONS}
        value={status === undefined ? 'all' : String(status)}
        onValueChange={value => {
          setStatus(!value || value === 'all' ? undefined : Number(value))
          resetPage()
        }}
        className="w-full sm:w-32"
      />
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
        hasPermission('system:client:create') && (
          <Button className="w-full sm:w-auto" onClick={() => setFormTarget({ clientId: null })}>
            <Plus />
            新增客户端
          </Button>
        )
      }
    />
  )

  return (
    <>
      <ListPageCard
        title="客户端配置"
        description="管理客户端凭据、令牌策略与登录限制"
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
          skeletonWidths={[
            'mx-auto w-6',
            'w-24',
            'w-20',
            'w-24',
            'mx-auto w-12 rounded-full',
            'w-24',
            'w-24',
            'w-24',
            'mx-auto w-10'
          ]}
        />
      </ListPageCard>

      <ClientFormDrawer
        clientId={formTarget?.clientId ?? null}
        open={formTarget !== null}
        clientTypeOptions={clientTypeOptions}
        authTypeOptions={authTypeOptions}
        replacedRangeOptions={replacedRangeOptions}
        logoutModeOptions={logoutModeOptions}
        onClose={() => setFormTarget(null)}
      />
      <ClientDetailDrawer
        client={detailTarget}
        open={detailTarget !== null}
        clientTypeMap={clientTypeMap}
        authTypeMap={authTypeMap}
        replacedRangeMap={replacedRangeMap}
        logoutModeMap={logoutModeMap}
        onClose={() => setDetailTarget(null)}
      />
      <ClientDeleteDialog client={deleteTarget} open={deleteTarget !== null} onClose={() => setDeleteTarget(null)} />
    </>
  )
}
