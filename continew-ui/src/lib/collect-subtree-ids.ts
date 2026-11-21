/**
 * Collects the target node and all of its descendants, keyed as strings.
 * Used to forbid a node from being selected as its own child.
 */
export function collectSubtreeIds<T extends { id: string | number; children?: T[] }>(
  nodes: readonly T[],
  targetId: string | number
): Set<string> {
  const disabled = new Set<string>()
  const targetIdStr = String(targetId)

  function findAndMark(list: readonly T[], foundParent = false) {
    for (const node of list) {
      const shouldMark = foundParent || String(node.id) === targetIdStr

      if (shouldMark) {
        disabled.add(String(node.id))
      }

      if (node.children && node.children.length > 0) {
        findAndMark(node.children, shouldMark)
      }
    }
  }

  findAndMark(nodes)
  return disabled
}
