# ContiNew Admin 中后台管理框架

<a href="https://github.com/Charles7c/continew-admin/blob/dev/LICENSE" target="_blank">
<img src="https://img.shields.io/badge/License-Apache--2.0-blue.svg" alt="License" />
</a>
<a href="https://github.com/Charles7c/continew-admin/tree/1.0.x" target="_blank">
<img src="https://img.shields.io/badge/SNAPSHOT-v2.0.0-%23ff3f59.svg" alt="Release" />
</a>
<a href="https://github.com/Charles7c/continew-admin" target="_blank">
<img src="https://img.shields.io/github/stars/Charles7c/continew-admin?style=social" alt="GitHub stars" />
</a>
<a href="https://github.com/Charles7c/continew-admin" target="_blank">
<img src="https://img.shields.io/github/forks/Charles7c/continew-admin?style=social" alt="GitHub forks" />
</a>
<a href="https://gitee.com/Charles7c/continew-admin" target="_blank">
<img src="https://gitee.com/Charles7c/continew-admin/badge/star.svg?theme=white" alt="Gitee stars" />
</a>
<a href="https://gitee.com/Charles7c/continew-admin" target="_blank">
<img src="https://gitee.com/Charles7c/continew-admin/badge/fork.svg?theme=white" alt="Gitee forks" />
</a>

📚 <a href="https://doc.charles7c.top" target="_blank">在线文档</a> | ✨ <a href="https://doc.charles7c.top/require" target="_blank">提交需求</a> | 🚀 <a href="https://cnadmin.charles7c.top" target="_blank">演示地址</a>（账号/密码：admin/admin123）

## 简介

ContiNew Admin 中后台管理框架/脚手架，Continue New Admin，持续以最新流行技术栈构建，拥抱变化，迭代优化。当前采用的技术栈：Vue3、TypeScript、Arco Design Vue、Spring Boot、Undertow、Sa-Token、JWT、MariaDB、MyBatis Plus、Redis、Redisson、Easy Excel、Hutool 等。

## 项目源码

| 开源平台      | 源码地址                                    |
| ------------- | ------------------------------------------- |
| GitHub        | https://github.com/Charles7c/continew-admin |
| Gitee（码云） | https://gitee.com/Charles7c/continew-admin  |

## 主要特性

- 精选技术栈，综合考虑成熟度、流行性、发展潜力
- 高效率开发，后端提供了 CRUD 组件，在 Controller 添加一个注解，搞定增、删、改、查、分页、列表、树列表
- 基于注解的通用查询方式，降低拼接 MyBatis Plus QueryWrapper 的烦恼
- 提供一套 Base 基类（BaseDO、BaseVO、BaseController、BaseService、升级版 BaseMapper...，用了你就知道），既方便复用又方便扩展
- 提供一套校验工具实践，另根据实际需要，基于 Hutool 扩展部分工具类（能 PR 到 Hutool 的已尽量 PR）
- 支持 API 级别的功能权限与数据权限，可自定义操作
- 前后端统一异常拦截处理，统一输出异常，避免繁琐的判断
- 良好的代码、配置文件结构和命名，完善的代码注释，遵循阿里巴巴 <a href="https://github.com/Charles7c/continew-admin/blob/dev/code-style/Java%E5%BC%80%E5%8F%91%E6%89%8B%E5%86%8C(%E9%BB%84%E5%B1%B1%E7%89%88).pdf" target="_blank">《Java开发手册(黄山版)》</a> 的代码规范
- 提供多项实用基础配置，例如：Jackson 针对日期、通用枚举的序列化、反序列化配置，统一线程池配置...
- 通过 Liquibase 管理数据库脚本版本记录
- 更多细节及实践，敬请探索与关注

##  系统功能

> v1.x 开发和 v2.x 开发同步进行中。小步快跑，快速迭代。
>
> 详细进度请查看 <a href="https://github.com/Charles7c/continew-admin/projects" target="_blank">GitHub Project</a> 

**v2.0.0：** :fire: 升级并适配 Spring Boot 3.x。

