import * as React from 'react'

import { useQuery, useQueryClient } from '@tanstack/react-query'

import { ArrowLeft, FolderOpen, FolderPlus, LayoutGrid, List, RefreshCw, Trash2, Upload } from 'lucide-react'

import { cn } from 'cn'

import { PaginationArrow } from '@/components/pagination-arrow'
import { QueryState } from '@/components/query-state'
import { TableSearch } from '@/components/table-search'
import {
  Breadcrumb,
  BreadcrumbItem,
  BreadcrumbLink,
  BreadcrumbList,
  BreadcrumbPage,
  BreadcrumbSeparator
} from '@/components/ui/breadcrumb'
import { Button } from '@/components/ui/button'
import { Card, CardAction, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Empty, EmptyDescription, EmptyHeader, EmptyMedia, EmptyTitle } from '@/components/ui/empty'
import { Pagination, PaginationContent, PaginationItem } from '@/components/ui/pagination'
import { Skeleton } from '@/components/ui/skeleton'
import { Spinner } from '@/components/ui/spinner'
import { ToggleGroup, ToggleGroupItem } from '@/components/ui/toggle-group'
import { usePermission } from '@/hooks/use-permission'
import {
  fileKeys,
  getFileStatistics,
  listFiles,
  type FileFilterType,
  type FilePageQuery,
  type FileResp
} from '@/lib/api/file'

import { type FileActionHandlers } from './file-actions'
import { FileDeleteDialog } from './file-delete-dialog'
import { FileDetailDrawer } from './file-detail-drawer'
import { FILE_TYPE_FILTERS } from './file-display'
import { FileGrid } from './file-grid'
import { FileNameDialog } from './file-name-dialog'
import { FilePreviewDialog } from './file-preview-dialog'
import { FileRecycleDrawer } from './file-recycle-drawer'
import { FileStorageCard } from './file-storage-card'
import { FileTable } from './file-table'
import { FileTypeChart } from './file-type-chart'
import { FileUploadDialog } from './file-upload-dialog'

const PAGE_SIZE = 24

/** Matches the dashboard/list skeleton rhythm without pulling in the table instance. */
function FileListSkeleton({ view }: { view: 'grid' | 'list' }) {
  if (view === 'list') {
    return (
      <div className="flex flex-col gap-3 px-4">
        {Array.from({ length: 6 }, (_, index) => (
          <div key={index} className="flex items-center gap-3">
            <Skeleton className="size-4 rounded" />
            <Skeleton className="h-4 flex-1" />
            <Skeleton className="hidden h-3 w-16 sm:block" />
            <Skeleton className="hidden h-3 w-28 lg:block" />
          </div>
        ))}
      </div>
    )
  }

  return (
    <div className="grid gap-3 px-4 sm:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-4">
      {Array.from({ length: 8 }, (_, index) => (
        <Card key={index} size="sm" className="gap-0 overflow-hidden">
          <Skeleton className="h-28 w-full rounded-none" />
          <div className="flex flex-col gap-2 p-4">
            <Skeleton className="h-4 w-3/4" />
            <Skeleton className="h-3 w-1/2" />
          </div>
        </Card>
      ))}
    </div>
  )
}

