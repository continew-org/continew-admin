import { expect, test, type Response } from '@playwright/test'

import {
  AUTH_STATE_FILE,
  collectSidebarTitles,
  findTitleByPath,
  isApiPath,
  loginViaUi,
  type ApiEnvelope,
  type PageData,
  type RouteNode
} from './helpers'
import {
  listCardTitle,
  sidebar,
  sidebarMenuItem,
  tableBodyRows,
  userMenuLogoutItem,
  userMenuTrigger
} from './selectors'

const MENU_API = '/api/auth/user/route'
const NOTICE_LIST_API = '/api/system/notice'
const USER_INFO_API = '/api/auth/user/info'
const REFRESH_API = '/api/auth/refresh'
const LOGOUT_API = '/api/auth/logout'

test.describe('流程一：登录', () => {
  test('登录成功进入首页，侧栏渲染当前账号有权限的菜单', async ({ page }) => {
    const menuResponsePromise = page.waitForResponse(
      r => isApiPath(r.url(), MENU_API) && r.request().method() === 'GET'
    )
    await loginViaUi(page)

    // 业务断言基准来自后端菜单接口的实返数据：按钮型与隐藏项已过滤，不硬编码菜单名
    const menuResponse = await menuResponsePromise
    expect(menuResponse.ok()).toBeTruthy()
    // 浏览器拿到的是全局响应封装，应用侧的 RouteResp[] 在 data 字段里
    const menuPayload = (await menuResponse.json()) as ApiEnvelope<RouteNode[]>
    expect(menuPayload.success).toBe(true)
    const routes = menuPayload.data
    const titles = collectSidebarTitles(routes)
    expect(titles.length).toBeGreaterThan(0)

    const sidebarLocator = sidebar(page)
    for (const title of titles) {
      await expect(sidebarMenuItem(sidebarLocator, title)).toBeVisible()
    }
    await expect(page).not.toHaveURL(/\/auth\/login/)
  })
})

test.describe('只读流程（共享认证态）', () => {
  test.use({ storageState: AUTH_STATE_FILE })

  test('流程二：业务列表页数据加载成功', async ({ page }) => {
    const listResponsePromise = page.waitForResponse(
      r => isApiPath(r.url(), NOTICE_LIST_API) && r.request().method() === 'GET'
    )
    await page.goto('/system/notice')

    // 先等接口成功响应，再按响应的 total/list 分支断言（初始化脚本里该表可能无数据）
    const listResponse = await listResponsePromise
    expect(listResponse.ok()).toBeTruthy()
    const payload = (await listResponse.json()) as ApiEnvelope<PageData<{ id: number | string }>>
    expect(payload.success).toBe(true)

    if (payload.data.total > 0) {
      expect(payload.data.list.length).toBeGreaterThan(0)
      await expect(tableBodyRows(page).first()).toBeVisible()
      await expect(page.getByText(new RegExp(`共\\s*${payload.data.total}\\s*条`))).toBeVisible()
    } else {
      await expect(page.getByText('暂无数据')).toBeVisible()
    }
    // 两个分支都必须排除错误态
    await expect(page.getByText('数据加载失败')).toHaveCount(0)
  })

  test('切换在线用户范围时使用服务端筛选并同步状态列', async ({ page }) => {
    const waitForList = () =>
      page.waitForResponse(r => isApiPath(r.url(), '/api/monitor/online') && r.request().method() === 'GET')
    const onlineResponse = waitForList()
    await page.goto('/monitor/online')
    const online = await onlineResponse
    expect(online.ok()).toBe(true)
    const onlineParams = new URL(online.url()).searchParams
    expect(onlineParams.get('online')).toBe('true')
    const status = page.getByRole('columnheader', { name: '状态', exact: true })
    await expect(status).toHaveCount(0)

    const allResponse = waitForList()
    await page.getByRole('button', { name: '全部会话' }).click()
    const all = await allResponse
    expect(all.ok()).toBe(true)
    const allParams = new URL(all.url()).searchParams
    expect(allParams.has('online')).toBe(false)
    expect(allParams.get('page')).toBe('1')
    expect(allParams.get('loginTime')).toBe(onlineParams.get('loginTime'))
    await expect(status).toBeVisible()
    await page.getByRole('button', { name: '仅看在线' }).click()
    await expect(status).toHaveCount(0)
  })

  test('流程四：深层路由刷新后仍正常渲染', async ({ page }) => {
    const menuResponsePromise = page.waitForResponse(
      r => isApiPath(r.url(), MENU_API) && r.request().method() === 'GET'
    )
    await page.goto('/system/notice')

    // 菜单标题从后端实返数据按 path 推导，不用硬编码菜单名做「对应菜单链接」断言
    const menuResponse = await menuResponsePromise
    expect(menuResponse.ok()).toBeTruthy()
    const menuPayload = (await menuResponse.json()) as ApiEnvelope<RouteNode[]>
    expect(menuPayload.success).toBe(true)
    const routes = menuPayload.data
    const menuTitle = findTitleByPath(routes, '/system/notice')
    expect(menuTitle).not.toBeNull()

    await expect(listCardTitle(page)).toHaveText(menuTitle as string)
    await expect(page.getByRole('table')).toBeVisible()

    // 深层路径直接 reload：URL 不变、主体重新渲染、对应菜单链接存在且处于选中态
    await page.reload()
    await expect(listCardTitle(page)).toHaveText(menuTitle as string)
    await expect(page).toHaveURL(/\/system\/notice$/)

    const activeLink = sidebar(page).getByRole('link', { name: menuTitle as string })
    await expect(activeLink).toBeVisible()
    // Base UI 的 state.active=true 渲染为 data-active=""（选中态）
    await expect(activeLink).toHaveAttribute('data-active', '')
  })
})