- [x] 依赖升级：升级并适配 Spring Boot 3.x
- [x] 依赖升级：其他依赖升级
- [ ] 计划对接 <a href="https://gitee.com/aizuda/flowlong" target="_blank">FlowLong</a> 纯国产工作流引擎
- [ ] 其他需求汇集中...

**v1.2.0：** 第三方服务支持。

- [ ] 文件管理：提供 OSS 及本地文件管理
- [ ] 支持第三方登录
- [ ] 支持 SMS
- [x] 文档：整理使用文档，创建文档站点
- [ ] 依赖升级：升级至最新 Spring Boot 2.x 版本
- [ ] 依赖升级：其他依赖升级
- [ ] 其他需求汇集中...

**v1.1.0：** 丰富中后台管理框架/脚手架的基础功能。

- [ ] 代码生成：高灵活度生成前后端代码，减少大量重复的工作任务
- [ ] 公告管理：提供系统公告的发布和维护功能
- [ ] 通知管理：提供系统通知管理，设为已读、未读
- [ ] 仪表盘优化：各区块数据动态渲染
- [ ] 网站配置：支持配置系统网站标题、网站 Logo、favicon、版权信息等
- [x] 依赖升级：升级至最新 Spring Boot 2.x 版本
- [x] 依赖升级：其他依赖升级
- [ ] 其他需求汇集中...

**v1.0.0：** 初步完成中后台管理框架/脚手架的基础功能。

- 用户管理：提供用户的相关配置，新增用户后，默认密码为 123456
- 角色管理：对权限与菜单进行分配，可根据部门设置角色的数据权限
- 菜单管理：已实现菜单动态路由，后端可配置化，支持多级菜单
- 部门管理：可配置系统组织架构，树形表格展示
- 在线用户：管理当前登录用户，可一键踢下线
- 日志管理：提供在线用户监控、登录日志监控、操作日志监控和系统日志监控等监控功能

## 项目结构

### 后端

采用按功能拆分模块的开发方式，项目目录结构如下：

> 下方项目目录结构是按照模块的层次顺序进行介绍的，实际 IDE 中 `continew-admin-common` 模块会因为字母排序原因排在上方。

