# continew-ui

ContiNew Admin 前端（React 19 / Vite 8 / shadcn/ui / Tailwind 4）。全仓分流、跨端契约与后端门禁见[根 AGENTS.md](../AGENTS.md)；本文件只写前端目录内「看代码看不出来、且容易做错」的事。

## 技术栈

React 19 / Vite 8 / TypeScript / TanStack Router · Query · Table / Tailwind 4 / shadcn/ui（base-nova 风格）/ zustand / zod / oxlint / oxfmt / vitest。

- Node 版本锁定 `^22.0.0`（`package.json` 的 `engines`）；
- pnpm 版本以 `package.json` 的 `packageManager` 字段为准，不要用全局 pnpm 绕过。

## 目录职责

| 目录 | 职责 |
|:--|:--|
| `src/routes` | 页面（TanStack 文件路由，目录结构即 URL；`-` 前缀目录与 `()` 分组不进 URL） |
| `src/lib/api` | 接口定义，按业务域分文件（`user.ts`、`role.ts` 等） |
| `src/lib/http.ts` | 请求封装：超时、错误提示、401 会话刷新、租户 ID 头注入 |
| `src/navigation` | 后端菜单 → 侧栏/页签/面包屑映射，只认 `path` |
| `src/stores` | zustand 全局状态（auth、preferences、tabs 等） |
| `src/components/ui` | shadcn/ui（base-nova）拷贝组件层，CLI 生成后按需定制 |
| `src/components` | 业务组合组件（表格、抽屉、筛选行、删除确认等） |
| `tests` | Vitest 单元测试 |

## 硬约束

- **不手改 `src/routeTree.gen.ts`**。TanStack Router 插件自动生成，改动 `src/routes` 后由 `pnpm dev` / `pnpm build` 重新生成，diff 随本次改动一起提交。
- **新增页面**：在 `src/routes` 下建 `route.tsx`（`createFileRoute`），再到「系统管理 → 菜单」配置菜单，`path` 必须与路由文件的实际 URL 一致。菜单的 `component` 字段不参与路由解析，路由解析只认 `path`（`src/navigation/route-info.ts` 的 `findMenuByPath`），页面组件完全由文件路由决定。
- **接口调用统一走 `src/lib/http.ts`**，不直接 `fetch`：超时、错误提示、401 刷新、租户 ID 头都在这一层。
- **组件优先用 `src/components/ui` 已有组件**，不引入第二套 UI 库；图标统一用 `lucide-react`。

## 验证命令

在 `continew-ui/` 下执行：

```bash
pnpm check    # 依次执行下列五条，任一步失败即中断
```

| 命令 | 作用 |
|:--|:--|
| `pnpm fmt:check` | 格式检查（oxfmt），不通过时执行 `pnpm fmt` 修复 |
| `pnpm lint` | oxlint 静态检查 |
| `pnpm typecheck` | `tsc --noEmit` |
| `pnpm test` | vitest run |
| `pnpm build` | 生产构建，同时重新生成 `routeTree.gen.ts` |

排查失败原因时单独运行对应一条；提交前用 `pnpm check` 一把跑完。

oxfmt 会重排代码（含 JSX 折行），改动会波及你没动过的行。**提交前先跑 `pnpm fmt`**，避免把格式化噪声混进功能提交。

本文件不受格式检查约束：`.oxfmtrc.json` 的 `ignorePatterns` 已排除 `**/AGENTS.md`，修改它无需跑 `pnpm fmt`。

## 示例：新增一个列表页

1. 建 `src/routes/(main)/(app)/system/<name>/route.tsx`，`createFileRoute` 指向该路径并挂页面组件，私有组件放同级 `-components/`；
2. 接口定义加到 `src/lib/api/<name>.ts`，请求经 `src/lib/http.ts` 发出；
3. 跑 `pnpm dev` 生成路由，登录后在「系统管理 → 菜单」新增菜单，`path` 填路由 URL（如 `/system/user` 对应 `src/routes/(main)/(app)/system/user/`）；
4. 跑上方五条验证命令。
