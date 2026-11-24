import SparkMD5 from 'spark-md5'

import {
  cancelMultipartUpload,
  completeMultipartUpload,
  initMultipartUpload,
  uploadFile,
  uploadMultipartPart
} from '@/lib/api/file'
import { ApiError } from '@/lib/http'
import type { FileUploadConfigResp } from '@/lib/api/file'

/** 计算文件 MD5 的分块大小；只影响哈希读取粒度，与后端分片无关。 */
const MD5_CHUNK_SIZE = 4 * 1024 * 1024

/** 上传失败归类：服务端明确拒绝的重试；已发出请求结果不确定，须按「可能已保存」处理。 */
export interface UploadFailureClassification {
  /** true：结果未确认（请求已发出但响应丢失/超时/合并发出后失败），不能进普通重试队列 */
  uncertain: boolean
  /** true：用户取消（区分取消前是否已发请求，决定是否可能已保存） */
  aborted: boolean
}

/**
 * 失败分类。
 *
 * - 'direct' 阶段出错：直传请求已发出——带业务码（R.code）的 ApiError 或确定性
 *   HTTP 4xx（408 除外）才算服务端明确拒绝；响应体无法解析的 2xx、408、5xx、
 *   网络/超时/中止都只能算结果未确认（响应可能在到达前已写入）。
 * - 'completing' 阶段出错：合并请求已发出，任何失败（含取消）都未确认，
 *   `uploadManagedFile` 已保证此阶段不发后端 cancel。
 * - 'hashing'/'uploading'/null：摘要、init、分片或预检失败，均未提交最终对象，可正常重试。
 */
export function classifyUploadFailure(
  lastPhase: ManagedUploadProgress['phase'] | null,
  aborted: boolean,
  reason: unknown
): UploadFailureClassification {
  if (lastPhase === 'direct') {
    // 明确拒绝才可普通重试。优先级：HTTP 状态先行——408/5xx 即便带业务码也可能已落库；
    // 其余 4xx 是确定拒绝；HTTP 200 要靠 R.code 才算确定业务失败（无码即响应不可读，归未确认）
    const definiteRejection =
      reason instanceof ApiError &&
      reason.status !== undefined &&
      (reason.status >= 500 || reason.status === 408 ? false : reason.status >= 400 || reason.code !== undefined)
    return { uncertain: !definiteRejection, aborted }
  }
  if (lastPhase === 'completing') {
    return { uncertain: true, aborted }
  }
  return { uncertain: false, aborted }
}

export interface ManagedUploadProgress {
  /** 'hashing'：计算文件 MD5；'direct'：直传请求已发出；'uploading'：传输分片；'completing'：合并已提交、等待结果 */
  phase: 'hashing' | 'direct' | 'uploading' | 'completing'
  /** 已确认上传的字节数（续传命中时已传分片计入） */
  uploadedBytes: number
  totalBytes: number
}

export interface ManagedUploadOptions {
  file: File
  parentPath: string
  /** 默认存储生效配置，`getDefaultUploadConfig` 返回值 */
  config: FileUploadConfigResp
  signal?: AbortSignal
  onProgress?: (progress: ManagedUploadProgress) => void
}

/** 达到或超过阈值走分片上传；阈值取自默认存储生效配置，后端低于此值的分片策略不会生效。 */
export function shouldUseMultipart(fileSize: number, threshold: number) {
  return threshold > 0 && fileSize >= threshold
}

/**
 * 按 partSize 切分出全部序号与字节区间（1 起编号）。最后一个分片取剩余字节，
 * 与后端 `validatePartSize` 期望（非末片恒等于 partSize、末片为余量）一致。
 */
export function buildPartSlices(fileSize: number, partSize: number) {
  const count = Math.max(1, Math.ceil(fileSize / partSize))
  return Array.from({ length: count }, (_, index) => {
    const start = index * partSize
    return { partNumber: index + 1, start, end: Math.min(start + partSize, fileSize) }
  })
}

/**
 * 分块读取文件计算 MD5；init 的 fileMd5 必填且用于断点续传会话匹配。
 * `spark.append` 为同步 CPU 密集计算，每块结束后让出宏任务并在下一块前复查 signal，
 * 保证取消信号能穿插执行；不迁入 Worker，避免新增 worker 打包与 SparkMD5 兼容问题。
 */
