# ContiNew Admin 多租户中后台管理框架

<a href="https://github.com/continew-org/continew-admin" title="Release" target="_blank">
<img src="https://img.shields.io/badge/SNAPSHOT-v4.2.0-%23ff3f59.svg" alt="Release" />
</a>
<a href="https://github.com/continew-org/continew-starter" title="ContiNew Starter" target="_blank">
<img src="https://img.shields.io/badge/ContiNew Starter-2.15.0-%236CB52D.svg" alt="ContiNew Starter" />
</a>
<a href="https://spring.io/projects/spring-boot" title="Spring Boot" target="_blank">
<img src="https://img.shields.io/badge/Spring Boot-3.3.12-%236CB52D.svg?logo=Spring-Boot" alt="Spring Boot" />
</a>
<a href="https://github.com/continew-org/continew-admin" title="Open JDK" target="_blank">
<img src="https://img.shields.io/badge/Open JDK-17-%236CB52D.svg?logo=OpenJDK&logoColor=FFF" alt="Open JDK" />
</a>
<a href="https://app.codacy.com/gh/continew-org/continew-admin/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade" title="Codacy" target="_blank">
<img src="https://app.codacy.com/project/badge/Grade/19e3e2395d554efe902c3822e65db30e" alt="Codacy" />
</a>
<a href="https://sonarcloud.io/summary/new_code?id=Charles7c_continew-admin" title="Sonar" target="_blank">
<img src="https://sonarcloud.io/api/project_badges/measure?project=Charles7c_continew-admin&metric=alert_status" alt="Sonar" />
</a>
<br />
<a href="https://github.com/continew-org/continew-admin/blob/dev/LICENSE" title="License" target="_blank">
<img src="https://img.shields.io/badge/License-Apache--2.0-blue.svg" alt="License" />
</a>
<a href="https://github.com/continew-org/continew-admin" title="GitHub Stars" target="_blank">
<img src="https://img.shields.io/github/stars/continew-org/continew-admin?style=social" alt="GitHub Stars" />
</a>
<a href="https://github.com/continew-org/continew-admin" title="GitHub Forks" target="_blank">
<img src="https://img.shields.io/github/forks/continew-org/continew-admin?style=social" alt="GitHub Forks" />
</a>
<a href="https://atomgit.com/continew/continew-admin" title="AtomGit Stars" target="_blank">
<img src="https://atomgit.com/continew/continew-admin/star/badge.svg" alt="AtomGit Stars" />
</a>
<a href="https://atomgit.com/continew/continew-admin" title="AtomGit Forks" target="_blank">
<img src="https://atomgit.com/continew/continew-admin/fork/badge.svg" alt="AtomGit Forks" />
</a>
<a href="https://gitee.com/continew/continew-admin" title="Gitee Stars" target="_blank">
<img src="https://gitee.com/continew/continew-admin/badge/star.svg?theme=dark" alt="Gitee Stars" />
</a>
<a href="https://gitee.com/continew/continew-admin" title="Gitee Forks" target="_blank">
<img src="https://gitee.com/continew/continew-admin/badge/fork.svg?theme=dark" alt="Gitee Forks" />
</a>

