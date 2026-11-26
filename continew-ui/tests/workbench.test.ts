import { describe, expect, it, vi } from 'vitest'

import { QueryClient } from '@tanstack/react-query'

import type {
  WorkbenchAuthor,
  WorkbenchChartMeta,
  WorkbenchCommitBucket,
  WorkbenchCommitsResp,
  WorkbenchStarBucket,
  WorkbenchStarsResp
} from '@/lib/api/workbench'
import { chartBodyCheck, isCommitBucket, isReleasesBody, starsBodyCheck } from '@/lib/api/workbench'
import { workbenchKeys } from '@/lib/query-keys'
import {
  aggregateAuthors,
  authorInitial,
  bucketTotal,
  githubProfileUrl,
  labelledBuckets,
  resolveAvatarUrl,
  safeHttpUrl,
  safeOptionalHttpUrl,
  starDomain,
  starMonthTicks,
  starTimeline,
  toCommitBars,
  toLeaderboard,
  toStarPoints
} from '@/routes/(main)/(app)/dashboard/workplace/-components/workbench-data'

import {
  commitsQueryOptions,
  starsQueryOptions
} from '@/routes/(main)/(app)/dashboard/workplace/-components/workbench-queries'

const author = (authorId: string, displayName: string, count: number, avatarUrl: string | null = null) => ({
  authorId,
  displayName,
  count,
  avatarUrl
})

const meta = (overrides: Partial<WorkbenchChartMeta> = {}): WorkbenchChartMeta => ({
  range: '30d',
  window: 'Asia/Shanghai',
  granularity: 'day',
  start: '2026-10-25',
  endExclusive: '2026-11-24',
  updatedAt: '2026-11-24T08:00:00+08:00',
  freshness: 'fresh',
  partialBuckets: [],
  ...overrides
})

describe('githubProfileUrl', () => {
  it.each(['a', 'dev-42', 'a'.repeat(39)])('maps canonical GitHub login %s to its profile', login => {
    expect(githubProfileUrl(`github:${login}`)).toBe(`https://github.com/${login}`)
  })

  it.each([
    'unlinked',
    'dev-42',
    'gitlab:dev',
    'github:',
    'github:Dev',
    'github:a--b',
    'github:-dev',
    'github:dev-',
    'github:dev/name',
    'github://example.org',
    'github:dev?tab',
    'github:dev#bio',
    'github:a%2fb',
    'github:dev\n',
    'github:用户',
    `github:${'a'.repeat(40)}`
  ])('rejects noncanonical identity %j', id => {
    expect(githubProfileUrl(id)).toBeNull()
  })
})

describe('toCommitBars', () => {
  it('keeps a bucket with a null detail as a partial zero, and an empty list as a complete zero', () => {
    const buckets: WorkbenchCommitBucket[] = [
      { periodStart: '2026-10-25', authors: [author('linzhou', '林舟', 5), author('alan', '阿岚', 5)] },
      { periodStart: '2026-10-26', authors: [] },
      { periodStart: '2026-10-27', authors: null }
    ]

    const bars = toCommitBars(meta(), buckets)

    expect(bars.map(bar => bar.total)).toEqual([10, 0, 0])
    // `[]` is a complete bucket with no commits; null means the detail is missing.
    expect(bars.map(bar => bar.partial)).toEqual([false, false, true])
    expect(bars.map(bar => bar.label)).toEqual(['10/25', '10/26', '10/27'])
  })

  it('marks a bucket listed in partialBuckets even when it still carries detail', () => {
    const buckets: WorkbenchCommitBucket[] = [
      { periodStart: '2026-10-25', authors: [author('qinghe', '青禾', 2)] },
      { periodStart: '2026-10-26', authors: [author('qinghe', '青禾', 3)] }
    ]

    const bars = toCommitBars(meta({ partialBuckets: ['2026-10-26'] }), buckets)

    // The known subtotal stays, but it must not be read as the bucket's full count.
    expect(bars[0].partial).toBe(false)
    expect(bars[1].partial).toBe(true)
    expect(bars[1].total).toBe(3)
  })

  it('labels month buckets by month instead of by day', () => {
    const bars = toCommitBars(meta({ range: '6m', granularity: 'month' }), [
      { periodStart: '2026-05-01', authors: [author('linzhou', '林舟', 4)] }
    ])

    expect(bars[0].label).toBe('5 月')
  })
})

