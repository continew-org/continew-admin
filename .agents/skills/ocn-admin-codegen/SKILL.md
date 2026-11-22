---
name: ocn-admin-codegen
description: >
  continew-admin 业务模块脚手架技能：从业务描述引导设计数据表（含建表确认门），或直接按既有表，
  在前后端双端生成一套配套的增删改查模块——后端 BaseDO / BaseController / CrudRequestMapping 体系 9 个文件 +
  前端 TanStack Router 路由与组件 + Liquibase mysql/postgresql 双份建表 SQL + sys_menu 菜单 SQL，
  含 query-keys / master.yaml / 租户白名单等集成点登记。全程选择题问答确认入口、位置、范围、API 集与字段映射，
  生成后自带双端构建自验证。用户说"加个 CRUD"、"新增业务模块"、"按这张表生成一套"、"建个管理页面"、
  "给 XX 做增删改查"，或只给一段业务描述就想落库建表并配套页面时使用。
  专用于 continew-admin 仓库：技能绑的是本仓分层约定（BaseDO / BaseController / CrudRequestMapping），
  换仓库不成立。
---

# continew-admin 业务模块脚手架

双入口（业务引导建表 / 既有表）、问答式决策、P0-P6 全链路产出、自验证不过不交付。

## 1. 设计原则

- **事实源是当前代码，不是模板**：动手前实读第 2 节清单，import 与注解照抄现状。模板化工具最大的坑是
  模板与框架演进脱节（4.x→5.x 分层写法与前端整套都换过血），本技能靠「读现状」根治，不靠背规范。
- **决策权在用户，节奏靠默认值**：范围、位置、API 集、字段映射一律问答确认；每轮给推荐默认，用户点选即可。
- **全链路一次到位**：代码文件只是产物的一半，`query-keys.ts`、`master.yaml`、菜单 SQL、租户白名单这些
  集成点同批补齐——漏一步集成点就是白屏、403 或启动失败。
- **过不了自验证不交付**：能自己跑构建就跑，红了自己修；修两轮仍红就停下如实报告，不把失败说成环境问题。

取舍依据（旧版脚手架生成器的弊端逐项对照）见 `references/design-notes.md`，演进本技能前先读它。

## 2. 前置：先读再写（硬性）

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

## 3. 交互协议（问答式，先问后动笔）

用 AskUserQuestion（或所在智能体等效的多选问答机制），中文提问；每轮最多 4 问；先扫一遍用户请求里
已明确的项，只问缺口，已答过的不重复问。用户一句「都按推荐来」即后续轮次全走推荐，只保留确认门。
全程不超过 3 轮问答 + 各确认门。

**第一轮（范围四问，用户没说清就必问）：**

| 问题 | 选项（★ = 推荐默认） |
|:--|:--|
| 入口模式 | ★业务引导建表（只有业务描述）；已有表（用户给表名 / DDL / 数据库连接走 dbx 读取） |
| 放置位置 | ★continew-system 对应业务子包；新建插件模块（continew-plugin 下，需两处 POM 注册）；自定义域 |
| 生成范围 | ★双端全套（后端 + 前端 + 建表 SQL + 菜单）；仅后端；仅前端；双端不含菜单 |
| CRUD API 集 | ★PAGE/GET/CREATE/UPDATE/BATCH_DELETE 标准五件套；追加 EXPORT；追加 TREE 等（以源码 Api 枚举实际值为准） |

**模式 A 第二轮（建表确认门）**：按 `references/table-design.md` 把业务描述整理成表设计草案
（实体拆分、列与类型、标准列、索引、租户、字典联动），产出 MySQL/PostgreSQL 双份 DDL，
问答确认：「按此建表」/「调整（说明改哪）」/「重来」。**未确认不写任何文件。**

**模式 B 第二轮（读表）**：有数据库连接工具（如 dbx MCP）时直接 describe；否则让用户贴
`SHOW CREATE TABLE` 或 DDL。对照 `references/table-design.md` 的类型映射产出字段映射推荐表。

**字段映射轮（两种模式都要，生成前最后一问）**：先展示推荐映射表（列 → Java 类型 / Query / Resp /
表单组件 / 校验注解），再问：

| 问题 | 选项 |
|:--|:--|
| 筛选字段（多选） | 从推荐映射里选进 Query 的列（推荐项已按列类型与业务语义预选） |
| 表单弹层 | ★抽屉（与 system/notice 一致）；对话框 |
| 菜单挂载点 | ★挂「系统管理」既有目录；新建目录；不入菜单（跳过菜单 SQL） |
| 范围与 API 集复核 | 第一轮答案有变在此改；无变化选「按前述」 |

## 4. 命名推导表（逐项算，不要意译）

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

## 5. 工作流（按阶段读参考文档，顺序固定）

| 阶段 | 动作 | 必读 |
|:--|:--|:--|
| P0 问需求 | 第 3 节交互协议，问完再动笔 | — |
| P1 定表 | 模式 A 设计并过确认门 / 模式 B 解析既有表 | `references/table-design.md` |
| P2 后端 | 9 个 Java 文件按清单落位 | `references/backend.md` |
| P3 前端 | route + `-components/` + api + query-keys 登记 | `references/frontend.md` |
| P4 数据与菜单 | 双份 Liquibase 变更集 + master.yaml + 菜单 SQL + 租户白名单 | `references/sql-and-menu.md` |
| P5 自验证 | 双端构建 + 实跑 + 交付 grep | `references/verification.md` |
| P6 交付 | 交付说明（格式见 verification.md），明示 CRUD 之外需人工设计的部分 | — |

P1-P4 中发现前面的决策要改（如 DDL 改列），回头同步所有受影响产物，不要只改一处。

## 6. 全局硬约束

- 新 Java 文件必带 Apache-2.0 License Header（Spotless 会拦）；`@author` 署使用者本人，`@since` 5.0.0；方法级不署名
- 注释只写当前代码的非显然约束；变更历史进 commit message，修复原因进 PR 正文，不混进代码注释
- **不改任何已有 Liquibase changeset**（checksum 失配即启动失败），只新增
- 第三方依赖版本不硬编码（starter 统一管理）；内部模块依赖 groupId 用 `${project.groupId}`
- 本技能文件与产物的行文避开已退役功能的字面量（PR #318 验收 grep 口径），新增参考文档时同样遵守

## 7. 常见失败清单（生成时就对照避坑）

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

`examples/` 下有一套按本技能 P0-P6 产出的完整样例（入口 = 已有表 biz_demo）：
后端 9 文件 + 前端 route/-components/api + 双份 Liquibase 建表 SQL + 菜单 SQL，照抄可跑通；
生成过程与验证记录见 `examples/README.md`。模式 A 产出的 DDL 应与样例 biz_demo 同构
（五审计列 + deleted 索引条件 + 可选 tenant_id）。
