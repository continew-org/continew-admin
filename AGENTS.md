# AGENTS.md

本文件为在本代码库中工作的 AI 编程智能体（DeepSeek Harness、Claude Code、Codex、Cursor 等）提供指引。面向人类贡献者的说明请查阅 [CONTRIBUTING.md](./CONTRIBUTING.md)。

## AI 贡献准则

- **不得以 AI 身份在 Issue 或 PR 上发表评论**。讨论区只属于人类。
- **先讨论再实现**：非平凡改动（如新功能、重构）开工前，先在 Issue 评论中与维护者就实现方向达成一致。
- **第三方依赖版本不由本项目管理**：第三方依赖版本统一由父 POM（ContiNew Starter）的依赖管理供给，禁止在模块 POM 中硬编码版本号；升级基础能力通过更换 `continew-starter` parent 版本完成。
- **内部模块依赖统一写 `${project.groupId}`**：模块 POM 中的内部依赖 groupId 一律使用 `${project.groupId}`，版本由根 POM `dependencyManagement`（`${revision}`）统一供给；`<parent>` 与项目自身坐标保持字面量。
- **新增模块需在两处注册**：根 POM 的 `<modules>` 与根 POM `dependencyManagement`。
- **数据库结构变更必须走 Liquibase**：新增或修改变更集后需在 `continew-server/src/main/resources/db/changelog/db.changelog-master.yaml` 登记，且 `mysql/` 与 `postgresql/` 两套 SQL 必须同步提供。
- **新增公共表需登记租户白名单**：不参与租户隔离的表必须加入 `continew-starter.tenant.ignore-tables` 配置。
- **披露 AI 使用**：当提交中较大部分由 AI 生成时，请在 commit message 末尾追加 trailer，注明实际使用的智能体，例如：

  ```
  Assisted-by: DeepSeek Harness
  ```
- 贡献流程一律**遵循 [CONTRIBUTING.md](./CONTRIBUTING.md)**。

## 项目概述

ContiNew Admin（Continue New Admin）是基于 ContiNew Starter 构建的多租户中后台管理系统框架，前后端分离：本仓库为后端（Spring Boot 3.5 / Java 17 的 Maven 多模块工程），前端为独立仓库 `continew-admin-ui`（Vue3 & Arco Design & TS & Vite）。**这是一个可运行的应用项目**（启动类 `top.continew.admin.ContiNewAdminApplication`，位于 continew-server 模块，默认 dev 环境端口 8000），而非类库。

**当前版本**：4.2.0-SNAPSHOT（`${revision}` 统一管理） | **主分支**：`dev` | **Java**：JDK 17 | **构建**：Maven 3.9.16（`./mvnw`，Windows 为 `mvnw.cmd`）

## 核心架构

Maven 多模块工程，根 `pom.xml` 用 `flatten-maven-plugin` 统一 `${revision}` 版本。模块依赖自上而下（server 依赖 system，system 依赖 common）：

| 模块 | 职责 |
|:-----|:-----|
| `continew-server` | 打包部署入口（启动类 + 通用 controller + Liquibase + 配置文件） |
| `continew-system` | 核心业务：auth（系统认证）、system（部门/角色/用户/菜单/字典/文件/公告/系统配置等） |
| `continew-common` | 公共基类、工具、配置（CRUD/MyBatis/Websocket/Doc/Excel/Exception） |
| `continew-plugin` | 独立可插拔插件：open（能力开放 AK/SK）、tenant（多租户 SaaS）、schedule（任务调度）、generator（代码生成器） |
| `continew-extension` | 可独立部署的扩展服务（continew-extension-schedule-server 为 Snail Job 服务端，可选） |

### 关键机制

