import { useEffect, useState } from 'react'

import { Maximize, Minimize } from 'lucide-react'

import { Button } from '@/components/ui/button'
import { Tooltip, TooltipContent, TooltipTrigger } from '@/components/ui/tooltip'

/**
 * Toggles browser fullscreen.
 *
 * The icon follows `document.fullscreenElement` through the `fullscreenchange` event, so
 * leaving fullscreen with Esc or F11 keeps the button in sync.
 */
export function FullscreenToggle() {
  const [isFullscreen, setIsFullscreen] = useState(() => Boolean(document.fullscreenElement))

  useEffect(() => {
    const sync = () => setIsFullscreen(Boolean(document.fullscreenElement))
    document.addEventListener('fullscreenchange', sync)

    return () => document.removeEventListener('fullscreenchange', sync)
  }, [])

  // A frame that was not granted `allowfullscreen` exposes no Fullscreen API at all.
  if (!document.fullscreenEnabled) {
    return null
  }

  const toggle = () => {
    const request = document.fullscreenElement
      ? document.exitFullscreen()
      : document.documentElement.requestFullscreen()

    // The browser rejects when it refuses the request, for example without a user
    // gesture. The icon resyncs from `fullscreenchange` either way.
    void request.catch(() => {})
  }

  const label = isFullscreen ? '退出全屏' : '进入全屏'

  return (
    <Tooltip>
      <TooltipTrigger
        render={
          <Button variant="outline" size="icon-sm" aria-label={label} aria-pressed={isFullscreen} onClick={toggle} />
        }
      >
        {isFullscreen ? <Minimize /> : <Maximize />}
      </TooltipTrigger>
      <TooltipContent side="bottom" sideOffset={8}>
        {label}
      </TooltipContent>
    </Tooltip>
  )
}
