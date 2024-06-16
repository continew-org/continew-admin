# ContiNew Admin 中后台管理框架

<a href="https://github.com/Charles7c/continew-admin/blob/dev/LICENSE" target="_blank">
<img src="https://img.shields.io/badge/License-Apache--2.0-blue.svg" alt="License" />
</a>
<a href="https://github.com/Charles7c/continew-admin" target="_blank">
<img src="https://img.shields.io/badge/RELEASE-v3.1.0-%23ff3f59.svg" alt="Release" />
</a>
<a href="https://app.codacy.com/gh/Charles7c/continew-admin/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade" target="_blank">
<img src="https://app.codacy.com/project/badge/Grade/19e3e2395d554efe902c3822e65db30e" alt="Codacy Badge" />
</a>
<a href="https://sonarcloud.io/summary/new_code?id=Charles7c_continew-admin" target="_blank">
<img src="https://sonarcloud.io/api/project_badges/measure?project=Charles7c_continew-admin&metric=alert_status" alt="Sonar Status" />
</a>
<a href="https://github.com/Charles7c/continew-starter" target="_blank">
<img src="https://img.shields.io/badge/ContiNew Starter-2.1.0-%236CB52D.svg" alt="ContiNew Starter" />
</a>
<a href="https://spring.io/projects/spring-boot" target="_blank">
<img src="https://img.shields.io/badge/Spring Boot-3.1.11-%236CB52D.svg?logo=Spring-Boot" alt="Spring Boot" />
</a>
<a href="https://github.com/Charles7c/continew-starter" target="_blank">
<img src="https://img.shields.io/badge/Open JDK-17-%236CB52D.svg?logo=OpenJDK&logoColor=FFF" alt="Open JDK" />
</a>
<a href="https://github.com/Charles7c/continew-admin" target="_blank">
<img src="https://img.shields.io/github/stars/Charles7c/continew-admin?style=social" alt="GitHub stars" />
</a>
<a href="https://github.com/Charles7c/continew-admin" target="_blank">
<img src="https://img.shields.io/github/forks/Charles7c/continew-admin?style=social" alt="GitHub forks" />
</a>
<a href="https://gitee.com/continew/continew-admin" target="_blank">
<img src="https://gitee.com/continew/continew-admin/badge/star.svg?theme=white" alt="Gitee stars" />
</a>
<a href="https://gitee.com/continew/continew-admin" target="_blank">
<img src="https://gitee.com/continew/continew-admin/badge/fork.svg?theme=white" alt="Gitee forks" />
</a>

