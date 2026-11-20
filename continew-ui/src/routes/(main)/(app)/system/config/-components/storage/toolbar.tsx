import { Cloud, HardDrive, Maximize, Minimize, Plus, RefreshCw } from 'lucide-react'

import { Button } from '@/components/ui/button'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuItem,
  DropdownMenuTrigger
} from '@/components/ui/dropdown-menu'
import { Spinner } from '@/components/ui/spinner'
import type { StorageType } from '@/lib/api/storage'

interface StorageToolbarProps {
  readonly canCreate: boolean
  readonly isRefreshing: boolean
  readonly isFullscreen: boolean
  readonly onCreate: (type: StorageType) => void
  readonly onRefresh: () => void
  readonly onToggleFullscreen: () => void
}

/** The storage page's actions, rendered in the list card's toolbar slot. */
export function StorageToolbar({
  canCreate,
  isRefreshing,
  isFullscreen,
  onCreate,
  onRefresh,
  onToggleFullscreen
}: StorageToolbarProps) {
  return (
    <>
      {canCreate && (
        <DropdownMenu>
          <DropdownMenuTrigger render={<Button />}>
            <Plus />
            新增存储
          </DropdownMenuTrigger>
          <DropdownMenuContent align="start" className="w-40">
            <DropdownMenuGroup>
              <DropdownMenuItem onClick={() => onCreate(1)}>
                <HardDrive />
                本地存储
              </DropdownMenuItem>
              <DropdownMenuItem onClick={() => onCreate(2)}>
                <Cloud />
                对象存储
              </DropdownMenuItem>
            </DropdownMenuGroup>
          </DropdownMenuContent>
        </DropdownMenu>
      )}

      <Button variant="outline" disabled={isRefreshing} onClick={onRefresh}>
        {isRefreshing ? <Spinner /> : <RefreshCw />}
        刷新
      </Button>

      <Button variant="outline" onClick={onToggleFullscreen}>
        {isFullscreen ? <Minimize /> : <Maximize />}
        表格全屏
      </Button>
    </>
  )
}
