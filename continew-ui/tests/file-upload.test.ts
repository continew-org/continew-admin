import { createHash } from 'node:crypto'

import { describe, expect, it, vi } from 'vitest'

import {
  cancelMultipartUpload,
  completeMultipartUpload,
  initMultipartUpload,
  uploadFile,
  uploadMultipartPart
} from '@/lib/api/file'
import {
  buildPartSlices,
  classifyUploadFailure,
  computeFileMd5,
  shouldUseMultipart,
  uploadManagedFile,
  type ManagedUploadProgress
} from '@/lib/file-upload'
import { ApiError } from '@/lib/http'
import type { FileUploadConfigResp, MultipartUploadCreateResp } from '@/lib/api/file'

vi.mock('@/lib/api/file', () => ({
  uploadFile: vi.fn(),
  initMultipartUpload: vi.fn(),
  uploadMultipartPart: vi.fn(),
  completeMultipartUpload: vi.fn(),
  cancelMultipartUpload: vi.fn()
}))

const config = (overrides: Partial<FileUploadConfigResp> = {}): FileUploadConfigResp => ({
  storageId: 1,
  storageName: '本地存储',
  storageCode: 'local',
  storageType: 1,
  multipartUploadThreshold: 10,
  multipartUploadPartSize: 4,
  multipartTempDir: null,
  ...overrides
})

const session = (overrides: Partial<MultipartUploadCreateResp> = {}): MultipartUploadCreateResp => ({
  fileId: 'f1',
  uploadId: 'u1',
  bucket: 'b',
  platform: 'local',
  fileName: 'x.bin',
  originalFileName: 'a.bin',
  fileMd5: 'md5',
  fileSize: 11,
  extension: 'bin',
  contentType: 'application/octet-stream',
  type: 'other',
  parentPath: '/docs',
  path: '/docs/x.bin',
  partSize: 4,
  uploadedPartNumbers: [],
  ...overrides
})

const fileOf = (size: number, name = 'a.bin') => new File([new Uint8Array(size)], name)

describe('shouldUseMultipart', () => {
  it('只在文件大小达到阈值时使用分片上传', () => {
    expect(shouldUseMultipart(9, 10)).toBe(false)
    expect(shouldUseMultipart(10, 10)).toBe(true)
    expect(shouldUseMultipart(11, 10)).toBe(true)
    expect(shouldUseMultipart(0, 10)).toBe(false)
    expect(shouldUseMultipart(100, 0)).toBe(false)
  })
})

describe('buildPartSlices', () => {
  it('整除时末片等于 partSize', () => {
    expect(buildPartSlices(8, 4)).toEqual([
      { partNumber: 1, start: 0, end: 4 },
      { partNumber: 2, start: 4, end: 8 }
    ])
  })

  it('有余数时末片取剩余字节', () => {
    expect(buildPartSlices(11, 4)).toEqual([
      { partNumber: 1, start: 0, end: 4 },
      { partNumber: 2, start: 4, end: 8 },
      { partNumber: 3, start: 8, end: 11 }
    ])
  })

  it('partSize 大于文件大小时只有一片', () => {
    expect(buildPartSlices(3, 4)).toEqual([{ partNumber: 1, start: 0, end: 3 }])
  })
})

describe('computeFileMd5', () => {
  it('分块计算的 MD5 与整体一致', async () => {
    const bytes = new Uint8Array(9 * 1024 * 1024).map((_, i) => i % 251)
    const file = new File([bytes], 'big.bin')
    const expected = createHash('md5').update(bytes).digest('hex')
    expect(await computeFileMd5(file)).toBe(expected)
  })
})

