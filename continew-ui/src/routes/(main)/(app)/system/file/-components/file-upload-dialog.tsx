import * as React from 'react'

import { useQueryClient } from '@tanstack/react-query'
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
import { Progress } from '@/components/ui/progress'
import { Spinner } from '@/components/ui/spinner'
import { toast } from '@/components/ui/toast'
import { getDefaultUploadConfig } from '@/lib/api/file'
import { classifyUploadFailure, uploadManagedFile, type ManagedUploadProgress } from '@/lib/file-upload'
import { storageKeys } from '@/lib/query-keys'

import { formatFileSize } from './file-display'

interface FileUploadDialogProps {
  readonly open: boolean
  readonly parentPath: string
  readonly onClose: () => void
  /** Called after all selected files uploaded, so the list and the statistics can refresh. */
  readonly onUploaded: () => void
}

/** 文件小于默认存储分片阈值走普通上传，达到阈值走 init/part/complete 分片流程。 */
export function FileUploadDialog({ open, parentPath, onClose, onUploaded }: FileUploadDialogProps) {
  const queryClient = useQueryClient()
  const inputRef = React.useRef<HTMLInputElement>(null)
  const abortRef = React.useRef<AbortController | null>(null)
  const lastPhaseRef = React.useRef<ManagedUploadProgress['phase'] | null>(null)
  const [files, setFiles] = React.useState<File[]>([])
  const [isUploading, setIsUploading] = React.useState(false)
  const [error, setError] = React.useState<string | null>(null)
  const [progress, setProgress] = React.useState<ManagedUploadProgress | null>(null)
  const [currentName, setCurrentName] = React.useState('')

  React.useEffect(() => {
    if (!open) {
      return
    }

    setFiles([])
    setError(null)
    setProgress(null)
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

    const controller = new AbortController()
    abortRef.current = controller
    setIsUploading(true)
    setError(null)
    setProgress(null)

    // Keep the files that did not upload so a retry does not upload the successful ones twice.
    let remaining = [...files]
    let uploaded = 0

    try {
      // 生效阈值/分片大小来自默认存储配置，整批共用一次查询结果；
      // 取消须立即中断等待：Query 的 signal 跟随内部请求且取消粒度是整个 key，
      // 故仅 race 当前批次的 signal，不影响同 key 的其他订阅者
      const config = await Promise.race([
        queryClient.fetchQuery({
          queryKey: storageKeys.uploadConfig(),
          queryFn: ({ signal }) => getDefaultUploadConfig(signal),
          staleTime: 60_000
        }),
        // lib 目标 ES2022 无 Promise.withResolvers；promise 须由外部 abort 事件兑现，保留 executor 形式
        new Promise<never>((_, reject) => {
          controller.signal.addEventListener('abort', () => reject(controller.signal.reason), { once: true })
        })
      ])
      controller.signal.throwIfAborted()

      while (remaining.length > 0) {
        const next = remaining[0]
        setCurrentName(next.name)
        await uploadManagedFile({
          file: next,
          parentPath,
          config,
          signal: controller.signal,
          onProgress: p => {
            lastPhaseRef.current = p.phase
            setProgress(p)
          }
        })
        lastPhaseRef.current = null
        remaining = remaining.slice(1)
        uploaded += 1
        setFiles(remaining)
        setProgress(null)
      }

      toast.add({ title: `已上传 ${uploaded} 个文件`, type: 'success' })
      onUploaded()
      onClose()
    } catch (reason) {
      const { uncertain, aborted } = classifyUploadFailure(lastPhaseRef.current, controller.signal.aborted, reason)
      const failedName = remaining[0]?.name ?? currentName
      // 结果未确认的文件不能留在重试队列：合并已在途的同 uploadId 再 complete 可能写入空对象，
      // 未知结果可能其实已经保存；从队列剔除，交由用户核对后自行重选
      const retryable = uncertain ? remaining.slice(1) : remaining
      setFiles(retryable)
      // 已完成文件与任何未确认结果都必须刷新出来，不能靠用户手动刷新核对
      if (uploaded > 0 || uncertain) {
        onUploaded()
      }
      if (uncertain) {
        setError(
          `已上传 ${uploaded} 个文件；「${failedName}」结果未确认，可能已保存，请刷新核对；其余 ${retryable.length} 个未上传，可重新选择`
        )
      } else if (aborted) {
        setError(uploaded > 0 ? `已上传 ${uploaded} 个文件，其余已取消` : '上传已取消，可重新发起')
      } else if (uploaded > 0) {
        setError(`已上传 ${uploaded} 个文件，${retryable.length} 个未完成，请重试`)
      } else {
        setError('上传失败，请检查文件大小或类型后重试')
      }
    } finally {
      abortRef.current = null
      lastPhaseRef.current = null
      setIsUploading(false)
      setProgress(null)
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
          {isUploading && progress ? (
            <div className="flex flex-col gap-1.5">
              <div className="flex items-center justify-between gap-2 text-xs text-muted-foreground">
                <span className="truncate">
                  {progress.phase === 'hashing'
                    ? `正在计算 ${currentName} 的摘要`
                    : progress.phase === 'completing'
                      ? `正在合并 ${currentName}`
                      : currentName}
                </span>
                {progress.phase === 'uploading' && progress.totalBytes > 0 ? (
                  <span className="shrink-0">{Math.floor((progress.uploadedBytes / progress.totalBytes) * 100)}%</span>
                ) : null}
              </div>
              <Progress
                aria-label={`${currentName} 上传进度`}
                value={
                  progress.phase === 'uploading' && progress.totalBytes > 0
                    ? (progress.uploadedBytes / progress.totalBytes) * 100
                    : null
                }
              />
            </div>
          ) : null}

          {error ? <p className="text-xs text-destructive">{error}</p> : null}
        </div>

        <DialogFooter>
          <Button
            type="button"
            variant="outline"
            disabled={isUploading && progress?.phase === 'completing'}
            onClick={() => (isUploading ? abortRef.current?.abort() : onClose())}
          >
            {isUploading ? '取消上传' : '取消'}
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
