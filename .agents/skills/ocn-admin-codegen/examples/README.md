# ocn-admin-codegen 配套样例：示例管理（biz_demo）

照抄此例可跑通：把下表文件放回真实路径、登记 Liquibase 变更集与菜单 SQL，再跑通 `./mvnw verify` 与 `pnpm check`，登录后即可在「系统管理 → 示例管理」完成增删改查。本样例对应技能 P0 问答的「已有表」入口——输入即下方三要素；「业务引导建表」入口产出的 DDL 也应与本样例 biz_demo 同构（五审计列 + deleted 索引条件 + 可选 tenant_id）。

## 输入三要素

- 建表 DDL：表 `biz_demo`，业务列四类各一——`name varchar(64) NOT NULL`（名称）、`quantity int NOT NULL DEFAULT 0`（数量）、`online_time datetime NULL`（上线时间）、`ext_config json NULL`（扩展配置，演示 JSON 列的 autoResultMap 要点）；另含五审计列（create_user/create_time/update_user/update_time/deleted）与 deleted 索引条件
- 业务中文名：示例管理（Tag 用「示例管理 API」）
- 权限前缀：`system:demo`（API 路径 `/system/demo`）

## 文件 → 真实路径映射

| examples 内路径 | 真实放置路径 |
|:--|:--|
| `backend/top/continew/admin/system/demo/controller/DemoController.java` | `continew-system/src/main/java/top/continew/admin/system/controller/DemoController.java` |
| `backend/top/continew/admin/system/demo/service/DemoService.java` | `continew-system/src/main/java/top/continew/admin/system/service/DemoService.java` |
| `backend/top/continew/admin/system/demo/service/impl/DemoServiceImpl.java` | `continew-system/src/main/java/top/continew/admin/system/service/impl/DemoServiceImpl.java` |
| `backend/top/continew/admin/system/demo/mapper/DemoMapper.java` | `continew-system/src/main/java/top/continew/admin/system/mapper/DemoMapper.java` |
| `backend/top/continew/admin/system/demo/model/entity/DemoDO.java` | `continew-system/src/main/java/top/continew/admin/system/model/entity/DemoDO.java` |
| `backend/top/continew/admin/system/demo/model/query/DemoQuery.java` | `continew-system/src/main/java/top/continew/admin/system/model/query/DemoQuery.java` |
| `backend/top/continew/admin/system/demo/model/req/DemoReq.java` | `continew-system/src/main/java/top/continew/admin/system/model/req/DemoReq.java` |
| `backend/top/continew/admin/system/demo/model/resp/DemoResp.java` | `continew-system/src/main/java/top/continew/admin/system/model/resp/DemoResp.java` |
| `backend/top/continew/admin/system/demo/model/resp/DemoDetailResp.java` | `continew-system/src/main/java/top/continew/admin/system/model/resp/DemoDetailResp.java` |
| `frontend/routes/(main)/(app)/system/demo/route.tsx` | `continew-ui/src/routes/(main)/(app)/system/demo/route.tsx` |
| `frontend/routes/(main)/(app)/system/demo/-components/demo-columns.tsx` | `continew-ui/src/routes/(main)/(app)/system/demo/-components/demo-columns.tsx` |
| `frontend/routes/(main)/(app)/system/demo/-components/demo-list.tsx` | `continew-ui/src/routes/(main)/(app)/system/demo/-components/demo-list.tsx` |
| `frontend/routes/(main)/(app)/system/demo/-components/demo-form-drawer.tsx` | `continew-ui/src/routes/(main)/(app)/system/demo/-components/demo-form-drawer.tsx` |
| `frontend/routes/(main)/(app)/system/demo/-components/demo-delete-dialog.tsx` | `continew-ui/src/routes/(main)/(app)/system/demo/-components/demo-delete-dialog.tsx` |
| `frontend/api/demo.ts` | `continew-ui/src/lib/api/demo.ts` |
| `db/mysql/demo/biz_demo.sql` | `continew-server/src/main/resources/db/changelog/mysql/demo/biz_demo.sql` |
| `db/postgresql/demo/biz_demo.sql` | `continew-server/src/main/resources/db/changelog/postgresql/demo/biz_demo.sql` |
| `db/menu/biz_demo_menu.sql` | `continew-server/src/main/resources/db/changelog/mysql/demo/biz_demo_menu.sql`（MySQL 语法；PostgreSQL 环境按 `postgresql/main_data.sql` 的既有风格把 `b'0'` 改为 `false` 后另存一份，两份必须同步提供） |

