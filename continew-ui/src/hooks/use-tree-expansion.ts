import * as React from 'react'

import type { TreeKey } from '@/lib/tree-index'

export interface TreeExpansionOptions {
  /** All node keys of the current data set. */
  readonly keys: TreeKey[]
  /** Expand every node once the first non-empty data set arrives, then only drop stale keys. */
  readonly autoExpandOnLoad?: boolean
}

export interface TreeExpansion {
  /** Undefined until the first expansion change, so an uncontrolled tree can pick its own initial state. */
  readonly expandedKeys: TreeKey[] | undefined
  readonly setExpandedKeys: React.Dispatch<React.SetStateAction<TreeKey[] | undefined>>
  readonly expandAll: () => void
  readonly collapseAll: () => void
}

/**
 * Expansion state shared by the tree shells.
 * `autoExpandOnLoad` matches the checkable tree: expand everything on the first non-empty load and
 * later only drop keys that no longer exist, so a refresh keeps the branches the user toggled.
 * Without it the state stays undefined until the first change, letting the inner tree expand the
 * selected path itself.
 */
export function useTreeExpansion({ keys, autoExpandOnLoad = false }: TreeExpansionOptions): TreeExpansion {
  const [expandedKeys, setExpandedKeys] = React.useState<TreeKey[] | undefined>(autoExpandOnLoad ? [] : undefined)
  const hasInitialized = React.useRef(false)

  React.useEffect(() => {
    if (!autoExpandOnLoad || keys.length === 0) {
      return
    }

    if (!hasInitialized.current) {
      hasInitialized.current = true
      setExpandedKeys(keys)
      return
    }

    const validKeys = new Set(keys)
    setExpandedKeys(previous => previous?.filter(key => validKeys.has(key)))
  }, [autoExpandOnLoad, keys])

  const expandAll = React.useCallback(() => {
    setExpandedKeys(keys)
  }, [keys])

  const collapseAll = React.useCallback(() => {
    setExpandedKeys([])
  }, [])

  return { expandedKeys, setExpandedKeys, expandAll, collapseAll }
}