📚 [在线文档](https://continew.top) | 🚀 [演示地址](https://admin.continew.top)（账号/密码：admin/admin123）

## 简介

ContiNew Admin（Continue New Admin）持续迭代优化的前后端分离中后台管理系统框架。开箱即用，重视每一处代码规范，重视每一种解决方案细节，持续提供舒适的前、后端开发体验。

当前采用的技术栈：Spring Boot3（Java17）、Vue3 & Arco Design & TS & Vite、Sa-Token、MyBatis Plus、Redisson、JetCache、JustAuth、Crane4j、EasyExcel、Liquibase、Hutool 等。

## 项目源码

|          | Gitee（码云）                                                                            | GitHub                                                                          |
|----------|--------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------|
| 后端       | [continew/continew-admin](https://gitee.com/continew/continew-admin)                 | [charles7c/continew-admin](https://github.com/Charles7c/continew-admin)                 |
| 前端       | [continew/continew-admin-ui](https://gitee.com/continew/continew-admin-ui)           | [charles7c/continew-admin-ui](https://github.com/Charles7c/continew-admin-ui)           |
| 前端（v2.5） | [continew/continew-admin-ui-arco](https://gitee.com/continew/continew-admin-ui-arco) | [charles7c/continew-admin-ui-arco](https://github.com/Charles7c/continew-admin-ui-arco) |

## 项目起源

我热衷于做数据归档，归档后的数据可以提高学习/工作效率，为记忆“减负”，在持续的数据归档中，优质的“沉淀”会带来非匀速、跨越式的学习/工作体验。**数据归档是一件需要持续去做的事情**。

从接触程序代码的第一天，我的程序数据归档也随之开始了，刷过的算法题、笔记、对接各种组件的配置文件，甚至于一些亮眼的样式设计、“如诗”的代码片段。这些数据的沉淀丰富了我的解决方案，提高了我的编程效率，逐渐为各种场景落实成了一个个雏形程序。再后来，我意识到，我归档的这些雏形程序，有一个更为妥贴的名称：**程序框架/脚手架**。

技术的发展，导致这些雏形程序的生命周期很是短暂，它们有别于我归档的其他数据，有时由于工作的原因，没有时间很好的去沉淀它们，在使用时变得越来越不顺手。所以，某段时间，我放弃了维护，而是去采用一些更为成熟的框架。

不过，在陆续几年使用了一些成熟框架后，我前后遇到了一些困难：

1. 代码洁癖想要找到一个**扩展性佳，代码规范良好，开发体验舒适**的框架很不容易，总是差些什么
2. 项目上手困难或是基础版功能不全，需要的全在专业版，亦或者代码阅读性差，文档收费
3. 部分解决方案缺失，已有解决方案也过于偏向样板化，无法形成良好的逻辑闭环
4. 好不容易找到一些相较合适的，没过多久，部分作者可能暂时没法对外发“电”了，随着了解深入，很多 Bug 或新技术趋势还是需要自己研究解决

在工作中，很多想法/设计受限于客户需求、开发工期，必须优先以交付为导向，但一些优秀的实践需要花时间持续进行沉淀，只要我没跳出这个圈子，我还是需要一直去做好程序归档。“种一棵树最好的时间是十年前，其次是现在”，最终，我选择在业余时间更加正视这件事，从头归档沉淀，从添加每一个依赖开始，我希望它能持续的迭代优化、演进，所以我把它命名为 **ContiNew（Continue New）**。并且这次我选择了开源，我希望它不仅仅能吸收我的需求和沉淀，而是依托开源协作模式，及时发现更多的问题，接受更多的可能性，沉淀更优秀的思考，设计。

## 为什么选我们？

> [!TIP]
> 更为完整的图文描述请查阅[《在线文档》](https://continew.top/admin/intro/why.html)。

1.**甄选技术栈：** ContiNew（Continue New） 项目致力于持续迭代优化，让技术不掉队。在技术选型时，进行深度广泛地调研，从流行度、成熟度和发展潜力等多方面甄选技术栈。

2.**Starter 组件：** 从 v2.1.0 版本开始，抽取并封装后端基础组件及各框架集成配置到 ContiNew Starter 项目，且 **[已发布至 Maven 中央仓库](https://central.sonatype.com/search?q=continew-starter&namespace=top.continew)**，可在你的任意项目中直接引入所需依赖使用。即使你不用脚手架项目，难道能让你搭项目框架更快、更爽、更省力的 Starter 也要 Say No 吗？

3.**CRUD 套件：** 封装通用增删改查套件，适配后端各分层，几分钟即可提供一套 CRUD API，包括新增、修改、批量删除、查询详情、分页列表查询、全部列表查询、树型列表查询、导出到 Excel，且 API 支持按实际所需开放或扩展。
```java
@Tag(name = "部门管理 API")
@RestController
@CrudRequestMapping(value = "/system/dept", api = {Api.TREE, Api.GET, Api.ADD, Api.UPDATE, Api.DELETE, Api.EXPORT})
public class DeptController extends BaseController<DeptService, DeptResp, DeptDetailResp, DeptQuery, DeptReq> {}
```

4.**代码生成器：** 提供代码生成器，已配套前、后端代码生成模板，数据表设计完之后，简单配置一下即可生成前、后端 80% 的代码，包含 CRUD API、权限控制、参数校验、接口文档等内容。如果业务不复杂，也可能就是 95% 的代码。

5.**改善开发体验：** 持续优化及适配能改善开发体验的组件。
- 适配 ContiNew Starter 组件，针对多数框架进行了深度封装的 starter，改善你在开发每个 Spring Boot Web 项目的体验。
- 适配 Crane4j 数据填充组件，减少因为一个用户名而产生的联表回填；
- 适配 P6Spy SQL 性能分析组件，开发期间方便监控 SQL 执行；
- 适配 TLog 链路追踪组件，方便在杂乱的日志文件中追踪你某次请求的日志记录；
- 适配 JetCache 缓存框架（比 Spring Cache 更强大易用），通过注解声明即可快速实现方法级缓存，极大改善编码式缓存体验，且支持灵活的二级缓存配置、分布式自动刷新等能力；
- 前端适配 Vue Devtools（Vue 官方提供的调试浏览器插件），极大提高 Vue 开发及调试效率

6.**Almost最佳后端规范：** 后端严格遵循阿里巴巴 Java 编码规范，注释覆盖率 > 45%，接口参数示例 100%，代码分层使用体验佳，变量、方法命名清晰统一，前端代码也使用严格的 ESLint、StyleLint 等检查。良好的设计，代码复用率极高！写代码时，让你有一种无需多写，理应如此的感觉。我是代码洁癖，我实际写的时候很清楚这到底是不是乱吹。

7.**卓越工程：** 后端采用模块化工程结构，并适配了统一项目版本号、编译项目自动代码格式化、代码混淆等插件，提供了自定义打包部署结构配置（配置文件、三方依赖和主程序分离），提供全套环境及应用的 Docker Compose 部署脚本。为了减少您开发新项目时的改造耗时，项目品牌配置持续进行深度聚合，简单的配置和结构修改即可快速开始独属于你的新项目。

8.**业务脚手架：** 有颜有料，不止是说说而已，持续打磨 UI 设计与色彩主题。提供基于 RBAC 的权限控制、通用数据权限，包含丰富的通用业务功能：第三方登录，邮箱、短信（生产级炸弹漏洞处理方案），个人中心、用户管理、角色管理、部门管理、系统配置（基础站点配置、邮件配置、安全配置）、系统日志、消息中心、通知公告等，设计用心，逻辑合理闭环。
> 一个好的脚手架项目，不仅仅是提供一系列组件集成与配置，也不仅仅是封装一堆好用的工具，还更应该提供一系列通用基础业务解决方案及设计，为初创团队项目减负。

9.**质量与安全：** CI 已集成 Sonar、Codacy，Push 即扫描代码质量，定期扫描 CVE 漏洞，及时解决潜在问题。封装数据库字段加密、JSON 脱敏、XSS 过滤等工具，提供诸多安全解决方案。

由于篇幅有限，且项目正处于高速发展期，更多功能正在陆续上线（敬请关注仓库或群内动态）。另外像最基本的统一异常、错误处理，基础线程池等配置就不在此赘述，细节优化详情请 clone 代码查看。
> Talk is cheap, show the code.

##  系统功能

> [!TIP]
> 更多功能和优化正在赶来💦，最新项目计划、进展请进群或关注 [任务清单](https://continew.top/admin/intro/require.html#任务清单) 和 [更新日志](https://continew.top/admin/other/changelog.html)。

- 个人中心：支持基础信息修改、密码修改、邮箱绑定、手机号绑定（并提供行为验证码、短信限流等安全处理）、第三方账号绑定/解绑、头像裁剪上传
- 消息中心：提供站内信消息统一查看、标记已读、全部已读、删除等功能
- 用户管理：提供用户的相关配置，新增、修改、删除、重置密码、导出
- 部门管理：可配置系统组织架构，并以树形表格展示
- 角色管理：对权限与菜单进行分配，可根据部门设置角色的数据权限
- 菜单管理：已实现菜单动态路由，后端可配置化，支持多级菜单
- 通知公告：提供公告的发布、查看和删除等功能。管理员可以在后台发布公告，并可以设置公告的生效时间、终止时间，以 markdown-it 为内核渲染 Markdown 格式内容显示
- 字典管理：提供对系统公用数据字典的维护，例如：公告类型，支持字典标签背景色和排序等配置
- 文件管理：提供文件上传、下载、预览（目前支持图片、音视频）、重命名、切换视图（列表、网格）等功能
- 存储管理：提供文件存储库新增、编辑、删除等功能，支持本地存储、兼容 S3 协议存储
- 系统配置：
  - 基础配置：提供修改系统标题、Logo、favicon、版权信息等基础配置功能，以方便用户系统与其自身品牌形象保持一致
  - 邮件配置：提供系统发件箱配置，也支持通过配置文件指定
  - 安全配置：提供密码策略修改，支持丰富的密码策略设定，包括但不限于 `密码有效期`、`密码重复次数`、`密码错误锁定账号次数、时间` 等
- 代码生成：提供根据数据库表自动生成相应的前后端 CRUD 代码的功能，支持同步最新表结构及代码生成预览
- 在线用户：管理当前登录用户，可一键踢下线
- 日志管理：提供登录日志、操作日志管理功能，可查看指定日志的详细请求及响应信息

## 系统截图

> [!TIP]
> 受篇幅长度及功能更新频率影响，下方仅为系统 **部分** 功能于 **2024年6月13日** 进行的截图，更多新增功能及细节请登录演示环境或 clone 代码到本地启动查看。

<table border="1" cellpadding="1" cellspacing="1" style="width: 500px">
    <tbody>
        <tr>
            <td><img src=".image/screenshot/000登录页面.png" alt="登录页面" width="1920" /></td>
            <td><img src=".image/screenshot/001仪表盘.png" alt="仪表盘" width="1920" /></td>
        </tr>
        <tr>
            <td><img src=".image/screenshot/002仪表盘-查看公告.png" alt="仪表盘-查看公告" width="1920" /></td>
            <td><img src=".image/screenshot/010个人中心.png" alt="个人中心" width="1920" /></td>
        </tr>
        <tr>
            <td><img src=".image/screenshot/011消息中心.png" alt="消息中心" width="1920" /></td>
            <td><img src=".image/screenshot/012安全设置-修改邮箱-邮箱验证码.png" alt="安全设置-修改邮箱-邮箱验证码" width="1920" /></td>
        </tr>
        <tr>
            <td><img src=".image/screenshot/060系统管理-系统配置.png" alt="系统管理-系统配置" width="1920" /></td>
            <td><img src=".image/screenshot/061系统管理-安全配置.png" alt="系统管理-安全配置" width="1920" /></td>
        </tr>
        <tr>
            <td><img src=".image/screenshot/020系统管理-用户管理-列表.png" alt="系统管理-用户管理-列表" width="1920" /></td>
            <td><img src=".image/screenshot/021系统管理-用户管理-新增.png" alt="系统管理-用户管理-新增" width="1920" /></td>
        </tr>
        <tr>
            <td><img src=".image/screenshot/025系统管理-部门管理-列表.png" alt="系统管理-部门管理-列表" width="1920" /></td>
            <td><img src=".image/screenshot/026系统管理-部门管理-新增.png" alt="系统管理-部门管理-新增" width="1920" /></td>
        </tr>
        <tr>
            <td><img src=".image/screenshot/030系统管理-角色管理-列表.png" alt="系统管理-角色管理-列表" width="1920" /></td>
            <td><img src=".image/screenshot/031系统管理-角色管理-新增.png" alt="系统管理-角色管理-新增" width="1920" /></td>
        </tr>
        <tr>
            <td><img src=".image/screenshot/035系统管理-菜单管理-列表.png" alt="系统管理-菜单管理-列表" width="1920" /></td>
            <td><img src=".image/screenshot/036系统管理-菜单管理-新增.png" alt="系统管理-菜单管理-新增" width="1920" /></td>
        </tr>
        <tr>
            <td><img src=".image/screenshot/040系统管理-公告管理-列表.png" alt="系统管理-公告管理-列表" width="1920" /></td>
            <td><img src=".image/screenshot/041系统管理-公告管理-修改.png" alt="系统管理-公告管理-修改" width="1920" /></td>
        </tr>
        <tr>
            <td><img src=".image/screenshot/045系统管理-字典管理-列表.png" alt="系统管理-字典管理-列表" width="1920" /></td>
            <td><img src=".image/screenshot/046系统管理-字典项管理.png" alt="系统管理-字典项管理" width="1920" /></td>
        </tr>
        <tr>
            <td><img src=".image/screenshot/050系统管理-文件管理-列表-1.png" alt="系统管理-文件管理-列表-1" width="1920" /></td>
            <td><img src=".image/screenshot/051系统管理-文件管理-列表-2.png" alt="系统管理-文件管理-列表-2" width="1920" /></td>
        </tr>
        <tr>
            <td><img src=".image/screenshot/052系统管理-文件管理-图片.png" alt="系统管理-文件管理-图片" width="1920" /></td>
            <td><img src=".image/screenshot/053系统管理-文件管理-音乐.png" alt="系统管理-文件管理-音乐" width="1920" /></td>
        </tr>
        <tr>
            <td><img src=".image/screenshot/101系统工具-代码生成-配置.png" alt="系统工具-代码生成-配置" width="1920" /></td>
            <td><img src=".image/screenshot/102系统工具-代码生成-预览.png" alt="系统工具-代码生成-预览" width="1920" /></td>
        </tr>
        <tr>
            <td><img src=".image/screenshot/200系统监控-在线用户.png" alt="系统监控-在线用户" width="1920" /></td>
            <td><img src=".image/screenshot/201系统监控-系统日志-登录日志.png" alt="系统监控-系统日志-登录日志" width="1920" /></td>
        </tr>
        <tr>
            <td><img src=".image/screenshot/202系统监控-系统日志-操作日志.png" alt="系统监控-系统日志-操作日志" width="1920" /></td>
            <td><img src=".image/screenshot/202系统监控-系统日志-操作日志-详情.png" alt="系统监控-系统日志-操作日志-详情" width="1920" /></td>
        </tr>
    </tbody>
</table>

## 核心技术栈

| 名称                                                         | 版本           | 简介                                                         |
| :----------------------------------------------------------- |:-------------| :----------------------------------------------------------- |
| <a href="https://cn.vuejs.org/" target="_blank">Vue</a>      | 3.4.21       | 渐进式 JavaScript 框架，易学易用，性能出色，适用场景丰富的 Web 前端框架。 |
| <a href="https://arco.design/vue/docs/start" target="_blank">Arco Design</a> | 2.55.0       | 字节跳动推出的前端 UI 框架，年轻化的色彩和组件设计。         |
| <a href="https://www.typescriptlang.org/zh/" target="_blank">TypeScript</a> | 5.0.4        | TypeScript 是微软开发的一个开源的编程语言，通过在 JavaScript 的基础上添加静态类型定义构建而成。 |
| <a href="https://cn.vitejs.dev/" target="_blank">Vite</a>    | 5.1.5        | 下一代的前端工具链，为开发提供极速响应。                     |
| [ContiNew Starter](https://github.com/Charles7c/continew-starter) | 2.1.0        | ContiNew Starter 包含了一系列经过企业实践优化的依赖包（如 MyBatis-Plus、SaToken），可轻松集成到应用中，为开发人员减少手动引入依赖及配置的麻烦，为 Spring Boot Web 项目的灵活快速构建提供支持。 |
| <a href="https://spring.io/projects/spring-boot" target="_blank">Spring Boot</a> | 3.1.11       | 简化 Spring 应用的初始搭建和开发过程，基于“约定优于配置”的理念，使开发人员不再需要定义样板化的配置。（Spring Boot 3.0 开始，要求 Java 17 作为最低版本） |
| <a href="https://undertow.io/" target="_blank">Undertow</a>  | 2.3.10.Final | 采用 Java 开发的灵活的高性能 Web 服务器，提供包括阻塞和基于 NIO 的非堵塞机制。 |
| <a href="https://sa-token.dev33.cn/" target="_blank">Sa-Token + JWT</a> | 1.38.0       | 轻量级 Java 权限认证框架，让鉴权变得简单、优雅。             |
| <a href="https://baomidou.com/" target="_blank">MyBatis Plus</a> | 3.5.5        | MyBatis 的增强工具，在 MyBatis 的基础上只做增强不做改变，简化开发、提高效率。 |
| <a href="https://www.kancloud.cn/tracy5546/dynamic-datasource/2264611" target="_blank">dynamic-datasource-spring-boot-starter</a> | 4.3.0        | 基于 Spring Boot 的快速集成多数据源的启动器。                |
| Hikari                                                       | 5.0.1        | JDBC 连接池，号称 “史上最快连接池”，SpringBoot 在 2.0 之后，采用的默认数据库连接池就是 Hikari。 |
| <a href="https://dev.mysql.com/downloads/mysql/" target="_blank">MySQL</a> | 8.0.33       | 体积小、速度快、总体拥有成本低，是最流行的关系型数据库管理系统之一。 |
| <a href="https://dev.mysql.com/doc/connector-j/8.0/en/" target="_blank">mysql-connector-j</a> | 8.0.33       | MySQL Java 驱动。                                            |
| <a href="https://github.com/p6spy/p6spy" target="_blank">P6Spy</a> | 3.9.1        | SQL 性能分析组件。                                           |
| <a href="https://github.com/liquibase/liquibase" target="_blank">Liquibase</a> | 4.20.0       | 用于管理数据库版本，跟踪、管理和应用数据库变化。             |
| [JetCache](https://github.com/alibaba/jetcache/blob/master/docs/CN/Readme.md) | 2.7.5        | 一个基于 Java 的缓存系统封装，提供统一的 API 和注解来简化缓存的使用。提供了比 SpringCache 更加强大的注解，可以原生的支持 TTL、两级缓存、分布式自动刷新，还提供了 Cache 接口用于手工缓存操作。 |
| <a href="https://github.com/redisson/redisson/wiki/Redisson%E9%A1%B9%E7%9B%AE%E4%BB%8B%E7%BB%8D" target="_blank">Redisson</a> | 3.30.0       | 不仅仅是一个 Redis Java 客户端，Redisson 充分的利用了 Redis 键值数据库提供的一系列优势，为使用者提供了一系列具有分布式特性的常用工具：分布式锁、限流器等。 |
| <a href="https://redis.io/" target="_blank">Redis</a>        | 7.2.3        | 高性能的 key-value 数据库。                                  |
| [X File Storage](https://x-file-storage.xuyanwu.cn/#/)       | 2.1.0        | 一行代码将文件存储到本地、FTP、SFTP、WebDAV、阿里云 OSS、华为云 OBS...等其它兼容 S3 协议的存储平台。 |
| <a href="https://sms4j.com/" target="_blank">SMS4J</a>       | 3.2.1        | 短信聚合框架，轻松集成多家短信服务，解决接入多个短信 SDK 的繁琐流程。 |
| <a href="https://justauth.cn/" target="_blank">Just Auth</a> | 1.16.6       | 开箱即用的整合第三方登录的开源组件，脱离繁琐的第三方登录 SDK，让登录变得 So easy！ |
| <a href="https://easyexcel.opensource.alibaba.com/" target="_blank">Easy Excel</a> | 3.3.4        | 一个基于 Java 的、快速、简洁、解决大文件内存溢出的 Excel 处理工具。 |
| [AJ-Captcha](https://ajcaptcha.beliefteam.cn/captcha-doc/)   | 1.3.0        | Java 行为验证码，包含滑动拼图、文字点选两种方式，UI支持弹出和嵌入两种方式。 |
| Easy Captcha                                                 | 1.6.2        | Java 图形验证码，支持 gif、中文、算术等类型，可用于 Java Web、JavaSE 等项目。 |
| [Crane4j](https://createsequence.gitee.io/crane4j-doc/#/)    | 2.8.0        | 一个基于注解的，用于完成一切 “根据 A 的 key 值拿到 B，再把 B 的属性映射到 A” 这类需求的字段填充框架。 |
| <a href="https://doc.xiaominfo.com/" target="_blank">Knife4j</a> | 4.5.0        | 前身是 swagger-bootstrap-ui，集 Swagger2 和 OpenAPI3 为一体的增强解决方案。 |
| <a href="https://www.hutool.cn/" target="_blank">Hutool</a>  | 5.8.27       | 小而全的 Java 工具类库，通过静态方法封装，降低相关 API 的学习成本，提高工作效率，使 Java 拥有函数式语言般的优雅，让 Java 语言也可以“甜甜的”。 |
| <a href="https://projectlombok.org/" target="_blank">Lombok</a> | 1.18.30      | 在 Java 开发过程中用注解的方式，简化了 JavaBean 的编写，避免了冗余和样板式代码，让编写的类更加简洁。 |

## 快速开始

> [!TIP]
> 更详细的流程，请查看在线文档[《快速开始》](https://continew.top/admin/intro/quick-start.html)。

```bash
# 1.克隆本项目
git clone https://github.com/Charles7c/continew-admin.git

# 2.在 IDE（IntelliJ IDEA/Eclipse）中打开本项目

# 3.修改配置文件中的数据源配置信息、Redis 配置信息、邮件配置信息等
# [3.也可以在 IntelliJ IDEA 中直接配置程序启动环境变量（DB_HOST、DB_PORT、DB_USER、DB_PWD、DB_NAME；REDIS_HOST、REDIS_PORT、REDIS_PWD、REDIS_DB）]

# 4.启动程序
# 4.1 启动成功：访问 http://localhost:8000/，页面输出：Xxx started successfully.
# 4.2 接口文档：http://localhost:8000/doc.html

# 5.部署
# 5.1 Docker 部署
#   5.1.1 服务器安装好 docker 及 docker-compose（参考：https://blog.charles7c.top/categories/fragments/2022/10/31/CentOS%E5%AE%89%E8%A3%85Docker）
#   5.1.2 执行 mvn package 进行项目打包，将 target/app 目录下的所有内容放到 /docker/continew-admin 目录下
#   5.1.3 将 docker 目录上传到服务器 / 目录下，并授权（chmod -R 777 /docker）
#   5.1.4 修改 docker-compose.yml 中的 MySQL 配置、Redis 配置、continew-admin-server 配置、Nginx 配置
#   5.1.5 执行 docker-compose up -d 创建并后台运行所有容器
# 5.2 其他方式部署
```

## 项目结构

> [!TIP]
> 后端采用按功能拆分模块的开发方式，下方项目目录结构是按照模块的层次顺序进行介绍的，实际 IDE 中 `continew-admin-common` 模块会因为字母排序原因排在上方。

```
continew-admin
├─ continew-admin-webapi（API 及打包部署模块）
│  ├─ src
│  │  ├─ main
│  │  │  ├─ java/top/continew/admin
│  │  │  │  ├─ webapi
│  │  │  │  │  ├─ auth（系统认证相关 API）
│  │  │  │  │  ├─ common（通用相关 API）
│  │  │  │  │  ├─ monitor（系统监控相关 API）
│  │  │  │  │  ├─ system（系统管理相关 API）
│  │  │  │  │  └─ tool（系统工具相关 API）
│  │  │  │  └─ ContiNewAdminApplication.java（Spring Boot 启动程序）
│  │  │  └─ resources
│  │  │     ├─ config（核心配置目录）
│  │  │     │  ├─ application-dev.yml（开发环境配置文件）
│  │  │     │  ├─ application-prod.yml（生产环境配置文件）
│  │  │     │  └─ application.yml（通用配置文件）
│  │  │     ├─ db/changelog（Liquibase 数据脚本配置目录）
│  │  │     │  ├─ mysql（MySQL 数据库初始 SQL 脚本目录）
│  │  │     │  ├─ postgresql（PostgreSQL 数据库初始 SQL 脚本目录）
│  │  │     │  └─ db.changelog-master.yaml（Liquibase 变更记录文件）
│  │  │     ├─ templates（模板配置目录，例如：邮件模板）
│  │  │     ├─ banner.txt（Banner 配置文件）
│  │  │     └─ logback-spring.xml（日志配置文件）
│  │  └─ test（测试相关代码目录）
│  └─ pom.xml（包含打包相关配置）
├─ continew-admin-system（系统管理模块，存放系统管理相关业务功能，例如：部门管理、角色管理、用户管理等）
│  ├─ src
│  │  ├─ main
│  │  │  ├─ java/top/continew/admin
│  │  │  │  ├─ auth（系统认证相关业务）
│  │  │  │  │  ├─ config（系统认证相关配置）
│  │  │  │  │  ├─ model（系统认证相关模型）
│  │  │  │  │  │  ├─ query（系统认证相关查询条件）
│  │  │  │  │  │  ├─ req（系统认证相关请求对象（Request））
│  │  │  │  │  │  └─ resp（系统认证相关响应对象（Response））
│  │  │  │  │  └─ service（系统认证相关业务接口及实现类）
│  │  │  │  └─ system（系统管理相关业务）
│  │  │  │     ├─ config（系统管理相关配置）
│  │  │  │     ├─ enums（系统管理相关枚举）
│  │  │  │     ├─ mapper（系统管理相关 Mapper）
│  │  │  │     ├─ model（系统管理相关模型）
│  │  │  │     │  ├─ entity（系统管理相关实体对象）
│  │  │  │     │  ├─ query（系统管理相关查询条件）
│  │  │  │     │  ├─ req（系统管理相关请求对象（Request））
│  │  │  │     │  └─ resp（系统管理相关响应对象（Response））
│  │  │  │     └─ service（系统管理相关业务接口及实现类）
│  │  │  └─ resources
│  │  │     └─ mapper（系统管理相关 Mapper XML 文件目录）
│  │  └─ test（测试相关代码目录）
│  └─ pom.xml
├─ continew-admin-generator（代码生成器插件模块）
│  ├─ src
│  │  ├─ main
│  │  │  ├─ java/top/continew/admin/generator
│  │  │  │  ├─ config（代码生成器相关配置）
│  │  │  │  ├─ enums（代码生成器相关枚举）
│  │  │  │  ├─ mapper（代码生成器相关 Mapper）
│  │  │  │  ├─ model（代码生成器相关模型）
│  │  │  │  │  ├─ entity（代码生成器相关实体对象）
│  │  │  │  │  ├─ query（代码生成器相关查询条件）
│  │  │  │  │  ├─ req（代码生成器相关请求对象（Request））
│  │  │  │  │  └─ resp（代码生成器相关响应对象（Response））
│  │  │  │  └─ service（代码生成器相关业务接口及实现类）
│  │  │  └─ resources
│  │  │     ├─ templates/generator（代码生成相关模板目录）
│  │  │     ├─ application.yml（代码生成配置文件）
│  │  │     └─ generator.properties（代码生成类型映射配置文件）
│  │  └─ test（测试相关代码目录）
│  └─ pom.xml
├─ continew-admin-common（公共模块，存放公共工具类，公共配置等）
│  ├─ src
│  │  ├─ main/java/top/continew/admin/common
│  │  │  ├─ config（公共配置）
│  │  │  ├─ constant（公共常量）
│  │  │  ├─ enums（公共枚举）
│  │  │  ├─ model（公共模型）
│  │  │  │  ├─ dto（公共 DTO（Data Transfer Object））
│  │  │  │  ├─ req（公共请求对象（Request））
│  │  │  │  └─ resp（公共响应对象（Response））
│  │  │  └─ util（公共工具类）
│  │  └─ test（测试相关代码目录）
│  └─ pom.xml
├─ .github（GitHub 相关配置目录，实际开发时直接删除）
├─ .idea
│  └─ icon.png（IDEA 项目图标，实际开发时直接删除）
├─ .image（截图目录，实际开发时直接删除）
├─ .style（代码格式相关配置目录，实际开发时根据需要取舍，删除时注意删除 spotless 插件配置）
├─ .gitignore（Git 忽略文件相关配置文件）
├─ docker（项目部署相关配置目录，实际开发时可备份后直接删除）
├─ LICENSE（开源协议文件）
├─ CHANGELOG.md（更新日志文件，实际开发时直接删除）
├─ README.md（项目 README 文件，实际开发时替换为真实内容）
├─ lombok.config（Lombok 全局配置文件）
└─ pom.xml（包含版本锁定及全局插件相关配置）
```

## 贡献指南

ContiNew Admin 致力于提供开箱即用，持续舒适的开发体验。作为一个开源项目，Creator 的初心是希望 ContiNew Admin 依托开源协作模式，提升技术透明度、放大集体智慧、共创优秀实践，源源不断地为企业级项目开发提供助力。

我们非常欢迎广大社区用户为 ContiNew Admin **贡献（开发，测试、文档、答疑等）** 或优化代码，欢迎各位感兴趣的小伙伴儿，[添加微信](https://continew.top/support.html) 讨论或认领任务。

### 分支说明

ContiNew Admin 的分支目前分为下个大版本的开发分支和上个大版本的维护分支，PR 前请注意对应分支是否处于维护状态，版本支持情况请查看 [更新日志/版本支持](https://continew.top/admin/other/changelog.html#%E7%89%88%E6%9C%AC%E6%94%AF%E6%8C%81)。

| 分支  | 说明                                                         |
| ----- | ------------------------------------------------------------ |
| dev   | 开发分支，默认为下个大版本的 SNAPSHOT 版本，接受新功能或新功能优化 PR |
| x.x.x | 维护分支，在 vx.x.x 版本维护期终止前（一般为下个大版本发布前），用于修复上个版本的 Bug，只接受已有功能修复，不接受新功能 PR |

### 贡献代码

如果您想提交新功能或优化现有代码，可以按照以下步骤操作：

1. 首先，在 Gitee 或 Github 上将项目 fork 到您自己的仓库
2. 然后，将 fork 过来的项目（即您的项目）克隆到本地
3. 切换到当前仍在维护的分支（请务必充分了解分支使用说明，可进群联系维护者确认）
4. 开始修改代码，修改完成后，将代码 commit 并 push 到您的远程仓库
5. 在 Gitee 或 Github 上新建 pull request（pr），选择好源和目标，按模板要求填写说明信息后提交即可（多多参考 [已批准合并的 pr 记录](https://github.com/Charles7c/continew-admin/pulls?q=is%3Apr+is%3Amerged)，会大大增加批准合并率）
6. 最后，耐心等待维护者合并您的请求即可

请记住，如果您有任何疑问或需要帮助，我们将随时提供支持。

> [!IMPORTANT]
> 欢迎大家为 ContiNew Admin 贡献代码，我们非常感谢您的支持！为了更好地管理项目，维护者有一些要求：
>
> 1. 请确保代码、配置文件的结构和命名规范良好，完善的代码注释甚至包括接口文档参数示例，并遵循阿里巴巴的 <a href="https://github.com/Charles7c/continew-admin/blob/dev/.style/Java%E5%BC%80%E5%8F%91%E6%89%8B%E5%86%8C(%E9%BB%84%E5%B1%B1%E7%89%88).pdf" target="_blank">《Java开发手册(黄山版)》</a> 中的代码规范，保证代码质量和可维护性
> 2. 在提交代码前，请按照 [Angular 提交规范](https://github.com/conventional-changelog/conventional-changelog/tree/master/packages/conventional-changelog-angular) 编写 commit 的 message（建议在 IntelliJ IDEA 中下载并安装 Git Commit Template 插件，以便按照规范进行 commit）
> 3. 提交代码之前，请关闭所有代码窗口，执行 `mvn compile` 命令（代码格式化插件会在项目编译时对全局代码进行格式修正），编译通过后，不要再打开查看任何代码窗口，直接提交即可，以免不同的 IDE 配置会自动进行代码格式化

## 反馈交流

欢迎各位小伙伴儿扫描下方二维码加好友，备注 `cnadmin`，拉你进群，探讨技术、提提需求~   

加入交流群后，你将会：

- 第一时间收到框架动态
- 第一时间收到框架更新通知
- 第一时间收到框架 Bug 通知
- 和众多大佬互相 (huá shuǐ) 交流 (mō yú)

<div align="left">
  <img src=".image/qrcode.jpg" alt="二维码" width="230px" />
</div>
<details>
<summary>无加群意愿</summary>
如无加群意愿，欢迎在 <a href="https://github.com/Charles7c/continew-admin/issues" target="_blank">Issues</a> 中反馈交流~ 🍻
</details>

## 鸣谢

### 鸣谢

感谢参与贡献的每一位小伙伴🥰

<a href="https://github.com/Charles7c/continew-admin/graphs/contributors">
  <img src="https://opencollective.com/continew-admin/contributors.svg?width=890&button=false" alt="contributors" />
</a>

### 特别鸣谢

- 感谢 <a href="https://www.jetbrains.com/" target="_blank">JetBrains</a> 提供的 <a href="https://jb.gg/OpenSourceSupport" target="_blank">非商业开源软件开发授权</a> 
- 感谢 <a href="https://github.com/baomidou/mybatis-plus" target="_blank">MyBatis Plus</a>、<a href="https://github.com/dromara/sa-token" target="_blank">Sa-Token</a> 、<a href="https://github.com/alibaba/jetcache" target="_blank">JetCache</a>、<a href="https://github.com/opengoofy/crane4j" target="_blank">Crane4j</a>、<a href="https://github.com/xiaoymin/knife4j" target="_blank">Knife4j</a>、<a href="https://github.com/dromara/hutool" target="_blank">Hutool</a> 等开源组件作者为国内开源世界作出的贡献
- 感谢项目使用或未使用到的每一款开源组件，致敬各位开源先驱 :fire:

## License

- 遵循 <a href="https://github.com/Charles7c/continew-admin/blob/dev/LICENSE" target="_blank">Apache-2.0</a> 开源许可协议
- Copyright © 2022-present <a href="https://blog.charles7c.top" target="_blank">Charles7c</a>

## GitHub Star 趋势

![GitHub Star 趋势](https://starchart.cc/charles7c/continew-admin.svg)