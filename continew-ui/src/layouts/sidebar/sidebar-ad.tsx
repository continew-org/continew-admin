import * as React from 'react'

import { cn } from 'cn'

import { useSidebar } from '@/components/ui/sidebar'
import { DESKTOP_MEDIA_QUERY } from '@/hooks/use-mobile'

import { isSidebarAdVisible, type SidebarCollapsible } from './sidebar-ad-visibility'

const SCRIPT_SRC = 'https://cdn.wwads.cn/js/makemoney.js'

/** Ad unit id from the build; empty when ads are off. */
const UNIT_ID = import.meta.env.VITE_WWADS_UNIT_ID

let scriptRequested = false

/** The vendor throws if it finds a second copy, so this runs once per page. */
function loadScript() {
  if (scriptRequested) return
  scriptRequested = true
  // HMR re-evaluates this module without reloading the page.
  if (document.querySelector('script[src*="makemoney.js"]')) return

  const script = document.createElement('script')
  script.src = SCRIPT_SRC
  script.async = true
  script.charset = 'UTF-8'
  document.head.appendChild(script)
}

/**
 * WWAds' vertical unit at the bottom of the sidebar. The host stays mounted once shown, so
 * folding or changing pages keeps the filled creative; other shell states never render it.
 */
export function SidebarAd({ collapsible }: { collapsible: SidebarCollapsible }) {
  const { state, isMobile } = useSidebar()
  const visible = isSidebarAdVisible({ unitId: UNIT_ID, isMobile, state, collapsible })
  const [shown, setShown] = React.useState(false)

  React.useEffect(() => {
    if (!visible) return
    // `useIsMobile` is still false on the first frame, so check the real viewport before loading.
    if (!window.matchMedia(DESKTOP_MEDIA_QUERY).matches) return
    setShown(true)
    loadScript()
  }, [visible])

  if (!UNIT_ID || !(visible || shown)) return null

  return (
    <div data-slot="sidebar-ad" className={cn('shrink-0 p-1', !visible && 'hidden')}>
      <div className="wwads-cn wwads-vertical mx-auto w-full max-w-[180px]" data-id={UNIT_ID} />
    </div>
  )
}
