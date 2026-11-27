import { isValidElement } from 'react'
import { renderToStaticMarkup } from 'react-dom/server'
import { describe, expect, it, vi } from 'vitest'

import { FileDeleteDialog } from '@/routes/(main)/(app)/system/file/-components/file-delete-dialog'

vi.mock('@/hooks/use-last-record', () => ({ useLastRecord: <T>(v: T) => v }))
vi.mock('@/lib/api/file', () => ({ deleteFiles: vi.fn() }))
vi.mock('@/components/delete-confirm-dialog', () => ({
  DeleteConfirmDialog: (props: { description?: unknown }) => {
    if (!isValidElement(props.description)) throw new Error('not element')
    return props.description as never
  }
}))

describe('file-delete-dialog', () => {
  it('长无空格文件名在描述中可换行且不截断', () => {
    const file = {
      id: 1n,
      type: 1,
      originalName: '3f2d05fc151e4dd389d47b9d1180e3ae.mp4'
    } as never
    const html = renderToStaticMarkup(
      FileDeleteDialog({ file, open: true, onClose: () => {}, onCompleted: () => {} }) as never
    )
    expect(html).toContain('min-w-0')
    expect(html).toContain('break-all')
    expect(html).toContain('3f2d05fc151e4dd389d47b9d1180e3ae.mp4')
    expect(html).toContain('是否确定删除文件「')
  })
})
