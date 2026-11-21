import { Info } from 'lucide-react'

import { SuccessFailureCell } from '@/components/status-cell'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Tooltip, TooltipContent, TooltipTrigger } from '@/components/ui/tooltip'
import type { LogStatus } from '@/lib/api/log'

/** 成功/失败 badge; a failed operation keeps its exception message behind the info button. */
export function LogStatusCell({ status, errorMsg }: { status: LogStatus; errorMsg?: string | null }) {
  return (
    <div className="flex items-center gap-1">
      <SuccessFailureCell status={status} />
      {status !== 1 && errorMsg ? (
        <Tooltip>
          <TooltipTrigger render={<Button variant="ghost" size="icon-xs" />}>
            <Info className="size-3.5" />
          </TooltipTrigger>
          <TooltipContent side="top" className="max-w-80 whitespace-pre-wrap">
            {errorMsg}
          </TooltipContent>
        </Tooltip>
      ) : null}
    </div>
  )
}

/** Over 500 ms is slow, over 200 ms is worth noticing. */
export function TimeTakenCell({ timeTaken }: { timeTaken: number }) {
  const tone =
    timeTaken > 500
      ? 'bg-destructive/10 text-destructive'
      : timeTaken > 200
        ? 'bg-status-caution/10 text-status-caution-foreground'
        : 'bg-status-success/10 text-status-success-foreground'

  return (
    <Badge variant="secondary" className={tone}>
      {timeTaken} ms
    </Badge>
  )
}
