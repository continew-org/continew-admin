---
name: ocn-admin-codegen
description: >
  continew-admin 业务模块脚手架技能：按建表 DDL 在前后端双端生成一套配套的增删改查模块——
  后端 BaseDO / BaseController / CrudRequestMapping 体系 8 个文件 + 前端 TanStack Router 路由与组件 +
  Liquibase mysql/postgresql 双份建表 SQL + sys_menu 菜单 SQL。
  用户说"加个 CRUD"、"新增业务模块"、"按这张表生成代码"、"生成一套增删改查"、"建个管理页面"，
  或给出建表 SQL 并要求配套后端 + 前端 + 菜单时使用。
  专用于 continew-admin 仓库：技能绑的是本仓分层约定（BaseDO / BaseController / CrudRequestMapping），
  换仓库不成立。
---

# continew-admin 业务模块脚手架

给一张建表 DDL 和业务名，按本仓分层约定产出一套可跑通的增删改查模块：后端 8 个文件、
前端路由与组件、双份 Liquibase 建表 SQL、菜单 SQL。生成完必须跑通第 7 节的自验证才能交付。

## 1. 前置：先读再写（硬性）

开工前必须实际读取以下文件，把真实 import 与注解抄进产物，不要凭记忆生成：

- `continew-common/src/main/java/top/continew/admin/common/base/model/entity/BaseDO.java`
  （已含 id/createUser/createTime/updateUser/updateTime/deleted）
- `continew-common/src/main/java/top/continew/admin/common/base/controller/BaseController.java`
- `continew-system/src/main/java/top/continew/admin/system/controller/NoticeController.java`（精简 CRUD 范例）
- `continew-system/src/main/java/top/continew/admin/system/controller/UserController.java`（带自定义接口与数据权限的范例）
- `continew-system/src/main/java/top/continew/admin/system/service/impl/NoticeServiceImpl.java`
- `continew-system/src/main/java/top/continew/admin/system/model/entity/NoticeDO.java`
- `continew-ui/src/routes/(main)/(app)/system/notice/route.tsx` 与其 `-components/` 目录
- `continew-ui/src/lib/api/notice.ts`

读不到就停，报告缺哪个文件，不要凭猜测继续。

## 2. 输入

需要三样：建表 DDL（或已存在的表名）、业务中文名、权限前缀（如 `system:goods` 或 `biz:goods`）。
缺 DDL 且表不存在时，先按第 6 节的规范给出建表语句让用户确认，不要自己拍字段。

## 3. 命名推导表（逐项算，不要意译）

| 输入 | 产物 | 规则 |
|:--|:--|:--|
| 表名 `sys_goods` | 实体类名 | 去掉表前缀（`sys_`/`biz_` 等）后大驼峰 + `DO` → `GoodsDO` |
| 同上 | 类名前缀 | `Goods`（DO 以外的 Query/Req/Resp 共用） |
| 权限前缀 `system:goods` | API 路径 | `/system/goods` |
| 同上 | 接口权限码 | `system:goods:list` / `:create` / `:update` / `:delete` / `:export` |
| 业务名「商品」 | Tag 与注释 | `@Tag(name = "商品管理 API")` |
| 前端路径 | route 目录 | `continew-ui/src/routes/(main)/(app)/system/goods/` |
| 前端 API 文件 | `src/lib/api/goods.ts` | 与 route 目录末段同名（小驼峰） |

权限码推导规则以 AGENTS.md「关键机制 → CRUD 套件」段为准：`BaseController.preHandle` 根据 API 类型
自动生成并校验权限码（如 `POST /system/user` → `system:user:create`）。不要在代码里手写鉴权注解。

## 4. 后端产出清单（8 个文件）

放在 `continew-system/src/main/java/top/continew/admin/system/`（或按业务域选模块）：

1. `model/entity/<X>DO.java` —— extends `BaseDO`，`@TableName("<table>")`，`@Data`；
   **JSON 列字段必须同时加 `@TableField(typeHandler = JacksonTypeHandler.class)` 与
   `@TableName(..., autoResultMap = true)`**（漏后者会导致读出来恒为 null，消息删除接口踩过这个坑）
2. `model/query/<X>Query.java` —— 只放可筛选字段，implements `Serializable`；
   分页不放进 Query：PAGE 接口的分页参数 `PageQuery` 由 CRUD 套件作为独立方法参数注入
   （参照 NoticeQuery 与 OnlineUserController.page 的真实签名，不要让 Query 继承 PageQuery）
3. `model/req/<X>Req.java` —— 校验注解齐全（@NotBlank/@NotNull/@Size），与 DDL 长度一致
4. `model/resp/<X>Resp.java` 与 `<X>DetailResp.java` —— Resp 放列表字段，DetailResp 继承并加详情字段
5. `mapper/<X>Mapper.java` —— extends `BaseMapper<<X>DO>`；有联表/自定义查询时再加 XML
6. `service/<X>Service.java` + `service/impl/<X>ServiceImpl.java` —— extends BaseService 体系：
   接口 extends `BaseService<<X>Resp, <X>DetailResp, <X>Query, <X>Req>`（必要时加 `IService<<X>DO>`），
   实现类 extends `BaseServiceImpl<<X>Mapper, <X>DO, <X>Resp, <X>DetailResp, <X>Query, <X>Req>>` 并 implements 接口；
   类上加 `@Service`、`@RequiredArgsConstructor`，日志用 `@Slf4j`