## query-keys 追加

`api/demo.ts` 末尾 re-export `demoKeys`，需在 `continew-ui/src/lib/query-keys.ts` 追加（import 与既有类型导入合并，导出块放在 `noticeKeys` 附近）：

```typescript
export const demoKeys = {
  all: ['system', 'demo'] as const,
  lists: () => [...demoKeys.all, 'list'] as const,
  list: (query?: DemoPageQuery) => [...demoKeys.lists(), query] as const,
  details: () => [...demoKeys.all, 'detail'] as const,
  detail: (id: string | number | null) => [...demoKeys.details(), id] as const
}
```

## master.yaml 登记

在 `continew-server/src/main/resources/db/changelog/db.changelog-master.yaml` 追加 include 两行：MySQL 行真实生效，PostgreSQL 段当前整段处于注释状态，保持注释风格示例。

```yaml
  - include:
      file: db/changelog/mysql/demo/biz_demo.sql
#  - include:
#      file: db/changelog/postgresql/demo/biz_demo.sql
```

MySQL 行插在 `db/changelog/mysql/plugin/` 目录的最后一个 include 之后；PostgreSQL 行插在注释段对应位置。菜单 SQL 若作为独立变更集文件，按同法各追加一行；**只新增变更集，不改任何已有变更集**（checksum 会失配）。

## 菜单 ID 段位说明

样例自拟段位 4000-4005，页面菜单 4000 挂「系统管理」目录（存量 ID 1000）下。存量已占用：1000-2055（系统与监控）、3000-3025（能力开放）、7000-7018（任务调度）、8000-8023（多租户）；9000 段为已退役模块的历史段位，勿复用。照抄时若与库内数据冲突，整段换号即可。

## 复现步骤

1. 按映射表把文件放回真实路径（后端 Java 9 个 + 前端 6 个 + SQL 3 份），并在 `continew-ui/src/lib/query-keys.ts` 追加上方的 `demoKeys`；
2. 按「master.yaml 登记」一节追加 include；
3. 后端：`./mvnw verify`——四道静态门禁 + 单测；被 Spotless 拦时先 `./mvnw compile -Pformat` 自动修复再重跑；
4. 前端：`cd continew-ui && pnpm fmt && pnpm build && pnpm check`——新路由先由 build 写入 `src/routeTree.gen.ts` 再跑 check（typecheck 依赖该文件）；check 含 fmt:check/lint/typecheck/test/build；不要手改 `routeTree.gen.ts`；
5. 起后端 + 前端，确认菜单已入树（菜单 SQL 已由 Liquibase 执行，或手工执行后授权 admin），列表可打开、新增/修改/删除走得通，`ext_config` 保存后重新打开详情值不丢。

## 样例要点（与常见失败清单对照）

- `ext_config`：`DemoDO` 上 `@TableName(value = "biz_demo", autoResultMap = true)` 与字段 `@TableField(typeHandler = JacksonTypeHandler.class)` 成对出现，缺后者读取恒为 null；
- `DemoQuery` implements `Serializable`：分页参数 `PageQuery` 由 CRUD 套件作为独立方法参数注入，不放进 Query，也不要让 Query 继承 PageQuery；
- `DemoController` 只写空类体：权限码由 `preHandle` 按 Api 集推导——PAGE→`system:demo:list`、GET→`:get`、CREATE→`:create`、UPDATE→`:update`、BATCH_DELETE→`:delete`，菜单按钮 permission 与其逐字一致，Api 集里没有 export 就不造导出按钮；
- `DemoDetailResp` extends `BaseDetailResp` 并重复列表字段（与 `UserDetailResp`/`NoticeDetailResp` 同形态），`extConfig` 只放详情响应；
- 页面菜单 `component` 字段不参与新前端路由解析（路由只认 `path`），为兼容旧约定仍按 `system/demo/index` 填写；
- 日期时间：前端表单用 `datetime-local`，提交时补秒并把 `T` 换为空格，与后端 `LocalDateTime` 的 `yyyy-MM-dd HH:mm:ss` 序列化格式对齐。
