import { expect, type Page } from '@playwright/test'

import { loginButton, passwordInput, usernameInput } from './selectors'

const account = 'typography-reader'
const captcha = '8426'

/** All application traffic terminates in this context; only local frontend assets may pass through. */
export async function isolateDashboard(page: Page, baseURL: string) {
  const origin = new URL(baseURL).origin
  const context = page.context()
  const shell = new Map<string, unknown>([
    [
      '/api/captcha/image',
      {
        uuid: 'typography-image',
        isEnabled: true,
        img: `data:image/svg+xml,${encodeURIComponent(`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 80 30"><text y="20">${captcha}</text></svg>`)}`
      }
    ],
    [
      '/api/auth/user/info',
      { id: 7102, username: account, nickname: '排版读者', roles: [], roleNames: [], permissions: [] }
    ],
    [
      '/api/auth/user/route',
      ['workplace', 'analysis'].map((name, index) => ({
        id: index + 1,
        parentId: 0,
        type: 2,
        title: ['工作台', '分析页'][index],
        name,
        component: '',
        path: `/dashboard/${name}`
      }))
    ],
    ['/api/system/common/dict/option/site', []],
    ['/api/user/message/unread', { total: 0 }],
    ['/api/user/message/notice/unread', { total: 0 }],
    ['/api/user/message/notice/unread/POPUP', []]
  ])
  // Any WebSocket left unmatched would open a real connection; intercept them all instead.
  await context.routeWebSocket('**/*', () => {})
  await context.route('**/*', route => {
    const request = route.request()
    const url = new URL(request.url())
    if (url.origin === origin && url.pathname === '/api/auth/login' && request.method() === 'POST') {
      const body = request.postDataJSON()
      expect(body.username).toBe(account)
      expect(body.captcha).toBe(captcha)
      expect(body.uuid).toBe('typography-image')
      return route.fulfill({
        json: { success: true, data: { accessToken: 'typography-fixture-only', tokenType: 'Bearer' } }
      })
    }
    if (url.origin === origin && request.method() === 'GET' && shell.has(url.pathname)) {
      return route.fulfill({ json: { success: true, data: shell.get(url.pathname) } })
    }
    if (
      url.origin !== origin ||
      // Vite matches the `/api` proxy key by prefix, so `/api` itself must be blocked too.
      url.pathname.startsWith('/api') ||
      // The dev server proxies `/hub` to the real Hub; an unmatched image GET would escape there.
      url.pathname.startsWith('/hub/') ||
      ['fetch', 'xhr'].includes(request.resourceType()) ||
      request.method() !== 'GET'
    ) {
      return route.abort('blockedbyclient')
    }
    return route.continue()
  })
}

export async function submitFixtureLogin(page: Page) {
  await usernameInput(page).fill(account)
  await passwordInput(page).fill('fixture input only')
  await page.getByPlaceholder('请输入验证码', { exact: true }).fill(captcha)
  await page.getByRole('checkbox', { name: '记住我' }).uncheck()
  await loginButton(page).click()
}

export const modules = [
  { name: 'PlatformTransactionAuthorisationMonitoringNorthRegion', value: 37 },
  { name: 'PlatformConfigurationDistributionSouthRegion', value: 19 }
] as const

const months = ['05', '06', '07', '08', '09', '10'].map(month => `2026-${month}-01`)
const days = Array.from({ length: 30 }, (_, index) => `2026-10-${String(index + 1).padStart(2, '0')}`)
// The star feed declares a daily window, so its buckets must span that window day by day.
const starDays = Array.from({ length: 184 }, (_, index) => {
  const date = new Date(Date.UTC(2026, 4, 1 + index))
  return date.toISOString().slice(0, 10)
})
const meta = { window: 'Asia/Shanghai', updatedAt: '2026-11-01T01:00:00+08:00', freshness: 'fresh', partialBuckets: [] }

/** Matches one exact GET endpoint on the app origin; other requests fall through to the isolation rule. */
function endpoint(baseURL: string, pathname: string) {
  const origin = new URL(baseURL).origin
  return (url: URL) => url.origin === origin && url.pathname === pathname
}

export async function serveWorkbenchData(page: Page, baseURL: string) {
  const commits: string[] = []
  const stars: string[] = []
  // The dev Hub base is `/hub`, so these requests carry that prefix on the app origin.
  await page.route(endpoint(baseURL, '/hub/api/workbench/commits'), route => {
    if (route.request().method() !== 'GET') return route.fallback()
    const params = new URL(route.request().url()).searchParams
    commits.push(params.toString())
    const range = params.get('range')
    const periods = range === '6m' ? months : days
    return route.fulfill({
      json: {
        ...meta,
        range,
        start: periods[0],
        endExclusive: range === '6m' ? '2026-11-01' : '2026-10-31',
        granularity: range === '6m' ? 'month' : 'day',
        buckets: periods.map((periodStart, index) => ({
          periodStart,
          authors: [{ authorId: 'qa', displayName: '贡献者甲', avatarUrl: null, count: index === 0 ? 7 : 3 }]
        }))
      }
    })
  })
  await page.route(endpoint(baseURL, '/hub/api/workbench/stars'), route => {
    if (route.request().method() !== 'GET') return route.fallback()
    const params = new URL(route.request().url()).searchParams
    params.sort()
    stars.push(params.toString())
    return route.fulfill({
      json: {
        ...meta,
        range: '6m',
        granularity: 'day',
        start: starDays[0],
        endExclusive: '2026-11-01',
        buckets: starDays.map((periodStart, index) => ({ periodStart, totalStars: 2000 + index }))
      }
    })
  })
  await page.route(endpoint(baseURL, '/hub/api/workbench/releases'), route => {
    if (route.request().method() !== 'GET') return route.fallback()
    return route.fulfill({ json: { freshness: 'fresh', updatedAt: meta.updatedAt, items: [] } })
  })
  await page.route(endpoint(baseURL, '/api/dashboard/notice'), route => {
    if (route.request().method() !== 'GET') return route.fallback()
    return route.fulfill({ json: { success: true, data: [] } })
  })
  await page.route('https://continew.top/sponsor.json', route => {
    if (route.request().method() !== 'GET') return route.fallback()
    return route.fulfill({ json: { infrastructure: [] } })
  })
  return { commits, stars }
}

export async function serveAnalysisData(page: Page, baseURL: string) {
  const responses = {
    'access/trend/7': [
      { date: '2026-10-29', pvCount: 61, ipCount: 22 },
      { date: '2026-10-30', pvCount: 78, ipCount: 24 }
    ],
    'analysis/module': modules,
    'analysis/timeslot': [{ name: '08:00-10:00', value: 12 }],
    'analysis/geo': [{ name: '江苏', value: 14 }],
    'analysis/os': [{ name: 'Linux', value: 18 }],
    'analysis/browser': [{ name: 'Firefox', value: 13 }],
    'analysis/overview/pv': { total: 9876, today: 78, growth: 5, dataList: [] },
    'analysis/overview/ip': { total: 1852, today: 24, growth: 2, dataList: [] }
  }
  for (const [path, data] of Object.entries(responses)) {
    const match = endpoint(baseURL, `/api/dashboard/${path}`)
    await page.route(
      url => match(url),
      route => {
        if (route.request().method() !== 'GET') return route.fallback()
        return route.fulfill({ json: { success: true, data } })
      }
    )
  }
}
