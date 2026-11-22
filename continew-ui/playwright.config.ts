import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'

import { defineConfig } from '@playwright/test'

// 前端端口必须固定 5173：application-dev.yml 的刷新 Cookie 只允许 localhost:5173/5777，
// 端口漂移会让 /auth/refresh 失败；域名用 localhost 而非 127.0.0.1，避免 Cookie 域不匹配。
const FRONTEND_PORT = 5173
const BASE_URL = process.env.E2E_BASE_URL ?? `http://localhost:${FRONTEND_PORT}`

// 地址契约（不可混淆）：浏览器访问前端 dev server（BASE_URL）；
// 后端地址是 http://127.0.0.1:${APP_PORT}，APP_PORT 由 scripts/test/up.sh 探测后写入
// docker/test/.ports.env，这里读取后注入成前端的 VITE_API_BASE_URL（走 /api 代理）。
// 把 .ports.env 的端口当成 E2E_BASE_URL 会把浏览器直接指向后端。
function readAppPort(): number | null {
  const portsFile = fileURLToPath(new URL('../docker/test/.ports.env', import.meta.url))
  try {
    const match = /^APP_PORT=(\d+)$/m.exec(readFileSync(portsFile, 'utf8'))
    return match ? Number(match[1]) : null
  } catch {
    return null
  }
}

const appPort = readAppPort()
const API_BASE_URL = process.env.VITE_API_BASE_URL ?? (appPort === null ? undefined : `http://127.0.0.1:${appPort}`)

const webServerEnv = { ...process.env } as Record<string, string>
if (API_BASE_URL) {
  webServerEnv.VITE_API_BASE_URL = API_BASE_URL
}

export default defineConfig({
  testDir: './e2e',
  fullyParallel: false,
  workers: 1,
  timeout: 60_000,
  expect: { timeout: 10_000 },
  reporter: [['list'], ['html', { open: 'never' }]],
  use: {
    baseURL: BASE_URL,
    // 固定桌面视口：深层路由刷新的断言依赖默认布局（header-sidebar-nav）下的侧栏
    viewport: { width: 1440, height: 900 },
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure'
  },
  webServer: {
    command: `pnpm exec vite --port ${FRONTEND_PORT} --strictPort`,
    url: BASE_URL,
    cwd: fileURLToPath(new URL('.', import.meta.url)),
    reuseExistingServer: !process.env.CI,
    timeout: 120_000,
    // env 沿用 process.env：调用方可用环境变量覆盖前端 RSA 公钥（VITE_APP_RSA_PUBLIC_KEY）
    // 与其他 VITE_ 变量，VITE_API_BASE_URL 仅在未显式提供时按 .ports.env 注入
    env: webServerEnv
  },
  projects: [
    { name: 'setup', testMatch: /.*\.setup\.ts/ },
    { name: 'smoke', testIgnore: /.*\.setup\.ts/, dependencies: ['setup'] }
  ]
})