describe('toStarPoints', () => {
  it('keeps a missing snapshot as null so the line can break, and flags the listed buckets', () => {
    const buckets: WorkbenchStarBucket[] = [
      { periodStart: '2026-05-01', totalStars: 143 },
      { periodStart: '2026-06-01', totalStars: null },
      { periodStart: '2026-07-01', totalStars: 182 }
    ]

    const points = toStarPoints(meta({ range: '6m', granularity: 'month', partialBuckets: ['2026-06-01'] }), buckets)

    expect(points.map(point => point.totalStars)).toEqual([143, null, 182])
    expect(points.map(point => point.partial)).toEqual([false, true, false])
    expect(points.map(point => point.label)).toEqual(['2026-05 月末', '2026-06 月末', '2026-07 月末'])
    expect(starMonthTicks(points)).toEqual(['2026-05-01', '2026-06-01', '2026-07-01'])
  })

  it('keeps daily detail in a six-month window but ticks only the first bucket of each month', () => {
    const points = toStarPoints(meta({ range: '6m', granularity: 'day' }), [
      { periodStart: '2025-12-30', totalStars: 3 },
      { periodStart: '2025-12-31', totalStars: 2 },
      { periodStart: '2026-01-01', totalStars: null },
      { periodStart: '2026-01-02', totalStars: 0 }
    ])

    expect(points.map(point => point.label)).toEqual([
      '2025-12-30 日末',
      '2025-12-31 日末',
      '2026-01-01 日末',
      '2026-01-02 日末'
    ])
    expect(points.map(point => point.totalStars)).toEqual([3, 2, null, 0])
    expect(starMonthTicks(points)).toEqual(['2025-12-30', '2026-01-01'])
  })

  it('keeps a measured zero distinct from a gap', () => {
    const points = toStarPoints(meta(), [
      { periodStart: '2026-10-25', totalStars: 0 },
      { periodStart: '2026-10-26', totalStars: null }
    ])

    expect(points[0].totalStars).toBe(0)
    expect(points[1].totalStars).toBeNull()
  })
})

describe('aggregateAuthors', () => {
  it('merges buckets by authorId and sorts by the summed count, not by name or order', () => {
    const merged = aggregateAuthors([
      [author('linzhou', '林舟', 5), author('alan', '阿岚', 5)],
      [author('qinghe', '青禾', 2), author('linzhou', '林舟', 1)],
      [author('alan', '阿岚', 6)]
    ])

    expect(merged.map(row => [row.authorId, row.count])).toEqual([
      ['alan', 11],
      ['linzhou', 6],
      ['qinghe', 2]
    ])
  })

  it('separates the same contributor from two buckets by id, not by display name', () => {
    // Two distinct identities that happen to share a display name must stay two rows.
    const merged = aggregateAuthors([[author('a1', '林舟', 1)], [author('a2', '林舟', 2)]])

    expect(merged).toHaveLength(2)
    expect(merged.map(row => row.authorId)).toEqual(['a2', 'a1'])
  })

  it('does not mutate the bucket authors it reads', () => {
    const first = [author('linzhou', '林舟', 5)]
    aggregateAuthors([first, [author('linzhou', '林舟', 1)]])

    expect(first[0].count).toBe(5)
  })
})

