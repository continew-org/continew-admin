import { http } from '@/lib/http'
import type { PageResp } from '@/lib/api/types'

/** `JobExecuteStatusEnum`. */
export type JobExecuteStatus = 1 | 2 | 3 | 4 | 5 | 6

/** One row of `GET /schedule/log`; the API has no detail endpoint. */
export interface JobLogResp {
  id: string | number
  groupName: string
  jobName: string
  jobId: string | number
  taskBatchStatus: JobExecuteStatus
  operationReason: number | null
  executorType: number | null
  executorInfo: string | null
  executionAt: string | null
  createDt: string
}

export type JobLogQuery = {
  page: number
  size: number
  /** The API has no sort parameter; the backend decides the order. */
  jobId?: string | number
  groupName?: string
  jobName?: string
  taskBatchStatus?: JobExecuteStatus
  /** `start,end`, both `yyyy-MM-dd HH:mm:ss`, for the `LocalDateTime[]` filter. */
  datetimeRange?: string
}

export const JOB_EXECUTE_STATUS_LABELS: Record<JobExecuteStatus, string> = {
  1: '待处理',
  2: '运行中',
  3: '成功',
  4: '已失败',
  5: '已停止',
  6: '已取消'
}

export const JOB_EXECUTE_STATUS_OPTIONS = [
  { value: 'all', label: '全部' },
  { value: '1', label: '待处理' },
  { value: '2', label: '运行中' },
  { value: '3', label: '成功' },
  { value: '4', label: '已失败' },
  { value: '5', label: '已停止' },
  { value: '6', label: '已取消' }
]

export const JOB_EXECUTE_REASON_LABELS: Record<number, string> = {
  0: '无',
  1: '任务执行超时',
  2: '无客户端节点',
  3: '任务已关闭',
  4: '任务丢弃',
  5: '任务被覆盖',
  6: '无可执行任务项',
  7: '任务执行期间发生非预期异常',
  8: '手动停止',
  9: '条件节点执行异常',
  10: '任务中断',
  11: '回调节点执行异常',
  12: '无需处理',
  13: '节点关闭跳过执行',
  14: '判定未通过',
  15: '任务已完成',
  16: '任务正在执行',
  17: '任务等待执行',
  18: '任务执行失败',
  19: '任务执行成功'
}

/** One page of job logs. */
export function listJobLogs(query: JobLogQuery, signal?: AbortSignal) {
  return http.get<PageResp<JobLogResp>>('/schedule/log', { query, signal })
}

/** Stops one running job batch. */
export function stopJobLog(id: string | number) {
  return http.post<void>(`/schedule/log/stop/${id}`)
}

/** Retries one finished job batch. */
export function retryJobLog(id: string | number) {
  return http.post<void>(`/schedule/log/retry/${id}`)
}

export { jobLogKeys } from '@/lib/query-keys'