- **内部 API 解耦**：业务模块将对其他模块暴露的公共业务定义为接口，放在 `continew-common` 的 `top.continew.admin.common.api` 包下，由对应 biz 模块的 `api/` 目录提供实现；其他模块依赖 common 的接口而非具体实现，避免循环依赖。新增跨模块调用时遵循此模式。
- **CRUD 套件**：封装于 ContiNew Starter 的 `continew-starter-crud`。`BaseController<S, L, D, Q, C>` + `@CrudRequestMapping(value, api = {...})` 声明即可自动生成 CRUD 接口，`BaseController.preHandle` 根据 API 类型自动生成并校验权限码（如 `POST /system/user` → `system:user:create`）。
- **数据填充**：`xxx/container/` 目录配置 Crane4j 容器，基于注解完成数据映射，减少单字段联表查询。
- **多租户**：`continew-starter.tenant` 行级隔离（LINE），租户 ID 经 `X-Tenant-Id`/`X-Tenant-Code` 头传递，默认租户 `0`。
- **认证与权限**：Sa-Token + JWT（Token 名 `Authorization`，jwt-simple 模式）；前端传入密码经 RSA 公钥加密，后端 `SecureUtils.decryptPasswordByRsaPrivateKey` 解密；数据权限由 `DataPermissionMapper` 提供。内置用户和角色不允许变更。
- **缓存**：JetCache 二级缓存（Caffeine + Redisson），`broadcastChannel` 开启多 JVM 本地缓存失效广播。
- **ID 生成**：CosID 雪花算法（`COSID`），租户编码生成器前缀 `T`。
- **全局响应**：Graceful Response 统一封装，响应类 `top.continew.starter.web.model.R`。
- **任务调度**：`continew-plugin-schedule` 通过 OpenAPI 对接 Snail Job 服务端，`snail-job.enabled: false` 默认关闭。

### 分层目录约定

每个业务功能按以下子包组织（参考 continew-system 的 auth/system 子包）：

| 子包 | 职责 |
|:--|:--|
| `controller` | REST API |
| `service` / `service.impl` | 业务接口与实现 |
| `mapper` | MyBatis Plus Mapper（XML 统一放 `src/main/resources/mapper/`） |
| `model.entity` / `query` / `req` / `resp` | 实体 / 查询 / 请求 / 响应 |
| `enums` / `constant` / `util` / `config` | 枚举 / 常量 / 工具 / 配置 |
| `container` | Crane4j 数据填充容器配置 |
| `api` | 对外公共业务 API 实现（配合 common.api 接口） |
| `validation` | 自定义校验注解/工具 |

## 构建与运行命令

```bash
# 完整构建（全部门禁：validate 阶段 Enforcer -> Spotless -> Checkstyle，编译，verify 阶段 SpotBugs）
./mvnw verify

# 仅编译（含 validate 阶段三道门禁，不含 SpotBugs）——仅用于快速迭代，不可作为提交前自检
./mvnw compile

# 编译单个模块（含依赖模块）
./mvnw -pl :continew-system -am compile

# 自动修复格式（被 Spotless 门禁拦截时使用：格式化 + 清理无用 import + 补 License Header）
./mvnw compile -Pformat

# 打包（默认瘦包，依赖、配置外置，生产部署用）
./mvnw package

# 胖包打包（依赖、配置打入 jar）
./mvnw clean package -P fat-jar

# 清理所有 target 目录及 flatten 生成的 .flattened-pom.xml
./mvnw clean
```

本项目 `maven-surefire-plugin` 已设置 `skip=true`，单元测试默认跳过。代码改动的验证方式是执行 `./mvnw verify` 确保四道门禁全部通过。

运行时数据源/Redis 可通过环境变量注入：`DB_HOST`、`DB_PORT`、`DB_USER`、`DB_PWD`、`DB_NAME`；`REDIS_HOST`、`REDIS_PORT`、`REDIS_PWD`、`REDIS_DB`。配置文件位于 `continew-server/src/main/resources/config/`（application.yml 通用，application-dev.yml / application-prod.yml 分环境）。

### 提交前门禁（必须通过）

提交 Java 代码前，AI 智能体**必须**让门禁通过：

1. 执行 `./mvnw verify`。四道门禁依次为：validate 阶段的 **Enforcer**（构建环境与依赖合规）、**Spotless check**（代码格式）、**Checkstyle**（代码规范），以及编译后 verify 阶段的 **SpotBugs**（字节码缺陷），任一不通过都会直接构建失败。
2. 若被 Spotless 拦截，执行 `./mvnw compile -Pformat` 自动修复，然后再执行一次 `./mvnw verify` 确认通过。
3. 四道门禁全部通过后才能提交。

