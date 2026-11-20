import { siGithub } from 'simple-icons'

import { SimpleIcon } from '@/components/simple-icon'
import { Button } from '@/components/ui/button'
import { HoverCard, HoverCardContent, HoverCardTrigger } from '@/components/ui/hover-card'

/** Where to send people for help with this project. */
const DOCS_URL = 'https://continew.top/docs/admin'
const REPOSITORY_URL = 'https://github.com/ppxb/continew-ui-next'

/** The help entry in the header toolbar: a GitHub icon that reveals the help links on hover. */
export function GithubHelp() {
  return (
    <HoverCard>
      <HoverCardTrigger render={<Button variant="outline" size="icon-sm" aria-label="需要帮助" />}>
        <SimpleIcon icon={siGithub} className="size-4 fill-current" aria-hidden />
      </HoverCardTrigger>
      <HoverCardContent align="end" sideOffset={8} className="w-72 p-4">
        <div className="flex flex-col gap-1.5">
          <p className="leading-none font-medium">需要帮助？</p>
          <p className="leading-relaxed text-muted-foreground">
            {'遇到问题或有建议，请查阅'}
            <a className="text-foreground hover:underline" href={DOCS_URL} target="_blank" rel="noreferrer">
              在线文档
            </a>
            {'，或到'}
            <a
              className="mx-1 inline-flex items-center gap-1 text-foreground hover:underline"
              href={`${REPOSITORY_URL}/issues`}
              target="_blank"
              rel="noreferrer"
            >
              <SimpleIcon icon={siGithub} aria-hidden className="size-3 fill-foreground" />
              GitHub
            </a>
            {'提交。'}
          </p>
        </div>
      </HoverCardContent>
    </HoverCard>
  )
}