describe('uploadManagedFile', () => {
  it('小于阈值走普通上传，不初始化分片', async () => {
    vi.mocked(uploadFile).mockResolvedValue({ id: '1', url: '/u', thUrl: null, metadata: null })
    const file = fileOf(9)

    await uploadManagedFile({ file, parentPath: '/docs', config: config() })

    expect(uploadFile).toHaveBeenCalledWith(file, '/docs', undefined)
    expect(initMultipartUpload).not.toHaveBeenCalled()
  })

  it('达到阈值按会话 partSize 顺序上传全部分片后 complete', async () => {
    vi.mocked(initMultipartUpload).mockResolvedValue(session())
    vi.mocked(uploadMultipartPart).mockResolvedValue({
      partNumber: 1,
      partETag: 'e',
      partSize: 4,
      success: true,
      errorMessage: null
    })
    const file = fileOf(11)
    const progress: number[] = []

    await uploadManagedFile({
      file,
      parentPath: '/docs',
      config: config(),
      onProgress: (p: ManagedUploadProgress) => progress.push(p.uploadedBytes)
    })

    expect(initMultipartUpload).toHaveBeenCalledWith(
      expect.objectContaining({ fileName: 'a.bin', fileSize: 11, parentPath: '/docs' }),
      undefined
    )
    const calls = vi.mocked(uploadMultipartPart).mock.calls
    expect(calls.map(call => call[1])).toEqual([1, 2, 3])
    expect(calls[0][2]).toBe('/docs/x.bin')
    expect(calls.map(call => call[3].size)).toEqual([4, 4, 3])
    expect(completeMultipartUpload).toHaveBeenCalledWith('u1', undefined)
    expect(progress.at(-1)).toBe(11)
  })

  it('init 命中续传会话时跳过已传分片', async () => {
    vi.mocked(initMultipartUpload).mockResolvedValue(session({ uploadedPartNumbers: [1, 3] }))
    vi.mocked(uploadMultipartPart).mockResolvedValue({
      partNumber: 2,
      partETag: 'e',
      partSize: 4,
      success: true,
      errorMessage: null
    })
    const progress: number[] = []

    await uploadManagedFile({
      file: fileOf(11),
      parentPath: '/',
      config: config(),
      onProgress: (p: ManagedUploadProgress) => progress.push(p.uploadedBytes)
    })

    const calls = vi.mocked(uploadMultipartPart).mock.calls
    expect(calls.map(call => call[1])).toEqual([2])
    // progress[0] 是哈希阶段的 0，已传分片计入首个 uploading 进度
    expect(progress[1]).toBe(7)
    expect(progress.at(-1)).toBe(11)
  })

  it('分片失败保留会话（不调用 cancel）供重试续传', async () => {
    vi.mocked(initMultipartUpload).mockResolvedValue(session())
    vi.mocked(uploadMultipartPart).mockRejectedValue(new Error('网络错误'))

    await expect(uploadManagedFile({ file: fileOf(11), parentPath: '/', config: config() })).rejects.toThrow('网络错误')
    expect(cancelMultipartUpload).not.toHaveBeenCalled()
  })

  it('服务端返回 success=false 同样视为失败', async () => {
    vi.mocked(initMultipartUpload).mockResolvedValue(session())
    vi.mocked(uploadMultipartPart).mockResolvedValue({
      partNumber: 1,
      partETag: null,
      partSize: 0,
      success: false,
      errorMessage: '分片大小不匹配'
    })

    await expect(uploadManagedFile({ file: fileOf(11), parentPath: '/', config: config() })).rejects.toThrow(
      '分片大小不匹配'
    )
    expect(completeMultipartUpload).not.toHaveBeenCalled()
  })

  it('用户取消时清理服务端分片并向上抛错', async () => {
    vi.mocked(initMultipartUpload).mockResolvedValue(session())
    const controller = new AbortController()
    vi.mocked(uploadMultipartPart).mockImplementation(async () => {
      controller.abort()
      throw new Error('aborted')
    })
    vi.mocked(cancelMultipartUpload).mockResolvedValue(undefined)

    await expect(
      uploadManagedFile({ file: fileOf(11), parentPath: '/', config: config(), signal: controller.signal })
    ).rejects.toThrow('aborted')
    expect(cancelMultipartUpload).toHaveBeenCalledWith('u1')
  })

  it('取消后不再发起新的直传请求', async () => {
    const controller = new AbortController()
    controller.abort()

    await expect(
      uploadManagedFile({ file: fileOf(9), parentPath: '/', config: config(), signal: controller.signal })
    ).rejects.toThrow()
    expect(uploadFile).not.toHaveBeenCalled()
    expect(initMultipartUpload).not.toHaveBeenCalled()
  })

  it('MD5 计算期间取消停止后续读取且不发起 init', async () => {
    const controller = new AbortController()
    // MD5 按 4MB 分块读取，文件需产生多个块才能验证中途停止
    const file = fileOf(9 * 1024 * 1024)
    const originalSlice = file.slice.bind(file)
    let reads = 0
    vi.spyOn(file, 'slice').mockImplementation((start?: number, end?: number) => {
      reads += 1
      if (reads === 2) {
        controller.abort()
      }
      return originalSlice(start, end)
    })

    await expect(
      uploadManagedFile({ file, parentPath: '/', config: config(), signal: controller.signal })
    ).rejects.toThrow()
    expect(reads).toBe(2)
    expect(initMultipartUpload).not.toHaveBeenCalled()
  })

  it('complete 前上报 completing 阶段，complete 失败也会保留会话', async () => {
    vi.mocked(initMultipartUpload).mockResolvedValue(session())
    vi.mocked(uploadMultipartPart).mockResolvedValue({
      partNumber: 1,
      partETag: 'e',
      partSize: 4,
      success: true,
      errorMessage: null
    })
    vi.mocked(completeMultipartUpload).mockRejectedValue(new Error('timeout'))
    const phases: ManagedUploadProgress['phase'][] = []

    await expect(
      uploadManagedFile({
        file: fileOf(11),
        parentPath: '/',
        config: config(),
        onProgress: (p: ManagedUploadProgress) => phases.push(p.phase)
      })
    ).rejects.toThrow('timeout')
    expect(phases).toContain('completing')
    // 合并结果未确认不是可重传的普通失败，也不能当成用户取消去清理
    expect(cancelMultipartUpload).not.toHaveBeenCalled()
  })

  it('complete 发出后被取消也不得调用后端 cancel', async () => {
    vi.mocked(initMultipartUpload).mockResolvedValue(session())
    vi.mocked(uploadMultipartPart).mockResolvedValue({
      partNumber: 1,
      partETag: 'e',
      partSize: 4,
      success: true,
      errorMessage: null
    })
    const controller = new AbortController()
    // 合并请求已在途时取消：HTTP abort 不代表服务端停止，cancel 与合并会竞争
    vi.mocked(completeMultipartUpload).mockImplementation(async () => {
      controller.abort()
      throw controller.signal.reason
    })

    await expect(
      uploadManagedFile({ file: fileOf(11), parentPath: '/', config: config(), signal: controller.signal })
    ).rejects.toThrow()
    expect(cancelMultipartUpload).not.toHaveBeenCalled()
  })

  it('直传请求在途时被取消：错误照常上抛且不涉及分片清理', async () => {
    const controller = new AbortController()
    vi.mocked(uploadFile).mockImplementation(async () => {
      controller.abort()
      throw controller.signal.reason
    })

    await expect(
      uploadManagedFile({ file: fileOf(9), parentPath: '/', config: config(), signal: controller.signal })
    ).rejects.toThrow()
    expect(cancelMultipartUpload).not.toHaveBeenCalled()
  })
})

