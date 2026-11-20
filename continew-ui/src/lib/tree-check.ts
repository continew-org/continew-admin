import type { TreeIndex, TreeKey } from './tree-index'

export interface ToggleCheckedKeysOptions<T = Record<string, unknown>> {
  readonly index: TreeIndex<T>
  readonly nodeId: TreeKey
  /** Whether the node is checked before the toggle. */
  readonly checked: boolean
  readonly checkedKeys: ReadonlySet<TreeKey>
  readonly checkStrictly: boolean
  readonly disabledKeys: ReadonlySet<TreeKey>
}

/**
 * Returns the checked keys after toggling one node.
 * Cascade mode checks the enabled descendants, then walks from the nearest parent up to the root
 * so each ancestor sees its children's new state.
 */
export function toggleCheckedKeys<T>({
  index,
  nodeId,
  checked,
  checkedKeys,
  checkStrictly,
  disabledKeys
}: ToggleCheckedKeysOptions<T>): TreeKey[] {
  const next = new Set(checkedKeys)

  if (checkStrictly) {
    if (checked) {
      next.delete(nodeId)
    } else {
      next.add(nodeId)
    }
    return Array.from(next)
  }

  // Locked nodes keep their checked state through every cascade path, so the relations handled
  // below exclude them.
  const descendants = (index.descendantsMap.get(nodeId) ?? []).filter(d => !disabledKeys.has(d))
  const ancestors = index.ancestorsMap.get(nodeId) ?? []

  if (checked) {
    next.delete(nodeId)
    for (const d of descendants) {
      next.delete(d)
    }
    for (const a of ancestors) {
      if (!disabledKeys.has(a)) {
        next.delete(a)
      }
    }
    return Array.from(next)
  }

  next.add(nodeId)
  for (const d of descendants) {
    next.add(d)
  }
  for (let i = ancestors.length - 1; i >= 0; i--) {
    const a = ancestors[i]
    if (disabledKeys.has(a)) {
      continue
    }

    const children = (index.directChildrenMap.get(a) ?? []).filter(c => !disabledKeys.has(c))
    const allChecked = children.every(c => next.has(c))
    if (allChecked) {
      next.add(a)
    }
  }

  return Array.from(next)
}
