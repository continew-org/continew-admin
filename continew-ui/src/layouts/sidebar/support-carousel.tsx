import * as React from 'react'

import useEmblaCarousel, { CarouselDots } from '@animate-ui/react/primitives/carousel'
import { ChevronLeft, ChevronRight } from 'lucide-react'

interface PromoItem {
  readonly name: string
  readonly img: string
  readonly url: string
}

interface SponsorEntry {
  readonly name?: string
  readonly img?: string
  readonly url?: string
}

/** Only the two groups the workbench carousel uses. */
interface SponsorFeed {
  readonly special?: readonly SponsorEntry[]
  readonly platinum?: readonly SponsorEntry[]
}

const SPONSOR_BASE = 'https://continew.top'

/** Always-available fallback promo images, shown before (and if) the sponsor feed loads. */
const STATIC_ITEMS: readonly PromoItem[] = [
  {
    name: '公众号',
    img: `${SPONSOR_BASE}/images/home/join-group.webp`,
    url: SPONSOR_BASE
  },
  {
    name: '赞助',
    img: `${SPONSOR_BASE}/images/qrcode/wxpay.webp`,
    url: `${SPONSOR_BASE}/sponsor`
  }
]

const AUTOPLAY_MS = 4000

/** Sponsor images may be absolute URLs or paths relative to the partners folder. */
function toImageUrl(img: string) {
  return /^https?:\/\//i.test(img) ? img : `${SPONSOR_BASE}/images/sponsor/partners/${img}`
}

function sponsorsToItems(feed: SponsorFeed | null): PromoItem[] {
  if (!feed) {
    return []
  }

  return [...(feed.special ?? []), ...(feed.platinum ?? [])]
    .filter(entry => Boolean(entry.name && entry.img))
    .map(entry => ({
      name: entry.name as string,
      img: toImageUrl(entry.img as string),
      url: entry.url ?? SPONSOR_BASE
    }))
}

/**
 * A small auto-cycling promo carousel for the sidebar footer: the real sponsors from
 * continew.top's sponsor.json (special + platinum), followed by two fallback promo images. Only the
 * image changes — no "Slide x of y" counter and no scale effect on the neighbours.
 */
export function SupportCarousel() {
  const [sponsors, setSponsors] = React.useState<PromoItem[]>([])

  React.useEffect(() => {
    let cancelled = false

    const load = async () => {
      try {
        const response = await fetch(`${SPONSOR_BASE}/sponsor.json`, { cache: 'no-store' })
        if (!response.ok) {
          return
        }
        const feed = (await response.json()) as SponsorFeed
        if (!cancelled) {
          setSponsors(sponsorsToItems(feed))
        }
      } catch {
        // Keep the static promo images when the sponsor feed is unavailable.
      }
    }

    void load()
    return () => {
      cancelled = true
    }
  }, [])

  const items = React.useMemo(() => [...sponsors, ...STATIC_ITEMS], [sponsors])

  const [emblaRef, emblaApi] = useEmblaCarousel({ loop: true, align: 'start' })

  // The slide count changes once the sponsor feed resolves, so re-init the loop.
  React.useEffect(() => {
    emblaApi?.reInit()
  }, [emblaApi, items.length])

  React.useEffect(() => {
    if (!emblaApi || items.length <= 1) {
      return
    }

    const timer = window.setInterval(() => emblaApi.scrollNext(), AUTOPLAY_MS)
    return () => window.clearInterval(timer)
  }, [emblaApi, items.length])

  return (
    <div data-slot="carousel" className="group/carousel relative group-data-[collapsible=icon]:hidden">
      <div ref={emblaRef} className="overflow-hidden rounded-lg border">
        <div className="flex">
          {items.map(item => (
            <a
              key={`${item.name}-${item.img}`}
              href={item.url}
              target="_blank"
              rel="noreferrer"
              className="flex min-w-0 flex-[0_0_100%] items-center justify-center bg-background"
            >
              <img src={item.img} alt={item.name} loading="lazy" className="h-[120px] w-full object-contain" />
            </a>
          ))}
        </div>
      </div>

      {items.length > 1 && (
        <>
          <button
            type="button"
            aria-label="上一张"
            onClick={() => emblaApi?.scrollPrev()}
            className="absolute top-1/2 left-1.5 z-10 flex size-6 -translate-y-1/2 items-center justify-center rounded-full bg-black/30 text-white opacity-0 transition-opacity group-hover/carousel:opacity-100 hover:bg-black/50 focus-visible:opacity-100"
          >
            <ChevronLeft className="size-4" />
          </button>
          <button
            type="button"
            aria-label="下一张"
            onClick={() => emblaApi?.scrollNext()}
            className="absolute top-1/2 right-1.5 z-10 flex size-6 -translate-y-1/2 items-center justify-center rounded-full bg-black/30 text-white opacity-0 transition-opacity group-hover/carousel:opacity-100 hover:bg-black/50 focus-visible:opacity-100"
          >
            <ChevronRight className="size-4" />
          </button>
          <CarouselDots className="absolute bottom-2 left-1/2 z-10 -translate-x-1/2 text-white" />
        </>
      )}
    </div>
  )
}
