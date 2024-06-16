## [v3.1.0](https://github.com/Charles7c/continew-admin/compare/v3.0.1...v3.1.0) (2024-06-16)

### ✨ 新特性

* 系统配置新增安全设置功能，支持多种密码策略配置，例如：有效期、密码重复使用次数、密码错误锁定等 (GitHub#61) ([1de2a8f](https://github.com/Charles7c/continew-admin/commit/1de2a8f2dcf01ef8eeb7e2d662c09af00b38ffb1)) ([90ecaab](https://github.com/Charles7c/continew-admin/commit/90ecaab63241b8d351ebaffa4faece8c609882b1)) ([3994142](https://github.com/Charles7c/continew-admin/commit/3994142ace6cd6ce9d094b8a4ceed080a2b2ec33)) ([1427c13](https://github.com/Charles7c/continew-admin/commit/1427c13b7a0b4e0f569ccc20a3172ad748c98e14)) ([5f5fee6](https://github.com/Charles7c/continew-admin/commit/5f5fee63f8e584ffd9542bf132ee724762187d83)) ([c1e9d31](https://github.com/Charles7c/continew-admin/commit/c1e9d318e0643c0854a848425955948d05b158d3)) ([48d0f47](https://github.com/Charles7c/continew-admin/commit/48d0f476149ff7bc30fe05fe3dec84e5a947cf19))
* 图片文件支持缩略图 (GitHub#63) ([d320c95](https://github.com/Charles7c/continew-admin/commit/d320c9596a321dce1028ac36d23bcd04535e87a6)) ([d44fb3a](https://github.com/Charles7c/continew-admin/commit/d44fb3a681370b5763f028cc761fbb5fc94a104c))
* 在线用户增加最后活跃时间显示 ([926497a](https://github.com/Charles7c/continew-admin/commit/926497a18acbb6ef170de9f68bd97e025e212f3e))
* 新增 WebSocket 消息通知，站内信重新上线 (GitHub#67) ([9970c46](https://github.com/Charles7c/continew-admin/commit/9970c461cce5c710f1f8479d4bdd258e65558256)) ([94168e2](https://github.com/Charles7c/continew-admin/commit/94168e246f75364605b6fbdb82c9438e2b959c61)) ([5abdb8d](https://github.com/Charles7c/continew-admin/commit/5abdb8d86161e7bd03ace14b3c899f6ad13020e2))
* 文件上传按日期拆分目录 (GitHub#68) ([08aa085](https://github.com/Charles7c/continew-admin/commit/08aa08550589876a821a37d56a5fae8867292978))
* 代码生成增加了 TREE_SELECT/CHECK_GROUP/INPUT_NUMBER/INPUT_PASSWORD控件 (Gitee#17) ([8632b22](https://github.com/Charles7c/continew-admin/commit/8632b22bd5b6d43e31b6aeac930836464849441b)) ([cf18c10](https://github.com/Charles7c/continew-admin/commit/cf18c1046b77c9f38d28b0e6608d345df3bbd5a9))
* 系统参数新增根据类别查询方法 ([694cbb2](https://github.com/Charles7c/continew-admin/commit/694cbb2850c05f7eb35fb982530a6c204f1f64b0))
* 支持动态邮件 ([1dbb339](https://github.com/Charles7c/continew-admin/commit/1dbb33935a05e9fde749191e34682e632c8e1e63))

### 💎 功能优化

- 优化部分命名 ([a3cf39f](https://github.com/Charles7c/continew-admin/commit/a3cf39f9f8611f7f34dd9f55166d0fe75b72145b))
- 优化代码生成预览 (Gitee#14) ([ad7412f](https://github.com/Charles7c/continew-admin/commit/ad7412f9cbee5d851d060d8e9d717b2553d3d4cb))
- 优化个人中心部分参数命名 ([61dd3a4](https://github.com/Charles7c/continew-admin/commit/61dd3a4c3aef2d4a873a1d4de13bc1962179eeb1))
- 根据前端最新 ESLint 配置优化代码生成模板 ([044b4b6](https://github.com/Charles7c/continew-admin/commit/044b4b669aa2b77b3f5ac27095202e57045fd10d))
- 优化代码生成模板 ([3ddcdf0](https://github.com/Charles7c/continew-admin/commit/3ddcdf0f67d12250da4b7f6f6ff3d63a880e6d4c)) ([6396e9a](https://github.com/Charles7c/continew-admin/commit/6396e9a7364bf899023c2399083b43c67efb30cf)) ([2fb4001](https://github.com/Charles7c/continew-admin/commit/2fb40015c1ca2bc58b66891e2cfbfac70b029016))
- 使用 Crane4j 优化在线用户数据填充 ([cb81135](https://github.com/Charles7c/continew-admin/commit/cb811350f36364fcc85355c081b8b60c7d5bfb2a))
- 用户角色名称调整为角色名称列表返回，并全局优化 Crane4j 组件的使用方式 ([857a1c9](https://github.com/Charles7c/continew-admin/commit/857a1c90838c8305f916af9dccfd4ca847ca8c66)) ([0b76d5c](https://github.com/Charles7c/continew-admin/commit/0b76d5ca33f5900862d48321e8384ee8ded6ae4e))
- 优化部分方法排序 ([651cc8a](https://github.com/Charles7c/continew-admin/commit/651cc8ae71bd3f544bd41adac0ef7044011300a7))
- 字典管理分页查询接口 => 查询列表接口 ([b13d0e9](https://github.com/Charles7c/continew-admin/commit/b13d0e9ee530fc1a8b382e08ead065989f2b0e7f))
- 移除部门响应信息中的 getDisabled 方法 ([659144a](https://github.com/Charles7c/continew-admin/commit/659144afdaf5d1b4667437053a770cf39681cdd7))
- 文件管理存储路径改为相对路径 (GitHub#69) ([8854f20](https://github.com/Charles7c/continew-admin/commit/8854f20ce90f6a1c379ac81a27bb40784a838095))
- 查询文件列表增加存储名称信息返回 ([69bc1e5](https://github.com/Charles7c/continew-admin/commit/69bc1e52e122c14a695d2e37589d36cbb64de8a4))
- 系统参数表结构新增ID、类别字段 ([45396f2](https://github.com/Charles7c/continew-admin/commit/45396f2dc23b4de47912c1d77b05711c839672ce))
- 优化公告状态判断 ([a07aedb](https://github.com/Charles7c/continew-admin/commit/a07aedbf35e32f3d09d2e9ed8857b9a9d2e2e13a))
- 重构系统参数相关接口 ([6d0060b](https://github.com/Charles7c/continew-admin/commit/6d0060b21c374afd9880e57490345a140187a7cd))
- 优化用户及部门查询 ([448f9a0](https://github.com/Charles7c/continew-admin/commit/448f9a0a819d6816a5ae3ada2e07690a5f70f7df))
- 用户头像改为Base64存储 ([969216d](https://github.com/Charles7c/continew-admin/commit/969216d7c67332eae826c11233c8745d9d3ad81c)) ([513ea83](https://github.com/Charles7c/continew-admin/commit/513ea83152708194348afeb93d31aed1a9914e57)) ([7a6cafc](https://github.com/Charles7c/continew-admin/commit/7a6cafc6e4ff6914fa62a870538b51759af866a8))
- 优化配置文件 ([5b3d4f5](https://github.com/Charles7c/continew-admin/commit/5b3d4f57788e30b62a1e1af9c2620a4cc8659bfe))
- 优化登录 Helper ([afbd619](https://github.com/Charles7c/continew-admin/commit/afbd619d098ff3d70fc786db9594cebf03c07e10))
- 重构查询参数及字典接口 ([1d60213](https://github.com/Charles7c/continew-admin/commit/1d602134377fc13b062981476b8c17947b36006d))
- 重构查询角色字典接口 ([1e73d06](https://github.com/Charles7c/continew-admin/commit/1e73d06a972d380bbed253eaffd806d8e0698525))
- 使用 CompletableFuture 实现异步加载用户权限、角色代码和角色信息，以提高登录时的性能和响应速度 ([d5f3c74](https://github.com/charles7c/continew-admin/commit/d5f3c7417ad7b1178b5a53da3fd6f3cb7cd3b19a))


### 🐛 问题修复

- 补充查询文件资源统计权限校验注解 ([60cbf04](https://github.com/Charles7c/continew-admin/commit/60cbf0402a350d05a09abec57ba94c7758b46499))
- Postgresql startup script fixes (GitHub#60) ([8caad16](https://github.com/Charles7c/continew-admin/commit/8caad16ef226e473a81f79b82485c3d03ded7a42))
- 修复初始菜单数据错误 ([f062797](https://github.com/Charles7c/continew-admin/commit/f062797629bd7fc220ceae1e44859146ef4a14ff))
- 字典编码、存储编码及类型、菜单类型不允许修改 ([79d0101](https://github.com/Charles7c/continew-admin/commit/79d0101e5eb1baa86979b6a6d3c584a2a483320f))
- 修复行为验证码接口请求次数限制 ([573e634](https://github.com/Charles7c/continew-admin/commit/573e634b433c473551244418e695a34bbd3fa675))
- 修复导出用户报错 ([655a695](https://github.com/Charles7c/continew-admin/commit/655a695753d12db624fbf2afaf10d38f67241e31))
- 移除部门名称错误正则 ([0285874](https://github.com/Charles7c/continew-admin/commit/0285874540c0cadc8aae74077f6368b6e7977c35))
- 修复插入第三方登录用户时报错 ([0cfc7a5](https://github.com/Charles7c/continew-admin/commit/0cfc7a5c80c3558028c7225fc459ecb07149ab0d))
- 修复更新手机号、邮箱未加密的问题 ([485d708](https://github.com/Charles7c/continew-admin/commit/485d708cd45df922ec8e601b7bd7344e9ebd9299)) ([e6d7205](https://github.com/Charles7c/continew-admin/commit/e6d720571d3273f27351e81a19fdd97b9b4336f6))

### 📦 依赖升级

- ContiNew Starter 2.0.0 => 2.1.0 (更多依赖升级详情，请查看 ContiNew Starter [更新日志](https://github.com/Charles7c/continew-starter/blob/dev/CHANGELOG.md))

## [v3.0.1](https://github.com/Charles7c/continew-admin/compare/v3.0.0...v3.0.1) (2024-05-03)

### ✨ 新特性

* 新增验证码超时显示效果，超时后显示已过期请刷新 (GitHub#56) ([4c6a7fb](https://github.com/Charles7c/continew-admin/commit/4c6a7fb91ad195b86d776f8aef6aef81d07b2eb1))
* 文件管理增加资源统计，统计总存储量、各类型文件存储占用 (GitHub#58) ([15c966f](https://github.com/Charles7c/continew-admin/commit/15c966f7bb255db3edea249f8d3354324cbdbf5b))

### 💎 功能优化

- 获取图片验证码 URL /img => /image ([9a1a472](https://github.com/Charles7c/continew-admin/commit/9a1a472ec996362cb918e79b9ce37bfa2639a10b))
- 移除对部分 API 重复的权限校验 ([53eaef9](https://github.com/Charles7c/continew-admin/commit/53eaef9fbdfd6d0866a3d5e424d783e2e7bc0e17))
- 优化代码生成模板 ([dc92731](https://github.com/Charles7c/continew-admin/commit/dc9273132dc8e266f2d44c834b9c2733256afdfe)) ([def831f](https://github.com/Charles7c/continew-admin/commit/def831f2dca0703f5ef8b84b0e695a32b171461d))


### 🐛 问题修复

- 修复查询用户邮箱、手机号时未自动加密导致的错误 ([faa56d1](https://github.com/Charles7c/continew-admin/commit/faa56d16b92cbdb8f7e16c8b43c2916ae692d881))
- 修复根据部门查询用户列表数据错误 ([42ac82e](https://github.com/Charles7c/continew-admin/commit/42ac82e7ceef9336741c2514470c0db36ab7075e))
- 修复文件类型处理错误 ([9b60e24](https://github.com/Charles7c/continew-admin/commit/9b60e24364bfb4cc7cd9996a43579a062197cdf3))

## [v3.0.0](https://github.com/Charles7c/continew-admin/compare/v2.5.0...v3.0.0) (2024-04-27)

### ✨ 新特性

* 系统日志新增导出 API ([bd0f40c](https://github.com/Charles7c/continew-admin/commit/bd0f40c6ad397174baf80b04923ef1e94ff28e3c))
* 适配 3.0 前端菜单，并梳理菜单数据
* 适配 3.0 前端代码生成模板，代码预览及生成 ([3dbe72f](https://github.com/Charles7c/continew-admin/commit/3dbe72fd570c44b32599d869abd30331137a6c7d))

### 💎 功能优化

- 重构日志管理相关接口 ([7793f82](https://github.com/Charles7c/continew-admin/commit/7793f82009bcdb5fcdfe5e91daab211ab1705bf7))
- 优化部门管理相关 API，合并 DeptResp 及 DeptDetailResp ([a2cf072](https://github.com/Charles7c/continew-admin/commit/a2cf072609ac33543605ecbb5f8e498237bc3d91))
- 优化存储管理相关 API，合并 StorageResp 及 StorageDetailResp ([f7b5a4f](https://github.com/Charles7c/continew-admin/commit/f7b5a4ff8dd93f444c00d103b0609ae81e0dd70c))
- 优化字典管理相关 API ([9ec5945](https://github.com/Charles7c/continew-admin/commit/9ec594509f2d4b31f46e3aca66d65d139dc8b94f))
- 移除部门、角色、菜单、用户、存储的状态默认值 ([bd5ede2](https://github.com/Charles7c/continew-admin/commit/bd5ede2e2956057376b930ecfed88ca44437cbc1))
- 代码生成新增 MySQL json 数据类型映射 ([fe57350](https://github.com/Charles7c/continew-admin/commit/fe5735090d94f5d900d79142e5e42ba2db9c0249))
- 优化角色管理相关 API，角色编码不允许修改 ([df59cee](https://github.com/Charles7c/continew-admin/commit/df59cee98565f9f45b04d16533888be39c3d7a6f))
- 优化用户管理相关 API ([5269608](https://github.com/Charles7c/continew-admin/commit/5269608c61b1f5a6a9f61cd45b349f28db714232))
- 文件管理查询 API 调整为分页查询 ([f8bea90](https://github.com/Charles7c/continew-admin/commit/f8bea901938aec0f0ac21c63179c5bde2a0965a7))
- 移除 Qodana 扫描 ([d88581f](https://github.com/Charles7c/continew-admin/commit/d88581f939afb0caa3589c8ee76c9b296bb9997e))
- 移除菜单导出接口 ([4363c91](https://github.com/Charles7c/continew-admin/commit/4363c91872e6d83e139b22c95a0d7e83183d8f69))
- 优化系统日志、在线用户、存储管理、部门管理相关代码 ([a2e4f9a](https://github.com/Charles7c/continew-admin/commit/a2e4f9a28b744e269c46dc66c60311bb939021a7))
- 优化查询参数字典 API 地址 ([79a3de8](https://github.com/Charles7c/continew-admin/commit/79a3de8971c613277bdcea79463b6f06959e7b85))
- 移除角色状态字段 ([e89ba7d](https://github.com/Charles7c/continew-admin/commit/e89ba7d5cd793e20c3562c7bd1e4655ed1e5a2a3))


### 🐛 问题修复

- 使用字典时，仅查询启用状态字典 ([17c795f](https://github.com/Charles7c/continew-admin/commit/17c795fedef5b6801f2053d97b9d78d067775ca1))
- 获取 Authorization 请求头内容兼容小写请求头场景 ([e68c445](https://github.com/Charles7c/continew-admin/commit/e68c4455a8af1b4d7a25cd63f9fc9e5aabb441ab))
- 修复查询用户权限存在空值的问题 ([fce4a56](https://github.com/Charles7c/continew-admin/commit/fce4a566d7204791650153f0a5507a5d05d2d6c3))
- 存储管理 S3 存储功能修复 (GitHub#51) ([f71c4c2](https://github.com/Charles7c/continew-admin/commit/f71c4c226ffd7c27f6726873be6af125affaf148))
- 修复 sys_role_menu 表初始数据错误 ([70ed667](https://github.com/Charles7c/continew-admin/commit/70ed667c16388093204eecd97e4914076c62d1ff))
- 修复用户管理/角色管理编辑及状态变更问题 (GitHub#53) ([abf1e65](https://github.com/Charles7c/continew-admin/commit/abf1e651e9782a6f7bf2a896018de17130038c57))
- 修复Failed to submit a listener notification task. Event loop shut down? 问题，开发时表现为需要点击两次才能关闭程序 ([f5ab22e](https://github.com/Charles7c/continew-admin/commit/f5ab22eedf594cee43592a2f29409ee9c33a88d3))

### 💥 破坏性变更

- 适配 continew-starter 2.0.0，top.charles7c.continew.starter => top.continew.starter ([f5ab22e](https://github.com/Charles7c/continew-admin/commit/f5ab22eedf594cee43592a2f29409ee9c33a88d3))
- 移除 monitor 模块 ([b6206a3](https://github.com/Charles7c/continew-admin/commit/b6206a334671894306043f86ec07d7c045cd757d))
- top.charles7c.continew.admin => top.continew.admin ([08eeabc](https://github.com/Charles7c/continew-admin/commit/08eeabc47d58db3cfc861a3a527e52bf89f6183b))
- 公告管理 Announcement => Notice ([dbe93df](https://github.com/Charles7c/continew-admin/commit/dbe93df8bcec0b7dfb24fbd92f35928a3156f4e5))

### 📦 依赖升级

- ContiNew Starter 1.5.1 => 2.0.0 (更多依赖升级详情，请查看 ContiNew Starter [更新日志](https://github.com/Charles7c/continew-starter/blob/dev/CHANGELOG.md))

## [v2.5.0](https://github.com/Charles7c/continew-admin/compare/v2.4.0...v2.5.0) (2024-03-23)

### ✨ 新特性

* 新增 PostgreSQL 数据源配置示例 ([ee48c80](https://github.com/Charles7c/continew-admin/commit/ee48c80cd10a4c4546d1cb24f1f4716bb2ac08ea))
* 新增 PostgreSQL 部署脚本 ([3129e0a](https://github.com/Charles7c/continew-admin/commit/3129e0a6dcbd809f0013fbf6c53ad029ae9f7a0e))
* 新增 PostgreSQL 初始 SQL 脚本 ([33b8102](https://github.com/Charles7c/continew-admin/commit/33b81029df0b51058b3525b4317b51a2351319dc))
* 新增代码生成器插件模块（后续会改造为独立插件） ([87829d3](https://github.com/Charles7c/continew-admin/commit/87829d3ce8ab5a35091800900f7d7708f15ed9c2))
* 代码生成同步最新数据表结构支持同步排序 ([89546de](https://github.com/Charles7c/continew-admin/commit/89546deced78f83daca7ac0ba2e7d3d8cd101d0c))
* 新增代码批量生成功能  ([Gitee PR#12](https://gitee.com/continew/continew-admin/pulls/12)) ([040f137](https://github.com/Charles7c/continew-admin/commit/040f137934130451700bc28aeabbced30970c5f6))

### 💎 功能优化

- 移除 ` 符号的使用，保持数据库无关性 ([d6b07bd](https://github.com/Charles7c/continew-admin/commit/d6b07bd6d1b1f9077a7571702b58c5e9c782b446))
- 优化字符串模板方法 API 使用 ([0f39384](https://github.com/Charles7c/continew-admin/commit/0f393845a19432e7c965e811c96774694f4d2372))
- 调整部分 SQL 语句，以兼容 PostgreSQL 数据库 ([9f5049b](https://github.com/Charles7c/continew-admin/commit/9f5049bf26c557738867dfe833261d60d071d4a8)) ([bf60d48](https://github.com/Charles7c/continew-admin/commit/bf60d48d3a53dd5d73a78e73b6b230e3271ec3de))
- 新增插件仓库配置 ([0439252](https://github.com/Charles7c/continew-admin/commit/04392524ac13c2096b549f99d0391fa1d375ca31))
- 优化部分接口响应格式为 kv 格式 ([b40d872](https://github.com/Charles7c/continew-admin/commit/b40d872bc4b8dd30ad952d639158619b43cef999))
- 适配 Crane4j 条件注解 ([bf00747](https://github.com/Charles7c/continew-admin/commit/bf007470b2362159309ff8231a2f0ad180cfc947))
- 重构代码生成配置 ([7031a51](https://github.com/Charles7c/continew-admin/commit/7031a51cd4d7072d4da841736678bb81b2123e9d))
- 重构代码生成功能，由指定路径生成模式调整为下载模式，更方便复杂场景 ([df0c0dd](https://github.com/Charles7c/continew-admin/commit/df0c0dd7dcf39620abaf21bd450620ec3fffcf37))


### 🐛 问题修复

- 修复 MySQL 初始 SQL 脚本数据错误 ([49d6bd6](https://github.com/Charles7c/continew-admin/commit/49d6bd6874b3df66fd2e2051ea273cb43cb7b4f6))
- 修复参数缓存未及时过期的问题 ([976e9c4](https://github.com/Charles7c/continew-admin/commit/976e9c43df5926c533723a75222c59fde05e122e))
- 修复代码生成 text 类型数据的长度校验时，数值显示为 65,535 的问题 ([8026f66](https://github.com/Charles7c/continew-admin/commit/8026f660c7af7bba6d4caaf31535a890e5b40a96))

### 💥 破坏性变更

- 调整 liquibase 目录结构，更适合开源类项目适配多种数据库脚本场景 ([1ca48a6](https://github.com/Charles7c/continew-admin/commit/1ca48a6620cff62f3648cc28042843163589e150))
- 适配 ContiNew Starter 日志及数据库工具的包结构优化 ([3405868](https://github.com/Charles7c/continew-admin/commit/3405868c7f042beafb77a7407a388a40b9a75466))
- 适配 ContiNew Starter Query 组件的包结构优化 ([6be1b6c](https://github.com/Charles7c/continew-admin/commit/6be1b6cfb1e7fef4422b8c38e6073a435ebae5c2))

### 📦 依赖升级

- ContiNew Starter 1.4.0 => 1.5.1 (更多依赖升级详情，请查看 ContiNew Starter [更新日志](https://github.com/Charles7c/continew-starter/blob/dev/CHANGELOG.md))

## [v2.4.0](https://github.com/Charles7c/continew-admin/compare/v2.3.0...v2.4.0) (2024-02-16)

### ✨ 新特性

* 集成 TLog（轻量级的分布式日志标记追踪神器） ([Gitee PR#10](https://gitee.com/continew/continew-admin/pulls/10))
* 系统日志新增 traceId 链路号记录，方便查看完整日志链路 ([860ca40](https://github.com/Charles7c/continew-admin/commit/860ca403c2c32cc6395c1608217bc9b6e7c18bd8))
* 取消用户默认密码，改为表单填写密码 ([3d77aa9](https://github.com/Charles7c/continew-admin/commit/3d77aa91ee32065b53d9c47a57c33d6d7e4efb0e))
* 适配 ContiNew Starter 加密模块（安全模块） ([6435175](https://github.com/Charles7c/continew-admin/commit/6435175dc3d853cb170270e39e8f1505adffeae5)) ([43da462](https://github.com/Charles7c/continew-admin/commit/43da462560e224ed92f239cb5af4db64dea51d18))
* 适配 ContiNew Starter 脱敏模块（安全模块） ([2109789](https://github.com/Charles7c/continew-admin/commit/2109789116d9ff18773d8afeb854d1dfc70b935a))

### 💎 功能优化

- 优化 API 文档分组配置 ([2df4cce](https://github.com/Charles7c/continew-admin/commit/2df4cceedd35b1c2c07bcbf38b5a157604a752c2))
- 优化 QueryTypeEnum 枚举值命名 ([9648cf6](https://github.com/Charles7c/continew-admin/commit/9648cf64a4679657f0e609f980805d274563aa53))
- 优化 Query 相关注解使用方式 ([15b1520](https://github.com/Charles7c/continew-admin/commit/15b152008c6ae8ab89704d83a969dcfbbb8b5b88))
- 新增 Qodana 扫描 ([f6a9581](https://github.com/Charles7c/continew-admin/commit/f6a9581adef87a8915639e6cb2d7c4d02315ebd0))
- 新增 SonarCloud 扫描 ([a154abd](https://github.com/Charles7c/continew-admin/commit/a154abde8a39cfecc421c79e01998274b944d2c1)) ([c03c082](https://github.com/Charles7c/continew-admin/commit/c03c082d2e2884962547633f5e98663088bd2c3b))
- 移除 Lombok 私有构造注解使用 ([a2420d3](https://github.com/Charles7c/continew-admin/commit/a2420d3f4b4652a1d9711f513b8fb22a56105141))
- 获取不到当前登录用户信息则抛出未登录异常 ([d972a44](https://github.com/Charles7c/continew-admin/commit/d972a4466a9e8a1a6e6375e4171a4790c2ba156e))
- 优化代码，解决 [Sonar](https://sonarcloud.io/organizations/charles7c/projects)、[Codacy](https://app.codacy.com/gh/Charles7c/continew-admin/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade)、[Qodana](https://qodana.cloud/organizations/pQDPD/teams/p5jqd/) 扫描问题，点击各链接查看对应实时质量分析报告（Codacy 已达到 A）
- 优化部署配置 ([b5d668e](https://github.com/Charles7c/continew-admin/commit/b5d668e014690d3f1a8a2bab0d0ad0039083e7bb))
- 使用密码编码器重构密码加密、密码判断等相关处理 ([594f7fd](https://github.com/Charles7c/continew-admin/commit/594f7fd042f1ff96a298f2e59ffdda112113cb51))
- 优化 SaToken 及图形验证码配置 ([70973db](https://github.com/Charles7c/continew-admin/commit/70973db71f2eed49c5878d69d8b93ff04b13a8b9))
- 优化图形验证码使用及部分配置 ([a50d857](https://github.com/Charles7c/continew-admin/commit/a50d857c41d164355d36ae5dfd14c6badbe06202))


### 🐛 问题修复

- 修复 API 响应内容类型错误 ([439f7c7](https://github.com/Charles7c/continew-admin/commit/439f7c7c58ee27ff56b5093df71bc902c46f48fa))

### 💥 破坏性变更

- 调整自增 ID 为分布式 ID ([4779887](https://github.com/Charles7c/continew-admin/commit/4779887751bd3a696e4d31294057e8c03d66eaf3))

### 📦 依赖升级

- ContiNew Starter 1.2.0 => 1.4.0 (更多依赖升级详情，请查看 ContiNew Starter [更新日志](https://github.com/Charles7c/continew-starter/blob/dev/CHANGELOG.md))

## [v2.3.0](https://github.com/Charles7c/continew-admin/compare/v2.2.0...v2.3.0) (2024-01-21)

### ✨ 新特性

* 代码生成 Request 实体时，针对字符串类型增加数据长度校验注解 ([ee82558](https://github.com/Charles7c/continew-admin/commit/ee8255876f618137f811e14ee305509e4e6466d0))
* 适配 Crane4j 数据填充组件，优化部分数据填充处理 ([d598408](https://github.com/Charles7c/continew-admin/commit/d5984087a306de31690e9a81d951bd831434a0c9)) ([a2411f7](https://github.com/Charles7c/continew-admin/commit/a2411f728a811910a0918668c0811e7df0345640)) ([7a3ccc2](https://github.com/Charles7c/continew-admin/commit/7a3ccc2dee8f7b938a91df52fd4903ce09e662e5))
* 移除 Spring Cache，适配 JetCache ([d4bb39d](https://github.com/Charles7c/continew-admin/commit/d4bb39d9b4483b7d7fed76b5f2b997538d86d719)) ([1b7aa9d](https://github.com/Charles7c/continew-admin/commit/1b7aa9db0c56af733f63f602988dba9e225fe445)) ([8596e47](https://github.com/Charles7c/continew-admin/commit/8596e47ed62e19083d6007448c5369d72fa4f2b6))

### 💎 功能优化

- 优化本地存储库注册 ([918e897](https://github.com/Charles7c/continew-admin/commit/918e897838628b24a160c3a8f3b3dea1eefd1883))
- 增加华为云镜像源仓库配置 ([16ee2b4](https://github.com/Charles7c/continew-admin/commit/16ee2b4b6fe2a663830972ed99d4e80ddf5a3593))
- 优化部分字段名称 ([e3e958b](https://github.com/Charles7c/continew-admin/commit/e3e958b419e1ea23fe146b255fce749050302f63))
- 调整代码生成前端 Vue 页面模板 ([7c34574](https://github.com/Charles7c/continew-admin/commit/7c345745aadc6272e5f1db674c757ff0f9cea604))
- 更新格式配置，优化全局代码格式 ([35e3123](https://github.com/Charles7c/continew-admin/commit/35e31233c531b68b761c73fc0daf7444843b4059))
- 优化配置文件格式 ([a8a4cad](https://github.com/Charles7c/continew-admin/commit/a8a4cad840b6d32fbb8d3df24b193f9e7c826d22))
- 使用钩子方法优化部分增、删、改处理 ([61c5724](https://github.com/Charles7c/continew-admin/commit/61c57242fa481d2668b1d1ac4ff4802c47fd07bc))
- 完善 flatten Maven 插件配置，以覆盖更多使用情况 ([657accd](https://github.com/Charles7c/continew-admin/commit/657accd8a595ab0c2e9ff4d00e49c569eae03123))
- 移除部分无用 Maven 配置 ([5db1f66](https://github.com/Charles7c/continew-admin/commit/5db1f669e0bc5022bcd2164757a0f82dfe8d6c30))
- 优化日志配置，滚动策略调整为基于日志文件大小和时间滚动 ([2fa8c25](https://github.com/Charles7c/continew-admin/commit/2fa8c254fc53cda3d33c56931569822e645dd902))

### 🐛 问题修复

- 完善代码生成前端路径配置校验 ([bee04d5](https://github.com/Charles7c/continew-admin/commit/bee04d5f363b6de88df5249b0fba85607978b303))

### 💥 破坏性变更

- 根据发展需要，拆分前端项目 continew-admin-ui 到独立仓库 ([4067eb9](https://github.com/Charles7c/continew-admin/commit/4067eb97bf344dec6ae718433b57bdb7d0b8d6cd))
- PageDataResp => PageResp ([d8c946e](https://github.com/Charles7c/continew-admin/commit/d8c946e8014d205c4fd3f38d1f04b3225faede7a))
- 适配 ContiNew Starter IService 接口，CRUD 查询详情方法不再检查是否存在 ([47a133a](https://github.com/Charles7c/continew-admin/commit/47a133a065b5c858b588bf77ad51bb9fc38d1222))
- 适配 ContiNew Starter CRUD 模块注解 ([7fa70e7](https://github.com/Charles7c/continew-admin/commit/7fa70e74070c7c0f487baa5098f85d7dfb808106))
- 调整部分类的所在包 ([8dc42c7](https://github.com/Charles7c/continew-admin/commit/8dc42c7a21e7422399b49690b28899df299e20c7)) ([6efe1ad](https://github.com/Charles7c/continew-admin/commit/6efe1ad6f416c52130b2380a699129e7dae29499))

### 📦 依赖升级

- ContiNew Starter 1.1.0 => 1.2.0 (更多依赖升级详情，请查看 ContiNew Starter [更新日志](https://github.com/Charles7c/continew-starter/blob/dev/CHANGELOG.md))

## [v2.2.0](https://github.com/Charles7c/continew-admin/compare/v2.1.0...v2.2.0) (2023-12-31)

### ✨ 新特性

* 发送短信验证码新增限流处理 ([e719d20](https://github.com/Charles7c/continew-admin/commit/e719d207fb76c82b584f2e1ac7210061dc71a89a))
* 代码生成新增生成预览功能 ([4017029](https://github.com/Charles7c/continew-admin/commit/401702972f30c4e556a2cf8d048f78fa9ee1c5ba)) ([505ba49](https://github.com/Charles7c/continew-admin/commit/505ba49a5304fb3e2ba655dea901cd5e3ea74673))
* 适配 ContiNew Starter 行为验证码，系统内所有短信发送新增前置行为验证码验证 ([Gitee PR#9](https://gitee.com/continew/continew-admin/pulls/9))
* 文件管理：提供文件上传、下载、预览（目前支持图片、音视频）、重命名、切换视图（列表、网格）等功能
* 存储库管理：提供文件存储库新增、编辑、删除、导出等功能

### 💎 功能优化

- 优化 API 文档配置 ([108f1c4](https://github.com/Charles7c/continew-admin/commit/108f1c4ae7b855ac0bab2d3fe028270472a8be71))
- 调整枚举配置值为大写 ([3ece42b](https://github.com/Charles7c/continew-admin/commit/3ece42b94e071ece87e6b4616f7817bf851ba28f))
- 优化由于 Mock 引起的导出报错提示 ([349899b](https://github.com/Charles7c/continew-admin/commit/349899b4fc9572450ca31d9a5e19268ce0b868a8))
- 优化查询访客地域分布信息接口 SQL ([4df887d](https://github.com/Charles7c/continew-admin/commit/4df887d82678ced0d30aa0c7a6f92edcac902052))
- 调整后端部分方法名 save => add ([45bd3e1](https://github.com/Charles7c/continew-admin/commit/45bd3e10b6ac6aecde41ff9484668e557a485b27))
- 优化系统日志详情 ([55effa3](https://github.com/Charles7c/continew-admin/commit/55effa36580a57ddedb688e2ce30bec45c761224)) ([99997c1](https://github.com/Charles7c/continew-admin/commit/99997c160eefc152a6f4e74bcd9c5ef6fc77a9c5))
- 移除部分方法中仅有单个非读操作的事务处理 ([b85d692](https://github.com/Charles7c/continew-admin/commit/b85d69298de1a6c48d15300bb9ff1b3ea569fdbd))
- 优化编译配置 ([ed8bb57](https://github.com/Charles7c/continew-admin/commit/ed8bb57fe24dfbe8f45b8f53370ebb79f1511268))
- 优化配置文件格式 ([3399bc8](https://github.com/Charles7c/continew-admin/commit/3399bc8dde0c8c8ac6d3e583ffbe299f7e6dd80b))

### 🐛 问题修复

- 修复代码生成相关错误 ([3fdc50d](https://github.com/Charles7c/continew-admin/commit/3fdc50d78ec50a878cec2b35c7d5028e741c42d7))
- 更新仪表盘帮助文档部分过期链接 ([ac42836](https://github.com/Charles7c/continew-admin/commit/ac4283679a847ed372db28aae1ea05fd791651b8))

### 💥 破坏性变更

- 适配 ContiNew Starter QueryTypeEnum 命名变更 ([97c273f](https://github.com/Charles7c/continew-admin/commit/97c273f99ecb038e041e3d39dbfacf326d49cc1b))
- 适配 ContiNew Starter Log HttpTracePro（日志模块） ([9bf0150](https://github.com/Charles7c/continew-admin/commit/9bf015059b96f41c29f05ecbf7612d611b3a98c3))
- 适配 ContiNew Starter 全局异常处理器 ([4ed4ddd](https://github.com/Charles7c/continew-admin/commit/4ed4ddd4f055cefe1f85482bd6b9ef760978691b))
- 适配 ContiNew Starter 数据权限解决方案（数据访问模块-MyBatis Plus） ([0849426](https://github.com/Charles7c/continew-admin/commit/084942630ab0e1846c1836b8dc4bf5b2c9a5b16e))
- 调整 IBaseEnum 所属包 ([e6c6e1c](https://github.com/Charles7c/continew-admin/commit/e6c6e1cb0e326c5f531ca5cb2e17a1e26efac7d9))
- 重构原有文件上传接口并优化配置文件配置格式 ([5e37025](https://github.com/Charles7c/continew-admin/commit/5e370254dd00deaab62438c5feb4de14192ad7e6))

### 📦 依赖升级

- ContiNew Starter 1.0.0 => 1.1.0 ([fc80921](https://github.com/Charles7c/continew-admin/commit/fc80921c047862b424ca625317f4657667bc2c6b)) (更多依赖升级详情，请查看 ContiNew Starter [更新日志](https://github.com/Charles7c/continew-starter/blob/dev/CHANGELOG.md))
- Arco Design Vue 2.53.0 => 2.53.3 ([2720275](https://github.com/Charles7c/continew-admin/commit/2720275b97334545dde71d548173bfcda7e660cb))
- Vite 4.5.0 => 4.5.1 ([2720275](https://github.com/Charles7c/continew-admin/commit/2720275b97334545dde71d548173bfcda7e660cb))
- TypeScript 5.2.2 => 5.3.3 ([2720275](https://github.com/Charles7c/continew-admin/commit/2720275b97334545dde71d548173bfcda7e660cb))
- unplugin-vue-components 0.25.2 => 0.26.0 ([2720275](https://github.com/Charles7c/continew-admin/commit/2720275b97334545dde71d548173bfcda7e660cb))
- @kangc/v-md-editor 2.3.17 => 2.3.18 ([2720275](https://github.com/Charles7c/continew-admin/commit/2720275b97334545dde71d548173bfcda7e660cb))
- eslint 8.53.0 => 8.56.0 ([2720275](https://github.com/Charles7c/continew-admin/commit/2720275b97334545dde71d548173bfcda7e660cb))
- @vueuse/core 10.5.0 => 10.7.0 ([2720275](https://github.com/Charles7c/continew-admin/commit/2720275b97334545dde71d548173bfcda7e660cb))
- vue-i18n 9.6.5 => 9.8.0 ([2720275](https://github.com/Charles7c/continew-admin/commit/2720275b97334545dde71d548173bfcda7e660cb))
- vue-json-pretty 2.2.4 => 2.3.0 ([2720275](https://github.com/Charles7c/continew-admin/commit/2720275b97334545dde71d548173bfcda7e660cb))
- 由于篇幅限制，仅列出部分前端依赖升级情况，更多请查看 [提交记录](https://github.com/Charles7c/continew-admin/commit/2720275b97334545dde71d548173bfcda7e660cb)

## [v2.1.0](https://github.com/Charles7c/continew-admin/compare/v2.0.0...v2.1.0) (2023-12-03)

### 💎 功能优化

- 优化数据权限注解 ([bb59a78](https://github.com/Charles7c/continew-admin/commit/bb59a78573bec521e8852f1c88ce6078fb14b14e))
- 回退全局响应结果处理器 ([c7a4e32](https://github.com/Charles7c/continew-admin/commit/c7a4e329945d8368a9b93a2488c059cf3333feba))
- 优化字典 Controller CRUD 注解使用 ([8c1c4b0](https://github.com/Charles7c/continew-admin/commit/8c1c4b014463d073e848e2f2abc33e089efa2abb))
- 优化常量命名风格，XxxConsts => XxxConstants ([ec28705](https://github.com/Charles7c/continew-admin/commit/ec28705b6ff6dd26ec3ef673fb3827259f1b9c41))
- 移除 XML 文件头部的协议信息 ([b476956](https://github.com/Charles7c/continew-admin/commit/b47695603afb0c19679c4100c1e3c23bc8007238))
- 优化菜单标题校验 ([3dd81a1](https://github.com/Charles7c/continew-admin/commit/3dd81a1192c4e340dad0b1bae5e29d1d7218fb25))

### 🐛 问题修复

- 修复 mock 被错误关闭的问题 ([a34070f](https://github.com/Charles7c/continew-admin/commit/a34070ffed3044ad2bea604701b074665e7b4e42))
- 修复保存生成配置校验失效的问题，并优化部分提示效果 ([c34e934](https://github.com/Charles7c/continew-admin/commit/c34e934bb553d7f814d0fb5aa87eac0f565289b4))

### 💥 破坏性变更

- 项目包结构 top.charles7c.cnadmin => top.charles7c.continew.admin ([b86fe32](https://github.com/Charles7c/continew-admin/commit/b86fe329d07317fed6a7d0b7015856de4b9e75d1))
- 适配 ContiNew Starter 全局错误处理配置 ([b62095d](https://github.com/Charles7c/continew-admin/commit/b62095d66e2318d35e4af07b128203b5a5e016f7))
- 适配 ContiNew Starter CRUD（扩展模块） ([ce5a2ec](https://github.com/Charles7c/continew-admin/commit/ce5a2ec9319b86e69a6bda67a886e1c96079ffc2))
- 适配 ContiNew Starter Mail（消息模块） ([ce785dd](https://github.com/Charles7c/continew-admin/commit/ce785ddce28733eeefbf970ed08b01e36e0abd4b))
- 适配 ContiNew Starter Excel（文件处理模块） ([1311ae3](https://github.com/Charles7c/continew-admin/commit/1311ae3603a26dc44dfffc5be86ea1ab81ff7958))
- 适配 ContiNew Starter 认证模块-JustAuth ([7ad8d17](https://github.com/Charles7c/continew-admin/commit/7ad8d1773a8e50e37a326b8f73f9ba38a3a7ff3a)) ([f28fbd1](https://github.com/Charles7c/continew-admin/commit/f28fbd14fa83a82df49b07f16070e0ff3385b0ec))
- 适配 ContiNew Starter 认证模块-SaToken ([86ca8f0](https://github.com/Charles7c/continew-admin/commit/86ca8f094ff6d1c00b52c1406985bc00105b297f))
- 适配 ContiNew Starter 图形验证码 ([8a11a02](https://github.com/Charles7c/continew-admin/commit/8a11a020e04e271da7b700b5d73cf8475b50ee5c))
- 适配 ContiNew Starter MyBatis Plus 自动配置 ([7306cd9](https://github.com/Charles7c/continew-admin/commit/7306cd9d2f9492aa39e11b8f0dc8c7c11b534a3f))
- 适配 ContiNew Starter Redisson 自动配置 ([a40e609](https://github.com/Charles7c/continew-admin/commit/a40e609ea14acda840d2771f05ca9690d41236a1))
- 适配 ContiNew Starter Jackson、API 文档（Knife4j：Spring Doc）自动配置 ([a86f3a5](https://github.com/Charles7c/continew-admin/commit/a86f3a5047eda2f67cc9ad7721006d2db1fd710f))
- 适配 ContiNew Starter 线程池自动配置 ([ec1daaf](https://github.com/Charles7c/continew-admin/commit/ec1daaf0456296dbc3704ae700b9577001bdd5bb))
- 引入 ContiNew Starter，适配跨域自动配置 ([2c4f511](https://github.com/Charles7c/continew-admin/commit/2c4f5116c999b9316ab0bee4fa661338fea63c11))
- 项目 group id top.charles7c => top.charles7c.continew ([3e23acb](https://github.com/Charles7c/continew-admin/commit/3e23acb3e257d5813b858356aa926a96c906acf1))

## [v2.0.0](https://github.com/Charles7c/continew-admin/compare/v1.3.1...v2.0.0) (2023-11-15)

### 💎 功能优化

- 优化部分代码格式 ([2f87310](https://github.com/Charles7c/continew-admin/commit/2f87310bc886af604a2667285a973ec6ae983430))
- 优化 401 状态处理逻辑 ([c70e28a](https://github.com/Charles7c/continew-admin/commit/c70e28a535c78214fe8d68a09824c786c457ef06))
- 优化超时登录处理逻辑 ([d5da184](https://github.com/Charles7c/continew-admin/commit/d5da1847e33e6cd7a0e5c3434335044167c1241c))

### 🐛 问题修复

- sms4j 3.0.3 => 3.0.4 ([23558d4](https://github.com/Charles7c/continew-admin/commit/23558d45620a48ed82b32a5bdd2f948a4a37263d))
- 发送消息增加事务处理 ([Gitee#7](https://gitee.com/continew/continew-admin/pulls/7)) ([1ca6f6c](https://github.com/Charles7c/continew-admin/commit/1ca6f6c7e5f8a7c78f74df547f14517293241ac4))
- 修复前端控制台 eslint 告警 ([Gitee#6](https://gitee.com/continew/continew-admin/pulls/6)) ([f4523d2](https://github.com/Charles7c/continew-admin/commit/f4523d24817b4fee5c015eaba6b98fe99f350bba)) ([2304f28](https://github.com/Charles7c/continew-admin/commit/2304f28a942fa8ea3e6d36fbebbe9346b0d3b741))
- 修复仪表盘访问趋势区块 y 轴数值过大时无法展示的问题 ([fea6024](https://github.com/Charles7c/continew-admin/commit/fea602439a3c9589bee078bfa9ff1e7efb378d71))
- 修复控制台报错 Please use theme before using plugins ([98fbe05](https://github.com/Charles7c/continew-admin/commit/98fbe0506c1cbe2f3c16347d9610ebfa5688b506))
- 调整 Logback 配置，取消启动时打印 Logback 状态日志 ([1f7fef5](https://github.com/Charles7c/continew-admin/commit/1f7fef5b31212e94652777be37bea4d4e02eb8c7))

### 💥 破坏性变更

- 优化部署相关脚本，mariadb => mysql ([5f4f0f1](https://github.com/Charles7c/continew-admin/commit/5f4f0f1b21fe882dc51801d7c508c10b87d7af36))
- 适配 Java 16 新特性 ([cf30443](https://github.com/Charles7c/continew-admin/commit/cf3044312c8631a8c2b306e466e3d4d663d8eb6d))
- 适配 Java 14 新特性 ([38f52aa](https://github.com/Charles7c/continew-admin/commit/38f52aaafa22ebc958a22b7c38b084c655064fbc))
- 适配 Java 11 新特性 ([5a5bd16](https://github.com/Charles7c/continew-admin/commit/5a5bd1681e076ac6814d552da5415a8f154b93af))
- 升级前端依赖 ([79fa2c8](https://github.com/Charles7c/continew-admin/commit/79fa2c8abcf5f70f96ae7c6de35c47dbae76ee2d)) ([c44162d](https://github.com/Charles7c/continew-admin/commit/c44162d431cb87cae251067fff9a5ae707aed9b3))
  - Arco Design Vue 2.52.0 => 2.53.0
  - Vue 3.3.4 => 3.3.7
  - Vite 3.2.7 => 4.5.0
  - vue-router 4.2.4 => 4.2.5
  - vue-i18n 9.5.0 => 9.6.5
  - vue-tsc 1.2.0 => 1.8.22
  - @vueuse/core 9.13.0 => 10.5.0
  - pinia 2.1.6 => 2.1.7
  - rollup 3.20.2 => 4.3.0
  - vue-cropper 1.0.9 => 1.1.1
  - crypto-js 4.1.1 => 4.2.0
  - vite-svg-loader 3.6.0 => 4.0.0
  - highlight.js 11.8.0 => 11.9.0
  - mitt 3.0.0 => 3.0.1
  - consola 2.15.3 => 3.2.3
  - prettier 2.8.7 => 3.0.3
  - less 4.1.3 => 4.2.0
  - eslint 8.48.0 => 8.53.0
  - stylelint 15.10.3 => 15.11.0
  - lint-staged 13.2.0 => 3.0.3
- 升级后端依赖  ([dea160a](https://github.com/Charles7c/continew-admin/commit/dea160a7b2d69e1b46edc936c9a697048bbb507a)) ([95c27ea](https://github.com/Charles7c/continew-admin/commit/95c27ea323e015c915d352618158df830b4d1c05)) ([fa23287](https://github.com/Charles7c/continew-admin/commit/fa232874aa88ab14fdc669e54a907e5ef05d2a7e)) ([8dbec9d](https://github.com/Charles7c/continew-admin/commit/8dbec9d1a3bcb0f6d7ef4bbfb9715effd61b2025)) ([3bd56d8](https://github.com/Charles7c/continew-admin/commit/3bd56d8a1ee274aac6d4ea57d61f6d470de0dc9c)) ([7b741d5](https://github.com/Charles7c/continew-admin/commit/7b741d5f8c42d154c5b325326d0cc954fb566502))
  - Spring Boot 2.7.16 => 3.0.5 => 3.1.5
    - javax.* => jakarta.*
    - ServletUtil => JakartaServletUtil（Hutool）
    - 其他配置变更
  - JDK 8 => JDK 17
  - Sa-Token 1.36.0 => 1.37.0（适配 Spring Boot 3.x）
  - MyBatis Plus 3.5.3.2 => 3.5.4（适配 Spring Boot 3.x）
  - Dynamic Datasource 3.6.1 => 4.2.0（适配 Spring Boot 3.x）
  - Redisson 3.20.1 => 3.24.3（适配 Spring Boot 3.x）
  - Knife4j 适配 Spring Boot 3.x
  - ip2region 2.7.15 => 3.1.5.1（适配 Spring Boot 3.x）
  - spotless 2.30.0 => 2.40.0

## [v1.3.1](https://github.com/Charles7c/continew-admin/compare/v1.3.0...v1.3.1) (2023-11-15)

### 💎 功能优化

- 完善 Redis 部署配置 ([39969eb](https://github.com/Charles7c/continew-admin/commit/39969ebf6173fc379dc3501e9204a344d1cf62cf))
- 优化 401 状态处理逻辑 ([8820c1d](https://github.com/Charles7c/continew-admin/commit/8820c1dfc858b9ef9df470e90dfe9ba4b1166e29))
- 优化超时登录处理逻辑 ([712eedb](https://github.com/Charles7c/continew-admin/commit/712eedba1be0ec371119745d4596cd35c2ce25d6))
- 优化部分变量命名 ([f15494d](https://github.com/Charles7c/continew-admin/commit/f15494d34823ded87efc396d98e2eb0108f74a3d))

### 🐛 问题修复

- sms4j 3.0.3 => 3.0.4 ([3fcdb54](https://github.com/Charles7c/continew-admin/commit/3fcdb54442b380e76838478fa46e8dfb70a2759b))
- 发送消息增加事务处理 ([5d159c6](https://github.com/Charles7c/continew-admin/commit/5d159c6ab337a9432419d84cf246cff506500567))
- 修复仪表盘访问趋势区块 y 轴数值过大时无法展示的问题 ([47a5746](https://github.com/Charles7c/continew-admin/commit/47a5746794e552faf9c41fbcc21af091a878eb95))
- 修复控制台报错 Please use theme before using plugins ([47a8160](https://github.com/Charles7c/continew-admin/commit/47a8160d70862a5ee7284c165004cece2714a10f))
- 修复 Swagger 分组接口缺失 ([b63d7d7](https://github.com/Charles7c/continew-admin/commit/b63d7d725da5e9e9b2db9fd59bd140d64b50040c))

## [v1.3.0](https://github.com/Charles7c/continew-admin/compare/v1.2.0...v1.3.0) (2023-11-04)

### ✨ 新特性

* 消息管理：提供消息查看、标记已读、全部已读、删除等功能（适配对接导航栏站内信功能）
* 新增头像上传前裁剪功能 ([Gitee#5](https://gitee.com/continew/continew-admin/pulls/5)) ([cbc652d](https://gitee.com/continew/continew-admin/commit/cbc652de77200d29bcd42bb399c86c2e7df29c4d)) ([28f4791](https://gitee.com/continew/continew-admin/commit/28f4791833060469d132c4383665e81458f9c852))
* 支持手机号登录（演示环境不开放） ([4d70bc8](https://github.com/Charles7c/continew-admin/commit/4d70bc84db47c36c13d8e41e3a33e5a589483de8))
* 支持邮箱登录 ([17b169e](https://github.com/Charles7c/continew-admin/commit/17b169eb0ea2ded759b6bccb213c78bfb3425941))
* 个人中心-安全设置，支持绑定、解绑三方账号 ([efe4557](https://github.com/Charles7c/continew-admin/commit/efe455736c158e73bf0c6514c31bec5d83fe843b))
* 支持第三方账号登录 ([05cb609](https://github.com/Charles7c/continew-admin/commit/05cb60978017edbd14f1c7af83053f8a91800b5c))

### 💎 功能优化

- 新增接口文档菜单，演示环境开放接口文档 ([4a42336](https://github.com/Charles7c/continew-admin/commit/4a4233647f2ea212b007f591aafc50380b15c099))
- 项目配置增加是否为生产环境配置项 ([38deb95](https://github.com/Charles7c/continew-admin/commit/38deb950ac7b2ed81f0e10816e943156aa076795))
- 优化校验相关方法命名 ([f25de2d](https://github.com/Charles7c/continew-admin/commit/f25de2d7f835a3fa75d59d3de0a014c37b3b32e1))
- 新增全局响应结果处理器 ([Gitee#3](https://gitee.com/continew/continew-admin/pulls/3)) ([992a8fc](https://gitee.com/continew/continew-admin/commit/992a8fca173ea76722b388aca462cff8a1128803)) ([Gitee#4](https://gitee.com/continew/continew-admin/pulls/4)) ([a0b1afc](https://gitee.com/continew/continew-admin/commit/a0b1afc546657766cb6031794b98ccc2b6e4cb2d))
- 优化部分代码格式及注释 ([3a176ac](https://github.com/Charles7c/continew-admin/commit/3a176ac5efbda4aea1e883b29e68861bd352d642))
- 重构登录页面 UI 以适配多维度认证、第三方登录等场景 ([d40d5b4](https://github.com/Charles7c/continew-admin/commit/d40d5b4ae61d858fbee3ffa0606ebebb4282d9a2)) ([a5a4cd4](https://github.com/Charles7c/continew-admin/commit/a5a4cd49646db3fa1108a8b917ef70c7757e81ad))
- 升级前端依赖 ([698a725](https://github.com/Charles7c/continew-admin/commit/698a7251b742e6b679694f21bfc174904dca8990))
  - Arco Design Vue 2.51.0 => 2.52.0
  - vue-i18n 9.2.2 => 9.5.0
  - dayjs 1.11.9 => 1.11.10

- 升级后端依赖 ([698a725](https://github.com/Charles7c/continew-admin/commit/698a7251b742e6b679694f21bfc174904dca8990))
  - Spring Boot 2.7.15 => 2.7.16
  - Sa-Token 1.35.0.RC => 1.36.0
  - Hutool 5.8.20 => 5.8.22


### 🐛 问题修复

- 开放前端项目IP访问 ([22a291d](https://github.com/Charles7c/continew-admin/commit/22a291d4cf48e33dc2415e44b5d991b46451e7eb))
- 修复获取验证码倒计时显示 ([2f2905e](https://github.com/Charles7c/continew-admin/commit/2f2905efdc0baec2f2c38f686f72306394801ebf))
- 用户邮箱信息增加脱敏处理 ([5bb35a1](https://github.com/Charles7c/continew-admin/commit/5bb35a13d6b5801317a295eacc67d88b2c3e1682))
- 修复重载校验方法定义及使用错误 ([a1ccc42](https://github.com/Charles7c/continew-admin/commit/a1ccc421c440e5fef54e5d22b9bed26d2b16dda5))
- 修复个人中心密码设置状态显示错误的问题 ([b04a228](https://github.com/Charles7c/continew-admin/commit/b04a228a1a5bc0a575dd9e29e515285708b8ca85))
- 修复登录后访问首页却跳转到登录页面的问题 ([Fixes #23](https://github.com/Charles7c/continew-admin/issues/23)) ([7cf5e00](https://github.com/Charles7c/continew-admin/commit/7cf5e0018c87720303f731317b5eb3cb7d127327))
- 修复字典名称表单校验 ([#22](https://github.com/Charles7c/continew-admin/pull/22)) ([c0ee2ea](https://github.com/Charles7c/continew-admin/commit/c0ee2eac026d2d5a950a41b6f0a475b95b71d47a))

### 💥 破坏性变更

- 调整后端请求、响应参数模型命名风格 ([87f9056](https://github.com/Charles7c/continew-admin/commit/87f90567dbd99f873aea1b85510c7b9939a2abb8))
- 枚举接口 BaseEnum => IBaseEnum ([f5e8b09](https://github.com/Charles7c/continew-admin/commit/f5e8b0943c6076c476b7d78bb623707740fb452f))
- 优化前端登录模块 API 路径 ([43590bf](https://github.com/Charles7c/continew-admin/commit/43590bf66e7e4873a85bdd416bd38b269f3af80e))
- 优化后端部分参数模型命名 ([51f5528](https://github.com/Charles7c/continew-admin/commit/51f552892ccb11ed594bf908069a1fd426324b69))
- 优化个人中心路由地址 ([36d52d3](https://github.com/Charles7c/continew-admin/commit/36d52d3e1522cd221cf3f03d76efd3e0eaf1b18f))
- 还原前端 loginStore 命名，重命名为 userStore ([8d39493](https://github.com/Charles7c/continew-admin/commit/8d394937cfc8418799215bd3659d26bed1f834c5))

## [v1.2.0](https://github.com/Charles7c/continew-admin/compare/v1.1.2...v1.2.0) (2023-09-24)

### ✨ 新特性

* 字典管理：提供对系统公用数据字典的维护，例如：公告类型，支持字典标签背景色和排序等配置
* 系统配置：提供修改系统标题、Logo、favicon 等基础配置功能，以方便用户系统与其自身品牌形象保持一致
* 完善仪表盘最近访问区块内容 ([36fda57](https://github.com/Charles7c/continew-admin/commit/36fda57d499b0c3fb092a13f269bc9ffb7a26a9e))
* 完善仪表盘访问趋势区块内容 ([a1c20af](https://github.com/Charles7c/continew-admin/commit/a1c20afb1b9eb447f62bfd2e4f2996dfdf37c8ca)) ([1722133](https://github.com/Charles7c/continew-admin/commit/1722133ac4872b40d6d47f65f359dea8a354b91a))
* 完善仪表盘访客地域分布区块内容 ([dc1691f](https://github.com/Charles7c/continew-admin/commit/dc1691f0195ef6c96aee36f50fc7e86cfcf651b9))
* 完善仪表盘热门模块区块内容 ([83b2e2a](https://github.com/Charles7c/continew-admin/commit/83b2e2a7c02d38c7041497e0ac5b3b0e78abac29))
* 完善仪表盘总计区块内容 ([3440aa4](https://github.com/Charles7c/continew-admin/commit/3440aa4faa23e267735f564476d8bccaf8c0208f))
* 完善仪表盘快捷操作区块内容 ([0178fbb](https://github.com/Charles7c/continew-admin/commit/0178fbb89a0e75729aa60443a812496bd5b19cb8))

### 💎 功能优化

- 前端表单重置优化 ([e947312](https://github.com/Charles7c/continew-admin/commit/e947312f244d6af01f18b542ff7395440c68b089))
- 优化登录和菜单加载相关提示 ([d080120](https://github.com/Charles7c/continew-admin/commit/d080120d4228e77200d8f152397b0ebee413b089))
- 完善前后端校验 ([90d825a](https://github.com/Charles7c/continew-admin/commit/90d825a02fdc54e8685508a6fe4fb2d5f20e77f4)) ([8e506dc](https://github.com/Charles7c/continew-admin/commit/8e506dc6e69529627a0aace6118f7310cc2f030a))
- 优化枚举字典处理，增加颜色类型 ([1f73aa7](https://github.com/Charles7c/continew-admin/commit/1f73aa732d101c7f7a58bc678e85d597d54d9770))
- 公告类型适配字典数据 ([3a3a5d6](https://github.com/Charles7c/continew-admin/commit/3a3a5d6b712f435d77ea04301afa0bdd8703567f))
- 优化通用查询注解多字段模糊查询 ([3758107](https://github.com/Charles7c/continew-admin/commit/375810772aa8cb928fb1f6820e781cb43f869e03))
- 合并菜单管理图标和标题列 ([36d38ae](https://github.com/Charles7c/continew-admin/commit/36d38aec1602f5ac6d2afbb5c5adf4d6e455ab97))
- 封装 Spring Boot 默认错误处理 ([b874ca0](https://github.com/Charles7c/continew-admin/commit/b874ca0782eb116bdedfc08023959a977f170a94))
- 优化分页查询登录日志列表接口实现 ([566c9a1](https://github.com/Charles7c/continew-admin/commit/566c9a122453980b585bd68442bb545073504a3d))
- 更换登录页面 banner ([6f19660](https://github.com/Charles7c/continew-admin/commit/6f19660cfbc3be6e0d702e3f488e266c50622f0a))
- 优化登录用户信息角色相关信息命名 ([be394f3](https://github.com/Charles7c/continew-admin/commit/be394f3de4ea7ea692042db3556f706a3d141b51)) ([31f0abb](https://github.com/Charles7c/continew-admin/commit/31f0abbae2e38d1cfa3f6221c9be0b54cf5337ad))
- 升级前端依赖 ([c665902](https://github.com/Charles7c/continew-admin/commit/c6659020f8bac7319c5c407389cd745527a8cd97))
- 升级后端依赖 ([5049e1e](https://github.com/Charles7c/continew-admin/commit/5049e1e312ab500e284abccbbee4186db2710d01)) ([d20aadf](https://github.com/Charles7c/continew-admin/commit/d20aadfc93b54339d19d173fce364310e90b016d)) ([32904b5](https://github.com/Charles7c/continew-admin/commit/32904b54ef63536ef5c5106adc00a7376b907632))

### 🐛 问题修复

- 修复删除列表数据后 Select 选择框重置问题 ([#21](https://github.com/Charles7c/continew-admin/pull/21)) ([3288f2d](https://github.com/Charles7c/continew-admin/commit/3288f2d38dfebc1381842d67cdfb17675c786859))
- 修复前端部分拼写错误 ([62021f8](https://github.com/Charles7c/continew-admin/commit/62021f8fdc171ad04d07c25c5a9357a64cc4a087))

### 💥 破坏性变更

- 优化系统内置类型数据标识 ([8a02401](https://github.com/Charles7c/continew-admin/commit/8a02401a24b546f2a6aab04cf05371ecb4236ca0))
- 分离 HTTP 状态码和业务状态码 ([b3b6446](https://github.com/Charles7c/continew-admin/commit/b3b6446433972422cf62dfc47c031134b91cd7ec))
- 调整生产环境本地存储、日志位置 ([2254e55](https://github.com/Charles7c/continew-admin/commit/2254e555af9cade4897d5335b252a0312d6805eb))
- 调整项目打包结构，分离依赖、配置文件 ([e679abf](https://github.com/Charles7c/continew-admin/commit/e679abfccc6c80198512958b6d07b363074d9d76))

## [v1.1.2](https://github.com/Charles7c/continew-admin/compare/v1.1.1...v1.1.2) (2023-09-24)

### 💎 功能优化

- 优化后端程序启动成功输出内容 ([6322859](https://github.com/Charles7c/continew-admin/commit/63228598d9fcd6e5d00172c12418a371d4c96766))
- 配置子级菜单图标 ([5544836](https://github.com/Charles7c/continew-admin/commit/55448364a39085debb776463f5e95a15b186c447))

### 🐛 问题修复

- 修复生产环境和开发环境样式不一致的问题 ([be8732d](https://github.com/Charles7c/continew-admin/commit/be8732d812e021631864b0ff6225b4da24cafcee))
- 排除路径配置放开 /error ([0428fe7](https://github.com/Charles7c/continew-admin/commit/0428fe776224afb64601901cef4d3100e5d30bd6))
- 修复初始数据缺失字段列表的问题 ([d5138e1](https://github.com/Charles7c/continew-admin/commit/d5138e1e43bdc8b347e061890131ac2646b2dd3c))
- 修复系统日志表索引缺失导致查询耗时较长的问题 ([ac43833](https://github.com/Charles7c/continew-admin/commit/ac438337219f5a160d49b255805774da36ab865c))
- 修复部分菜单数据 component 信息配置错误 ([11ea072](https://github.com/Charles7c/continew-admin/commit/11ea072d600f24fe97fe8145208e821712b84839))
- 修复图标 SVG 内容格式错误 ([20f1e8a](https://github.com/Charles7c/continew-admin/commit/20f1e8aecc737b28ab869d363957513d868b4ab7))

## [v1.1.1](https://github.com/Charles7c/continew-admin/compare/v1.1.0...v1.1.1) (2023-09-06)

### 💎 功能优化

- 调整 Mock 响应时长，以解决前端偶发需重复登录问题 ([df19c5d](https://github.com/Charles7c/continew-admin/commit/df19c5d2197fabb61cbdd4dccf1c427fb23d77d4))

### 🐛 问题修复

- 还原登录 Helper 优化（导致重大登录问题及查询在线用户错误） ([#15](https://github.com/Charles7c/continew-admin/pull/15)) ([7a6db2d](https://github.com/Charles7c/continew-admin/commit/7a6db2d14e60a5fcc1a2786e6eaa3d46a0714e6c)) ([#9](https://github.com/Charles7c/continew-admin/pull/9)) ([9e2a5ef](https://github.com/Charles7c/continew-admin/commit/9e2a5ef1249fd93dd10f2c255bf77c3eaa64a241))
- 修复刷新页面后，选中菜单无法保持展开状态的问题 ([3fc7adb](https://github.com/Charles7c/continew-admin/commit/3fc7adb1e2bd4b648753bd2999df725417e01680))
- 修复侧边栏菜单无法显示自定义图标的问题 ([10ca5d8](https://github.com/Charles7c/continew-admin/commit/10ca5d8c76aa39a207ea7db4442bf63ff4578273))
- 更正 README 文档项目结构部分内容 ([486da2f](https://github.com/Charles7c/continew-admin/commit/486da2f79bfc5379213bf666b8f325fb8096ebc6))
- 修复公告缺失待发布状态的问题 ([#14](https://github.com/Charles7c/continew-admin/pull/14)) ([46cc4c9](https://github.com/Charles7c/continew-admin/commit/46cc4c9307e3cc7060ae436f59f007831104884a))

## [v1.1.0](https://github.com/Charles7c/continew-admin/compare/v1.0.1...v1.1.0) (2023-09-01)

### ✨ 新特性

* 公告管理：提供公告的发布、查看和删除等功能。管理员可以在后台发布公告，并可以设置公告的生效时间、终止时间，以 markdown-it 为内核渲染 Markdown 格式内容显示
* 代码生成：提供根据数据库表自动生成相应的前后端 CRUD 代码的功能
* 允许表格调整列宽，不允许新增/修改类表单对话框按 Esc 关闭 ([1b06a96](https://github.com/Charles7c/continew-admin/commit/1b06a96cfbe5774931d8c4c0d7827703caa096df))

### 💎 功能优化

- 最终适配及启用 Arco Design Pro Vue 动态路由 ([9baf341](https://github.com/Charles7c/continew-admin/commit/9baf3410138cb8a152ec51f70340d500fa009510))
- 优化分页总记录数数据类型 ([bfea689](https://github.com/Charles7c/continew-admin/commit/bfea689b0eaf44c8d54b4fd59c042d72ac71e395))
- 修复在线用户列表等自定义分页查询 NPE 的问题 ([015ff55](https://github.com/Charles7c/continew-admin/commit/015ff5512b3662efce88d02ab1dda6d55501a501))
- 对获取路由信息接口增加缓存处理 ([4639d13](https://github.com/Charles7c/continew-admin/commit/4639d13ba61abfaed3c9d3da0e057892577b5c40))⚡
- 完善前端 axios 请求响应拦截器 ([bb398d8](https://github.com/Charles7c/continew-admin/commit/bb398d8101e3780f450c6508852fc727fb936cee)) ([e18692f](https://github.com/Charles7c/continew-admin/commit/e18692fa74e0a0d9558db6643b945c6c6a00db36))
- 优化仪表盘公告区块、帮助文档区块内容 ([b59a819](https://github.com/Charles7c/continew-admin/commit/b59a819ad5f2bdbd357951f070d155e91f2d7903)) ([315c059](https://github.com/Charles7c/continew-admin/commit/315c059713833be10b0cf05d302259a3146f3707)) ([6d024a9](https://github.com/Charles7c/continew-admin/commit/6d024a90d7a231439c8e260b9bd625e8b5027515))
- 将 Swagger 文档中的额外请求参数隐藏 ([#11](https://github.com/Charles7c/continew-admin/pull/11)) ([a9ed02b](https://github.com/Charles7c/continew-admin/commit/a9ed02bf4ff6a8a4d9f68db2d62d29000c543943))
- 优化前端 CRUD 相关命名 ([6d81928](https://github.com/Charles7c/continew-admin/commit/6d81928541f4da568e9c7138f91d4dc1c5c6dd4e))
- 优化部分超链接标签属性 ([46a75d0](https://github.com/Charles7c/continew-admin/commit/46a75d029798e8d5a162b53b8a61c8e3c3f4dd9e))
- 使用属性变量消除配置文件中分散的 ContiNew Admin 品牌元素 ([54ea410](https://github.com/Charles7c/continew-admin/commit/54ea41048abd096cf1e2c32ee871c1eb85d4ece1))
- 拆分 Swagger 接口文档分组 ([#10](https://github.com/Charles7c/continew-admin/pull/10)) ([72df45e](https://github.com/Charles7c/continew-admin/commit/72df45e9b3373d28f1845af16a81cb8bd8408647))
- 优化登录 Helper ([#9](https://github.com/Charles7c/continew-admin/pull/9)) ([9e2a5ef](https://github.com/Charles7c/continew-admin/commit/9e2a5ef1249fd93dd10f2c255bf77c3eaa64a241))
- 将全局异常处理器未知异常的异常类型从 Exception 调整为 Throwable ([90e1c64](https://github.com/Charles7c/continew-admin/commit/90e1c64db684df97454e4753932b7f4017d8e23d))
- 优化 == 及 != 表达式格式 ([487fa82](https://github.com/Charles7c/continew-admin/commit/487fa82306fbd84033f6c39ad20b72755b03e875))
- 集成 Spring Cache，优化查询用户昵称性能 ([b23b00d](https://github.com/Charles7c/continew-admin/commit/b23b00d02a4738a61b4a13676fab6d2c9ec927de)) ([76622c2](https://github.com/Charles7c/continew-admin/commit/76622c238f1d6028826407490e50a14bdba25ade))⚡
- 将验证码唯一标识格式从无符号 UUID 调整为带符号 UUID ([a61196c](https://github.com/Charles7c/continew-admin/commit/a61196cd62cea4f684154bb42a949656650f626b))
- 完善接口文档示例信息 ([#7](https://github.com/Charles7c/continew-admin/pull/7)) ([ad7d699](https://github.com/Charles7c/continew-admin/commit/ad7d6995ba40a0cb70a194693fa450bdbb3cc7a0)) ([#8](https://github.com/Charles7c/continew-admin/pull/8)) ([0ac0213](https://github.com/Charles7c/continew-admin/commit/0ac0213628023c04b5be531522d76f09712f7317)) ([190385e](https://github.com/Charles7c/continew-admin/commit/190385ed3636206224bc90780fcede2e49f9c118)) ([332bd6c](https://github.com/Charles7c/continew-admin/commit/332bd6cd2a9b4e25678a3eec565965c5b2702aa2))
- 使用 DatePattern 中的日期格式常量替代字符串常量中的日期格式 ([241a9cf](https://github.com/Charles7c/continew-admin/commit/241a9cf85b3c19eb093d4d661c35d71c490adf1f))
- 优化分组校验 ([78a5d5e](https://github.com/Charles7c/continew-admin/commit/78a5d5ec7a14ee37d92a9520211adca23f12b287))
- 优化 springdoc-openapi 对象型参数处理 ([ae8d294](https://github.com/Charles7c/continew-admin/commit/ae8d294705536e99d6c30a9ff5257fdb3ee5b35f))
- 升级前端依赖，并更换包管理器 yarn => pnpm ([6164110](https://github.com/Charles7c/continew-admin/commit/6164110462cc3aff66d79539f54e84d47c6d5894))
- 升级后端依赖 ([51a82d8](https://github.com/Charles7c/continew-admin/commit/51a82d8f4eabd6aa27e1a991f05f516171b6ae03))

### 🐛 问题修复

- 完善部分数据库表的唯一索引 ([88d6118](https://github.com/Charles7c/continew-admin/commit/88d6118693586fbd8da573df3b2f942d049e4b3c))
- 修复访问 doc.html 接口文档，控制台报 No mapping for GET /favicon.ico 警告的问题 ([94f88ba](https://github.com/Charles7c/continew-admin/commit/94f88bad2278d64a4b8a3bc930a9f754fb00cba6))
- 登录页面输入错误时，自动清空验证码输入框 ([a76f47f](https://github.com/Charles7c/continew-admin/commit/a76f47fbd86bfa7fbf85440c653ae6259fce7969))

### 💥 破坏性变更

- 更新信息调整为仅在更新数据时自动填充 ([df77e57](https://github.com/Charles7c/continew-admin/commit/df77e574cca605afd89f1b3781f1cde699bcb7e6))
- 将时间戳单位从毫秒调整为秒 ([fa916b9](https://github.com/Charles7c/continew-admin/commit/fa916b93247e10462eb44185ad45cdca4dedda7d))
- 移除所有的 @Accessors(chain = true)，并全局配置禁止使用 ([76c6546](https://github.com/Charles7c/continew-admin/commit/76c65463c2e5ddf0c90fa1622fd86706a4373c80))

## [v1.0.1](https://github.com/Charles7c/continew-admin/compare/v1.0.0...v1.0.1) (2023-08-17)

### 💎 功能优化

- 优化根据 ID 查询用户昵称方法 ([4a8af1f](https://github.com/Charles7c/continew-admin/commit/4a8af1f72d9249afa1c013e08674f492f453b020))
- 优化 BaseController 中部分权限码的使用 ([b0b1127](https://github.com/Charles7c/continew-admin/commit/b0b1127b5bd39e9bc431e9fa9c86201bbc18e891))
- 优化分页总记录数数据类型 ([76f04dd](https://github.com/Charles7c/continew-admin/commit/76f04dd38f90aad6abf82d2dccba031d4d9108cf))
- 优化通用查询注解解析器 ([a623acd](https://github.com/Charles7c/continew-admin/commit/a623acd4a5529ae42898ec359f595716acc5bab8)) ([b632c18](https://github.com/Charles7c/continew-admin/commit/b632c183994ac71382180a38bf7bdb7a6315c1e6))
- 优化数据库表结构中部分类型长度 ([f3fabea](https://github.com/Charles7c/continew-admin/commit/f3fabea7dd736d94badecbc08091eec6274f5fb7))
- 使用常量优化部分魔法值 ([e6f7429](https://github.com/Charles7c/continew-admin/commit/e6f7429fa30cbc87c03a073a53b6f7df24d33d8d))
- 优化部分 Properties 用法 ([48de2e8](https://github.com/Charles7c/continew-admin/commit/48de2e85e0fbf60f10769cd3529f79ac3c531e92))

### 🐛 问题修复

- 修复获取字典参数为空时的判断条件 ([#6](https://github.com/Charles7c/continew-admin/pull/6)) ([104f69e](https://github.com/Charles7c/continew-admin/commit/104f69e8a09ce36163f6f9680b2d8d61bb45f11a))
- 完善查询用户数据权限 ([026247f](https://github.com/Charles7c/continew-admin/commit/026247f677110ae199124a67c68503729cbaec92))
- 解决 IDE 报 Delete ␍ eslint(prettier/prettier) 警告的问题 ([8743ed1](https://github.com/Charles7c/continew-admin/commit/8743ed14d927ab52814ed5f5f166afaa7a6b78b2))
- 修复分页查询条件默认值未生效的问题 ([2d2a7e7](https://github.com/Charles7c/continew-admin/commit/2d2a7e7c8e31763ac3ea514d8a92c3938376dd3a))
- 完善各模块事务注解 ([18c54a7](https://github.com/Charles7c/continew-admin/commit/18c54a74fc6ff0650ff53eeadc094d7e1df0b0a5))
- 修复邮箱健康检查报错问题并优化部分配置写法 ([5968f40](https://github.com/Charles7c/continew-admin/commit/5968f402ed478244d36f5825373190ed00d8c1f1))
- 完善各模块参数校验 ([8b955a0](https://github.com/Charles7c/continew-admin/commit/8b955a0b1bde4e8959fc0dfbc11a326d9eec0b45))

## v1.0.0 (2023-03-26)

### ✨ 新特性

* 用户管理：提供用户的相关配置，新增用户后，默认密码为 123456
* 角色管理：对权限与菜单进行分配，可根据部门设置角色的数据权限
* 部门管理：可配置系统组织架构，树形表格展示
* 菜单管理：已实现菜单动态路由，后端可配置化，支持多级菜单
* 在线用户：管理当前登录用户，可一键踢下线
* 日志管理：提供在线用户监控、登录日志监控、操作日志监控和系统日志监控等监控功能
