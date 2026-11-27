import { expect, test } from '@playwright/test'

import { AUTH_STATE_FILE } from './helpers'

test.use({ storageState: AUTH_STATE_FILE })

test('欢迎区按顺序提供问题、选型和支持入口', async ({ page }) => {
  await page.goto('/dashboard/workplace')
  const links = page.getByRole('navigation', { name: '项目链接' }).getByRole('link')
  await expect(links).toHaveText(['常见问题', '为什么选我们', '支持我们'])
  await expect(links.nth(0)).toHaveAttribute('href', 'https://continew.top/docs/admin/5.x/reference/faq')
  await expect(links.nth(1)).toHaveAttribute('href', 'https://continew.top/docs/admin/5.x/why-choose-us')
  await expect(links.nth(2)).toHaveAttribute('href', 'https://continew.top/sponsor')
})

test('减少动态效果时仍自动轮播，可暂停、恢复和手动切换', async ({ page }) => {
  await page.emulateMedia({ reducedMotion: 'reduce' })
  const carousel = page.getByRole('region', { name: '赞助与支持' })
  await page.clock.install()
  await page.route('https://continew.top/sponsor.json', async route => {
    await expect(carousel.getByRole('link', { name: '公众号' })).toBeVisible()
    await expect(carousel.getByRole('button')).toHaveCount(0)
    await route.fulfill({
      json: {
        infrastructure: [
          { name: '风铃云信息科技', img: 'aeoliancloud/banner.webp' },
          { name: '伙伴乙', img: 'partner-b.webp' }
        ]
      }
    })
  })
  await page.goto('/dashboard/workplace', { waitUntil: 'domcontentloaded' })
  const partner = carousel.getByRole('link', { name: '风铃云信息科技' })
  await expect(partner).toBeVisible()
  const dots = carousel.getByRole('group', { name: '选择轮播图' })
  const first = dots.getByRole('button', { name: '切换到第 1 张：风铃云信息科技' })
  const second = dots.getByRole('button', { name: '切换到第 2 张：伙伴乙' })
  const third = dots.getByRole('button', { name: '切换到第 3 张：公众号' })
  const previous = carousel.getByRole('button', { name: '上一张' })
  await expect(carousel.locator('button')).toHaveCount(5)
  await expect(carousel.getByRole('button', { name: /播放|暂停/, includeHidden: true })).toHaveCount(0)
  const url = page.url()
  const pages = page.context().pages().length
  await expect(first).toHaveAttribute('aria-current', 'true')
  await expect(third).toBeVisible()
  for (const dot of [second, third, first]) {
    await page.clock.fastForward(4000)
    await expect(dot).toHaveAttribute('aria-current', 'true')
  }
  await third.click()
  await expect(carousel.getByRole('link', { name: '公众号' })).toBeVisible()
  await expect(first).not.toHaveAttribute('aria-current', 'true')
  await expect(third).toHaveAttribute('aria-current', 'true')
  await expect(page).toHaveURL(url)
  expect(page.context().pages()).toHaveLength(pages)
  await previous.press('Enter')
  await expect(second).toHaveAttribute('aria-current', 'true')
  await third.press('Space')
  await expect(third).toHaveAttribute('aria-current', 'true')

  // Moving off the banner must not resume while a dot still has keyboard focus.
  await page.mouse.move(0, 0)
  await page.clock.fastForward(4000)
  await expect(third).toHaveAttribute('aria-current', 'true')

  // Conversely, hovering an arrow keeps rotation paused after focus leaves the carousel.
  await previous.hover()
  await page.getByRole('navigation', { name: '项目链接' }).getByRole('link').first().focus()
  await page.clock.fastForward(4000)
  await expect(third).toHaveAttribute('aria-current', 'true')
  await page.mouse.move(0, 0)
  for (const dot of [first, second]) {
    await page.clock.fastForward(4000)
    await expect(dot).toHaveAttribute('aria-current', 'true')
  }
})

