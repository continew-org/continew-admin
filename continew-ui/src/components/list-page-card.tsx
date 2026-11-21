import type { ReactNode } from 'react'

import { cn } from 'cn'

import { ListFilterRow } from '@/components/list-filter-row'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { useFullscreenTransition } from '@/hooks/use-fullscreen-transition'

type FullscreenControls = ReturnType<typeof useFullscreenTransition>

interface ListPageCardProps {
  /** Omitted by a tab whose page already renders its own title. */
  readonly title?: string
  readonly description?: string
  /** Search and filter controls, on the left of the row above the table. */
  readonly filters?: ReactNode
  /** Toolbar actions, on the right of the same row. */
  readonly actions?: ReactNode
  readonly fullscreen: FullscreenControls
  readonly children: ReactNode
}

/**
 * The card every list screen shares: an optional title row, the filter and toolbar row above the
 * table, and the fullscreen spacer that keeps the page height stable while the table expands.
 */
export function ListPageCard({ title, description, filters, actions, fullscreen, children }: ListPageCardProps) {
  return (
    <Card className="pb-0">
      {title ? (
        <CardHeader>
          <CardTitle className="text-xl">{title}</CardTitle>
          <CardDescription>{description}</CardDescription>
        </CardHeader>
      ) : null}

      <CardContent className="flex flex-col gap-4 px-0">
        {fullscreen.isFullscreen ? (
          <div ref={fullscreen.spacerRef} style={{ height: fullscreen.reservedHeight }} />
        ) : null}

        <div
          ref={fullscreen.fullscreenRef}
          className={cn('flex flex-col gap-4', fullscreen.isFullscreen && 'bg-background')}
        >
          <ListFilterRow filters={filters} actions={actions} />

          {children}
        </div>
      </CardContent>
    </Card>
  )
}
