import * as React from 'react'

const ENTER_MS = 200
const EXIT_MS = 180
const FLUID_EASE = 'cubic-bezier(0.16, 1, 0.3, 1)'

type FullscreenState = 'off' | 'open' | 'closing'

/**
 * Manages full-screen expansion and contraction for a table container.
 *
 * Uses continuous FLIP geometry transition so that entering expands from the card
 * into the viewport, and exiting contracts from the viewport back into the card.
 * This guarantees a single mounted DOM tree with zero duplicate component instances.
 */
export function useFullscreenTransition() {
  const [fullscreen, setFullscreen] = React.useState<FullscreenState>('off')
  const [reservedHeight, setReservedHeight] = React.useState(0)
  const fullscreenRef = React.useRef<HTMLDivElement>(null)
  const spacerRef = React.useRef<HTMLDivElement>(null)
  const timerRef = React.useRef<ReturnType<typeof setTimeout>>(undefined)

  const isOpen = fullscreen === 'open'
  const isClosing = fullscreen === 'closing'
  const isActive = fullscreen !== 'off'

  const closeFullscreen = React.useCallback(() => {
    const el = fullscreenRef.current
    const spacer = spacerRef.current
    if (!el) {
      setFullscreen('off')
      return
    }

    clearTimeout(timerRef.current)
    setFullscreen('closing')

    const targetRect = spacer?.getBoundingClientRect()
    el.scrollTop = 0
    Object.assign(el.style, {
      overflow: 'hidden',
      transition: `all ${EXIT_MS}ms ${FLUID_EASE}`,
      ...(targetRect
        ? {
            top: `${targetRect.top}px`,
            left: `${targetRect.left}px`,
            width: `${targetRect.width}px`,
            height: `${targetRect.height}px`,
            paddingTop: '0px',
            paddingBottom: '0px'
          }
        : { opacity: '0' })
    })

    timerRef.current = setTimeout(() => {
      Object.assign(el.style, {
        position: '',
        top: '',
        left: '',
        width: '',
        height: '',
        margin: '',
        zIndex: '',
        overflow: '',
        transition: '',
        paddingTop: '',
        paddingBottom: '',
        opacity: ''
      })
      setFullscreen('off')
      setReservedHeight(0)
    }, EXIT_MS)
  }, [])

  const openFullscreen = React.useCallback(() => {
    const el = fullscreenRef.current
    if (!el) return

    clearTimeout(timerRef.current)
    const rect = el.getBoundingClientRect()
    setReservedHeight(rect.height)
    setFullscreen('open')

    Object.assign(el.style, {
      position: 'fixed',
      top: `${rect.top}px`,
      left: `${rect.left}px`,
      width: `${rect.width}px`,
      height: `${rect.height}px`,
      margin: '0',
      zIndex: '50',
      overflow: 'hidden',
      transition: 'none'
    })

    void el.offsetHeight

    requestAnimationFrame(() => {
      Object.assign(el.style, {
        transition: `all ${ENTER_MS}ms ${FLUID_EASE}`,
        top: '0px',
        left: '0px',
        width: '100vw',
        height: '100vh',
        paddingTop: '1rem',
        paddingBottom: '1rem'
      })
    })

    timerRef.current = setTimeout(() => {
      el.style.overflow = 'auto'
    }, ENTER_MS)
  }, [])

  const toggleFullscreen = React.useCallback(() => {
    if (isClosing) return
    if (isOpen) {
      closeFullscreen()
    } else {
      openFullscreen()
    }
  }, [isClosing, isOpen, closeFullscreen, openFullscreen])

  React.useEffect(() => {
    if (!isActive) return

    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        closeFullscreen()
      }
    }

    document.addEventListener('keydown', onKeyDown)
    return () => document.removeEventListener('keydown', onKeyDown)
  }, [isActive, closeFullscreen])

  React.useEffect(() => () => clearTimeout(timerRef.current), [])

  return {
    isFullscreen: isActive,
    reservedHeight,
    fullscreenRef,
    spacerRef,
    toggleFullscreen,
    closeFullscreen
  }
}
