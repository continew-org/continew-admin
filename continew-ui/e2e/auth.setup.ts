import { expect, test } from '@playwright/test'

import { AUTH_STATE_FILE, ensureAuthStateDir, loginViaUi } from './helpers'

/**
 * 只读用例（业务列表、深层路由刷新）共享的认证态：登录一次并把会话落盘。
 * 会改变会话的用例（登录/会话刷新/登出）不消费这份状态，各自独立登录——
 * 后端会话原子轮换，共享 storageState 会互相踢下线。
 */
test('为只读用例准备认证态', async ({ page }) => {
  ensureAuthStateDir()
  await loginViaUi(page)
  await expect(page).toHaveURL(/\/dashboard/)
  await page.context().storageState({ path: AUTH_STATE_FILE })
})
