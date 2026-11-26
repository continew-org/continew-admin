import { expect, test, type Locator, type Page } from '@playwright/test'

import {
  isolateDashboard,
  modules,
  serveAnalysisData,
  serveWorkbenchData,
  submitFixtureLogin
} from './dashboard-typography-data'

test.use({ storageState: { cookies: [], origins: [] }, serviceWorkers: 'block', reducedMotion: 'reduce' })

test.beforeEach(async ({ page, baseURL }) => {
  await isolateDashboard(page, baseURL!)
})

function heading(page: Page, level: 1 | 2 | 3, name: string | RegExp) {
  return page.getByRole('heading', { level, name, exact: true }).and(page.locator(`h${level}`))
}

function card(page: Page, title: string) {
  return page.locator('[data-slot="card"]').filter({ has: page.getByText(title, { exact: true }) })
}

function ticks(chart: Locator, axis: 'x' | 'y') {
  return chart.locator(`.recharts-${axis}Axis-tick-labels text tspan`)
}

async function expectFontSize(labels: Locator, pixels: number) {
  await expect(labels.first()).toBeVisible()
  await expect
    .poll(() => labels.evaluateAll(nodes => Array.from(new Set(nodes.map(node => getComputedStyle(node).fontSize)))))
    .toEqual([`${pixels}px`])
}

async function openDashboard(page: Page, section: 'workplace' | 'analysis', root: number) {
  // Use the persisted preference so the application's boot path, not test CSS, sets the root size.
  await page.addInitScript(size => {
    if (window === window.top) localStorage.setItem('pref_font_size', String(size))
  }, root)
  await page.goto(`/dashboard/${section}`, { waitUntil: 'domcontentloaded' })
  await submitFixtureLogin(page)
  await expect(page).toHaveURL(new RegExp(`/dashboard/${section}$`))
  await expect(page.locator('html')).toHaveCSS('font-size', `${root}px`)
}

async function expectNoHorizontalOverflow(page: Page) {
  const content = page.locator('[data-slot="app-content"]')
  await expect
    .poll(() =>
      content.evaluate(el => {
        const html = document.documentElement
        const bounds = el.getBoundingClientRect()
        const panels = Array.from(el.querySelectorAll('[data-slot="card"], [data-slot="chart"]'), panel => {
          const rect = panel.getBoundingClientRect()
          return Math.max(bounds.left - rect.left, rect.right - bounds.right, panel.scrollWidth - panel.clientWidth)
        })
        return Math.max(html.scrollWidth - html.clientWidth, el.scrollWidth - el.clientWidth, ...panels)
      })
    )
    .toBeLessThanOrEqual(1) // CSSOM scroll widths are rounded to whole pixels.
}