describe('labelledBuckets', () => {
  it('labels every month bucket and only spreads day labels, leaving the last two feet apart', () => {
    const days = Array.from({ length: 30 }, (_, index) => ({
      periodStart: `2026-11-${String(index + 1).padStart(2, '0')}`
    }))
    const dayLabels = labelledBuckets(days, '30d')

    expect(dayLabels.has('2026-11-01')).toBe(true)
    expect(dayLabels.has('2026-11-08')).toBe(true)
    expect(dayLabels.has('2026-11-30')).toBe(true)
    // Index 28 would collide with the final label, so the weekly tick is skipped there.
    expect(dayLabels.size).toBe(5)
    expect(labelledBuckets(days, '6m').size).toBe(30)
  })
})

describe('toLeaderboard', () => {
  it('counts linked contributors without the unlinked segment', () => {
    const board = toLeaderboard([
      author('linzhou', '林舟', 5),
      author('unlinked', '未关联账号', 4),
      author('alan', '阿岚', 1)
    ])

    expect(board.total).toBe(10)
    expect(board.contributors).toBe(2)
    expect(board.rows.map(row => row.authorId)).toEqual(['linzhou', 'unlinked', 'alan'])
  })
})

describe('starTimeline', () => {
  const buckets = [
    { periodStart: '2026-11-24', totalStars: null },
    { periodStart: '2026-11-25', totalStars: 0 }
  ]
  const project = (sampledAt: string) => {
    const data = { ...meta({ endExclusive: '2026-11-26' }), buckets, latest: { totalStars: 0, sampledAt } }
    return starTimeline(data, toStarPoints(data, buckets))
  }
  it('keeps the intraday observation independent even if the count did not change', () => {
    const rows = project('2026-11-26T06:36:15Z')
    expect(rows.map(row => row.totalStars)).toEqual([null, 0, null])
    expect(rows.at(-1)).toMatchObject({
      label: '11/26 14:36',
      latestStars: 0,
      time: Date.parse('2026-11-26T06:36:15Z')
    })
  })
  it('marks the preceding close only once for a matching midnight collection', () => {
    const rows = project('2026-11-25T16:10:00Z')
    expect(rows).toHaveLength(2)
    expect(rows.at(-1)).toMatchObject({ label: '11/26 00:10', totalStars: 0, latestStars: 0 })
  })
  it('keeps a later intraday sample independent instead of relabelling the period close', () => {
    const rows = project('2026-11-25T17:30:00Z')
    expect(rows).toHaveLength(3)
    expect(rows[1]).toMatchObject({ label: '2026-11-25 日末', totalStars: 0, latestStars: null })
    expect(rows.at(-1)).toMatchObject({ label: '11/26 01:30', totalStars: null, latestStars: 0 })
  })
  it('does not place an observation older than the displayed window on the chart', () => {
    expect(project('2026-10-01T12:00:00+08:00').every(row => row.latestStars === null)).toBe(true)
  })
})

describe('starDomain', () => {
  it('never drops below zero and keeps a flat series visible', () => {
    expect(starDomain([100, null, 0, 120])).toEqual([0, 134])
    expect(starDomain([0, 0])).toEqual([0, 1])
    expect(starDomain([null, null])).toBeNull()
  })
})

describe('resolveAvatarUrl', () => {
  it('resolves a Hub-relative path against the Hub base and leaves absolute URLs alone', () => {
    expect(resolveAvatarUrl('/avatars/linzhou.svg', 'https://admin.continew.top/hub')).toBe(
      'https://admin.continew.top/hub/avatars/linzhou.svg'
    )
    expect(resolveAvatarUrl('avatars/alan.svg', 'https://admin.continew.top/hub/')).toBe(
      'https://admin.continew.top/hub/avatars/alan.svg'
    )
    expect(resolveAvatarUrl('https://avatars.githubusercontent.com/u/1', 'https://admin.continew.top/hub')).toBe(
      'https://avatars.githubusercontent.com/u/1'
    )
  })

  it('resolves a relative Hub base for a same-origin dev proxy', () => {
    expect(resolveAvatarUrl('/avatars/qinghe.svg', '/hub')).toBe('/hub/avatars/qinghe.svg')
  })

  it('returns null for a missing avatar or a missing Hub base', () => {
    expect(resolveAvatarUrl(null, 'https://admin.continew.top/hub')).toBeNull()
    expect(resolveAvatarUrl('/avatars/linzhou.svg', '')).toBeNull()
  })

  it('cannot be talked into another host by extra slashes or backslashes', () => {
    // A protocol-relative value would otherwise be read as an absolute URL.
    expect(resolveAvatarUrl('//evil.example/a.png', 'https://admin.continew.top/hub')).toBe(
      'https://admin.continew.top/hub/evil.example/a.png'
    )
    // Browsers read a backslash as a separator, so this would climb to another origin.
    expect(resolveAvatarUrl('\\evil.example/a.png', 'https://admin.continew.top/hub')).toBeNull()
  })
})

