import { getRaw } from '@/lib/http'

/** Statistics period of the two charts; the Hub owns the fixed windows. */
export type WorkbenchRange = '30d' | '6m'

/** `fresh` within the refresh window, `stale` an older snapshot, `unavailable` no snapshot. */
export type WorkbenchFreshness = 'fresh' | 'stale' | 'unavailable'

/** Fields shared by the two chart responses. */
export interface WorkbenchChartMeta {
  /** Echo of the requested range. */
  range: WorkbenchRange
  /** Time zone the buckets are sliced in; the Hub always answers `Asia/Shanghai`. */
  window: string
  granularity: 'day' | 'month'
  /** Inclusive window start, `YYYY-MM-DD`. */
  start: string
  /** Exclusive window end; today and the current month are left out. */
  endExclusive: string
  /** Snapshot build time, not the request time; null when no snapshot exists. */
  updatedAt: string | null
  freshness: WorkbenchFreshness
  /** `periodStart` of buckets whose collection is incomplete or missing entirely. */
  partialBuckets: string[]
}

export interface WorkbenchAuthor {
  /** Stable platform identity; the leaderboard binds to this, never to the display name. */
  authorId: string
  displayName: string
  /** Path relative to the Hub web root, or an absolute URL; null when the platform has none. */
  avatarUrl: string | null
  count: number
}

export interface WorkbenchCommitBucket {
  periodStart: string
  /** null when the bucket has no usable detail, [] when it is complete with no commits. */
  authors: WorkbenchAuthor[] | null
}

export interface WorkbenchCommitsResp extends WorkbenchChartMeta {
  buckets: WorkbenchCommitBucket[]
}

export interface WorkbenchStarBucket {
  periodStart: string
  /** Sum of each repository's star count at the end of the period; null when unmeasured. */
  totalStars: number | null
}

export interface WorkbenchStarsResp extends WorkbenchChartMeta {
  /** Latest complete observation, independent of the trend window; older Hub responses may omit it. */
  latest?: { totalStars: number; sampledAt: string } | null
  buckets: WorkbenchStarBucket[]
}

/** One formal GitHub release of continew-admin. */
export interface WorkbenchRelease {
  tagName: string
  title: string
  publishedAt: string
  htmlUrl: string
}

export interface WorkbenchReleasesResp {
  updatedAt: string | null
  freshness: WorkbenchFreshness
  /** null when no snapshot exists yet, [] when a complete query matched nothing. */
  items: WorkbenchRelease[] | null
}

/** Body plus the Hub's `X-Data-Source` hint (`mock` while it serves fixtures). */
export interface HubResponse<T> {
  data: T
  source: string | null
}

/** Hub web root: a same-origin path or an absolute URL. Empty disables Hub requests. */
export const WORKBENCH_HUB_BASE = (import.meta.env.VITE_WORKBENCH_HUB_URL ?? '').trim().replace(/\/+$/, '')

export const isWorkbenchEnabled = WORKBENCH_HUB_BASE.length > 0

const COMMITS_PATH = '/api/workbench/commits'
const STARS_PATH = '/api/workbench/stars'
const RELEASES_PATH = '/api/workbench/releases'

/** A response the Hub answered but did not shape as documented; the panel then shows its error state. */
export class WorkbenchShapeError extends Error {
  constructor(path: string) {
    super(`工作台数据格式异常（${path}）`)
    this.name = 'WorkbenchShapeError'
  }
}

/**
 * The Hub payload is JSON from another service, so the declared generic is not evidence: an error
 * object with status 200 would be cached as a success and then crash the chart projections on a
 * missing `buckets`. Each endpoint's check lists the fields its projection reads.
 */
function assertShape<T>(path: string, body: T, isValid: (value: unknown) => boolean): T {
  if (!isValid(body)) {
    throw new WorkbenchShapeError(path)
  }

  return body
}

const isObject = (value: unknown): value is Record<string, unknown> =>
  typeof value === 'object' && value !== null && !Array.isArray(value)

/** A count or a star total is a non-negative integer; anything else would skew the sums. */
const isCount = (value: unknown) => typeof value === 'number' && Number.isSafeInteger(value) && value >= 0

/**
 * Bucket dates become chart timestamps; a malformed one yields NaN and breaks the X axis. The
 * round trip also rejects dates `Date.parse` silently normalizes, such as `2025-02-30`.
 */
const isDay = (value: unknown): value is string => {
  if (typeof value !== 'string' || !/^\d{4}-\d{2}-\d{2}$/.test(value)) {
    return false
  }

  const parsed = Date.parse(`${value}T00:00:00Z`)
  return !Number.isNaN(parsed) && new Date(parsed).toISOString().slice(0, 10) === value
}

