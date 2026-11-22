/**
 * Build script that injects a Baidu Analytics snippet when `VITE_BAIDU_ANALYTICS_ID` is set.
 *
 * The id lives in the environment rather than in this file so that projects deployed from this
 * repository do not report to someone else's analytics account. Leaving the variable unset keeps
 * the built `index.html` byte-identical to a build without this feature.
 */
const PLACEHOLDER = '<!--baidu-analytics-->'

export function buildBaiduAnalyticsScript(rawId: string | undefined): string {
  const id = rawId?.trim()
  if (!id) return ''

  // The id is embedded in a JS string literal, so anything that could close it is rejected
  // instead of escaped: a build-time failure is louder than a silently broken snippet.
  if (!/^[A-Za-z0-9]+$/.test(id)) {
    throw new Error(`VITE_BAIDU_ANALYTICS_ID 只能包含字母和数字，当前值：${id}`)
  }

  return [
    '<script>',
    'var _hmt = _hmt || [];',
    '(function() {',
    'var hm = document.createElement("script");',
    `hm.src = "https://hm.baidu.com/hm.js?${id}";`,
    'var s = document.getElementsByTagName("script")[0];',
    's.parentNode.insertBefore(hm, s);',
    '})();',
    '</script>'
  ].join('\n')
}

export { PLACEHOLDER as BAIDU_ANALYTICS_PLACEHOLDER }
