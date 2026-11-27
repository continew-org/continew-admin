---
name: ocn-admin-mysql2pgsql
description: 在 continew-admin 后端把目标 profile 的 MySQL 配置切换为 PostgreSQL，只改配置（JDBC、MyBatis 方言、Liquibase 入口、运行时驱动），不修改任何 SQL 语句。默认仅切 dev，不负责实例、凭据、数据搬迁或连通性。
agent_created: true
---

# MySQL → PostgreSQL 配置切换

## 0. 范围：只改配置，不改 SQL

本技能只动四类配置：数据源、MyBatis 方言、Liquibase 入口、运行时驱动。

**不改任何 SQL**：不改 mapper XML 的语句与 `databaseId` 分支，不改 `db/changelog/**/*.sql` 的内容，不新增或转换迁移脚本。既有 SQL 由仓库维护者负责；切换后 SQL 若报方言错误，只报告位置，交给开发者另行处理。

目标是：配置改完后，开发者建好库、填好账号密码即可启动。**连通性、建库、权限与数据迁移不在本技能范围。**

## 1. 先记录基线，再判定是否需要修改

从仓库根运行 `python .agents/skills/ocn-admin-mysql2pgsql/scripts/scan_mysql_refs.py . -p dev`。退出码 0 仅表示扫描器的静态检查通过；1 表示有阻塞项、证据不足或扫描不完整；2 表示参数或根目录无效。只将 MUST / WARN / INFO 用作定位线索；扫描结果不等于 profile 合并结果，引用配置片段时先脱敏。

分别检查 `continew-server/src/main/resources/config/` 下共享 `application.yml` 和 dev、prod 覆盖文件，沿各自的 `spring.liquibase.change-log` 检查活动 include，再检查 `continew-common/pom.xml` 的有效依赖（排除注释）。忽略 `src/test/**` 下的配置——测试资源不是独立部署的应用。记录未提交改动；不要为清零扫描告警覆盖用户工作。

下表是**仅切 dev 的目标，不是当前状态声明**：

| 核对项 | dev | prod |
|:--|:--|:--|
| JDBC | `jdbc:p6spy:postgresql://` + P6SpyDriver | 原 `jdbc:mysql://` + MySQL 裸驱动 |
| mapper / 分页 | dev 显式覆盖 `pgsql` / `POSTGRE_SQL` | 原共享 `mysql` / `MYSQL` |
| Liquibase | dev 独立 PG 入口 | 原 master 与 MySQL include 顺序不变 |
| 运行时依赖 | PG JDBC | 保留 MySQL JDBC |

若共享方言或 master 已切 PG，或 MySQL 依赖已被注释，即使 prod 文件没变也不能宣称 prod 不受影响。报告冲突；未经授权不改共享配置、原 master、prod 或其他已有改动。

## 2. 核对 dev 数据源

在 `application-dev.yml` 合并以下脱敏示意。优先核对已有 PG 段，不重复声明键；保留变量名、P6Spy 包装及已配置的时区、绑定和超时选项。端口 5432 是默认值，不覆盖部署方显式指定的端口。

```yaml
spring.datasource:
  url: jdbc:p6spy:postgresql://${DB_HOST}:${DB_PORT:5432}/${DB_NAME}?options=-c%20TimeZone=Asia/Shanghai&sslmode=prefer&channelBinding=prefer&stringtype=unspecified&socketTimeout=30&connectTimeout=10
  username: ${DB_USER}
  password: ${DB_PWD}
  driver-class-name: com.p6spy.engine.spy.P6SpyDriver
```

保留目标 profile 的原 MySQL 数据源整段为注释，不删除；只启用 PG 段。上例仅示意结构，不得据此删改原变量或默认值。PG 的 host、库名、用户名、密码必须逐项沿用该 MySQL 基线；连接要素中只改端口，不从 prod 借值，不另设账号或密码。四项缺失、不同或基线不唯一时不得判定通过。仅在本地比较配置，不展开环境变量，不在命令或报告中输出实际值。`sslmode=prefer` 是仓内 dev 示例，不是强制加密策略。prod 使用裸 JDBC，不套用 dev 的代理驱动。

## 3. 隔离 mapper、分页和 Liquibase

在 dev 的对应节点合并以下键，不重复声明 YAML 父节点：

```yaml
mybatis-plus:
  configuration:
    database-id: pgsql
  extension:
    pagination:
      db-type: POSTGRE_SQL
spring.liquibase:
  change-log: classpath:/db/changelog/db.changelog-dev-pgsql.yaml
```

`database-id` 只是让 MyBatis 挑选已有语句，**不改写任何 mapper XML**。