/** An author entry is usable when the leaderboard can read every field it renders. */
const isAuthor = (value: unknown) =>
  isObject(value) &&
  typeof value.authorId === 'string' &&
  typeof value.displayName === 'string' &&
  (value.avatarUrl === null || typeof value.avatarUrl === 'string') &&
  isCount(value.count)

/** Commit bucket: `authors` is either the documented null or an author array. */
export const isCommitBucket = (value: unknown) =>
  isObject(value) &&
  isDay(value.periodStart) &&
  (value.authors === null || (Array.isArray(value.authors) && value.authors.every(isAuthor)))

/** Star bucket: `totalStars` is either the documented null or a non-negative integer. */
export const isStarBucket = (value: unknown) =>
  isObject(value) && isDay(value.periodStart) && (value.totalStars === null || isCount(value.totalStars))

/**
 * Chart body for the metric the caller asked for.
 *
 * Each bucket is checked field by field: `[null]`, an `authors: [null]`, or a missing `totalStars`
 * would otherwise pass the array check and crash the projection later, where an error state can no
 * longer be shown. `range` is compared with the request because the projection picks its axis
 * labels from it — a mismatched echo would label month buckets as days.
 */
export function chartBodyCheck(range: WorkbenchRange, isBucket: (value: unknown) => boolean) {
  return (body: unknown) => {
    if (!isObject(body)) {
      return false
    }

    const { buckets, partialBuckets } = body
    return (
      body.range === range &&
      (body.start === undefined || isDay(body.start)) &&
      Array.isArray(buckets) &&
      buckets.every(isBucket) &&
      Array.isArray(partialBuckets) &&
      partialBuckets.every(period => typeof period === 'string')
    )
  }
}

/** Star labels follow the returned bucket size, independently of the requested window. */
export function starsBodyCheck(range: WorkbenchRange) {
  const check = chartBodyCheck(range, isStarBucket)
  return (body: unknown) => {
    if (!isObject(body) || !check(body) || (body.granularity !== 'day' && body.granularity !== 'month')) {
      return false
    }

    const { latest } = body
    return (
      latest === undefined ||
      latest === null ||
      (isObject(latest) && isCount(latest.totalStars) && typeof latest.sampledAt === 'string')
    )
  }
}

/** Release body: `items` is a release array or the documented null; `updatedAt` is a string or null. */
export function isReleasesBody(body: unknown) {
  if (!isObject(body) || !('items' in body)) {
    return false
  }

  const { items, updatedAt } = body
  const itemsOk =
    items === null ||
    (Array.isArray(items) &&
      items.every(
        item =>
          isObject(item) &&
          typeof item.tagName === 'string' &&
          typeof item.title === 'string' &&
          typeof item.publishedAt === 'string' &&
          typeof item.htmlUrl === 'string'
      ))

  return itemsOk && (updatedAt === null || typeof updatedAt === 'string')
}

const request = async <T>(
  path: string,
  query: Record<string, string> | undefined,
  isValid: (value: unknown) => boolean,
  signal?: AbortSignal
): Promise<HubResponse<T>> => {
  // The header is read inside the response hook; a holder keeps its type from narrowing to null.
  const hint: { source: string | null } = { source: null }
  const data = await getRaw<T>(path, {
    baseUrl: WORKBENCH_HUB_BASE,
    query,
    signal,
    onResponse: response => {
      hint.source = response.headers.get('X-Data-Source')
    }
  })

  // The body arrives as `unknown` in truth: the generic is only the expected shape.
  return { data: assertShape(path, data, isValid), source: hint.source }
}

export function getWorkbenchCommits(range: WorkbenchRange, signal?: AbortSignal) {
  return request<WorkbenchCommitsResp>(COMMITS_PATH, { range }, chartBodyCheck(range, isCommitBucket), signal)
}

export function getWorkbenchStars(range: WorkbenchRange, signal?: AbortSignal) {
  return request<WorkbenchStarsResp>(
    STARS_PATH,
    range === '6m' ? { range, granularity: 'day' } : { range },
    starsBodyCheck(range),
    signal
  )
}

/** Releases do not follow the chart period, so this request carries no range. */
export function getWorkbenchReleases(signal?: AbortSignal) {
  return request<WorkbenchReleasesResp>(RELEASES_PATH, undefined, isReleasesBody, signal)
}

export { workbenchKeys } from '@/lib/query-keys'
