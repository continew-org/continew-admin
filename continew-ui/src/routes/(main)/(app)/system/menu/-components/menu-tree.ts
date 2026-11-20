import { FileText, Folder } from 'lucide-react'

import type { TreeNodeData } from '@/components/ui/tree'
import type { MenuResp } from '@/lib/api/menu'
import { resolveMenuIcon } from '@/navigation/menu-icons'

/** Projects the menu tree into tree-select nodes; buttons cannot own children, so they are skipped. */
export function toMenuNodeData(nodes: MenuResp[], disabledIds: ReadonlySet<string>): TreeNodeData<MenuResp>[] {
  return nodes
    .filter(node => node.type !== 3)
    .map(node => ({
      id: String(node.id),
      parentId: node.parentId ? String(node.parentId) : null,
      label: node.title,
      icon: node.icon ? resolveMenuIcon(node.icon) : node.type === 1 ? Folder : FileText,
      disabled: disabledIds.has(String(node.id)),
      data: node,
      children: node.children?.length ? toMenuNodeData(node.children, disabledIds) : undefined
    }))
}
