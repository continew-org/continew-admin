import * as React from 'react'

import { ScrollArea as ScrollAreaPrimitive } from '@base-ui/react/scroll-area'
import { cn } from 'cn'

interface TableProps extends React.ComponentProps<'table'> {
  /**
   * Scroll inside this element instead of the page, using overlay scrollbars. The vertical bar sits
   * below the sticky header, so it never runs across the header row or steals width from it.
   */
  readonly scrollArea?: boolean
}

function Table({ className, scrollArea = false, ...props }: TableProps) {
  const containerRef = React.useRef<HTMLDivElement>(null)

  // Hand the sticky header's height to the scrollbar, which insets its top by that amount. Re-measure
  // when the header changes size (density, column visibility, entering fullscreen).
  React.useLayoutEffect(() => {
    if (!scrollArea) return

    const container = containerRef.current
    const header = container?.querySelector<HTMLElement>('[data-slot="table-header"]')
    if (!container || !header) return

    const measure = () => {
      // Only a sticky header stays at the top while scrolling; otherwise the bar should use the whole
      // track and start at the top edge.
      const headerCell = header.querySelector('th')
      const isSticky = headerCell ? getComputedStyle(headerCell).position === 'sticky' : false
      const height = isSticky ? header.getBoundingClientRect().height : 0
      container.style.setProperty('--table-header-height', `${height}px`)
    }

    measure()
    const observer = new ResizeObserver(measure)
    observer.observe(header)
    return () => observer.disconnect()
  }, [scrollArea])

  const table = (
    <table data-slot="table" className={cn('w-full caption-bottom text-sm', className)} {...props} />
  )

  if (!scrollArea) {
    return (
      <div data-slot="table-container" className="relative w-full overflow-x-auto">
        {table}
      </div>
    )
  }

  return (
    <ScrollAreaPrimitive.Root ref={containerRef} data-slot="table-container" className="relative w-full">
      <ScrollAreaPrimitive.Viewport
        data-slot="table-viewport"
        className="size-full max-h-[inherit] outline-none focus-visible:ring-[3px] focus-visible:ring-ring/50"
      >
        {table}
      </ScrollAreaPrimitive.Viewport>
      <TableScrollbar orientation="vertical" />
      <TableScrollbar orientation="horizontal" />
      <ScrollAreaPrimitive.Corner />
    </ScrollAreaPrimitive.Root>
  )
}

function TableScrollbar({ orientation }: { readonly orientation: 'horizontal' | 'vertical' }) {
  return (
    <ScrollAreaPrimitive.Scrollbar
      data-slot="table-scrollbar"
      orientation={orientation}
      // Overlay scrollbar: floats over the rows and fades in only while hovering or scrolling, so it
      // never reserves width next to the table header the way the native scrollbar did.
      className={cn(
        'flex touch-none p-px opacity-0 transition-opacity select-none data-hovering:opacity-100 data-scrolling:opacity-100',
        orientation === 'vertical' ? 'w-2.5' : 'h-2.5 flex-col'
      )}
      // Base UI pins the vertical bar to the top edge; start it at the bottom of the sticky header.
      style={orientation === 'vertical' ? { top: 'var(--table-header-height, 0px)' } : undefined}
    >
      <ScrollAreaPrimitive.Thumb
        data-slot="table-scrollbar-thumb"
        className="relative flex-1 rounded-full bg-border"
      />
    </ScrollAreaPrimitive.Scrollbar>
  )
}

function TableHeader({ className, ...props }: React.ComponentProps<'thead'>) {
  return <thead data-slot="table-header" className={cn('[&_tr]:border-b', className)} {...props} />
}

function TableBody({ className, ...props }: React.ComponentProps<'tbody'>) {
  return <tbody data-slot="table-body" className={cn('[&_tr:last-child]:border-0', className)} {...props} />
}

function TableFooter({ className, ...props }: React.ComponentProps<'tfoot'>) {
  return (
    <tfoot
      data-slot="table-footer"
      className={cn('border-t bg-muted/50 font-medium [&>tr]:last:border-b-0', className)}
      {...props}
    />
  )
}

function TableRow({ className, ...props }: React.ComponentProps<'tr'>) {
  return (
    <tr
      data-slot="table-row"
      className={cn(
        'border-b transition-colors hover:bg-muted/50 has-aria-expanded:bg-muted/50 data-[state=selected]:bg-muted',
        className
      )}
      {...props}
    />
  )
}

function TableHead({ className, ...props }: React.ComponentProps<'th'>) {
  return (
    <th
      data-slot="table-head"
      className={cn(
        'h-10 px-2 text-left align-middle font-medium whitespace-nowrap text-foreground [&:has([role=checkbox])]:pr-0',
        className
      )}
      {...props}
    />
  )
}

function TableCell({ className, ...props }: React.ComponentProps<'td'>) {
  return (
    <td
      data-slot="table-cell"
      className={cn('p-2 align-middle whitespace-nowrap [&:has([role=checkbox])]:pr-0', className)}
      {...props}
    />
  )
}

function TableCaption({ className, ...props }: React.ComponentProps<'caption'>) {
  return (
    <caption data-slot="table-caption" className={cn('mt-4 text-sm text-muted-foreground', className)} {...props} />
  )
}

export { Table, TableHeader, TableBody, TableFooter, TableHead, TableRow, TableCell, TableCaption }
