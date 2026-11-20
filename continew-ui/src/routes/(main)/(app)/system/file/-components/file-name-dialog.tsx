import * as React from 'react'

import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle
} from '@/components/ui/dialog'
import { Field, FieldError, FieldLabel } from '@/components/ui/field'
import { Input } from '@/components/ui/input'
import { Spinner } from '@/components/ui/spinner'
import { toast } from '@/components/ui/toast'
import { createDir, updateFile, type FileResp } from '@/lib/api/file'

type FileNameDialogMode = 'createDir' | 'rename'

interface FileNameDialogProps {
  readonly open: boolean
  readonly mode: FileNameDialogMode
  /** The folder that receives a new directory, or the file's own parent path when renaming. */
  readonly parentPath: string
  readonly file?: FileResp | null
  readonly onClose: () => void
  readonly onCompleted: () => void
}

/** Creates a directory or renames a file, both of which are just a name in this API. */
export function FileNameDialog({ open, mode, parentPath, file, onClose, onCompleted }: FileNameDialogProps) {
  const [name, setName] = React.useState('')
  const [error, setError] = React.useState<string | null>(null)
  const [isSubmitting, setIsSubmitting] = React.useState(false)
  const isCreate = mode === 'createDir'

  React.useEffect(() => {
    if (!open) {
      return
    }

    setName(isCreate ? '' : (file?.originalName ?? ''))
    setError(null)
  }, [open, isCreate, file])

  const submit = async () => {
    const trimmed = name.trim()

    if (!trimmed) {
      setError('请输入名称')
      return
    }

    if (/[\\/]/.test(trimmed)) {
      setError('名称不能包含 / 或 \\')
      return
    }

    if (!isCreate && !file) {
      return
    }

    setIsSubmitting(true)

    try {
      if (isCreate) {
        await createDir(parentPath, trimmed)
      } else if (file) {
        await updateFile(file.id, { originalName: trimmed, parentPath: file.parentPath })
      }

      toast.add({ title: isCreate ? '创建成功' : '修改成功', type: 'success' })
      onCompleted()
      onClose()
    } catch {
      // The HTTP layer already reported the failure.
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <Dialog
      open={open}
      onOpenChange={nextOpen => {
        if (!nextOpen && !isSubmitting) {
          onClose()
        }
      }}
    >
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>{isCreate ? '新建文件夹' : '重命名'}</DialogTitle>
          <DialogDescription>
            {isCreate ? `在 ${parentPath === '/' ? '根目录' : parentPath} 下创建文件夹` : '修改文件或文件夹的显示名称'}
          </DialogDescription>
        </DialogHeader>

        <form
          className="flex flex-col gap-4"
          onSubmit={event => {
            event.preventDefault()
            void submit()
          }}
        >
          <Field className="gap-1.5" data-invalid={Boolean(error)}>
            <FieldLabel htmlFor="file-name">名称</FieldLabel>
            <Input
              id="file-name"
              value={name}
              autoFocus
              disabled={isSubmitting}
              aria-invalid={Boolean(error)}
              placeholder={isCreate ? '请输入文件夹名称' : '请输入名称'}
              onChange={event => {
                setName(event.target.value)
                setError(null)
              }}
            />
            {error ? <FieldError>{error}</FieldError> : null}
          </Field>

          <DialogFooter>
            <Button type="button" variant="outline" disabled={isSubmitting} onClick={onClose}>
              取消
            </Button>
            <Button type="submit" disabled={isSubmitting}>
              {isSubmitting ? (
                <>
                  <Spinner />
                  {isCreate ? '创建中' : '保存中'}
                </>
              ) : isCreate ? (
                '创建'
              ) : (
                '保存'
              )}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
