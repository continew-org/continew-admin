import { type RefObject, useCallback, useLayoutEffect, useRef } from 'react'

/**
 * Height of the in-flow content stacked after `node` inside its parent (for example the shell
 * footer after the content area). Only vertical stacking counts: siblings in a flex row or grid sit
 * beside `node`, and out-of-flow siblings (overlays, fixed buttons) take no space.
 */
function stackedBelow(node: HTMLElement) {
  const parent = node.parentElement
  if (!parent || !node.nextElementSibling) return 0
  const parentStyle = getComputedStyle(parent)
  const flexColumn = parentStyle.display.endsWith('flex') && parentStyle.flexDirection.startsWith('column')
  if (!flexColumn && parentStyle.display !== 'block') return 0
  const gap = flexColumn ? Number.parseFloat(parentStyle.rowGap) || 0 : 0

  let height = 0
  for (let sibling: Element | null = node.nextElementSibling; sibling; sibling = sibling.nextElementSibling) {
    if (!(sibling instanceof HTMLElement) || sibling.offsetHeight === 0) continue
    const style = getComputedStyle(sibling)
    if (style.position === 'absolute' || style.position === 'fixed') continue
    height +=
      sibling.offsetHeight +
      gap +
      (Number.parseFloat(style.marginTop) || 0) +
      (Number.parseFloat(style.marginBottom) || 0)
  }
  return height
}

/**
 * Measures the vertical space left below `ref` and exposes it as a CSS variable, so a descendant can
 * fill the viewport instead of hard-coding how tall the surrounding chrome is.
 *
 * The measurement is idempotent and re-runs when the element's box changes (for example a tab panel
 * that mounts hidden then becomes visible), not only on viewport resize.
 *
 * Reads geometry and ancestor bottom spacing before writing the height. Keeping the reads together
 * avoids repeated synchronous layout passes while a page mounts or its rows change.
 * Returns a callback for callers that need to remeasure after a content change.
 */
export function useAvailableHeight<T extends HTMLElement>(ref: RefObject<T | null>, variable: string) {
  const applyRef = useRef<() => void>(() => {})

  useLayoutEffect(() => {
    const element = ref.current
    if (!element) return

    let frame = 0

    const apply = () => {
      frame = 0
      // A hidden panel (for example an inactive tab) has no client rects. Measuring it would write
      // an innerHeight-sized value into the variable; keep the last good value instead and let the
      // ResizeObserver re-measure the moment the panel becomes visible (its box goes 0 -> real).
      if (element.getClientRects().length === 0) {
        return
      }
      let top = element.getBoundingClientRect().top
      let bottomSpacing = 0
      let fixed = false
      for (let node: HTMLElement | null = element; node; node = node.parentElement) {
        const style = getComputedStyle(node)
        bottomSpacing +=
          (Number.parseFloat(style.paddingBottom) || 0) +
          (Number.parseFloat(style.borderBottomWidth) || 0) +
          (Number.parseFloat(style.marginBottom) || 0)
        // A fullscreen container is positioned against the viewport, not its DOM ancestors.
        if (style.position === 'fixed') {
          fixed = true
          break
        }
        bottomSpacing += stackedBelow(node)
      }
      // Document-relative top: a viewport-relative top shrinks as the window scrolls, which grows
      // the element, which makes the page taller and scrollable again (feedback loop).
      if (!fixed) top += window.scrollY
      const available = `${Math.max(0, Math.round(window.innerHeight - top - bottomSpacing))}px`
      if (element.style.getPropertyValue(variable) !== available) {
        element.style.setProperty(variable, available)
      }
    }

    const schedule = () => {
      if (frame) return
      frame = requestAnimationFrame(apply)
    }

    applyRef.current = apply
    apply()
    const observer = new ResizeObserver(schedule)
    // Ancestors too: toggling chrome below the element (footer) resizes an ancestor such as the
    // content area while the document and the element keep their size.
    for (let node: HTMLElement | null = element; node; node = node.parentElement) observer.observe(node)
    window.addEventListener('resize', schedule)

    return () => {
      if (frame) cancelAnimationFrame(frame)
      observer.disconnect()
      window.removeEventListener('resize', schedule)
      element.style.removeProperty(variable)
      applyRef.current = () => {}
    }
  }, [ref, variable])

  return useCallback(() => applyRef.current(), [])
}
