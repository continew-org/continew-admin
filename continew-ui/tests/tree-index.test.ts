import { describe, expect, it } from 'vitest'

import { buildTreeIndex, type TreeNodeData } from '@/lib/tree-index'

const tree: TreeNodeData[] = [
  {
    id: 'R',
    label: '根',
    children: [
      { id: 'P', label: '父', children: [{ id: 'L', label: '叶' }] },
      { id: 'Q', label: '父二', children: [{ id: 'M', label: '叶二' }] }
    ]
  }
]

describe('buildTreeIndex', () => {
  it('records parents and the root-to-parent ancestor order', () => {
    const index = buildTreeIndex(tree)

    expect(index.allKeys).toEqual(['R', 'P', 'L', 'Q', 'M'])
    expect(index.parentMap.get('R')).toBeNull()
    expect(index.parentMap.get('L')).toBe('P')
    expect(index.ancestorsMap.get('R')).toEqual([])
    expect(index.ancestorsMap.get('P')).toEqual(['R'])
    expect(index.ancestorsMap.get('L')).toEqual(['R', 'P'])
  })

  it('lists descendants in depth-first order', () => {
    const index = buildTreeIndex(tree)

    expect(index.descendantsMap.get('R')).toEqual(['P', 'L', 'Q', 'M'])
    expect(index.descendantsMap.get('P')).toEqual(['L'])
    expect(index.descendantsMap.get('L')).toEqual([])
  })

  it('skips descendants when cascade is off but still maps parents', () => {
    const index = buildTreeIndex(tree, { cascade: false })

    expect(index.descendantsMap.size).toBe(0)
    expect(index.ancestorsMap.get('L')).toEqual(['R', 'P'])
  })
})
