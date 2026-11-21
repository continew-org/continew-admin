import { Building2 } from 'lucide-react'

import type { TreeNodeData } from '@/components/ui/tree'
import type { DeptResp } from '@/lib/api/dept'

/** Transforms a department tree into generic tree-select nodes. */
export function toDeptNodeData(nodes: DeptResp[], disabledIds?: ReadonlySet<string>): TreeNodeData<DeptResp>[] {
  return nodes.map(node => {
    const id = String(node.id)

    return {
      id,
      parentId: node.parentId ? String(node.parentId) : null,
      label: node.name,
      icon: Building2,
      disabled: disabledIds?.has(id) || undefined,
      data: node,
      children: node.children && node.children.length > 0 ? toDeptNodeData(node.children, disabledIds) : undefined
    }
  })
}
