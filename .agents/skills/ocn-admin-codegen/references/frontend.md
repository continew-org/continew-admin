# P3 前端产出（route + `-components/` + api + query-keys）

生成范围含前端时才进入本阶段（第一轮问答「仅后端」则跳过）。形态照抄 `system/notice` 既有页面。

## 路由与组件

- `src/routes/(main)/(app)/<module>/<name>/route.tsx`：
  `createFileRoute('/(main)/(app)/system/goods')({ component: GoodsList })`
  **目录结构即 URL**，不要手改 `src/routeTree.gen.ts`（由 pnpm dev/build 生成）
- 同级 `-components/`：`<name>-columns.tsx` / `<name>-list.tsx` / `<name>-form-drawer.tsx` /
  `<name>-delete-dialog.tsx`，命名照抄 `system/notice` 的既有形态
- 列表列与列定义来自字段映射轮「进 Resp / 进表单」的选定结果；表格列定义、筛选行、删除确认
  都复用现有业务组件，不要从头写

## api 文件

- `src/lib/api/<name>.ts`：从 `@/lib/http` 导入 http，导出 `<X>Resp` / `<X>DetailResp` /
  `<X>PageQuery` / `<X>Req` 四个类型 + page/get/create/update/delete 五个函数（按选定 Api 集裁减）；
  **不要直接 fetch**
- 类型字段与后端 Resp/Req 对齐；`datetime` 字段前端表单用 `datetime-local`，
  提交时补秒并把 `T` 换为空格，与后端 `LocalDateTime` 的 `yyyy-MM-dd HH:mm:ss` 序列化格式对齐

## query-keys 登记

`src/lib/query-keys.ts` 追加（import 与既有类型导入合并，导出块放 `noticeKeys` 附近）：

```typescript
export const demoKeys = {
  all: ['system', 'demo'] as const,
  lists: () => [...demoKeys.all, 'list'] as const,
  list: (query?: DemoPageQuery) => [...demoKeys.lists(), query] as const,
  details: () => [...demoKeys.all, 'detail'] as const,
  detail: (id: string | number | null) => [...demoKeys.details(), id] as const
}
```

`api/<name>.ts` 末尾 re-export 该 keys。漏登记会导致 useQuery 缓存键不成体系。

## 组件约束

- 只用 `src/components/ui` 与 `src/components` 现成的，不引第二套 UI 库；图标用 lucide-react
- 表单弹层按字段映射轮选项：抽屉（`-form-drawer`，默认，与 notice 一致）或对话框
  （`-form-dialog`，照仓库既有 dialog 形态写）
- 状态/枚举列的渲染与字典联动照既有页面的写法（如 notice 的状态列），不自己造轮子
