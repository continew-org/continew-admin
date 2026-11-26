import * as React from 'react'

import { DragDropProvider } from '@dnd-kit/react'
import { isSortable, useSortable } from '@dnd-kit/react/sortable'
import { Link, useRouter, type RegisteredRouter } from '@tanstack/react-router'
import { cn } from 'cn'
import {
  ArrowLeftToLine,
  ArrowRightToLine,
  Ban,
  ChevronDown,
  Fullscreen,
  Layers,
  Minimize2,
  Pin,
  PinOff,
  RotateCw,
  X
} from 'lucide-react'
import { useShallow } from 'zustand/react/shallow'

import {
  ContextMenu,
  ContextMenuContent,
  ContextMenuItem,
  ContextMenuSeparator,
  ContextMenuTrigger
} from '@/components/ui/context-menu'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger
} from '@/components/ui/dropdown-menu'
import { useMenuRoutes } from '@/hooks/use-menu'
import type { TabbarStyle } from '@/lib/preferences/layout'
import { resolveMenuIcon } from '@/navigation/menu-icons'
import { usePreferencesStore } from '@/stores/preferences/preferences-provider'
import { DEFAULT_PINNED_TAB, type TabItem, useTabsStore } from '@/stores/tabs'

import { resolveRouteInfo, usePathname } from '../route-info'
import { useShellStore } from '../shell-store'

/** Visual style of each tab, keyed by `tabbar_style`. */
const TAB_STYLES: Record<TabbarStyle, { base: string; active: string; idle: string }> = {
  chrome: {
    base: 'rounded-t-lg mt-auto h-[calc(100%-4px)] px-3',
    active: 'bg-background text-foreground shadow-[0_-1px_0_var(--border)]',
    idle: 'text-muted-foreground hover:bg-accent/60 hover:text-foreground'
  },
  plain: {
    base: 'h-full border-r px-3',
    active: 'bg-background text-primary',
    idle: 'text-muted-foreground hover:text-foreground'
  },
  card: {
    base: 'my-1 h-[calc(100%-8px)] rounded-md border px-3',
    active: 'border-primary/40 bg-primary/10 text-primary',
    idle: 'bg-background text-muted-foreground hover:text-foreground'
  },
  brisk: {
    base: 'h-full px-3 after:absolute after:inset-x-3 after:bottom-0 after:h-0.5 after:origin-center after:scale-x-0 after:rounded-full after:bg-primary after:transition-transform',
    active: 'text-primary after:scale-x-100',
    idle: 'text-muted-foreground hover:text-foreground'
  }
}

function useTabActions() {
  const router = useRouter()
  const pathname = usePathname()
  const refresh = useShellStore(state => state.refresh)
  const visitHistory = usePreferencesStore(state => state.values.tabbar_visit_history)

  return React.useMemo(() => {
    const go = (path: string) => {
      if (path !== pathname) void router.navigate({ href: path })
    }
    const ensureActiveTab = () => {
      const { tabs } = useTabsStore.getState()
      if (!tabs.some(tab => tab.path === pathname)) go(tabs.at(-1)?.path ?? DEFAULT_PINNED_TAB.path)
    }

    return {
      close: (tab: TabItem) => {
        const next = useTabsStore.getState().closeTab(tab.path, visitHistory)
        if (tab.path === pathname && next) go(next)
      },
      closeOthers: (tab: TabItem) => {
        useTabsStore.getState().closeOtherTabs(tab.path)
        go(tab.path)
      },
      closeLeft: (tab: TabItem) => {
        useTabsStore.getState().closeLeftTabs(tab.path)
        ensureActiveTab()
      },
      closeRight: (tab: TabItem) => {
        useTabsStore.getState().closeRightTabs(tab.path)
        ensureActiveTab()
      },
      closeAll: () => {
        useTabsStore.getState().closeAllTabs()
        ensureActiveTab()
      },
      togglePin: (tab: TabItem) => useTabsStore.getState().togglePin(tab.path),
      // A tab that is not open yet is visited first; the refresh then remounts the page there.
      refresh: (tab: TabItem) => {
        if (tab.path === pathname) {
          refresh()
          void router.invalidate()
        } else {
          void router.navigate({ href: tab.path }).then(() => {
            refresh()
            void router.invalidate()
          })
        }
      }
    }
  }, [pathname, refresh, router, visitHistory])
}

type TabActions = ReturnType<typeof useTabActions>