```bash
continew-admin  # 全局通用项目配置及依赖版本管理
  ├─ continew-admin-webapi   # API 模块（存放 Controller 层代码，打包部署的模块）
  │  └─ src
  │    └─ main
  │      ├─ java        # 工程源文件代码目录
  │      │  └─ top
  │      │    └─ charles7c
  │      │      └─ cnadmin
  │      │        ├─ webapi
  │      │        │  └─ controller  
  │      │        │    ├─ auth     # 认证相关 API
  │      │        │    ├─ common   # 公共相关 API（例如：验证码 API 等）
  │      │        │    ├─ monitor  # 系统监控相关 API
  │      │        │    └─ system   # 系统管理相关 API
  │      │        └─ ContinewAdminApplication.java  # 启动入口
  │      └─ resources   # 工程配置目录
  │        ├─ db.changelog   # 数据库脚本文件
  │        │  ├─ v1.0.0        # v1.0.0 版本数据库脚本文件
  │        │  └─ v1.1.0        # v1.1.0 版本数据库脚本文件
  │        └─ templates      # 模板文件
  │          └─ mail           # 邮件模板
  ├─ continew-admin-monitor  # 系统监控模块（存放系统监控模块相关功能，例如：日志管理、服务监控等）
  │  └─ src
  │    └─ main
  │      ├─ java        # 工程源文件代码目录
  │      │  └─ top
  │      │    └─ charles7c
  │      │      └─ cnadmin
  │      │        └─ monitor
  │      │          ├─ annotation    # 系统监控相关注解
  │      │          ├─ config        # 系统监控相关配置
  │      │          │  └─ properties   # 系统监控相关配置属性
  │      │          ├─ enums         # 系统监控相关枚举
  │      │          ├─ filter        # 系统监控相关过滤器
  │      │          ├─ interceptor   # 系统监控相关拦截器
  │      │          ├─ mapper        # 系统监控相关 Mapper
  │      │          ├─ model         # 系统监控相关模型
  │      │          │  ├─ entity       # 系统监控相关实体对象
  │      │          │  ├─ query        # 系统监控相关查询条件
  │      │          │  └─ vo           # 系统监控相关 VO（View Object）
  │      │          └─ service       # 系统监控相关业务接口及实现类
  │      │             └─ impl         # 系统监控相关业务实现类
  │      └─ resources   # 工程配置目录
  │         └─ mapper        # MyBatis Mapper XML 文件目录
  ├─ continew-admin-system   # 系统管理模块（存放系统管理模块相关功能，例如：部门管理、角色管理、用户管理等）
  │  └─ src
  │    └─ main
  │      ├─ java        # 工程源文件代码目录
  │      │  └─ top
  │      │    └─ charles7c
  │      │      └─ cnadmin
  │      │        ├─ auth     # 系统认证相关业务及配置
  │      │        │  ├─ config    # 系统认证相关配置
  │      │        │  │  └─ satoken    # Sa-Token 配置
  │      │        │  ├─ model     # 系统认证相关模型
  │      │        │  │  ├─ request    # 系统认证相关请求对象
  │      │        │  │  └─ vo         # 系统认证相关 VO（View Object）
  │      │        │  └─ service   # 系统认证相关业务接口及实现类
  │      │        │     └─ impl       # 系统认证相关业务实现类
  │      │        └─ system   # 系统管理相关业务及配置
  │      │          ├─ mapper     # 系统管理相关 Mapper
  │      │          ├─ model      # 系统管理相关模型
  │      │          │  ├─ entity      # 系统管理相关实体对象
  │      │          │  ├─ query       # 系统管理相关查询条件
  │      │          │  ├─ request     # 系统管理相关请求对象
  │      │          │  └─ vo          # 系统管理相关 VO（View Object）
  │      │          └─ service    # 系统管理相关业务接口及实现类
  │      │             └─ impl        # 系统管理相关业务实现类
  │      └─ resources   # 工程配置目录
  │         └─ mapper        # MyBatis Mapper XML 文件目录
  ├─ continew-admin-common   # 公共模块（存放公共工具类，公共配置等）
  │  └─ src
  │    └─ main
  │      └─ java        # 工程源文件代码目录
  │        └─ top
  │          └─ charles7c
  │            └─ cnadmin
  │              └─ common
  │                ├─ annotation   # 公共注解
  │                ├─ base         # 公共基类
  │                ├─ config       # 公共配置
  │                │  ├─ easyexcel    # Easy Excel 配置
  │                │  ├─ jackson      # Jackson 配置
  │                │  ├─ mybatis      # MyBatis Plus 配置
  │                │  ├─ threadpool   # 线程池配置
  │                │  └─ properties   # 公共配置属性
  │                ├─ constant     # 公共常量
  │                ├─ enums        # 公共枚举
  │                ├─ exception    # 公共异常
  │                ├─ handler      # 公共处理器
  │                ├─ model        # 公共模型
  │                │  ├─ dto          # 公共 DTO（Data Transfer Object）
  │                │  ├─ query        # 公共查询条件
  │                │  ├─ request      # 公共请求对象
  │                │  └─ vo           # 公共 VO（View Object）
  │                ├─ service      # 公共业务接口
  │                └─ util         # 公共工具类
  │                  ├─ helper        # 公共 Helper（助手）
  │                  ├─ holder        # 公共 Holder（持有者）
  │                  └─ validate      # 公共校验器（参数校验，业务校验）
```

### 前端

