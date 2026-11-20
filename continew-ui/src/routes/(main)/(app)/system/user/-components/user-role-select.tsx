import * as React from 'react'

import { useQuery } from '@tanstack/react-query'

import { QueryState } from '@/components/query-state'
import {
  Combobox,
  ComboboxChip,
  ComboboxChips,
  ComboboxChipsInput,
  ComboboxContent,
  ComboboxEmpty,
  ComboboxItem,
  ComboboxList,
  useComboboxAnchor
} from '@/components/ui/combobox'
import { Spinner } from '@/components/ui/spinner'
import { listRoleDict, roleKeys } from '@/lib/api/role'

interface UserRoleSelectProps {
  readonly value: string[]
  readonly onChange: (nextValue: string[]) => void
  readonly disabled?: boolean
  readonly invalid?: boolean
  /** Gates the query until the drawer or dialog hosting the picker is open. */
  readonly enabled?: boolean
}

interface RoleOption {
  value: string | number
  label: string
}

const EMPTY_OPTIONS: RoleOption[] = []

/**
 * Role multi-select over the shared role dictionary.
 *
 * Selected roles render as removable chips and typing filters the list, so a long role list
 * stays searchable without leaving the field.
 */
export function UserRoleSelect({
  value,
  onChange,
  disabled = false,
  invalid = false,
  enabled = true
}: UserRoleSelectProps) {
  const anchor = useComboboxAnchor()

  const roles = useQuery({
    queryKey: roleKeys.dicts(),
    queryFn: ({ signal }) => listRoleDict(signal),
    enabled
  })

  const options = roles.data ?? EMPTY_OPTIONS

  const selected = React.useMemo(
    () => value.flatMap(id => options.find(option => String(option.value) === id) ?? []),
    [value, options]
  )

  return (
    <Combobox
      multiple
      items={options}
      value={selected}
      onValueChange={next => onChange(next.map(option => String(option.value)))}
      itemToStringLabel={option => option.label}
      disabled={disabled}
    >
      <ComboboxChips ref={anchor}>
        {selected.map(option => (
          <ComboboxChip key={String(option.value)}>{option.label}</ComboboxChip>
        ))}
        <ComboboxChipsInput
          placeholder={selected.length === 0 ? '请选择角色' : ''}
          disabled={disabled}
          aria-invalid={invalid}
        />
      </ComboboxChips>

      <ComboboxContent anchor={anchor}>
        {roles.isPending ? (
          <div className="flex items-center justify-center p-2">
            <Spinner />
          </div>
        ) : roles.isError ? (
          <QueryState state="error" title="角色加载失败" onRetry={() => void roles.refetch()} className="p-2" />
        ) : (
          <>
            <ComboboxEmpty>暂无可选角色</ComboboxEmpty>
            <ComboboxList>
              {(option: RoleOption) => (
                <ComboboxItem key={String(option.value)} value={option}>
                  {option.label}
                </ComboboxItem>
              )}
            </ComboboxList>
          </>
        )}
      </ComboboxContent>
    </Combobox>
  )
}
