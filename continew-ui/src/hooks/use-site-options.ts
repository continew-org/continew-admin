import { useQuery } from '@tanstack/react-query'

import { listSiteOptionDict } from '@/lib/api/common'
import { siteOptionKeys } from '@/lib/query-keys'
import { getCachedSiteOptions, setCachedSiteOptions } from '@/lib/site-option-cache'

const BASE_URL = import.meta.env.BASE_URL

/** Site options store site-root paths (`/logo.svg`), which have to sit under the deploy sub-path. */
function resolveSiteAsset(value: string) {
  return value.startsWith('/') && !value.startsWith('//') ? `${BASE_URL.replace(/\/$/, '')}${value}` : value
}

/** Public site options (`SITE_TITLE`, `SITE_LOGO`, ...) shared by the auth screens and the shell. */
export function useSiteOptions() {
  const query = useQuery({
    queryKey: siteOptionKeys.all,
    queryFn: async ({ signal }) => {
      const options = await listSiteOptionDict(signal)
      setCachedSiteOptions(options)
      return options
    },
    initialData: () => getCachedSiteOptions() ?? undefined
  })

  const options = new Map((query.data ?? []).map(option => [option.label, option.value]))
  const read = (code: string) => options.get(code)?.trim() || ''

  return {
    title: read('SITE_TITLE'),
    logo: resolveSiteAsset(read('SITE_LOGO') || '/logo.svg'),
    copyright: read('SITE_COPYRIGHT'),
    beian: read('SITE_BEIAN')
  }
}
