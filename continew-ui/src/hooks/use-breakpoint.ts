import { useEffect, useState } from 'react'

/**
 * The media query only supplies the change events; the value is read from `window.innerWidth`,
 * because media queries exclude the scrollbar while `innerWidth` includes it.
 */
export function useBreakpoint(px: number, mode: 'min' | 'max') {
  const [value, setValue] = useState<boolean | undefined>(undefined)

  useEffect(() => {
    const query = mode === 'min' ? `(min-width: ${px}px)` : `(max-width: ${px - 1}px)`
    const mql = window.matchMedia(query)
    const onChange = () => setValue(mode === 'min' ? window.innerWidth >= px : window.innerWidth < px)

    mql.addEventListener('change', onChange)
    onChange()

    return () => mql.removeEventListener('change', onChange)
  }, [px, mode])

  return !!value
}