```bash
continew-admin
  └─ continew-admin-ui      # 前端项目
    ├─ config               # 全局 Vite 配置
    ├─ public               # 公共静态资源（favicon.ico、logo.svg）
    ├─ src
    │  ├─ api               # 请求接口
    │  │  ├─ auth             # 认证模块
    │  │  ├─ common           # 公共模块
    │  │  ├─ monitor          # 系统监控模块
    │  │  └─ system           # 系统管理模块
    │  ├─ assets            # 静态资源
    │  │  ├─ icons            # 图标资源
    │  │  ├─ images           # 图片资源
    │  │  └─ style            # 样式资源
    │  ├─ components        # 通用业务组件
    │  ├─ config            # 全局配置（包含 echarts 主题）
    │  │  └─ settings.json    # 配置文件
    │  ├─ directives        # 指令集（如需，可自行补充）
    │  ├─ hooks             # 全局 hooks
    │  ├─ layout            # 布局
    │  ├─ locale            # 国际化语言包
    │  ├─ mock              # 模拟数据
    │  ├─ router            # 路由配置
    │  ├─ store             # 状态管理中心
    │  ├─ types             # TypeScript 类型
    │  ├─ utils             # 工具库
    │  ├─ views             # 页面模板
    │  │  ├─ login            # 登录模块
    │  │  ├─ monitor          # 系统监控模块
    │  │  │  ├─ log              # 日志管理
    │  │  │  │  ├─ login            # 登录日志
    │  │  │  │  ├─ operation        # 操作日志
    │  │  │  │  └─ system           # 系统日志
    │  │  │  └─ online           # 在线用户
    │  │  └─ system           # 系统管理模块
    │  │    ├─ dept             # 部门管理
    │  │    ├─ menu             # 菜单管理
    │  │    ├─ role             # 角色管理
    │  │    └─ user             # 用户模块
    │  │      └─ center           # 个人中心
    │  ├─ App.vue           # 视图入口
    │  └─ main.ts           # 入口文件
    ├─ .env.development
    ├─ .env.production
    ├─ index.html
    ├─ package.json
    └─ tsconfig.json
```

## 技术栈

