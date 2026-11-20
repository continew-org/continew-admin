import { useQuery } from '@tanstack/react-query'

import { listDict } from '@/lib/api/common'
import { dictKeys } from '@/lib/query-keys'

export interface DictOption {
  readonly value: string
  readonly label: string
}

/** One dictionary of enum labels, falling back to the built-in presets when it is empty. */
export function useDictOptions(code: string, presets: DictOption[]): DictOption[] {
  const { data = [] } = useQuery({
    queryKey: dictKeys.code(code),
    queryFn: ({ signal }) => listDict(code, signal)
  })

  return data.length > 0 ? data : presets
}

/** The code-to-label lookup the table cells and the drawers render with. */
export function toLabelMap(options: readonly DictOption[]): Record<string, string> {
  return Object.fromEntries(options.map(option => [option.value, option.label] as const))
}
