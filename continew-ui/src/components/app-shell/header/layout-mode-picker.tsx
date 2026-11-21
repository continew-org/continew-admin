import { cn } from 'cn'
import { Check } from 'lucide-react'

import { LAYOUT_MODE_OPTIONS, type LayoutMode } from '@/lib/preferences/layout'

/**
 * Schematic preview of a layout mode, drawn from the mode's flags: a header band, one or two
 * sidebar columns and content blocks. Rendered with the app's own tokens (primary + currentColor)
 * and softly rounded corners.
 */
function LayoutPreview({ mode }: { mode: LayoutMode }) {
  const headerTop = ['header-nav', 'header-sidebar-nav', 'mixed-nav', 'header-mixed-nav'].includes(mode)
  const showHeader = true
  const primary = ['sidebar-nav', 'header-sidebar-nav', 'mixed-nav'].includes(mode)
  const mixed = ['sidebar-mixed-nav', 'header-mixed-nav'].includes(mode)
  const hasMenu = ['header-nav', 'mixed-nav', 'header-mixed-nav'].includes(mode)

  const top = headerTop ? 13 : 0
  const rootW = mixed ? 14 : 0
  const sideW = primary ? 24 : mixed ? 30 : 0
  const contentX = rootW + sideW + 4

  return (
    <svg viewBox="0 0 120 74" className="h-14 w-full" fill="none" aria-hidden>
      <rect width="120" height="74" rx="10" fill="currentColor" fillOpacity="0.035" />

      {mixed && <rect x="0" y={top} width={rootW} height={74 - top} rx="4" fill="var(--primary)" fillOpacity="0.9" />}
      {primary && <rect x="0" y={top} width={sideW} height={74 - top} rx="4" fill="var(--primary)" fillOpacity="0.9" />}
      {mixed && (
        <rect x={rootW} y={top} width={sideW} height={74 - top} rx="4" fill="currentColor" fillOpacity="0.12" />
      )}

      {showHeader && (
        <rect
          x={headerTop ? 0 : rootW + sideW}
          y="0"
          width={headerTop ? 120 : 120 - (rootW + sideW)}
          height="9"
          rx="4"
          fill={headerTop ? 'var(--primary)' : 'currentColor'}
          fillOpacity={headerTop ? 0.9 : 0.12}
        />
      )}
      {hasMenu &&
        [38, 50, 62].map(x => (
          <rect key={x} x={x} y="3" width="8" height="3" rx="1.5" fill="currentColor" fillOpacity="0.5" />
        ))}

      <rect
        x={contentX}
        y={showHeader ? 16 : 5}
        width={116 - contentX}
        height="23"
        rx="5"
        fill="currentColor"
        fillOpacity="0.1"
      />
      <rect
        x={contentX}
        y={showHeader ? 44 : 35}
        width={116 - contentX}
        height="23"
        rx="5"
        fill="currentColor"
        fillOpacity="0.1"
      />
    </svg>
  )
}

/** Visual layout-mode selector: one card per mode with a schematic preview. */
export function LayoutModePicker({ value, onChange }: { value: LayoutMode; onChange: (mode: LayoutMode) => void }) {
  return (
    <div className="grid grid-cols-3 gap-2">
      {LAYOUT_MODE_OPTIONS.map(option => {
        const active = option.value === value

        return (
          <button
            key={option.value}
            type="button"
            title={option.description}
            aria-pressed={active}
            onClick={() => onChange(option.value)}
            className={cn(
              'relative flex flex-col items-center gap-1.5 rounded-xl border p-2 text-center transition-colors',
              active ? 'border-primary bg-primary/5' : 'border-border hover:border-primary/50'
            )}
          >
            <LayoutPreview mode={option.value} />
            <span
              className={cn(
                'w-full truncate text-center text-[11px] leading-none',
                active ? 'font-medium text-primary' : 'text-muted-foreground'
              )}
            >
              {option.label}
            </span>
            {active && <Check className="absolute top-1.5 right-1.5 size-3 text-primary" strokeWidth={3} />}
          </button>
        )
      })}
    </div>
  )
}
