import * as React from 'react'

import { useSelector } from '@tanstack/react-form'

import { Globe, LayoutDashboard, Lock, LogIn } from 'lucide-react'

import { cn } from 'cn'

interface SitePreviewValues {
  SITE_LOGO: string
  SITE_FAVICON: string
  SITE_TITLE: string
  SITE_DESCRIPTION: string
  SITE_COPYRIGHT: string
  SITE_BEIAN: string
}

interface SitePreviewProps<TSource extends { values: SitePreviewValues }> {
  /** The form store: subscribing here keeps the panel out of every keystroke's re-render. */
  store: {
    get: () => TSource
    subscribe: (listener: (value: TSource) => void) => { unsubscribe: () => void }
  }
}

const PREVIEW_MODES = [
  { mode: 'admin', label: '工作台视图', icon: LayoutDashboard },
  { mode: 'login', label: '登录页视图', icon: LogIn }
] as const

export function SitePreview<TSource extends { values: SitePreviewValues }>({ store }: SitePreviewProps<TSource>) {
  const values = useSelector(store, state => state.values)
  const [previewMode, setPreviewMode] = React.useState<'admin' | 'login'>('admin')

  return (
    <div className="hidden flex-col overflow-hidden rounded-xl border bg-card xl:flex">
      <div className="flex items-center justify-between border-b px-4 py-2.5 sm:px-5">
        <h2 className="text-base font-semibold">效果预览</h2>
        <div className="flex items-center rounded-lg border bg-muted/40 p-0.5">
          {PREVIEW_MODES.map(({ mode, label, icon: Icon }) => (
            <button
              key={mode}
              type="button"
              onClick={() => setPreviewMode(mode)}
              className={cn(
                'flex items-center gap-1.5 rounded-md px-2.5 py-1 text-xs font-medium transition-colors',
                previewMode === mode ? 'bg-background text-foreground' : 'text-muted-foreground hover:text-foreground'
              )}
            >
              <Icon className="size-3.5" />
              {label}
            </button>
          ))}
        </div>
      </div>

      <div className="relative flex flex-1 flex-col bg-muted/30 p-3.5 sm:p-4">
        <div className="flex flex-1 flex-col overflow-hidden rounded-xl border border-border/70 bg-background">
          <div className="flex items-center gap-2.5 border-b bg-muted/50 px-3 py-2 text-xs">
            <div className="flex shrink-0 items-center gap-1.5 pr-1">
              <span className="size-2.5 rounded-full bg-red-400/80" />
              <span className="size-2.5 rounded-full bg-amber-400/80" />
              <span className="size-2.5 rounded-full bg-emerald-400/80" />
            </div>

            <div className="flex max-w-[150px] items-center gap-1.5 rounded-md border border-border/60 bg-background px-2.5 py-0.5 font-sans text-xs font-medium text-foreground">
              {values.SITE_FAVICON ? (
                <img src={values.SITE_FAVICON} alt="Favicon" className="size-3.5 shrink-0 object-contain" />
              ) : (
                <Globe className="size-3.5 shrink-0 text-primary" />
              )}
              <span className="truncate">{values.SITE_TITLE || '系统名称'}</span>
            </div>

            <div className="hidden flex-1 items-center justify-center sm:flex">
              <div className="flex w-full max-w-[270px] items-center justify-center rounded-full border border-border/60 bg-background/90 px-3.5 py-0.5 font-sans text-xs text-muted-foreground">
                <Lock className="mr-1 size-3 shrink-0 text-emerald-600 dark:text-emerald-400" />
                <span className="text-[11px] font-medium text-emerald-600 dark:text-emerald-400">https://</span>
                <span className="truncate text-foreground/80">admin.continew.top</span>
              </div>
            </div>
          </div>

          {previewMode === 'admin' ? (
            <div className="flex flex-1 flex-col overflow-hidden text-xs">
              <div className="flex flex-1">
                <div className="flex w-28 shrink-0 flex-col border-r bg-muted/20 p-2.5 sm:w-36">
                  <div className="flex items-center gap-2 border-b pb-2">
                    {values.SITE_LOGO ? (
                      <img src={values.SITE_LOGO} className="size-5 shrink-0 object-contain" />
                    ) : (
                      <div className="flex size-5 shrink-0 items-center justify-center rounded bg-primary text-[10px] font-bold text-primary-foreground">
                        {values.SITE_TITLE.charAt(0)}
                      </div>
                    )}
                    <span className="truncate text-xs font-semibold text-foreground">{values.SITE_TITLE}</span>
                  </div>

                  <div className="mt-2.5 flex flex-col gap-1.5">
                    <div className="flex items-center gap-2 rounded-md bg-muted/70 p-1.5">
                      <div className="size-3 rounded-xs bg-muted-foreground/20" />
                      <div className="h-2 w-12 rounded bg-muted-foreground/25" />
                    </div>
                    <div className="flex items-center gap-2 rounded-md p-1.5">
                      <div className="size-3 rounded-xs bg-muted/70" />
                      <div className="h-2 w-14 rounded bg-muted/70" />
                    </div>
                    <div className="flex items-center gap-2 rounded-md p-1.5">
                      <div className="size-3 rounded-xs bg-muted/70" />
                      <div className="h-2 w-10 rounded bg-muted/70" />
                    </div>
                  </div>
                </div>

                <div className="flex flex-1 flex-col justify-between overflow-hidden p-3">
                  <div className="space-y-2.5">
                    <div className="flex items-center justify-between border-b pb-1.5">
                      <div className="h-2 w-16 rounded bg-muted/60" />
                      <div className="size-4.5 rounded-full bg-muted/60" />
                    </div>

                    <div className="rounded-lg border bg-gradient-to-br from-primary/5 via-card to-background p-2.5">
                      <h3 className="truncate font-semibold">{values.SITE_TITLE}</h3>
                      <p className="line-clamp-2 leading-relaxed text-muted-foreground">{values.SITE_DESCRIPTION}</p>
                    </div>

                    <div className="grid grid-cols-2 gap-2">
                      <div className="space-y-1.5 rounded-md border bg-card p-2">
                        <div className="h-2 w-10 rounded bg-muted/60" />
                        <div className="h-3 w-16 rounded bg-muted/80" />
                      </div>
                      <div className="space-y-1.5 rounded-md border bg-card p-2">
                        <div className="h-2 w-10 rounded bg-muted/60" />
                        <div className="h-3 w-16 rounded bg-muted/80" />
                      </div>
                    </div>

                    <div className="space-y-1.5 rounded-md border bg-card p-2">
                      <div className="h-2 w-20 rounded bg-muted/60" />
                      <div className="h-1.5 w-full rounded bg-muted/40" />
                      <div className="h-1.5 w-3/4 rounded bg-muted/40" />
                    </div>
                  </div>

                  <div className="mt-2.5 space-y-0.5 border-t pt-2 text-center text-[10px] text-muted-foreground">
                    <div className="truncate">{values.SITE_COPYRIGHT}</div>
                    {values.SITE_BEIAN && (
                      <div className="cursor-pointer truncate text-primary/80 hover:underline">{values.SITE_BEIAN}</div>
                    )}
                  </div>
                </div>
              </div>
            </div>
          ) : (
            <div className="flex flex-1 flex-col justify-between p-4">
              <div className="flex flex-1 flex-col items-center justify-center">
                <div className="w-full max-w-[220px] space-y-3 text-center">
                  <div className="flex flex-col items-center gap-1.5">
                    {values.SITE_LOGO ? (
                      <img src={values.SITE_LOGO} alt="Logo" className="size-9 object-contain" />
                    ) : (
                      <div className="flex size-9 items-center justify-center rounded-xl bg-primary text-sm font-bold text-primary-foreground">
                        {values.SITE_TITLE.charAt(0)}
                      </div>
                    )}
                    <h3 className="font-semibold tracking-tight text-foreground">{values.SITE_TITLE}</h3>
                    <p className="line-clamp-2 text-[10px] text-muted-foreground">{values.SITE_DESCRIPTION}</p>
                  </div>

                  <div className="space-y-1.5 pt-1">
                    <div className="h-6.5 w-full rounded-md border bg-muted/20" />
                    <div className="h-6.5 w-full rounded-md border bg-muted/20" />
                    <div className="h-6.5 w-full rounded-md bg-muted/70" />
                  </div>
                </div>
              </div>

              <div className="space-y-0.5 pt-2 text-center text-[10px] text-muted-foreground">
                <div className="truncate">{values.SITE_COPYRIGHT}</div>
                {values.SITE_BEIAN && (
                  <div className="cursor-pointer truncate text-primary/80 hover:underline">{values.SITE_BEIAN}</div>
                )}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
