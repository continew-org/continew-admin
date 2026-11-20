import { useBreakpoint } from './use-breakpoint'

const MOBILE_BREAKPOINT = 768

export function useIsMobile() {
  return useBreakpoint(MOBILE_BREAKPOINT, 'max')
}
