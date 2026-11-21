import type { ComponentType, ReactNode } from 'react'

/** Ids are strings: callers convert with `String(id)` so Snowflake ids keep their precision and map lookups match. */
export type TreeKey = string

export interface TreeNodeData<T = Record<string, unknown>> {
  id: TreeKey
  parentId?: TreeKey | null
  label: string
  disabled?: boolean
  icon?: ComponentType<{ className?: string }> | ReactNode
  tag?: ReactNode
  subtitle?: ReactNode
  searchTerms?: string
  children?: TreeNodeData<T>[]
  data?: T
}

export interface TreeIndex<T> {
  nodeMap: Map<TreeKey, TreeNodeData<T>>
  parentMap: Map<TreeKey, TreeKey | null>
  directChildrenMap: Map<TreeKey, TreeKey[]>
  descendantsMap: Map<TreeKey, TreeKey[]>
  ancestorsMap: Map<TreeKey, TreeKey[]>
  allKeys: TreeKey[]
}

export interface TreeIndexOptions {
  /** Build the descendant relations cascade checkboxes need; single-select trees can skip them. */
  cascade?: boolean
}

export function buildTreeIndex<T>(nodes: TreeNodeData<T>[], { cascade = true }: TreeIndexOptions = {}): TreeIndex<T> {
  const nodeMap = new Map<TreeKey, TreeNodeData<T>>()
  const parentMap = new Map<TreeKey, TreeKey | null>()
  const directChildrenMap = new Map<TreeKey, TreeKey[]>()
  const descendantsMap = new Map<TreeKey, TreeKey[]>()
  const ancestorsMap = new Map<TreeKey, TreeKey[]>()
  const allKeys: TreeKey[] = []

  function traverse(list: TreeNodeData<T>[], parentAncestors: TreeKey[], parentId: TreeKey | null) {
    for (const node of list) {
      nodeMap.set(node.id, node)
      parentMap.set(node.id, parentId)
      ancestorsMap.set(node.id, parentAncestors)
      allKeys.push(node.id)

      const children = node.children ?? []
      const childKeys = children.map(c => c.id)
      directChildrenMap.set(node.id, childKeys)

      if (children.length > 0) {
        traverse(children, [...parentAncestors, node.id], node.id)
      }
    }
  }

  traverse(nodes, [], null)

  if (cascade) {
    for (let i = allKeys.length - 1; i >= 0; i--) {
      const key = allKeys[i]
      const children = directChildrenMap.get(key) ?? []
      const descendants: TreeKey[] = []
      for (const childKey of children) {
        descendants.push(childKey)
        const childDescendants = descendantsMap.get(childKey)
        if (childDescendants) {
          for (let j = 0; j < childDescendants.length; j++) {
            descendants.push(childDescendants[j])
          }
        }
      }
      descendantsMap.set(key, descendants)
    }
  }

  return { nodeMap, parentMap, directChildrenMap, descendantsMap, ancestorsMap, allKeys }
}
