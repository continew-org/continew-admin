import { PreviewCard } from '@animate-ui/react/primitives/base-ui/preview-card'
import { siGithub } from 'simple-icons'

import { SimpleIcon } from '@/components/simple-icon'
import { Button } from '@/components/ui/button'

import { PROJECT_LINKS } from '@/navigation/route-titles'

/** The help entry in the header toolbar: a GitHub icon that reveals the help links on hover. */
export function GithubHelp() {
  return (
    <PreviewCard.Root>
      <PreviewCard.Trigger render={<Button variant="ghost" size="icon-sm" aria-label="需要帮助" />}>
        <SimpleIcon icon={siGithub} className="size-4 fill-current" />
      </PreviewCard.Trigger>
      <PreviewCard.Portal>
        <PreviewCard.Positioner side="bottom" align="end" sideOffset={8} className="isolate z-50">
          <PreviewCard.Popup className="w-72 rounded-lg bg-popover p-4 text-sm text-popover-foreground shadow-md ring-1 ring-foreground/10 outline-hidden">
            <div className="flex flex-col gap-1.5">
              <p className="text-sm leading-none font-medium">需要帮助？</p>
              <p className="text-sm leading-relaxed text-muted-foreground">
                {'遇到问题或有建议，请查阅'}
                <a
                  className="text-foreground hover:underline"
                  href={PROJECT_LINKS.docs}
                  target="_blank"
                  rel="noreferrer"
                >
                  在线文档
                </a>
                {'，或到'}
                <a
                  className="mx-1 inline-flex items-center gap-1 text-foreground hover:underline"
                  href={`${PROJECT_LINKS.repository}/issues`}
                  target="_blank"
                  rel="noreferrer"
                >
                  <SimpleIcon icon={siGithub} aria-hidden className="size-3 fill-foreground" />
                  GitHub
                </a>
                {'提交。'}
              </p>
            </div>
          </PreviewCard.Popup>
        </PreviewCard.Positioner>
      </PreviewCard.Portal>
    </PreviewCard.Root>
  )
}