describe('small helpers', () => {
  it('uses the first character of the display name as the avatar fallback', () => {
    expect(authorInitial('林舟')).toBe('林')
    expect(authorInitial('  Alan ')).toBe('A')
    expect(authorInitial('')).toBe('')
  })

  it('sums a bucket and treats a null detail as zero', () => {
    expect(bucketTotal([author('a', 'A', 3), author('b', 'B', 2)])).toBe(5)
    expect(bucketTotal(null)).toBe(0)
    expect(bucketTotal([])).toBe(0)
  })
})

const commitResponse = (range: '30d' | '6m'): WorkbenchCommitsResp => ({
  ...meta({ range }),
  buckets: [{ periodStart: '2026-10-25', authors: [author('linzhou', '林舟', 5)] }]
})

const starsResponse = (range: '30d' | '6m'): WorkbenchStarsResp => ({
  ...meta({ range }),
  buckets: [{ periodStart: '2026-10-25', totalStars: 120 }]
})

describe('safeHttpUrl', () => {
  it('keeps http(s) targets and falls back for anything else', () => {
    expect(safeHttpUrl('https://github.com/continew-org/continew-admin/releases/tag/v4.2.0', 'https://fallback')).toBe(
      'https://github.com/continew-org/continew-admin/releases/tag/v4.2.0'
    )
    expect(safeHttpUrl('http://example.com/a', 'https://fallback')).toBe('http://example.com/a')
    expect(safeHttpUrl('javascript:alert(1)', 'https://fallback')).toBe('https://fallback')
    expect(safeHttpUrl('', 'https://fallback')).toBe('https://fallback')
    expect(safeHttpUrl(null, 'https://fallback')).toBe('https://fallback')
    expect(safeHttpUrl('not a url', 'https://fallback')).toBe('https://fallback')
    expect(safeOptionalHttpUrl('javascript:alert(1)')).toBeNull()
    // The value comes from remote JSON; a number must degrade, not throw.
    expect(safeHttpUrl(42, 'https://fallback')).toBe('https://fallback')
    expect(safeHttpUrl({ href: 'https://x' }, 'https://fallback')).toBe('https://fallback')
  })
})

describe('workbench trend requests', () => {
  it.each([
    ['6m', 'range=6m&granularity=day'],
    ['30d', 'range=30d']
  ] as const)('only requests daily granularity for six-month stars: %s', async (range, starParams) => {
    const client = new QueryClient()
    const fetch = vi
      .fn<typeof globalThis.fetch>()
      .mockResolvedValueOnce(Response.json(starsResponse(range)))
      .mockResolvedValueOnce(Response.json(commitResponse(range)))
    vi.stubGlobal('fetch', fetch)
    try {
      await client.fetchQuery(starsQueryOptions(range))
      await client.fetchQuery(commitsQueryOptions(range))
      expect(
        fetch.mock.calls.map(([url, options]) => [String(url).split('/api/workbench/')[1], options?.method])
      ).toEqual([
        [`stars?${starParams}`, 'GET'],
        [`commits?range=${range}`, 'GET']
      ])
    } finally {
      vi.unstubAllGlobals()
    }
  })
})

