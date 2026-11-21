import * as React from 'react'

import { EditorContent, useEditor, useEditorState } from '@tiptap/react'
import StarterKit from '@tiptap/starter-kit'

import {
  Bold,
  Code,
  Heading2,
  Italic,
  List,
  ListOrdered,
  Minus,
  Quote,
  Redo2,
  Strikethrough,
  Undo2
} from 'lucide-react'

import { cn } from 'cn'

import { Button } from '@/components/ui/button'
import { Tooltip, TooltipContent, TooltipTrigger } from '@/components/ui/tooltip'

/** The typography shared by the editor surface and the read-only notice view. */
export const RICH_TEXT_CONTENT_CLASS =
  'text-sm leading-relaxed text-foreground [&_blockquote]:my-2 [&_blockquote]:border-l-2 [&_blockquote]:pl-3 [&_blockquote]:text-muted-foreground [&_code]:rounded [&_code]:bg-muted [&_code]:px-1 [&_code]:py-0.5 [&_code]:text-xs [&_h1]:my-2 [&_h1]:text-xl [&_h1]:font-semibold [&_h2]:my-2 [&_h2]:text-lg [&_h2]:font-semibold [&_h3]:my-1.5 [&_h3]:text-base [&_h3]:font-medium [&_hr]:my-3 [&_li]:my-0.5 [&_ol]:my-1.5 [&_ol]:list-decimal [&_ol]:pl-5 [&_p]:my-1.5 [&_pre]:my-2 [&_pre]:overflow-x-auto [&_pre]:rounded-md [&_pre]:bg-muted [&_pre]:p-3 [&_ul]:my-1.5 [&_ul]:list-disc [&_ul]:pl-5'

export function RichTextContent({ html, className }: { html?: string | null; className?: string }) {
  if (!html) {
    return <p className="text-sm text-muted-foreground">暂无内容</p>
  }

  return <div className={cn(RICH_TEXT_CONTENT_CLASS, className)} dangerouslySetInnerHTML={{ __html: html }} />
}

function ToolbarButton({
  label,
  active = false,
  disabled = false,
  onClick,
  children
}: {
  label: string
  active?: boolean
  disabled?: boolean
  onClick: () => void
  children: React.ReactNode
}) {
  return (
    <Tooltip>
      <TooltipTrigger
        render={
          <Button
            type="button"
            variant={active ? 'secondary' : 'ghost'}
            size="icon-sm"
            disabled={disabled}
            onClick={onClick}
          />
        }
      >
        {children}
      </TooltipTrigger>
      <TooltipContent side="top">{label}</TooltipContent>
    </Tooltip>
  )
}

interface RichTextEditorProps {
  readonly value: string
  readonly onChange: (value: string) => void
  readonly disabled?: boolean
  readonly invalid?: boolean
}

/**
 * The toolbar state before the editor exists, and the value reused once it is gone. Tiptap only
 * re-renders this component on transactions we subscribe to, so the booleans must never be read
 * straight off the editor during render.
 */
const EMPTY_TOOLBAR_STATE = {
  canUndo: false,
  canRedo: false,
  bold: false,
  italic: false,
  strike: false,
  code: false,
  heading2: false,
  bulletList: false,
  orderedList: false,
  blockquote: false
}

