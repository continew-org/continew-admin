import * as React from 'react'

import { Upload } from 'lucide-react'

import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle
} from '@/components/ui/dialog'
import { Spinner } from '@/components/ui/spinner'
import { toast } from '@/components/ui/toast'
import { uploadFile } from '@/lib/api/file'

import { formatFileSize } from './file-display'

interface FileUploadDialogProps {
  readonly open: boolean
  readonly parentPath: string
  readonly onClose: () => void
  /** Called after all selected files uploaded, so the list and the statistics can refresh. */
  readonly onUploaded: () => void
}

/** A first-version uploader: one multipart request per file, sequential, with no chunking yet. */
export function FileUploadDialog({ open, parentPath, onClose, onUploaded }: FileUploadDialogProps) {
  const inputRef = React.useRef<HTMLInputElement>(null)
  const [files, setFiles] = React.useState<File[]>([])
  const [isUploading, setIsUploading] = React.useState(false)
  const [error, setError] = React.useState<string | null>(null)

  React.useEffect(() => {
    if (!open) {
      return
    }

    setFiles([])
    setError(null)
    if (inputRef.current) {
      inputRef.current.value = ''
    }
  }, [open])

  const handleFilesChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setFiles(Array.from(event.target.files ?? []))
    setError(null)
  }

  const clearFiles = () => {
    setFiles([])
    setError(null)
    if (inputRef.current) {
      inputRef.current.value = ''
    }
  }

  const upload = async () => {
    if (files.length === 0) {
      return
    }

    setIsUploading(true)
    setError(null)

    // Keep the files that did not upload so a retry does not upload the successful ones twice.
    let remaining = [...files]
    let uploaded = 0

    try {
      while (remaining.length > 0) {
        const next = remaining[0]
        await uploadFile(next, parentPath)
        remaining = remaining.slice(1)
        uploaded += 1
        setFiles(remaining)
      }

      toast.add({ title: `已上传 ${uploaded} 个文件`, type: 'success' })
      onUploaded()
      onClose()
    } catch {
      // The HTTP layer already reported the failure.
      setFiles(remaining)
      if (uploaded > 0) {
        // Files that did reach the server must show up without a manual refresh.
        onUploaded()
        setError(`已上传 ${uploaded} 个文件，${remaining.length} 个未完成，请重试`)
      } else {
        setError('上传失败，请检查文件大小或类型后重试')
      }
    } finally {
      setIsUploading(false)
    }
  }

  return (
    <Dialog
      open={open}
      onOpenChange={nextOpen => {
        if (!nextOpen && !isUploading) {
          onClose()
        }
      }}
    >
      <DialogContent className="sm:max-w-lg">
        <DialogHeader>
          <DialogTitle>上传文件</DialogTitle>
          <DialogDescription>
            上传到 {parentPath === '/' ? '根目录' : parentPath}，支持一次选择多个文件
          </DialogDescription>
        </DialogHeader>

        <div className="flex flex-col gap-3">
          <label className="flex cursor-pointer flex-col items-center justify-center gap-2 rounded-lg border border-dashed p-8 text-center transition-colors hover:bg-muted/40">
            <Upload className="size-6 text-muted-foreground" />
            <span className="text-sm font-medium">点击选择文件</span>
            <span className="text-xs text-muted-foreground">支持多选，上传到当前文件夹</span>
            <input
              ref={inputRef}
              type="file"
              multiple
              className="sr-only"
              disabled={isUploading}
              onChange={handleFilesChange}
            />
          </label>

          {files.length > 0 ? (
            <div className="flex flex-col gap-2 rounded-lg border p-3">
              <div className="flex items-center justify-between gap-2 text-xs text-muted-foreground">
                <span>已选择 {files.length} 个文件</span>
                <Button type="button" variant="ghost" size="sm" disabled={isUploading} onClick={clearFiles}>
                  清空
                </Button>
              </div>
              <ul className="flex max-h-40 flex-col gap-1 overflow-y-auto text-sm">
                {files.map((file, index) => (
                  <li key={`${file.name}-${file.size}-${index}`} className="flex justify-between gap-2">
                    <span className="truncate">{file.name}</span>
                    <span className="shrink-0 text-xs text-muted-foreground">{formatFileSize(file.size)}</span>
                  </li>
                ))}
              </ul>
            </div>
          ) : null}

          {error ? <p className="text-xs text-destructive">{error}</p> : null}
        </div>

        <DialogFooter>
          <Button type="button" variant="outline" disabled={isUploading} onClick={onClose}>
            取消
          </Button>
          <Button type="button" disabled={files.length === 0 || isUploading} onClick={() => void upload()}>
            {isUploading ? (
              <>
                <Spinner />
                上传中
              </>
            ) : (
              '开始上传'
            )}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
