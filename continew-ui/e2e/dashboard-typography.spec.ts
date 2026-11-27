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

const lightBlueHex = ['#165dff', '#4080ff', '#6aa1ff', '#94bfff', '#bedaff']
const lightBlue = lightBlueHex.map(hex => `rgb(${[1, 3, 5].map(i => parseInt(hex.slice(i, i + 2), 16)).join(', ')})`)

async function expectRootPalette(root: Locator) {
  for (const [index, color] of lightBlueHex.entries()) {
    await expect(root).toHaveCSS(`--chart-${index + 1}`, color)
  }
}

async function expectPalette(marks: Locator, property: 'backgroundColor' | 'fill') {
  await expect
    .poll(() => marks.evaluateAll((nodes, prop) => nodes.map(node => getComputedStyle(node)[prop]), property))
    .toEqual(lightBlue)
}

test('工作台浅色霁蓝的五段提交柱由按钮蓝逐级变浅', async ({ page, baseURL }) => {
  await serveWorkbenchData(page, baseURL!)
  await page.emulateMedia({ colorScheme: 'light' })
  await openDashboard(page, 'workplace', 16)

  const activity = page.getByRole('region', { name: '近期提交', exact: true })
  const segments = activity.getByTitle('2026-10-01｜7 次提交').locator('span > span')
  await expect(activity.getByRole('button', { name: '近 30 天' })).toHaveCSS('background-color', lightBlue[0])
  const root = page.locator('html')
  await expect(root).toHaveAttribute('data-theme-color', 'blue')
  await expectRootPalette(root)
  await expectPalette(segments, 'backgroundColor')
  await root.evaluate(el => el.removeAttribute('data-theme-color'))
  await expectRootPalette(root)
  await expectPalette(segments, 'backgroundColor')
})

for (const [theme, mode, secondShade] of [
  ['blue', 'dark', 'rgb(104, 159, 255)'],
  ['default', 'light', 'oklch(0.556 0 0)']
] as const) {
  test(`工作台 ${theme}/${mode} 保留主题本身的第二档柱色`, async ({ page, baseURL }) => {
    await page.context().addCookies([
      { url: baseURL!, name: 'theme_color', value: theme },
      { url: baseURL!, name: 'theme_mode', value: mode }
    ])
    await serveWorkbenchData(page, baseURL!)
    await openDashboard(page, 'workplace', 16)
    await expect(page.locator('html')).toHaveAttribute('data-theme-color', theme)
    const segments = page.getByTitle('2026-10-01｜7 次提交').locator('span > span')
    await expect(segments.nth(1)).toHaveCSS('background-color', secondShade)
  })
}

test('分析页浅色霁蓝的扇区、趋势线与各自图例同色', async ({ page, baseURL }) => {
  await serveAnalysisData(page, baseURL!)
  await page.emulateMedia({ colorScheme: 'light' })
  await openDashboard(page, 'analysis', 16)

  const os = card(page, '终端分布')
  await expectPalette(os.locator('path.recharts-sector'), 'fill')
  await expectPalette(os.locator('span[style*="background-color"]'), 'backgroundColor')
  const trend = card(page, '访问趋势')
  for (const [index, [curve, label]] of [
    ['area', '访问量'],
    ['line', '独立 IP']
  ].entries()) {
    await expect(trend.locator(`path.recharts-${curve}-curve`)).toHaveCSS('stroke', lightBlue[index])
    await expect(trend.getByText(label, { exact: true }).locator('> div')).toHaveCSS(
      'background-color',
      lightBlue[index]
    )
  }
})

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
