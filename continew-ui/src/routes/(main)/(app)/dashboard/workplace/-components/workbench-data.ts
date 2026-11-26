import type {
  WorkbenchAuthor,
  WorkbenchChartMeta,
  WorkbenchCommitBucket,
  WorkbenchRange,
  WorkbenchStarBucket,
  WorkbenchStarsResp
} from '@/lib/api/workbench'

import { dashboardChartColor } from '../../-chart-color'

/** The Hub groups commits it could not link to an account under this reserved author id. */
export const UNLINKED_AUTHOR_ID = 'unlinked'

/** One bar of the commit chart: the bucket's own authors plus whether they are only a subtotal. */
export interface CommitBar {
  periodStart: string
  /** `M/d` for day buckets, `M 月` for month buckets. */
  label: string
  /** Known commit count; 0 when the bucket carries no detail at all. */
  total: number
  /** Known authors of this bucket. */
  authors: WorkbenchAuthor[]
  /** The bucket is incomplete: `total` is a known subtotal, not the full count. */
  partial: boolean
}

export interface StarPoint {
  periodStart: string
  label: string
  /** null marks a gap; the line breaks there instead of interpolating. */
  totalStars: number | null
  /** The bucket is listed in `partialBuckets`, so a missing value is expected rather than an error. */
  partial: boolean
}

export interface Leaderboard {
  /** One row per `authorId`, highest count first. */
  rows: WorkbenchAuthor[]
  /** Sum of the rows' counts. */
  total: number
  /** Linked contributors only; the unlinked segment has no person behind it. */
  contributors: number
}

/** `YYYY-MM-DD` → `M/d`; slicing keeps this independent of the browser time zone. */
export function monthDay(periodStart: string) {
  return `${Number(periodStart.slice(5, 7))}/${Number(periodStart.slice(8, 10))}`
}

/** `YYYY-MM-DD` (a month bucket's first day) → `M 月`. */
export function monthLabel(periodStart: string) {
  return `${Number(periodStart.slice(5, 7))} 月`
}

export function bucketLabel(periodStart: string, range: WorkbenchRange) {
  return range === '6m' ? monthLabel(periodStart) : monthDay(periodStart)
}

/** Only a canonical Hub identity, never a display name or avatar URL, identifies a GitHub profile. */
export function githubProfileUrl(authorId: string): string | null {
  const login = /^github:([a-z0-9]+(?:-[a-z0-9]+)*)$/.exec(authorId)?.[1]
  return login && login.length <= 39 ? `https://github.com/${login}` : null
}

const PROTOCOL = /^[a-z][a-z0-9+.-]*:\/\//i

/**
 * Hub avatar paths are relative to the Hub web root, so they resolve against `hubBase` instead of
 * the Admin origin. Absolute http(s) URLs pass through. Anything else is treated as a Hub path:
 * leading slashes are dropped so a protocol-relative value cannot point at another host, and a
 * value the URL parser rejects yields null so the caller falls back to the initial.
 */
export function resolveAvatarUrl(avatarUrl: string | null | undefined, hubBase: string) {
  const value = avatarUrl?.trim()

  if (!value) {
    return null
  }

  if (/^https?:\/\//i.test(value)) {
    try {
      return new URL(value).toString()
    } catch {
      return null
    }
  }

  const base = hubBase.replace(/\/+$/, '')

  if (!base) {
    return null
  }

  // A path base (`/hub`) has no origin, so it is parsed against a stand-in and only the path kept.
  // Browsers treat a backslash as a path separator, so a value like `\evil.example/x` would resolve
  // to another host under an absolute base; such a value is treated as unusable.
  const relative = value.replace(/^\/+/, '')
  if (relative.includes('\\')) {
    return null
  }

  const isAbsoluteBase = PROTOCOL.test(base)
  const baseUrl = isAbsoluteBase ? base : `http://hub.local${base}`

  try {
    const resolved = new URL(relative, `${baseUrl}/`)
    if (resolved.origin !== new URL(baseUrl).origin) {
      return null
    }

    return isAbsoluteBase ? resolved.toString() : `${resolved.pathname}${resolved.search}`
  } catch {
    return null
  }
}

