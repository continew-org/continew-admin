import * as React from 'react'

import { useQuery, useQueryClient } from '@tanstack/react-query'

import { RefreshCw, RotateCcw, Trash2 } from 'lucide-react'

import { EntityDrawer } from '@/components/entity-drawer'
import { PaginationArrow } from '@/components/pagination-arrow'
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogMedia,
  AlertDialogTitle
} from '@/components/ui/alert-dialog'
import { Button } from '@/components/ui/button'
import { Empty, EmptyDescription, EmptyHeader, EmptyMedia, EmptyTitle } from '@/components/ui/empty'
import { Pagination, PaginationContent, PaginationItem } from '@/components/ui/pagination'
import { Skeleton } from '@/components/ui/skeleton'
import { Spinner } from '@/components/ui/spinner'
import { toast } from '@/components/ui/toast'
import { usePermission } from '@/hooks/use-permission'
import {
  cleanRecycleBin,
  deleteRecycleFile,
  fileKeys,
  listRecycleFiles,
  restoreRecycleFile,
  type FileResp
} from '@/lib/api/file'

import { FileTypeIcon, fileTypeLabel, formatFileSize } from './file-display'

type RecycleAction = { type: 'restore'; file: FileResp } | { type: 'delete'; file: FileResp } | { type: 'clean' }

const RECYCLE_PAGE_SIZE = 10

interface FileRecycleDrawerProps {
  readonly open: boolean
  readonly onClose: () => void
}

