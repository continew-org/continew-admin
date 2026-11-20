import * as React from 'react'

import { useRouterState } from '@tanstack/react-router'
import { useShallow } from 'zustand/react/shallow'

import { toast } from '@/components/ui/toast'
import { useSiteOptions } from '@/hooks/use-site-options'
import { STATIC_ROUTE_TITLES } from '@/navigation/route-titles'
import { useAuthStore } from '@/stores/auth/auth-provider'
import { usePreferencesStore } from '@/stores/preferences/preferences-provider'

import { useBreadcrumbTrail } from './app-breadcrumb'

/** `app_dynamic_title`: "<page> - <site title>". */
export function DocumentTitle() {
  const pathname = useRouterState({ select: state => state.location.pathname })
  const trail = useBreadcrumbTrail()
  const { title: siteTitle } = useSiteOptions()
  const dynamic = usePreferencesStore(state => state.values.app_dynamic_title)

  const pageTitle = trail.length > 0 ? trail[trail.length - 1].title : STATIC_ROUTE_TITLES[pathname]

  React.useEffect(() => {
    const base = siteTitle || 'ContiNew Admin'
    document.title = dynamic && pageTitle ? `${pageTitle} - ${base}` : base
  }, [dynamic, pageTitle, siteTitle])

  return null
}

function formatWatermarkTime(date: Date) {
  const pad = (value: number) => String(value).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

/** Re-renders on an interval so the watermark clock stays current; idle while `active` is false. */
function useNow(active: boolean) {
  const [now, setNow] = React.useState(() => new Date())

  React.useEffect(() => {
    if (!active) return
    setNow(new Date())
    const timer = window.setInterval(() => setNow(new Date()), 1000)
    return () => window.clearInterval(timer)
  }, [active])

  return now
}

const WATERMARK_TILE = 280

/** Tiled watermark drawing the user name and current time, using the app's global font. */
export function Watermark() {
  const { enabled, content, dark } = usePreferencesStore(
    useShallow(state => ({
      enabled: state.values.app_watermark,
      content: state.values.app_watermark_content,
      dark: state.resolvedThemeMode === 'dark'
    }))
  )
  const userName = useAuthStore(state => state.user?.nickname || state.user?.username || '')
  const now = useNow(enabled)
  const patternId = `wm${React.useId().replace(/[^a-zA-Z0-9_-]/g, '')}`

  const lines = [content || userName, formatWatermarkTime(now)].filter(line => line.length > 0)
  if (!enabled || lines.length === 0) return null

  const color = dark ? 'rgba(255,255,255,0.08)' : 'rgba(0,0,0,0.07)'

  return (
    <svg aria-hidden data-slot="app-watermark" style={{ fontFamily: 'var(--app-font-sans)' }}>
      <defs>
        <pattern
          id={patternId}
          width={WATERMARK_TILE}
          height={180}
          patternUnits="userSpaceOnUse"
          patternTransform="rotate(-22)"
        >
          {lines.map((line, index) => (
            <text
              key={`${index}-${line}`}
              x={WATERMARK_TILE / 2}
              y={90 + (index - (lines.length - 1) / 2) * 22}
              textAnchor="middle"
              dominantBaseline="middle"
              fontSize={14}
              fill={color}
            >
              {line}
            </text>
          ))}
        </pattern>
      </defs>
      <rect width="100%" height="100%" fill={`url(#${patternId})`} />
    </svg>
  )
}

/**
 * `app_enable_check_updates`: polls index.html every 5 minutes (only while the tab is visible)
 * and offers a reload when the deployed entry script has changed.
 */
export function UpdateChecker() {
  const enabled = usePreferencesStore(state => state.values.app_enable_check_updates)

  React.useEffect(() => {
    if (!enabled || import.meta.env.DEV) return

    const current = document.querySelector<HTMLScriptElement>('script[type="module"][src]')?.src
    if (!current) return

    let notified = false
    const check = async () => {
      if (notified || document.visibilityState !== 'visible') return
      try {
        const html = await fetch(`${import.meta.env.BASE_URL}index.html?t=${Date.now()}`, { cache: 'no-store' }).then(
          r => r.text()
        )
        const match = html.match(/<script[^>]+type="module"[^>]+src="([^"]+)"/)
        if (match && new URL(match[1], location.href).href !== current) {
          notified = true
          toast.add({
            title: '发现新版本',
            description: '系统已更新，刷新页面即可使用新版本。',
            type: 'info'
          })
        }
      } catch {
        // Offline or blocked: try again next round.
      }
    }

    const timer = window.setInterval(() => void check(), 5 * 60_000)
    return () => window.clearInterval(timer)
  }, [enabled])

  return null
}
