import type { ReactNode } from 'react'

interface ListFilterRowProps {
  readonly filters?: ReactNode
  readonly actions?: ReactNode
}

/**
 * The filters-and-tools row that sits above every business table. Screens without their own page
 * header (the config tabs) use it directly; `ListPageCard` renders it for full list pages.
 */
export function ListFilterRow({ filters, actions }: ListFilterRowProps) {
  if (!filters && !actions) {
    return null
  }

  return (
    <div className="flex flex-col gap-3 px-4 xl:flex-row xl:items-center xl:justify-between">
      <div className="grid min-w-0 grid-cols-2 gap-2 sm:flex sm:flex-wrap sm:items-center">{filters}</div>
      {actions ? <div className="flex flex-wrap items-center gap-2">{actions}</div> : null}
    </div>
  )
}
