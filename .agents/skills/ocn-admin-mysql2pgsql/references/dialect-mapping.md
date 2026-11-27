# MySQL → PostgreSQL 切换速查

本技能只改配置，不改 SQL。下面的 SQL 对照表仅用于**判断报错原因**，不是修改清单。

## JDBC 与驱动

- dev 使用 P6Spy：保留 `com.p6spy.engine.spy.P6SpyDriver`，把代理 URL 的 `mysql://` 改成 `postgresql://`。prod 未切换时，保留原 MySQL 裸 URL 和 `com.mysql.cj.jdbc.Driver`。不要仅搜索 `jdbc:mysql`。
- 保留目标 profile 原 MySQL 数据源整段注释作为回滚与比对基线。PG 的 host、库名、用户名、密码逐项沿用原配置，包括变量表达式及默认值；连接要素中只改端口（PG 默认 5432）。不借用 prod，不输出字段实际值，也不尝试登录。
- dev URL 完整示意见 `../SKILL.md` 第 2 节。`options=-c%20TimeZone=Asia/Shanghai` 设置会话时区；`stringtype=unspecified` 让服务端推断字符串绑定类型；`connectTimeout` 与 `socketTimeout` 的单位都是秒。
- 目标 profile 用块标量（`url: >-`）或多行写法时，折叠后的文本才是生效值；`.properties` 中值末尾的空白属于值本身，与 YAML 的标量语义不同，比对密码时不要 strip。`#` / `!` 注释行不支持续行。
- `sslmode=prefer` 允许回退，不等价于强制 TLS；需要校验服务端身份时按部署策略核对 `verify-full` 和受信证书。`channelBinding=prefer` 不代替证书验证。
- 不照搬 MySQL 的 `characterEncoding`、`allowPublicKeyRetrieval`、`nullCatalogMeansCurrent`。批量 INSERT 重写是驱动优化，如有需要单独评估 PG 的 `reWriteBatchedInserts`，不默认追加。

## SQL 语法（仅用于定位报错，不在本技能修改）

| MySQL | PostgreSQL | 核对点 |
|:--|:--|:--|
| 反引号标识符 | 双引号或不引用 | 不引用时折叠为小写；已引用的混合大小写必须匹配 |
| `IFNULL(a,b)` / `IF(cond,a,b)` | `COALESCE(a,b)` / `CASE WHEN cond THEN a ELSE b END` | 检查分支类型 |
| `GROUP_CONCAT(x SEPARATOR ',')` | `STRING_AGG(x, ',')` | 有顺序要求时显式指定 ORDER BY |
| `DATE_FORMAT(d,'%Y-%m-%d')` | `TO_CHAR(d,'YYYY-MM-DD')` | 格式串不同 |
| `STR_TO_DATE(s,'%Y-%m-%d')` | `TO_DATE(s,'YYYY-MM-DD')` | 含时间时另选时间戳类型并核对时区 |
| `CURDATE()` / `NOW()` | `CURRENT_DATE` / `CURRENT_TIMESTAMP` | PG 取事务开始时间 |
| `LIMIT 10,20` | `LIMIT 20 OFFSET 10` | 稳定分页需确定排序 |
| `ON DUPLICATE KEY UPDATE` | `ON CONFLICT ... DO UPDATE SET ...` | 明确匹配的唯一索引或约束 |
| `INSERT IGNORE` | `ON CONFLICT DO NOTHING` | 只处理冲突，不忽略所有错误 |
| `REPLACE INTO` | 无直接等价 | 更新不等于删除再插入 |
| `AUTO_INCREMENT` / `LAST_INSERT_ID()` | identity / `INSERT ... RETURNING id` | 显式导入主键时核对序列 |
| `UNSIGNED` | 足够宽的类型及 CHECK | 检查上下界，不只检查非负 |
| `TINYINT(1)` | `boolean` | 仅确认为布尔域时转换 |
| `DATETIME` | `timestamp` 或 `timestamptz` | 按业务时间语义选 |
| `BLOB` / `LONGBLOB` | `bytea` | 核对读写绑定 |
| `FIND_IN_SET(x,list)` | `x = ANY(string_to_array(list, ','))` | 只替代成员判断，不返回位置 |
| `REGEXP` | `~` 或 `~*` | 核对大小写及正则语法 |