export async function computeFileMd5(file: File, signal?: AbortSignal) {
  const spark = new SparkMD5.ArrayBuffer()
  for (let offset = 0; offset < file.size; offset += MD5_CHUNK_SIZE) {
    signal?.throwIfAborted()
    spark.append(await file.slice(offset, offset + MD5_CHUNK_SIZE).arrayBuffer())
    // 让出主线程：渲染与 AbortSignal 处理插入到下一事件循环
    await new Promise<void>(resolve => setTimeout(resolve, 0))
  }
  return spark.end()
}

/**
 * 按默认存储阈值选择直传或分片上传完成一个文件。
 *
 * - 小于阈值走普通 `POST /system/file/upload`；
 * - 达到阈值先计算 MD5 再 init：后端按（存储、bucket、MD5、原文件名、目录）命中已有会话时
 *   返回同一 uploadId 与已传分片编号，未传分片继续上传即完成断点续传；
 * - 中途失败保留服务端会话，重试同一文件可续传；仅 `signal.aborted`（用户取消）时调用
 *   cancel 清理服务端分片，再向上抛错。
 * - 进入 'completing' 阶段后不再可回滚：即便 signal 中止也不调用 cancel，
 *   避免与正在执行的服务端合并竞争；complete 请求失败不代表未落库，
 *   错误照常上抛，由调用方按结果未确认提示，而不是按失败重传。
 */
export async function uploadManagedFile(options: ManagedUploadOptions) {
  const { file, parentPath, config, signal, onProgress } = options
  const report = (phase: ManagedUploadProgress['phase'], uploadedBytes: number) =>
    onProgress?.({ phase, uploadedBytes, totalBytes: file.size })

  if (!shouldUseMultipart(file.size, config.multipartUploadThreshold)) {
    // 先检查中止再进入 direct 阶段：请求未发出的取消不算「结果未确认」
    signal?.throwIfAborted()
    report('direct', 0)
    return uploadFile(file, parentPath, signal)
  }

  report('hashing', 0)
  const fileMd5 = await computeFileMd5(file, signal)
  signal?.throwIfAborted()

  const session = await initMultipartUpload(
    {
      fileName: file.name,
      fileSize: file.size,
      fileMd5,
      contentType: file.type || undefined,
      parentPath
    },
    signal
  )

  // partSize 以服务端会话为准（可能与配置的存储实例值不同源）；缺省回退配置值。
  // 二者均来自网络响应：非正整数视为后端契约违约，在发出任何分片前报错
  const partSize = session.partSize ?? config.multipartUploadPartSize
  if (!Number.isSafeInteger(partSize) || partSize <= 0) {
    throw new Error(`服务端返回的分片大小非法: ${partSize}`)
  }
  const parts = buildPartSlices(file.size, partSize)
  const done = new Set(session.uploadedPartNumbers ?? [])
  let uploadedBytes = parts
    .filter(part => done.has(part.partNumber))
    .reduce((sum, part) => sum + part.end - part.start, 0)

  let inCompleting = false
  report('uploading', uploadedBytes)
  try {
    for (const part of parts) {
      signal?.throwIfAborted()
      if (done.has(part.partNumber)) {
        continue
      }
      const resp = await uploadMultipartPart(
        session.uploadId,
        part.partNumber,
        session.path,
        file.slice(part.start, part.end),
        signal
      )
      if (!resp.success) {
        throw new Error(resp.errorMessage ?? `分片 ${part.partNumber} 上传失败`)
      }
      uploadedBytes += part.end - part.start
      report('uploading', uploadedBytes)
    }
    report('completing', uploadedBytes)
    inCompleting = true
    return await completeMultipartUpload(session.uploadId, signal)
  } catch (error) {
    // 合并请求一旦发出便不可安全回滚：cancel 与服务端正在进行的合并可能竞争，不再调用
    if (signal?.aborted && !inCompleting) {
      // 取消时清空服务端已传分片；失败则保留会话供重试续传
      await cancelMultipartUpload(session.uploadId).catch(() => {})
    }
    throw error
  }
}
