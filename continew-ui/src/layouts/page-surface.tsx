import type { ReactNode } from 'react'

import { useRouterState } from '@tanstack/react-router'
import { motion, useReducedMotion } from 'motion/react'

import { Spinner } from '@/components/ui/spinner'
import type { TransitionName } from '@/lib/preferences/layout'

// The shake transition runs through motion and only honours the OS reduced-motion setting,
// not the `animation_enable` preference; the four fade variants stay CSS-driven.
const SHAKE_KEYFRAMES = [0, -8, 6, -4, 2, 0]

/**
 * Route content wrapper. It is keyed on the pathname (and the refresh counter) by the layout,
 * so every navigation remounts it and replays the entrance animation; the shell around it
 * never re-animates. `fade-*` variants animate via `data-transition` on the parent, while
 * `fade-shake` uses motion on this wrapper.
 */
export function PageSurface({
  showLoading,
  transition,
  children
}: {
  showLoading: boolean
  transition?: TransitionName
  children: ReactNode
}) {
  // Subscribed here, not in the layout, so a pending navigation re-renders only this surface.
  const loading = useRouterState({ select: state => showLoading && state.status === 'pending' })

  const shake = transition === 'fade-shake'
  const reduceMotion = useReducedMotion()
  const Wrapper = shake ? motion.div : 'div'

  return (
    <Wrapper
      data-slot="page-surface"
      className="relative flex min-h-0 w-full min-w-0 flex-1 flex-col"
      {...(shake && {
        initial: { opacity: 0, x: 0 },
        animate: reduceMotion ? { opacity: 1, x: 0 } : { opacity: 1, x: SHAKE_KEYFRAMES },
        transition: { duration: 0.38, ease: 'easeOut' }
      })}
    >
      {children}
      {loading && (
        <div
          aria-hidden
          className="pointer-events-none absolute inset-0 z-10 flex items-start justify-center pt-24 motion-safe:animate-in motion-safe:delay-150 motion-safe:fill-mode-backwards motion-safe:fade-in"
        >
          <Spinner className="size-5 text-primary" />
        </div>
      )}
    </Wrapper>
  )
}