describe('workbench query keys', () => {
  it('nests the two charts under the requested range and keeps releases range-free', () => {
    expect(workbenchKeys.commits('30d')).toEqual(['workbench', 'commits', '30d'])
    expect(workbenchKeys.stars('6m')).toEqual(['workbench', 'stars', '6m'])
    expect(workbenchKeys.releases()).toEqual(['workbench', 'releases'])
    expect(workbenchKeys.all).toEqual(['workbench'])
  })

  it('reuses the six-month star trend and releases when the commit range changes', async () => {
    // Same freshness rule as the app's query client: data stays fresh, so a revisit does not refetch.
    const client = new QueryClient({ defaultOptions: { queries: { retry: false, staleTime: 5 * 60_000 } } })
    const fetchReleases = vi.fn(async () => ({ data: { updatedAt: null, freshness: 'fresh' as const, items: [] } }))
    const fetchChart = vi.fn(async (range: '30d' | '6m') => ({ data: commitResponse(range) }))
    const fetchStars = vi.fn(async (range: '30d' | '6m') => ({ data: starsResponse(range) }))

    const range = (next: '30d' | '6m') => [
      client.fetchQuery({ queryKey: workbenchKeys.commits(next), queryFn: () => fetchChart(next) }),
      client.fetchQuery({ queryKey: workbenchKeys.stars('6m'), queryFn: () => fetchStars('6m') }),
      client.fetchQuery({ queryKey: workbenchKeys.releases(), queryFn: fetchReleases })
    ]

    await Promise.all(range('30d'))
    await Promise.all(range('6m'))
    // Back to the first range: the cache answers, so neither chart is fetched again either.
    await Promise.all(range('30d'))

    expect(fetchChart.mock.calls.map(([next]) => next)).toEqual(['30d', '6m'])
    expect(fetchStars.mock.calls.map(([next]) => next)).toEqual(['6m'])
    expect(fetchReleases).toHaveBeenCalledTimes(1)
  })
})

describe('author identity binding', () => {
  it('keeps a leaderboard row attached to its authorId across two ranges with different orders', () => {
    const firstRange: WorkbenchAuthor[] = [author('linzhou', '林舟', 2), author('alan', '阿岚', 7)]
    const secondRange: WorkbenchAuthor[] = [author('alan', '阿岚', 1), author('linzhou', '林舟', 9)]

    const first = toLeaderboard(firstRange)
    const second = toLeaderboard(secondRange)

    expect(first.rows[0].authorId).toBe('alan')
    // The leader changes rank, but the row still points at the same identity.
    expect(second.rows[0].authorId).toBe('linzhou')
  })
})