/** First character of the display name, shown when there is no avatar or it fails to load. */
export function authorInitial(displayName: string) {
  return Array.from(displayName.trim())[0] ?? ''
}

/** Sum of a bucket's known counts; a null detail contributes nothing. */
export function bucketTotal(authors: readonly WorkbenchAuthor[] | null) {
  return (authors ?? []).reduce((sum, author) => sum + author.count, 0)
}

/**
 * Projects the commit buckets for the chart, keeping the response order.
 *
 * `authors: []` is a complete bucket with no commits, so only a missing detail — a null array or
 * membership in `partialBuckets` — marks a bar as partial.
 */
export function toCommitBars(meta: WorkbenchChartMeta, buckets: readonly WorkbenchCommitBucket[]): CommitBar[] {
  return buckets.map(bucket => ({
    periodStart: bucket.periodStart,
    label: bucketLabel(bucket.periodStart, meta.range),
    total: bucketTotal(bucket.authors),
    authors: bucket.authors ?? [],
    partial: meta.partialBuckets.includes(bucket.periodStart) || bucket.authors === null
  }))
}

/** Projects the star buckets; nulls stay null so the line breaks instead of guessing a value. */
export function toStarPoints(meta: WorkbenchChartMeta, buckets: readonly WorkbenchStarBucket[]): StarPoint[] {
  return buckets.map(bucket => ({
    periodStart: bucket.periodStart,
    label: meta.granularity === 'month' ? `${bucket.periodStart.slice(0, 7)} 月末` : `${bucket.periodStart} 日末`,
    totalStars: bucket.totalStars,
    partial: meta.partialBuckets.includes(bucket.periodStart)
  }))
}

const DAY_MS = 86_400_000
const shanghaiOffset = 8 * 3_600_000

function starBucketEnd(date: string, monthly: boolean) {
  const end = new Date(`${date}T00:00:00+08:00`)
  if (monthly) return Date.UTC(Number(date.slice(0, 4)), Number(date.slice(5, 7)), 1) - shanghaiOffset
  return end.getTime() + DAY_MS
}

export function starTimeline(meta: WorkbenchStarsResp, points: readonly StarPoint[]) {
  const rows = points.map(point => ({
    ...point,
    time: starBucketEnd(point.periodStart, meta.granularity === 'month'),
    latestStars: null as number | null
  }))
  const latest = meta.latest
  const time = Date.parse(latest?.sampledAt ?? '')
  const windowStart = /^\d{4}-\d{2}-\d{2}$/.test(meta.start) ? Date.parse(`${meta.start}T00:00:00+08:00`) : NaN
  if (!latest || !Number.isFinite(time) || (Number.isFinite(windowStart) && time < windowStart)) return rows

  const local = new Date(time + shanghaiOffset).toISOString()
  const date = local.slice(0, 10)
  const label = `${monthDay(date)} ${local.slice(11, 16)}`
  // Hub's midnight collection supplies the preceding period close and the latest observation
  // together. Only a near-midnight sample qualifies: a later intraday sample with an unchanged
  // count is a separate observation, not the period close (which keeps its midnight label).
  const midnight = Date.parse(`${date}T00:00:00+08:00`)
  const close =
    time >= midnight &&
    time - midnight < 30 * 60_000 &&
    rows.find(row => row.time === midnight && row.totalStars === latest.totalStars)
  if (close) {
    close.time = time
    close.label = label
    close.latestStars = latest.totalStars
  } else {
    rows.push({ time, periodStart: date, label, totalStars: null, latestStars: latest.totalStars, partial: false })
    rows.sort((a, b) => a.time - b.time)
  }
  return rows
}

/** One X-axis tick per calendar month, anchored to its first returned bucket. */
export function starMonthTicks(points: readonly StarPoint[]) {
  return points
    .filter(
      (point, index) => index === 0 || point.periodStart.slice(0, 7) !== points[index - 1].periodStart.slice(0, 7)
    )
    .map(point => point.periodStart)
}

