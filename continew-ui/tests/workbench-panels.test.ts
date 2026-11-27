import { createElement } from 'react'
import { renderToStaticMarkup } from 'react-dom/server'
import { describe, expect, it, vi } from 'vitest'

import { QueriesObserver, QueryClient, QueryClientProvider } from '@tanstack/react-query'

import { dashboardKeys } from '@/lib/api/dashboard'
import type {
  WorkbenchCommitBucket,
  WorkbenchReleasesResp,
  WorkbenchStarBucket,
  WorkbenchStarsResp
} from '@/lib/api/workbench'
import { workbenchKeys } from '@/lib/query-keys'
import { Route } from '@/routes/(main)/(app)/dashboard/workplace/route'

import {
  commitsQueryOptions,
  releasesQueryOptions,
  starsQueryOptions
} from '@/routes/(main)/(app)/dashboard/workplace/-components/workbench-queries'
import { WorkbenchWelcome } from '@/routes/(main)/(app)/dashboard/workplace/-components/workbench-welcome'
import { WorkbenchSponsors } from '@/routes/(main)/(app)/dashboard/workplace/-components/workbench-sponsors'

function renderSponsors(feed: unknown): string {
  const client = new QueryClient()
  client.setQueryData(['workbench', 'sponsors'], feed)
  return renderToStaticMarkup(createElement(QueryClientProvider, { client }, createElement(WorkbenchSponsors)))
}

const hub = vi.hoisted(() => ({ enabled: true }))
vi.mock('@/lib/api/workbench', async importOriginal => ({
  ...(await importOriginal<typeof import('@/lib/api/workbench')>()),
  get isWorkbenchEnabled() {
    return hub.enabled
  }
}))
const { readUser } = vi.hoisted(() => ({
  readUser: vi.fn<() => { nickname: string; loginIp?: string | null; loginTime?: string | null }>(() => ({
    nickname: '测试用户'
  }))
}))
vi.mock('@/stores/auth/auth-provider', () => ({ useAuthStore: readUser }))

function renderWorkbench({
  sources = [null, null, null],
  buckets = [],
  stars = [],
  latest = null,
  items = [],
  stale = false
}: {
  sources?: [string | null, string | null, string | null]
  buckets?: WorkbenchCommitBucket[]
  stars?: WorkbenchStarBucket[]
  latest?: WorkbenchStarsResp['latest']
  items?: WorkbenchReleasesResp['items']
  stale?: boolean
} = {}): string {
  const client = new QueryClient()
  client.setQueryData(workbenchKeys.commits('30d'), {
    source: sources[0],
    data: {
      range: '30d',
      partialBuckets: [],
      buckets,
      freshness: stale ? 'stale' : 'fresh',
      updatedAt: stale ? '2026-11-01T08:00:00+08:00' : null
    }
  })
  client.setQueryData(workbenchKeys.stars('6m'), {
    source: sources[1],
    data: { range: '6m', granularity: 'day', partialBuckets: [], buckets: stars, latest }
  })
  client.setQueryData(workbenchKeys.releases(), { source: sources[2], data: { updatedAt: null, items } })
  return renderPage(client)
}

function renderPage(client: QueryClient): string {
  const Page = Route.options.component!
  return renderToStaticMarkup(createElement(QueryClientProvider, { client }, createElement(Page)))
}