for (const width of [390, 1280]) {
  test.describe(`${width}px 视口`, () => {
    test.use({ viewport: { width, height: 900 } })

    for (const root of [14, 16, 20]) {
      test(`工作台在 ${root}px 偏好下保留标题层级、两图刻度和切换布局`, async ({ page, baseURL }) => {
        const requests = await serveWorkbenchData(page, baseURL!)
        await openDashboard(page, 'workplace', root)

        await expect(heading(page, 1, /^你好，.+/)).toHaveCSS('font-size', `${root * (width === 390 ? 1.25 : 1.5)}px`)
        for (const name of ['近期提交', 'Star 趋势', '更新日志', '最新公告']) {
          await expect(heading(page, 2, name)).toHaveCSS('font-size', `${root}px`)
        }
        await expect(heading(page, 2, '赞助与支持')).toHaveCount(1)
        for (const name of ['每日提交次数', '贡献榜']) {
          await expect(heading(page, 3, name)).toHaveCSS('font-size', `${root * 0.875}px`)
        }

        const activity = page.getByRole('region', { name: '近期提交', exact: true })
        const star = card(page, 'Star 趋势')
        const xs = root * 0.75
        const counts = activity.getByText(/^(0|5|10)$/)
        await expect(counts).toHaveText(['10', '5', '0'])
        await expectFontSize(counts, xs)
        const dates = activity.getByText(/^10\/\d+$/)
        await expect(dates.first()).toHaveText('10/1')
        await expect(dates.last()).toHaveText('10/30')
        await expectFontSize(dates, xs)
        const dateOverflow = await dates.evaluateAll(labels => {
          const grid = labels[0].parentElement!.getBoundingClientRect()
          const rects = labels.map(label => {
            const text = document.createRange()
            text.selectNodeContents(label)
            return text.getBoundingClientRect()
          })
          return Math.max(
            grid.left - rects[0].left,
            rects.at(-1)!.right - grid.right,
            ...rects.slice(1).map((rect, index) => rects[index].right - rect.left)
          )
        })
        expect(dateOverflow).toBeLessThanOrEqual(1)
        await expectFontSize(ticks(star, 'x'), xs)
        await expectFontSize(ticks(star, 'y'), xs)
        const historyLine = star.locator('path.recharts-area-curve')
        await expect(historyLine).toBeVisible()
        await expect(historyLine).toHaveAttribute('d', /^M.+[CL].+/)
        await expect(star.locator('.recharts-area-dot')).toHaveCount(0)
        await expectNoHorizontalOverflow(page)

        const height = () => activity.evaluate(el => el.getBoundingClientRect().height)
        const before = await height()
        const initialCommits = [...requests.commits]
        const initialStars = [...requests.stars]
        expect(new Set(initialCommits)).toEqual(new Set(['range=30d']))
        expect(new Set(initialStars)).toEqual(new Set(['granularity=day&range=6m']))
        await activity.getByTitle('2026-10-01｜7 次提交').click()
        await expect(activity.getByRole('button', { name: '重置筛选' })).toBeVisible()
        const monthly = activity.getByRole('button', { name: '近 6 个月' })
        await monthly.click()
        await expect(monthly).toHaveAttribute('aria-pressed', 'true')
        await expect(heading(page, 3, '每月提交次数')).toBeVisible()
        await expect(activity.getByRole('button', { name: '重置筛选' })).toHaveCount(0)
        await expectFontSize(activity.getByText(/^\d+ 月$/), xs)
        await expectNoHorizontalOverflow(page)
        expect(await height()).toBeCloseTo(before, 0)
        expect(requests.commits).toEqual([...initialCommits, 'range=6m'])
        expect(requests.stars).toEqual(initialStars)
      })

      test(`分析页在 ${root}px 偏好下保持排行、图例、正文与 KPI 的字号阶梯`, async ({ page, baseURL }) => {
        await serveAnalysisData(page, baseURL!)
        await openDashboard(page, 'analysis', root)

        await expect(heading(page, 1, '分析页')).toHaveCount(1)
        for (const name of ['访问趋势', '访问时段', '模块访问 Top10', '终端分布', '浏览器分布', '地域分布']) {
          await expect(heading(page, 2, name)).toHaveCSS('font-size', `${root}px`)
        }
        const ranking = card(page, '模块访问 Top10')
        await expect(ticks(ranking, 'y')).toHaveCount(modules.length)
        for (const [title, axis] of [
          ['模块访问 Top10', 'y'],
          ['地域分布', 'y'],
          ['访问时段', 'x']
        ] as const) {
          await expectFontSize(ticks(card(page, title), axis), root * 0.75)
        }
        await expectFontSize(card(page, '终端分布').getByText('Linux', { exact: true }), root * 0.75)
        const pv = card(page, '累计访问量')
        await expectFontSize(pv.getByText('系统累计访问次数（PV）', { exact: true }), root * 0.875)
        await expectFontSize(pv.getByText('9,876', { exact: true }), root * 1.875)
        await expectNoHorizontalOverflow(page)

        const tooltip = ranking.getByRole('status')
        const chart = ranking.getByRole('application')
        const bars = ranking.locator('.recharts-bar-rectangle')
        await expect(bars).toHaveCount(modules.length)
        for (const [index, item] of modules.entries()) {
          if (width === 390) {
            if (index === 0) {
              await chart.focus()
            } else {
              // Recharts advances a vertical category axis with ArrowLeft.
              await chart.press('ArrowLeft')
            }
          } else {
            await bars.nth(index).hover()
          }
          const fullName = tooltip.getByText(item.name, { exact: true })
          await expect(fullName).toBeVisible()
          await expect(tooltip.getByText(String(item.value), { exact: true })).toBeVisible()
          const bounds = await ranking.boundingBox()
          await expect
            .poll(() =>
              fullName.evaluate((label, card) => {
                const text = document.createRange()
                text.selectNodeContents(label)
                const tip = label.closest('.recharts-tooltip-wrapper')!.getBoundingClientRect()
                return Math.max(
                  ...[tip, ...Array.from(text.getClientRects())].map(rect =>
                    Math.max(
                      card.x - rect.left,
                      rect.right - card.x - card.width,
                      card.y - rect.top,
                      rect.bottom - card.y - card.height
                    )
                  )
                )
              }, bounds!)
            )
            .toBeLessThanOrEqual(1)
        }
      })
    }
  })
}
