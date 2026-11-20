import { type RefObject, useCallback, useLayoutEffect, useRef } from 'react'

/**
 * Measures the vertical space left below `ref` and exposes it as a CSS variable, so a descendant can
 * fill the viewport instead of hard-coding how tall the surrounding chrome is.
 *
 * The measurement is idempotent and re-runs when the element's box changes (for example a tab panel
 * that mounts hidden then becomes visible), not only on viewport resize.
 *
 * Returns a `remeasure` callback. Call it from a layout effect when the content height changes
 * (rows expanded, filtered or loaded): the observer-driven run lands after the frame that painted
 * the taller content, which briefly overflows the page and shifts the whole shell by a
 * scrollbar width. A layout-effect call corrects the height before that frame is painted.
 */
export function useAvailableHeight<T extends HTMLElement>(ref: RefObject<T | null>, variable: string) {
  const applyRef = useRef<() => void>(() => {})

  useLayoutEffect(() => {
    const element = ref.current
    if (!element) return

    const root = document.documentElement
    let frame = 0

    const apply = () => {
      frame = 0
      // A hidden panel (for example an inactive tab) has no client rects. Measuring it would write
      // an innerHeight-sized value into the variable; keep the last good value instead and let the
      // ResizeObserver re-measure the moment the panel becomes visible (its box goes 0 -> real).
      if (element.getClientRects().length === 0) {
        return
      }
      // Viewport coordinates: the space to the viewport bottom is innerHeight - rect.top. (Adding
      // scrollY to `top` while subtracting from innerHeight undercounts by the scroll offset, which
      // collapses the table to 0 once the page has scrolled.)
      const top = element.getBoundingClientRect().top
      // Start from the full space below the area, then subtract the chrome below it (page paddings
      // and borders) by however far the page still overflows. This offset is recomputed from scratch
      // on every run rather than accumulated: a run in a shorter or mobile layout used to grow it
      // without bound, so switching back to the desktop layout collapsed the table to 0px.
      let chrome = 0
      let available = Math.max(0, Math.round(window.innerHeight - top))
      element.style.setProperty(variable, `${available}px`)

      // A few bounded passes let the page end exactly at the viewport without drifting over time.
      for (let pass = 0; pass < 3; pass += 1) {
        const overflow = root.scrollHeight - root.clientHeight
        if (overflow <= 0) break
        chrome += overflow
        available = Math.max(0, Math.round(window.innerHeight - top - chrome))
        element.style.setProperty(variable, `${available}px`)
      }
    }

    const schedule = () => {
      if (frame) return
      frame = requestAnimationFrame(apply)
    }

    applyRef.current = apply
    apply()
    const observer = new ResizeObserver(schedule)
    observer.observe(root)
    observer.observe(element)
    window.addEventListener('resize', schedule)
    window.addEventListener('scroll', schedule, { passive: true })

    return () => {
      if (frame) cancelAnimationFrame(frame)
      observer.disconnect()
      window.removeEventListener('resize', schedule)
      window.removeEventListener('scroll', schedule)
      element.style.removeProperty(variable)
      applyRef.current = () => {}
    }
  }, [ref, variable])

  return useCallback(() => applyRef.current(), [])
}