describe('workbench panels', () => {
  it('keeps all five cards in reading order without Hub requests, loading placeholders or invented counts', () => {
    hub.enabled = false
    const client = new QueryClient()
    client.setQueryData(dashboardKeys.notices(), [])
    const fetch = vi.fn(() => Promise.reject(new Error('unexpected Hub request')))
    vi.stubGlobal('fetch', fetch)
    try {
      const markup = renderPage(client)
      expect([...markup.matchAll(/<h2\b[^>]*>([^<]+)<\/h2>/g)].map(([, title]) => title)).toEqual([
        '近期提交',
        'Star 趋势',
        '更新日志',
        '赞助与支持',
        '最新公告'
      ])
      expect(markup).not.toMatch(/data-slot="skeleton"|未启用仓库统计|合计 0 次|0 位贡献者|重新加载/)
      expect(markup).toContain('全部版本')
      // Server rendering does not start requests; subscribing exercises the same queries on mount.
      const queries = new QueriesObserver(client, [
        commitsQueryOptions('30d'),
        starsQueryOptions('6m'),
        releasesQueryOptions()
      ])
      const unsubscribe = queries.subscribe(() => undefined)
      try {
        expect(queries.getCurrentResult().map(result => result.fetchStatus)).toEqual(['idle', 'idle', 'idle'])
        expect(fetch).not.toHaveBeenCalled()
      } finally {
        unsubscribe()
      }
    } finally {
      hub.enabled = true
      vi.unstubAllGlobals()
    }
  })

  it('offers a retry for each failed Hub card without replacing the notice or sponsor content', async () => {
    const client = new QueryClient({ defaultOptions: { queries: { retry: false, retryOnMount: false } } })
    for (const queryKey of [workbenchKeys.commits('30d'), workbenchKeys.stars('6m'), workbenchKeys.releases()]) {
      await expect(
        client.fetchQuery({ queryKey, queryFn: () => Promise.reject(new Error('offline')) })
      ).rejects.toThrow('offline')
    }
    client.setQueryData(dashboardKeys.notices(), [{ id: '1', title: '系统维护通知' }])
    const markup = renderPage(client)
    expect(markup.match(/重新加载/g)).toHaveLength(3)
    expect(markup).toContain('系统维护通知')
    expect(markup).toContain('alt="公众号"')
  })

  it('uses active banners without repeating compatibility entries or showing historical partners', () => {
    const banner = { name: '风铃云信息科技', img: 'aeoliancloud/banner.webp' }
    const markup = renderSponsors({
      strategic: [],
      infrastructure: [banner, { name: '无横幅伙伴' }],
      special: [banner],
      resourceHistory: [{ name: '往期伙伴', img: 'past.webp' }]
    })
    expect(markup.match(/alt="风铃云信息科技"/g)).toHaveLength(1)
    expect(markup).toContain('https://continew.top/images/sponsor/partners/aeoliancloud/banner.webp')
    expect(markup).not.toMatch(/无横幅伙伴|往期伙伴/)
    expect(markup).not.toContain('alt="公众号"')
    expect(
      [...markup.matchAll(/<button\b[^>]*aria-label="切换到第 ([^"]+)"([^>]*)>/g)].map(([, name, attrs]) => [
        name,
        attrs.includes('aria-current="true"')
      ])
    ).toEqual([
      ['1 张：风铃云信息科技', true],
      ['2 张：公众号', false]
    ])
    expect(markup).not.toContain('1 / 2')
    expect(markup).toContain('下一张')
    expect(markup.match(/<button\b/g)).toHaveLength(4)
    expect(markup).not.toMatch(/播放轮播|暂停轮播/)
  })

  it('accepts legacy feeds but shows no controls with only the community image', () => {
    expect(renderSponsors({ platinum: [{ name: '伙伴', img: 'banner.webp' }] })).toContain('上一张')
    const markup = renderSponsors({ strategic: [], infrastructure: [null, { name: '无图' }] })
    expect(markup).toContain('alt="公众号"')
    expect(markup).not.toContain('<button')
  })

  it('keeps the greeting and the three external project links in reading order', () => {
    const markup = renderWorkbench()
    expect(markup).toContain('你好，测试用户')
    const nav = markup.match(/<nav aria-label="项目链接"[\s\S]*?<\/nav>/)?.[0] ?? ''
    const links = [...nav.matchAll(/<a\b([^>]*)>([^<]+)<svg[^>]*>/g)]
    expect(links.map(([, attrs, label]) => [label, attrs.match(/href="([^"]+)"/)?.[1]])).toEqual([
      ['常见问题', 'https://continew.top/docs/admin/5.x/reference/faq'],
      ['为什么选我们', 'https://continew.top/docs/admin/5.x/why-choose-us'],
      ['支持我们', 'https://continew.top/sponsor']
    ])
    for (const [anchor] of links) {
      expect(anchor).not.toContain('role="button"')
      expect(anchor).toContain('target="_blank"')
      expect(anchor).toContain('lucide-external-link')
    }
  })

  it.each([
    ['2001:0db8:1234:5678:9abc:def0:1234:5678', '2026-11-23 09:30:45'],
    [null, '2026-11-23 09:30:45'],
    ['192.0.2.1', null],
    [undefined, undefined]
  ])('renders the two current session fields independently: %s, %s', (loginIp, loginTime) => {
    readUser.mockReturnValueOnce({ nickname: '测试用户', loginIp, loginTime })
    const markup = renderToStaticMarkup(createElement(WorkbenchWelcome))
    const fields = [...markup.matchAll(/<dt\b[^>]*>([^<]+)<\/dt><dd\b[^>]*>([^<]+)<\/dd>/g)]
    expect(fields.map(([, label, value]) => [label, value])).toEqual([
      ['登录 IP', loginIp ?? '—'],
      ['登录时间', loginTime ?? '—']
    ])
  })

  it.each<[[string | null, string | null, string | null], string]>([
    [['mock', 'mock', 'mock'], '近期提交、Star 趋势、更新日志'],
    [[null, 'mock', 'live'], 'Star 趋势'],
    [['mock', null, 'mock'], '近期提交、更新日志'],
    [['live', null, null], '']
  ])('marks only the panels whose responses are mock: %j', (sources, panels) => {
    const markup = renderWorkbench({ sources })
    expect(markup.match(/示例数据/g) ?? []).toHaveLength(panels ? 1 : 0)
    if (panels) expect(markup).toContain(`示例数据：${panels}<`)
  })

  it('marks a stale snapshot with its build time instead of presenting it as current', () => {
    expect(renderWorkbench()).not.toContain('旧快照')
    expect(renderWorkbench({ stale: true })).toContain('部分数据为旧快照：更新于 2026-11-01T08:00:00+08:00')
  })

  it.each<[number, string]>([
    [0, '0'],
    [999, '999'],
    [1000, '1K'],
    [2134, '2.1K'],
    [1_250_000, '1.3M']
  ])('shows the latest measured Star total %i as %s and retains the exact count', (count, label) => {
    const sampledAt = '2026-11-26T14:36:15+08:00'
    const markup = renderWorkbench({
      latest: { totalStars: count, sampledAt },
      stars: [
        { periodStart: '2026-11-01', totalStars: null },
        { periodStart: '2026-11-02', totalStars: 42 }
      ]
    })
    expect(markup).toContain(`title="${count.toLocaleString('zh-CN')}｜采样时间：${sampledAt}">${label}</span>`)
    expect(markup).not.toContain('截至')
    expect(markup).toContain('Open ContiNew 仓库 Star 总数<')
    expect(markup).toContain('部分数据')
  })

  it('keeps the Star header empty when history exists without a latest observation', () => {
    const markup = renderWorkbench({ latest: null, stars: [{ periodStart: '2026-11-25', totalStars: 42 }] })
    expect(markup).toMatch(/近 6 个月<\/span><span[^>]*>—<\/span>/)
    expect(markup).not.toContain('这段时间暂无 Star 记录')
  })

  it('keeps the Star empty state when both history and the latest observation are missing', () => {
    const markup = renderWorkbench({ latest: null, stars: [{ periodStart: '2026-11-25', totalStars: null }] })
    expect(markup).toContain('这段时间暂无 Star 记录')
    expect(markup).not.toContain('最新实测：')
  })

  it('shows the latest observation even when every daily bucket is unmeasured', () => {
    const sampledAt = '2026-11-26T14:36:15+08:00'
    const markup = renderWorkbench({
      latest: { totalStars: 2835, sampledAt },
      stars: Array.from({ length: 209 }, (_, day) => ({
        periodStart: new Date(Date.UTC(2026, 4, 1 + day)).toISOString().slice(0, 10),
        totalStars: null
      }))
    })
    expect(markup).toContain(`title="2,835｜采样时间：${sampledAt}">2.8K</span>`)
    expect(markup).not.toContain('这段时间暂无 Star 记录')
    expect(markup).toContain('最新实测：11/26 14:36 · 2,835 Star')
    expect(markup).toContain('暂无历史日末记录')
    expect(markup).not.toContain('部分数据')
  })

  it('links the avatar and name only for mapped contributors and leaves counts outside the link', () => {
    const markup = renderWorkbench({
      buckets: [
        {
          periodStart: '2026-11-01',
          authors: [
            { authorId: 'github:canonical-42', displayName: '显示别名', count: 7, avatarUrl: null },
            { authorId: 'unlinked', displayName: 'looks-like-a-login', count: 3, avatarUrl: null },
            { authorId: 'github:bad/path', displayName: 'another-login', count: 1, avatarUrl: null }
          ]
        }
      ]
    })
    const board = markup.match(/<section aria-label="贡献榜"[\s\S]*?<\/section>/)![0]
    const links = [...board.matchAll(/<a\b([^>]*)>([\s\S]*?)<\/a>/g)]
    expect(links).toHaveLength(1)
    const [, attrs, content] = links[0]
    expect(attrs).toContain('href="https://github.com/canonical-42"')
    expect(attrs).toContain('target="_blank"')
    expect(attrs).toContain('rel="noopener noreferrer"')
    expect(content).toContain('data-slot="avatar"')
    expect(content).toContain('显示别名')
    expect(board).toMatch(/<\/a><span[^>]*>7<span/)
    expect(board).toContain('looks-like-a-login')
    expect(board).toContain('another-login')
  })

  it('does not present a missing window as zero commits or zero contributors', () => {
    const markup = renderWorkbench({ buckets: [{ periodStart: '2026-11-01', authors: null }] })
    expect(markup).toContain('2026-11-01｜暂无可用提交明细')
    expect(markup).not.toContain('0 次提交')
    expect(markup).not.toContain('0 位贡献者')
    expect(markup).not.toContain('合计 0 次')
  })

  it('shows a measured empty bucket as zero even when the next bucket is missing', () => {
    const markup = renderWorkbench({
      buckets: [
        { periodStart: '2026-11-01', authors: [] },
        { periodStart: '2026-11-02', authors: null }
      ]
    })
    expect(markup).toContain('2026-11-01｜0 次提交')
    expect(markup).toContain('已知小计 0 次')
    expect(markup).toContain('部分提交数据不完整')
    expect(markup).not.toContain('重置筛选')
  })

  it.each<[WorkbenchReleasesResp['items'], string]>([
    [null, '发布记录暂不可用'],
    [[], '暂无正式版本']
  ])('distinguishes the release snapshot state for %j', (items, message) => {
    const markup = renderWorkbench({ items })
    expect(markup).toContain(message)
  })
})
