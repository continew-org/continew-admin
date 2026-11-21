import { ThemeToggler } from '@animate-ui/react/primitives/effects/theme-toggler'
import { Moon, Sun } from 'lucide-react'
import { useShallow } from 'zustand/react/shallow'

import { Button } from '@/components/ui/button'
import { Tooltip, TooltipContent, TooltipTrigger } from '@/components/ui/tooltip'
import { usePreferencesStore } from '@/stores/preferences/preferences-provider'

export function ThemeSwitcher({ showTooltip = true }: { readonly showTooltip?: boolean }) {
  const { themeMode, resolvedThemeMode, setPreference } = usePreferencesStore(
    useShallow(state => ({
      themeMode: state.values.theme_mode,
      resolvedThemeMode: state.resolvedThemeMode,
      setPreference: state.setPreference
    }))
  )

  return (
    <ThemeToggler
      theme={themeMode}
      resolvedTheme={resolvedThemeMode}
      setTheme={mode => setPreference('theme_mode', mode)}
    >
      {({ resolved, toggleTheme }) => {
        const trigger = (
          <Button
            variant="ghost"
            size="icon-sm"
            aria-label="切换主题"
            onClick={() => toggleTheme(resolved === 'dark' ? 'light' : 'dark')}
          >
            {resolved === 'dark' ? <Sun /> : <Moon />}
          </Button>
        )

        if (!showTooltip) {
          return trigger
        }

        return (
          <Tooltip>
            <TooltipTrigger render={trigger} />
            <TooltipContent side="bottom" sideOffset={8}>
              切换主题
            </TooltipContent>
          </Tooltip>
        )
      }}
    </ThemeToggler>
  )
}