| 名称                                                         | 版本         | 简介                                                         |
| :----------------------------------------------------------- | :----------- | :----------------------------------------------------------- |
| <a href="https://cn.vuejs.org/" target="_blank">Vue</a>      | 3.2.47       | 渐进式 JavaScript 框架，易学易用，性能出色，适用场景丰富的 Web 前端框架。 |
| <a href="https://www.typescriptlang.org/zh/" target="_blank">TypeScript</a> | 4.9.5        | TypeScript 是微软开发的一个开源的编程语言，通过在 JavaScript 的基础上添加静态类型定义构建而成。 |
| <a href="https://arco.design/vue/docs/start" target="_blank">Arco Design Vue</a> | 2.44.5       | 字节跳动推出的前端 UI 框架，样式美观，组件丰富。             |
| <a href="https://spring.io/projects/spring-boot" target="_blank">Spring Boot</a> | 2.7.10       | 简化新 Spring 应用的初始搭建以及开发过程。                   |
| <a href="https://undertow.io/" target="_blank">Undertow</a>  | 2.2.23.Final | 采用 Java 开发的灵活的高性能 Web 服务器，提供包括阻塞和基于 NIO 的非堵塞机制。 |
| <a href="https://sa-token.dev33.cn/" target="_blank">Sa-Token + JWT</a> | 1.34.0       | 轻量级 Java 权限认证框架，让鉴权变得简单、优雅。             |
| <a href="https://mariadb.org/" target="_blank">MariaDB</a>   | 10.10.2      | MySQL 的一个分支，主要由开源社区在维护，完全兼容 MySQL，包括 API 和命令行，能轻松成为 MySQL 的代替品。 |
| <a href="https://baomidou.com/" target="_blank">MyBatis Plus</a> | 3.5.3.1      | MyBatis 的增强工具，在 MyBatis 的基础上只做增强不做改变，简化开发、提高效率。 |
| <a href="https://www.kancloud.cn/tracy5546/dynamic-datasource/2264611" target="_blank">dynamic-datasource-spring-boot-starter</a> | 3.6.1        | 基于 Spring Boot 的快速集成多数据源的启动器。                |
| Hikari                                                       | 4.0.3        | JDBC 连接池，号称 “史上最快连接池”，SpringBoot 在 2.0 之后，采用的默认数据库连接池就是 Hikari。 |
| <a href="https://dev.mysql.com/doc/connector-j/8.0/en/" target="_blank">mysql-connector-j</a> | 8.0.32       | MySQL Java 驱动。                                            |
| <a href="https://github.com/p6spy/p6spy" target="_blank">P6Spy</a> | 3.9.1        | SQL 性能分析组件。                                           |
| <a href="https://github.com/liquibase/liquibase" target="_blank">Liquibase</a> | 4.9.1        | 用于管理数据库版本，跟踪、管理和应用数据库变化。             |
| <a href="https://redis.io/" target="_blank">Redis</a>        | 6.2.7        | 高性能的 key-value 数据库。                                  |
| <a href="https://github.com/redisson/redisson/wiki/Redisson%E9%A1%B9%E7%9B%AE%E4%BB%8B%E7%BB%8D" target="_blank">Redisson</a> | 3.20.0       | 不仅仅是一个 Redis Java 客户端，同其他 Redis Java 客户端有着很大的区别，相比之下其他客户端提供的功能还仅仅停留在作为数据库驱动层面上，比如仅针对 Redis 提供连接方式，发送命令和处理返回结果等。而 Redisson 充分的利用了 Redis 键值数据库提供的一系列优势，基于 Java 实用工具包中常用接口，为使用者提供了一系列具有分布式特性的常用工具类。使得原本作为协调单机多线程并发程序的工具包获得了协调分布式多机多线程并发系统的能力，大大降低了设计和研发大规模分布式系统的难度。同时结合各富特色的分布式服务，更进一步简化了分布式环境中程序相互之间的协作。 |
| <a href="https://easyexcel.opensource.alibaba.com/" target="_blank">Easy Excel</a> | 3.2.1        | 一个基于 Java 的、快速、简洁、解决大文件内存溢出的 Excel 处理工具。 |
| Easy Captcha                                                 | 1.6.2        | Java 图形验证码，支持 gif、中文、算术等类型，可用于 Java Web、JavaSE 等项目。 |
| <a href="https://doc.xiaominfo.com/" target="_blank">Knife4j</a> | 4.1.0        | 前身是 swagger-bootstrap-ui，集 Swagger2 和 OpenAPI3 为一体的增强解决方案。本项目使用的是 <a href="https://gitee.com/xiaoym/swagger-bootstrap-ui-demo/tree/master/knife4j-springdoc-openapi-demo" target="_blank">knife4j-openapi3-spring-boot-starter</a> 基于 OpenAPI3 规范，在 Spring Boot < 3.0.0-M1 的单体架构下可以直接引用此 starter，该模块包含了 UI 部分，底层基于 springdoc-openapi 项目。 |
| <a href="https://www.hutool.cn/" target="_blank">Hutool</a>  | 5.8.16       | 小而全的 Java 工具类库，通过静态方法封装，降低相关 API 的学习成本，提高工作效率，使 Java 拥有函数式语言般的优雅，让 Java 语言也可以“甜甜的”。 |
| <a href="https://projectlombok.org/" target="_blank">Lombok</a> | 1.18.26      | 在 Java 开发过程中用注解的方式，简化了 JavaBean 的编写，避免了冗余和样板式代码，让编写的类更加简洁。 |

## 部分系统截图

