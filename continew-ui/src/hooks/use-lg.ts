import { useBreakpoint } from './use-breakpoint'

const LG_BREAKPOINT = 1024

export function useIsLg() {
  return useBreakpoint(LG_BREAKPOINT, 'min')
}
