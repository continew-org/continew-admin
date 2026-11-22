# P1 定表：模式 A 业务引导建表 / 模式 B 既有表解析

## 模式 A：从业务描述到表设计

适用：用户只有业务描述（如「我要管理商品，有名称、分类、上下架状态、上架时间」），没有表。
产物：一份经用户确认的建表 DDL（MySQL + PostgreSQL 双方言），确认后才写文件。

### 1. 从业务描述到表结构（先梳理后成文）

按顺序梳理，拿不准的点并入 SKILL.md §3 的下一轮问答：

1. **实体识别**：业务描述里的名词，哪些是「要管理的主数据」（建表），哪些只是属性（列），
   哪些是独立实体（关联表）。多对多与主子表（如订单-明细）要拆表并说清关联方式——旧工具只能按单表渲染，
   本技能没有这个限制，但也不要过度设计：先满足当前描述，关联表等描述里真出现再建。
2. **属性盘点**：每个实体的属性逐个过——必填性、长度、默认值、是否枚举（状态类）、
   是否字典联动（下拉项会运营配置的用 `sys_dict` 联动，写死在代码里的用枚举）。
3. **关系与冗余决策**：显示用关联字段（如 `type_id`）+ 列表联查，还是直接冗余文本列
   （低频变更的名称类冗余可省联查）；给出取舍理由让用户确认。
4. **查询形态**：树形（`parent_id`）/ 平铺；是否需要排序字段 `sort`（默认 999）。

### 2. 标准列（每个业务表必备，精确写法照抄 main_table.sql）

- 主键 `id`
- 五审计列：`create_user` / `create_time` / `update_user` / `update_time` / `deleted`
  （`deleted bigint NOT NULL DEFAULT 0`，注释「是否已删除（0：否；id：是）」+ deleted 索引条件），
  精确 DDL 照抄 `continew-server/src/main/resources/db/changelog/mysql/main_table.sql` 与
  `postgresql/main_table.sql` 的既有写法，不要凭记忆写
- `tenant_id`：业务表默认必须有——行级隔离按**表名**白名单拼 `tenant_id` 条件、不检查列是否存在，
  缺列又未登记白名单的表运行期 SQL 直接报错；确实不参与隔离的才登记
  `continew-starter.tenant.ignore-tables` 并在 PR 说明理由（见 sql-and-menu.md）；
  写法照 plugin_tenant.sql 的 ALTER 段（列注释「租户ID」，DEFAULT 0），main_table.sql 无此列

### 3. 命名与类型映射

- 表名前缀：系统域 `sys_` / 业务域 `biz_`（与样例 biz_demo 同口径）；列名小写下划线，Java 属性小驼峰
- DB 类型 → Java 类型：`varchar`→String、`int`→Integer、`bigint`→Long、`datetime`→LocalDateTime、
  `date`→LocalDate、`time`→LocalTime、`decimal`→BigDecimal、`text`→String、
  `tinyint` 状态列→Integer 或枚举（是否枚举化以仓库既有 Req/DO 真实用法为准，不凭本技能文本）、
  `json`→String（DO 上加 `@TableField(typeHandler = JacksonTypeHandler.class)` 并在
  `@TableName` 开 `autoResultMap = true`，成对出现，缺后者读取恒为 null）
- 列注释写业务含义与枚举取值（如「状态（1：启用；2：禁用）」），Req 的校验注解长度与列长度一致

### 4. 索引与约束

- 唯一约束列（编码/编号类）加唯一索引；`create_user` / `update_user` 照 main_table.sql 建 `idx_`，
  `tenant_id` 照 plugin_tenant.sql 的 ALTER 段建 `idx_tenant_id`
- 高频筛选列按业务语义加索引，宁缺勿滥，说清每条索引服务的查询

### 5. 模式 A 确认门

产出双份 DDL（MySQL 反引号 / PostgreSQL 双引号且类型对齐，方言差异参照
`examples/db/mysql/demo/biz_demo.sql` 与 `examples/db/postgresql/demo/biz_demo.sql`），
外加一张「列 → 类型 / 必填 / 默认 / 注释」的简表。AskUserQuestion 确认：
「按此建表」/「调整（用户说明改哪，改后重出草案再过门）」/「重来」。确认的是表结构草案，
Liquibase 变更集文件按 sql-and-menu.md 的格式在 P4 另产；建表 SQL 进 P4，
列定义进 P2/P3；**此后改列必须回头同步 Resp/Req/前端列定义，不要只改 SQL**。

## 模式 B：既有表解析

读取途径（按优先级）：用户已贴 DDL → dbx MCP describe（有连接配置时）→ 让用户跑
`SHOW CREATE TABLE <name>` 贴结果。MySQL 与 PostgreSQL 语法都能解析。

解析后输出三样，供 SKILL.md §3 字段映射轮展示：

1. **列清单与类型映射**：按模式 A 第 3 节映射逐列给出 Java 类型；
   `json` 列标记 typeHandler + autoResultMap 要点
2. **健全性检查**（缺了就明说，让用户决定补还是带病生成）：
   - 五审计列是否齐全（缺 `deleted` 会破坏逻辑删除语义；缺审计列要说明后果）
   - `tenant_id` 有无 → 决定是否需要登记租户白名单
   - 列注释是否写了枚举取值（影响字典/枚举映射与前端展示）
3. **推荐映射表**：列 → Java 类型 / 进 Query（筛选）/ 进 Resp / 进表单 / 校验注解，
   推荐项按列类型与业务语义预选，用户只调例外

既有表若与仓库约定冲突（如无 `deleted` 列、表名无前缀），不擅自改表：在映射表旁列出冲突点与
两种走法（照表生成并登记差异 / 先修表再生成），让用户选。