test('公告列表不预取详情，点击只读取所选公告的用户端详情', async ({ page }) => {
  const requests: string[] = []
  const notices = [
    { id: '901', title: '工作台测试公告甲', type: '1', isTop: true },
    { id: '902', title: '工作台测试公告乙', type: '1', isTop: false }
  ]
  await page.route('**/api/dashboard/notice', route => route.fulfill({ json: { success: true, data: notices } }))
  await page.route(/\/api\/(?:user\/message|system)\/notice\/\d+$/, route => {
    const path = new URL(route.request().url()).pathname
    requests.push(path)
    return route.fulfill({ json: { success: true, data: { ...notices[1], content: '<p>公告乙正文</p>' } } })
  })

  await page.goto('/dashboard/workplace')
  await expect(page.getByRole('button', { name: /工作台测试公告甲.*置顶/ })).toBeVisible()
  expect(requests).toEqual([])
  await page.getByRole('button', { name: notices[1].title }).click()
  await expect(page.getByText('公告乙正文')).toBeVisible()
  expect(requests).toEqual(['/api/user/message/notice/902'])
})

test('Hub 失败后卡片不换列，重试只更新所选卡片', async ({ page }) => {
  let recover = false
  let starTotal: number | null = null
  const requests: string[] = []
  await page.route('**/api/dashboard/notice', route => route.fulfill({ json: { success: true, data: [] } }))
  await page.route('**/api/workbench/**', route => {
    const path = new URL(route.request().url()).pathname
    requests.push(path)
    if (starTotal !== null && path.endsWith('/stars')) {
      return route.fulfill({
        json: {
          range: '6m',
          granularity: 'day',
          partialBuckets: [],
          latest: { totalStars: starTotal, sampledAt: '2026-11-26T08:00:00+08:00' },
          buckets: [{ periodStart: '2026-11-25', totalStars: starTotal }]
        }
      })
    }
    if (recover && path.endsWith('/releases')) {
      return route.fulfill({ json: { updatedAt: null, items: [] } })
    }
    return route.fulfill({ status: 503, body: '' })
  })
  await page.goto('/dashboard/workplace')
  const card = (name: string) =>
    page.locator('[data-slot="card"]').filter({ has: page.getByRole('heading', { name, exact: true }) })
  const activity = card('近期提交')
  const stars = card('Star 趋势')
  const releases = card('更新日志')
  const sponsors = page.getByRole('region', { name: '赞助与支持' })
  const notices = card('最新公告')
  const panels = [activity, stars, releases, sponsors, notices]
  const positions = () =>
    Promise.all(
      panels.map(panel =>
        panel.evaluate(el => {
          const content = el.closest('[data-slot="app-content"]')!.getBoundingClientRect()
          const { left, top, right, bottom } = el.getBoundingClientRect()
          return {
            left: left - content.left,
            top: top - content.top,
            right: right - content.left,
            bottom: bottom - content.top
          }
        })
      )
    )
  for (const panel of [activity, stars, releases]) {
    await expect(panel.getByRole('button', { name: '重新加载' })).toBeVisible()
  }
  await expect(notices.getByText('暂无公告')).toBeVisible()
  const [a, s, r, sponsor, notice] = await positions()
  expect(s.left).toBe(a.left)
  expect(s.top).toBeGreaterThan(a.bottom)
  expect(r.left).toBeGreaterThan(a.right)
  expect(r.top).toBe(a.top)
  expect(sponsor.left).toBe(r.left)
  expect(sponsor.top).toBeGreaterThan(r.bottom)
  expect(notice.left).toBe(r.left)
  expect(notice.top).toBeGreaterThan(sponsor.bottom)

  const beforeRetry = requests.length
  recover = true
  await releases.getByRole('button', { name: '重新加载' }).click()
  await expect(releases.getByText('暂无正式版本')).toBeVisible()
  await expect(activity.getByRole('button', { name: '重新加载' })).toBeVisible()
  await expect(stars.getByRole('button', { name: '重新加载' })).toBeVisible()
  expect(requests.slice(beforeRetry)).toEqual([expect.stringMatching(/\/api\/workbench\/releases$/)])
  expect(await positions()).toEqual([a, s, r, sponsor, notice])

  await page.setViewportSize({ width: 390, height: 844 })
  const mobile = await positions()
  for (let index = 1; index < mobile.length; index++) {
    expect(mobile[index].top).toBeGreaterThan(mobile[index - 1].bottom)
  }

  starTotal = 141
  await stars.getByRole('button', { name: '重新加载' }).click()
  await expect(stars.getByText('141', { exact: true })).toBeVisible()
  await expect.poll(positions).toEqual(mobile)

  starTotal = 987_654_321
  await page.reload()
  await expect(stars.getByText('987.7M', { exact: true })).toBeVisible()
  await expect(notices.getByText('暂无公告')).toBeVisible()
  await expect(releases.getByText('暂无正式版本')).toBeVisible()
  await expect.poll(positions).toEqual(mobile)
})

