import { describe, expect, it } from 'vitest'

import { toggleCheckedKeys } from '@/lib/tree-check'
import { buildTreeIndex, type TreeNodeData } from '@/lib/tree-index'

const chain: TreeNodeData[] = [
  { id: 'R', label: '根', children: [{ id: 'P', label: '父', children: [{ id: 'L', label: '叶' }] }] }
]

const siblings: TreeNodeData[] = [
  {
    id: 'R',
    label: '根',
    children: [
      { id: 'P1', label: '父一', children: [{ id: 'L1', label: '叶一' }] },
      { id: 'P2', label: '父二', children: [{ id: 'L2', label: '叶二' }] }
    ]
  }
]

function toggle(
  tree: TreeNodeData[],
  checkedKeys: string[],
  nodeId: string,
  options: { checkStrictly?: boolean; disabledKeys?: string[] } = {}
): string[] {
  const checked = new Set(checkedKeys)

  return toggleCheckedKeys({
    index: buildTreeIndex(tree),
    nodeId,
    checked: checked.has(nodeId),
    checkedKeys: checked,
    checkStrictly: options.checkStrictly ?? false,
    disabledKeys: new Set(options.disabledKeys ?? [])
  }).sort()
}

describe('toggleCheckedKeys', () => {
  it('checks every ancestor of a leaf (regression: P1 tree cascade)', () => {
    expect(toggle(chain, [], 'L')).toEqual(['L', 'P', 'R'])
  })

  it('keeps an ancestor unchecked until all of its children are checked', () => {
    expect(toggle(siblings, [], 'L1')).toEqual(['L1', 'P1'])
    expect(toggle(siblings, ['L1', 'P1'], 'L2')).toEqual(['L1', 'L2', 'P1', 'P2', 'R'])
  })

  it('unchecks the ancestors when a checked leaf is unchecked', () => {
    expect(toggle(siblings, ['L1', 'L2', 'P1', 'P2', 'R'], 'L1')).toEqual(['L2', 'P2'])
  })

  it('checks all enabled descendants when a parent is checked', () => {
    expect(toggle(siblings, [], 'P1')).toEqual(['L1', 'P1'])
  })

  it('leaves disabled children unchecked and out of the parent completeness test', () => {
    const tree: TreeNodeData[] = [
      {
        id: 'R',
        label: '根',
        children: [
          { id: 'A', label: '禁用', disabled: true },
          { id: 'B', label: '可用' }
        ]
      }
    ]

    expect(toggle(tree, [], 'B', { disabledKeys: ['A'] })).toEqual(['B', 'R'])
    expect(toggle(tree, [], 'R', { disabledKeys: ['A'] })).toEqual(['B', 'R'])
  })

  it('only toggles the node itself in strict mode', () => {
    expect(toggle(chain, [], 'L', { checkStrictly: true })).toEqual(['L'])
  })
})
