import type { Locator, Page } from '@playwright/test'

/*
 * 语义锚点清单（所有 e2e 定位锚点集中在这里，顶部逐个说明对应的语义元素与选用理由）。
 * 纪律：优先语义锚点（role/label/placeholder/input[name]）；禁止 :nth-child、CSS/Tailwind 类名、
 * DOM 层级链。`data-slot` 不是样式类，是组件库（shadcn/Base UI）自带的语义挂载点，仅在
 * 没有可访问名的组件树上作为稳定锚点使用，每处使用均注明原因。
 *
 * | 锚点                                            | 对应的语义元素                                   | 为什么用它 |
 * | ----------------------------------------------- | ------------------------------------------------ | ---------- |
 * | input[name="username"] / input[name="password"] | 登录表单的用户名/密码输入框（login-form.tsx）     | 表单控件带稳定 name 属性，优先于 placeholder 文案 |
 * | getByRole('button', { name: '登录', exact })     | 登录提交按钮                                     | 可访问名即按钮文本；exact 区分提交态文案「登录中」 |
 * | [data-slot="sidebar"]                           | 侧栏容器（ui/sidebar.tsx）                       | 菜单断言收窄到侧栏，避免命中面包屑等同名文本 |
 * | [data-slot="card-title"]                        | 列表页卡片标题（ui/card.tsx CardTitle，div 无 role） | 页面主体业务标题；无 heading role，data-slot 是其唯一语义钩子 |
 * | [data-slot="table-body"]                        | 表格主体（ui/table.tsx TableBody）               | 行数断言收窄到 tbody，其内用 role=row 语义统计 |
 * | [data-slot="app-header"] 下的 [data-slot="dropdown-menu-trigger"] | 头部用户菜单触发器（admin-header.tsx > UserMenu） | 触发器是 DropdownMenuTrigger 包 Avatar，没有 aria-label，AvatarImage 的 alt 也不是触发器可访问名，只能经头部容器向下定位；Avatar 包装组件的 props 展开会把组件库的 data-slot="avatar" 覆盖成触发器自身的 data-slot，头部内 dropdown-menu-trigger 仅此一处 |
 * | getByRole('menuitem', { name: '登出系统' })      | 用户下拉菜单的登出项                              | 可访问名即菜单文本（子串匹配兼容快捷键提示） |
 */

export function usernameInput(page: Page): Locator {
  return page.locator('input[name="username"]')
}

export function passwordInput(page: Page): Locator {
  return page.locator('input[name="password"]')
}

export function loginButton(page: Page): Locator {
  return page.getByRole('button', { name: '登录', exact: true })
}

export function sidebar(page: Page): Locator {
  return page.locator('[data-slot="sidebar"]')
}

/** 侧栏内的菜单标题文本；同名多节点时断言任一可见（目录组标题与菜单项可能同名）。 */
export function sidebarMenuItem(sidebarLocator: Locator, title: string): Locator {
  return sidebarLocator.getByText(title, { exact: true }).first()
}

export function listCardTitle(page: Page): Locator {
  return page.locator('[data-slot="card-title"]')
}

export function tableBody(page: Page): Locator {
  return page.locator('[data-slot="table-body"]')
}

export function tableBodyRows(page: Page): Locator {
  return tableBody(page).getByRole('row')
}

/** 触发器是 DropdownMenuTrigger 包 Avatar、无 aria-label（AvatarImage 的 alt 不算触发器可访问名），只能经头部容器向下定位（见顶部清单）。 */
export function userMenuTrigger(page: Page): Locator {
  return page.locator('[data-slot="app-header"]').locator('[data-slot="dropdown-menu-trigger"]')
}

export function userMenuLogoutItem(page: Page): Locator {
  return page.getByRole('menuitem', { name: '登出系统' })
}
