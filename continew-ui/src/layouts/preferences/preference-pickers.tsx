import { TinyColor } from '@ctrl/tinycolor'
import { cn } from 'cn'
import { Check, Monitor, Moon, RotateCcw, Sun } from 'lucide-react'
import { useShallow } from 'zustand/react/shallow'
import { motion, useReducedMotion } from 'motion/react'

import { Button } from '@/components/ui/button'
import {
  CONTENT_LAYOUT_VALUES,
  LAYOUT_MODE_OPTIONS,
  RADIUS_OPTIONS,
  TRANSITION_NAME_OPTIONS,
  type LayoutMode
} from '@/lib/preferences/layout'
import type { PreferenceValueMap } from '@/lib/preferences/preferences-config'
import { THEME_COLOR_OPTIONS } from '@/lib/preferences/theme'
import { usePreferencesStore } from '@/stores/preferences/preferences-provider'

import { ChoiceTile, usePreferenceField } from './preference-controls'

const PRIMARY = 'var(--primary)'
const MUTED = 'color-mix(in oklab, currentColor 10%, transparent)'

/** Schematic preview of a layout mode: header, sidebar columns and content blocks. */
function LayoutPreview({ layout }: { layout: LayoutMode }) {
  const headerTop = ['header-nav', 'header-sidebar-nav', 'mixed-nav', 'header-mixed-nav'].includes(layout)
  const header = layout !== 'full-content'
  const root = ['sidebar-mixed-nav', 'header-mixed-nav'].includes(layout)
  const tree = layout === 'sidebar-tree-nav'
  const side = ['sidebar-nav', 'header-sidebar-nav', 'mixed-nav'].includes(layout) || root || tree
  const sideTop = headerTop ? 9 : 0
  const rootW = root ? 9 : 0
  const sideW = side ? (root ? 15 : 22) : 0
  const contentX = rootW + sideW + 4
  const headerX = headerTop ? 0 : rootW + sideW

  return (
    <svg viewBox="0 0 104 66" className="h-14 w-full text-foreground" fill="none" aria-hidden>
      <rect width="104" height="66" rx="4" fill="currentColor" fillOpacity="0.02" />
      {root && <rect x="0" y={sideTop} width={rootW} height={66 - sideTop} fill={PRIMARY} />}
      {side && (
        <rect
          x={rootW}
          y={sideTop}
          width={sideW}
          height={66 - sideTop}
          fill={root ? MUTED : PRIMARY}
          fillOpacity={root ? 1 : 0.9}
        />
      )}
      {tree &&
        [
          [4, 13, 14],
          [8, 19, 10],
          [8, 25, 10],
          [4, 31, 14],
          [4, 37, 14]
        ].map(([x, y, w]) => (
          <rect key={y} x={x} y={y} width={w} height="2.5" rx="1.25" fill="white" fillOpacity="0.7" />
        ))}
      {header && (
        <rect x={headerX} y="0" width={104 - headerX} height="9" fill={headerTop && !side ? PRIMARY : MUTED} />
      )}
      {headerTop &&
        ['header-nav', 'mixed-nav', 'header-mixed-nav'].includes(layout) &&
        [30, 42, 54].map(x => (
          <rect key={x} x={x} y="3" width="8" height="3" rx="1.5" fill="currentColor" fillOpacity="0.35" />
        ))}
      <rect x={contentX} y={header ? 13 : 4} width={98 - contentX} height="22" rx="2" fill={MUTED} />
      <rect x={contentX} y={header ? 39 : 30} width={98 - contentX} height="22" rx="2" fill={MUTED} />
    </svg>
  )
}

export function LayoutModePicker() {
  const [layout, setLayout] = usePreferenceField('layout')

  return (
    <div className="grid grid-cols-3 gap-3 px-2 py-1">
      {LAYOUT_MODE_OPTIONS.map(option => (
        <ChoiceTile
          key={option.value}
          active={layout === option.value}
          label={option.label}
          title={option.description}
          onClick={() => setLayout(option.value)}
        >
          <LayoutPreview layout={option.value} />
          {layout === option.value && (
            <Check className="absolute right-1 bottom-1 size-3.5 text-primary" strokeWidth={3} />
          )}
        </ChoiceTile>
      ))}
    </div>
  )
}

export function ContentModePicker() {
  const [mode, setMode] = usePreferenceField('content_layout')

  return (
    <div className="grid grid-cols-2 gap-3 px-2 py-1">
      {CONTENT_LAYOUT_VALUES.map(value => (
        <ChoiceTile
          key={value}
          active={mode === value}
          label={value === 'full-width' ? '流式' : '定宽'}
          onClick={() => setMode(value)}
        >
          <svg viewBox="0 0 104 66" className="h-14 w-full" fill="none" aria-hidden>
            <rect width="104" height="9" fill={MUTED} />
            <rect
              x={value === 'full-width' ? 4 : 18}
              y="14"
              width={value === 'full-width' ? 96 : 68}
              height="22"
              rx="2"
              fill={MUTED}
            />
            <rect
              x={value === 'full-width' ? 4 : 18}
              y="40"
              width={value === 'full-width' ? 96 : 68}
              height="22"
              rx="2"
              fill={MUTED}
            />
          </svg>
        </ChoiceTile>
      ))}
    </div>
  )
}

const THEME_MODES = [
  { value: 'light', label: '浅色', icon: Sun },
  { value: 'dark', label: '深色', icon: Moon },
  { value: 'system', label: '跟随系统', icon: Monitor }
] as const