test('没有历史日末记录时仍绘制独立的最新实测点', async ({ page }) => {
  await page.route('**/api/workbench/stars?*', route =>
    route.fulfill({
      json: {
        range: '6m',
        granularity: 'day',
        start: '2026-05-01',
        endExclusive: '2026-11-26',
        partialBuckets: ['2026-11-25'],
        latest: { totalStars: 2835, sampledAt: '2026-11-26T16:44:36+08:00' },
        buckets: [{ periodStart: '2026-11-25', totalStars: null }]
      }
    })
  )
  await page.goto('/dashboard/workplace')
  const marker = page.getByRole('img', { name: '最新实测：11/26 16:44 · 2,835 Star' })
  await expect(marker).toBeVisible()
  await expect(marker.locator('title')).toHaveText('最新实测：11/26 16:44 · 2,835 Star')
  await expect(page.getByText('这段时间暂无 Star 记录')).toHaveCount(0)
})

test('榜单随选择变化但全窗缺口保留，切换提交周期后重置且不重查 Star', async ({ page }) => {
  const stars: string[] = []
  const author = { authorId: 'a', displayName: '贡献者甲', count: 3, avatarUrl: null }
  let deliverMonthly!: () => void
  const monthly = new Promise<void>(resolve => {
    deliverMonthly = resolve
  })
  await page.route('**/api/workbench/commits?*', async route => {
    const range = new URL(route.request().url()).searchParams.get('range')
    const buckets = [
      { periodStart: '2026-11-01', authors: [author] },
      { periodStart: '2026-11-02', authors: null }
    ]
    if (range === '6m') {
      await monthly
      return route.fulfill({
        json: {
          range,
          partialBuckets: [],
          buckets: [
            {
              periodStart: '2026-11-01',
              authors: Array.from({ length: 12 }, (_, i) => ({ ...author, authorId: `m${i}` }))
            }
          ]
        }
      })
    }
    return route.fulfill({ json: { range, partialBuckets: [], buckets } })
  })
  await page.route('**/api/workbench/stars?*', route => {
    const params = new URL(route.request().url()).searchParams
    const range = params.get('range')
    stars.push(params.toString())
    return route.fulfill({
      json: {
        range,
        partialBuckets: [],
        granularity: 'day',
        latest: { totalStars: 2835, sampledAt: '2026-11-26T14:36:15+08:00', repoCount: 8 },
        buckets: [{ periodStart: '2026-11-01', totalStars: 8 }]
      }
    })
  })

  await page.goto('/dashboard/workplace')
  const stats = page.getByRole('region', { name: '近期提交' })
  const board = stats.getByRole('region', { name: '贡献榜' })
  const complete = stats.getByTitle('2026-11-01｜3 次提交')
  const reset = board.getByRole('button', { name: '重置筛选' })
  await expect(page.getByText('Open ContiNew 仓库 Star 总数', { exact: true })).toBeVisible()
  await expect(page.getByTitle('2,835｜采样时间：2026-11-26T14:36:15+08:00', { exact: true })).toHaveText('2.8K')
  // StrictMode replays the mount effect in dev, so the first fetch may be issued twice (one aborted).
  // Capture the settled set here; what matters is that the interactions below add no new queries.
  const initialStars = [...stars]
  const boardLayout = () =>
    board.evaluate(el => ({
      height: el.getBoundingClientRect().height,
      title: el.querySelector('h3')!.getBoundingClientRect().top,
      list: el.querySelector('ol')!.parentElement!.getBoundingClientRect().top
    }))
  await expect(board.getByRole('list')).toBeVisible()
  const initialLayout = await boardLayout()
  const cardHeight = () => stats.evaluate(el => el.getBoundingClientRect().height)
  const baseline = () =>
    stats
      .locator('button[title]')
      .first()
      .evaluate(el => el.getBoundingClientRect().bottom - el.closest('[role="region"]')!.getBoundingClientRect().top)
  const initialHeight = await cardHeight()
  const initialBaseline = await baseline()
  const heading = await board.getByRole('heading', { name: '贡献榜' }).boundingBox()
  const summary = await board.getByText('已知 3 次提交 · 1 位贡献者', { exact: true }).boundingBox()
  expect(summary!.x).toBeGreaterThanOrEqual(heading!.x + heading!.width)
  expect(summary!.y).toBeLessThan(heading!.y + heading!.height)
  await expect(reset).toHaveCount(0)
  await complete.click()
  await expect(reset).toBeVisible()
  expect(await boardLayout()).toEqual(initialLayout)
  await reset.click()
  await expect(reset).toHaveCount(0)
  await complete.click()
  await expect(board.getByText('11/1', { exact: true })).toBeVisible()
  await expect(board.getByText('部分数据')).toHaveCount(0)
  await expect(stats.getByText(/部分提交数据不完整/)).toBeVisible()
  await stats.getByTitle('2026-11-02｜暂无可用提交明细').click()
  await expect(board.getByText('该周期暂无可用提交明细')).toBeVisible()
  await expect(board.getByText(/0 次提交/)).toHaveCount(0)
  await complete.click()
  await stats.getByRole('button', { name: '近 6 个月' }).click()
  await expect(stats.getByRole('button', { name: '近 6 个月' })).toHaveAttribute('aria-pressed', 'true')
  try {
    await expect(stats.locator('[data-slot="skeleton"]')).toBeVisible()
    expect(await cardHeight()).toBeCloseTo(initialHeight, 0)
  } finally {
    deliverMonthly()
  }
  await expect(board.getByRole('listitem')).toHaveCount(12)
  expect(await cardHeight()).toBeCloseTo(initialHeight, 0)
  expect(await baseline()).toBeCloseTo(initialBaseline, 0)
  await expect(board.getByText('近 6 个月', { exact: true })).toHaveCount(0)
  await expect(reset).toHaveCount(0)
  await expect(board.getByText('部分数据')).toHaveCount(0)
  const firstRowGap = () =>
    board.evaluate(el => {
      const header = el.querySelector('h3')!.parentElement!
      return el.querySelector('li')!.getBoundingClientRect().top - header.getBoundingClientRect().bottom
    })
  expect(await firstRowGap()).toBeCloseTo(8, 0)
  await stats.getByTitle('2026-11-01｜36 次提交').click()
  await expect(reset).toBeVisible()
  expect(await firstRowGap()).toBeGreaterThan(8)
  expect(await cardHeight()).toBeCloseTo(initialHeight, 0)
  expect(await baseline()).toBeCloseTo(initialBaseline, 0)
  await reset.click()
  expect(await firstRowGap()).toBeCloseTo(8, 0)
  await expect(page.getByTitle('2,835｜采样时间：2026-11-26T14:36:15+08:00', { exact: true })).toHaveText('2.8K')
  expect(new Set(stars)).toEqual(new Set(['range=6m&granularity=day']))
  expect(stars).toEqual(initialStars)
})
