import { tanstackRouter } from '@tanstack/router-plugin/vite'
import tailwindcss from '@tailwindcss/vite'
import react from '@vitejs/plugin-react'
import { defineConfig, loadEnv, type Plugin } from 'vite'

import { buildThemeBootScript } from './src/scripts/theme-boot-script'

function themeBootPlugin(): Plugin {
  return {
    name: 'continew:theme-boot',
    transformIndexHtml(html) {
      return html.replace('<!--theme-boot-->', () => `<script>${buildThemeBootScript()}</script>`)
    }
  }
}

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd()) as ImportMetaEnv
  const { VITE_API_BASE_URL, VITE_API_PREFIX, VITE_APP_PORT, VITE_BASE_PATH } = env

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
          rewrite: path => path.replace(VITE_API_PREFIX, '')
        }
      }
    },
    plugins: [themeBootPlugin(), tanstackRouter({ target: 'react', autoCodeSplitting: true }), react(), tailwindcss()]
  }
})