describe('Hub response shape guards', () => {
  it('validates commit buckets field by field and requires the requested range back', () => {
    const author = { authorId: 'a', displayName: '甲', avatarUrl: null, count: 3 }
    const ok = { range: '30d', buckets: [{ periodStart: '2026-11-01', authors: [author] }], partialBuckets: [] }
    const check = chartBodyCheck('30d', isCommitBucket)

    expect(check(ok)).toBe(true)
    expect(check({ ...ok, buckets: [{ periodStart: '2026-11-01', authors: null }] })).toBe(true)
    expect(check({ ...ok, buckets: [{ periodStart: '2026-11-01', authors: [] }] })).toBe(true)
    // An author entry the leaderboard cannot read would crash it mid-render.
    expect(check({ ...ok, buckets: [{ periodStart: '2026-11-01', authors: [null] }] })).toBe(false)
    expect(check({ ...ok, buckets: [{ periodStart: '2026-11-01', authors: [{ authorId: 'a' }] }] })).toBe(false)
    // A negative count would cancel another author's commits in the leaderboard sum.
    expect(check({ ...ok, buckets: [{ periodStart: '2026-11-01', authors: [{ ...author, count: -1 }] }] })).toBe(false)
    expect(check({ ...ok, buckets: [null] })).toBe(false)
    // Bucket dates become chart timestamps; a malformed one yields NaN and breaks the X axis.
    expect(check({ ...ok, buckets: [{ periodStart: 'garbage', authors: [] }] })).toBe(false)
    // Date.parse normalizes a nonexistent day; the label would then disagree with the plotted slot.
    expect(check({ ...ok, buckets: [{ periodStart: '2025-02-30', authors: [] }] })).toBe(false)
    expect(check({ ...ok, buckets: [{ periodStart: '2024-02-29', authors: [] }] })).toBe(true)
    // A malformed window start would let an out-of-window observation onto the chart.
    expect(check({ ...ok, start: 'garbage' })).toBe(false)
    expect(check({ ...ok, partialBuckets: [1] })).toBe(false)
    expect(check({ error: 'upstream' })).toBe(false)
    // A mismatched echo would make the projection label month buckets as days.
    expect(check({ ...ok, range: '6m' })).toBe(false)
  })

  it('validates star buckets and treats a missing totalStars as a bad shape', () => {
    const check = starsBodyCheck('6m')
    const body = { range: '6m', buckets: [{ periodStart: '2026-11-01', totalStars: 143 }], partialBuckets: [] }
    const ok = { ...body, granularity: 'month' }

    expect(check(ok)).toBe(true)
    expect(check({ ...body, granularity: 'day' })).toBe(true)
    expect(check(body)).toBe(false)
    expect(check({ ...body, granularity: 'week' })).toBe(false)
    expect(check({ ...ok, range: '30d' })).toBe(false)
    expect(check({ ...ok, buckets: [{ periodStart: '2026-11-01', totalStars: null }] })).toBe(true)
    // Absent is not the same as the documented null: starDomain would compute NaN.
    expect(check({ ...ok, buckets: [{ periodStart: '2026-11-01' }] })).toBe(false)
    expect(check({ ...ok, buckets: [{ periodStart: '2026-11-01', totalStars: '143' }] })).toBe(false)
    // A negative or fractional total would reach the chart as a real value.
    expect(check({ ...ok, buckets: [{ periodStart: '2026-11-01', totalStars: -1 }] })).toBe(false)
    expect(check({ ...ok, buckets: [{ periodStart: '2026-11-01', totalStars: 1.5 }] })).toBe(false)
  })

  it('allows absent and null latest observations but validates both displayed fields when present', () => {
    const check = starsBodyCheck('6m')
    const body = starsResponse('6m')
    const latest = { totalStars: 0, sampledAt: '2026-11-26T14:36:15+08:00', repoCount: 8 }
    expect(check(body)).toBe(true)
    expect(check({ ...body, latest: null })).toBe(true)
    expect(check({ ...body, latest })).toBe(true)
    for (const invalid of [
      0,
      [],
      { totalStars: 8 },
      { sampledAt: latest.sampledAt },
      { ...latest, totalStars: -1 },
      { ...latest, sampledAt: 1 }
    ]) {
      expect(check({ ...body, latest: invalid })).toBe(false)
    }
  })

  it('rejects a releases body whose items is neither null nor a release array', () => {
    const release = {
      tagName: 'v4.2.0',
      title: 'v4.2.0',
      publishedAt: '2026-10-11T00:00:00+08:00',
      htmlUrl: 'https://github.com/continew-org/continew-admin/releases/tag/v4.2.0'
    }
    expect(isReleasesBody({ updatedAt: null, items: [release] })).toBe(true)
    expect(isReleasesBody({ updatedAt: '2026-11-24T08:00:00+08:00', items: null })).toBe(true)
    expect(isReleasesBody({ updatedAt: null, items: [] })).toBe(true)
    // `items: "error"` would reach `items.map()` and throw during render.
    expect(isReleasesBody({ updatedAt: null, items: 'error' })).toBe(false)
    expect(isReleasesBody({ updatedAt: null })).toBe(false)
    expect(isReleasesBody({ updatedAt: 5, items: [] })).toBe(false)
  })
})