function TabMenuItems({
  tab,
  index,
  tabs,
  actions,
  Item,
  Separator
}: {
  tab: TabItem
  index: number
  tabs: TabItem[]
  actions: TabActions
  Item: typeof ContextMenuItem | typeof DropdownMenuItem
  Separator: typeof ContextMenuSeparator | typeof DropdownMenuSeparator
}) {
  const hasClosableLeft = tabs.slice(0, index).some(t => t.closable)
  const hasClosableRight = tabs.slice(index + 1).some(t => t.closable)
  const hasOthers = tabs.some(t => t.closable && t.path !== tab.path)
  const canPin = tab.path !== DEFAULT_PINNED_TAB.path

  return (
    <>
      <Item onClick={() => actions.refresh(tab)}>
        <RotateCw />
        重新加载
      </Item>
      {canPin && (
        <Item onClick={() => actions.togglePin(tab)}>
          {tab.closable ? <Pin /> : <PinOff />}
          {tab.closable ? '固定标签' : '取消固定'}
        </Item>
      )}
      <Separator />
      <Item disabled={!tab.closable} onClick={() => actions.close(tab)}>
        <X />
        关闭当前
      </Item>
      <Item disabled={!hasClosableLeft} onClick={() => actions.closeLeft(tab)}>
        <ArrowLeftToLine />
        关闭左侧
      </Item>
      <Item disabled={!hasClosableRight} onClick={() => actions.closeRight(tab)}>
        <ArrowRightToLine />
        关闭右侧
      </Item>
      <Item disabled={!hasOthers} onClick={() => actions.closeOthers(tab)}>
        <Layers />
        关闭其他
      </Item>
      <Item disabled={!tabs.some(t => t.closable)} onClick={actions.closeAll}>
        <Ban />
        关闭全部
      </Item>
    </>
  )
}

interface TabProps {
  tab: TabItem
  index: number
  tabs: TabItem[]
  isActive: boolean
  actions: TabActions
  styleType: TabbarStyle
  showIcon: boolean
  draggable: boolean
  middleClickClose: boolean
}

const Tab = React.memo(function Tab({
  tab,
  index,
  tabs,
  isActive,
  actions,
  styleType,
  showIcon,
  draggable,
  middleClickClose
}: TabProps) {
  const { ref, isDragging } = useSortable({
    id: tab.path,
    index,
    disabled: !draggable,
    group: tab.closable ? 'closable' : 'pinned'
  })
  const style = TAB_STYLES[styleType]

  return (
    <ContextMenu>
      <ContextMenuTrigger
        render={
          <div
            ref={ref}
            data-tab-path={tab.path}
            data-active={isActive || undefined}
            onAuxClick={event => {
              if (middleClickClose && event.button === 1 && tab.closable) {
                event.preventDefault()
                actions.close(tab)
              }
            }}
            className={cn(
              'group relative flex max-w-40 min-w-0 shrink-0 items-center gap-1.5 text-xs transition-colors duration-150 select-none',
              style.base,
              isActive ? style.active : style.idle,
              isDragging && 'z-10 opacity-80 shadow-md'
            )}
          />
        }
      >
        <Link<RegisteredRouter, string, string>
          href={tab.path}
          draggable={false}
          className="flex h-full min-w-0 flex-1 items-center gap-1.5 outline-none"
        >
          {showIcon && React.createElement(resolveMenuIcon(tab.icon), { className: 'size-3.5 shrink-0' })}
          <span className="truncate">{tab.title}</span>
        </Link>
        {tab.closable ? (
          <button
            type="button"
            onClick={() => actions.close(tab)}
            className={cn(
              'flex size-4 shrink-0 items-center justify-center rounded-sm transition-opacity hover:bg-foreground/10',
              isActive ? 'opacity-60 hover:opacity-100' : 'opacity-0 group-hover:opacity-60 hover:opacity-100'
            )}
          >
            <X className="size-3" />
            <span className="sr-only">关闭 {tab.title}</span>
          </button>
        ) : (
          tab.path !== DEFAULT_PINNED_TAB.path && <Pin className="size-3 shrink-0 opacity-50" />
        )}
      </ContextMenuTrigger>
      <ContextMenuContent className="min-w-36">
        <TabMenuItems
          tab={tab}
          index={index}
          tabs={tabs}
          actions={actions}
          Item={ContextMenuItem}
          Separator={ContextMenuSeparator}
        />
      </ContextMenuContent>
    </ContextMenu>
  )
})

function TabbarTool({ label, ...props }: React.ComponentProps<'button'> & { label: string }) {
  return (
    <button
      type="button"
      aria-label={label}
      title={label}
      {...props}
      className="flex h-full w-9 shrink-0 items-center justify-center border-l text-muted-foreground transition-colors hover:bg-accent hover:text-foreground [&_svg]:size-3.5"
    />
  )
}

/**
 * Multi-tab bar under the header. Adds a tab for every page the user opens, keeps it in step
 * with the router, and supports pin, drag sorting, wheel scrolling and the close menu.
 */
