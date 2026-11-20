import type { ReactNode } from 'react'

import { X } from 'lucide-react'

import { cn } from 'cn'

import { Button } from '@/components/ui/button'
import { Drawer, DrawerClose, DrawerContent, DrawerHeader, DrawerTitle } from '@/components/ui/drawer'

/** The drawer widths the screens use; the Tailwind class must stay a literal to be generated. */
type EntityDrawerWidth = 30 | 32 | 36 | 38 | 40 | 42 | 44 | 46 | 48

const WIDTH_CLASSES: Record<EntityDrawerWidth, string> = {
  30: 'data-[swipe-axis=x]:sm:[--drawer-content-width:30rem]',
  32: 'data-[swipe-axis=x]:sm:[--drawer-content-width:32rem]',
  36: 'data-[swipe-axis=x]:sm:[--drawer-content-width:36rem]',
  38: 'data-[swipe-axis=x]:sm:[--drawer-content-width:38rem]',
  40: 'data-[swipe-axis=x]:sm:[--drawer-content-width:40rem]',
  42: 'data-[swipe-axis=x]:sm:[--drawer-content-width:42rem]',
  44: 'data-[swipe-axis=x]:sm:[--drawer-content-width:44rem]',
  46: 'data-[swipe-axis=x]:sm:[--drawer-content-width:46rem]',
  48: 'data-[swipe-axis=x]:sm:[--drawer-content-width:48rem]'
}

interface EntityDrawerProps {
  readonly open: boolean
  readonly onClose: () => void
  readonly title: string
  readonly icon?: ReactNode
  /** A short tag next to the title, such as the selected record's name. */
  readonly badge?: ReactNode
  /** A secondary line under the title. */
  readonly description?: string
  readonly width?: EntityDrawerWidth
  /** Extra classes for the drawer panel, such as a mobile width override. */
  readonly contentClassName?: string
  /** Disables the close button outright, for states that re-render while saving. */
  readonly closeDisabled?: boolean
  /** Return false to keep the drawer open, such as while a save is in flight. */
  readonly canClose?: () => boolean
  readonly children: ReactNode
}

/**
 * The right-side drawer shell shared by the entity screens: width variable, header with the
 * record icon and title, and a labelled close button. The body and any footer stay with the screen
 * because forms need to submit from their own footer.
 */
export function EntityDrawer({
  open,
  onClose,
  title,
  icon,
  badge,
  description,
  width = 32,
  contentClassName,
  closeDisabled = false,
  canClose,
  children
}: EntityDrawerProps) {
  return (
    <Drawer
      open={open}
      onOpenChange={nextOpen => {
        if (!nextOpen && !closeDisabled && (canClose?.() ?? true)) {
          onClose()
        }
      }}
      swipeDirection="right"
    >
      <DrawerContent className={cn(WIDTH_CLASSES[width], contentClassName)}>
        <DrawerHeader
          className={cn(
            'flex-row justify-between gap-2 border-b p-4 md:gap-2',
            description ? 'items-start' : 'items-center'
          )}
        >
          <div className="flex flex-col gap-1">
            <div className="flex items-center gap-2">
              {icon}
              <DrawerTitle>{title}</DrawerTitle>
              {badge}
            </div>
            {description ? <p className="text-xs text-muted-foreground">{description}</p> : null}
          </div>

          <DrawerClose render={<Button variant="ghost" size="icon-sm" disabled={closeDisabled} />}>
            <X />
            <span className="sr-only">关闭</span>
          </DrawerClose>
        </DrawerHeader>

        {children}
      </DrawerContent>
    </Drawer>
  )
}