7. `controller/<X>Controller.java` ——
   `@CrudRequestMapping(value = "/system/goods", api = {Api.PAGE, Api.GET, Api.CREATE, Api.UPDATE, Api.BATCH_DELETE})`
   extends `BaseController<<X>Service, <X>Resp, <X>DetailResp, <X>Query, <X>Req>`
   **只写空类体**：CRUD 接口由基类生成，不要在子类里重复声明
8. 有联表查询时放 `continew-system/src/main/resources/mapper/<X>Mapper.xml`

注释与署名规则（与 AGENTS.md 一致，这两条最常被违反）：
- 类级必须有 `@author`（署使用者本人）与 `@since`（5.0.0）；方法级不写 `@author`
- 注释只写当前代码的非显然约束，不写「本方法用于…」这类复述
- 新增 Java 文件必须带 Apache-2.0 License Header，否则 Spotless 会拦

## 5. 前端产出清单

- `src/routes/(main)/(app)/<module>/<name>/route.tsx`：
  `createFileRoute('/(main)/(app)/system/goods')({ component: GoodsList })`
  **目录结构即 URL**，不要手改 `src/routeTree.gen.ts`（由 pnpm dev/build 生成）
- 同级 `-components/`：`<name>-columns.tsx` / `<name>-list.tsx` / `<name>-form-drawer.tsx` /
  `<name>-delete-dialog.tsx`，命名照抄 `system/notice` 的既有形态
- `src/lib/api/<name>.ts`：从 `@/lib/http` 导入 http，导出 `<X>Resp` / `<X>DetailResp` / `<X>PageQuery` / `<X>Req`
  四个类型 + page/get/create/update/delete 五个函数；**不要直接 fetch**
- 组件只用 `src/components/ui` 与 `src/components` 现成的，不引第二套 UI 库；图标用 lucide-react
- 表格列定义、筛选行、删除确认都复用现有业务组件，不要从头写

## 6. 数据库与菜单（两步都不可省）

a) Liquibase：`mysql/` 与 `postgresql/` **两份 SQL 同步提供**，并在
   `continew-server/src/main/resources/db/changelog/db.changelog-master.yaml` 登记。
   新建表写成新的 changeset 文件（如 `mysql/<domain>/<name>.sql`），**不要改已有 changeset**（checksum 会失配）。
   表结构必须含 create_user/create_time/update_user/update_time/deleted 五列 + deleted 的索引条件，
   与 `main_table.sql`（`continew-server/src/main/resources/db/changelog/mysql/` 与
   `continew-server/src/main/resources/db/changelog/postgresql/` 下的同名文件）现有写法一致
   （`deleted bigint NOT NULL DEFAULT 0`，注释「是否已删除（0：否；id：是）」）。
   业务表默认参与租户隔离；**若该表不参与，必须登记到 `continew-starter.tenant.ignore-tables`**，并在 PR 说明理由。

b) 菜单数据：生成 sys_menu 的 INSERT SQL（目录 type=1 / 页面 type=2 / 按钮 type=3），
   页面菜单的 path 填前端路由 URL（如 `/system/goods`），**component 字段不参与新前端路由解析，
   填与不填都不影响页面渲染，但为兼容旧约定仍按 `<module>/<name>/index` 填**。
   按钮菜单的 permission 必须与第 3 节推导的权限码逐字一致。

## 7. 自验证（不过不交付）

生成完依次执行，全绿才能报告完成：
1. `./mvnw verify` —— 四道门禁 + 单测；被 Spotless 拦时先 `./mvnw compile -Pformat` 再重跑
2. `cd continew-ui && pnpm fmt && pnpm build && pnpm check` —— 新路由先由 build 写入 src/routeTree.gen.ts 再跑 check（typecheck 依赖该文件）；check 含 fmt:check/lint/typecheck/test/build
3. 起后端 + 前端，登录后在「系统管理 → 菜单」挂上新菜单、授给 admin，确认列表能打开、增删改走得通
4. 把三步的输出（关键行）贴进交付说明

任一步红了：自己修。修两轮仍红就停下来，报告「哪一步、什么错、我判断的原因」，
**不要交付红的产物，也不要把失败说成「环境问题」**。

## 8. 常见失败清单（写进 Skill 的最后一节）

| 症状 | 原因 |
|:--|:--|
| 启动时报 Liquibase checksum 不匹配 | 改了已有 changeset 而不是新增 |
| 列表页白屏 404 | 菜单 path 与 routes 目录的 URL 不一致 |
| 页面上去了但接口 403 | 按钮菜单的 permission 与 preHandle 推导出的不一致 |
| JSON 字段读出恒 null | `@TableName` 漏 autoResultMap |
| PostgreSQL 环境建表失败 | 只写了 mysql/ 那一份 SQL |
| 前端 build 报 routeTree 冲突 | 手改了 routeTree.gen.ts |
| Spotless 拦 License Header | 新 Java 文件没加头，或没跑 -Pformat |
| 联表查询报字段找不到 | 只在 Mapper 接口写了方法，XML 没同步 |
| typecheck 报路由 path 不在 FileRoutesByPath | routeTree.gen.ts 还没收录新路由，先跑一次 build 再 check |

## 配套样例

本目录 `examples/` 下有一套按本 Skill 全流程产出的完整样例（后端 8 文件 + 前端 route/-components/api +
双份 Liquibase 建表 SQL + 菜单 SQL），照抄此例可跑通；样例的生成过程与验证记录见 `examples/README.md`。
