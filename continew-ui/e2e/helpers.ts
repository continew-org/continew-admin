import { mkdirSync } from 'node:fs'

import { expect, type Page } from '@playwright/test'

import { loginButton, passwordInput, usernameInput } from './selectors'

export const E2E_USERNAME = process.env.E2E_USERNAME ?? 'admin'
export const E2E_PASSWORD = process.env.E2E_PASSWORD ?? 'admin123'

/** 只读用例共享的认证态落盘路径；含令牌，已由 continew-ui/.gitignore 排除，严禁入库。 */
export const AUTH_STATE_FILE = `${import.meta.dirname}/.auth/state.json`

/** 后端菜单树节点（GET /api/auth/user/route 实返结构的子集）。 */
export interface RouteNode {
  title: string
  type: number
  path: string
  isHidden?: boolean
  children?: RouteNode[]
}

/** 全局响应封装（top.continew.starter.web.model.R）的浏览器侧子集。 */
export interface ApiEnvelope<T> {
  success: boolean
  code: string
  msg: string
  data: T
}

export interface PageData<T> {
  list: T[]
  total: number
}

/** 浏览器侧 API 路径匹配：前端以 /api 前缀走 vite 代理，对比 pathname 避免误匹配 query。 */
export function isApiPath(url: string, pathname: string): boolean {
  try {
    return new URL(url).pathname === pathname
  } catch {
    return false
  }
}

const TYPE_MENU = 2
const TYPE_BUTTON = 3

const isVisible = (node: RouteNode) => node.type !== TYPE_BUTTON && !node.isHidden

/**
 * 从后端实返菜单树推导侧栏顶层应渲染的标题（不硬编码菜单名，插件启停会让菜单变化）。
 * 顶层可见节点 = 顶级菜单项 + 有可见子项的目录组；组标题与组内直属子项标题在展开的
 * 侧栏上都直接渲染。按钮型与隐藏项过滤；孙级藏在折叠面板里，展开前不渲染，不纳入。
 */
export function collectSidebarTitles(routes: RouteNode[]): string[] {
  const titles: string[] = []
  for (const node of routes.filter(isVisible)) {
    if (node.type === TYPE_MENU || !node.children?.length) {
      titles.push(node.title)
      continue
    }

    const children = node.children.filter(isVisible)
    if (children.length === 0) {
      continue
    }

    titles.push(node.title)
    for (const child of children) {
      titles.push(child.title)
    }
  }
  return titles
}

const splitPath = (path: string) => {
  const queryIndex = path.indexOf('?')
  return queryIndex === -1 ? path : path.slice(0, queryIndex)
}

/** 按 path（去 query）查菜单标题，供深层路由刷新断言「对应菜单链接」。 */
export function findTitleByPath(routes: RouteNode[], pathname: string): string | null {
  for (const node of routes) {
    if (node.type !== TYPE_BUTTON && splitPath(node.path) === pathname) {
      return node.title
    }

    const childTitle = node.children ? findTitleByPath(node.children, pathname) : null
    if (childTitle !== null) {
      return childTitle
    }
  }
  return null
}

/**
 * 独立登录：登录/会话刷新/登出用例各自调用，不复用 storageState——后端会话是原子轮换的，
 * 共享会互相失效。登录成功后由路由跳转到 /dashboard。
 */
export async function loginViaUi(page: Page): Promise<void> {
  await page.goto('/auth/login')
  await usernameInput(page).fill(E2E_USERNAME)
  await passwordInput(page).fill(E2E_PASSWORD)
  await loginButton(page).click()
  await expect(page).toHaveURL(/\/dashboard/)
}

/** 确保认证态目录存在（storageState 落盘前调用）。 */
export function ensureAuthStateDir(): void {
  mkdirSync(`${import.meta.dirname}/.auth`, { recursive: true })
}
