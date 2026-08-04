# CLAUDE.md

This file provides guidance to AI agents when working with code in this repository.

`CLAUDE.md` and `AGENTS.md` are mirror files. Whenever either file changes, apply the identical change to the other file and verify that their contents remain byte-for-byte identical（标题文件名除外）.

## 项目概述

ContiNew Admin（Continue New Admin）是 **AI 编程纪元** 下基于 ContiNew Starter 构建的高质量多租户中后台管理框架。后端基于 Spring Boot 3.3 / Java 17，前端（独立仓库 `continew-admin-ui`）基于 Vue3 & Arco Design & TS & Vite。后端遵循阿里巴巴《Java开发手册(黄山版)》，注释覆盖率 > 45%，接口参数示例 100%。

当前版本：`4.2.0-SNAPSHOT`（分支 `feat/starter-2.16.0`），依赖 ContiNew Starter `2.16.0-SNAPSHOT`。

## 常用命令

**构建/编译**（提交前必做，会自动执行 Spotless 代码格式化）：
```bash
mvn compile                  # 编译并自动格式化代码（提交前务必执行，执行后勿再打开 IDE 代码窗口）
mvn clean package -P fat-jar # 胖包打包（依赖、配置打入 jar）
mvn clean package           # 默认瘦包模式（依赖、配置外置，生产部署用）
```

**运行**：
```bash
# 启动类：top.continew.admin.ContiNewAdminApplication（位于 continew-server 模块）
# 默认 dev 环境端口 8000，可通过 IDEA 环境变量配置数据源/Redis：DB_HOST、DB_PORT、DB_USER、DB_PWD、DB_NAME；REDIS_HOST、REDIS_PORT、REDIS_PWD、REDIS_DB
```

**测试**：`pom.xml` 中 `maven-surefire-plugin` 已设置 `skip=true`，单元测试默认跳过；`continew-server/pom.xml` 引入了 `spring-boot-starter-test` 供本地使用。

**代码质量**（Sonar）：
```bash
mvn verify -P sonar  # 触发 SonarCloud 扫描（host: sonarcloud.io，org: charles7c）
```

**数据库迁移**：Liquibase 自动执行，变更日志 `continew-server/src/main/resources/db/changelog/db.changelog-master.yaml`，初始 SQL 位于其下 `mysql/` 或 `postgresql/` 目录。新增变更需在该 YAML 中登记。

**Docker 部署**：`docker/docker-compose.yml` 包含 mysql、redis、continew-server（18000 业务端口 / 17889 任务调度客户端端口）、continew-web（Nginx 80/443）、schedule-server（18001/17888）。生产环境通过 `PROFILES_ACTIVE=prod` 等环境变量注入配置。

## 代码规范（强制）

