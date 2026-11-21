import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import type { FileResp } from '@/lib/api/file'

import { FileActions, type FileActionHandlers } from './file-actions'
import { FileTypeIcon, fileTypeLabel, formatFileSize } from './file-display'

interface FileTableProps {
  readonly files: FileResp[]
  readonly canDownload: boolean
  readonly canUpdate: boolean
  readonly canDelete: boolean
  readonly handlers: FileActionHandlers
}

/** The list mode, using the same data and actions as the grid. */
export function FileTable({ files, canDownload, canUpdate, canDelete, handlers }: FileTableProps) {
  return (
    <Table className="**:data-[slot='table-cell']:px-4 **:data-[slot='table-head']:px-4">
      <TableHeader className="[&_tr]:border-t">
        <TableRow>
          <TableHead className="py-3 font-normal">名称</TableHead>
          <TableHead className="hidden py-3 font-normal md:table-cell">类型</TableHead>
          <TableHead className="hidden py-3 font-normal sm:table-cell">大小</TableHead>
          <TableHead className="hidden py-3 font-normal lg:table-cell">修改时间</TableHead>
          <TableHead className="w-20 py-3 font-normal">
            <span className="sr-only">操作</span>
          </TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        {files.map(file => (
          <TableRow key={String(file.id)}>
            <TableCell className="py-3">
              <div className="flex min-w-0 items-center gap-2">
                <FileTypeIcon type={file.type} className="size-4 shrink-0 text-muted-foreground" />
                <button
                  type="button"
                  className="max-w-72 truncate text-left text-sm hover:underline"
                  onClick={() => (file.type === 0 ? handlers.onOpen(file) : handlers.onDetail(file))}
                >
                  {file.originalName}
                </button>
              </div>
            </TableCell>
            <TableCell className="hidden py-3 text-sm text-muted-foreground md:table-cell">
              {fileTypeLabel(file.type)}
            </TableCell>
            <TableCell className="hidden py-3 text-sm text-muted-foreground tabular-nums sm:table-cell">
              {file.type === 0 ? '—' : formatFileSize(file.size)}
            </TableCell>
            <TableCell className="hidden py-3 text-sm text-muted-foreground lg:table-cell">
              {file.updateTime || '—'}
            </TableCell>
            <TableCell className="py-3">
              <FileActions
                file={file}
                canDownload={canDownload}
                canUpdate={canUpdate}
                canDelete={canDelete}
                handlers={handlers}
              />
            </TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
  )
}
