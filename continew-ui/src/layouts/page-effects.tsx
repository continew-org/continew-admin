import * as React from 'react'

import { useRouterState } from '@tanstack/react-router'
import { useKeyPress } from 'ahooks'
import NProgress from 'nprogress'
import { useShallow } from 'zustand/react/shallow'

import { toast } from '@/components/ui/toast'
import { useLogout } from '@/hooks/use-logout'
import { useSiteOptions } from '@/hooks/use-site-options'
import { useAuthStore } from '@/stores/auth/auth-provider'
import { usePreferencesStore } from '@/stores/preferences/preferences-provider'

import { useCurrentRouteInfo } from './route-info'
import { useShellStore } from './shell-store'

NProgress.configure({ showSpinner: false, trickleSpeed: 120, minimum: 0.2 })

/**
 * Top progress bar driven by the router: it starts when a navigation begins and finishes when
 * the new page is ready, so it shows real loading time instead of a fixed animation.
 */
export function RouteProgress() {
  const isLoading = useRouterState({ select: state => state.status === 'pending' })

  React.useEffect(() => {
    if (!isLoading) return
    // Navigations that resolve within a frame (cached loaders) never show the bar.
    const timer = window.setTimeout(() => NProgress.start(), 80)
    return () => {
      window.clearTimeout(timer)
      NProgress.done()
    }
  }, [isLoading])

  React.useEffect(() => () => void NProgress.remove(), [])

  return null
}

/** `app_dynamic_title`: "<page> - <site title>". */
export function DocumentTitle() {
  const pageTitle = useCurrentRouteInfo()?.title
  const { title: siteTitle } = useSiteOptions()
  const dynamic = usePreferencesStore(state => state.values.app_dynamic_title)

  React.useEffect(() => {
    const base = siteTitle || 'ContiNew Admin'
    document.title = dynamic && pageTitle ? `${pageTitle} - ${base}` : base
  }, [dynamic, pageTitle, siteTitle])

  return null
}

function watermarkUrl(text: string, dark: boolean) {
  const color = dark ? 'rgba(255,255,255,0.08)' : 'rgba(0,0,0,0.07)'
  const safe = text.replace(/[<>&"']/g, ch => `&#${ch.charCodeAt(0)};`)
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="280" height="180"><text x="50%" y="50%" fill="${color}" font-size="15" font-family="sans-serif" text-anchor="middle" dominant-baseline="middle" transform="rotate(-22 140 90)">${safe}</text></svg>`
  return `url("data:image/svg+xml;charset=utf-8,${encodeURIComponent(svg)}")`
}

/** Tiled watermark: one fixed layer with an SVG background, re-generated only when its text changes. */
export function Watermark() {
  const { enabled, content, dark } = usePreferencesStore(
    useShallow(state => ({
      enabled: state.values.app_watermark,
      content: state.values.app_watermark_content,
      dark: state.resolvedThemeMode === 'dark'
    }))
  )
  const userName = useAuthStore(state => state.user?.nickname || state.user?.username || '')
  const text = content || userName
  const backgroundImage = React.useMemo(() => (text ? watermarkUrl(text, dark) : undefined), [text, dark])

  if (!enabled || !backgroundImage) return null

  return <div aria-hidden data-slot="app-watermark" style={{ backgroundImage }} />
}

/** Global shortcuts, each gated by its own preference and the master switch. */
export function GlobalShortcuts() {
  const prefs = usePreferencesStore(
    useShallow(state => ({
      enabled: state.values.shortcut_enable,
      search: state.values.shortcut_search,
      logout: state.values.shortcut_logout,
      lock: state.values.shortcut_lock_screen && state.values.widget_lock_screen,
      escape: state.values.shortcut_escape
    }))
  )
  const logout = useLogout()

  const options = (on: boolean) => ({
    exactMatch: true,
    useCapture: true,
    events: ['keydown' as const],
    target: () => (prefs.enabled && on ? document : null)
  })

  useKeyPress(
    ['ctrl.j', 'meta.j'],
    event => {
      event.preventDefault()
      const { searchOpen, setSearchOpen } = useShellStore.getState()
      setSearchOpen(!searchOpen)
    },
    options(prefs.search)
  )
  useKeyPress(
    'alt.l',
    event => {
      event.preventDefault()
      useShellStore.getState().setLockSetupOpen(true)
    },
    options(prefs.lock)
  )
  useKeyPress(
    'alt.q',
    event => {
      event.preventDefault()
      void logout()
    },
    options(prefs.logout)
  )
  // Esc leaves maximized content; dialogs already close themselves on Esc.
  useKeyPress(
    'esc',
    () => {
      const { contentMaximized, setContentMaximized } = useShellStore.getState()
      if (contentMaximized) setContentMaximized(false)
    },
    options(prefs.escape)
  )

  return null
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
            timeout: 0,
            actionProps: { children: '立即刷新', onClick: () => location.reload() }
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
