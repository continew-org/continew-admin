import { tanstackRouter } from '@tanstack/router-plugin/vite'
import tailwindcss from '@tailwindcss/vite'
import react from '@vitejs/plugin-react'
import { defineConfig, loadEnv, type Plugin } from 'vite'

import { BAIDU_ANALYTICS_PLACEHOLDER, buildBaiduAnalyticsScript } from './src/scripts/baidu-analytics-script'
import { buildThemeBootScript } from './src/scripts/theme-boot-script'

function themeBootPlugin(): Plugin {
  return {
    name: 'continew:theme-boot',
    transformIndexHtml(html) {
      return html.replace('<!--theme-boot-->', () => `<script>${buildThemeBootScript()}</script>`)
    }
  }
}

function baiduAnalyticsPlugin(analyticsId: string | undefined): Plugin {
  return {
    name: 'continew:baidu-analytics',
    transformIndexHtml(html) {
      return html.replace(BAIDU_ANALYTICS_PLACEHOLDER, () => buildBaiduAnalyticsScript(analyticsId))
    }
  }
}

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd()) as ImportMetaEnv
  const {
    VITE_API_BASE_URL,
    VITE_API_PREFIX,
    VITE_APP_PORT,
    VITE_BASE_PATH,
    VITE_BAIDU_ANALYTICS_ID,
    VITE_WORKBENCH_HUB_TARGET,
    VITE_WORKBENCH_HUB_URL
  } = env

  // Fail the build on a malformed id; the client only checks for emptiness.
  if (env.VITE_WWADS_UNIT_ID && !/^\d+$/.test(env.VITE_WWADS_UNIT_ID)) {
    throw new Error(`VITE_WWADS_UNIT_ID 只能是数字，当前值：${env.VITE_WWADS_UNIT_ID}`)
  }

  // The public Hub rejects any request whose Referer is not on its allowlist, and a browser on
  // the dev server sends `localhost`. A same-origin prefix therefore proxies to the Hub origin
  // and rewrites the Referer; an absolute VITE_WORKBENCH_HUB_URL is called directly instead.
  // `/` alone would proxy the whole dev site, and the client strips trailing slashes to `/` too,
  // so it counts as unconfigured on both sides.
  // Same normalization the client applies (trim, then strip trailing slashes), so the proxy and
  // the request path always agree. The trailing slash stays on the proxy key so `/hub-preview`
  // is not swept up by the `/hub` rule.
  const rawHubPrefix = (VITE_WORKBENCH_HUB_URL ?? '').trim().replace(/\/+$/, '')
  const hubPrefix = rawHubPrefix.startsWith('/') && rawHubPrefix.length > 1 ? `${rawHubPrefix}/` : null

  return {
    base: VITE_BASE_PATH || '/',
    resolve: {
      tsconfigPaths: true
    },
    server: {
      port: +VITE_APP_PORT,
      proxy: {
        [VITE_API_PREFIX]: {
          target: VITE_API_BASE_URL,
          changeOrigin: true,
          rewrite: path => path.replace(VITE_API_PREFIX, ''),
          // The message socket lives under the API prefix, so proxy upgrades too.
          ws: true
        },
        ...(hubPrefix &&
          VITE_WORKBENCH_HUB_TARGET && {
            [hubPrefix]: {
              target: VITE_WORKBENCH_HUB_TARGET,
              changeOrigin: true,
              headers: { Referer: `${VITE_WORKBENCH_HUB_TARGET}/` },
              // `getRaw` already sends no credentials; this also covers a hand-typed URL in the
              // address bar, which the browser would otherwise decorate with this site's cookies.
              // The reverse direction matters too: a hostless Set-Cookie is scoped to this origin
              // and could overwrite the site's own session cookie.
              configure: proxy => {
                proxy.on('proxyReq', proxyRequest => {
                  proxyRequest.removeHeader('cookie')
                  proxyRequest.removeHeader('authorization')
                })
                proxy.on('proxyRes', proxyResponse => {
                  proxyResponse.headers['set-cookie'] = undefined
                })
              }
            }
          })
      }
    },
    build: {
      rolldownOptions: {
        output: {
          codeSplitting: {
            groups: [
              {
                // Group lucide-react's per-icon modules into one chunk so `icon-picker` does not
                // fan out ~1,900 individual icon requests when opened alongside `lucide-react/dynamic`.
                name: 'lucide-icons',
                test: /node_modules[\\/]lucide-react[\\/]dist[\\/]esm[\\/]icons[\\/]/
              }
            ]
          }
        }
      }
    },
    plugins: [
      themeBootPlugin(),
      baiduAnalyticsPlugin(VITE_BAIDU_ANALYTICS_ID),
      tanstackRouter({ target: 'react', autoCodeSplitting: true }),
      react(),
      tailwindcss()
    ]
  }
})
