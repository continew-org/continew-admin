import * as React from 'react'

import { cn } from 'cn'

import { useAvailableHeight } from '@/hooks/use-available-height'
import type { useFullscreenTransition } from '@/hooks/use-fullscreen-transition'

type FullscreenControls = ReturnType<typeof useFullscreenTransition>

interface TableFillAreaProps {
  /** The page's fullscreen controller, so the fill height matches normal and fullscreen states. */
  readonly fullscreen: FullscreenControls
  readonly children: React.ReactNode
  readonly className?: string
}

/**
 * The shared "fill the remaining height" shell for a list table: it measures the space from here to
 * the viewport bottom and sizes an inner column to it, so the table scrolls inside the card instead
 * of the page. Pair it with `<DataTable fillHeight />`.
 */
export function TableFillArea({ fullscreen, children, className }: TableFillAreaProps) {
  const areaRef = React.useRef<HTMLDivElement>(null)
  useAvailableHeight(areaRef, '--table-available-height')

  return (
    <div ref={areaRef} className="flex min-h-0 flex-1 flex-col">
      <div
        className={cn(
          'flex min-h-0 flex-col',
          fullscreen.isFullscreen ? 'h-[calc(100vh-6.5rem)]' : 'md:h-[var(--table-available-height)]',
          className
        )}
      >
        {children}
      </div>
    </div>
  )
}
