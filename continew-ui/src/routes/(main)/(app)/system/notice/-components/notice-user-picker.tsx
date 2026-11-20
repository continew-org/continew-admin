import * as React from 'react'

import { UserPlus } from 'lucide-react'

import { UserPicker } from '@/components/user-picker'
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle
} from '@/components/ui/dialog'

interface NoticeUserPickerProps {
  readonly open: boolean
  readonly selected: string[]
  readonly onClose: () => void
  readonly onConfirm: (ids: string[]) => void
}

/** The Vue page uses a user-selection modal; this is the same picker in the project's dialog style. */
export function NoticeUserPicker({ open, selected, onClose, onConfirm }: NoticeUserPickerProps) {
  const [selectedIds, setSelectedIds] = React.useState<Set<string>>(new Set())

  React.useEffect(() => {
    if (open) {
      setSelectedIds(new Set(selected))
    }
  }, [open, selected])

  const toggleSelect = (id: string) => {
    setSelectedIds(prev => {
      const next = new Set(prev)
      if (next.has(id)) {
        next.delete(id)
      } else {
        next.add(id)
      }
      return next
    })
  }

  return (
    <Dialog
      open={open}
      onOpenChange={next => {
        if (!next) {
          onClose()
        }
      }}
    >
      <DialogContent className="max-w-lg">
        <DialogHeader>
          <div className="flex items-center gap-2">
            <UserPlus className="size-4" />
            <DialogTitle>选择通知用户</DialogTitle>
          </div>
          <DialogDescription>通知范围选择“指定用户”时，公告只发送给这里选中的用户</DialogDescription>
        </DialogHeader>

        <div className="flex flex-col gap-3">
          <UserPicker open={open} selectedIds={selectedIds} onToggle={toggleSelect} />

          <div className="flex items-center justify-between px-1 text-xs text-muted-foreground">
            <span>已选 {selectedIds.size} 名用户</span>
            {selectedIds.size > 0 ? (
              <Button type="button" variant="ghost" size="xs" onClick={() => setSelectedIds(new Set())}>
                清空
              </Button>
            ) : null}
          </div>
        </div>

        <DialogFooter>
          <Button type="button" variant="outline" onClick={onClose}>
            取消
          </Button>
          <Button type="button" onClick={() => onConfirm(Array.from(selectedIds))}>
            确定
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
