import { cn } from 'cn'
import { ExternalLink } from 'lucide-react'

import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { buttonVariants } from '@/components/ui/button'
import { getInitials } from '@/lib/utils'
import { PROJECT_LINKS } from '@/navigation/route-titles'
import { useAuthStore } from '@/stores/auth/auth-provider'

const WELCOME_LINKS = [
  { label: '常见问题', href: PROJECT_LINKS.faq },
  { label: '为什么选我们', href: PROJECT_LINKS.whyChooseUs },
  { label: '支持我们', href: PROJECT_LINKS.sponsor }
] as const

const linkClass = cn(
  buttonVariants({ variant: 'link', size: 'sm' }),
  'border-0 px-0 text-muted-foreground hover:text-foreground'
)

/** Workbench greeting, project links and current session details. */
export function WorkbenchWelcome() {
  const user = useAuthStore(state => state.user)
  const displayName = user?.nickname || user?.username || '朋友'

  return (
    <header className="grid min-w-0 grid-cols-[auto_minmax(0,1fr)] items-center gap-4 rounded-xl border bg-card p-4 text-card-foreground sm:grid-cols-[auto_minmax(0,1fr)_auto] sm:px-6 sm:py-5">
      <Avatar aria-hidden className="size-12 sm:size-14">
        <AvatarImage src={user?.avatar ?? undefined} alt="" />
        <AvatarFallback className="bg-primary/10 text-lg text-primary">{getInitials(displayName)}</AvatarFallback>
      </Avatar>
      <div className="min-w-0 space-y-1">
        <h1
          className="truncate font-heading text-xl leading-6 font-semibold tracking-tight sm:text-2xl sm:leading-7"
          title={`你好，${displayName}`}
        >
          你好，{displayName}
        </h1>
        <nav aria-label="项目链接" className="flex flex-wrap items-center gap-x-3 gap-y-1">
          {WELCOME_LINKS.map(link => (
            <a key={link.href} href={link.href} target="_blank" rel="noopener noreferrer" className={linkClass}>
              {link.label}
              <ExternalLink aria-hidden className="opacity-60" />
            </a>
          ))}
        </nav>
      </div>
      <dl
        aria-label="本次登录信息"
        className="col-span-full grid min-w-0 grid-cols-[auto_minmax(0,1fr)] gap-x-3 gap-y-1 text-xs leading-5 tabular-nums sm:col-span-1 sm:max-w-72"
      >
        <dt className="text-muted-foreground">登录 IP</dt>
        <dd className="wrap-anywhere">{user?.loginIp?.trim() || '—'}</dd>
        <dt className="text-muted-foreground">登录时间</dt>
        <dd>{user?.loginTime?.trim() || '—'}</dd>
      </dl>
    </header>
  )
}
