import { describe, expect, it, vi } from 'vitest'

import { ApiError } from '@/lib/http'
import { uploadFile } from '@/lib/api/file'
import { classifyUploadFailure } from '@/lib/file-upload'

describe('uploadFile 真实 http 响应分类', () => {
  it('HTTP 200 响应体截断时归入结果未确认', async () => {
    vi.stubGlobal('window', {
      localStorage: { getItem: () => null, setItem: vi.fn(), removeItem: vi.fn() },
      sessionStorage: { getItem: () => null, setItem: vi.fn(), removeItem: vi.fn() }
    })
    vi.stubGlobal(
      'fetch',
      vi.fn(async () => new Response('{"success":true,"data":', { status: 200 }))
    )

    const reason = await uploadFile(new File(['x'], 'a.bin'), '/').then(
      () => {
        throw new Error('unexpected success')
      },
      error => error
    )

    expect(reason).toBeInstanceOf(ApiError)
    expect((reason as ApiError).status).toBe(200)
    expect((reason as ApiError).code).toBeUndefined()
    expect(classifyUploadFailure('direct', false, reason)).toEqual({ uncertain: true, aborted: false })
    vi.unstubAllGlobals()
  })
})
