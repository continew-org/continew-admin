import { Maximize, Minimize, RefreshCw, Trash2 } from 'lucide-react'

import { Button } from '@/components/ui/button'
import { Spinner } from '@/components/ui/spinner'

interface DictToolbarProps {
  readonly canClearCache: boolean
  readonly isClearingCache: boolean
  readonly isRefreshing: boolean
  readonly isFullscreen: boolean
  readonly onClearCache: () => void
  readonly onRefresh: () => void
  readonly onToggleFullscreen: () => void
}

/** The dictionary page's global actions, rendered in the list card's toolbar slot. */
export function DictToolbar({
  canClearCache,
  isClearingCache,
  isRefreshing,
  isFullscreen,
  onClearCache,
  onRefresh,
  onToggleFullscreen
}: DictToolbarProps) {
  return (
    <>
      {canClearCache && (
        <Button
          variant="outline"
          disabled={isClearingCache}
          onClick={onClearCache}
          className="text-amber-600 hover:text-amber-700 dark:text-amber-400 dark:hover:text-amber-300"
        >
          {isClearingCache ? <Spinner /> : <Trash2 />}
          清除缓存
        </Button>
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
