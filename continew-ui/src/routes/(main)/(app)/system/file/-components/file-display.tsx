import { File, FileAudio, FileImage, FileText, FileVideo, Folder, LayoutGrid, type LucideIcon } from 'lucide-react'

import { FILE_TYPE_LABELS, type FileFilterType, type FileType } from '@/lib/api/file'

export interface FileTypeFilter {
  readonly value: 'all' | FileFilterType
  readonly label: string
  readonly icon: LucideIcon
}

/** The sidebar order of the Vue client: 全部、图片、文档、视频、音频、其他。 */
export const FILE_TYPE_FILTERS: FileTypeFilter[] = [
  { value: 'all', label: '全部', icon: LayoutGrid },
  { value: 2, label: '图片', icon: FileImage },
  { value: 3, label: '文档', icon: FileText },
  { value: 4, label: '视频', icon: FileVideo },
  { value: 5, label: '音频', icon: FileAudio },
  { value: 1, label: '其他', icon: File }
]

export function FileTypeIcon({ type, className }: { type?: FileType | null; className?: string }) {
  switch (type) {
    case 0:
      return <Folder className={className} />
    case 2:
      return <FileImage className={className} />
    case 3:
      return <FileText className={className} />
    case 4:
      return <FileVideo className={className} />
    case 5:
      return <FileAudio className={className} />
    default:
      return <File className={className} />
  }
}

export function fileTypeLabel(type?: number | null) {
  return type === null || type === undefined ? '其他' : (FILE_TYPE_LABELS[type as FileType] ?? '其他')
}

export function isImageFile(type?: number | null) {
  return type === 2
}

/** Formats a byte count with a binary unit and at most one decimal for small values. */
export function formatFileSize(bytes?: number | null) {
  if (!bytes || bytes <= 0) {
    return '0 B'
  }

  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  const exponent = Math.min(Math.floor(Math.log(bytes) / Math.log(1024)), units.length - 1)
  const value = bytes / 1024 ** exponent

  return `${exponent === 0 || value >= 10 ? Math.round(value) : value.toFixed(1)} ${units[exponent]}`
}