<table border="1" cellpadding="1" cellspacing="1" style="width: 500px">
    <tbody>
        <tr>
            <td><img src="https://s1.ax1x.com/2023/02/20/pSOwWQJ.png" alt="登录页面" width="1920" /></td>
            <td><img src="https://s1.ax1x.com/2023/03/09/ppn4wVA.png" alt="仪表盘" width="1920" /></td>
        </tr>
        <tr>
            <td><img src="https://user-images.githubusercontent.com/25446948/224081765-8cf61216-d30a-402d-8972-24eb714687bd.png" alt="数据可视化-分析页" width="1920" /></td>
            <td><img src="https://user-images.githubusercontent.com/25446948/224081884-d0da486d-5e39-4f24-8e6f-3bd806c09b45.png" alt="数据可视化-实时监控" width="1920" /></td>
        </tr>
        <tr>
            <td><img src="https://s1.ax1x.com/2023/02/20/pSOw7FK.png" alt="个人中心" width="1920" /></td>
            <td><img src="https://s1.ax1x.com/2023/02/20/pSOwHJO.png" alt="个人中心-安全设置" width="1920" /></td>
        </tr>
        <tr>
            <td><img src="https://s1.ax1x.com/2023/02/20/pSOwLSe.png" alt="个人中心-安全设置-修改密码" width="1920" /></td>
            <td><img src="https://s1.ax1x.com/2023/02/20/pSOwXyd.png" alt="个人中心-安全设置-修改邮箱" width="1920" /></td>
        </tr>
        <tr>
            <td><img src="https://s1.ax1x.com/2023/02/20/pSOwbWD.png" alt="个人中心-修改邮箱-发送验证码" width="1920" /></td>
            <td><img src="https://s1.ax1x.com/2023/01/14/pSM3lxx.png" alt="个人中心-修改邮箱-邮箱验证码" width="1920" /></td>
        </tr>
        <tr>
            <td><img src="https://s1.ax1x.com/2023/02/23/pSvhOEV.png" alt="个人中心-操作日志" width="1920" /></td>
            <td><img src="https://s1.ax1x.com/2023/03/09/ppn5ise.png" alt="系统监控-操作日志" width="1920" /></td>
        </tr>
        <tr>
            <td><img src="https://s1.ax1x.com/2023/03/09/ppn4bMF.png" alt="系统监控-在线用户" width="1920" /></td>
            <td><img src="https://s1.ax1x.com/2023/03/09/ppn5SG6.png" alt="系统监控-登录日志" width="1920" /></td>
        </tr>
        <tr>
            <td><img src="https://s1.ax1x.com/2023/03/09/ppn5eit.png" alt="系统监控-系统日志" width="1920" /></td>
            <td><img src="https://s1.ax1x.com/2023/03/09/ppn5VII.png" alt="系统监控-系统日志-详情" width="1920" /></td>
        </tr>
        <tr>
            <td><img src="https://s1.ax1x.com/2023/03/20/ppNru8A.png" alt="系统管理-部门管理" width="1920" /></td>
            <td><img src="https://s1.ax1x.com/2023/03/09/ppn5Qsg.png" alt="系统管理-菜单管理" width="1920" /></td>
        </tr>
        <tr>
            <td><img src="https://s1.ax1x.com/2023/03/09/ppn5lLQ.png" alt="系统管理-菜单管理-新增" width="1920" /></td>
            <td><img src="https://s1.ax1x.com/2023/03/20/ppNrMvt.png" alt="系统管理-角色管理" width="1920" /></td>
        </tr>
        <tr>
            <td><img src="https://s1.ax1x.com/2023/03/20/ppNr0K0.png" alt="系统管理-角色管理-新增" width="1920" /></td>
            <td><img src="https://s1.ax1x.com/2023/03/20/ppNrlKP.png" alt="系统管理-角色管理-详情" width="1920" /></td>
        </tr>
        <tr>
            <td><img src="https://s1.ax1x.com/2023/03/20/ppNrGVS.png" alt="系统管理-用户管理" width="1920" /></td>
            <td><img src="https://s1.ax1x.com/2023/03/20/ppNrJUg.png" alt="系统管理-用户管理-新增" width="1920" /></td>
        </tr>
    </tbody>
</table>

## 快速开始

> 注意：下方步骤有重叠部分，无需重复执行。

### 后端

```bash
# 1.克隆本项目
git clone https://github.com/Charles7c/continew-admin.git

# 2.在 IDE（IntelliJ IDEA/Eclipse）中打开本项目

# 3.修改配置文件中的数据源配置信息、Redis 配置信息、邮件配置信息等
# [3.也可以在 IntelliJ IDEA 中直接配置程序启动环境变量（DB_HOST、DB_PORT、DB_USER、DB_PWD、DB_NAME；REDIS_HOST、REDIS_PORT、REDIS_PWD、REDIS_DB）]

# 4.启动程序
# 4.1 启动成功：访问 http://localhost:8000/，页面输出：ContiNew Admin backend service started successfully.
# 4.2 接口文档：http://localhost:8000/doc.html

# 5.部署
# 5.1 Docker 部署
#   5.1.1 服务器安装好 docker 及 docker-compose（参考：https://blog.charles7c.top/categories/fragments/2022/10/31/CentOS%E5%AE%89%E8%A3%85Docker）
#   5.1.2 执行 mvn package -P prod 进行项目打包，将 target 目录下的 continew-admin.jar 放到 /docker/continew-admin/server 目录下
#   5.1.3 将 docker 目录上传到服务器 / 目录下，并授权（chmod -R 777 /docker）
#   5.1.4 修改 docker-compose.yml 中的 MariaDB 配置、Redis 配置、continew-admin-server 配置、Nginx 配置
#   5.1.5 执行 docker-compose up -d 创建并后台运行所有容器
# 5.2 其他方式部署
```