export function ThemeModePicker() {
  const [mode, setMode] = usePreferenceField('theme_mode')

  return (
    <div className="grid grid-cols-3 gap-3 px-2 py-1">
      {THEME_MODES.map(({ value, label, icon: Icon }) => (
        <ChoiceTile key={value} active={mode === value} label={label} onClick={() => setMode(value)}>
          <span className="flex h-12 items-center justify-center">
            <Icon className="size-5" />
          </span>
        </ChoiceTile>
      ))}
    </div>
  )
}

/** Built-in palettes. Picking one clears a custom primary colour so the palette shows through. */
export function ThemeColorGrid() {
  const { color, dark, setPreferences } = usePreferencesStore(
    useShallow(state => ({
      color: state.values.theme_color,
      dark: state.resolvedThemeMode === 'dark',
      setPreferences: state.setPreferences
    }))
  )
  const custom = usePreferencesStore(state => state.values.theme_color_primary)

  return (
    <div className="grid grid-cols-5 gap-3 px-2 py-1">
      {THEME_COLOR_OPTIONS.map(option => {
        const active = !custom && color === option.value
        return (
          <ChoiceTile
            key={option.value}
            active={active}
            label={option.label}
            onClick={() => setPreferences({ theme_color: option.value, theme_color_primary: '' })}
          >
            <span className="flex h-9 w-full items-center justify-center">
              <span
                className="size-5 rounded-full"
                style={{ backgroundColor: dark ? option.primary.dark : option.primary.light }}
              />
            </span>
          </ChoiceTile>
        )
      })}
    </div>
  )
}

/** Converts any CSS colour the user saved into the #rrggbb value `<input type="color">` needs. */
function toHex(value: string, fallback: string) {
  const color = new TinyColor(value || fallback)
  return color.isValid ? color.toHexString() : '#1677ff'
}

const SEMANTIC_COLORS = [
  { field: 'theme_color_primary', label: '主色', fallback: '#3b82f6', cssVar: '--primary' },
  { field: 'theme_color_success', label: '成功色', fallback: '#10b981', cssVar: '--status-success' },
  { field: 'theme_color_warning', label: '警告色', fallback: '#f59e0b', cssVar: '--status-warning' },
  { field: 'theme_color_destructive', label: '危险色', fallback: '#ef4444', cssVar: '--destructive' }
] as const satisfies readonly { field: keyof PreferenceValueMap; label: string; fallback: string; cssVar: string }[]

function ColorField({ field, label, fallback, cssVar }: (typeof SEMANTIC_COLORS)[number]) {
  const [value, setValue] = usePreferenceField(field)

  return (
    <div className="flex min-h-10 items-center justify-between gap-3 rounded-md px-2 py-1.5 hover:bg-accent/50">
      <span className="text-sm">{label}</span>
      <div className="flex items-center gap-1.5">
        <span className="font-mono text-xs text-muted-foreground uppercase">{value || '默认'}</span>
        <label className="relative size-7 cursor-pointer overflow-hidden rounded-md border shadow-xs">
          <span className="absolute inset-0" style={{ backgroundColor: value || `var(${cssVar})` }} />
          <input
            type="color"
            aria-label={label}
            className="absolute inset-0 cursor-pointer opacity-0"
            value={toHex(value, fallback)}
            // `change` fires when the picker closes; `input` would reflow the whole page on every drag step.
            onChange={event => setValue(event.target.value)}
          />
        </label>
        <Button
          variant="ghost"
          size="icon-xs"
          aria-label={`重置${label}`}
          disabled={!value}
          onClick={() => setValue('')}
          className={cn(!value && 'invisible')}
        >
          <RotateCcw />
        </Button>
      </div>
    </div>
  )
}

export function SemanticColorFields() {
  return SEMANTIC_COLORS.map(color => <ColorField key={color.field} {...color} />)
}

export function RadiusPicker() {
  const [radius, setRadius] = usePreferenceField('radius')

  return (
    <div className="grid grid-cols-5 gap-2 px-2 py-1">
      {RADIUS_OPTIONS.map(option => (
        <button
          key={option.value}
          type="button"
          aria-pressed={radius === option.value}
          onClick={() => setRadius(option.value)}
          className={cn(
            'h-8 rounded-md border text-xs transition-colors',
            radius === option.value
              ? 'border-primary bg-primary/10 font-medium text-primary'
              : 'hover:border-primary/50'
          )}
        >
          {option.label}
        </button>
      ))}
    </div>
  )
}

export function TransitionPicker() {
  const [name, setName] = usePreferenceField('transition_name')
  const reduceMotion = useReducedMotion()

  return (
    <div className="grid grid-cols-4 gap-3 px-2 py-1">
      {TRANSITION_NAME_OPTIONS.map(option => (
        <ChoiceTile
          key={option.value}
          active={name === option.value}
          label={option.label}
          onClick={() => setName(option.value)}
        >
          <span className="flex h-12 w-full items-center justify-center">
            {option.value === 'fade-shake' ? (
              <motion.span
                className="h-7 w-8 rounded-sm bg-primary"
                animate={reduceMotion ? { opacity: 1 } : { opacity: [0, 1, 1, 1, 1, 1], x: [0, -8, 6, -4, 2, 0] }}
                transition={{ duration: 0.38, ease: 'easeOut', repeat: Infinity, repeatDelay: 1 }}
              />
            ) : (
              <span className={cn('h-7 w-8 rounded-sm bg-primary', `transition-preview-${option.value}`)} />
            )}
          </span>
        </ChoiceTile>
      ))}
    </div>
  )
}
