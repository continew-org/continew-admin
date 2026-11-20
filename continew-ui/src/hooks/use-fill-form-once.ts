import * as React from 'react'

interface FillFormOnceState<TRecord> {
  readonly isEdit: boolean
  readonly record?: TRecord
}

interface UseFillFormOnceOptions<TValues, TRecord> {
  readonly open: boolean
  readonly isEdit: boolean
  /** Identifies the record on edit, so switching records refills even while the drawer stays open. */
  readonly entityKey: string | number | null | undefined
  /** Distinguishes create defaults when a create screen has variants (such as storage type). */
  readonly createKey?: string | number
  readonly record?: TRecord
  /**
   * A version that changes when a revalidated copy of the same record arrives, such as
   * `detail.dataUpdatedAt`. Leave it out for row-snapshot forms that never revalidate.
   */
  readonly version?: number
  /** The values to write into the form for the current state. */
  readonly buildValues: (state: FillFormOnceState<TRecord>) => TValues
  /** Applies the values, normally `values => form.reset(values, { keepDefaultValues: true })`. */
  readonly apply: (values: TValues) => void
  /** Extra side effects after a reset, such as syncing local arrays or opening a section. */
  readonly onFilled?: (state: FillFormOnceState<TRecord>) => void
  /** Reset to the create values when the drawer closes. Defaults to true. */
  readonly resetOnClose?: boolean
  /** When false, hold the fill until the data it needs is ready (defaults to true). */
  readonly ready?: boolean
}

/**
 * Fills a drawer form once per opened record and again only when a revalidated copy arrives.
 *
 * `keepDefaultValues` is load-bearing in `apply`: `reset(values)` otherwise rewrites the form's own
 * defaults, and the next render would put the empty values back into the fields. The `version` key
 * lets a fresh detail response refresh the form on open without the "stale cache first" case, while
 * `entityKey` makes switching records refill even if the drawer stays mounted.
 */
export function useFillFormOnce<TValues, TRecord>(options: UseFillFormOnceOptions<TValues, TRecord>) {
  const latest = React.useRef(options)
  latest.current = options

  const filledForRef = React.useRef<string | null>(null)
  const { open, isEdit, entityKey, createKey, record, version, ready } = options

  React.useEffect(() => {
    const current = latest.current

    if (!current.open) {
      if (current.resetOnClose ?? true) {
        const state: FillFormOnceState<TRecord> = { isEdit: false }
        current.apply(current.buildValues(state))
        current.onFilled?.(state)
      }
      filledForRef.current = null
      return
    }

    if (current.ready === false) {
      return
    }

    if (current.isEdit && current.record === undefined) {
      return
    }

    const base = current.isEdit
      ? `edit:${current.entityKey}`
      : current.createKey === undefined
        ? 'create'
        : `create:${current.createKey}`
    const key = current.isEdit && current.version !== undefined ? `${base}:${current.version}` : base

    if (filledForRef.current === key) {
      return
    }

    filledForRef.current = key
    const state: FillFormOnceState<TRecord> = { isEdit: current.isEdit, record: current.record }
    current.apply(current.buildValues(state))
    current.onFilled?.(state)
  }, [open, isEdit, entityKey, createKey, record, version, ready])
}
