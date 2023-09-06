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