/** Merges buckets into one row per `authorId`, highest count first. */
export function aggregateAuthors(buckets: readonly (readonly WorkbenchAuthor[])[]): WorkbenchAuthor[] {
  const byId = new Map<string, WorkbenchAuthor>()

  for (const authors of buckets) {
    for (const author of authors) {
      const existing = byId.get(author.authorId)
      if (existing) {
        existing.count += author.count
      } else {
        byId.set(author.authorId, { ...author })
      }
    }
  }

  return sortAuthors([...byId.values()])
}

/** Sorts a leaderboard by count; ties keep the incoming order, so the ranking does not jitter. */
export function sortAuthors(authors: readonly WorkbenchAuthor[]): WorkbenchAuthor[] {
  return [...authors].sort((a, b) => b.count - a.count)
}

export function toLeaderboard(authors: readonly WorkbenchAuthor[]): Leaderboard {
  const rows = sortAuthors(authors)

  return {
    rows,
    total: rows.reduce((sum, author) => sum + author.count, 0),
    contributors: rows.filter(author => author.authorId !== UNLINKED_AUTHOR_ID).length
  }
}

/** Bar axis: a round maximum and its midpoint, so the plot has two gridlines. */
export function barAxis(totals: readonly number[]) {
  const tallest = Math.max(0, ...totals)
  const half = tallest / 2
  const magnitude = 10 ** Math.floor(Math.log10(Math.max(half, 1)))
  const step = [1, 2, 2.5, 5, 10].map(multiple => multiple * magnitude).find(candidate => candidate >= half)
  const axisHalf = step ?? magnitude * 10

  return { max: axisHalf * 2, half: axisHalf }
}

/** Buckets that carry an X label; a 30-day axis labels the first, every seventh day and the last. */
export function labelledBuckets(points: readonly { periodStart: string }[], range: WorkbenchRange) {
  if (range === '6m') {
    return new Set(points.map(point => point.periodStart))
  }

  const last = points.length - 1
  return new Set(
    points
      // The weekly tick is dropped when it would sit right next to the final label.
      .filter((_, index) => index === 0 || index === last || (index % 7 === 0 && index <= last - 3))
      .map(point => point.periodStart)
  )
}

/** Vertical domain of the star line: a little headroom above and below the measured values. */
export function starDomain(values: readonly (number | null)[]) {
  const measured = values.filter((value): value is number => value !== null)

  if (measured.length === 0) {
    return null
  }

  const low = Math.min(...measured)
  const high = Math.max(...measured)
  // A flat series still needs a band to draw in; a star total never goes below zero.
  const pad = high === low ? Math.max(1, Math.round(high * 0.02)) : Math.max(1, Math.round((high - low) * 0.12))

  return [Math.max(0, low - pad), high + pad] as const
}

/** Each author's segments share a shade across the displayed periods. */
export function authorShades(buckets: readonly (readonly WorkbenchAuthor[])[]) {
  const shades = new Map<string, string>()

  for (const authors of buckets) {
    for (const author of authors) {
      if (!shades.has(author.authorId)) {
        shades.set(author.authorId, dashboardChartColor(shades.size))
      }
    }
  }

  return shades
}

/** Splits a release title into its version tag and the remaining summary. */
export function releaseTitle(tagName: string, title: string) {
  const rest = title.startsWith(tagName) ? title.slice(tagName.length).replace(/^[-–—:：\s]+/, '') : title

  return { version: tagName, rest }
}

/**
 * Keeps an http(s) target from a remote payload, so a bad or hostile value cannot turn a link
 * into `javascript:` or an empty href. `fallback` is returned when the value is unusable.
 */
export function safeHttpUrl(url: unknown, fallback: string) {
  // The payload is remote JSON: a number or object here must degrade to the fallback, not throw.
  const value = typeof url === 'string' ? url.trim() : ''

  if (!value) {
    return fallback
  }

  try {
    const parsed = new URL(value)
    return parsed.protocol === 'http:' || parsed.protocol === 'https:' ? parsed.toString() : fallback
  } catch {
    return fallback
  }
}

/** Absolute http(s) only; null when the payload value cannot be used as a link. */
export function safeOptionalHttpUrl(url: unknown) {
  const value = safeHttpUrl(url, '')
  return value || null
}

/** `2026-10-11` from a release timestamp. */
export function releaseDate(publishedAt: string) {
  return publishedAt.slice(0, 10)
}
