import { Download } from 'lucide-react'

import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle
} from '@/components/ui/dialog'
import { useLastRecord } from '@/hooks/use-last-record'
import type { FileResp } from '@/lib/api/file'

import { formatFileSize } from './file-display'

interface FilePreviewDialogProps {
  readonly file: FileResp | null
  readonly open: boolean
  readonly canDownload: boolean
  readonly onClose: () => void
  readonly onDownload: (file: FileResp) => void
}

/** Full-size image preview; non-image files open through their URL instead. */
export function FilePreviewDialog({ file, open, canDownload, onClose, onDownload }: FilePreviewDialogProps) {
  const shownFile = useLastRecord(file)

  return (
    <Dialog
      open={open}
      onOpenChange={nextOpen => {
        if (!nextOpen) {
          onClose()
        }
      }}
    >
      <DialogContent className="sm:max-w-3xl">
        <DialogHeader>
          <DialogTitle className="truncate">{shownFile?.originalName ?? '图片预览'}</DialogTitle>
          <DialogDescription>{shownFile ? formatFileSize(shownFile.size) : null}</DialogDescription>
        </DialogHeader>

        {shownFile ? (
          <div className="flex items-center justify-center overflow-hidden rounded-lg border bg-muted/40 p-2">
            <img
              src={shownFile.url}
              alt={shownFile.originalName}
              className="max-h-[70vh] w-auto max-w-full object-contain"
            />
          </div>
        ) : null}

        <DialogFooter>
          <Button type="button" variant="outline" onClick={onClose}>
            关闭
          </Button>
          {shownFile && canDownload ? (
            <Button type="button" onClick={() => onDownload(shownFile)}>
              <Download />
              下载
            </Button>
          ) : null}
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