构建过程**不会修改任何源码文件**；`-Pformat` 是唯一会修改源码的 profile。不要用 IDE 格式化或 `git diff --check` 替代 Spotless 门禁——IDE 格式化引擎是另一套实现，可能放行项目格式化器拒绝的代码。

> **Windows 注意**：提交前执行 `./mvnw verify` 后勿再在 IDE 中打开代码窗口，避免不同 IDE 配置导致的格式差异。

## 代码风格

遵循**阿里巴巴《Java 开发手册(黄山版)》**（P3C）。全部风格配置集中于 `style/` 目录（OCN-CodeStyle，基于 Apache Nacos 社区代码风格调整，OpenContiNew 社区各项目通用）：

- Eclipse Formatter 配置（唯一事实源，Spotless 使用）：[`style/ocn-eclipse-formatter.xml`](style/ocn-eclipse-formatter.xml)
- IDEA 代码风格（近似映射）：[`style/ocn-idea-code-style.xml`](style/ocn-idea-code-style.xml)
- Checkstyle 规则：[`style/ocn-checkstyle.xml`](style/ocn-checkstyle.xml)
- 风格说明与 IDE 配置指引：[`style/STYLE.md`](style/STYLE.md)

### AI 智能体关键规则

| 规则 | 值 |
|------|-----|
| 缩进 | **4 空格**（禁用 Tab），续行缩进 4 空格 |
| 行宽 | 最多 **100 字符**（由 Spotless 的 Eclipse 格式化器 `lineSplit=100` 强制；Checkstyle `LineLength` 设为 150 仅作兜底） |
| 星号导入 | **禁止**（`AvoidStarImport`），包括静态星导入 |
| 无用 import | **禁止**（`-Pformat` 自动清理） |
| 大括号 | `if/else/for/while/do-while` 必须加大括号（`NeedBraces`） |
| 空行 | 连续空行最多保留 1 行（`EmptyLineSeparator`） |
| 类注释 | 必须包含 `@author` 与 `@since` 标签；公共方法需有 Javadoc |
| 重载方法 | 同一组重载必须相邻声明（`OverloadMethodsDeclarationOrder`） |
| 格式化豁免 | `// @formatter:off` 与 `// @formatter:on` 之间的代码不参与格式化 |
| 命名 | `style/` 配置文件与 agent 技能统一使用 `ocn-` 前缀（OCN = OpenContiNew） |

### License Header

每个新增 Java 源文件**必须**包含 Apache-2.0 License Header。Spotless 在 validate 阶段校验，`./mvnw compile -Pformat` 可自动补全。模板位于 [`style/license-header`](style/license-header)：

```java
/*
 * Copyright (c) 2022-present Charles7c Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
```

### Lombok 约定

`lombok.config` 全局配置：继承场景自动应用 `@EqualsAndHashCode(callSuper = true)` 和 `@ToString(callSuper = true)`；**禁用** `@val`、`@Log4j`、`@Log4j2`（报 ERROR），日志统一用 `@Slf4j`。

## PR 约定

所有 PR 必须提交到 `dev` 分支（新功能与功能优化）；维护分支 `x.x.x` 仅接受 bug 修复。请基于目标分支创建特性分支（如 `feat/new-feature`），不要直接修改源分支。遵循 [PR 模板](.github/PULL_REQUEST_TEMPLATE.md)。

**提交格式**：[Conventional Commits（约定式提交）1.0.0](https://www.conventionalcommits.org/zh-hans/v1.0.0/)规范，`<类型>[可选作用域]: <描述>`，破坏性变更在类型或作用域后追加 `!`，如 `feat(system): 新增 xxx`、`feat!:`。

**提交前检查**：

```bash
./mvnw verify     # 四道门禁必须全部通过（被 Spotless 拦截时使用 -Pformat）
```

## 安全漏洞

不得通过 GitHub Issue 报告安全漏洞。请使用 GitHub 私有漏洞报告——详见 [SECURITY.md](./SECURITY.md)。

## Agent Skills

各 agent 工具（DeepSeek Harness / Claude Code / Codex）共用的技能统一存放在 `.agents/skills/` 作为唯一事实源——每个技能一个目录、含 `SKILL.md`。新增技能沿用 `ocn-` 命名前缀。
