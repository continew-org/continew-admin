import * as React from 'react'

import { cn } from 'cn'

import { Input } from '@/components/ui/input'
import { Switch } from '@/components/ui/switch'
import { ToggleGroup, ToggleGroupItem } from '@/components/ui/toggle-group'
import { PREFERENCE_REGISTRY, type PreferenceKey, type PreferenceValueMap } from '@/lib/preferences/preferences-config'
import { usePreferencesStore, usePreferencesStoreApi } from '@/stores/preferences/preferences-provider'

type KeysOf<T> = { [K in PreferenceKey]: PreferenceValueMap[K] extends T ? K : never }[PreferenceKey]

/** Subscribes to one preference and returns a stable setter; each control re-renders on its own key only. */
export function usePreferenceField<K extends PreferenceKey>(key: K) {
  const value = usePreferencesStore(state => state.values[key])
  const store = usePreferencesStoreApi()
  const setValue = React.useCallback(
    (next: PreferenceValueMap[K]) => store.getState().setPreference(key, next),
    [store, key]
  )
  return [value, setValue] as const
}

export function PreferenceBlock({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <section className="space-y-1 pt-5 first:pt-1">
      <h3 className="pb-1.5 text-sm font-semibold">{title}</h3>
      <div className="space-y-0.5">{children}</div>
    </section>
  )
}

function Row({
  label,
  hint,
  disabled,
  children
}: {
  label: string
  hint?: string
  disabled?: boolean
  children: React.ReactNode
}) {
  return (
    <div
      className={cn(
        'flex min-h-10 items-center justify-between gap-4 rounded-md px-2 py-1.5 transition-colors hover:bg-accent/50',
        disabled && 'pointer-events-none opacity-50'
      )}
    >
      <div className="min-w-0">
        <div className="text-sm">{label}</div>
        {hint && <div className="text-xs text-muted-foreground">{hint}</div>}
      </div>
      <div className="shrink-0">{children}</div>
    </div>
  )
}

export function PreferenceSwitch({
  field,
  label,
  hint,
  disabled
}: {
  field: KeysOf<boolean>
  label: string
  hint?: string
  disabled?: boolean
}) {
  const [value, setValue] = usePreferenceField(field)

  return (
    <Row label={label} hint={hint} disabled={disabled}>
      <Switch checked={value as boolean} onCheckedChange={checked => setValue(checked as never)} disabled={disabled} />
    </Row>
  )
}

export function PreferenceSegmented<K extends KeysOf<string>>({
  field,
  label,
  options,
  disabled
}: {
  field: K
  label: string
  options: readonly { label: string; value: PreferenceValueMap[K] }[]
  disabled?: boolean
}) {
  const [value, setValue] = usePreferenceField(field)

  return (
    <Row label={label} disabled={disabled}>
      <ToggleGroup
        size="sm"
        spacing={0}
        variant="outline"
        value={[value as string]}
        onValueChange={([next]) => next && setValue(next as PreferenceValueMap[K])}
      >
        {options.map(option => (
          <ToggleGroupItem
            key={option.value}
            value={option.value}
            className="px-2.5 text-xs aria-pressed:bg-primary aria-pressed:text-primary-foreground"
          >
            {option.label}
          </ToggleGroupItem>
        ))}
      </ToggleGroup>
    </Row>
  )
}

/**
 * Number input that saves on blur or Enter, so typing "2" on the way to "240" does not
 * reflow the layout for every keystroke.
 */
export function PreferenceNumber({
  field,
  label,
  disabled,
  suffix = 'px'
}: {
  field: KeysOf<number>
  label: string
  disabled?: boolean
  suffix?: string
}) {
  const [value, setValue] = usePreferenceField(field)
  const { min, max } = PREFERENCE_REGISTRY[field] as { min: number; max: number }
  const [draft, setDraft] = React.useState(String(value))
  const [synced, setSynced] = React.useState(value)
  if (synced !== value) {
    setSynced(value)
    setDraft(String(value))
  }

  const commit = () => {
    const next = Math.min(Math.max(Math.round(Number(draft) || min), min), max)
    setDraft(String(next))
    setValue(next as never)
  }

  return (
    <Row label={label} disabled={disabled}>
      <div className="flex items-center gap-1.5">
        <Input
          type="number"
          inputMode="numeric"
          min={min}
          max={max}
          value={draft}
          disabled={disabled}
          onChange={event => setDraft(event.target.value)}
          onBlur={commit}
          onKeyDown={event => event.key === 'Enter' && commit()}
          className="h-8 w-20 text-right tabular-nums"
        />
        {suffix && <span className="w-5 text-xs text-muted-foreground">{suffix}</span>}
      </div>
    </Row>
  )
}

export function PreferenceText({
  field,
  label,
  placeholder,
  disabled
}: {
  field: KeysOf<string>
  label: string
  placeholder?: string
  disabled?: boolean
}) {
  const [value, setValue] = usePreferenceField(field)
  const [draft, setDraft] = React.useState(value as string)
  const [synced, setSynced] = React.useState(value)
  if (synced !== value) {
    setSynced(value)
    setDraft(value as string)
  }

  return (
    <Row label={label} disabled={disabled}>
      <Input
        value={draft}
        placeholder={placeholder}
        disabled={disabled}
        maxLength={64}
        onChange={event => setDraft(event.target.value)}
        onBlur={() => setValue(draft.trim() as never)}
        onKeyDown={event => event.key === 'Enter' && setValue(draft.trim() as never)}
        className="h-8 w-40"
      />
    </Row>
  )
}

/** Image-or-swatch choice tile used by the layout, theme and transition pickers. */
export function ChoiceTile({
  active,
  label,
  title,
  onClick,
  children,
  className
}: {
  active: boolean
  label: string
  title?: string
  onClick: () => void
  children: React.ReactNode
  className?: string
}) {
  return (
    <button
      type="button"
      title={title}
      aria-pressed={active}
      onClick={onClick}
      className={cn('group flex min-w-0 flex-col items-center gap-1.5 text-xs text-muted-foreground', className)}
    >
      <span
        className={cn(
          'relative flex w-full items-center justify-center overflow-hidden rounded-md border-2 bg-background transition-all',
          active ? 'border-primary shadow-sm' : 'border-border group-hover:border-primary/50'
        )}
      >
        {children}
      </span>
      <span className={cn('w-full truncate text-center', active && 'font-medium text-primary')}>{label}</span>
    </button>
  )
}
