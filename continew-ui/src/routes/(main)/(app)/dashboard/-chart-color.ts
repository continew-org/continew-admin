const CHART_COLORS = ['var(--chart-1)', 'var(--chart-2)', 'var(--chart-3)', 'var(--chart-4)', 'var(--chart-5)'] as const

/** Themes define five chart shades at once; index them instead of mixing new ones. */
export function dashboardChartColor(index: number) {
  return CHART_COLORS[Math.abs(index) % CHART_COLORS.length]
}