export function Tabbar() {
  const router = useRouter()
  const pathname = usePathname()
  const { data: menuRoutes } = useMenuRoutes()
  const tabs = useTabsStore(state => state.tabs)
  const actions = useTabActions()
  const scrollRef = React.useRef<HTMLDivElement>(null)

  const prefs = usePreferencesStore(
    useShallow(state => ({
      styleType: state.values.tabbar_style,
      showIcon: state.values.tabbar_show_icon,
      draggable: state.values.tabbar_draggable,
      wheelable: state.values.tabbar_wheelable,
      middleClickClose: state.values.tabbar_middle_click_close,
      maxCount: state.values.tabbar_max_count,
      showMore: state.values.tabbar_show_more,
      showRefresh: state.values.tabbar_show_refresh,
      showMaximize: state.values.tabbar_show_maximize
    }))
  )
  const contentMaximized = useShellStore(state => state.contentMaximized)
  const setContentMaximized = useShellStore(state => state.setContentMaximized)

  // Record the page as a tab in the same frame it renders, instead of one paint later.
  React.useLayoutEffect(() => {
    const info = resolveRouteInfo(pathname, menuRoutes ?? [], router)
    if (!info) return

    useTabsStore
      .getState()
      .addTab(
        { path: pathname, title: info.title, icon: info.icon, closable: pathname !== DEFAULT_PINNED_TAB.path },
        prefs.maxCount
      )
  }, [pathname, menuRoutes, router, prefs.maxCount])

  // Drop tabs restored from pages that no longer exist for this user.
  React.useEffect(() => {
    if (menuRoutes) {
      useTabsStore.getState().pruneTabs(path => resolveRouteInfo(path, menuRoutes, router) !== null)
    }
  }, [menuRoutes, router])

  // Keep the active tab in view.
  React.useEffect(() => {
    const node = scrollRef.current?.querySelector<HTMLElement>(`[data-tab-path="${CSS.escape(pathname)}"]`)
    node?.scrollIntoView({ block: 'nearest', inline: 'nearest', behavior: 'smooth' })
  }, [pathname, tabs.length])

  // Vertical wheel scrolls the strip sideways; a native listener so it can be non-passive.
  React.useEffect(() => {
    const node = scrollRef.current
    if (!node || !prefs.wheelable) return

    const onWheel = (event: WheelEvent) => {
      if (Math.abs(event.deltaY) <= Math.abs(event.deltaX)) return
      event.preventDefault()
      node.scrollLeft += event.deltaY
    }

    node.addEventListener('wheel', onWheel, { passive: false })
    return () => node.removeEventListener('wheel', onWheel)
  }, [prefs.wheelable])

  const activeIndex = tabs.findIndex(tab => tab.path === pathname)
  const activeTab = tabs[activeIndex]

  return (
    <div
      data-slot="app-tabbar"
      data-style={prefs.styleType}
      className={cn(
        'flex h-(--app-tabbar-height) shrink-0 items-stretch border-b bg-muted/30',
        prefs.styleType === 'chrome' && 'bg-muted/60'
      )}
    >
      <DragDropProvider
        onDragOver={event => {
          // Pinned and closable tabs reorder within their own section only.
          const { source, target } = event.operation
          if (isSortable(source) && isSortable(target) && source.group !== target.group) event.preventDefault()
        }}
        onDragEnd={event => {
          const { source } = event.operation
          // The optimistic sorting plugin already moved the DOM, so the target is the source
          // itself; the new position comes from the sortable indices instead.
          if (event.canceled || !isSortable(source)) return
          const { initialIndex, index } = source
          if (initialIndex !== index) useTabsStore.getState().moveTab(initialIndex, index)
        }}
      >
        <div
          ref={scrollRef}
          className="flex min-w-0 flex-1 scrollbar-none items-stretch gap-0.5 overflow-x-auto px-1.5"
        >
          {tabs.map((tab, index) => (
            <Tab
              key={tab.id}
              tab={tab}
              index={index}
              tabs={tabs}
              isActive={tab.path === pathname}
              actions={actions}
              styleType={prefs.styleType}
              showIcon={prefs.showIcon}
              draggable={prefs.draggable}
              middleClickClose={prefs.middleClickClose}
            />
          ))}
        </div>
      </DragDropProvider>

      {prefs.showMore && activeTab && (
        <DropdownMenu>
          <DropdownMenuTrigger render={<TabbarTool label="更多操作" />}>
            <ChevronDown />
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end" className="min-w-36">
            <TabMenuItems
              tab={activeTab}
              index={activeIndex}
              tabs={tabs}
              actions={actions}
              Item={DropdownMenuItem}
              Separator={DropdownMenuSeparator}
            />
          </DropdownMenuContent>
        </DropdownMenu>
      )}
      {prefs.showRefresh && activeTab && (
        <TabbarTool label="刷新" onClick={() => actions.refresh(activeTab)}>
          <RotateCw />
        </TabbarTool>
      )}
      {prefs.showMaximize && (
        <TabbarTool label={contentMaximized ? '还原' : '最大化'} onClick={() => setContentMaximized(!contentMaximized)}>
          {contentMaximized ? <Minimize2 /> : <Fullscreen />}
        </TabbarTool>
      )}
    </div>
  )
}