## profile、依赖与迁移历史

- 仅切 dev 时，在 dev 同时覆盖 `mybatis-plus.configuration.database-id: pgsql` 和 `mybatis-plus.extension.pagination.db-type: POSTGRE_SQL`；分别核对 prod 的有效值，不凭文件名判断是否隔离。
- `continew-common/pom.xml` 的实际运行时依赖应覆盖所有仍启用的数据库；不要改测试驱动或测试 profile。配置缺少驱动时先查运行时 classpath，而不是尝试登录。直接依赖省略 `scope` 时按有效 POM 的受管值判定：父链 `dependencyManagement` 与 `scope=import` 导入的 BOM 都算来源，同一 POM 里显式写的条目优先于它导入的 BOM；受管为 test / provided 不算运行时可用，BOM 在本机仓库缺失时记为未确认，不按 compile 放行。
- dev 的独立 Liquibase 入口目标、路径和 include 顺序见 `../SKILL.md` 第 3 节。六个 `include` 须是根 `databaseChangeLog` 的直接子项，放进 `changeSet.changes` 的 `include` 不会被执行，`sqlFile` 会改换外层 changeSet 身份。限定 `contextFilter` / `labels` 的 include 只在匹配时才执行：`spring.liquibase.contexts` 未配置时普通 context 视为全部执行，但 `@name`（显式 context）要求运行时明确指定；`contexts` 写成 YAML 列表时按列表元素取值，且高优先级文件会整体替换低优先级的列表而非追加。`labels` 由运行时的 `spring.liquibase.label-filter` 表达式筛选，方向与活动集合相反，不要与之混用。不匹配或不支持的表达式按无法确认处理。只 include 仓库已有的 PG 脚本；缺脚本属既有资产问题，报告而不代写。Liquibase 在目标库的 `DATABASECHANGELOG` 中以 `(id, author, filename)` 识别 changeset；注意 `logicalFilePath` 会影响记录的路径。不同数据库不共享历史，仅凭两侧 id 相同不能断言被跳过。不要改已发布标识。
- `.properties` 的值里，反斜杠转义出来的空白属于值本身（`password=\ secret` 的密码以空格开头）。必须**先按未转义的分隔符拆键值、再各自解码**，顺序反了会把这段空格当成键值间隔而吞掉，让两个不同的密码看起来一致。YAML 合并键 `<<: *alias`（块形式与行内映射 `{<<: *alias}` 都一样）、纯键行（值为空串）、`\uXXXX` 转义的键都属合法写法：合并键无法静态展开按解析不完整报告；纯键行会把共享值清空；转义键要先解码再比对。
- 两类别名/间接引用无法静态展开，须报无法确认而非按共享值放行：YAML 别名作节点值（`spring.datasource: *legacy`，块形式与行内映射 `{datasource: *legacy}` 都一样）、`spring.profiles.include` / `spring.profiles.group.<名>` 激活的附加 profile 文件（其中可单独设 `spring.liquibase.url`）。`spring.profiles.active` 不在此列，它选的就是被扫的目标 profile。
- 列表与标量的覆盖关系是双向整体替换：高优先级文件用列表覆盖标量、或用标量覆盖列表，旧值都作废，不能取并集。YAML 允许序列项与所属键同列（`contexts:` 下 `- dev` 顶格），此时列表仍归属该键。入口 changelog 里夹带原生 `changeSet`（`dropTable` 等，块形式或 `- {changeSet: ...}` 行内形式）不在 include 清单中，无法确认属于预期迁移，必须阻塞。
- schema 有两条独立路径：`spring.liquibase.url` 的 `currentSchema` 必须与业务 URL 一致；复用业务数据源时 `spring.liquibase.default-schema` 与业务 schema（URL 的 `currentSchema`，缺省 `public`）不一致同样会让迁移建的表在业务查询里找不到。
- 遇到缺表提示，静态核对 profile、入口、include、schema 和 changeset 路径；认证或授权问题交由开发者核对，不执行建库、改密或真实验证。
