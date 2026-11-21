import { Download, ExternalLink, Eye, FolderOpen, Info, Pencil, Trash2 } from 'lucide-react'

import { RowActionsMenu } from '@/components/row-actions-menu'
import { DropdownMenuGroup, DropdownMenuItem, DropdownMenuSeparator } from '@/components/ui/dropdown-menu'
import type { FileResp } from '@/lib/api/file'

import { isImageFile } from './file-display'

export interface FileActionHandlers {
  readonly onActivate: (file: FileResp) => void
  readonly onOpen: (file: FileResp) => void
  readonly onPreview: (file: FileResp) => void
  readonly onDownload: (file: FileResp) => void
  readonly onDetail: (file: FileResp) => void
  readonly onRename: (file: FileResp) => void
  readonly onDelete: (file: FileResp) => void
}

interface FileActionsProps {
  readonly file: FileResp
  readonly canDownload: boolean
  readonly canUpdate: boolean
  readonly canDelete: boolean
  readonly handlers: FileActionHandlers
}

/** The per-file menu shared by the grid and the list view. */
export function FileActions({ file, canDownload, canUpdate, canDelete, handlers }: FileActionsProps) {
  const isDir = file.type === 0

  return (
    <div className="flex items-center justify-end">
      <RowActionsMenu>
        <DropdownMenuGroup>
          <DropdownMenuItem
            onClick={() => (isDir || !isImageFile(file.type) ? handlers.onOpen(file) : handlers.onPreview(file))}
          >
            {isDir ? <FolderOpen /> : isImageFile(file.type) ? <Eye /> : <ExternalLink />}
            {isDir ? '打开' : isImageFile(file.type) ? '预览' : '打开'}
          </DropdownMenuItem>
          <DropdownMenuItem onClick={() => handlers.onDetail(file)}>
            <Info />
            详情
          </DropdownMenuItem>
          {!isDir && canDownload ? (
            <DropdownMenuItem onClick={() => handlers.onDownload(file)}>
              <Download />
              下载
            </DropdownMenuItem>
          ) : null}
          {canUpdate ? (
            <DropdownMenuItem onClick={() => handlers.onRename(file)}>
              <Pencil />
              重命名
            </DropdownMenuItem>
          ) : null}
        </DropdownMenuGroup>
        {canDelete ? (
          <>
            <DropdownMenuSeparator />
            <DropdownMenuGroup>
              <DropdownMenuItem variant="destructive" onClick={() => handlers.onDelete(file)}>
                <Trash2 />
                删除
              </DropdownMenuItem>
            </DropdownMenuGroup>
          </>
        ) : null}
      </RowActionsMenu>
    </div>
  )
}
