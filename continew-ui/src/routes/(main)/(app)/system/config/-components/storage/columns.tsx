import type { ReactNode } from 'react'

import { CircleCheck, CircleSlash, PenLine, Star, Trash2 } from 'lucide-react'

import { RowActionsMenu } from '@/components/row-actions-menu'
import { StatusCell } from '@/components/status-cell'
import { DropdownMenuGroup, DropdownMenuItem, DropdownMenuSeparator } from '@/components/ui/dropdown-menu'
import { usePermission } from '@/hooks/use-permission'
import type { StorageResp } from '@/lib/api/storage'

import { MonoCell, RecycleBinCell, StorageNameCell } from './display'

/** The row actions a storage row raises. */
export type StorageAction = 'edit' | 'setDefault' | 'toggleStatus' | 'delete'

/**
 * One column of a storage group table.
 *
 * The group panels render plain tables, the way the infrastructure screen does, so a column only
 * needs a header, a width for its `colgroup` entry, and a cell renderer.
 */
export interface StorageColumn {
  readonly id: string
  readonly header: string
  /** `colgroup` width; the tables use a fixed layout so long paths truncate instead of stretching. */
  readonly width: string
  readonly headerClassName?: string
  readonly cell: (storage: StorageResp) => ReactNode
}

export interface StorageColumnOptions {
  readonly onAction: (action: StorageAction, storage: StorageResp) => void
}

/** Sum of the local column widths, so the fixed layout keeps them instead of squeezing. */
export const LOCAL_TABLE_MIN_WIDTH = 'min-w-[1200px]'

/** Sum of the object-storage column widths. */
export const OSS_TABLE_MIN_WIDTH = 'min-w-[1584px]'

const NAME_COLUMN: StorageColumn = {
  id: 'name',
  header: '名称',
  width: 'w-64',
  cell: storage => <StorageNameCell storage={storage} />
}

const STATUS_COLUMN: StorageColumn = {
  id: 'status',
  header: '状态',
  width: 'w-28',
  cell: storage => <StatusCell status={storage.status} />
}

const RECYCLE_BIN_COLUMN: StorageColumn = {
  id: 'recycleBin',
  header: '回收站',
  width: 'w-28',
  cell: storage => <RecycleBinCell enabled={storage.recycleBinEnabled} />
}

function RowActions({
  storage,
  onAction
}: {
  storage: StorageResp
  onAction: (action: StorageAction, storage: StorageResp) => void
}) {
  const { hasPermission } = usePermission()
  // The backend refuses to disable or delete the default storage, and only an enabled one can
  // become the default.
  const isDefault = Boolean(storage.isDefault)
  const isEnabled = storage.status === 1
  const canPromote = hasPermission('system:storage:setDefault') && !isDefault
  const canEdit = hasPermission('system:storage:update')
  const canToggleStatus = hasPermission('system:storage:updateStatus') && !isDefault
  const canRemove = hasPermission('system:storage:delete') && !isDefault
  const hasRowGroup = canPromote || canEdit

  return (
    <div className="flex items-center justify-end">
      <RowActionsMenu>
        {hasRowGroup && (
          <DropdownMenuGroup>
            {canPromote && (
              <DropdownMenuItem disabled={!isEnabled} onClick={() => onAction('setDefault', storage)}>
                <Star />
                设为默认
              </DropdownMenuItem>
            )}
            {canEdit && (
              <DropdownMenuItem onClick={() => onAction('edit', storage)}>
                <PenLine />
                修改
              </DropdownMenuItem>
            )}
          </DropdownMenuGroup>
        )}

        {canToggleStatus && (
          <>
            {hasRowGroup && <DropdownMenuSeparator />}
            <DropdownMenuGroup>
              <DropdownMenuItem onClick={() => onAction('toggleStatus', storage)}>
                {isEnabled ? <CircleSlash /> : <CircleCheck />}
                {isEnabled ? '禁用' : '启用'}
              </DropdownMenuItem>
            </DropdownMenuGroup>
          </>
        )}

        {canRemove && (
          <>
            {(hasRowGroup || canToggleStatus) && <DropdownMenuSeparator />}
            <DropdownMenuGroup>
              <DropdownMenuItem variant="destructive" onClick={() => onAction('delete', storage)}>
                <Trash2 />
                删除
              </DropdownMenuItem>
            </DropdownMenuGroup>
          </>
        )}
      </RowActionsMenu>
    </div>
  )
}

function actionsColumn(onAction: (action: StorageAction, storage: StorageResp) => void): StorageColumn {
  return {
    id: 'actions',
    header: '操作',
    width: 'w-20',
    headerClassName: 'text-right',
    cell: storage => <RowActions storage={storage} onAction={onAction} />
  }
}

/** 本地存储: the two paths the Vue client's `StorageLocal.vue` shows, plus the recycle bin. */
export function createLocalStorageColumns(options: StorageColumnOptions): StorageColumn[] {
  return [
    NAME_COLUMN,
    STATUS_COLUMN,
    {
      id: 'bucketName',
      header: '存储路径',
      width: 'w-80',
      cell: storage => <MonoCell value={storage.bucketName} />
    },
    {
      id: 'domain',
      header: '访问路径',
      width: 'w-80',
      cell: storage => <MonoCell value={storage.domain} />
    },
    RECYCLE_BIN_COLUMN,
    actionsColumn(options.onAction)
  ]
}

/** 对象存储: the credentials and addresses the Vue client's `StorageOss.vue` shows. */
export function createOssStorageColumns(options: StorageColumnOptions): StorageColumn[] {
  return [
    NAME_COLUMN,
    STATUS_COLUMN,
    {
      id: 'accessKey',
      header: 'Access Key',
      width: 'w-56',
      cell: storage => <MonoCell value={storage.accessKey} />
    },
    {
      id: 'endpoint',
      header: 'Endpoint',
      width: 'w-72',
      cell: storage => <MonoCell value={storage.endpoint} />
    },
    {
      id: 'bucketName',
      header: 'Bucket',
      width: 'w-56',
      cell: storage => <MonoCell value={storage.bucketName} />
    },
    {
      id: 'domain',
      header: '自定义域名',
      width: 'w-72',
      cell: storage => <MonoCell value={storage.domain} />
    },
    RECYCLE_BIN_COLUMN,
    actionsColumn(options.onAction)
  ]
}
