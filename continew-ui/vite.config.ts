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
  const { VITE_API_BASE_URL, VITE_API_PREFIX, VITE_APP_PORT, VITE_BASE_PATH, VITE_BAIDU_ANALYTICS_ID } = env

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
        }
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