/** The recycle bin side panel: list, restore, permanent delete and clean. */
export function FileRecycleDrawer({ open, onClose }: FileRecycleDrawerProps) {
  const queryClient = useQueryClient()
  const { hasPermission } = usePermission()
  const canRestore = hasPermission('system:fileRecycle:restore')
  const canDelete = hasPermission('system:fileRecycle:delete')
  const canClean = hasPermission('system:fileRecycle:clean')

  const [action, setAction] = React.useState<RecycleAction | null>(null)
  const [isSubmitting, setIsSubmitting] = React.useState(false)
  const [page, setPage] = React.useState(1)

  const query = useQuery({
    queryKey: fileKeys.recycle({ page, size: RECYCLE_PAGE_SIZE }),
    queryFn: ({ signal }) => listRecycleFiles({ page, size: RECYCLE_PAGE_SIZE }, signal),
    placeholderData: previous => previous,
    enabled: open
  })

  const items = query.data?.list ?? []
  const total = query.data?.total ?? 0
  const pageCount = Math.max(Math.ceil(total / RECYCLE_PAGE_SIZE), 1)
  const isRestore = action?.type === 'restore'
  const isClean = action?.type === 'clean'

  // Reopening starts from the first page instead of the one left behind last time.
  React.useEffect(() => {
    if (!open) {
      setPage(1)
    }
  }, [open])

  // Restoring, deleting or cleaning can empty the page that is being viewed.
  React.useEffect(() => {
    if (query.isSuccess && page > pageCount) {
      setPage(pageCount)
    }
  }, [page, pageCount, query.isSuccess])

  const handleConfirm = async () => {
    if (!action) {
      return
    }

    setIsSubmitting(true)

    try {
      if (action.type === 'restore') {
        await restoreRecycleFile(action.file.id)
      } else if (action.type === 'delete') {
        await deleteRecycleFile(action.file.id)
      } else {
        await cleanRecycleBin()
      }

      toast.add({
        title: action.type === 'restore' ? '还原成功' : action.type === 'delete' ? '删除成功' : '清空成功',
        type: 'success'
      })
      setAction(null)
      void queryClient.invalidateQueries({ queryKey: fileKeys.all })
    } catch {
      // The HTTP layer already reported the failure.
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <>
      <EntityDrawer open={open} onClose={onClose} title="回收站" icon={<Trash2 className="size-4" />} width={40}>
        <div className="flex items-center justify-between gap-2 border-b px-4 py-2">
          <span className="text-xs text-muted-foreground">共 {total} 项</span>
          <div className="flex items-center gap-2">
            <Button
              type="button"
              size="sm"
              variant="outline"
              disabled={query.isFetching}
              onClick={() => void query.refetch()}
            >
              {query.isFetching ? <Spinner /> : <RefreshCw />}
              刷新
            </Button>
            {canClean ? (
              <Button
                type="button"
                size="sm"
                variant="destructive"
                disabled={total === 0 || query.isPending}
                onClick={() => setAction({ type: 'clean' })}
              >
                <Trash2 />
                清空回收站
              </Button>
            ) : null}
          </div>
        </div>

        <div className="min-h-0 flex-1 overflow-y-auto overscroll-contain p-4">
          {query.isPending ? (
            <div className="flex flex-col gap-2">
              {Array.from({ length: 3 }, (_, index) => (
                <div key={index} className="flex flex-col gap-2 rounded-lg border p-3">
                  <Skeleton className="h-4 w-40" />
                  <Skeleton className="h-3 w-28" />
                </div>
              ))}
            </div>
          ) : query.isError ? (
            <div className="flex h-40 flex-col items-center justify-center gap-2">
              <span className="text-sm text-muted-foreground">回收站加载失败</span>
              <Button type="button" variant="outline" size="sm" onClick={() => void query.refetch()}>
                重新加载
              </Button>
            </div>
          ) : items.length === 0 && total === 0 ? (
            <Empty className="min-h-48">
              <EmptyHeader>
                <EmptyMedia variant="icon">
                  <Trash2 />
                </EmptyMedia>
                <EmptyTitle>回收站为空</EmptyTitle>
                <EmptyDescription>删除的文件会先放到这里，之后可以还原</EmptyDescription>
              </EmptyHeader>
            </Empty>
          ) : (
            <ul className={`flex flex-col gap-2 ${query.isPlaceholderData ? 'opacity-60 transition-opacity' : ''}`}>
              {items.map(file => (
                <li key={String(file.id)} className="flex flex-col gap-2 rounded-lg border p-3">
                  <div className="flex min-w-0 items-start gap-2">
                    <FileTypeIcon type={file.type} className="mt-0.5 size-4 shrink-0 text-muted-foreground" />
                    <div className="min-w-0 flex-1">
                      <p className="truncate text-sm">{file.originalName}</p>
                      <p className="truncate text-xs text-muted-foreground">
                        {fileTypeLabel(file.type)}
                        {file.type === 0 ? '' : ` · ${formatFileSize(file.size)}`}
                        {file.updateTime ? ` · ${file.updateTime}` : ''}
                      </p>
                    </div>
                  </div>
                  {canRestore || canDelete ? (
                    <div className="flex justify-end gap-2">
                      {canRestore ? (
                        <Button
                          type="button"
                          size="sm"
                          variant="outline"
                          onClick={() => setAction({ type: 'restore', file })}
                        >
                          还原
                        </Button>
                      ) : null}
                      {canDelete ? (
                        <Button
                          type="button"
                          size="sm"
                          variant="destructive"
                          onClick={() => setAction({ type: 'delete', file })}
                        >
                          删除
                        </Button>
                      ) : null}
                    </div>
                  ) : null}
                </li>
              ))}
            </ul>
          )}
        </div>

        {total > 0 ? (
          <div className="flex items-center justify-between gap-3 border-t px-4 py-3">
            <span className="text-xs text-muted-foreground tabular-nums">
              第 {page} / {pageCount} 页
            </span>
            <Pagination className="mx-0 w-auto">
              <PaginationContent>
                <PaginationItem>
                  <PaginationArrow
                    direction="previous"
                    text="上一页"
                    disabled={page <= 1}
                    onClick={() => setPage(previous => Math.max(previous - 1, 1))}
                  />
                </PaginationItem>
                <PaginationItem>
                  <PaginationArrow
                    direction="next"
                    text="下一页"
                    disabled={page >= pageCount}
                    onClick={() => setPage(previous => Math.min(previous + 1, pageCount))}
                  />
                </PaginationItem>
              </PaginationContent>
            </Pagination>
          </div>
        ) : null}
      </EntityDrawer>

      <AlertDialog
        open={action !== null}
        onOpenChange={nextOpen => {
          if (!nextOpen && !isSubmitting) {
            setAction(null)
          }
        }}
      >
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogMedia
              className={isRestore ? 'bg-primary/10 text-primary' : 'bg-destructive/10 text-destructive'}
            >
              {isRestore ? <RotateCcw /> : <Trash2 />}
            </AlertDialogMedia>
            <AlertDialogTitle>{isRestore ? '还原文件' : isClean ? '清空回收站' : '彻底删除'}</AlertDialogTitle>
            <AlertDialogDescription>
              {action?.type === 'restore'
                ? `是否确定还原「${action.file.originalName}」？`
                : action?.type === 'delete'
                  ? `是否确定彻底删除「${action.file.originalName}」？删除后不可恢复`
                  : '是否确定清空回收站？该操作不可恢复'}
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel disabled={isSubmitting}>取消</AlertDialogCancel>
            <AlertDialogAction
              variant={isRestore ? 'default' : 'destructive'}
              disabled={isSubmitting}
              onClick={() => void handleConfirm()}
            >
              {isSubmitting ? (
                <>
                  <Spinner />
                  {isRestore ? '还原中' : isClean ? '清空中' : '删除中'}
                </>
              ) : isRestore ? (
                '还原'
              ) : isClean ? (
                '清空'
              ) : (
                '删除'
              )}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </>
  )
}
