import { DefaultBadge, StatusBadge } from '@/components/status-cell'
import type { StorageResp, StorageType } from '@/lib/api/storage'
import { EMPTY_PLACEHOLDER } from '@/lib/constants'

const TYPE_LABELS: Record<StorageType, string> = {
  1: '本地存储',
  2: '对象存储'
}

export function storageTypeLabel(type: StorageType) {
  return TYPE_LABELS[type] ?? '未知存储'
}

/** What the backend registers for the type, shown under the drawer title. */
export function storageTypeDescription(type: StorageType) {
  return type === 1 ? '文件保存在服务器本地目录' : '文件保存在对象存储，走 S3 兼容协议'
}

/** The name with its code underneath, plus the default marker the row menu keys off. */
export function StorageNameCell({ storage }: { storage: StorageResp }) {
  return (
    <span className="flex min-w-0 flex-col gap-0.5">
      <span className="flex min-w-0 items-center gap-1.5">
        <span className="truncate text-sm font-medium text-foreground" title={storage.name}>
          {storage.name}
        </span>
        {storage.isDefault ? <DefaultBadge /> : null}
      </span>
      <span className="truncate text-muted-foreground" title={storage.code}>
        {storage.code}
      </span>
    </span>
  )
}

/** A path or address, truncated so a long value cannot widen the column. */
export function MonoCell({ value }: { value?: string | null }) {
  if (!value) {
    return <span className="text-sm">{EMPTY_PLACEHOLDER}</span>
  }

  return (
    <span className="block truncate" title={value}>
      {value}
    </span>
  )
}

export function RecycleBinCell({ enabled }: { enabled?: boolean | null }) {
  return <StatusBadge tone={enabled ? 'success' : 'neutral'} label={enabled ? '已启用' : '未启用'} />
}

const BYTES_PER_MEGABYTE = 1024 * 1024

/** The backend stores the multipart settings in bytes; the drawer edits them in MB. */
export function bytesToMegabytes(bytes?: number | null) {
  if (typeof bytes !== 'number' || bytes <= 0) {
    return undefined
  }

  return Number((bytes / BYTES_PER_MEGABYTE).toFixed(2))
}

export function megabytesToBytes(megabytes?: number | null) {
  const value = Number(megabytes)

  if (!Number.isFinite(value) || value <= 0) {
    return null
  }

  return Math.round(value * BYTES_PER_MEGABYTE)
}
