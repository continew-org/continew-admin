# P4 数据库与菜单（两步都不可省，范围不含则跳过对应小节）

方言与写法对照 `examples/db/`（唯一一套端到端验证过的 SQL 产物）。

## 1. Liquibase 建表变更集

- **先判定要不要写**：表为本次新建（模式 A，或模式 B 但确认库里还没有）才写建表变更集；
  模式 B 且表已存在于库或已有变更集登记时，跳过建表变更集，只登记菜单变更集
- `mysql/` 与 `postgresql/` **两份 SQL 同步提供**，目录
  `continew-server/src/main/resources/db/changelog/{mysql,postgresql}/<domain>/<name>.sql`
- 新建表写成**新的 changeset 文件**，**不要改已有 changeset**（checksum 会失配，启动即失败）
- 表结构含五审计列 + deleted 索引条件，与 `main_table.sql`（`mysql/` 与 `postgresql/` 下同名文件）
  现有写法一致；方言差异（反引号 vs 双引号、类型对齐）参照 `examples/db/` 两份 biz_demo.sql
- 在 `continew-server/src/main/resources/db/changelog/db.changelog-master.yaml` 登记 include：
  MySQL 行插在 MySQL 段最后一个 include 之后真实生效；PostgreSQL 段当前整段处于注释状态，
  PG 行按注释风格插入（保持现状，不擅自启用）
- changeset id 用 `1`、author 用使用者（如 `charles7c`），注释只写当前约束

## 2. 菜单数据

- 生成 `sys_menu` 的 INSERT SQL（目录 type=1 / 页面 type=2 / 按钮 type=3），
  存放 `db/changelog/mysql/<domain>/<name>_menu.sql`（MySQL 语法），并按第 1 节同法登记变更集；
  PostgreSQL 环境按 `postgresql/main_data.sql` 既有风格把 `b'0'` 改为 `false` 后另存一份，两份同步提供
- 页面菜单的 path 填前端路由 URL（如 `/system/goods`），挂载点按字段映射轮选项
  （挂「系统管理」目录 / 新建目录）；**component 字段不参与新前端路由解析，
  填与不填都不影响页面渲染，但为兼容旧约定仍按 `<module>/<name>/index` 填**
- 按钮菜单的 permission 必须与 SKILL.md §4 推导的权限码逐字一致；
  Api 集里没有的（如未选 EXPORT）不造对应按钮
- 菜单 ID 段位：整段自拟（样例用 4000-4005），存量已占用：1000-2055（系统与监控）、
  3000-3025（能力开放）、7000-7018（任务调度）、8000-8023（多租户）；
  9000 段为已退役模块的历史段位勿复用；与库内数据冲突时整段换号
- 挂载既有目录时先查该目录的真实 ID（`sys_menu` 存量数据或 main_data.sql），不要猜 1000 之外的值
- 菜单 SQL 不含角色授权：实跑阶段在「菜单管理 → 角色授权」给目标角色勾选，
  或按需补 `sys_role_menu` 的 INSERT（样例验证走的是实跑时手工授权 admin）

## 3. 租户白名单

- 业务表默认参与租户隔离（模式 A 建表带 `tenant_id`），无需登记
- **不参与隔离的表必须登记 `continew-starter.tenant.ignore-tables`**
  （`continew-server/src/main/resources/config/application.yml`），并在交付说明里写明理由；
  漏登记会导致多租户环境查询串租户或直接报错