export function FileManager() {
  const queryClient = useQueryClient()
  const { hasPermission } = usePermission()
  const canUpload = hasPermission('system:file:upload')
  const canCreateDir = hasPermission('system:file:createDir')
  const canUpdate = hasPermission('system:file:update')
  const canDelete = hasPermission('system:file:delete')
  const canDownload = hasPermission('system:file:download')
  const canListRecycle = hasPermission('system:fileRecycle:list')

  const [activeType, setActiveType] = React.useState<'all' | FileFilterType>('all')
  // `undefined` means a global type view; a string means browsing that folder path.
  const [currentPath, setCurrentPath] = React.useState<string | undefined>('/')
  const [pathNames, setPathNames] = React.useState<Record<string, string>>({})
  const [keyword, setKeyword] = React.useState('')
  const [page, setPage] = React.useState(1)
  const [view, setView] = React.useState<'grid' | 'list'>('grid')

  const [uploadOpen, setUploadOpen] = React.useState(false)
  const [createDirOpen, setCreateDirOpen] = React.useState(false)
  const [renameFile, setRenameFile] = React.useState<FileResp | null>(null)
  const [deletingFile, setDeletingFile] = React.useState<FileResp | null>(null)
  const [detailFile, setDetailFile] = React.useState<FileResp | null>(null)
  const [previewFile, setPreviewFile] = React.useState<FileResp | null>(null)
  const [recycleOpen, setRecycleOpen] = React.useState(false)

  const params: FilePageQuery = {
    page,
    size: PAGE_SIZE,
    originalName: keyword || undefined,
    parentPath: activeType === 'all' ? currentPath : undefined,
    type: activeType === 'all' ? undefined : activeType,
    sort: 'type,asc'
  }

  const query = useQuery({
    queryKey: fileKeys.list(params),
    queryFn: ({ signal }) => listFiles(params, signal),
    placeholderData: previous => previous
  })

  const stats = useQuery({
    queryKey: fileKeys.statistics(),
    queryFn: ({ signal }) => getFileStatistics(signal)
  })

  const files = query.data?.list ?? []
  const total = query.data?.total ?? 0
  const pageCount = Math.max(1, Math.ceil(total / PAGE_SIZE))

  React.useEffect(() => {
    if (page > pageCount) {
      setPage(pageCount)
    }
  }, [page, pageCount])

  const refreshFiles = React.useCallback(() => {
    void queryClient.invalidateQueries({ queryKey: fileKeys.all })
  }, [queryClient])

  const handleTypeChange = React.useCallback((type: 'all' | FileFilterType) => {
    setActiveType(type)
    setCurrentPath(type === 'all' ? '/' : undefined)
    setPage(1)
  }, [])

  const handleSearch = React.useCallback((next: string) => {
    setKeyword(next)
    setPage(1)
  }, [])

  const openFolder = React.useCallback((file: FileResp) => {
    if (!file.path) {
      return
    }

    setActiveType('all')
    setCurrentPath(file.path)
    setPathNames(current => ({ ...current, [file.path]: file.originalName }))
    setPage(1)
  }, [])

  const goToPath = React.useCallback((path: string) => {
    setActiveType('all')
    setCurrentPath(path)
    setPage(1)
  }, [])

  const goParent = React.useCallback(() => {
    if (!currentPath || currentPath === '/') {
      return
    }

    const segments = currentPath.split('/').filter(Boolean)
    const parent = segments.length <= 1 ? '/' : '/' + segments.slice(0, -1).join('/')
    goToPath(parent)
  }, [currentPath, goToPath])

  const handleActivate = React.useCallback(
    (file: FileResp) => {
      if (file.type === 0) {
        openFolder(file)
        return
      }

      if (file.type === 2) {
        setPreviewFile(file)
        return
      }

      setDetailFile(file)
    },
    [openFolder]
  )

  const handleOpen = React.useCallback(
    (file: FileResp) => {
      if (file.type === 0) {
        openFolder(file)
        return
      }

      if (file.url) {
        window.open(file.url, '_blank', 'noopener,noreferrer')
      }
    },
    [openFolder]
  )

  const handlePreview = React.useCallback((file: FileResp) => {
    setPreviewFile(file)
  }, [])

  const handleDownload = React.useCallback((file: FileResp) => {
    if (!file.url) {
      return
    }

    const anchor = document.createElement('a')
    anchor.href = file.url
    anchor.download = file.originalName || file.name
    anchor.target = '_blank'
    anchor.rel = 'noreferrer'
    document.body.append(anchor)
    anchor.click()
    anchor.remove()
  }, [])

  const handleDetail = React.useCallback((file: FileResp) => {
    setDetailFile(file)
  }, [])

  const handleRename = React.useCallback((file: FileResp) => {
    setRenameFile(file)
  }, [])

  const handleDelete = React.useCallback((file: FileResp) => {
    setDeletingFile(file)
  }, [])

  const handlers: FileActionHandlers = {
    onActivate: handleActivate,
    onOpen: handleOpen,
    onPreview: handlePreview,
    onDownload: handleDownload,
    onDetail: handleDetail,
    onRename: handleRename,
    onDelete: handleDelete
  }

  const crumbs = React.useMemo(() => {
    if (!currentPath || currentPath === '/') {
      return []
    }

    const segments = currentPath.split('/').filter(Boolean)
    return segments.map((segment, index) => {
      const path = '/' + segments.slice(0, index + 1).join('/')
      return { path, name: pathNames[path] ?? segment }
    })
  }, [currentPath, pathNames])

  const activeTypeLabel = FILE_TYPE_FILTERS.find(item => item.value === activeType)?.label

  const fileContent = query.isError ? (
    <QueryState state="error" className="min-h-64" onRetry={() => void query.refetch()} />
  ) : query.isPending ? (
    <FileListSkeleton view={view} />
  ) : files.length === 0 ? (
    <Empty className="min-h-64">
      <EmptyHeader>
        <EmptyMedia variant="icon">
          <FolderOpen />
        </EmptyMedia>
        <EmptyTitle>{keyword ? '没有匹配的文件' : '暂无文件'}</EmptyTitle>
        <EmptyDescription>
          {keyword ? '试试其他关键词' : activeType === 'all' ? '上传文件或新建文件夹开始整理' : '该分类下还没有文件'}
        </EmptyDescription>
      </EmptyHeader>
    </Empty>
  ) : (
    <div className={cn(query.isPlaceholderData && 'opacity-60 transition-opacity')}>
      {view === 'grid' ? (
        <FileGrid
          files={files}
          canDownload={canDownload}
          canUpdate={canUpdate}
          canDelete={canDelete}
          handlers={handlers}
        />
      ) : (
        <FileTable
          files={files}
          canDownload={canDownload}
          canUpdate={canUpdate}
          canDelete={canDelete}
          handlers={handlers}
        />
      )}
    </div>
  )

  const pagination = (
    <div className="flex flex-wrap items-center justify-between gap-2 border-t px-4 py-3 text-sm text-muted-foreground">
      <span>共 {total} 项</span>
      <Pagination className="mx-0 w-auto">
        <PaginationContent>
          <PaginationItem>
            <PaginationArrow
              direction="previous"
              text="上一页"
              disabled={page <= 1 || query.isFetching}
              onClick={() => setPage(current => Math.max(1, current - 1))}
            />
          </PaginationItem>
          <span className="px-2 text-sm tabular-nums">
            第 {page} / {pageCount} 页
          </span>
          <PaginationItem>
            <PaginationArrow
              direction="next"
              text="下一页"
              disabled={page >= pageCount || query.isFetching}
              onClick={() => setPage(current => Math.min(pageCount, current + 1))}
            />
          </PaginationItem>
        </PaginationContent>
      </Pagination>
    </div>
  )

  return (
    <>
      <div className="flex flex-col gap-4 xl:flex-row">
        <aside className="flex w-full shrink-0 flex-col gap-4 xl:w-64">
          <FileStorageCard
            activeType={activeType}
            stats={stats.data}
            isLoading={stats.isPending}
            isError={stats.isError}
            onRetry={() => void stats.refetch()}
            onTypeChange={handleTypeChange}
          />
          <FileTypeChart stats={stats.data} isLoading={stats.isPending} />
        </aside>

        <Card className="min-w-0 flex-1 pb-0">
          <CardHeader className="border-b has-data-[slot=card-action]:grid-cols-1 md:has-data-[slot=card-action]:grid-cols-[1fr_auto]">
            <CardTitle className="text-xl">文件管理</CardTitle>
            <CardDescription>管理存储文件与文件夹</CardDescription>
            <CardAction className="col-start-1 row-start-auto flex w-full flex-wrap justify-start gap-2 justify-self-stretch md:col-start-2 md:row-span-2 md:row-start-1 md:w-auto md:flex-nowrap md:justify-end md:justify-self-end">
              <TableSearch
                clearable
                className="h-8 w-full sm:w-64"
                placeholder="搜索文件名称"
                onSearch={handleSearch}
              />
            </CardAction>
          </CardHeader>

          <CardContent className="flex flex-col gap-4 px-0">
            <div className="flex flex-wrap items-center justify-between gap-2 px-4">
              <Breadcrumb className="min-w-0">
                <BreadcrumbList>
                  <BreadcrumbItem>
                    {activeType === 'all' && crumbs.length === 0 ? (
                      <BreadcrumbPage>全部文件</BreadcrumbPage>
                    ) : (
                      <BreadcrumbLink
                        className="cursor-pointer rounded-sm outline-none focus-visible:ring-2 focus-visible:ring-ring"
                        render={<button type="button" />}
                        onClick={() => handleTypeChange('all')}
                      >
                        全部文件
                      </BreadcrumbLink>
                    )}
                  </BreadcrumbItem>
                  {activeType !== 'all' ? (
                    <>
                      <BreadcrumbSeparator />
                      <BreadcrumbItem>
                        <BreadcrumbPage>{activeTypeLabel}</BreadcrumbPage>
                      </BreadcrumbItem>
                    </>
                  ) : (
                    crumbs.map((crumb, index) => (
                      <React.Fragment key={crumb.path}>
                        <BreadcrumbSeparator />
                        <BreadcrumbItem>
                          {index === crumbs.length - 1 ? (
                            <BreadcrumbPage>{crumb.name}</BreadcrumbPage>
                          ) : (
                            <BreadcrumbLink
                              className="cursor-pointer rounded-sm outline-none focus-visible:ring-2 focus-visible:ring-ring"
                              render={<button type="button" />}
                              onClick={() => goToPath(crumb.path)}
                            >
                              {crumb.name}
                            </BreadcrumbLink>
                          )}
                        </BreadcrumbItem>
                      </React.Fragment>
                    ))
                  )}
                </BreadcrumbList>
              </Breadcrumb>
              {activeType === 'all' && currentPath && currentPath !== '/' ? (
                <Button type="button" variant="ghost" onClick={goParent}>
                  <ArrowLeft />
                  返回上级
                </Button>
              ) : null}
            </div>

            <div className="flex flex-wrap items-center justify-between gap-2 px-4">
              <div className="flex flex-wrap items-center gap-2">
                {canUpload ? (
                  <Button type="button" onClick={() => setUploadOpen(true)}>
                    <Upload />
                    上传文件
                  </Button>
                ) : null}
                {canCreateDir ? (
                  <Button
                    type="button"
                    variant="outline"
                    disabled={currentPath === undefined}
                    onClick={() => setCreateDirOpen(true)}
                  >
                    <FolderPlus />
                    新建文件夹
                  </Button>
                ) : null}
              </div>

              <div className="flex flex-wrap items-center gap-2">
                <Button type="button" variant="outline" disabled={query.isFetching} onClick={refreshFiles}>
                  {query.isFetching ? <Spinner /> : <RefreshCw />}
                  刷新
                </Button>
                {canListRecycle ? (
                  <Button type="button" variant="outline" onClick={() => setRecycleOpen(true)}>
                    <Trash2 />
                    回收站
                  </Button>
                ) : null}
                <ToggleGroup
                  variant="outline"
                  spacing={0}
                  value={[view]}
                  onValueChange={values => {
                    const next = values[0]
                    if (next === 'grid' || next === 'list') {
                      setView(next)
                    }
                  }}
                >
                  <ToggleGroupItem value="grid">
                    <LayoutGrid />
                    网格
                  </ToggleGroupItem>
                  <ToggleGroupItem value="list">
                    <List />
                    列表
                  </ToggleGroupItem>
                </ToggleGroup>
              </div>
            </div>

            {view === 'list' ? (
              <div className="flex flex-col">
                {fileContent}
                {pagination}
              </div>
            ) : (
              <>
                {fileContent}
                {pagination}
              </>
            )}
          </CardContent>
        </Card>
      </div>

      <FileUploadDialog
        open={uploadOpen}
        parentPath={currentPath ?? '/'}
        onClose={() => setUploadOpen(false)}
        onUploaded={refreshFiles}
      />
      <FileNameDialog
        open={createDirOpen}
        mode="createDir"
        parentPath={currentPath ?? '/'}
        onClose={() => setCreateDirOpen(false)}
        onCompleted={refreshFiles}
      />
      <FileNameDialog
        open={renameFile !== null}
        mode="rename"
        file={renameFile}
        parentPath={renameFile?.parentPath ?? '/'}
        onClose={() => setRenameFile(null)}
        onCompleted={refreshFiles}
      />
      <FileDeleteDialog
        file={deletingFile}
        open={deletingFile !== null}
        onClose={() => setDeletingFile(null)}
        onCompleted={refreshFiles}
      />
      <FileDetailDrawer file={detailFile} open={detailFile !== null} onClose={() => setDetailFile(null)} />
      <FilePreviewDialog
        file={previewFile}
        open={previewFile !== null}
        canDownload={canDownload}
        onClose={() => setPreviewFile(null)}
        onDownload={handleDownload}
      />
      <FileRecycleDrawer open={recycleOpen} onClose={() => setRecycleOpen(false)} />
    </>
  )
}