/** The project's tiptap editor: StarterKit commands only, no extra editor dependency. */
export function RichTextEditor({ value, onChange, disabled = false, invalid = false }: RichTextEditorProps) {
  const editor = useEditor({
    extensions: [StarterKit],
    content: value,
    immediatelyRender: false,
    editable: !disabled,
    editorProps: {
      attributes: {
        class: cn(RICH_TEXT_CONTENT_CLASS, 'min-h-64 px-3 py-2 outline-none')
      }
    },
    onUpdate: ({ editor: currentEditor }) => onChange(currentEditor.getHTML())
  })

  // Selection changes do not emit `onUpdate`, so the toolbar subscribes to the derived state.
  const toolbarState =
    useEditorState({
      editor,
      selector: ({ editor: currentEditor }) => {
        if (!currentEditor) {
          return EMPTY_TOOLBAR_STATE
        }

        return {
          canUndo: currentEditor.can().undo(),
          canRedo: currentEditor.can().redo(),
          bold: currentEditor.isActive('bold'),
          italic: currentEditor.isActive('italic'),
          strike: currentEditor.isActive('strike'),
          code: currentEditor.isActive('code'),
          heading2: currentEditor.isActive('heading', { level: 2 }),
          bulletList: currentEditor.isActive('bulletList'),
          orderedList: currentEditor.isActive('orderedList'),
          blockquote: currentEditor.isActive('blockquote')
        }
      }
    }) ?? EMPTY_TOOLBAR_STATE

  React.useEffect(() => {
    if (editor && value !== editor.getHTML()) {
      editor.commands.setContent(value, { emitUpdate: false })
    }
  }, [editor, value])

  React.useEffect(() => {
    editor?.setEditable(!disabled)
  }, [editor, disabled])

  if (!editor) {
    return <div className="min-h-64 rounded-lg border" />
  }

  return (
    <div className={cn('overflow-hidden rounded-lg border bg-transparent', invalid && 'border-destructive')}>
      <div className="flex flex-wrap items-center gap-0.5 border-b p-1">
        <ToolbarButton
          label="撤销"
          disabled={disabled || !toolbarState.canUndo}
          onClick={() => editor.chain().focus().undo().run()}
        >
          <Undo2 />
        </ToolbarButton>
        <ToolbarButton
          label="重做"
          disabled={disabled || !toolbarState.canRedo}
          onClick={() => editor.chain().focus().redo().run()}
        >
          <Redo2 />
        </ToolbarButton>
        <span className="mx-1 h-4 w-px bg-border" />
        <ToolbarButton
          label="加粗"
          active={toolbarState.bold}
          disabled={disabled}
          onClick={() => editor.chain().focus().toggleBold().run()}
        >
          <Bold />
        </ToolbarButton>
        <ToolbarButton
          label="斜体"
          active={toolbarState.italic}
          disabled={disabled}
          onClick={() => editor.chain().focus().toggleItalic().run()}
        >
          <Italic />
        </ToolbarButton>
        <ToolbarButton
          label="删除线"
          active={toolbarState.strike}
          disabled={disabled}
          onClick={() => editor.chain().focus().toggleStrike().run()}
        >
          <Strikethrough />
        </ToolbarButton>
        <ToolbarButton
          label="行内代码"
          active={toolbarState.code}
          disabled={disabled}
          onClick={() => editor.chain().focus().toggleCode().run()}
        >
          <Code />
        </ToolbarButton>
        <span className="mx-1 h-4 w-px bg-border" />
        <ToolbarButton
          label="二级标题"
          active={toolbarState.heading2}
          disabled={disabled}
          onClick={() => editor.chain().focus().toggleHeading({ level: 2 }).run()}
        >
          <Heading2 />
        </ToolbarButton>
        <ToolbarButton
          label="无序列表"
          active={toolbarState.bulletList}
          disabled={disabled}
          onClick={() => editor.chain().focus().toggleBulletList().run()}
        >
          <List />
        </ToolbarButton>
        <ToolbarButton
          label="有序列表"
          active={toolbarState.orderedList}
          disabled={disabled}
          onClick={() => editor.chain().focus().toggleOrderedList().run()}
        >
          <ListOrdered />
        </ToolbarButton>
        <ToolbarButton
          label="引用"
          active={toolbarState.blockquote}
          disabled={disabled}
          onClick={() => editor.chain().focus().toggleBlockquote().run()}
        >
          <Quote />
        </ToolbarButton>
        <ToolbarButton
          label="分隔线"
          disabled={disabled}
          onClick={() => editor.chain().focus().setHorizontalRule().run()}
        >
          <Minus />
        </ToolbarButton>
      </div>

      <EditorContent editor={editor} />
    </div>
  )
}
