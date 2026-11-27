import { describe, expect, it, vi } from 'vitest'

import { http } from '@/lib/http'
import { cancelMultipartUpload, completeMultipartUpload, uploadMultipartPart } from '@/lib/api/file'

vi.mock('@/lib/http', () => ({
  http: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    patch: vi.fn(),
    del: vi.fn()
  }
}))

describe('multipart-upload api', () => {
  it('complete/cancel 的 uploadId 按路径段编码', () => {
    const uploadId = 'a/b+c='
    completeMultipartUpload(uploadId)
    cancelMultipartUpload(uploadId)

    const encoded = encodeURIComponent(uploadId)
    expect(vi.mocked(http.get)).toHaveBeenCalledWith(
      `/system/multipart-upload/complete/${encoded}`,
      expect.objectContaining({ signal: undefined })
    )
    expect(vi.mocked(http.get)).toHaveBeenCalledWith(
      `/system/multipart-upload/cancel/${encoded}`,
      expect.objectContaining({ silent: true })
    )
  })

  it('part 的 uploadId/path 走 query 由 http 层编码，不拼路径', () => {
    const chunk = new Blob(['x'])
    uploadMultipartPart('a/b', 2, '/docs/x.bin', chunk)

    const [path, body, options] = vi.mocked(http.post).mock.calls[0]
    expect(path).toBe('/system/multipart-upload/part')
    expect(body).toBeInstanceOf(FormData)
    expect(options?.query).toEqual({ uploadId: 'a/b', partNumber: 2, path: '/docs/x.bin' })
  })
})