test.describe('流程三：会话刷新', () => {
  test('access token 失效后自动续期，页面不掉线', async ({ page }) => {
    await loginViaUi(page)

    // 令牌在模块内存 + localStorage 双份（auth-session.ts）：只改 storage 不 reload 不会产生
    // 401，必须改写存储后 reload 让 hydrateSession 把坏令牌读进内存；刷新 Cookie 保持有效
    await page.evaluate(() => {
      window.localStorage.setItem('continew.access-token', 'e2e-invalidated-token')
    })

    const userInfoResponses: Response[] = []
    const collectUserInfo = (response: Response) => {
      if (isApiPath(response.url(), USER_INFO_API)) {
        userInfoResponses.push(response)
      }
    }
    page.on('response', collectUserInfo)
    const refreshResponsePromise = page.waitForResponse(
      r => isApiPath(r.url(), REFRESH_API) && r.request().method() === 'POST'
    )
    await page.reload()

    // /auth/refresh 成功且返回了新的 access token
    const refreshResponse = await refreshResponsePromise
    expect(refreshResponse.ok()).toBeTruthy()
    const refreshPayload = (await refreshResponse.json()) as ApiEnvelope<{ accessToken?: string }>
    expect(refreshPayload.success).toBe(true)
    expect(refreshPayload.data.accessToken).toBeTruthy()

    // 原请求（恢复会话的 /auth/user/info）在刷新后重试成功
    await expect
      .poll(() => userInfoResponses.some(r => r.status() === 200), { message: 'refresh 后应有重试成功的请求' })
      .toBe(true)

    // 页面没有掉线回登录页
    await expect(page).toHaveURL(/\/dashboard/)
    await expect(page).not.toHaveURL(/\/auth\/login/)
    page.off('response', collectUserInfo)
  })
})

test.describe('流程五：登出', () => {
  test('登出回登录页，再访问受保护路由被挡回', async ({ page }) => {
    await loginViaUi(page)

    // 头部用户菜单触发器无 aria-label，锚点理由见 selectors.ts
    await userMenuTrigger(page).click()
    const logoutResponsePromise = page.waitForResponse(
      r => isApiPath(r.url(), LOGOUT_API) && r.request().method() === 'POST'
    )
    await userMenuLogoutItem(page).click()

    // 登出请求由前端 fire-and-forget 发出且错误被吞掉（auth-store），只有等响应才能证明服务端撤销了会话
    const logoutResponse = await logoutResponsePromise
    expect(logoutResponse.ok()).toBeTruthy()
    const logoutPayload = (await logoutResponse.json()) as ApiEnvelope<unknown>
    expect(logoutPayload.success).toBe(true)

    await expect(page).toHaveURL(/\/auth\/login/)

    // 再访问受保护路由：auth-guard 的 requireAuth 挡回登录页
    await page.goto('/dashboard')
    await expect(page).toHaveURL(/\/auth\/login/)
  })
})
