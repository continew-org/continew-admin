import { Card, CardAction, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import type { FileResp } from '@/lib/api/file'

import { FileActions, type FileActionHandlers } from './file-actions'
import { FileTypeIcon, fileTypeLabel, formatFileSize, isImageFile } from './file-display'

interface FileGridProps {
  readonly files: FileResp[]
  readonly canDownload: boolean
  readonly canUpdate: boolean
  readonly canDelete: boolean
  readonly handlers: FileActionHandlers
}

/** The card grid of the Vue page's grid mode, with thumbnails for images and type icons for the rest. */
export function FileGrid({ files, canDownload, canUpdate, canDelete, handlers }: FileGridProps) {
  return (
    <div className="grid gap-3 px-4 sm:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-4">
      {files.map(file => {
        return (
          <Card key={String(file.id)} size="sm" className="gap-0 overflow-hidden">
            <CardContent className="p-0">
              <button
                type="button"
                className="flex h-36 w-full items-center justify-center overflow-hidden bg-muted/30 p-2"
                onClick={() => handlers.onActivate(file)}
              >
                {isImageFile(file.type) && file.url ? (
                  <img src={file.url} alt={file.originalName} loading="lazy" className="h-full w-full object-contain" />
                ) : (
                  <FileTypeIcon type={file.type} className="size-10 text-muted-foreground" />
                )}
              </button>
            </CardContent>
            <CardHeader className="gap-1 p-3">
              <CardTitle className="truncate text-sm">
                <button
                  type="button"
                  className="max-w-full truncate text-left hover:underline"
                  onClick={() => (file.type === 0 ? handlers.onOpen(file) : handlers.onDetail(file))}
                >
                  {file.originalName}
                </button>
              </CardTitle>
              <CardDescription className="truncate text-xs">
                {file.type === 0 ? '文件夹' : `${fileTypeLabel(file.type)} · ${formatFileSize(file.size)}`}
                {file.updateTime ? ` · ${file.updateTime}` : ''}
              </CardDescription>
              <CardAction>
                <FileActions
                  file={file}
                  canDownload={canDownload}
                  canUpdate={canUpdate}
                  canDelete={canDelete}
                  handlers={handlers}
                />
              </CardAction>
            </CardHeader>
          </Card>
        )
      })}
    </div>
  )
}
