import { http } from '@/lib/http'
import type { PageResp } from '@/lib/api/types'

/** `JobStatusEnum`: 0 禁用, 1 启用. */
export type JobStatus = 0 | 1

/** `JobTriggerTypeEnum`: 2 固定时间, 3 CRON. */
export type JobTriggerType = 2 | 3

/** `JobTaskTypeEnum`: 1 集群, 2 广播, 3 静态切片. */
export type JobTaskType = 1 | 2 | 3

/** `JobRouteStrategyEnum`. */
export type JobRouteKey = 1 | 2 | 3 | 4

/** `JobBlockStrategyEnum`. */
export type JobBlockStrategy = 1 | 2 | 3

/** One row of `GET /schedule/job`; the API has no separate detail endpoint. */
export interface JobResp {
  id: string | number
  groupName: string
  jobName: string
  description: string | null
  triggerType: JobTriggerType
  /** Seconds for a fixed-time trigger, a Quartz expression for CRON. */
  triggerInterval: string
  executorType: number | null
  executorInfo: string
  taskType: JobTaskType
  argsStr: string | null
  argsType: string | null
  routeKey: JobRouteKey
  blockStrategy: JobBlockStrategy
  executorTimeout: number
  maxRetryTimes: number
  retryInterval: number
  parallelNum: number
  jobStatus: JobStatus
  nextTriggerAt: string | null
  createDt: string
  updateDt: string | null
}

export type JobQuery = {
  page: number
  size: number
  /** The API has no sort parameter; the backend decides the order. */
  groupName?: string
  jobName?: string
  jobStatus?: JobStatus
}

/** The body of `POST /schedule/job` and `PUT /schedule/job/{id}`. */
export type JobReq = {
  groupName: string
  jobName: string
  description?: string
  triggerType: JobTriggerType
  triggerInterval: string
  executorType?: number | null
  taskType: JobTaskType
  executorInfo: string
  argsStr?: string
  argsType?: number | null
  routeKey: JobRouteKey
  blockStrategy: JobBlockStrategy
  executorTimeout: number
  maxRetryTimes: number
  retryInterval: number
  parallelNum: number
  /** Kept on update so editing a job never flips its status. */
  jobStatus?: JobStatus
}

/** The body of `PATCH /schedule/job/{id}/status`. */
export type JobStatusReq = {
  jobStatus: JobStatus
}

export const JOB_STATUS_LABELS: Record<JobStatus, string> = {
  0: '禁用',
  1: '启用'
}

export const JOB_STATUS_OPTIONS = [
  { value: 'all', label: '全部' },
  { value: '1', label: '启用' },
  { value: '0', label: '禁用' }
]

export const JOB_TRIGGER_TYPE_LABELS: Record<JobTriggerType, string> = {
  2: '固定时间',
  3: 'CRON'
}

export const JOB_TRIGGER_TYPE_OPTIONS = [
  { value: '2', label: '固定时间' },
  { value: '3', label: 'CRON' }
]

export const JOB_TASK_TYPE_LABELS: Record<JobTaskType, string> = {
  1: '集群',
  2: '广播',
  3: '静态切片'
}

export const JOB_TASK_TYPE_OPTIONS = [
  { value: '1', label: '集群' },
  { value: '2', label: '广播' },
  { value: '3', label: '静态切片' }
]

export const JOB_ROUTE_KEY_LABELS: Record<JobRouteKey, string> = {
  1: '一致性哈希',
  2: '随机',
  3: 'LRU',
  4: '轮询'
}

export const JOB_ROUTE_KEY_OPTIONS = [
  { value: '1', label: '一致性哈希' },
  { value: '2', label: '随机' },
  { value: '3', label: 'LRU' },
  { value: '4', label: '轮询' }
]

export const JOB_BLOCK_STRATEGY_LABELS: Record<JobBlockStrategy, string> = {
  1: '丢弃',
  2: '覆盖',
  3: '并行'
}

export const JOB_BLOCK_STRATEGY_OPTIONS = [
  { value: '1', label: '丢弃' },
  { value: '2', label: '覆盖' },
  { value: '3', label: '并行' }
]

/** One page of jobs. */
export function listJobs(query: JobQuery, signal?: AbortSignal) {
  return http.get<PageResp<JobResp>>('/schedule/job', { query, signal })
}

export function createJob(body: JobReq) {
  return http.post<void>('/schedule/job', body)
}

export function updateJob(id: string | number, body: JobReq) {
  return http.put<void>(`/schedule/job/${id}`, body)
}

/** Enables or disables a job. */
export function updateJobStatus(id: string | number, body: JobStatusReq) {
  return http.patch<void>(`/schedule/job/${id}/status`, body)
}

export function deleteJob(id: string | number) {
  return http.del<void>(`/schedule/job/${id}`)
}

/** Runs a job once, outside its schedule. */
export function triggerJob(id: string | number) {
  return http.post<void>(`/schedule/job/trigger/${id}`)
}

/** The job groups the scheduler knows about. */
export function listJobGroups(signal?: AbortSignal) {
  return http.get<string[]>('/schedule/job/group', { signal })
}

export { jobKeys } from '@/lib/query-keys'