📚 [在线文档](https://continew.top) | 🚀 [演示地址](https://continew.top/docs/admin/guide/demo.html) | 💬 [吐槽广场（你就是 Talk King!）](https://continew.top/docs/admin/issue-hub.html) | [![问 DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/continew-org/continew-admin)

## 简介

**AI 编程纪元已经开启，基于 ContiNew 项目开发，让 AI 助手“学习”更优雅的代码规范，“写出”更优质的代码。**

ContiNew Admin（Continue New Admin），页面现代美观，且专注设计与代码细节的 **高质量多租户中后台** 管理系统框架。开箱即用，持续迭代优化，持续提供舒适的开发体验。

当前采用的技术栈：Spring Boot3（Java17）、Vue3 & Arco Design & TS & Vite、Sa-Token、MyBatis Plus、Redisson、FastExcel、CosId、JetCache、JustAuth、Crane4j、Spring Doc、Hutool 等。

我们始终坚信好的产品必然是反复打磨出来的，而在工作中我们受限于客户需求、开发周期等因素，无法深度打磨、重构我们的代码，这也是架构腐烂的根源。所以，我们希望能在业余时间，通过开源社区的力量来打磨出一个好的产品，一个好的实践，一个好的生态。

我们的愿景在于，当你将 ContiNew 系列项目应用到工作场景时，不仅仅是得到效率的提高，更可以得到舒适的开发体验，让更多开发者的编程工作多一点“甜”。

## 项目源码

|         | 后端                                                         | 前端                                                         |
| :------ | :----------------------------------------------------------- | :----------------------------------------------------------- |
| GitHub  | [continew-org/continew-admin](https://github.com/continew-org/continew-admin) | [continew-org/continew-admin-ui](https://github.com/continew-org/continew-admin-ui) |
| AtomGit | [continew/continew-admin](https://atomgit.com/continew/continew-admin) | [continew/continew-admin-ui](https://atomgit.com/continew/continew-admin-ui) |
| Gitee   | [continew/continew-admin](https://gitee.com/continew/continew-admin) | [continew/continew-admin-ui](https://gitee.com/continew/continew-admin-ui) |

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
> 更为完整的图文描述请查阅[《在线文档》](https://continew.top/docs/admin/guide/why-choose-us.html)。

**AI 编程纪元已经开启，基于 ContiNew 项目开发，让 AI 助手“学习”更优雅的代码规范，“写出”更优质的代码。**

**1.长期稳定：** 自 2022 年 12 月 8 日创建，2023 年 3 月 26 日发布 v1.0.0，截至今日，ContiNew Admin 已累计发布 25 个版本，ContiNew Starter 已累计发布 43 个版本。

**2.甄选技术栈：** ContiNew（Continue New） 项目致力于持续迭代优化，确保技术栈紧跟时代。在技术选型时，我们进行了深度广泛的调研，从流行度、成熟度和发展潜力等多维度精心挑选技术栈。

**3.Starter 组件：** 从 v2.1.0 版本开始，我们将后端基础组件及各框架集成配置抽取并封装到 ContiNew Starter 项目中，极大降低上手和升级难度。且 **[已发布至 Maven 中央仓库](https://central.sonatype.com/search?q=continew-starter&namespace=top.continew)**，你可以在任意项目中直接引入所需依赖使用。即使你不使用完整的中后台框架，这些能让你搭项目框架更快、更爽、更省力的 Starter 组件，难道不香吗？

**4.CRUD 套件：** 封装通用增删改查套件，适配后端各分层架构，几分钟即可提供一套完整的 CRUD API，包括新增、修改、批量删除、查询详情、分页列表、全部列表、树型列表、Excel 导出，甚至是字典列表（用于下拉选项场景）。所有 API 均可根据实际需求灵活开放或扩展。

```java
@Tag(name = "部门管理 API")
@RestController
@CrudRequestMapping(value = "/system/dept", api = {Api.TREE, Api.GET, Api.CREATE, Api.UPDATE, Api.DELETE, Api.EXPORT, Api.TREE_DICT})
public class DeptController extends BaseController<DeptService, DeptResp, DeptDetailResp, DeptQuery, DeptReq> {}
```

**5.代码生成器：** 同步提供了代码生成器，配套前后端代码生成模板。数据表设计完成后，简单配置即可生成前后端 80% 的代码，包括 CRUD API、权限控制、参数校验、接口文档等内容。若业务不复杂，甚至能覆盖 95% 的代码量。

**6.提升开发体验：** 持续优化并适配各类能提升开发体验的组件。

- ContiNew Starter 组件集合：针对 Spring 基础配置、通用解决方案及流行框架进行深度封装，改善你开发每个 Spring Boot Web 项目的体验（包含时间日期及枚举参数自动转换、默认线程池、跨域、加密、脱敏、限流、幂等、License、日志、异常及响应通用解决方案等）；
- Crane4j 数据填充组件：减少因单个字段（如用户名）而产生的联表查询；
- SpEL Validator：基于 SpEL 表达式的参数校验，强化复杂场景下的参数验证（如：当某字段为特定值时，另一字段不能为空）；
- P6Spy SQL 性能分析：开发期间可方便地监控 SQL 执行性能；
- TLog 链路追踪：在繁杂的日志中快速定位某次请求的完整日志；
- JetCache 缓存框架：通过注解即可实现方法级缓存，支持灵活的二级缓存配置和分布式自动刷新；

**7.Almost 最佳后端规范：** 后端严格遵循阿里巴巴 Java 编码规范，注释覆盖率 > 45%，接口参数示例 100%。代码分层清晰，变量与方法命名统一规范，前端代码同样采用严格的 ESLint、StyleLint 等检查。优秀的设计带来极高的代码复用率！开发时，你会有一种“无需多写，理应如此”的流畅感。

**8.卓越工程化实践：** 后端采用模块化工程结构，集成了统一版本管理、编译时自动代码格式化等插件。提供自定义打包部署配置（配置文件、第三方依赖与主程序分离），以及全套环境和应用的 Docker Compose 部署脚本。

为减少新项目开发的改造成本，我们持续深度聚合项目品牌配置，通过简单的配置和结构修改，即可快速启动你的专属项目。

我们还进行了全局 Lombok 配置，继承场景默认自动应用 `@EqualsAndHashCode(callSuper = true)` 和 `@ToString(callSuper = true)`，无需手动添加。同时主动禁用了部分 Lombok 注解（如 `@Val`、`@Log4j` 等），避免“又菜又爱玩”的 partner 滥用。

**9.全能业务脚手架：** 支持 **SaaS 租户架构**，基于 RBAC 的权限控制与通用数据权限管理。精心设计的 UI 界面与色彩主题，兼具美观与实用性。内置丰富的通用业务解决方案：第三方登录、邮箱/短信服务（含生产级漏洞处理方案）、个人中心、用户管理、角色管理、组织管理、系统配置、系统日志、消息中心、通知公告等，逻辑闭环，开箱即用。

> 优秀的中后台框架不仅提供组件集成与配置，封装好用的工具，更应提供通用基础业务设计及解决方案，为初创团队减负。

**10.质量与安全并重：** 我们高度重视项目质量与安全，CI 已集成 Sonar、Codacy，代码提交即自动扫描质量问题。定期扫描 CVE 漏洞，及时解决潜在风险。封装了数据库字段加密、JSON 脱敏、XSS 过滤等工具，提供全方位的安全解决方案。

许多项目在开发或交付过程中需满足 Sonarqube 等质量指标，使用 ContiNew Admin 框架，让你从一开始就站在高质量的起点。

---

由于篇幅有限，且项目正处于高速发展期，更多功能正在持续开发中，敬请关注仓库或加入交流群了解最新动态。至于统一异常处理、错误处理、基础线程池配置（默认线程参数、线程上下文支持异步传递）等基础特性，这里不再赘述，更多细节优化欢迎克隆代码体验。
> Talk is cheap, show the code.

##  系统功能

> [!TIP]
> 更多功能和优化正在赶来💦，最新项目计划、进展请进群或查看 [吐槽广场](https://continew.top/docs/admin/issue-hub.html) 和 [更新日志](https://continew.top/docs/admin/changelog/)。
> 功能不会用？请查看 [功能手册](https://continew.top/docs/admin/function/tenant/management.html)。

- 仪表盘：提供工作台、分析页，工作台提供功能快捷导航入口、最新公告、动态；分析页提供全面数据可视化能力
- 个人中心：支持基础信息修改、密码修改、邮箱绑定、手机号绑定（并提供行为验证码、短信限流等安全处理）、第三方账号绑定/解绑（微信登录）、头像裁剪上传
- 消息中心：提供站内信消息统一查看、标记已读、全部已读、删除等功能（目前仅支持系统通知消息）、提供个人公告查看
- 用户管理：管理系统用户，包含新增、修改、删除、导入、导出、重置密码、分配角色等功能
- 角色管理：管理系统用户的功能权限及数据权限，包含新增、修改、删除、分配角色等功能
- 菜单管理：管理系统菜单及按钮权限，支持多级菜单，动态路由，包含新增、修改、删除等功能
- 部门管理：管理系统组织架构，包含新增、修改、删除、导出等功能，以树形列表进行展示
- 通知公告：管理系统公告，支持通知范围（所有人、指定用户）、通知方式（系统消息、登录弹窗）、定时发送、置顶设置
- 文件管理：管理系统文件及文件夹，支持回收站、上传/分片上传、下载、预览（目前支持图片、音视频、PDF、Word、Excel、PPT）、重命名、切换视图（列表、网格）等功能
- 字典管理：管理系统公用数据字典，例如：消息类型。支持字典标签背景色和排序等配置
- 系统配置：
  - 网站配置：提供修改系统标题、Logo、favicon、版权信息等基础配置功能，以方便用户系统与其自身品牌形象保持一致
  - 安全配置：提供密码策略修改，支持丰富的密码策略设定，包括但不限于 `密码有效期`、`密码重复次数`、`密码错误锁定账号次数、时间` 等
  - 登录配置：提供验证码开关等登录相关配置
  - 邮件配置：提供系统发件箱配置，也支持通过配置文件指定
  - 短信配置：提供系统短信服务配置，也支持通过配置文件指定
  - 存储配置：管理文件存储配置，支持本地存储、兼容 S3 协议对象存储
  - 客户端配置：多端（PC端、小程序端等）认证管理，可设置不同的 token 有效期
- 在线用户：管理当前登录用户，可一键踢除下线
- 日志管理：管理系统登录日志、操作日志，支持查看日志详情，包含请求头、响应头等报文信息
- 短信日志：管理系统短信发送日志，支持删除、导出 
- 应用管理：管理第三方系统应用 AK、SK，包含新增、修改、删除、查看密钥、重置密钥等功能，支持设置密钥有效期
- 租户管理：管理租户信息，包含新增、修改、删除、分配角色等功能
- 租户套餐：管理租户套餐信息，包含新增、修改、删除、查看等功能
- 任务管理：管理系统定时任务，包含新增、修改、删除、执行功能，支持 Cron（可配置式生成 Cron 表达式） 和固定频率
- 任务日志：管理定时任务执行日志，包含停止、重试指定批次等功能
- 代码生成：提供根据数据库表自动生成相应的前后端 CRUD 代码的功能，支持同步最新表结构及代码生成预览

## 系统截图

> [!TIP]
> 受篇幅长度及功能更新频率影响，下方仅为系统 **部分** 功能于 **2024年11月18日** 进行的截图，更多新增功能及细节请登录演示环境或 clone 代码到本地启动查看。

<table border="1" cellpadding="1" cellspacing="1" style="width: 500px">
    <tbody>
        <tr>
            <td><img src=".image/screenshot/000登录页面.png" alt="登录页面" width="1920" /></td>
            <td><img src=".image/screenshot/000登录页面-H5.png" alt="登录页面-H5" width="1920" /></td>
        </tr>
        <tr>
            <td><img src=".image/screenshot/001仪表盘.png" alt="仪表盘" width="1920" /></td>
            <td><img src=".image/screenshot/002分析页.png" alt="分析页" width="1920" /></td>
        </tr>
       <tr>
            <td><img src=".image/screenshot/010个人中心.png" alt="个人中心" width="1920" /></td>
            <td><img src=".image/screenshot/013消息中心.png" alt="消息中心" width="1920" /></td>
        </tr>
        <tr>
            <td><img src=".image/screenshot/011安全设置-修改邮箱.png" alt="安全设置-修改邮箱" width="1920" /></td>
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
            <td><img src=".image/screenshot/025系统管理-角色管理-列表.png" alt="系统管理-角色管理-列表" width="1920" /></td>
            <td><img src=".image/screenshot/026系统管理-角色管理-新增.png" alt="系统管理-角色管理-新增" width="1920" /></td>
        </tr>
        <tr>
            <td><img src=".image/screenshot/030系统管理-菜单管理-列表.png" alt="系统管理-菜单管理-列表" width="1920" /></td>
            <td><img src=".image/screenshot/031系统管理-菜单管理-新增.png" alt="系统管理-菜单管理-新增" width="1920" /></td>
        </tr>
        <tr>
            <td><img src=".image/screenshot/045系统管理-公告管理-列表.png" alt="系统管理-公告管理-列表" width="1920" /></td>
            <td><img src=".image/screenshot/046系统管理-公告管理-修改.png" alt="系统管理-公告管理-修改" width="1920" /></td>
        </tr>
        <tr>
            <td><img src=".image/screenshot/040系统管理-字典管理-列表.png" alt="系统管理-字典管理-列表" width="1920" /></td>
            <td><img src=".image/screenshot/041系统管理-字典项管理.png" alt="系统管理-字典项管理" width="1920" /></td>
        </tr>
        <tr>
            <td><img src=".image/screenshot/051系统管理-文件管理-列表-2.png" alt="系统管理-文件管理-列表-2" width="1920" /></td>
            <td><img src=".image/screenshot/052系统管理-文件管理-查看文档.png" alt="系统管理-文件管理-查看文档" width="1920" /></td>
        </tr>
        <tr>
            <td><img src=".image/screenshot/301系统工具-代码生成-配置.png" alt="系统工具-代码生成-配置" width="1920" /></td>
            <td><img src=".image/screenshot/302系统工具-代码生成-预览.png" alt="系统工具-代码生成-预览" width="1920" /></td>
        </tr>
        <tr>
            <td><img src=".image/screenshot/100系统监控-在线用户.png" alt="系统监控-在线用户" width="1920" /></td>
            <td><img src=".image/screenshot/101系统监控-系统日志-登录日志.png" alt="系统监控-系统日志-登录日志" width="1920" /></td>
        </tr>
        <tr>
            <td><img src=".image/screenshot/102系统监控-系统日志-操作日志.png" alt="系统监控-系统日志-操作日志" width="1920" /></td>
            <td><img src=".image/screenshot/103系统监控-系统日志-操作日志-详情.png" alt="系统监控-系统日志-操作日志-详情" width="1920" /></td>
        </tr>
    </tbody>
</table>


## 核心技术栈

| 名称                                                                                                                                | 版本           | 简介                                                         |
|:----------------------------------------------------------------------------------------------------------------------------------|:-------------| :----------------------------------------------------------- |
| <a href="https://vuejs.org/" target="_blank">Vue</a>                                                                              | 3.5.4        | 渐进式 JavaScript 框架，易学易用，性能出色，适用场景丰富的 Web 前端框架。 |
| <a href="https://arco.design/vue/docs/start" target="_blank">Arco Design</a>                                                      | 2.57.0       | 字节跳动推出的前端 UI 框架，年轻化的色彩和组件设计。         |
| <a href="https://www.typescriptlang.org/zh/" target="_blank">TypeScript</a>                                                       | 5.0.4        | TypeScript 是微软开发的一个开源的编程语言，通过在 JavaScript 的基础上添加静态类型定义构建而成。 |
| <a href="https://vite.dev/" target="_blank">Vite</a>                                                                              | 5.1.5        | 下一代的前端工具链，为开发提供极速响应。                     |
| [ContiNew Starter](https://github.com/continew-org/continew-starter)                                                              | 2.15.0       | ContiNew Starter 包含了一系列经过企业实践优化的依赖包（如 MyBatis-Plus、SaToken），可轻松集成到应用中，为开发人员减少手动引入依赖及配置的麻烦，为 Spring Boot Web 项目的灵活快速构建提供支持。 |
| <a href="https://spring.io/projects/spring-boot" target="_blank">Spring Boot</a>                                                  | 3.3.12       | 简化 Spring 应用的初始搭建和开发过程，基于“约定优于配置”的理念，使开发人员不再需要定义样板化的配置。（Spring Boot 3.0 开始，要求 Java 17 作为最低版本） |
| <a href="https://undertow.io/" target="_blank">Undertow</a>                                                                       | 2.3.18.Final | 采用 Java 开发的灵活的高性能 Web 服务器，提供包括阻塞和基于 NIO 的非堵塞机制。 |
| <a href="https://sa-token.dev33.cn/" target="_blank">Sa-Token + JWT</a>                                                           | 1.44.0       | 轻量级 Java 权限认证框架，让鉴权变得简单、优雅。             |
| <a href="https://baomidou.com/" target="_blank">MyBatis Plus</a>                                                                  | 3.5.12       | MyBatis 的增强工具，在 MyBatis 的基础上只做增强不做改变，简化开发、提高效率。 |
| <a href="https://www.kancloud.cn/tracy5546/dynamic-datasource/2264611" target="_blank">dynamic-datasource-spring-boot-starter</a> | 4.3.1        | 基于 Spring Boot 的快速集成多数据源的启动器。                |
| Hikari                                                                                                                            | 5.1.0        | JDBC 连接池，号称 “史上最快连接池”，SpringBoot 在 2.0 之后，采用的默认数据库连接池就是 Hikari。 |
| <a href="https://dev.mysql.com/downloads/mysql/" target="_blank">MySQL</a>                                                        | 8.0.42       | 体积小、速度快、总体拥有成本低，是最流行的关系型数据库管理系统之一。 |
| <a href="https://dev.mysql.com/doc/connector-j/8.0/en/" target="_blank">mysql-connector-j</a>                                     | 8.3.0        | MySQL Java 驱动。                                            |
| <a href="https://github.com/p6spy/p6spy" target="_blank">P6Spy</a>                                                                | 3.9.1        | SQL 性能分析组件。                                           |
| <a href="https://github.com/liquibase/liquibase" target="_blank">Liquibase</a>                                                    | 4.27.0       | 用于管理数据库版本，跟踪、管理和应用数据库变化。             |
| [JetCache](https://github.com/alibaba/jetcache/blob/master/docs/CN/Readme.md)                                                     | 2.7.8        | 一个基于 Java 的缓存系统封装，提供统一的 API 和注解来简化缓存的使用。提供了比 SpringCache 更加强大的注解，可以原生的支持 TTL、两级缓存、分布式自动刷新，还提供了 Cache 接口用于手工缓存操作。 |
| <a href="https://github.com/redisson/redisson/wiki/Redisson%E9%A1%B9%E7%9B%AE%E4%BB%8B%E7%BB%8D" target="_blank">Redisson</a>     | 3.49.0       | 不仅仅是一个 Redis Java 客户端，Redisson 充分的利用了 Redis 键值数据库提供的一系列优势，为使用者提供了一系列具有分布式特性的常用工具：分布式锁、限流器等。 |
| <a href="https://redis.io/" target="_blank">Redis</a>                                                                             | 7.2.8        | 高性能的 key-value 数据库。                                  |
| [Snail Job](https://snailjob.opensnail.com/)                                                                                      | 1.5.0        | 灵活，可靠和快速的分布式任务重试和分布式任务调度平台。       |
| [X File Storage](https://x-file-storage.xuyanwu.cn/#/)                                                                            | 2.2.1        | 一行代码将文件存储到本地、FTP、SFTP、WebDAV、阿里云 OSS、华为云 OBS...等其它兼容 S3 协议的存储平台。 |
| <a href="https://sms4j.com/" target="_blank">SMS4J</a>                                                                            | 3.3.4        | 短信聚合框架，轻松集成多家短信服务，解决接入多个短信 SDK 的繁琐流程。 |
| <a href="https://justauth.cn/" target="_blank">Just Auth</a>                                                                      | 1.16.7       | 开箱即用的整合第三方登录的开源组件，脱离繁琐的第三方登录 SDK，让登录变得 So easy！ |
| <a href="https://github.com/fast-excel/fastexcel" target="_blank">Fast Excel</a>                                                  | 1.2.0        | （由原 EasyExcel 作者创建的新项目）一个基于 Java 的、快速、简洁、解决大文件内存溢出的 Excel 处理工具。 |
| [AJ-Captcha](https://ajcaptcha.beliefteam.cn/captcha-doc/)                                                                        | 1.3.0        | Java 行为验证码，包含滑动拼图、文字点选两种方式，UI支持弹出和嵌入两种方式。 |
| Easy Captcha                                                                                                                      | 1.6.2        | Java 图形验证码，支持 gif、中文、算术等类型，可用于 Java Web、JavaSE 等项目。 |
| [Crane4j](https://createsequence.gitee.io/crane4j-doc/#/)                                                                         | 2.9.0        | 一个基于注解的，用于完成一切 “根据 A 的 key 值拿到 B，再把 B 的属性映射到 A” 这类需求的字段填充框架。 |
| [SpEL Validator](https://spel-validator.sticki.cn/)                                                                               | 0.5.2-beta   | 基于 SpEL 的 jakarta.validation-api 扩展增强包。             |
| [CosID](https://cosid.ahoo.me/guide/getting-started.html)                                                                         | 2.13.0       | 旨在提供通用、灵活、高性能的分布式 ID 生成器。               |
| [Graceful Response](https://doc.feiniaojin.com/graceful-response/home.html)                                                       | 5.0.4-boot3  | 一个Spring Boot技术栈下的优雅响应处理组件，可以帮助开发者完成响应数据封装、异常处理、错误码填充等过程，提高开发效率，提高代码质量。 |
| <a href="https://nextdoc4j.top/" target="_blank">NextDoc4j</a>                                                                | 1.1.5        | 现代化 API 文档 UI 工具，全面替代 Swagger UI。 |
| [OpenFeign](https://springdoc.cn/spring-cloud-openfeign/)                                                                         | 13.5         | Spring Cloud OpenFeign 是一种基于 Spring Cloud 的声明式 REST 客户端，它简化了与 HTTP 服务交互的过程。 |
| <a href="https://www.hutool.cn/" target="_blank">Hutool</a>                                                                       | 5.8.38       | 小而全的 Java 工具类库，通过静态方法封装，降低相关 API 的学习成本，提高工作效率，使 Java 拥有函数式语言般的优雅，让 Java 语言也可以“甜甜的”。 |
| <a href="https://projectlombok.org/" target="_blank">Lombok</a>                                                                   | 1.18.36      | 在 Java 开发过程中用注解的方式，简化了 JavaBean 的编写，避免了冗余和样板式代码，让编写的类更加简洁。 |

## 快速开始

> [!TIP]
> 更详细的流程，请查看在线文档[《快速开始》](https://continew.top/docs/admin/guide/quick-start.html)。

```bash
# 1.克隆本项目
git clone https://github.com/continew-org/continew-admin.git

# 2.在 IDE（IntelliJ IDEA/Eclipse）中打开本项目

# 3.修改配置文件中的数据源配置信息、Redis 配置信息、邮件配置信息等
# [3.也可以在 IntelliJ IDEA 中直接配置程序启动环境变量（DB_HOST、DB_PORT、DB_USER、DB_PWD、DB_NAME；REDIS_HOST、REDIS_PORT、REDIS_PWD、REDIS_DB）]

# 4.启动程序
# 启动成功，在控制台末尾会输出 ContiNew Admin service started successfully.
# 并输出 API 地址及 API 接口文档地址
```

## 项目结构

> [!TIP]
> 后端采用按功能拆分模块的开发方式，下方项目目录结构是按照模块的层次顺序进行介绍的，实际 IDE 中 `continew-common` 模块会因为字母排序原因排在上方。

```
continew-admin
├─ continew-server（打包部署模块）
│  ├─ src
│  │  ├─ main
│  │  │  ├─ java/top/continew/admin
│  │  │  │  ├─ config （配置）
│  │  │  │  │  ├─ log（操作日志配置）
│  │  │  │  │  └─ satoken（SaToken 认证配置）
│  │  │  │  ├─ controller（通用 API）
│  │  │  │  ├─ job （定时任务）
│  │  │  │  └─ ContiNewAdminApplication.java（ContiNew Admin 启动程序）
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
├─ continew-system（系统管理模块，存放系统管理相关业务功能，例如：部门管理、角色管理、用户管理等）
│  ├─ src
│  │  ├─ main
│  │  │  ├─ java/top/continew/admin
│  │  │  │  ├─ auth（系统认证相关业务）
│  │  │  │  │  ├─ controller（系统认证相关 API）
│  │  │  │  │  ├─ service（系统认证相关业务接口及实现类）
│  │  │  │  │  ├─ model（系统认证相关模型）
│  │  │  │  │  │  ├─ query（系统认证相关查询条件）
│  │  │  │  │  │  ├─ req（系统认证相关请求参数（Request））
│  │  │  │  │  │  └─ resp（系统认证相关响应参数（Response））
│  │  │  │  │  ├─ enums（系统认证相关枚举）
│  │  │  │  │  ├─ constant（系统认证相关常量）
│  │  │  │  │  ├─ handler（系统认证相关处理器）
│  │  │  │  │  └─ config（系统认证相关配置）
│  │  │  │  └─ system（系统管理相关业务）
│  │  │  │     ├─ api（系统管理相关公共业务 API 实现）
│  │  │  │     ├─ controller（系统管理相关 API）
│  │  │  │     ├─ service（系统管理相关业务接口及实现类）
│  │  │  │     ├─ mapper（系统管理相关 Mapper）
│  │  │  │     ├─ model（系统管理相关模型）
│  │  │  │     │  ├─ entity（系统管理相关实体）
│  │  │  │     │  ├─ query（系统管理相关查询条件）
│  │  │  │     │  ├─ req（系统管理相关请求参数（Request））
│  │  │  │     │  └─ resp（系统管理相关响应参数（Response））
│  │  │  │     ├─ enums（系统管理相关枚举）
│  │  │  │     ├─ constant（系统管理相关常量）
│  │  │  │     ├─ util（系统管理相关工具类）
│  │  │  │     ├─ validation（系统管理相关参数校验工具类）
│  │  │  │     ├─ container（系统管理相关 Crane4j 数据填充容器配置）
│  │  │  │     └─ config（系统管理相关配置）
│  │  │  └─ resources
│  │  │     └─ mapper（系统管理相关 Mapper XML 文件目录）
│  │  └─ test（测试相关代码目录）
│  └─ pom.xml
├─ continew-plugin（插件模块，存放能力开放、租户等扩展模块，后续会进行插件化改造）
│  ├─ continew-plugin-open（能力开放插件模块）
│  │  ├─ src
│  │  │  ├─ main/java/top/continew/admin/open
│  │  │  │  ├─ controller（能力开放相关 API）
│  │  │  │  ├─ service（能力开放相关业务接口及实现类）
│  │  │  │  ├─ mapper（能力开放相关 Mapper）
│  │  │  │  ├─ model（能力开放相关模型）
│  │  │  │  │  ├─ entity（能力开放相关实体）
│  │  │  │  │  ├─ query（能力开放相关查询条件）
│  │  │  │  │  ├─ req（能力开放相关请求参数（Request））
│  │  │  │  │  └─ resp（能力开放相关响应参数（Response））
│  │  │  │  ├─ util（能力开放相关工具类）
│  │  │  │  ├─ handler（能力开放相关处理器）
│  │  │  │  ├─ sign（能力开放相关 API 参数签名算法）
│  │  │  │  └─ config（能力开放相关配置）
│  │  │  └─ test（测试相关代码目录）
│  │  └─ pom.xml
│  ├─ continew-plugin-tenant（租户插件模块）
│  │  ├─ src
│  │  │  ├─ main/java/top/continew/admin/tenant
│  │  │  │  ├─ api（租户相关公共业务 API 实现）
│  │  │  │  ├─ controller（租户相关 API）
│  │  │  │  ├─ service（租户相关业务接口及实现类）
│  │  │  │  ├─ mapper（租户相关 Mapper）
│  │  │  │  ├─ model（租户相关模型）
│  │  │  │  │  ├─ entity（租户相关实体）
│  │  │  │  │  ├─ query（租户相关查询条件）
│  │  │  │  │  ├─ req（租户相关请求参数（Request））
│  │  │  │  │  └─ resp（租户相关响应参数（Response））
│  │  │  │  ├─ enums（租户相关枚举）
│  │  │  │  ├─ constant（租户相关常量类）
│  │  │  │  ├─ util（租户相关工具类）
│  │  │  │  └─ config（租户相关配置）
│  │  │  └─ test（测试相关代码目录）
│  │  └─ pom.xml
│  ├─ continew-plugin-schedule（任务调度插件模块）
│  │  ├─ src
│  │  │  ├─ main/java/top/continew/admin/schedule
│  │  │  │  ├─ controller（任务调度相关 API）
│  │  │  │  ├─ service（代码生成器相关业务接口及实现类）
│  │  │  │  ├─ api（任务调度中心相关 Feign API）
│  │  │  │  ├─ model（任务调度相关模型）
│  │  │  │  │  ├─ query（任务调度相关查询条件）
│  │  │  │  │  ├─ req（任务调度相关请求参数（Request））
│  │  │  │  │  └─ resp（任务调度相关响应参数（Response））
│  │  │  │  ├─ enums（任务调度相关枚举）
│  │  │  │  ├─ constant（任务调度相关常量类）
│  │  │  │  ├─ exception（任务调度相关异常）
│  │  │  │  ├─ annotation（任务调度相关注解）
│  │  │  │  └─ config（任务调度相关配置）
│  │  │  └─ test（测试相关代码目录）
│  │  └─ pom.xml
│  ├─ continew-plugin-generator（代码生成器插件模块）
│  │  ├─ src
│  │  │  ├─ main
│  │  │  │  ├─ java/top/continew/admin/generator
│  │  │  │  │  ├─ controller（代码生成器相关 API）
│  │  │  │  │  ├─ service（代码生成器相关业务接口及实现类）
│  │  │  │  │  ├─ mapper（代码生成器相关 Mapper）
│  │  │  │  │  ├─ model（代码生成器相关模型）
│  │  │  │  │  │  ├─ entity（代码生成器相关实体）
│  │  │  │  │  │  ├─ query（代码生成器相关查询条件）
│  │  │  │  │  │  ├─ req（代码生成器相关请求参数（Request））
│  │  │  │  │  │  └─ resp（代码生成器相关响应参数（Response））
│  │  │  │  │  ├─ enums（代码生成器相关枚举）
│  │  │  │  │  └─ config（代码生成器相关配置）
│  │  │  │  └─ resources
│  │  │  │     └─ templates（代码生成相关模板目录）
│  │  │  │       ├─ backend（后端模板目录）
│  │  │  │       └─ frontend（前端模板目录）
│  │  │  └─ test（测试相关代码目录）
│  │  └─ pom.xml
│  └─ pom.xml
├─ continew-common（公共模块，存放公共工具类，公共配置等）
│  ├─ src
│  │  ├─ main/java/top/continew/admin/common
│  │  │  ├─ api（公共业务 API）
│  │  │  ├─ base（公共基类）
│  │  │  │  ├─ controller（控制器基类）
│  │  │  │  ├─ mapper（Mapper 接口基类）
│  │  │  │  ├─ model（公共模型）
│  │  │  │  │  ├─ entity（实体基类）
│  │  │  │  │  └─ resp（列表、详情响应基类）
│  │  │  │  └─ service（业务接口及实现基类）
│  │  │  ├─ model（公共模型）
│  │  │  │  ├─ dto（公共数据传输对象（DTO））
│  │  │  │  └─ req（公共请求参数（Request））
│  │  │  ├─ context（公共上下文）
│  │  │  ├─ enums（公共枚举）
│  │  │  ├─ constant（公共常量类）
│  │  │  ├─ util（公共工具类）
│  │  │  └─ config（公共配置）
│  │  │    ├─ crud（CRUD 配置）
│  │  │    ├─ mybatis（MyBatis Plus 配置）
│  │  │    ├─ websocket（Websocket 配置）
│  │  │    ├─ doc（接口文档配置）
│  │  │    ├─ excel（Excel 配置）
│  │  │    └─ exception（全局异常处理）
│  │  └─ test（测试相关代码目录）
│  └─ pom.xml
├─ continew-extension（扩展模块）
│  ├─ continew-extension-schedule-server（任务调度服务端模块，实际开发时如果是公司统一提供环境，可直接删除本模块）
│  │  ├─ src
│  │  │  ├─ main
│  │  │  │  ├─ java/top/continew/admin/extension/schedule
│  │  │  │  │  └─ ScheduleServerApplication.java（任务调度服务端启动程序）
│  │  │  │  └─ resources
│  │  │  │     ├─ config（核心配置目录）
│  │  │  │     │  ├─ application-dev.yml（开发环境配置文件）
│  │  │  │     │  ├─ application-prod.yml（生产环境配置文件）
│  │  │  │     │  └─ application.yml（通用配置文件）
│  │  │  │     ├─ db/changelog（Liquibase 数据脚本配置目录）
│  │  │  │     │  ├─ mysql（MySQL 数据库初始 SQL 脚本目录）
│  │  │  │     │  ├─ postgresql（PostgreSQL 数据库初始 SQL 脚本目录）
│  │  │  │     │  └─ db.changelog-master.yaml（Liquibase 变更记录文件）
│  │  │  │     └─ logback-spring.xml（日志配置文件）
│  │  │  └─ test（测试相关代码目录）
│  │  └─ pom.xml
│  └─ pom.xml
├─ .github（GitHub 相关配置目录，实际开发时直接删除）
├─ .idea
│  └─ icon.png（IDEA 项目图标，实际开发时直接删除）
├─ .image（截图目录，实际开发时直接删除）
├─ .style（代码格式、License文件头相关配置目录，实际开发时根据需要取舍，删除时注意删除 /pom.xml 中的 spotless 插件配置）
├─ .gitignore（Git 忽略文件相关配置文件）
├─ docker（项目部署相关配置目录，实际开发时可备份后直接删除）
├─ LICENSE（开源协议文件）
├─ CHANGELOG.md（更新日志文件，实际开发时直接删除）
├─ README.md（项目 README 文件，实际开发时替换为真实内容）
├─ lombok.config（Lombok 全局配置文件）
└─ pom.xml（包含版本锁定及全局插件相关配置）
```

## 参与贡献

ContiNew（Continue New）系列项目致力于通过持续迭代，为开发者提供舒适的开发体验。作为开源社区，我们的初衷是希望通过开源协作模式，提升技术透明度、放大集体智慧、共创优秀实践，源源不断地为企业级项目开发提供助力。

我们诚挚邀请广大社区用户为 ContiNew 项目贡献力量，包括但不限于 Issue 排查、测试验证、代码开发与重构等。每一份贡献，都是推动项目进步的重要力量（请查阅 [贡献指南](https://continew.top/about/contributing.html)）。欢迎各位感兴趣的小伙伴儿，[添加微信](https://continew.top/discussion.html) 讨论或认领任务。

### 分支说明

ContiNew 系列项目采用清晰的分支策略，确保开发与维护有序进行。提交 PR 前，请确认目标分支是否处于活跃维护状态，版本支持情况请查看 [更新日志#版本支持](https://continew.top/docs/admin/changelog/)。

| 分支  | 说明                                                         |
| ----- | ------------------------------------------------------------ |
| dev   | 开发分支，用于下个大版本的 SNAPSHOT 开发，接受新功能或功能优化 PR |
| x.x.x | 维护分支，用于特定版本（如 vx.x.x）的 bug 修复，仅接受已有功能的修复 PR，不接受新功能 |

### 流程步骤

若您希望提交新功能或优化现有代码，请遵循以下步骤：

1. 在开源平台上将项目 fork 到您的个人仓库
2. 将 fork 的项目克隆到本地开发环境
3. 基于当前维护的分支（如 dev）创建新分支（如 feat/newFeature），请勿直接修改源分支（源分支仅做同步 ContiNew 最新代码用）
4. 在新分支上进行代码修改，完成后提交并 push 到您的远程仓库
5. 在开源平台上创建 pull request (PR)，选择正确的源分支和目标分支，按模板填写说明信息（参考 [已合并的 PR](https://github.com/continew-org/continew-admin-ui/pulls?q=is%3Apr+is%3Amerged) 可提高合并率）
6. 提交 PR 后，系统会提示签署 CLA（贡献者协议）。请确保 commit 使用的邮箱与平台绑定邮箱一致（如果不一致，可以在本地通过 `git reset --soft HEAD~1` 回退，然后使用正确邮箱重新提交，最后 `git push -f` 即可，不需要重新创建 PR），然后使用该邮箱签署即可
7. 耐心等待维护者审核并合并您的 PR（建议通过交流群进行快捷沟通）
8. PR 合并后，下次贡献前请先同步最新代码，再重复步骤 3 开始

> [!IMPORTANT]
> 为了确保项目质量和协作效率，请注意以下事项：
>
> 1. 代码和配置文件请参考已有风格，遵循清晰的结构与命名规范，提供完善的注释（包括接口文档和参数示例），后端代码请符合阿里巴巴 <a href="https://github.com/continew-org/continew-admin/blob/dev/.style/Java%E5%BC%80%E5%8F%91%E6%89%8B%E5%86%8C(%E9%BB%84%E5%B1%B1%E7%89%88).pdf" target="_blank">《Java开发手册(黄山版)》</a> 中的代码规范
> 2. 提交后端代码前请关闭所有代码窗口，执行 `mvn compile` 命令进行代码格式化（ContiNew 项目后端编译时会自动执行插件进行代码格式修正）。编译通过后请勿再次打开代码窗口，避免不同 IDE 配置导致的格式差异 
> 3. 提交时，请按照 [Angular 提交规范](https://github.com/conventional-changelog/conventional-changelog/tree/master/packages/conventional-changelog-angular) 编写 commit message（参考已有风格）

## 反馈交流

感谢您对 ContiNew 开源项目的关注与支持！我们非常重视每一位用户的反馈和建议，这是推动项目不断进步的动力。 欢迎扫描下方二维码加入我们的官方交流群，与项目维护团队及其他大佬用户实时交流探讨。

- 与项目核心团队直接沟通，获取第一手项目动态
- 解决使用过程中遇到的问题，分享经验心得
- 参与功能讨论和需求收集，影响项目未来发展
- 结识志同道合的技术爱好者，扩展人脉圈

<div align="left">
  <img src=".image/qrcode.jpg" alt="二维码" height="230px" />
</div>

## 鸣谢

### 鸣谢

感谢参与贡献的每一位小伙伴🥰

<a href="https://github.com/continew-org/continew-admin/graphs/contributors">
  <img src="https://opencollective.com/continew-admin/contributors.svg?width=890&button=false" alt="contributors" />
</a>

### 特别鸣谢

- 感谢 <a href="https://www.jetbrains.com/" target="_blank">JetBrains</a> 提供的 <a href="https://jb.gg/OpenSourceSupport" target="_blank">非商业开源软件开发授权</a> 
- 感谢 <a href="https://github.com/baomidou/mybatis-plus" target="_blank">MyBatis Plus</a>、<a href="https://github.com/dromara/sa-token" target="_blank">Sa-Token</a> 、<a href="https://github.com/alibaba/jetcache" target="_blank">JetCache</a>、<a href="https://github.com/opengoofy/crane4j" target="_blank">Crane4j</a>、<a href="https://nextdoc4j.top/" target="_blank">NextDoc4j</a>、<a href="https://github.com/dromara/hutool" target="_blank">Hutool</a> 等开源组件作者为国内开源世界作出的贡献
- 感谢项目使用或未使用到的每一款开源组件，致敬各位开源先驱 :fire:

## License

- 遵循 <a href="https://github.com/continew-org/continew-admin/blob/dev/LICENSE" target="_blank">Apache-2.0</a> 开源许可协议
- Copyright © 2022-present <a href="https://charles7c.top" target="_blank">Charles7c</a>

## GitHub Star 趋势

![GitHub Star 趋势](https://starchart.cc/charles7c/continew-admin.svg)