1. **代码格式化**：Spotless 插件绑定到 `compile` 阶段，使用 `.style/p3c-codestyle.xml`（P3C 阿里规范）和 `.style/license-header`（Apache-2.0 文件头）。提交前执行 `mvn compile`，之后勿再在 IDE 中打开代码窗口以免格式差异。
2. **Lombok 全局配置**（`lombok.config`）：继承场景自动应用 `@EqualsAndHashCode(callSuper = true)` 和 `@ToString(callSuper = true)`；**禁用** `@val`、`@Log4j`、`@Log4j2`（报 ERROR），日志统一用 `@Slf4j`。
3. **提交规范**：遵循 [Angular 提交规范](https://github.com/conventional-changelog/conventional-changelog/tree/master/packages/conventional-changelog-angular)，参考 `CHANGELOG.md` 已有风格。
4. **命名风格**：统一「名词 + 动词 + 类型」，前端模板简化命名（如 `AddDrawer` 而非 `UserAddDrawer`）。
5. **物理删除已改为逻辑删除**：`mybatis-plus.global-config.db-config` 配置 `logic-delete-field: deleted`、`logic-delete-value: id`（解决唯一索引冲突），所有 DO 默认继承 `BaseDO`。

## 架构总览

### 模块依赖与分层

项目为 Maven 多模块工程，根 `pom.xml` 用 `flatten-maven-plugin` 统一 `${revision}` 版本，模块划分如下（依赖自上而下，server 依赖 system，system 依赖 common）：

```
continew-server        打包部署入口（启动类 ContiNewAdminApplication）+ 通用 controller + Liquibase + 配置文件
  └ continew-system    核心业务：auth（系统认证）、system（部门/角色/用户/菜单/字典/文件/公告/系统配置等）
    └ continew-common 公共基类、工具、配置（CRUD/MyBatis/Websocket/Doc/Excel/Exception）
continew-plugin        独立可插拔插件
  ├ continew-plugin-open       能力开放（AK/SK、签名算法）
  ├ continew-plugin-tenant    多租户（SaaS）
  ├ continew-plugin-schedule  任务调度（基于 Snail Job Open API）
  └ continew-plugin-generator 代码生成器（前后端模板）
continew-extension     扩展服务
  └ continew-extension-schedule-server  Snail Job 服务端（可选，公司统一提供环境可删）
```

**插件化趋势**：`continew-plugin-*` 后续将改造为独立插件；`continew-extension/*` 为可独立部署的辅助服务。

### 内部 API 解耦模式（common 的 api 包）

为降低模块耦合，业务模块（如 system、tenant）将**对其他模块暴露的公共业务**定义为接口，放在 `continew-common` 的 `top.continew.admin.common.api` 包下，由对应 biz 模块提供实现（`xxx/api/` 目录）。其他模块依赖 common 的接口而非具体实现，避免循环依赖。新增跨模块调用时遵循此模式。

### CRUD 套件（核心生产力）

封装于 ContiNew Starter 的 `continew-starter-crud`，本仓库在 `continew-common` 提供基类适配：

- `BaseController<S, L, D, Q, C>` 继承 `AbstractCrudController`，泛型依次为：Service、列表类型、详情类型、查询类型、创建/修改请求类型。
- `BaseService<L, D, Q, C>` 继承 `CrudService`。
- `BaseDO` / `BaseCreateDO` / `BaseUpdateDO` / `TenantBaseDO`（带租户字段）实体基类。
- `BaseResp` / `BaseDetailResp` 响应基类。

**Controller 用法极简**：通过 `@CrudRequestMapping(value, api = {...})` 声明开放的 API，基类自动生成 CRUD 接口。可选项：`Api.PAGE`、`Api.LIST`、`Api.TREE`、`Api.TREE_DICT`、`Api.DICT`、`Api.GET`、`Api.CREATE`、`Api.UPDATE`、`Api.DELETE`、`Api.BATCH_DELETE`、`Api.EXPORT`。

示例（部门管理，全套 CRUD 仅需声明）：
```java
@Tag(name = "部门管理 API")
@RestController
@CrudRequestMapping(value = "/system/dept", api = {Api.TREE, Api.GET, Api.CREATE, Api.UPDATE,
    Api.BATCH_DELETE, Api.EXPORT, Api.TREE_DICT})
public class DeptController extends BaseController<DeptService, DeptResp, DeptResp, DeptQuery, DeptReq> {}
```

**权限自动校验**：`BaseController.preHandle` 根据 CRUD API 类型生成权限码并校验，例如 `POST /system/user` → `system:user:create`。权限前缀由 `CrudApiPermissionPrefixCache` 缓存。放行场景：
- 带 `sign` 参数（API 签名，SaSignTemplate）
- 类/方法标注 `@SaIgnore`
- 命中 `sa-token.extension.security.excludes` 路径
- `Api.DICT` / `Api.TREE_DICT` 字典类接口

### 分层目录约定（每个业务模块）

每个业务功能在所属模块下按以下子包组织（参考 continew-system 的 auth/system 子包）：

| 子包 | 职责 |
| :-- | :-- |
| `controller` | REST API |
| `service` / `service.impl` | 业务接口与实现 |
| `mapper` | MyBatis Plus Mapper |
| `model.entity` / `query` / `req` / `resp` | 实体 / 查询 / 请求 / 响应 |
| `enums` / `constant` / `util` / `config` | 枚举 / 常量 / 工具 / 配置 |
| `container` | Crane4j 数据填充容器配置 |
| `handler` | 处理器 |
| `api` | 对外公共业务 API 实现（配合 common.api 接口） |
| `validation` | 自定义校验注解/工具 |
| `sign` | API 参数签名算法（能力开放模块） |
| `annotation` / `exception` | 注解 / 异常（任务调度模块） |

Mapper XML 统一放在 `src/main/resources/mapper/`，被 `mybatis-plus.mapper-locations: classpath*:/mapper/**/*Mapper.xml` 扫描。

### 多租户（SaaS）

`continew-starter.tenant` 默认开启，隔离级别 `LINE`（行级）。租户 ID 通过请求头 `X-Tenant-Id` 或 `X-Tenant-Code` 传递，默认租户 `0`（超级管理员所在）。**`ignore-tables`** 配置了不参与租户隔离的表（如 `sys_menu`、`sys_dict`、`tenant` 等），新增公共表需在此登记。**`ignore-menus`** 配置租户不可用的菜单 ID。租户相关业务在 `continew-plugin/continew-plugin-tenant`。

### 认证与权限

- **Sa-Token + JWT**（`sa-token.extension.jwt-enabled: true`，DAO 类型 REDIS）：Token 名 `Authorization`，使用 jwt-simple 模式（`is-share` 恒为 false）。Token 有效期等参数**动态配置**，通过前端「系统配置/客户端配置」调整，相关代码注释了静态配置。
- **密码加密**：`continew-starter.encrypt.password-encoder` 默认 BCrypt；前端传入密码经 RSA 公钥加密，后端 `SecureUtils.decryptPasswordByRsaPrivateKey` 解密。RSA 密钥配置于 `continew-starter.encrypt.field`。
- **数据权限**：`DataPermissionMapper` 提供基于角色的数据权限控制。
- 内置角色：超级管理员、租户管理员（系统管理员）；内置用户和角色不允许变更。

### 数据填充（Crane4j）

`xxx/container/` 目录配置 Crane4j 容器，基于注解完成「根据 A 的 key 拿到 B，再把 B 的属性映射到 A」，减少因单字段（如用户名）产生的联表查询。

### 缓存（JetCache 二级缓存）

JetCache 本地一级 Caffeine + 远程二级 Redisson，`broadcastChannel: ${spring.application.name}` 开启多 JVM 本地缓存失效广播。方法级缓存通过 `@Cached` 等注解使用。

### ID 生成（CosID）

`mybatis-plus.extension.id-generator.type: COSID`，雪花算法 + Redis 机器号分配器 + 守护进程。租户编码生成器配置为 `RADIX` 转换器，前缀 `T`。

### 任务调度（Snail Job）

`continew-plugin-schedule` 通过 OpenAPI(SDK API) 对接 Snail Job 服务端。`snail-job.enabled: false` 默认关闭，未启用时调用相关接口会有默认提示。服务端可独立部署（`continew-extension-schedule-server`）。配置在 `application-dev.yml` 的 `snail-job` 节，通过 `SCHEDULE_HOST/PORT/USERNAME/PASSWORD/TOKEN` 等环境变量注入。

### 配置文件结构

`continew-server/src/main/resources/`：
- `config/application.yml`：通用配置（应用信息、Undertow、Spring、CRUD、租户、限流、Sa-Token、MyBatis Plus、CosId、文档、日志、链路追踪）。
- `config/application-dev.yml`：开发环境（数据源 Hikari+P6Spy、Liquibase、Redis/Redisson、JetCache、跨域、加密、验证码、短信、邮件、WebSocket、JustAuth、Sa-Token excludes、Snail Job）。
- `config/application-prod.yml`：生产环境。
- `db/changelog/`：Liquibase 变更（`mysql/`、`postgresql/` 两套 SQL）。
- `templates/`：模板（如 `mail/captcha.ftl`）。
- `logback-spring.xml`：日志配置。

多数据库支持：`mybatis-plus.configuration.database-id` 可在 `mysql` / `pgsql` 间切换，适配 SQL 方言差异。

### 接口文档

SpringDoc + NextDoc4j（替代 Swagger UI）。访问路径：`/swagger-ui`、`/nextdoc/`。文档分组按模块拆分，controller 配置在各自模块。`springdoc.default-flat-param-object: true` 平展对象型参数。

### 全局响应

Graceful Response 统一封装，响应类 `top.continew.starter.web.model.R`。成功码 `0` / 提示「操作成功」，失败码 `1` / 提示「服务器异常，请联系管理员」，失败 HTTP 状态码 `200`。不将原生异常信息填充到响应。

### 链路追踪

`continew-starter-trace`（TLog），trace-id 名 `X-Trace-Id`，pattern `[$spanId][$traceId]`。

### 代码生成器

`continew-plugin-generator` 根据数据库表生成前后端 CRUD 代码，模板位于 `src/main/resources/templates/{backend,frontend}/`。配合前端「系统工具/代码生成」功能使用，支持表结构同步与预览。

## Agent skills

### Issue tracker

Issues live as GitHub issues in `continew-org/continew-admin`; use the `gh` CLI for all operations. See `docs/agents/issue-tracker.md`.

### Triage labels

Five canonical labels (`needs-triage`, `needs-info`, `ready-for-agent`, `ready-for-human`, `wontfix`). See `docs/agents/triage-labels.md`.

### Domain docs

Single-context — one `CONTEXT.md` + `docs/adr/` at the repo root. See `docs/agents/domain.md`.
