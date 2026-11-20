import type { MouseEvent } from 'react'
import { ChevronsDownUp, ChevronsUpDown } from 'lucide-react'

import { Button } from '@/components/ui/button'
import { Tooltip, TooltipContent, TooltipTrigger } from '@/components/ui/tooltip'

export interface TreeExpandActionsProps {
  readonly onExpandAll: (event: MouseEvent<HTMLButtonElement>) => void
  readonly onCollapseAll: (event: MouseEvent<HTMLButtonElement>) => void
}

/** The expand/collapse button pair shared by the tree shells. */
export function TreeExpandActions({ onExpandAll, onCollapseAll }: TreeExpandActionsProps) {
  return (
    <div className="flex shrink-0 items-center gap-0.5">
      <Tooltip>
        <TooltipTrigger
          render={<Button type="button" variant="ghost" size="icon-xs" aria-label="展开全部" onClick={onExpandAll} />}
        >
          <ChevronsUpDown className="size-3" />
        </TooltipTrigger>
        <TooltipContent side="top">展开全部</TooltipContent>
      </Tooltip>

      <Tooltip>
        <TooltipTrigger
          render={<Button type="button" variant="ghost" size="icon-xs" aria-label="折叠全部" onClick={onCollapseAll} />}
        >
          <ChevronsDownUp className="size-3" />
        </TooltipTrigger>
        <TooltipContent side="top">折叠全部</TooltipContent>
      </Tooltip>
    </div>
  )
}