该独立入口是目标文件，并非假定已存在；其内容只 include 仓库中**已存在**的 PG 脚本，顺序为 `main_table.sql`、`main_data.sql`、`plugin/plugin_open.sql`、`plugin/plugin_tenant.sql`、`plugin/plugin_schedule.sql`、`plugin/plugin_generator_removal.sql`，路径写作 `file: db/changelog/postgresql/<相对文件名>`，不包含共享 master。这六个 `include` 必须是根 `databaseChangeLog` 的**直接子项**：放进 `changeSet` 或 `changeSet.changes` 的 `include` 不会被 Liquibase 当作迁移执行，路径与顺序再对也不算数；`sqlFile` 只在 `changeSet` 内执行，会改用外层 changeSet 身份，不能替代 `include`。入口里也不应夹带原生 `changeSet`（`dropTable` 等），它不在清单中、无法确认属于预期迁移。`include` 上的 `contextFilter` / `labels` 只在匹配时才执行：`spring.liquibase.contexts` 未配置时普通 context 视为全部执行，但 `@name`（显式 context）要求运行时明确指定；`contexts` 写成 YAML 列表时按列表元素取值，高优先级文件整体替换而非追加。`labels` 由运行时的 `spring.liquibase.label-filter` 表达式筛选，方向与活动集合相反。把必需建表脚本限定在别的 context 就等于不跑它。若仓库缺少某个 PG 脚本，报告为既有资产缺失，由开发者补齐，**不自行编写或转换 SQL**。不因两边 id 相同而重命名已发布 changeset。详见 `references/dialect-mapping.md`。

Liquibase 默认复用业务数据源。若配置里出现 `spring.liquibase.url`，它会连到另一个库，此时「业务库已切 PG」不代表迁移跑在 PG 上——必须让它与业务数据源同库（同 host、端口、库名、`currentSchema`），或直接删掉这一项让它走业务数据源。复用业务数据源时，`spring.liquibase.default-schema` 仍可单独指定迁移的默认 schema，它不会出现在 URL 里，必须独立核对：与业务连接实际使用的 schema（URL 的 `currentSchema`，缺省为 `public`）不一致时，迁移建的表在业务查询里找不到。迁移脚本所在 `changeSet` 和 `sqlFile` 两侧的 `dbms` 都要命中目标库（`all` 命中，`!postgresql` 排除；`mysql` 与 `mariadb` 是不同的短名，不互相等价）。

## 4. 检查运行时依赖

在 `continew-common/pom.xml` 核对 `org.postgresql:postgresql` 和 prod 所需的 `com.mysql:mysql-connector-j`。注释、dependencyManagement、test/provided、非 jar 的 type/classifier、`<optional>true</optional>` 都不证明驱动能传到应用运行时。直接依赖省略 `scope` 时取的是有效 POM 的受管值，不是一律按 compile：受管值可能来自父链的 `dependencyManagement`，也可能来自 `scope=import` + `type=pom` 导入的 BOM（Spring Boot 的驱动版本就是这条路径），受管为 test / provided 同样进不了运行时 classpath（同一 POM 里显式写的条目优先于它导入的 BOM），受管值解析不出来（如本地仓库缺该 BOM）就记为未确认，不要默认放行。仅当所有环境都明确不再使用 MySQL 且获授权时才移除其运行时依赖。不改测试容器或测试 profile。

若 server 消费本地已安装的 common 而非同一 reactor，修改 common POM 后需更新本地制品。以下命令仅核对 reactor 的依赖树，不会更新本地制品。离线时只用已有 Maven 与缓存，在仓根运行 `mvn -o -pl continew-server -am dependency:tree -Dscope=runtime`；缺缓存则记录未验证，不下载、不跳过门禁、不把静态声明说成编译或运行时验证通过。

## 5. 交付边界

逐项报告目标 profile、有效方言、入口与驱动，以及其他 profile 的基线差异。仅列数据库变量名与配置路径，提醒开发者自行核对 Redis 的 host、port、password、database，不列实际值。Redis、实例创建、角色和授权、密码设置与连通性不在切换范围。

SQL 相关的发现（mapper 缺少 PG 分支、迁移脚本疑似残留 MySQL 语法、脚本为空）只作为 WARN 报告位置，**不是本技能的修改项**，也不阻塞配置验收。

不启动应用、不运行 Liquibase、不尝试任何账号或探测端口。`scripts/probe_pg.py` 仅供开发者另行获授权后手动排查，不是扫描、切换或验收步骤。静态通过不等于能连接或数据已迁移。
