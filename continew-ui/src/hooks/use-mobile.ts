import { useBreakpoint } from './use-breakpoint'

const MOBILE_BREAKPOINT = 768

export function useIsMobile() {
  return useBreakpoint(MOBILE_BREAKPOINT, 'max')
}

/** Same range as `useIsMobile`, for decisions taken before the hook has read the media query. */
export const DESKTOP_MEDIA_QUERY = `(min-width: ${MOBILE_BREAKPOINT}px)`