describe('classifyUploadFailure', () => {
  const apiError = (status?: number) => new ApiError('m', status === undefined ? {} : { status })

  it('分片/init/哈希阶段失败均可重试，不算未确认', () => {
    // success:false、init 拒绝、MD5 读取失败、分片取消：均未提交最终对象
    expect(classifyUploadFailure('uploading', false, new Error('分片大小不匹配'))).toEqual({
      uncertain: false,
      aborted: false
    })
    expect(classifyUploadFailure('hashing', true, new Error('aborted'))).toEqual({ uncertain: false, aborted: true })
    expect(classifyUploadFailure('uploading', true, new Error('aborted'))).toEqual({ uncertain: false, aborted: true })
    expect(classifyUploadFailure(null, false, apiError())).toEqual({ uncertain: false, aborted: false })
  })

  it('直传已发出后仅确定拒绝可重试，其余均结果未确认', () => {
    // 带业务码的 5xx/408 仍按未确认：HTTP 状态优先于 R.code
    expect(classifyUploadFailure('direct', false, new ApiError('m', { code: 'BIZ', status: 500 }))).toEqual({
      uncertain: true,
      aborted: false
    })
    expect(classifyUploadFailure('direct', false, new ApiError('m', { code: 'BIZ', status: 408 }))).toEqual({
      uncertain: true,
      aborted: false
    })
    // HTTP 200 无业务码是响应不可读，未确认；有业务码才确定业务失败
    expect(classifyUploadFailure('direct', false, apiError(200))).toEqual({ uncertain: true, aborted: false })
    expect(classifyUploadFailure('direct', false, new ApiError('m', { code: 'BAD_PARAM', status: 200 }))).toEqual({
      uncertain: false,
      aborted: false
    })
    // 其余 4xx 可确定拒绝
    expect(classifyUploadFailure('direct', false, apiError(413))).toEqual({ uncertain: false, aborted: false })
    expect(classifyUploadFailure('direct', false, apiError())).toEqual({ uncertain: true, aborted: false })
    expect(classifyUploadFailure('direct', true, new Error('aborted'))).toEqual({ uncertain: true, aborted: true })
  })

  it('合并发出后任何失败（含取消）都未确认', () => {
    expect(classifyUploadFailure('completing', false, apiError(500))).toEqual({ uncertain: true, aborted: false })
    expect(classifyUploadFailure('completing', true, new Error('aborted'))).toEqual({ uncertain: true, aborted: true })
  })
})