### 前端

```bash
# 1.克隆本项目
git clone https://github.com/Charles7c/continew-admin.git

# 2.在 IDE（Visual Studio Code/WebStorm）中打开前端项目 continew-admin-ui

# 3.配置淘宝源
pnpm config set registry https://registry.npm.taobao.org

# 4.安装依赖
pnpm i

# 5.启动程序
# 5.1 启动成功：访问 http://localhost:5173/
pnpm dev

# 6.部署
# 6.1 Docker 部署
#   6.1.1 服务器安装好 docker 及 docker-compose（参考：https://blog.charles7c.top/categories/fragments/2022/10/31/CentOS%E5%AE%89%E8%A3%85Docker）
#   6.1.2 执行 pnpm build 进行项目打包，将 dist 目录下的所有文件放到 /docker/continew-admin/web 目录下
#   6.1.3 将 docker 目录上传到服务器 / 目录下，并授权（chmod -R 777 /docker）
#   6.1.4 修改 docker-compose.yml 中的 MariaDB 配置、Redis 配置、continew-admin-server 配置、Nginx 配置
#   6.1.5 执行 docker-compose up -d 创建并后台运行所有容器
# 6.2 其他方式部署
```

## 反馈交流

💬 非常欢迎各位小伙伴儿在 Issues、Discussions 中进行交流探讨~

💬 也欢迎各位小伙伴儿扫码加作者好友（请备注：cnadmin），作者拉你进群，现有作者（群主）及 7 位群友，随意聊聊技术、提提需求，吐吐槽~

<div align="left">
  <img src="https://s1.ax1x.com/2023/03/09/ppnhe0A.jpg" alt="二维码" width="200" />
</div>

## 特别鸣谢

- 感谢 <a href="https://www.jetbrains.com/" target="_blank">JetBrains</a> 提供的 <a href="https://www.jetbrains.com/shop/eform/opensource" target="_blank">非商业开源软件开发授权</a> 
- 感谢 <a href="http://pro.arco.design/" target="_blank">Arco Design Pro</a> 开箱即用的中后台前端解决方案
- 感谢 <a href="https://github.com/baomidou/mybatis-plus" target="_blank">MyBatis Plus</a>、<a href="https://github.com/dromara/sa-token" target="_blank">Sa-Token</a> 、<a href="https://github.com/alibaba/easyexcel" target="_blank">Easy Excel</a>、<a href="https://github.com/xiaoymin/knife4j" target="_blank">Knife4j</a>、<a href="https://github.com/dromara/hutool" target="_blank">Hutool</a> 等国产开源组件作者为国内开源世界作出的贡献
- 感谢 <a href="https://github.com/elunez/eladmin" target="_blank">ELADMIN</a>、<a href="https://github.com/dromara/RuoYi-Vue-Plus" target="_blank">RuoYi-Vue-Plus</a>、<a href="https://github.com/yangzongzhuan/RuoYi-Vue3" target="_blank">RuoYi-Vue3</a> 提供的诸多成熟方案，致敬各位作者为开源世界作出的贡献

## License

- 遵循 <a href="https://github.com/Charles7c/continew-admin/blob/dev/LICENSE" target="_blank">Apache-2.0</a> 开源许可协议
- Copyright © 2022-present <a href="https://blog.charles7c.top" target="_blank">Charles7c</a>