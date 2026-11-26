import * as React from 'react'

import { useQuery } from '@tanstack/react-query'
import { ChevronLeft, ChevronRight } from 'lucide-react'

import { getRaw } from '@/lib/http'

import { safeHttpUrl } from './workbench-data'

const SPONSOR_BASE = 'https://continew.top'
const SPONSOR_FEED_PATH = '/sponsor.json'
const AUTOPLAY_MS = 4000

interface PromoItem {
  readonly name: string
  readonly img: string
  readonly url: string
}

interface SponsorEntry {
  name?: unknown
  img?: unknown
  url?: unknown
}

interface SponsorFeed {
  strategic?: unknown
  infrastructure?: unknown
  special?: unknown
  platinum?: unknown
}

/** Always available, shown before the feed resolves and kept when it fails. */
const STATIC_ITEMS: readonly PromoItem[] = [
  {
    name: '公众号',
    img: `${SPONSOR_BASE}/images/home/join-group.webp`,
    url: SPONSOR_BASE
  }
]

/** Sponsor images are either absolute URLs or file names inside the partners folder. */
function toImageUrl(img: string) {
  return safeHttpUrl(/^https?:\/\//i.test(img) ? img : `${SPONSOR_BASE}/images/sponsor/partners/${img}`, '')
}

/** Drops entries whose tag is missing or malformed; the feed is a third-party document. */
function entriesOf(group: unknown): SponsorEntry[] {
  return Array.isArray(group)
    ? group.filter((entry): entry is SponsorEntry => typeof entry === 'object' && entry !== null)
    : []
}

function sponsorsToItems(feed: SponsorFeed | null): PromoItem[] {
  if (!feed) {
    return []
  }

  // The compatibility tiers repeat the modern banners; never combine the two schemas.
  const entries =
    Array.isArray(feed.strategic) || Array.isArray(feed.infrastructure)
      ? [...entriesOf(feed.strategic), ...entriesOf(feed.infrastructure)]
      : [...entriesOf(feed.special), ...entriesOf(feed.platinum)]
  return entries.flatMap(entry => {
    const name = typeof entry.name === 'string' ? entry.name.trim() : ''
    const file = typeof entry.img === 'string' ? entry.img.trim() : ''
    const img = file ? toImageUrl(file) : ''

    return name && img ? [{ name, img, url: safeHttpUrl(entry.url, SPONSOR_BASE) }] : []
  })
}

function SponsorImage({ item }: { readonly item: PromoItem }) {
  const [failed, setFailed] = React.useState(false)

  return failed ? (
    <span className="grid h-full w-full place-items-center bg-muted px-4 text-center text-sm font-medium">
      {item.name}
    </span>
  ) : (
    <img
      src={item.img}
      alt={item.name}
      onError={() => setFailed(true)}
      className="h-full w-full object-contain motion-safe:animate-in motion-safe:duration-300 motion-safe:fade-in"
    />
  )
}

function ActiveDot({ index, count }: { readonly index: number; readonly count: number }) {
  const indicator = React.useRef<HTMLSpanElement>(null)
  const center = ((index + 0.5) / count) * 100
  const previous = React.useRef(center)

  React.useLayoutEffect(() => {
    const from = previous.current
    previous.current = center
    if (from === center || window.matchMedia('(prefers-reduced-motion: reduce)').matches) return

    // The leading edge reaches the next dot before the trailing edge catches up.
    const animation = indicator.current?.animate(
      [
        { left: `${from}%`, width: '0.5rem' },
        { left: `${Math.min(from, center)}%`, width: `calc(${Math.abs(center - from)}% + 0.5rem)` },
        { left: `${center}%`, width: '0.5rem' }
      ],
      { duration: 260, easing: 'ease-out' }
    )
    return () => animation?.cancel()
  }, [center])

  return (
    <span
      ref={indicator}
      aria-hidden="true"
      style={{ left: `${center}%` }}
      className="pointer-events-none absolute top-1/2 -ml-1 size-2 -translate-y-1/2 rounded-full bg-white shadow-[0_1px_3px_#0004]"
    />
  )
}

/** Rotates current partner banners and the official community link, independently of the Hub. */
export function WorkbenchSponsors() {
  const feed = useQuery({
    queryKey: ['workbench', 'sponsors'],
    queryFn: async ({ signal }) => {
      const body = await getRaw<unknown>(SPONSOR_FEED_PATH, { baseUrl: SPONSOR_BASE, signal })
      return (typeof body === 'object' && body !== null ? body : null) as SponsorFeed | null
    },
    // A marketing feed changes rarely and never within one visit.
    staleTime: 30 * 60_000,
    retry: 0
  })

  const items = React.useMemo(() => [...sponsorsToItems(feed.data ?? null), ...STATIC_ITEMS], [feed.data])

  const [index, setIndex] = React.useState(0)
  const [hovered, setHovered] = React.useState(false)
  const [focused, setFocused] = React.useState(false)
  const count = items.length
  const current = index % count
  const item = items[current]
  const rotating = count > 1 && !hovered && !focused

  React.useEffect(() => {
    if (!rotating) return

    const timer = window.setInterval(() => setIndex(value => (value + 1) % count), AUTOPLAY_MS)
    return () => window.clearInterval(timer)
  }, [count, rotating])

  return (
    <section
      onMouseEnter={() => setHovered(true)}
      onMouseLeave={() => setHovered(false)}
      onFocus={() => setFocused(true)}
      onBlur={event => {
        if (!event.currentTarget.contains(event.relatedTarget)) setFocused(false)
      }}
      aria-labelledby="workbench-sponsors"
      aria-describedby="workbench-sponsor-description"
      className="group/sponsors relative mx-auto w-full max-w-sm min-w-0 overflow-hidden rounded-xl bg-card text-card-foreground"
    >
      <h2 id="workbench-sponsors" className="sr-only">
        赞助与支持
      </h2>
      <p id="workbench-sponsor-description" className="sr-only">
        感谢赞助伙伴对开源社区的支持。
      </p>
      <div>
        <a
          href={item.url}
          target="_blank"
          rel="noreferrer"
          className="block aspect-9/5 w-full focus-visible:outline-offset-[-3px]"
        >
          <SponsorImage key={item.img} item={item} />
        </a>
        {count > 1 && (
          <>
            <div className="pointer-events-none absolute inset-x-1.5 top-1/2 flex -translate-y-1/2 justify-between opacity-0 group-focus-within/sponsors:opacity-100 group-hover/sponsors:opacity-100 motion-safe:transition-opacity">
              {([-1, 1] as const).map(step => (
                <button
                  key={step}
                  type="button"
                  className="grid size-6 cursor-pointer place-items-center rounded-full bg-black/30 text-white group-focus-within/sponsors:pointer-events-auto group-hover/sponsors:pointer-events-auto hover:bg-black/50 focus-visible:outline-2 focus-visible:outline-white"
                  aria-label={step < 0 ? '上一张' : '下一张'}
                  onClick={() => setIndex((current + step + count) % count)}
                >
                  {step < 0 ? <ChevronLeft className="size-4" /> : <ChevronRight className="size-4" />}
                </button>
              ))}
            </div>
            <div
              role="group"
              aria-label="选择轮播图"
              className="absolute bottom-0 left-1/2 max-w-[calc(100%-1rem)] -translate-x-1/2 overflow-x-auto"
            >
              <div className="relative flex w-max">
                {items.map((slide, position) => (
                  <button
                    key={position}
                    type="button"
                    aria-label={`切换到第 ${position + 1} 张：${slide.name}`}
                    aria-current={position === current ? 'true' : undefined}
                    onClick={() => setIndex(position)}
                    className="grid h-6 w-4 cursor-pointer place-items-center rounded-full focus-visible:outline-2 focus-visible:-outline-offset-2 focus-visible:outline-white pointer-coarse:w-6"
                  >
                    <span className="size-2 rounded-full bg-white/20 shadow-[0_1px_3px_#0004]" />
                  </button>
                ))}
                <ActiveDot index={current} count={count} />
              </div>
            </div>
            <span aria-live={rotating ? 'off' : 'polite'} aria-atomic="true" className="sr-only">
              {item.name}
            </span>
          </>
        )}
      </div>
    </section>
  )
}
