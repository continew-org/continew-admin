## [v3.7.0](https://github.com/continew-org/continew-admin/compare/v3.6.0...v3.7.0) (2025-06-13)

### ✨ 新特性

- 文件管理支持目录层级 (GitHub#151@luoqiz) ([9b79990](https://github.com/continew-org/continew-admin/commit/9b79990cc0dc803de9f4181dad32d883679341b4)) ([943d640](https://github.com/continew-org/continew-admin/commit/943d640995ebde64e44842fac47527950c4eade4))
- 短信渠道支持数据字典配置 (GitHub#159) ([e4828bf](https://github.com/continew-org/continew-admin/commit/e4828bf2a39f3bfd32dce018874ef4cba674c02e)) ([e0747cb](https://github.com/continew-org/continew-admin/commit/e0747cbf367f6117c6539ab20086d8f8945e58e5))
- 短信配置新增设为默认功能 ([0730487](https://github.com/continew-org/continew-admin/commit/07304872b6c4edd1bc69ebf6eaf88ddb82f33e51))
- 全局异常处理器添加 BaseException 处理 (Gitee#59@chengangi) ([4274f2e](https://github.com/continew-org/continew-admin/commit/4274f2ede6ff8a929fe4cc7cc51aa8fa4e1eca19))
- 文件管理新增计算文件大小接口 ([798182d](https://github.com/continew-org/continew-admin/commit/798182d1202ba975ad45a05e65cb37283e2a3e07))
- GlobalExceptionHandler 增加 MethodArgumentNotValidException 处理（之前使用 Graceful Reponse 托管会存在错误全部返回的问题） ([0726a21](https://github.com/continew-org/continew-admin/commit/0726a21eadd2ce2862a1792aa3acd4ce4d1c1213))
- 新增 ConditionalOnEnabledScheduleJob 注解 ([056eb8a](https://github.com/continew-org/continew-admin/commit/056eb8a3731c4a50d8954eb03487f7e8c3401179))
- 重构公告及消息，公告支持系统消息推送提醒、定时发布、置顶、记录读取状态 ([0f3e94f](https://github.com/continew-org/continew-admin/commit/0f3e94f32fcf8be90549b90e5b9b853bd63079a5)) ([af1f540](https://github.com/continew-org/continew-admin/commit/af1f540d6c1dd858eadbbcc8357ec2a81fac4058)) ([222339b](https://github.com/continew-org/continew-admin/commit/222339b54bb260cbca1d357663b67c28040028da)) ([881974f](https://github.com/continew-org/continew-admin/commit/881974fbfa125d5a43ad815e3f697a06aff47b5f)) ([9269429](https://github.com/continew-org/continew-admin/commit/9269429c37c53f92ec0ebec584cbede0ff50cedb))

### 💎 功能优化

- 还原 终端 => 客户端（终端容易被误解） ([be5bfc8](https://github.com/continew-org/continew-admin/commit/be5bfc8a5bfaae69105ac58bc5c9c19d08a114d3))
- 优化代码生成模板中的枚举类导入语句 ([32ac708](https://github.com/continew-org/continew-admin/commit/32ac708ede93b16d350b78d560abc0a912395c90))
- 角色表是否父子节点关联字段调整默认值为 true ([283a5e0](https://github.com/continew-org/continew-admin/commit/283a5e0b9366ea316341b1966ad564569d812574))
- 统一请求参数、响应参数注释 ([4ae716c](https://github.com/continew-org/continew-admin/commit/4ae716c34f4d39756b90fbfafebd77d00077d135)) ([bf3e159](https://github.com/continew-org/continew-admin/commit/bf3e1590fa975cd37330a2d79beb1872b967291e))
- 查询日志不再记录操作日志 ([588bc7e](https://github.com/continew-org/continew-admin/commit/588bc7ef0a3e86dfcd070f32c6c6c238fb78cb7f))
- 调整配置文件内的配置顺序（由配置修改频率高则靠上规则 => 新手上手适合阅读顺序） ([e0de990](https://github.com/continew-org/continew-admin/commit/e0de99051bb57e3d2f434f6fa333645e18d558f2))
- 优化配置文件注释 ([cd4adcf](https://github.com/continew-org/continew-admin/commit/cd4adcf7a2a6b20dc40d36c005c8de8ac60b7084))
- 重构存储配置及文件上传相关代码 ([bc057da](https://github.com/continew-org/continew-admin/commit/bc057da265fdafe1ca91cf6206f0da6f676db58d))
- 重构文件管理相关代码，完善文件夹场景 ([37027c7](https://github.com/continew-org/continew-admin/commit/37027c774b2f65b7feec63657b2befda6a3896ae)) ([e099b5e](https://github.com/continew-org/continew-admin/commit/e099b5e5aaf33b273d03a56ce6d4ba45973a0f7e))  ([5a9958c](https://github.com/continew-org/continew-admin/commit/5a9958c36cdafc5b27de580a5f11a59319818904))
- 修改默认PostgreSQL数据源用户名 (GitHub#162@HideOnSomke) ([e2deb99](https://github.com/continew-org/continew-admin/commit/e2deb99b78d73430f42d5a68b7c5c24f4884286c))
- 调整代码风格 null != xx => xx != null null == xx => xx == null（更符合大众风格） ([a07a2dc](https://github.com/continew-org/continew-admin/commit/a07a2dcd65b3750392bd941804209e7944311507)) ([3850858](https://github.com/continew-org/continew-admin/commit/385085826fe060b2aeee4598b302ce12dcc6454e))
- 新增自定义异常优化任务管理错误提示 ([9d6d798](https://github.com/continew-org/continew-admin/commit/9d6d7984d5c402d34076c2612633915e1ddd6102))

### 🐛 问题修复

- 修复代码生成前端api文件模版路径问题 (GitHub#155@qiuquanwu) ([1c85b43](https://github.com/continew-org/continew-admin/commit/1c85b4393161ddd17ea76bc16858a02857d4c746))
- 修复 BCryptEncryptor 在部分场景会导致重复加密的错误 ([c9d5810](https://github.com/continew-org/continew-admin/commit/c9d5810f7d4da34df98f2175583e1b76e6a9deb7))
- 修复短信配置加载错误，移除 SmsSupplierEnum ([16cdff7](https://github.com/continew-org/continew-admin/commit/16cdff753f56cdad4d73f04b83e8879ee76f0a1c))
- 修复文件 contentType 字段长度配置错误 ([7326e47](https://github.com/continew-org/continew-admin/commit/7326e4738f1f05f62b234454b8e0dbab3d0d3e5a))
- 修复用户管理水平越权错误 ([5bc657a](https://github.com/continew-org/continew-admin/commit/5bc657ad88b6fcf9d43efb6c6e5dfff2d3ecde0b)) ([4f38218](https://github.com/continew-org/continew-admin/commit/4f38218628e6ac970888091e9a5b9830b79dd0f2)) ([d3389db](https://github.com/continew-org/continew-admin/commit/d3389dbe17fc952f73d52f1b385e0e8ffa4d0fa0))
- 修复支持任意格式上传错误 ([a2e156a](https://github.com/continew-org/continew-admin/commit/a2e156aae80fdb8d8f9f3fefa014370930f131e8))
- 修复绑定部分三方账号错误（暂时修复，后续通过重写 starter 解决） ([c242a9f](https://github.com/continew-org/continew-admin/commit/c242a9fafd4405b5812dd0ba1fdf5d00510a5458))
- 修复未绑定字典时，生成下拉选项报错的问题 ([73e2477](https://github.com/continew-org/continew-admin/commit/73e247775cdf8f5d6510efa5b490b70fb9db9813))
- 临时修复使用 @CrudRequestMapping 注解的接口无法通过 @SaIgnore 放行的问题 https://gitee.com/dromara/sa-token/issues/I8RIBL (Gitee#61@dom-w) ([5f9f3e1](https://github.com/continew-org/continew-admin/commit/5f9f3e1035c56d2027f21ab93068ec993fcb3f6e))

### 📦 依赖升级

- 🔥ContiNew Starter 2.11.0 => 2.12.1 (更多特性及依赖升级详情，请查看 ContiNew Starter [更新日志](https://github.com/continew-org/continew-starter/blob/dev/CHANGELOG.md))

## [v3.6.0](https://github.com/continew-org/continew-admin/compare/v3.5.0...v3.6.0) (2025-04-13)

### ✨ 新特性

- 添加字典和菜单缓存清除接口，并优化字典的缓存逻辑 ([093d2d3](https://github.com/continew-org/continew-admin/commit/093d2d3c8aad5c2aa27d1d0d59b584a2a881b643)) ([a300d36](https://github.com/continew-org/continew-admin/commit/a300d36d719d6e828d9736619260dd2ddde08f10))
- 新增短信配置 (GitHub#144@luoqiz) ([1a4716f](https://github.com/continew-org/continew-admin/commit/1a4716f3ba614a2278bcba7b937b1af62d84e02e)) ([394b93e](https://github.com/continew-org/continew-admin/commit/394b93ead55c7be1d5b99bc3090ee9f51c4f95a6)) ([78b8c70](https://github.com/continew-org/continew-admin/commit/78b8c7000b92b447b61e85ad0b365894518efc03))
- 新增普通用户角色并调整第三方登录用户默认角色 ([594ea32](https://github.com/continew-org/continew-admin/commit/594ea3208d6cca33b50394f81fa69c38b6046ec5))
- 新增 MissingServletRequestParameterException、HttpMessageNotReadableException 异常处理 ([754ef06](https://github.com/continew-org/continew-admin/commit/754ef0639baf097fb67f59bedc7c1a7e11714c5b))
- 【generator】支持生成枚举类型属性 (Gitee#53@lcyearn) ([1de7b20](https://github.com/continew-org/continew-admin/commit/1de7b20fb0845e1d9ac1db553cbbf7ee98678b4f))
- 新增个人消息接口，调整个人信息接口地址 ([e8aa739](https://github.com/continew-org/continew-admin/commit/e8aa739860a9e01b9bf63a55ffae37ee92df4c44))
- 新增 Excel 字典数据转换器 ([449478b](https://github.com/continew-org/continew-admin/commit/449478b188d15fa10069fe5ad7784e436387b9e0))

### 💎 功能优化

- 上传文件后返回id及缩略图 (GitHub#137@luoqiz) ([d83fd0d](https://github.com/continew-org/continew-admin/commit/d83fd0d5cb93fc4ba617e110bbaaff3a867a90f6))
- 优化文件大小限制的错误提示信息 ([b6f01bc](https://github.com/continew-org/continew-admin/commit/b6f01bc2d061dac5a09ca712144ab9de41382b9b))
- 文件添加路径和md5值 (GitHub#138@luoqiz) ([42970d9](https://github.com/continew-org/continew-admin/commit/42970d91ebde56436586c99737aa141cc5a7f2af))
- 优化文件相关类的属性顺序和注释，增强代码可读性 ([a75c2cc](https://github.com/continew-org/continew-admin/commit/a75c2cc4536fd81c8676916c7e79d925750779c1))
- 优化数据库配置文件 ([de9c9c5](https://github.com/continew-org/continew-admin/commit/de9c9c5abd52e0f7b03873da55b1df13259d0c6b))
- 优化文件相关代码 ([74c4270](https://github.com/continew-org/continew-admin/commit/74c4270323782f37947214c43024258831a26006))
- 【generator】更新代码生成列表模板 ([6b17742](https://github.com/continew-org/continew-admin/commit/6b17742a1b8f83212c6a3df51a9192f7aa51dc8b))
- 调整用户相关类到 user 包下 ([b879f02](https://github.com/continew-org/continew-admin/commit/b879f02c682e5eb7be5bb8e60b5646c7c157e2c9))
- 将短信验证码长度从 4 位增加到 6 位 (PR by Gitee@hagyao520) ([611c3d8](https://github.com/continew-org/continew-admin/commit/611c3d8a0a9914ac911129b33f25462bab38eb11))
- 优化字典项颜色 ([1861a80](https://github.com/continew-org/continew-admin/commit/1861a809795804d4ac380e3f433b3b5df94370a4))
- 移除 DateTimeFormat 注解的使用，日期类型统一改为 LocalDateTime ([7d4ae0b](https://github.com/continew-org/continew-admin/commit/7d4ae0b35a9180022b87e64bc85165270f6fe404))
- 用 @Email 和 @Mobile 注解替换了部分验证，提高了代码可读性，修改了多处错误提示信息，使其更加友好 ([19639c9](https://github.com/continew-org/continew-admin/commit/19639c946a0117a1efed2aad764f766d08e0a73a))
- 重构任务调度模块，使用 OpenFeign 替代 WebClient ([c041496](https://github.com/continew-org/continew-admin/commit/c041496f65e73f7d513cf9ecdce9c324a8c6c5de))
- 账号锁定提示增加解锁时间显示 ([eef9232](https://github.com/continew-org/continew-admin/commit/eef92326902fd207e22e20a9cacd0f3892e59d3a))
- 修改头像接口调整为 Patch 请求方式 (Gitee#56@httpsjt) ([aed2753](https://github.com/continew-org/continew-admin/commit/aed27533a64ff80a45c4ba4964e8d4db0f1d56dc))
- 完善系统配置菜单及权限配置 ([36f975c](https://github.com/continew-org/continew-admin/commit/36f975c30bd5b2462d856365f88bf3dbb03525ed))
- 移除终端部分配置字段 ([240d6fc](https://github.com/continew-org/continew-admin/commit/240d6fcb01f5cf254a9e6230f5b6e9b25432e7d1))
- 优化演示环境数据清理定时任务逻辑 ([715b950](https://github.com/continew-org/continew-admin/commit/715b950fa4382ab3a5a9109424759952786d0b2e))
- 完善配置文件部分配置说明 ([01ef348](https://github.com/continew-org/continew-admin/commit/01ef34845a17690e0010f38845adf7b3d0c0f067))
- 优化 Nginx 配置文件 ([ca022cf](https://github.com/continew-org/continew-admin/commit/ca022cfcd5fbfab88f41019b96ba6ce2da1dcce9))
- 从 application.yml 中删除了逻辑删除相关的配置项（4.x版本回收站功能会再增加回来） ([3854040](https://github.com/continew-org/continew-admin/commit/3854040d111ce2cd668c6e2b30def0cdd40a50f8))
- 优化通知公告菜单名称 ([9ce4ec5](https://github.com/continew-org/continew-admin/commit/9ce4ec538c4c31114a65f4d1b8e3890ce071097c))
- 头像不再存储为 base64，而是存储到文件管理中 (GitHub#142@luoqiz) ([27cf464](https://github.com/continew-org/continew-admin/commit/27cf46409ac755878f0128d5c4828503068a237e))
- 重构删除接口，由 URL 传参重构为请求体传参

### 🐛 问题修复

- 修复访问日志配置参数错误 (Gitee#48@dom-w) ([c130f9c](https://github.com/continew-org/continew-admin/commit/c130f9c0bbec5e388f0a292797631479a24bfbf8))

- 【generator】修复前端模板字典码使用错误 ([2e76caa](https://github.com/continew-org/continew-admin/commit/2e76caa35ed39dc5bb7aec242557af67ce98337b))
- 修复枚举字典初始化时可能存在空字典的问题 ([1ce636f](https://github.com/continew-org/continew-admin/commit/1ce636feec02d585b6a3fd83adf8402c91281121))
- 增加设置默认存储前的状态检查 ([603b12d](https://github.com/continew-org/continew-admin/commit/603b12d10dfa0453c9596615932673d17995bf05))
- 修复删除用户时未及时清除第三方账号信息的错误 (PR by Gitee@hagyao520) ([2ec0b34](https://github.com/continew-org/continew-admin/commit/2ec0b343e0286b2f31caa3def4dffac6cdd0915f))
- 修复文件路径中存在bucketName返回错误relativePath的问题 (Gitee#51@limin04551) ([da67487](https://github.com/continew-org/continew-admin/commit/da674879a37d6c4c5a7209c92ce4893f9f946fcf))
- 修复文件管理域名未配置斜杠未后缀无法正常显示的问题 ([6d58a3b](https://github.com/continew-org/continew-admin/commit/6d58a3b7ddc4523567602efb5d49a64681f88618))
- 通知列表查询的数据不准确的问题 (Gitee#54@kiki1373639299) ([7cb34b3](https://github.com/continew-org/continew-admin/commit/7cb34b3aa54a019ff57a57ddf2146d4b781e0f4f))
- 修复 JustAuth 部分三方用户授权登录问题 (GitHub#148@Top2Hub) ([e2bac69](https://github.com/continew-org/continew-admin/commit/e2bac69de5a3efbc769214c2d56ea8380be2719a))

### 📦 依赖升级

- 使用 BellSoft Liberica JDK 17 替代 OpenJDK (Gitee#55@dom-w) ([67e6507](https://github.com/continew-org/continew-admin/commit/67e65072407b50c1c2a35828ec988e83878a0f9a))

- 🔥ContiNew Starter 2.9.0 => 2.11.0 (更多特性及依赖升级详情，请查看 ContiNew Starter [更新日志](https://github.com/continew-org/continew-starter/blob/dev/CHANGELOG.md))

## [v3.5.0](https://github.com/continew-org/continew-admin/compare/v3.4.1...v3.5.0) (2025-03-05)

### ✨ 新特性

* 【generator】生成预览支持批量 ([a7296a3](https://github.com/continew-org/continew-admin/commit/a7296a36278f68108a0513fe26b3cccd9af5244c))
* 【generator】代码生成新增 Mapper.xml 模板 ([b519364](https://github.com/continew-org/continew-admin/commit/b51936445d36a76650d94225b3ecfa81b86e066c)) ([98569ae](https://github.com/continew-org/continew-admin/commit/98569ae20530a3c94cd1a071a29fae3c904761e3))
* 🔥新增终端管理，重构认证体系，多端认证鉴权控制 ([Gitee#40](https://gitee.com/continew/continew-admin/pulls/40)) ([95f2617](https://github.com/continew-org/continew-admin/commit/95f2617a4c086e3e5113799fe213c24c42bfbd17)) ([c90e80e](https://github.com/continew-org/continew-admin/commit/c90e80e9d72eba173df586e7f12a0c9378c82ec9)) ([438615f](https://github.com/continew-org/continew-admin/commit/438615f87c5583890703c78c4e61aae36e3dab80)) ([229bd9b](https://github.com/continew-org/continew-admin/commit/229bd9becfda15cbcb049fd1e0842dbe04d7b135)) ([a305eac](https://github.com/continew-org/continew-admin/commit/a305eac96f766ad3a72051fe19f533f4fd64d304)) ([82cf439](https://github.com/continew-org/continew-admin/commit/82cf4390e8fdc73da1818dcc9a5dd0c7ff87db02)) ([5f68e84](https://github.com/continew-org/continew-admin/commit/5f68e84e7dbb5c2d0f793c72df76d69d326e574e))
* 【generator】支持源项目内生成代码文件 ([GitHub#125](https://github.com/continew-org/continew-admin/pull/125)) ([653802e](https://github.com/continew-org/continew-admin/commit/653802efbe2debdbfd1fe5097dae7280e80f1e43))
* 新增接口文档配置，支持显示 SaToken 权限码 ([Gitee#44](https://gitee.com/continew/continew-admin/pulls/44)) ([19c5dbd](https://github.com/continew-org/continew-admin/commit/19c5dbd2659264cfba59e2b1316420c39f82f731))
* 新增NoHandlerFoundException、HttpRequestMethodNotSupportedException异常处理  ([Gitee#44](https://gitee.com/continew/continew-admin/pulls/44)) ([4efe025](https://github.com/continew-org/continew-admin/commit/4efe025b2e36c56162dc8fcbd05482b7ecc21e5f))

### 💎 功能优化

- 菜单路由为空时默认返回空列表而非 null ([43cc429](https://github.com/continew-org/continew-admin/commit/43cc429234150e185a697b6dbc340bd966bf6133))
- 移除初始数据脚本 update_user、update_time 字段赋值（此优化无需跟进升级） ([9d0e1fc](https://github.com/continew-org/continew-admin/commit/9d0e1fc8e642c5be46d20173fd573582ad54d5e6))
- 【generator】消除前端红色报警、更新表格创建者和更新者字段索引，自定义单选框数据 ([GitHub#108](https://github.com/continew-org/continew-admin/pull/108)) ([4c8ebf2](https://github.com/continew-org/continew-admin/commit/4c8ebf2d0f3e65737f6a55c06eb1ff2d31273505))
- 更新 nginx.conf 部署配置文件 ([4920d7b](https://github.com/continew-org/continew-admin/commit/4920d7b730c2e975c4a50a5b8b8172f5365509c5))
- 调整 starter 内的 BaseResp、BaseDetailResp 到 admin 项目 ([144251b](https://github.com/continew-org/continew-admin/commit/144251b21ec0d79927164a705f3c846aace53ca1))
- 调整 starter 内的 CommonUserService、ContainerPool 到 admin 项目 ([f1d0b49](https://github.com/continew-org/continew-admin/commit/f1d0b491b14d806fbe1d0011cdadea64336fe3b0))
- 优化登录日志描述 ([a24136d](https://github.com/continew-org/continew-admin/commit/a24136d6fe92bcb055c965600caef399df563361))
- 丰富部门、角色、用户初始测试数据，方便开发场景 ([b5bbdb2](https://github.com/continew-org/continew-admin/commit/b5bbdb27e6e002d52b5a1894e2bf8e24e6f963e8))
- 调整 starter 内的 BaseDO、BaseCreateDO、BaseUpdateDO 到 admin 项目 ([498e680](https://github.com/continew-org/continew-admin/commit/498e680672df00be9e8f9546f2010fcc54faab23))
- 🔥重构角色管理，更新权限扁平化 ([f6535ef](https://github.com/continew-org/continew-admin/commit/f6535ef7a35794147d8094eed71a4789c06f3db8)) ([0a62f81](https://github.com/continew-org/continew-admin/commit/0a62f81ad7c1f7b086379059fe780a609e5d575b)) ([144cfa2](https://github.com/continew-org/continew-admin/commit/144cfa27ce944dc5b78d2d26466cd149b8ca7959))
- 优化任务调度服务配置，允许用户名密码使用环境变量 ([GitHub#129](https://github.com/continew-org/continew-admin/pull/129)) ([0e65190](https://github.com/continew-org/continew-admin/commit/0e651902f267feb8b4997d066285d979429bd56f))
- 优化系统配置 SQL 数据脚本 ([d336911](https://github.com/continew-org/continew-admin/commit/d3369119e090b62468fda38e1dfb52ddb5dc7df3))
- 调整日志 module 字段长度 50 => 100 ([65941c1](https://github.com/continew-org/continew-admin/commit/65941c1ee4020e2f9b4fc07f15c249b9d0d7e851))
- 🔥重构存储管理，新增设置默认存储、修改状态接口 ([37d6efb](https://github.com/continew-org/continew-admin/commit/37d6efb70e5bcfddc8a4ca6becaf440ab05785fb))

### 🐛 问题修复

- 【generator】修复 PostgreSQL 菜单 SQL 脚本模板错误 ([GitHub#107](https://github.com/continew-org/continew-admin/pull/107)) ([af403d0](https://github.com/continew-org/continew-admin/commit/af403d055af1c186d7b5976cff894e9dd2afcc01))
- 【generator】生成菜单脚本添加ID ([GitHub#109](https://github.com/continew-org/continew-admin/pull/109)) ([9ebecdc](https://github.com/continew-org/continew-admin/commit/9ebecdc1935a99f15ece4e2c933175d9919e9825))
- 【generator】前端页面生成表单类型 ([GitHub#110](https://github.com/continew-org/continew-admin/pull/110)) ([75d2662](https://github.com/continew-org/continew-admin/commit/75d26623652d4813643cea9a7c3f821e44edc885))
- 完善部分 in 查询前的空集合处理 ([899354a](https://github.com/continew-org/continew-admin/commit/899354a6e7239ed00f81155e8ed5f2760b191480))
- 修复公告通知范围字段类型错误 ([fdd0617](https://github.com/continew-org/continew-admin/commit/fdd0617a2832221bb017d2e17dc5fb82af862707))
- 修复通知公告分页 通知范围字段类型回显错误 ([160ab8d](https://github.com/continew-org/continew-admin/commit/160ab8d38bb68801ed6efe0ceeb4ccb4c3f4fba3))
- 🔥修复 PageResp 手动分页计算错误 ([6bcff72](https://github.com/continew-org/continew-admin/commit/6bcff7244f0a37474c39509dfed44d3cf630d898))
- 修复导入用户部门名称校验注解使用错误 ([Gitee#41](https://gitee.com/continew/continew-admin/pulls/41)) ([c870014](https://github.com/continew-org/continew-admin/commit/c870014730a47d9a9b567416b2f6736049a25125))
- 修复 PostgreSQL Liquibase 数据脚本缺失 ([8c53700](https://github.com/continew-org/continew-admin/commit/8c53700cfd8ea20ccece1867161d2315d5346d9b))
- 修复新增用户时日志记录获取 description 为空的问题 ([91924ac](https://github.com/continew-org/continew-admin/commit/91924acaa15658db88c387f81ff723ca043cd1f1))
- 调整 PostgreSQL 连接配置以消除部分类型使用报错 ([7e3257b](https://github.com/continew-org/continew-admin/commit/7e3257bd6d3965622ba53a906fd8b27e5209e67f))
- 修复部分过期配置信息 ([3fb9922](https://github.com/continew-org/continew-admin/commit/3fb9922b524a5c1a40de5bd011c99c9863032f7a))
- 修复邮箱登录，手机号登录对应日志没有记录操作人问题 ([Gitee#42](https://gitee.com/continew/continew-admin/pulls/42)) ([aab3931](https://github.com/continew-org/continew-admin/commit/aab3931f3078c1b3468c5f06e1f133862f947d7e))
- mysql 8.x failing to connect to the database correctly issue ([GitHub#128](https://github.com/continew-org/continew-admin/pull/128)) ([4caada8](https://github.com/continew-org/continew-admin/commit/4caada8c64c1f6646f312a0038338396ae860305))

### 📦 依赖升级

- 🔥ContiNew Starter 2.7.5 => 2.9.0 (更多特性及依赖升级详情，请查看 ContiNew Starter [更新日志](https://github.com/continew-org/continew-starter/blob/dev/CHANGELOG.md))

## [v3.4.1](https://github.com/continew-org/continew-admin/compare/v3.4.0...v3.4.1) (2024-12-08)

### ✨ 新特性

* 新增验证码配置开关 ([e314346](https://github.com/continew-org/continew-admin/commit/e31434617e751f08d12ace7773bb3ba7bf132370)) ([61fe39d](https://github.com/continew-org/continew-admin/commit/61fe39d439b73c90cfeb989f8f4727ade6b5b3b2)) (Gitee#37@aiming317)

### 💎 功能优化

- 【open】优化 API 参数签名处理 ([22b3564](https://github.com/continew-org/continew-admin/commit/22b3564a2217dee739fc2172453b23600d82d6de))
- 移除关于项目菜单初始数据（该菜单从动态路由调整为前端静态，且不再需要鉴权） ([88313c8](https://github.com/continew-org/continew-admin/commit/88313c8b2017e7ec620e5372f34bf5e431ce3e7f))
- 优化代码生成菜单图标 ([9296985](https://github.com/continew-org/continew-admin/commit/9296985be0ab63ba54c63c71c011681f91aef7fb))
- BaseServiceImpl 所在包调整 ([d7ae7b4](https://github.com/continew-org/continew-admin/commit/d7ae7b4e42c424a3db51c72a0ed79572c9fd7601))
- BaseController 改为在 Admin common 模块编写（重构权限校验 checkPermission 处理） ([d7ae7b4](https://github.com/continew-org/continew-admin/commit/d7ae7b4e42c424a3db51c72a0ed79572c9fd7601))
- CRUD ValidateGroup => CrudValidationGroup ([d7ae7b4](https://github.com/continew-org/continew-admin/commit/d7ae7b4e42c424a3db51c72a0ed79572c9fd7601))
- ValidateGroup => ValidationGroup ([d7ae7b4](https://github.com/continew-org/continew-admin/commit/d7ae7b4e42c424a3db51c72a0ed79572c9fd7601))

### 🐛 问题修复

- 【generator】修复 columnSize 类型错误，兼容无注释字段配置 ([6b64ae3](https://github.com/continew-org/continew-admin/commit/6b64ae3e07a76d844eec4bd05302126cbcaca31b))
- 补充能力开放模块接口文档配置 ([270fbf1](https://github.com/continew-org/continew-admin/commit/270fbf15af338a6ac3e6a686409eea8e9a32b6bf))
- 修复文件管理删除文件异常或不成功的情况 ([361a412](https://github.com/continew-org/continew-admin/commit/361a41258e9fdece5ba681298f2839b013d6cfab)) (Gitee#35@kiki1373639299)
- 修复本地文件管理删除文件异常或不成功的情况 ([c7b58a0](https://github.com/continew-org/continew-admin/commit/c7b58a0fd167c566f6680c87cc455b71c42b8eda)) (Gitee#36@kiki1373639299)
- 修复 Query 查询数组范围报错 ([d7ae7b4](https://github.com/continew-org/continew-admin/commit/d7ae7b4e42c424a3db51c72a0ed79572c9fd7601))

### 📦 依赖升级

- ContiNew Starter 2.7.4 => 2.7.5 (更多特性及依赖升级详情，请查看 ContiNew Starter [更新日志](https://github.com/continew-org/continew-starter/blob/dev/CHANGELOG.md))

## [v3.4.0](https://github.com/continew-org/continew-admin/compare/v3.3.0...v3.4.0) (2024-11-18)

### ✨ 新特性

* 新增仪表盘分析接口，查询访问时段分析、查询模块分析、查询终端分析、查询浏览器分析 ([dea8dbe](https://github.com/continew-org/continew-admin/commit/dea8dbe131867a564f7e151a6484db5be6effaa3))
* 新增查询仪表盘数据总览相关接口, 重构仪表盘相关代码 ([e01df09](https://github.com/continew-org/continew-admin/commit/e01df09127e6efc33971d64e2fe49a2a42282425))
* 公告支持设置通知范围 ([29202ae](https://github.com/continew-org/continew-admin/commit/29202aea307a7257c9d1e9649dee00140164c59c)) (GitCode#1)
* 角色管理增加分配角色功能 ([73f880e](https://github.com/continew-org/continew-admin/commit/73f880ec57cfdccfc297aac228410f5bb7fed448)) ([ad3f832](https://github.com/continew-org/continew-admin/commit/ad3f8329dd07858b0982020db3605200112f09b5)) (GitHub#93)
* 新增能力开放模块应用管理功能 ([f774183](https://github.com/continew-org/continew-admin/commit/f7741832bdd315039cc5f1aa062d8ffac32ddf0f)) ([d1b3824](https://github.com/continew-org/continew-admin/commit/d1b38242b9f291c897a8eb82bd330bd775e656cf)) ([4454daa](https://github.com/continew-org/continew-admin/commit/4454daa9e07af7c7202c386e533ea055bb79f0df)) (Gitee#31)
* 新增查询用户字典接口 ([d4b02ba](https://github.com/continew-org/continew-admin/commit/d4b02ba9180f82084e5ca844eaa9b4a0966a0164))
* 代码生成新增菜单SQL模板 ([fb947c9](https://github.com/continew-org/continew-admin/commit/fb947c98fdd075a19c55e1e9e5c137a8482db618)) (GitHub#95)

### 💎 功能优化

- 优化部分 Mapper 方法使用 (替换为 MP 新增方法) ([ad69d44](https://github.com/continew-org/continew-admin/commit/ad69d44ebda72dbdb639ba4ff48cde9aa7f6e400))
- 优化查询代码生成表性能 ([781d291](https://github.com/continew-org/continew-admin/commit/781d29142fbcc1f981d3760565f2f96b49570438))
- 移除上传文件时的多余校验 ([8466105](https://github.com/continew-org/continew-admin/commit/8466105a9b8b2fd807ac9d3029e4da7bc609d551))
- 重构获取登录用户信息方式（线程级存储） ([79ea39d](https://github.com/continew-org/continew-admin/commit/79ea39dd078639b4e137d576f3d7820bb6c24d0a))
- 完善及优化代码生成模板 ([ffdc971](https://github.com/continew-org/continew-admin/commit/ffdc9712d4cd1fd3093cec0780f630d672339cdf)) ([2b47ed7](https://github.com/continew-org/continew-admin/commit/2b47ed711074ad64d98fa0d9d68ccd2777b70bf2)) ([90e3bc0](https://github.com/continew-org/continew-admin/commit/90e3bc0595fdb692644ed7fd9b1a3735962cb68b)) ([985bc25](https://github.com/continew-org/continew-admin/commit/985bc25716daae6fb018a856af889919436f26e6))
- 字典项管理日志模块调整为字典管理 ([60cb2e3](https://github.com/continew-org/continew-admin/commit/60cb2e3b5cae56d9a3117aeea6c57ec02cb9abe4))
- 解决查询日志数据时索引失效的问题 ([4525cb3](https://github.com/continew-org/continew-admin/commit/4525cb3531c06354e3dc57147c93dd5f7f8a4e8a))
- 重构拆分 liquibase 脚本结构 ([aadaa5b](https://github.com/continew-org/continew-admin/commit/aadaa5b4a70caaa775a38fd542890aa8d7f951c6))
- 调整系统配置菜单图标 ([872bc1c](https://github.com/continew-org/continew-admin/commit/872bc1ca8143632ea0ca00eb320e57f61729937c))
- 优化系统管理、代码生成相关代码及初始数据脚本 ([9ecdeb5](https://github.com/continew-org/continew-admin/commit/9ecdeb52f601b93116f6e89d8db32d8db95cb0c5)) ([5717d03](https://github.com/continew-org/continew-admin/commit/5717d03d01f8f052688bef873def83a3b8defc21)) ([7870de2](https://github.com/continew-org/continew-admin/commit/7870de28926cde86a8732ad5d6950616e1078d57))
- 优化项目模块命名（简化、分类、统一） ([c276e53](https://github.com/continew-org/continew-admin/commit/c276e53a8e02e64f9e5d8270171e20424804382d))
- 优化任务调度配置及 docker 部署脚本 ([b927470](https://github.com/continew-org/continew-admin/commit/b927470e33bb75594f1546a4f06d48bc2281f5ab)) ([c5cd4e2](https://github.com/continew-org/continew-admin/commit/c5cd4e2c284fa195411f704e41ca50a70271c6b7))
- 重构仪表盘查询地域分析接口 ([e0e157f](https://github.com/continew-org/continew-admin/commit/e0e157f0e5b23ff6e27ec7ae0ee028af6f2facd1))
- 完善 PostgreSQL 代码生成类型映射配置 ([4c36f23](https://github.com/continew-org/continew-admin/commit/4c36f2339830272aa047e46d02f34485b9051ec3))
- 优化通知公告部分代码 ([e1941ec](https://github.com/continew-org/continew-admin/commit/e1941eca455a066dae631b1533d63f8d9a193161))
- 优化初始数据脚本 ([6abb444](https://github.com/continew-org/continew-admin/commit/6abb444f9dce7ed1ea4aa90502c21d2ab6c8e247))
- 忽略获取在线用户信息异常 ([4856366](https://github.com/continew-org/continew-admin/commit/48563663e1fed93154f63f6c9a6c07ea79d741da))
- 优化部分注释 ([3116836](https://github.com/continew-org/continew-admin/commit/3116836b0139232769797da271a400fa4d9d52fa))

### 🐛 问题修复

- 参数配置支持设值为空 ([d7e8fc9](https://github.com/continew-org/continew-admin/commit/d7e8fc9bc31409b3652b5ad03a79248726547088))
- 修复修改存储时同时设置默认存储及启用判断顺序错误 ([d9602e8](https://github.com/continew-org/continew-admin/commit/d9602e8639bcd125b15faea5ca7f618429bcc50e))
- 修复任务日志缺失异常堆栈的问题 ([5cbeddb](https://github.com/continew-org/continew-admin/commit/5cbeddb97bd38274641ac5d63226a937975d69ba)) (Gitee#29)
- 修复更新在线用户权限信息报错的问题 ([8278032](https://github.com/continew-org/continew-admin/commit/82780324b7c2faeba22f2dbde440d1cf0e42c3c9))
- 修复查询日志排序错误 ([8b403f4](https://github.com/continew-org/continew-admin/commit/8b403f4357caeac0c0a4cc9ac67c73043a1f4465))
- 修复部分错误规范代码 ([a83b45f](https://github.com/continew-org/continew-admin/commit/a83b45f776234274a844337a2f2b541705ba5aff))
- 调整部分实体包 ([3f4331e](https://github.com/continew-org/continew-admin/commit/3f4331e92b86e73303c4d675f0f1d4bc91a2a71b))
- 修复获取邮箱验证码未进行行为验证码校验错误 ([731bfa0](https://github.com/continew-org/continew-admin/commit/731bfa065ab3a10ab933aaffd2e9ceebf0a4d16d))
- 完善用户角色变更校验及在线用户权限处理 ([c28d3cf](https://github.com/continew-org/continew-admin/commit/c28d3cf1c45212e670b90fc0077b5e176a894bd2))
- 修复查询系统配置参数漏洞 :boom: ([8c3fe35](https://github.com/continew-org/continew-admin/commit/8c3fe353be5d68f1ed252eef12f5fcdc0a1e3c83))

### 📦 依赖升级

- ContiNew Starter 2.6.0 => 2.7.4 (更多特性及依赖升级详情，请查看 ContiNew Starter [更新日志](https://github.com/continew-org/continew-starter/blob/dev/CHANGELOG.md))

## [v3.3.0](https://github.com/continew-org/continew-admin/compare/v3.2.0...v3.3.0) (2024-09-09)

### ✨ 新特性

* 重构全局响应处理及异常拦截，自定义异常拦截从 Starter 调整到 Admin 项目 ([d7621c6](https://github.com/continew-org/continew-admin/commit/d7621c6b26bfd253d9295444ea144f5dabf67f44))
* 重构 Controller 接口方法返回值写法，接口文档也已适配处理 ([d7621c6](https://github.com/continew-org/continew-admin/commit/d7621c6b26bfd253d9295444ea144f5dabf67f44)) ([0f1479f](https://github.com/continew-org/continew-admin/commit/0f1479f40deef83a5f5d64cbc24a7691b274b112))
* 代码生成字段配置时支持指定排序 ([d56b9aa](https://github.com/continew-org/continew-admin/commit/d56b9aa35ee2502804f487487d7bac02f4edc9b0))
* 代码生成字段配置时支持选择关联字典 ([fdd21a0](https://github.com/continew-org/continew-admin/commit/fdd21a01c106e12321d0d1886ff643c72a09943b)) ([ecc98b1](https://github.com/continew-org/continew-admin/commit/ecc98b1999d90c1a7a29af94dc8705283f34dada))
* 修改角色功能权限、数据权限支持衔接新增角色时的父子联动选项 ([387fb19](https://github.com/continew-org/continew-admin/commit/387fb194640d4a288f053c3eba1bf5b314d64da7))

### 💎 功能优化

- 移除 WebMvcConfiguration 配置（已迁移到 Starter 项目）([d7621c6](https://github.com/continew-org/continew-admin/commit/d7621c6b26bfd253d9295444ea144f5dabf67f44))
- 重构日志持久层接口本地实现类 ([2c1eb56](https://github.com/continew-org/continew-admin/commit/2c1eb5660f69a9ab702d503944a11e47edac1142))
- 优化打包配置，模板等配置文件提取到 jar 包外部 ([75cef77](https://github.com/continew-org/continew-admin/commit/75cef773187e5b5060a10a12e7c9912002376d7a))
- 优化健康监测接口响应信息 ([bb5a92e](https://github.com/continew-org/continew-admin/commit/bb5a92e5ca238ed677d9ac3589fdf8009d2ac232))
- 优化代码生成列配置代码，取消后端部分默认值 ([f5ee2b5](https://github.com/continew-org/continew-admin/commit/f5ee2b5beb9572d3fcd5b7c2f6db0627dedb31aa)) ([ca9f34d](https://github.com/continew-org/continew-admin/commit/ca9f34d3d5a3f96c6df537036a5fd876cae2e89a))
- 重构权限变更逻辑，修改角色、变更用户角色不再下线用户 ([ad9a600](https://github.com/continew-org/continew-admin/commit/ad9a6000fcb5d64b04cf230caa3cbacc8c3ac8d7))

### 🐛 问题修复

- 修复打包部署后，下载用户导入模板异常问题 (Gitee#25) ([c7ffc67](https://github.com/continew-org/continew-admin/commit/c7ffc67cdc9139a4398c7dc819ca453880bd100a))
- 修复日志记录仅支持获取 JSON 结构响应体的问题 ([d7621c6](https://github.com/continew-org/continew-admin/commit/d7621c6b26bfd253d9295444ea144f5dabf67f44))
- 修复并增强 SQL 注入防御 ([0f1479f](https://github.com/continew-org/continew-admin/commit/0f1479f40deef83a5f5d64cbc24a7691b274b112))
- 修复目录、菜单的组件名称重复的错误问题 ([9e91f56](https://github.com/continew-org/continew-admin/commit/9e91f563e2a263ce302dc3bf17c89e37c2b56285))
- 修复 DataPermission 注解表别名配置无效的问题 ([6c4e252](https://github.com/continew-org/continew-admin/commit/6c4e2522df3f44ba0f5a21228e805b8ac98f8e6b))
- 临时移除 MyBatis Plus saveBatch 不兼容的 rewriteBatchedStatements 配置 ([25240fa](https://github.com/continew-org/continew-admin/commit/25240fa81957a1677deda294ce8f2b0af5413315))
- 修复更新会导致原加密失效的问题 ([8903195](https://github.com/continew-org/continew-admin/commit/89031954c0b7daee1c08e1a10fd50139301cd6ab)) ([c87317d](https://github.com/continew-org/continew-admin/commit/c87317d19946989e86dfbc5f24b155b2ea5abdc9))
- 修复角色查询参数与前端不一致的问题 ([098571f](https://github.com/continew-org/continew-admin/commit/098571ffb2febc6163d2b9e5b18c4796ea80cbfa))
- 修复特殊校验异常不打印堆栈 ([c87317d](https://github.com/continew-org/continew-admin/commit/c87317d19946989e86dfbc5f24b155b2ea5abdc9))
- 修复日志全局 includes 配置会被局部修改的问题 ([c87317d](https://github.com/continew-org/continew-admin/commit/c87317d19946989e86dfbc5f24b155b2ea5abdc9))
- 修复初始数据错误 ([403c72a](https://github.com/continew-org/continew-admin/commit/403c72aa52a0f0852208f15fa1c7117ee26414f0))

### 📦 依赖升级

- ContiNew Starter 2.4.0 => 2.6.0 (更多特性及依赖升级详情，请查看 ContiNew Starter [更新日志](https://github.com/continew-org/continew-starter/blob/dev/CHANGELOG.md))

## [v3.2.0](https://github.com/continew-org/continew-admin/compare/v3.1.0...v3.2.0) (2024-08-05)

### ✨ 新特性

* 新增用户批量导入功能 (GitHub#78) ([c2ad055](https://github.com/continew-org/continew-admin/commit/c2ad055cf82187e132e4cde9e15251e554deadff))
* 新增任务调度模块 SnailJob（灵活，可靠和快速的分布式任务重试和分布式任务调度平台） (Gitee#22) ([ce1acea](https://github.com/continew-org/continew-admin/commit/ce1acea1535083345c33bcc427054831faf5d2e3)) ([ed5594b](https://github.com/continew-org/continew-admin/commit/ed5594b31e1d31b2a9210b65838d545067ca812f)) ([797221b](https://github.com/continew-org/continew-admin/commit/797221b4dc66c582b95b872b3cb60429247b14e9)) ([7b381b3](https://github.com/continew-org/continew-admin/commit/7b381b36de4ed4ef657249028188abc2f274b036)) ([ffe75e1](https://github.com/continew-org/continew-admin/commit/ffe75e111eb333236e923a1ed14dae5257b09357)) ([cef5cb4](https://github.com/continew-org/continew-admin/commit/cef5cb4fa5e6ee2325fd3738e1c8601a75277dd8)) ([513d8d9](https://github.com/continew-org/continew-admin/commit/513d8d9324b952438ec5513e9e3c7dfb092d5b83))
* 修改 sys_option sql 脚本以适配 base64 图片 (Gitee#25) ([6848559](https://github.com/continew-org/continew-admin/commit/68485596c47a884b453e4632b59b525527293e17))

### 💎 功能优化

- 优化更新手机号、邮箱语句 ([9995bf0](https://github.com/continew-org/continew-admin/commit/9995bf0200e6256ccc4a26d8847e37fb85b4a226))
- 重构适配 ContiNew Starter 最新线程池配置 ([5604fe9](https://github.com/continew-org/continew-admin/commit/5604fe95784b2627f1c8144144546de05434577e))
- 获取短信、邮箱验证码接口适配 ContiNew Starter 限流器 ([44811fc](https://github.com/continew-org/continew-admin/commit/44811fc93283f508953f5fc9193c0b03305da5b2))
- 移动 SaToken 配置到 webapi 模块 ([d733b7f](https://github.com/continew-org/continew-admin/commit/d733b7f1661a5f0df2b6f12b33ba69da8810e0aa))
- 新增 continew-admin-plugins 插件模块，代码生成迁移到插件模块，为后续插件化改造铺垫 ([52f3be8](https://github.com/continew-org/continew-admin/commit/52f3be8ee3a07334c76cdfc06d15698b7ccd65ea))
- 使用分组校验优化存储管理 ([3a23db1](https://github.com/continew-org/continew-admin/commit/3a23db1a4bdfefc26de67ed9e0317097699d5db6))
- 移动日志配置和依赖至 webapi 模块 ([48aae87](https://github.com/continew-org/continew-admin/commit/48aae877646e93f20a43fe4002d7f19fa98897c3))
- 调整部分 Query 查询参数类型为对应枚举（目前已支持非 JSON 格式枚举参数转换） ([f80316e](https://github.com/continew-org/continew-admin/commit/f80316e34d9757225a1a7b6002061e8626018e47))
- 调整部分枚举类的包位置 ([6b69dd4](https://github.com/continew-org/continew-admin/commit/6b69dd43e1544bd955901d6110fa7d7f65aaa80c))
- 更新通知公告新增、查看菜单数据 ([4554526](https://github.com/continew-org/continew-admin/commit/45545260a36b57b594eb8329e95b7552cf6893f5))

### 🐛 问题修复

- 修复代码生成前端模板部分错误 (Gitee#20) ([b512ea9](https://github.com/continew-org/continew-admin/commit/b512ea99f39aaac04bd4db4a9d73e29ddb340d9e))
- 修复文件管理删除图片时未删除缩略图的问题 ([bc523eb](https://github.com/continew-org/continew-admin/commit/bc523eba30a500a4af62adbd590446c48a5cb0be))
- 修复存储管理私有密钥校验错误 ([eb65cff](https://github.com/continew-org/continew-admin/commit/eb65cff4c776a8d3e259f8c96d2918acfe038b6a))
- 删除用户未删除用户历史密码 ([f53d6b6](https://github.com/continew-org/continew-admin/commit/f53d6b6504d5d504581e2697589dcb9b8fbe82ef))
- 修复菜单缓存更新错误 ([10ff4ce](https://github.com/continew-org/continew-admin/commit/10ff4ce838b950df42994de4dbd2af20ff254949))
- 修复偶发性报错 zip file closed ([b587cb8](https://github.com/continew-org/continew-admin/commit/b587cb82aa5d48548b5ce75dd4863af037ae8274))
- 修复代码生成器前端新增数据模板错误 ([81de8d0](https://github.com/continew-org/continew-admin/commit/81de8d060ba081d14873d14d6e4083302a718bef))

### 📦 依赖升级

- ContiNew Starter 2.1.0 => 2.4.0 (更多依赖升级详情，请查看 ContiNew Starter [更新日志](https://github.com/continew-org/continew-starter/blob/dev/CHANGELOG.md))

## [v3.1.0](https://github.com/continew-org/continew-admin/compare/v3.0.1...v3.1.0) (2024-06-16)

### ✨ 新特性

* 系统配置新增安全设置功能，支持多种密码策略配置，例如：有效期、密码重复使用次数、密码错误锁定等 (GitHub#61) ([1de2a8f](https://github.com/continew-org/continew-admin/commit/1de2a8f2dcf01ef8eeb7e2d662c09af00b38ffb1)) ([90ecaab](https://github.com/continew-org/continew-admin/commit/90ecaab63241b8d351ebaffa4faece8c609882b1)) ([3994142](https://github.com/continew-org/continew-admin/commit/3994142ace6cd6ce9d094b8a4ceed080a2b2ec33)) ([1427c13](https://github.com/continew-org/continew-admin/commit/1427c13b7a0b4e0f569ccc20a3172ad748c98e14)) ([5f5fee6](https://github.com/continew-org/continew-admin/commit/5f5fee63f8e584ffd9542bf132ee724762187d83)) ([c1e9d31](https://github.com/continew-org/continew-admin/commit/c1e9d318e0643c0854a848425955948d05b158d3)) ([48d0f47](https://github.com/continew-org/continew-admin/commit/48d0f476149ff7bc30fe05fe3dec84e5a947cf19))
* 图片文件支持缩略图 (GitHub#63) ([d320c95](https://github.com/continew-org/continew-admin/commit/d320c9596a321dce1028ac36d23bcd04535e87a6)) ([d44fb3a](https://github.com/continew-org/continew-admin/commit/d44fb3a681370b5763f028cc761fbb5fc94a104c))
* 在线用户增加最后活跃时间显示 ([926497a](https://github.com/continew-org/continew-admin/commit/926497a18acbb6ef170de9f68bd97e025e212f3e))
* 新增 WebSocket 消息通知，站内信重新上线 (GitHub#67) ([9970c46](https://github.com/continew-org/continew-admin/commit/9970c461cce5c710f1f8479d4bdd258e65558256)) ([94168e2](https://github.com/continew-org/continew-admin/commit/94168e246f75364605b6fbdb82c9438e2b959c61)) ([5abdb8d](https://github.com/continew-org/continew-admin/commit/5abdb8d86161e7bd03ace14b3c899f6ad13020e2))
* 文件上传按日期拆分目录 (GitHub#68) ([08aa085](https://github.com/continew-org/continew-admin/commit/08aa08550589876a821a37d56a5fae8867292978))
* 代码生成增加了 TREE_SELECT/CHECK_GROUP/INPUT_NUMBER/INPUT_PASSWORD控件 (Gitee#17) ([8632b22](https://github.com/continew-org/continew-admin/commit/8632b22bd5b6d43e31b6aeac930836464849441b)) ([cf18c10](https://github.com/continew-org/continew-admin/commit/cf18c1046b77c9f38d28b0e6608d345df3bbd5a9))
* 系统参数新增根据类别查询方法 ([694cbb2](https://github.com/continew-org/continew-admin/commit/694cbb2850c05f7eb35fb982530a6c204f1f64b0))
* 支持动态邮件 ([1dbb339](https://github.com/continew-org/continew-admin/commit/1dbb33935a05e9fde749191e34682e632c8e1e63))

### 💎 功能优化

- 优化部分命名 ([a3cf39f](https://github.com/continew-org/continew-admin/commit/a3cf39f9f8611f7f34dd9f55166d0fe75b72145b))
- 优化代码生成预览 (Gitee#14) ([ad7412f](https://github.com/continew-org/continew-admin/commit/ad7412f9cbee5d851d060d8e9d717b2553d3d4cb))
- 优化个人中心部分参数命名 ([61dd3a4](https://github.com/continew-org/continew-admin/commit/61dd3a4c3aef2d4a873a1d4de13bc1962179eeb1))
- 根据前端最新 ESLint 配置优化代码生成模板 ([044b4b6](https://github.com/continew-org/continew-admin/commit/044b4b669aa2b77b3f5ac27095202e57045fd10d))
- 优化代码生成模板 ([3ddcdf0](https://github.com/continew-org/continew-admin/commit/3ddcdf0f67d12250da4b7f6f6ff3d63a880e6d4c)) ([6396e9a](https://github.com/continew-org/continew-admin/commit/6396e9a7364bf899023c2399083b43c67efb30cf)) ([2fb4001](https://github.com/continew-org/continew-admin/commit/2fb40015c1ca2bc58b66891e2cfbfac70b029016))
- 使用 Crane4j 优化在线用户数据填充 ([cb81135](https://github.com/continew-org/continew-admin/commit/cb811350f36364fcc85355c081b8b60c7d5bfb2a))
- 用户角色名称调整为角色名称列表返回，并全局优化 Crane4j 组件的使用方式 ([857a1c9](https://github.com/continew-org/continew-admin/commit/857a1c90838c8305f916af9dccfd4ca847ca8c66)) ([0b76d5c](https://github.com/continew-org/continew-admin/commit/0b76d5ca33f5900862d48321e8384ee8ded6ae4e))
- 优化部分方法排序 ([651cc8a](https://github.com/continew-org/continew-admin/commit/651cc8ae71bd3f544bd41adac0ef7044011300a7))
- 字典管理分页查询接口 => 查询列表接口 ([b13d0e9](https://github.com/continew-org/continew-admin/commit/b13d0e9ee530fc1a8b382e08ead065989f2b0e7f))
- 移除部门响应信息中的 getDisabled 方法 ([659144a](https://github.com/continew-org/continew-admin/commit/659144afdaf5d1b4667437053a770cf39681cdd7))
- 文件管理存储路径改为相对路径 (GitHub#69) ([8854f20](https://github.com/continew-org/continew-admin/commit/8854f20ce90f6a1c379ac81a27bb40784a838095))
- 查询文件列表增加存储名称信息返回 ([69bc1e5](https://github.com/continew-org/continew-admin/commit/69bc1e52e122c14a695d2e37589d36cbb64de8a4))
- 系统参数表结构新增ID、类别字段 ([45396f2](https://github.com/continew-org/continew-admin/commit/45396f2dc23b4de47912c1d77b05711c839672ce))
- 优化公告状态判断 ([a07aedb](https://github.com/continew-org/continew-admin/commit/a07aedbf35e32f3d09d2e9ed8857b9a9d2e2e13a))
- 重构系统参数相关接口 ([6d0060b](https://github.com/continew-org/continew-admin/commit/6d0060b21c374afd9880e57490345a140187a7cd))
- 优化用户及部门查询 ([448f9a0](https://github.com/continew-org/continew-admin/commit/448f9a0a819d6816a5ae3ada2e07690a5f70f7df))
- 用户头像改为Base64存储 ([969216d](https://github.com/continew-org/continew-admin/commit/969216d7c67332eae826c11233c8745d9d3ad81c)) ([513ea83](https://github.com/continew-org/continew-admin/commit/513ea83152708194348afeb93d31aed1a9914e57)) ([7a6cafc](https://github.com/continew-org/continew-admin/commit/7a6cafc6e4ff6914fa62a870538b51759af866a8))
- 优化配置文件 ([5b3d4f5](https://github.com/continew-org/continew-admin/commit/5b3d4f57788e30b62a1e1af9c2620a4cc8659bfe))
- 优化登录 Helper ([afbd619](https://github.com/continew-org/continew-admin/commit/afbd619d098ff3d70fc786db9594cebf03c07e10))
- 重构查询参数及字典接口 ([1d60213](https://github.com/continew-org/continew-admin/commit/1d602134377fc13b062981476b8c17947b36006d))
- 重构查询角色字典接口 ([1e73d06](https://github.com/continew-org/continew-admin/commit/1e73d06a972d380bbed253eaffd806d8e0698525))
- 使用 CompletableFuture 实现异步加载用户权限、角色代码和角色信息，以提高登录时的性能和响应速度 ([d5f3c74](https://github.com/charles7c/continew-admin/commit/d5f3c7417ad7b1178b5a53da3fd6f3cb7cd3b19a))


### 🐛 问题修复

- 补充查询文件资源统计权限校验注解 ([60cbf04](https://github.com/continew-org/continew-admin/commit/60cbf0402a350d05a09abec57ba94c7758b46499))
- Postgresql startup script fixes (GitHub#60) ([8caad16](https://github.com/continew-org/continew-admin/commit/8caad16ef226e473a81f79b82485c3d03ded7a42))
- 修复初始菜单数据错误 ([f062797](https://github.com/continew-org/continew-admin/commit/f062797629bd7fc220ceae1e44859146ef4a14ff))
- 字典编码、存储编码及类型、菜单类型不允许修改 ([79d0101](https://github.com/continew-org/continew-admin/commit/79d0101e5eb1baa86979b6a6d3c584a2a483320f))
- 修复行为验证码接口请求次数限制 ([573e634](https://github.com/continew-org/continew-admin/commit/573e634b433c473551244418e695a34bbd3fa675))
- 修复导出用户报错 ([655a695](https://github.com/continew-org/continew-admin/commit/655a695753d12db624fbf2afaf10d38f67241e31))
- 移除部门名称错误正则 ([0285874](https://github.com/continew-org/continew-admin/commit/0285874540c0cadc8aae74077f6368b6e7977c35))
- 修复插入第三方登录用户时报错 ([0cfc7a5](https://github.com/continew-org/continew-admin/commit/0cfc7a5c80c3558028c7225fc459ecb07149ab0d))
- 修复更新手机号、邮箱未加密的问题 ([485d708](https://github.com/continew-org/continew-admin/commit/485d708cd45df922ec8e601b7bd7344e9ebd9299)) ([e6d7205](https://github.com/continew-org/continew-admin/commit/e6d720571d3273f27351e81a19fdd97b9b4336f6))

### 📦 依赖升级

- ContiNew Starter 2.0.0 => 2.1.0 (更多依赖升级详情，请查看 ContiNew Starter [更新日志](https://github.com/continew-org/continew-starter/blob/dev/CHANGELOG.md))

## [v3.0.1](https://github.com/continew-org/continew-admin/compare/v3.0.0...v3.0.1) (2024-05-03)

### ✨ 新特性

* 新增验证码超时显示效果，超时后显示已过期请刷新 (GitHub#56) ([4c6a7fb](https://github.com/continew-org/continew-admin/commit/4c6a7fb91ad195b86d776f8aef6aef81d07b2eb1))
* 文件管理增加资源统计，统计总存储量、各类型文件存储占用 (GitHub#58) ([15c966f](https://github.com/continew-org/continew-admin/commit/15c966f7bb255db3edea249f8d3354324cbdbf5b))

### 💎 功能优化

- 获取图片验证码 URL /img => /image ([9a1a472](https://github.com/continew-org/continew-admin/commit/9a1a472ec996362cb918e79b9ce37bfa2639a10b))
- 移除对部分 API 重复的权限校验 ([53eaef9](https://github.com/continew-org/continew-admin/commit/53eaef9fbdfd6d0866a3d5e424d783e2e7bc0e17))
- 优化代码生成模板 ([dc92731](https://github.com/continew-org/continew-admin/commit/dc9273132dc8e266f2d44c834b9c2733256afdfe)) ([def831f](https://github.com/continew-org/continew-admin/commit/def831f2dca0703f5ef8b84b0e695a32b171461d))


### 🐛 问题修复

- 修复查询用户邮箱、手机号时未自动加密导致的错误 ([faa56d1](https://github.com/continew-org/continew-admin/commit/faa56d16b92cbdb8f7e16c8b43c2916ae692d881))
- 修复根据部门查询用户列表数据错误 ([42ac82e](https://github.com/continew-org/continew-admin/commit/42ac82e7ceef9336741c2514470c0db36ab7075e))
- 修复文件类型处理错误 ([9b60e24](https://github.com/continew-org/continew-admin/commit/9b60e24364bfb4cc7cd9996a43579a062197cdf3))

## [v3.0.0](https://github.com/continew-org/continew-admin/compare/v2.5.0...v3.0.0) (2024-04-27)

### ✨ 新特性

* 系统日志新增导出 API ([bd0f40c](https://github.com/continew-org/continew-admin/commit/bd0f40c6ad397174baf80b04923ef1e94ff28e3c))
* 适配 3.0 前端菜单，并梳理菜单数据
* 适配 3.0 前端代码生成模板，代码预览及生成 ([3dbe72f](https://github.com/continew-org/continew-admin/commit/3dbe72fd570c44b32599d869abd30331137a6c7d))

### 💎 功能优化

- 重构日志管理相关接口 ([7793f82](https://github.com/continew-org/continew-admin/commit/7793f82009bcdb5fcdfe5e91daab211ab1705bf7))
- 优化部门管理相关 API，合并 DeptResp 及 DeptDetailResp ([a2cf072](https://github.com/continew-org/continew-admin/commit/a2cf072609ac33543605ecbb5f8e498237bc3d91))
- 优化存储管理相关 API，合并 StorageResp 及 StorageDetailResp ([f7b5a4f](https://github.com/continew-org/continew-admin/commit/f7b5a4ff8dd93f444c00d103b0609ae81e0dd70c))
- 优化字典管理相关 API ([9ec5945](https://github.com/continew-org/continew-admin/commit/9ec594509f2d4b31f46e3aca66d65d139dc8b94f))
- 移除部门、角色、菜单、用户、存储的状态默认值 ([bd5ede2](https://github.com/continew-org/continew-admin/commit/bd5ede2e2956057376b930ecfed88ca44437cbc1))
- 代码生成新增 MySQL json 数据类型映射 ([fe57350](https://github.com/continew-org/continew-admin/commit/fe5735090d94f5d900d79142e5e42ba2db9c0249))
- 优化角色管理相关 API，角色编码不允许修改 ([df59cee](https://github.com/continew-org/continew-admin/commit/df59cee98565f9f45b04d16533888be39c3d7a6f))
- 优化用户管理相关 API ([5269608](https://github.com/continew-org/continew-admin/commit/5269608c61b1f5a6a9f61cd45b349f28db714232))
- 文件管理查询 API 调整为分页查询 ([f8bea90](https://github.com/continew-org/continew-admin/commit/f8bea901938aec0f0ac21c63179c5bde2a0965a7))
- 移除 Qodana 扫描 ([d88581f](https://github.com/continew-org/continew-admin/commit/d88581f939afb0caa3589c8ee76c9b296bb9997e))
- 移除菜单导出接口 ([4363c91](https://github.com/continew-org/continew-admin/commit/4363c91872e6d83e139b22c95a0d7e83183d8f69))
- 优化系统日志、在线用户、存储管理、部门管理相关代码 ([a2e4f9a](https://github.com/continew-org/continew-admin/commit/a2e4f9a28b744e269c46dc66c60311bb939021a7))
- 优化查询参数字典 API 地址 ([79a3de8](https://github.com/continew-org/continew-admin/commit/79a3de8971c613277bdcea79463b6f06959e7b85))
- 移除角色状态字段 ([e89ba7d](https://github.com/continew-org/continew-admin/commit/e89ba7d5cd793e20c3562c7bd1e4655ed1e5a2a3))


### 🐛 问题修复

- 使用字典时，仅查询启用状态字典 ([17c795f](https://github.com/continew-org/continew-admin/commit/17c795fedef5b6801f2053d97b9d78d067775ca1))
- 获取 Authorization 请求头内容兼容小写请求头场景 ([e68c445](https://github.com/continew-org/continew-admin/commit/e68c4455a8af1b4d7a25cd63f9fc9e5aabb441ab))
- 修复查询用户权限存在空值的问题 ([fce4a56](https://github.com/continew-org/continew-admin/commit/fce4a566d7204791650153f0a5507a5d05d2d6c3))
- 存储管理 S3 存储功能修复 (GitHub#51) ([f71c4c2](https://github.com/continew-org/continew-admin/commit/f71c4c226ffd7c27f6726873be6af125affaf148))
- 修复 sys_role_menu 表初始数据错误 ([70ed667](https://github.com/continew-org/continew-admin/commit/70ed667c16388093204eecd97e4914076c62d1ff))
- 修复用户管理/角色管理编辑及状态变更问题 (GitHub#53) ([abf1e65](https://github.com/continew-org/continew-admin/commit/abf1e651e9782a6f7bf2a896018de17130038c57))
- 修复Failed to submit a listener notification task. Event loop shut down? 问题，开发时表现为需要点击两次才能关闭程序 ([f5ab22e](https://github.com/continew-org/continew-admin/commit/f5ab22eedf594cee43592a2f29409ee9c33a88d3))

### 💥 破坏性变更

- 适配 continew-starter 2.0.0，top.charles7c.continew.starter => top.continew.starter ([f5ab22e](https://github.com/continew-org/continew-admin/commit/f5ab22eedf594cee43592a2f29409ee9c33a88d3))
- 移除 monitor 模块 ([b6206a3](https://github.com/continew-org/continew-admin/commit/b6206a334671894306043f86ec07d7c045cd757d))
- top.charles7c.continew.admin => top.continew.admin ([08eeabc](https://github.com/continew-org/continew-admin/commit/08eeabc47d58db3cfc861a3a527e52bf89f6183b))
- 公告管理 Announcement => Notice ([dbe93df](https://github.com/continew-org/continew-admin/commit/dbe93df8bcec0b7dfb24fbd92f35928a3156f4e5))

### 📦 依赖升级

- ContiNew Starter 1.5.1 => 2.0.0 (更多依赖升级详情，请查看 ContiNew Starter [更新日志](https://github.com/continew-org/continew-starter/blob/dev/CHANGELOG.md))

## [v2.5.0](https://github.com/continew-org/continew-admin/compare/v2.4.0...v2.5.0) (2024-03-23)

### ✨ 新特性

* 新增 PostgreSQL 数据源配置示例 ([ee48c80](https://github.com/continew-org/continew-admin/commit/ee48c80cd10a4c4546d1cb24f1f4716bb2ac08ea))
* 新增 PostgreSQL 部署脚本 ([3129e0a](https://github.com/continew-org/continew-admin/commit/3129e0a6dcbd809f0013fbf6c53ad029ae9f7a0e))
* 新增 PostgreSQL 初始 SQL 脚本 ([33b8102](https://github.com/continew-org/continew-admin/commit/33b81029df0b51058b3525b4317b51a2351319dc))
* 新增代码生成器插件模块（后续会改造为独立插件） ([87829d3](https://github.com/continew-org/continew-admin/commit/87829d3ce8ab5a35091800900f7d7708f15ed9c2))
* 代码生成同步最新数据表结构支持同步排序 ([89546de](https://github.com/continew-org/continew-admin/commit/89546deced78f83daca7ac0ba2e7d3d8cd101d0c))
* 新增代码批量生成功能  ([Gitee PR#12](https://gitee.com/continew/continew-admin/pulls/12)) ([040f137](https://github.com/continew-org/continew-admin/commit/040f137934130451700bc28aeabbced30970c5f6))

### 💎 功能优化

- 移除 ` 符号的使用，保持数据库无关性 ([d6b07bd](https://github.com/continew-org/continew-admin/commit/d6b07bd6d1b1f9077a7571702b58c5e9c782b446))
- 优化字符串模板方法 API 使用 ([0f39384](https://github.com/continew-org/continew-admin/commit/0f393845a19432e7c965e811c96774694f4d2372))
- 调整部分 SQL 语句，以兼容 PostgreSQL 数据库 ([9f5049b](https://github.com/continew-org/continew-admin/commit/9f5049bf26c557738867dfe833261d60d071d4a8)) ([bf60d48](https://github.com/continew-org/continew-admin/commit/bf60d48d3a53dd5d73a78e73b6b230e3271ec3de))
- 新增插件仓库配置 ([0439252](https://github.com/continew-org/continew-admin/commit/04392524ac13c2096b549f99d0391fa1d375ca31))
- 优化部分接口响应格式为 kv 格式 ([b40d872](https://github.com/continew-org/continew-admin/commit/b40d872bc4b8dd30ad952d639158619b43cef999))
- 适配 Crane4j 条件注解 ([bf00747](https://github.com/continew-org/continew-admin/commit/bf007470b2362159309ff8231a2f0ad180cfc947))
- 重构代码生成配置 ([7031a51](https://github.com/continew-org/continew-admin/commit/7031a51cd4d7072d4da841736678bb81b2123e9d))
- 重构代码生成功能，由指定路径生成模式调整为下载模式，更方便复杂场景 ([df0c0dd](https://github.com/continew-org/continew-admin/commit/df0c0dd7dcf39620abaf21bd450620ec3fffcf37))


### 🐛 问题修复

- 修复 MySQL 初始 SQL 脚本数据错误 ([49d6bd6](https://github.com/continew-org/continew-admin/commit/49d6bd6874b3df66fd2e2051ea273cb43cb7b4f6))
- 修复参数缓存未及时过期的问题 ([976e9c4](https://github.com/continew-org/continew-admin/commit/976e9c43df5926c533723a75222c59fde05e122e))
- 修复代码生成 text 类型数据的长度校验时，数值显示为 65,535 的问题 ([8026f66](https://github.com/continew-org/continew-admin/commit/8026f660c7af7bba6d4caaf31535a890e5b40a96))

### 💥 破坏性变更

- 调整 liquibase 目录结构，更适合开源类项目适配多种数据库脚本场景 ([1ca48a6](https://github.com/continew-org/continew-admin/commit/1ca48a6620cff62f3648cc28042843163589e150))
- 适配 ContiNew Starter 日志及数据库工具的包结构优化 ([3405868](https://github.com/continew-org/continew-admin/commit/3405868c7f042beafb77a7407a388a40b9a75466))
- 适配 ContiNew Starter Query 组件的包结构优化 ([6be1b6c](https://github.com/continew-org/continew-admin/commit/6be1b6cfb1e7fef4422b8c38e6073a435ebae5c2))

### 📦 依赖升级

- ContiNew Starter 1.4.0 => 1.5.1 (更多依赖升级详情，请查看 ContiNew Starter [更新日志](https://github.com/continew-org/continew-starter/blob/dev/CHANGELOG.md))

## [v2.4.0](https://github.com/continew-org/continew-admin/compare/v2.3.0...v2.4.0) (2024-02-16)

### ✨ 新特性

* 集成 TLog（轻量级的分布式日志标记追踪神器） ([Gitee PR#10](https://gitee.com/continew/continew-admin/pulls/10))
* 系统日志新增 traceId 链路号记录，方便查看完整日志链路 ([860ca40](https://github.com/continew-org/continew-admin/commit/860ca403c2c32cc6395c1608217bc9b6e7c18bd8))
* 取消用户默认密码，改为表单填写密码 ([3d77aa9](https://github.com/continew-org/continew-admin/commit/3d77aa91ee32065b53d9c47a57c33d6d7e4efb0e))
* 适配 ContiNew Starter 加密模块（安全模块） ([6435175](https://github.com/continew-org/continew-admin/commit/6435175dc3d853cb170270e39e8f1505adffeae5)) ([43da462](https://github.com/continew-org/continew-admin/commit/43da462560e224ed92f239cb5af4db64dea51d18))
* 适配 ContiNew Starter 脱敏模块（安全模块） ([2109789](https://github.com/continew-org/continew-admin/commit/2109789116d9ff18773d8afeb854d1dfc70b935a))

### 💎 功能优化

- 优化 API 文档分组配置 ([2df4cce](https://github.com/continew-org/continew-admin/commit/2df4cceedd35b1c2c07bcbf38b5a157604a752c2))
- 优化 QueryTypeEnum 枚举值命名 ([9648cf6](https://github.com/continew-org/continew-admin/commit/9648cf64a4679657f0e609f980805d274563aa53))
- 优化 Query 相关注解使用方式 ([15b1520](https://github.com/continew-org/continew-admin/commit/15b152008c6ae8ab89704d83a969dcfbbb8b5b88))
- 新增 Qodana 扫描 ([f6a9581](https://github.com/continew-org/continew-admin/commit/f6a9581adef87a8915639e6cb2d7c4d02315ebd0))
- 新增 SonarCloud 扫描 ([a154abd](https://github.com/continew-org/continew-admin/commit/a154abde8a39cfecc421c79e01998274b944d2c1)) ([c03c082](https://github.com/continew-org/continew-admin/commit/c03c082d2e2884962547633f5e98663088bd2c3b))
- 移除 Lombok 私有构造注解使用 ([a2420d3](https://github.com/continew-org/continew-admin/commit/a2420d3f4b4652a1d9711f513b8fb22a56105141))
- 获取不到当前登录用户信息则抛出未登录异常 ([d972a44](https://github.com/continew-org/continew-admin/commit/d972a4466a9e8a1a6e6375e4171a4790c2ba156e))
- 优化代码，解决 [Sonar](https://sonarcloud.io/organizations/charles7c/projects)、[Codacy](https://app.codacy.com/gh/continew-org/continew-admin/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade)、[Qodana](https://qodana.cloud/organizations/pQDPD/teams/p5jqd/) 扫描问题，点击各链接查看对应实时质量分析报告（Codacy 已达到 A）
- 优化部署配置 ([b5d668e](https://github.com/continew-org/continew-admin/commit/b5d668e014690d3f1a8a2bab0d0ad0039083e7bb))
- 使用密码编码器重构密码加密、密码判断等相关处理 ([594f7fd](https://github.com/continew-org/continew-admin/commit/594f7fd042f1ff96a298f2e59ffdda112113cb51))
- 优化 SaToken 及图形验证码配置 ([70973db](https://github.com/continew-org/continew-admin/commit/70973db71f2eed49c5878d69d8b93ff04b13a8b9))
- 优化图形验证码使用及部分配置 ([a50d857](https://github.com/continew-org/continew-admin/commit/a50d857c41d164355d36ae5dfd14c6badbe06202))


### 🐛 问题修复

- 修复 API 响应内容类型错误 ([439f7c7](https://github.com/continew-org/continew-admin/commit/439f7c7c58ee27ff56b5093df71bc902c46f48fa))

### 💥 破坏性变更

- 调整自增 ID 为分布式 ID ([4779887](https://github.com/continew-org/continew-admin/commit/4779887751bd3a696e4d31294057e8c03d66eaf3))

### 📦 依赖升级

- ContiNew Starter 1.2.0 => 1.4.0 (更多依赖升级详情，请查看 ContiNew Starter [更新日志](https://github.com/continew-org/continew-starter/blob/dev/CHANGELOG.md))

## [v2.3.0](https://github.com/continew-org/continew-admin/compare/v2.2.0...v2.3.0) (2024-01-21)

### ✨ 新特性

* 代码生成 Request 实体时，针对字符串类型增加数据长度校验注解 ([ee82558](https://github.com/continew-org/continew-admin/commit/ee8255876f618137f811e14ee305509e4e6466d0))
* 适配 Crane4j 数据填充组件，优化部分数据填充处理 ([d598408](https://github.com/continew-org/continew-admin/commit/d5984087a306de31690e9a81d951bd831434a0c9)) ([a2411f7](https://github.com/continew-org/continew-admin/commit/a2411f728a811910a0918668c0811e7df0345640)) ([7a3ccc2](https://github.com/continew-org/continew-admin/commit/7a3ccc2dee8f7b938a91df52fd4903ce09e662e5))
* 移除 Spring Cache，适配 JetCache ([d4bb39d](https://github.com/continew-org/continew-admin/commit/d4bb39d9b4483b7d7fed76b5f2b997538d86d719)) ([1b7aa9d](https://github.com/continew-org/continew-admin/commit/1b7aa9db0c56af733f63f602988dba9e225fe445)) ([8596e47](https://github.com/continew-org/continew-admin/commit/8596e47ed62e19083d6007448c5369d72fa4f2b6))

### 💎 功能优化

- 优化本地存储库注册 ([918e897](https://github.com/continew-org/continew-admin/commit/918e897838628b24a160c3a8f3b3dea1eefd1883))
- 增加华为云镜像源仓库配置 ([16ee2b4](https://github.com/continew-org/continew-admin/commit/16ee2b4b6fe2a663830972ed99d4e80ddf5a3593))
- 优化部分字段名称 ([e3e958b](https://github.com/continew-org/continew-admin/commit/e3e958b419e1ea23fe146b255fce749050302f63))
- 调整代码生成前端 Vue 页面模板 ([7c34574](https://github.com/continew-org/continew-admin/commit/7c345745aadc6272e5f1db674c757ff0f9cea604))
- 更新格式配置，优化全局代码格式 ([35e3123](https://github.com/continew-org/continew-admin/commit/35e31233c531b68b761c73fc0daf7444843b4059))
- 优化配置文件格式 ([a8a4cad](https://github.com/continew-org/continew-admin/commit/a8a4cad840b6d32fbb8d3df24b193f9e7c826d22))
- 使用钩子方法优化部分增、删、改处理 ([61c5724](https://github.com/continew-org/continew-admin/commit/61c57242fa481d2668b1d1ac4ff4802c47fd07bc))
- 完善 flatten Maven 插件配置，以覆盖更多使用情况 ([657accd](https://github.com/continew-org/continew-admin/commit/657accd8a595ab0c2e9ff4d00e49c569eae03123))
- 移除部分无用 Maven 配置 ([5db1f66](https://github.com/continew-org/continew-admin/commit/5db1f669e0bc5022bcd2164757a0f82dfe8d6c30))
- 优化日志配置，滚动策略调整为基于日志文件大小和时间滚动 ([2fa8c25](https://github.com/continew-org/continew-admin/commit/2fa8c254fc53cda3d33c56931569822e645dd902))

### 🐛 问题修复

- 完善代码生成前端路径配置校验 ([bee04d5](https://github.com/continew-org/continew-admin/commit/bee04d5f363b6de88df5249b0fba85607978b303))

### 💥 破坏性变更

- 根据发展需要，拆分前端项目 continew-admin-ui 到独立仓库 ([4067eb9](https://github.com/continew-org/continew-admin/commit/4067eb97bf344dec6ae718433b57bdb7d0b8d6cd))
- PageDataResp => PageResp ([d8c946e](https://github.com/continew-org/continew-admin/commit/d8c946e8014d205c4fd3f38d1f04b3225faede7a))
- 适配 ContiNew Starter IService 接口，CRUD 查询详情方法不再检查是否存在 ([47a133a](https://github.com/continew-org/continew-admin/commit/47a133a065b5c858b588bf77ad51bb9fc38d1222))
- 适配 ContiNew Starter CRUD 模块注解 ([7fa70e7](https://github.com/continew-org/continew-admin/commit/7fa70e74070c7c0f487baa5098f85d7dfb808106))
- 调整部分类的所在包 ([8dc42c7](https://github.com/continew-org/continew-admin/commit/8dc42c7a21e7422399b49690b28899df299e20c7)) ([6efe1ad](https://github.com/continew-org/continew-admin/commit/6efe1ad6f416c52130b2380a699129e7dae29499))

### 📦 依赖升级

- ContiNew Starter 1.1.0 => 1.2.0 (更多依赖升级详情，请查看 ContiNew Starter [更新日志](https://github.com/continew-org/continew-starter/blob/dev/CHANGELOG.md))

## [v2.2.0](https://github.com/continew-org/continew-admin/compare/v2.1.0...v2.2.0) (2023-12-31)

### ✨ 新特性

* 发送短信验证码新增限流处理 ([e719d20](https://github.com/continew-org/continew-admin/commit/e719d207fb76c82b584f2e1ac7210061dc71a89a))
* 代码生成新增生成预览功能 ([4017029](https://github.com/continew-org/continew-admin/commit/401702972f30c4e556a2cf8d048f78fa9ee1c5ba)) ([505ba49](https://github.com/continew-org/continew-admin/commit/505ba49a5304fb3e2ba655dea901cd5e3ea74673))
* 适配 ContiNew Starter 行为验证码，系统内所有短信发送新增前置行为验证码验证 ([Gitee PR#9](https://gitee.com/continew/continew-admin/pulls/9))
* 文件管理：提供文件上传、下载、预览（目前支持图片、音视频）、重命名、切换视图（列表、网格）等功能
* 存储库管理：提供文件存储库新增、编辑、删除、导出等功能

### 💎 功能优化

- 优化 API 文档配置 ([108f1c4](https://github.com/continew-org/continew-admin/commit/108f1c4ae7b855ac0bab2d3fe028270472a8be71))
- 调整枚举配置值为大写 ([3ece42b](https://github.com/continew-org/continew-admin/commit/3ece42b94e071ece87e6b4616f7817bf851ba28f))
- 优化由于 Mock 引起的导出报错提示 ([349899b](https://github.com/continew-org/continew-admin/commit/349899b4fc9572450ca31d9a5e19268ce0b868a8))
- 优化查询访客地域分布信息接口 SQL ([4df887d](https://github.com/continew-org/continew-admin/commit/4df887d82678ced0d30aa0c7a6f92edcac902052))
- 调整后端部分方法名 save => add ([45bd3e1](https://github.com/continew-org/continew-admin/commit/45bd3e10b6ac6aecde41ff9484668e557a485b27))
- 优化系统日志详情 ([55effa3](https://github.com/continew-org/continew-admin/commit/55effa36580a57ddedb688e2ce30bec45c761224)) ([99997c1](https://github.com/continew-org/continew-admin/commit/99997c160eefc152a6f4e74bcd9c5ef6fc77a9c5))
- 移除部分方法中仅有单个非读操作的事务处理 ([b85d692](https://github.com/continew-org/continew-admin/commit/b85d69298de1a6c48d15300bb9ff1b3ea569fdbd))
- 优化编译配置 ([ed8bb57](https://github.com/continew-org/continew-admin/commit/ed8bb57fe24dfbe8f45b8f53370ebb79f1511268))
- 优化配置文件格式 ([3399bc8](https://github.com/continew-org/continew-admin/commit/3399bc8dde0c8c8ac6d3e583ffbe299f7e6dd80b))

### 🐛 问题修复

- 修复代码生成相关错误 ([3fdc50d](https://github.com/continew-org/continew-admin/commit/3fdc50d78ec50a878cec2b35c7d5028e741c42d7))
- 更新仪表盘帮助文档部分过期链接 ([ac42836](https://github.com/continew-org/continew-admin/commit/ac4283679a847ed372db28aae1ea05fd791651b8))

### 💥 破坏性变更

- 适配 ContiNew Starter QueryTypeEnum 命名变更 ([97c273f](https://github.com/continew-org/continew-admin/commit/97c273f99ecb038e041e3d39dbfacf326d49cc1b))
- 适配 ContiNew Starter Log HttpTracePro（日志模块） ([9bf0150](https://github.com/continew-org/continew-admin/commit/9bf015059b96f41c29f05ecbf7612d611b3a98c3))
- 适配 ContiNew Starter 全局异常处理器 ([4ed4ddd](https://github.com/continew-org/continew-admin/commit/4ed4ddd4f055cefe1f85482bd6b9ef760978691b))
- 适配 ContiNew Starter 数据权限解决方案（数据访问模块-MyBatis Plus） ([0849426](https://github.com/continew-org/continew-admin/commit/084942630ab0e1846c1836b8dc4bf5b2c9a5b16e))
- 调整 IBaseEnum 所属包 ([e6c6e1c](https://github.com/continew-org/continew-admin/commit/e6c6e1cb0e326c5f531ca5cb2e17a1e26efac7d9))
- 重构原有文件上传接口并优化配置文件配置格式 ([5e37025](https://github.com/continew-org/continew-admin/commit/5e370254dd00deaab62438c5feb4de14192ad7e6))

### 📦 依赖升级

- ContiNew Starter 1.0.0 => 1.1.0 ([fc80921](https://github.com/continew-org/continew-admin/commit/fc80921c047862b424ca625317f4657667bc2c6b)) (更多依赖升级详情，请查看 ContiNew Starter [更新日志](https://github.com/continew-org/continew-starter/blob/dev/CHANGELOG.md))
- Arco Design Vue 2.53.0 => 2.53.3 ([2720275](https://github.com/continew-org/continew-admin/commit/2720275b97334545dde71d548173bfcda7e660cb))
- Vite 4.5.0 => 4.5.1 ([2720275](https://github.com/continew-org/continew-admin/commit/2720275b97334545dde71d548173bfcda7e660cb))
- TypeScript 5.2.2 => 5.3.3 ([2720275](https://github.com/continew-org/continew-admin/commit/2720275b97334545dde71d548173bfcda7e660cb))
- unplugin-vue-components 0.25.2 => 0.26.0 ([2720275](https://github.com/continew-org/continew-admin/commit/2720275b97334545dde71d548173bfcda7e660cb))
- @kangc/v-md-editor 2.3.17 => 2.3.18 ([2720275](https://github.com/continew-org/continew-admin/commit/2720275b97334545dde71d548173bfcda7e660cb))
- eslint 8.53.0 => 8.56.0 ([2720275](https://github.com/continew-org/continew-admin/commit/2720275b97334545dde71d548173bfcda7e660cb))
- @vueuse/core 10.5.0 => 10.7.0 ([2720275](https://github.com/continew-org/continew-admin/commit/2720275b97334545dde71d548173bfcda7e660cb))
- vue-i18n 9.6.5 => 9.8.0 ([2720275](https://github.com/continew-org/continew-admin/commit/2720275b97334545dde71d548173bfcda7e660cb))
- vue-json-pretty 2.2.4 => 2.3.0 ([2720275](https://github.com/continew-org/continew-admin/commit/2720275b97334545dde71d548173bfcda7e660cb))
- 由于篇幅限制，仅列出部分前端依赖升级情况，更多请查看 [提交记录](https://github.com/continew-org/continew-admin/commit/2720275b97334545dde71d548173bfcda7e660cb)

## [v2.1.0](https://github.com/continew-org/continew-admin/compare/v2.0.0...v2.1.0) (2023-12-03)

### 💎 功能优化

- 优化数据权限注解 ([bb59a78](https://github.com/continew-org/continew-admin/commit/bb59a78573bec521e8852f1c88ce6078fb14b14e))
- 回退全局响应结果处理器 ([c7a4e32](https://github.com/continew-org/continew-admin/commit/c7a4e329945d8368a9b93a2488c059cf3333feba))
- 优化字典 Controller CRUD 注解使用 ([8c1c4b0](https://github.com/continew-org/continew-admin/commit/8c1c4b014463d073e848e2f2abc33e089efa2abb))
- 优化常量命名风格，XxxConsts => XxxConstants ([ec28705](https://github.com/continew-org/continew-admin/commit/ec28705b6ff6dd26ec3ef673fb3827259f1b9c41))
- 移除 XML 文件头部的协议信息 ([b476956](https://github.com/continew-org/continew-admin/commit/b47695603afb0c19679c4100c1e3c23bc8007238))
- 优化菜单标题校验 ([3dd81a1](https://github.com/continew-org/continew-admin/commit/3dd81a1192c4e340dad0b1bae5e29d1d7218fb25))

### 🐛 问题修复

- 修复 mock 被错误关闭的问题 ([a34070f](https://github.com/continew-org/continew-admin/commit/a34070ffed3044ad2bea604701b074665e7b4e42))
- 修复保存生成配置校验失效的问题，并优化部分提示效果 ([c34e934](https://github.com/continew-org/continew-admin/commit/c34e934bb553d7f814d0fb5aa87eac0f565289b4))

### 💥 破坏性变更

- 项目包结构 top.charles7c.cnadmin => top.charles7c.continew.admin ([b86fe32](https://github.com/continew-org/continew-admin/commit/b86fe329d07317fed6a7d0b7015856de4b9e75d1))
- 适配 ContiNew Starter 全局错误处理配置 ([b62095d](https://github.com/continew-org/continew-admin/commit/b62095d66e2318d35e4af07b128203b5a5e016f7))
- 适配 ContiNew Starter CRUD（扩展模块） ([ce5a2ec](https://github.com/continew-org/continew-admin/commit/ce5a2ec9319b86e69a6bda67a886e1c96079ffc2))
- 适配 ContiNew Starter Mail（消息模块） ([ce785dd](https://github.com/continew-org/continew-admin/commit/ce785ddce28733eeefbf970ed08b01e36e0abd4b))
- 适配 ContiNew Starter Excel（文件处理模块） ([1311ae3](https://github.com/continew-org/continew-admin/commit/1311ae3603a26dc44dfffc5be86ea1ab81ff7958))
- 适配 ContiNew Starter 认证模块-JustAuth ([7ad8d17](https://github.com/continew-org/continew-admin/commit/7ad8d1773a8e50e37a326b8f73f9ba38a3a7ff3a)) ([f28fbd1](https://github.com/continew-org/continew-admin/commit/f28fbd14fa83a82df49b07f16070e0ff3385b0ec))
- 适配 ContiNew Starter 认证模块-SaToken ([86ca8f0](https://github.com/continew-org/continew-admin/commit/86ca8f094ff6d1c00b52c1406985bc00105b297f))
- 适配 ContiNew Starter 图形验证码 ([8a11a02](https://github.com/continew-org/continew-admin/commit/8a11a020e04e271da7b700b5d73cf8475b50ee5c))
- 适配 ContiNew Starter MyBatis Plus 自动配置 ([7306cd9](https://github.com/continew-org/continew-admin/commit/7306cd9d2f9492aa39e11b8f0dc8c7c11b534a3f))
- 适配 ContiNew Starter Redisson 自动配置 ([a40e609](https://github.com/continew-org/continew-admin/commit/a40e609ea14acda840d2771f05ca9690d41236a1))
- 适配 ContiNew Starter Jackson、API 文档（Knife4j：Spring Doc）自动配置 ([a86f3a5](https://github.com/continew-org/continew-admin/commit/a86f3a5047eda2f67cc9ad7721006d2db1fd710f))
- 适配 ContiNew Starter 线程池自动配置 ([ec1daaf](https://github.com/continew-org/continew-admin/commit/ec1daaf0456296dbc3704ae700b9577001bdd5bb))
- 引入 ContiNew Starter，适配跨域自动配置 ([2c4f511](https://github.com/continew-org/continew-admin/commit/2c4f5116c999b9316ab0bee4fa661338fea63c11))
- 项目 group id top.charles7c => top.charles7c.continew ([3e23acb](https://github.com/continew-org/continew-admin/commit/3e23acb3e257d5813b858356aa926a96c906acf1))

## [v2.0.0](https://github.com/continew-org/continew-admin/compare/v1.3.1...v2.0.0) (2023-11-15)

### 💎 功能优化

- 优化部分代码格式 ([2f87310](https://github.com/continew-org/continew-admin/commit/2f87310bc886af604a2667285a973ec6ae983430))
- 优化 401 状态处理逻辑 ([c70e28a](https://github.com/continew-org/continew-admin/commit/c70e28a535c78214fe8d68a09824c786c457ef06))
- 优化超时登录处理逻辑 ([d5da184](https://github.com/continew-org/continew-admin/commit/d5da1847e33e6cd7a0e5c3434335044167c1241c))

### 🐛 问题修复

- sms4j 3.0.3 => 3.0.4 ([23558d4](https://github.com/continew-org/continew-admin/commit/23558d45620a48ed82b32a5bdd2f948a4a37263d))
- 发送消息增加事务处理 ([Gitee#7](https://gitee.com/continew/continew-admin/pulls/7)) ([1ca6f6c](https://github.com/continew-org/continew-admin/commit/1ca6f6c7e5f8a7c78f74df547f14517293241ac4))
- 修复前端控制台 eslint 告警 ([Gitee#6](https://gitee.com/continew/continew-admin/pulls/6)) ([f4523d2](https://github.com/continew-org/continew-admin/commit/f4523d24817b4fee5c015eaba6b98fe99f350bba)) ([2304f28](https://github.com/continew-org/continew-admin/commit/2304f28a942fa8ea3e6d36fbebbe9346b0d3b741))
- 修复仪表盘访问趋势区块 y 轴数值过大时无法展示的问题 ([fea6024](https://github.com/continew-org/continew-admin/commit/fea602439a3c9589bee078bfa9ff1e7efb378d71))
- 修复控制台报错 Please use theme before using plugins ([98fbe05](https://github.com/continew-org/continew-admin/commit/98fbe0506c1cbe2f3c16347d9610ebfa5688b506))
- 调整 Logback 配置，取消启动时打印 Logback 状态日志 ([1f7fef5](https://github.com/continew-org/continew-admin/commit/1f7fef5b31212e94652777be37bea4d4e02eb8c7))

### 💥 破坏性变更

- 优化部署相关脚本，mariadb => mysql ([5f4f0f1](https://github.com/continew-org/continew-admin/commit/5f4f0f1b21fe882dc51801d7c508c10b87d7af36))
- 适配 Java 16 新特性 ([cf30443](https://github.com/continew-org/continew-admin/commit/cf3044312c8631a8c2b306e466e3d4d663d8eb6d))
- 适配 Java 14 新特性 ([38f52aa](https://github.com/continew-org/continew-admin/commit/38f52aaafa22ebc958a22b7c38b084c655064fbc))
- 适配 Java 11 新特性 ([5a5bd16](https://github.com/continew-org/continew-admin/commit/5a5bd1681e076ac6814d552da5415a8f154b93af))
- 升级前端依赖 ([79fa2c8](https://github.com/continew-org/continew-admin/commit/79fa2c8abcf5f70f96ae7c6de35c47dbae76ee2d)) ([c44162d](https://github.com/continew-org/continew-admin/commit/c44162d431cb87cae251067fff9a5ae707aed9b3))
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
- 升级后端依赖  ([dea160a](https://github.com/continew-org/continew-admin/commit/dea160a7b2d69e1b46edc936c9a697048bbb507a)) ([95c27ea](https://github.com/continew-org/continew-admin/commit/95c27ea323e015c915d352618158df830b4d1c05)) ([fa23287](https://github.com/continew-org/continew-admin/commit/fa232874aa88ab14fdc669e54a907e5ef05d2a7e)) ([8dbec9d](https://github.com/continew-org/continew-admin/commit/8dbec9d1a3bcb0f6d7ef4bbfb9715effd61b2025)) ([3bd56d8](https://github.com/continew-org/continew-admin/commit/3bd56d8a1ee274aac6d4ea57d61f6d470de0dc9c)) ([7b741d5](https://github.com/continew-org/continew-admin/commit/7b741d5f8c42d154c5b325326d0cc954fb566502))
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

## [v1.3.1](https://github.com/continew-org/continew-admin/compare/v1.3.0...v1.3.1) (2023-11-15)

### 💎 功能优化

- 完善 Redis 部署配置 ([39969eb](https://github.com/continew-org/continew-admin/commit/39969ebf6173fc379dc3501e9204a344d1cf62cf))
- 优化 401 状态处理逻辑 ([8820c1d](https://github.com/continew-org/continew-admin/commit/8820c1dfc858b9ef9df470e90dfe9ba4b1166e29))
- 优化超时登录处理逻辑 ([712eedb](https://github.com/continew-org/continew-admin/commit/712eedba1be0ec371119745d4596cd35c2ce25d6))
- 优化部分变量命名 ([f15494d](https://github.com/continew-org/continew-admin/commit/f15494d34823ded87efc396d98e2eb0108f74a3d))

### 🐛 问题修复

- sms4j 3.0.3 => 3.0.4 ([3fcdb54](https://github.com/continew-org/continew-admin/commit/3fcdb54442b380e76838478fa46e8dfb70a2759b))
- 发送消息增加事务处理 ([5d159c6](https://github.com/continew-org/continew-admin/commit/5d159c6ab337a9432419d84cf246cff506500567))
- 修复仪表盘访问趋势区块 y 轴数值过大时无法展示的问题 ([47a5746](https://github.com/continew-org/continew-admin/commit/47a5746794e552faf9c41fbcc21af091a878eb95))
- 修复控制台报错 Please use theme before using plugins ([47a8160](https://github.com/continew-org/continew-admin/commit/47a8160d70862a5ee7284c165004cece2714a10f))
- 修复 Swagger 分组接口缺失 ([b63d7d7](https://github.com/continew-org/continew-admin/commit/b63d7d725da5e9e9b2db9fd59bd140d64b50040c))

## [v1.3.0](https://github.com/continew-org/continew-admin/compare/v1.2.0...v1.3.0) (2023-11-04)

### ✨ 新特性

* 消息管理：提供消息查看、标记已读、全部已读、删除等功能（适配对接导航栏站内信功能）
* 新增头像上传前裁剪功能 ([Gitee#5](https://gitee.com/continew/continew-admin/pulls/5)) ([cbc652d](https://gitee.com/continew/continew-admin/commit/cbc652de77200d29bcd42bb399c86c2e7df29c4d)) ([28f4791](https://gitee.com/continew/continew-admin/commit/28f4791833060469d132c4383665e81458f9c852))
* 支持手机号登录（演示环境不开放） ([4d70bc8](https://github.com/continew-org/continew-admin/commit/4d70bc84db47c36c13d8e41e3a33e5a589483de8))
* 支持邮箱登录 ([17b169e](https://github.com/continew-org/continew-admin/commit/17b169eb0ea2ded759b6bccb213c78bfb3425941))
* 个人中心-安全设置，支持绑定、解绑三方账号 ([efe4557](https://github.com/continew-org/continew-admin/commit/efe455736c158e73bf0c6514c31bec5d83fe843b))
* 支持第三方账号登录 ([05cb609](https://github.com/continew-org/continew-admin/commit/05cb60978017edbd14f1c7af83053f8a91800b5c))

### 💎 功能优化

- 新增接口文档菜单，演示环境开放接口文档 ([4a42336](https://github.com/continew-org/continew-admin/commit/4a4233647f2ea212b007f591aafc50380b15c099))
- 项目配置增加是否为生产环境配置项 ([38deb95](https://github.com/continew-org/continew-admin/commit/38deb950ac7b2ed81f0e10816e943156aa076795))
- 优化校验相关方法命名 ([f25de2d](https://github.com/continew-org/continew-admin/commit/f25de2d7f835a3fa75d59d3de0a014c37b3b32e1))
- 新增全局响应结果处理器 ([Gitee#3](https://gitee.com/continew/continew-admin/pulls/3)) ([992a8fc](https://gitee.com/continew/continew-admin/commit/992a8fca173ea76722b388aca462cff8a1128803)) ([Gitee#4](https://gitee.com/continew/continew-admin/pulls/4)) ([a0b1afc](https://gitee.com/continew/continew-admin/commit/a0b1afc546657766cb6031794b98ccc2b6e4cb2d))
- 优化部分代码格式及注释 ([3a176ac](https://github.com/continew-org/continew-admin/commit/3a176ac5efbda4aea1e883b29e68861bd352d642))
- 重构登录页面 UI 以适配多维度认证、第三方登录等场景 ([d40d5b4](https://github.com/continew-org/continew-admin/commit/d40d5b4ae61d858fbee3ffa0606ebebb4282d9a2)) ([a5a4cd4](https://github.com/continew-org/continew-admin/commit/a5a4cd49646db3fa1108a8b917ef70c7757e81ad))
- 升级前端依赖 ([698a725](https://github.com/continew-org/continew-admin/commit/698a7251b742e6b679694f21bfc174904dca8990))
  - Arco Design Vue 2.51.0 => 2.52.0
  - vue-i18n 9.2.2 => 9.5.0
  - dayjs 1.11.9 => 1.11.10

- 升级后端依赖 ([698a725](https://github.com/continew-org/continew-admin/commit/698a7251b742e6b679694f21bfc174904dca8990))
  - Spring Boot 2.7.15 => 2.7.16
  - Sa-Token 1.35.0.RC => 1.36.0
  - Hutool 5.8.20 => 5.8.22


### 🐛 问题修复

- 开放前端项目IP访问 ([22a291d](https://github.com/continew-org/continew-admin/commit/22a291d4cf48e33dc2415e44b5d991b46451e7eb))
- 修复获取验证码倒计时显示 ([2f2905e](https://github.com/continew-org/continew-admin/commit/2f2905efdc0baec2f2c38f686f72306394801ebf))
- 用户邮箱信息增加脱敏处理 ([5bb35a1](https://github.com/continew-org/continew-admin/commit/5bb35a13d6b5801317a295eacc67d88b2c3e1682))
- 修复重载校验方法定义及使用错误 ([a1ccc42](https://github.com/continew-org/continew-admin/commit/a1ccc421c440e5fef54e5d22b9bed26d2b16dda5))
- 修复个人中心密码设置状态显示错误的问题 ([b04a228](https://github.com/continew-org/continew-admin/commit/b04a228a1a5bc0a575dd9e29e515285708b8ca85))
- 修复登录后访问首页却跳转到登录页面的问题 ([Fixes #23](https://github.com/continew-org/continew-admin/issues/23)) ([7cf5e00](https://github.com/continew-org/continew-admin/commit/7cf5e0018c87720303f731317b5eb3cb7d127327))
- 修复字典名称表单校验 ([#22](https://github.com/continew-org/continew-admin/pull/22)) ([c0ee2ea](https://github.com/continew-org/continew-admin/commit/c0ee2eac026d2d5a950a41b6f0a475b95b71d47a))

### 💥 破坏性变更

- 调整后端请求、响应参数模型命名风格 ([87f9056](https://github.com/continew-org/continew-admin/commit/87f90567dbd99f873aea1b85510c7b9939a2abb8))
- 枚举接口 BaseEnum => IBaseEnum ([f5e8b09](https://github.com/continew-org/continew-admin/commit/f5e8b0943c6076c476b7d78bb623707740fb452f))
- 优化前端登录模块 API 路径 ([43590bf](https://github.com/continew-org/continew-admin/commit/43590bf66e7e4873a85bdd416bd38b269f3af80e))
- 优化后端部分参数模型命名 ([51f5528](https://github.com/continew-org/continew-admin/commit/51f552892ccb11ed594bf908069a1fd426324b69))
- 优化个人中心路由地址 ([36d52d3](https://github.com/continew-org/continew-admin/commit/36d52d3e1522cd221cf3f03d76efd3e0eaf1b18f))
- 还原前端 loginStore 命名，重命名为 userStore ([8d39493](https://github.com/continew-org/continew-admin/commit/8d394937cfc8418799215bd3659d26bed1f834c5))

## [v1.2.0](https://github.com/continew-org/continew-admin/compare/v1.1.2...v1.2.0) (2023-09-24)

### ✨ 新特性

* 字典管理：提供对系统公用数据字典的维护，例如：公告类型，支持字典标签背景色和排序等配置
* 系统配置：提供修改系统标题、Logo、favicon 等基础配置功能，以方便用户系统与其自身品牌形象保持一致
* 完善仪表盘最近访问区块内容 ([36fda57](https://github.com/continew-org/continew-admin/commit/36fda57d499b0c3fb092a13f269bc9ffb7a26a9e))
* 完善仪表盘访问趋势区块内容 ([a1c20af](https://github.com/continew-org/continew-admin/commit/a1c20afb1b9eb447f62bfd2e4f2996dfdf37c8ca)) ([1722133](https://github.com/continew-org/continew-admin/commit/1722133ac4872b40d6d47f65f359dea8a354b91a))
* 完善仪表盘访客地域分布区块内容 ([dc1691f](https://github.com/continew-org/continew-admin/commit/dc1691f0195ef6c96aee36f50fc7e86cfcf651b9))
* 完善仪表盘热门模块区块内容 ([83b2e2a](https://github.com/continew-org/continew-admin/commit/83b2e2a7c02d38c7041497e0ac5b3b0e78abac29))
* 完善仪表盘总计区块内容 ([3440aa4](https://github.com/continew-org/continew-admin/commit/3440aa4faa23e267735f564476d8bccaf8c0208f))
* 完善仪表盘快捷操作区块内容 ([0178fbb](https://github.com/continew-org/continew-admin/commit/0178fbb89a0e75729aa60443a812496bd5b19cb8))

### 💎 功能优化

- 前端表单重置优化 ([e947312](https://github.com/continew-org/continew-admin/commit/e947312f244d6af01f18b542ff7395440c68b089))
- 优化登录和菜单加载相关提示 ([d080120](https://github.com/continew-org/continew-admin/commit/d080120d4228e77200d8f152397b0ebee413b089))
- 完善前后端校验 ([90d825a](https://github.com/continew-org/continew-admin/commit/90d825a02fdc54e8685508a6fe4fb2d5f20e77f4)) ([8e506dc](https://github.com/continew-org/continew-admin/commit/8e506dc6e69529627a0aace6118f7310cc2f030a))
- 优化枚举字典处理，增加颜色类型 ([1f73aa7](https://github.com/continew-org/continew-admin/commit/1f73aa732d101c7f7a58bc678e85d597d54d9770))
- 公告类型适配字典数据 ([3a3a5d6](https://github.com/continew-org/continew-admin/commit/3a3a5d6b712f435d77ea04301afa0bdd8703567f))
- 优化通用查询注解多字段模糊查询 ([3758107](https://github.com/continew-org/continew-admin/commit/375810772aa8cb928fb1f6820e781cb43f869e03))
- 合并菜单管理图标和标题列 ([36d38ae](https://github.com/continew-org/continew-admin/commit/36d38aec1602f5ac6d2afbb5c5adf4d6e455ab97))
- 封装 Spring Boot 默认错误处理 ([b874ca0](https://github.com/continew-org/continew-admin/commit/b874ca0782eb116bdedfc08023959a977f170a94))
- 优化分页查询登录日志列表接口实现 ([566c9a1](https://github.com/continew-org/continew-admin/commit/566c9a122453980b585bd68442bb545073504a3d))
- 更换登录页面 banner ([6f19660](https://github.com/continew-org/continew-admin/commit/6f19660cfbc3be6e0d702e3f488e266c50622f0a))
- 优化登录用户信息角色相关信息命名 ([be394f3](https://github.com/continew-org/continew-admin/commit/be394f3de4ea7ea692042db3556f706a3d141b51)) ([31f0abb](https://github.com/continew-org/continew-admin/commit/31f0abbae2e38d1cfa3f6221c9be0b54cf5337ad))
- 升级前端依赖 ([c665902](https://github.com/continew-org/continew-admin/commit/c6659020f8bac7319c5c407389cd745527a8cd97))
- 升级后端依赖 ([5049e1e](https://github.com/continew-org/continew-admin/commit/5049e1e312ab500e284abccbbee4186db2710d01)) ([d20aadf](https://github.com/continew-org/continew-admin/commit/d20aadfc93b54339d19d173fce364310e90b016d)) ([32904b5](https://github.com/continew-org/continew-admin/commit/32904b54ef63536ef5c5106adc00a7376b907632))

### 🐛 问题修复

- 修复删除列表数据后 Select 选择框重置问题 ([#21](https://github.com/continew-org/continew-admin/pull/21)) ([3288f2d](https://github.com/continew-org/continew-admin/commit/3288f2d38dfebc1381842d67cdfb17675c786859))
- 修复前端部分拼写错误 ([62021f8](https://github.com/continew-org/continew-admin/commit/62021f8fdc171ad04d07c25c5a9357a64cc4a087))

### 💥 破坏性变更

- 优化系统内置类型数据标识 ([8a02401](https://github.com/continew-org/continew-admin/commit/8a02401a24b546f2a6aab04cf05371ecb4236ca0))
- 分离 HTTP 状态码和业务状态码 ([b3b6446](https://github.com/continew-org/continew-admin/commit/b3b6446433972422cf62dfc47c031134b91cd7ec))
- 调整生产环境本地存储、日志位置 ([2254e55](https://github.com/continew-org/continew-admin/commit/2254e555af9cade4897d5335b252a0312d6805eb))
- 调整项目打包结构，分离依赖、配置文件 ([e679abf](https://github.com/continew-org/continew-admin/commit/e679abfccc6c80198512958b6d07b363074d9d76))

## [v1.1.2](https://github.com/continew-org/continew-admin/compare/v1.1.1...v1.1.2) (2023-09-24)

### 💎 功能优化

- 优化后端程序启动成功输出内容 ([6322859](https://github.com/continew-org/continew-admin/commit/63228598d9fcd6e5d00172c12418a371d4c96766))
- 配置子级菜单图标 ([5544836](https://github.com/continew-org/continew-admin/commit/55448364a39085debb776463f5e95a15b186c447))

### 🐛 问题修复

- 修复生产环境和开发环境样式不一致的问题 ([be8732d](https://github.com/continew-org/continew-admin/commit/be8732d812e021631864b0ff6225b4da24cafcee))
- 排除路径配置放开 /error ([0428fe7](https://github.com/continew-org/continew-admin/commit/0428fe776224afb64601901cef4d3100e5d30bd6))
- 修复初始数据缺失字段列表的问题 ([d5138e1](https://github.com/continew-org/continew-admin/commit/d5138e1e43bdc8b347e061890131ac2646b2dd3c))
- 修复系统日志表索引缺失导致查询耗时较长的问题 ([ac43833](https://github.com/continew-org/continew-admin/commit/ac438337219f5a160d49b255805774da36ab865c))
- 修复部分菜单数据 component 信息配置错误 ([11ea072](https://github.com/continew-org/continew-admin/commit/11ea072d600f24fe97fe8145208e821712b84839))
- 修复图标 SVG 内容格式错误 ([20f1e8a](https://github.com/continew-org/continew-admin/commit/20f1e8aecc737b28ab869d363957513d868b4ab7))

## [v1.1.1](https://github.com/continew-org/continew-admin/compare/v1.1.0...v1.1.1) (2023-09-06)

### 💎 功能优化

- 调整 Mock 响应时长，以解决前端偶发需重复登录问题 ([df19c5d](https://github.com/continew-org/continew-admin/commit/df19c5d2197fabb61cbdd4dccf1c427fb23d77d4))

### 🐛 问题修复

- 还原登录 Helper 优化（导致重大登录问题及查询在线用户错误） ([#15](https://github.com/continew-org/continew-admin/pull/15)) ([7a6db2d](https://github.com/continew-org/continew-admin/commit/7a6db2d14e60a5fcc1a2786e6eaa3d46a0714e6c)) ([#9](https://github.com/continew-org/continew-admin/pull/9)) ([9e2a5ef](https://github.com/continew-org/continew-admin/commit/9e2a5ef1249fd93dd10f2c255bf77c3eaa64a241))
- 修复刷新页面后，选中菜单无法保持展开状态的问题 ([3fc7adb](https://github.com/continew-org/continew-admin/commit/3fc7adb1e2bd4b648753bd2999df725417e01680))
- 修复侧边栏菜单无法显示自定义图标的问题 ([10ca5d8](https://github.com/continew-org/continew-admin/commit/10ca5d8c76aa39a207ea7db4442bf63ff4578273))
- 更正 README 文档项目结构部分内容 ([486da2f](https://github.com/continew-org/continew-admin/commit/486da2f79bfc5379213bf666b8f325fb8096ebc6))
- 修复公告缺失待发布状态的问题 ([#14](https://github.com/continew-org/continew-admin/pull/14)) ([46cc4c9](https://github.com/continew-org/continew-admin/commit/46cc4c9307e3cc7060ae436f59f007831104884a))

## [v1.1.0](https://github.com/continew-org/continew-admin/compare/v1.0.1...v1.1.0) (2023-09-01)

### ✨ 新特性

* 公告管理：提供公告的发布、查看和删除等功能。管理员可以在后台发布公告，并可以设置公告的生效时间、终止时间，以 markdown-it 为内核渲染 Markdown 格式内容显示
* 代码生成：提供根据数据库表自动生成相应的前后端 CRUD 代码的功能
* 允许表格调整列宽，不允许新增/修改类表单对话框按 Esc 关闭 ([1b06a96](https://github.com/continew-org/continew-admin/commit/1b06a96cfbe5774931d8c4c0d7827703caa096df))

### 💎 功能优化

- 最终适配及启用 Arco Design Pro Vue 动态路由 ([9baf341](https://github.com/continew-org/continew-admin/commit/9baf3410138cb8a152ec51f70340d500fa009510))
- 优化分页总记录数数据类型 ([bfea689](https://github.com/continew-org/continew-admin/commit/bfea689b0eaf44c8d54b4fd59c042d72ac71e395))
- 修复在线用户列表等自定义分页查询 NPE 的问题 ([015ff55](https://github.com/continew-org/continew-admin/commit/015ff5512b3662efce88d02ab1dda6d55501a501))
- 对获取路由信息接口增加缓存处理 ([4639d13](https://github.com/continew-org/continew-admin/commit/4639d13ba61abfaed3c9d3da0e057892577b5c40))⚡
- 完善前端 axios 请求响应拦截器 ([bb398d8](https://github.com/continew-org/continew-admin/commit/bb398d8101e3780f450c6508852fc727fb936cee)) ([e18692f](https://github.com/continew-org/continew-admin/commit/e18692fa74e0a0d9558db6643b945c6c6a00db36))
- 优化仪表盘公告区块、帮助文档区块内容 ([b59a819](https://github.com/continew-org/continew-admin/commit/b59a819ad5f2bdbd357951f070d155e91f2d7903)) ([315c059](https://github.com/continew-org/continew-admin/commit/315c059713833be10b0cf05d302259a3146f3707)) ([6d024a9](https://github.com/continew-org/continew-admin/commit/6d024a90d7a231439c8e260b9bd625e8b5027515))
- 将 Swagger 文档中的额外请求参数隐藏 ([#11](https://github.com/continew-org/continew-admin/pull/11)) ([a9ed02b](https://github.com/continew-org/continew-admin/commit/a9ed02bf4ff6a8a4d9f68db2d62d29000c543943))
- 优化前端 CRUD 相关命名 ([6d81928](https://github.com/continew-org/continew-admin/commit/6d81928541f4da568e9c7138f91d4dc1c5c6dd4e))
- 优化部分超链接标签属性 ([46a75d0](https://github.com/continew-org/continew-admin/commit/46a75d029798e8d5a162b53b8a61c8e3c3f4dd9e))
- 使用属性变量消除配置文件中分散的 ContiNew Admin 品牌元素 ([54ea410](https://github.com/continew-org/continew-admin/commit/54ea41048abd096cf1e2c32ee871c1eb85d4ece1))
- 拆分 Swagger 接口文档分组 ([#10](https://github.com/continew-org/continew-admin/pull/10)) ([72df45e](https://github.com/continew-org/continew-admin/commit/72df45e9b3373d28f1845af16a81cb8bd8408647))
- 优化登录 Helper ([#9](https://github.com/continew-org/continew-admin/pull/9)) ([9e2a5ef](https://github.com/continew-org/continew-admin/commit/9e2a5ef1249fd93dd10f2c255bf77c3eaa64a241))
- 将全局异常处理器未知异常的异常类型从 Exception 调整为 Throwable ([90e1c64](https://github.com/continew-org/continew-admin/commit/90e1c64db684df97454e4753932b7f4017d8e23d))
- 优化 == 及 != 表达式格式 ([487fa82](https://github.com/continew-org/continew-admin/commit/487fa82306fbd84033f6c39ad20b72755b03e875))
- 集成 Spring Cache，优化查询用户昵称性能 ([b23b00d](https://github.com/continew-org/continew-admin/commit/b23b00d02a4738a61b4a13676fab6d2c9ec927de)) ([76622c2](https://github.com/continew-org/continew-admin/commit/76622c238f1d6028826407490e50a14bdba25ade))⚡
- 将验证码唯一标识格式从无符号 UUID 调整为带符号 UUID ([a61196c](https://github.com/continew-org/continew-admin/commit/a61196cd62cea4f684154bb42a949656650f626b))
- 完善接口文档示例信息 ([#7](https://github.com/continew-org/continew-admin/pull/7)) ([ad7d699](https://github.com/continew-org/continew-admin/commit/ad7d6995ba40a0cb70a194693fa450bdbb3cc7a0)) ([#8](https://github.com/continew-org/continew-admin/pull/8)) ([0ac0213](https://github.com/continew-org/continew-admin/commit/0ac0213628023c04b5be531522d76f09712f7317)) ([190385e](https://github.com/continew-org/continew-admin/commit/190385ed3636206224bc90780fcede2e49f9c118)) ([332bd6c](https://github.com/continew-org/continew-admin/commit/332bd6cd2a9b4e25678a3eec565965c5b2702aa2))
- 使用 DatePattern 中的日期格式常量替代字符串常量中的日期格式 ([241a9cf](https://github.com/continew-org/continew-admin/commit/241a9cf85b3c19eb093d4d661c35d71c490adf1f))
- 优化分组校验 ([78a5d5e](https://github.com/continew-org/continew-admin/commit/78a5d5ec7a14ee37d92a9520211adca23f12b287))
- 优化 springdoc-openapi 对象型参数处理 ([ae8d294](https://github.com/continew-org/continew-admin/commit/ae8d294705536e99d6c30a9ff5257fdb3ee5b35f))
- 升级前端依赖，并更换包管理器 yarn => pnpm ([6164110](https://github.com/continew-org/continew-admin/commit/6164110462cc3aff66d79539f54e84d47c6d5894))
- 升级后端依赖 ([51a82d8](https://github.com/continew-org/continew-admin/commit/51a82d8f4eabd6aa27e1a991f05f516171b6ae03))

### 🐛 问题修复

- 完善部分数据库表的唯一索引 ([88d6118](https://github.com/continew-org/continew-admin/commit/88d6118693586fbd8da573df3b2f942d049e4b3c))
- 修复访问 doc.html 接口文档，控制台报 No mapping for GET /favicon.ico 警告的问题 ([94f88ba](https://github.com/continew-org/continew-admin/commit/94f88bad2278d64a4b8a3bc930a9f754fb00cba6))
- 登录页面输入错误时，自动清空验证码输入框 ([a76f47f](https://github.com/continew-org/continew-admin/commit/a76f47fbd86bfa7fbf85440c653ae6259fce7969))

### 💥 破坏性变更

- 更新信息调整为仅在更新数据时自动填充 ([df77e57](https://github.com/continew-org/continew-admin/commit/df77e574cca605afd89f1b3781f1cde699bcb7e6))
- 将时间戳单位从毫秒调整为秒 ([fa916b9](https://github.com/continew-org/continew-admin/commit/fa916b93247e10462eb44185ad45cdca4dedda7d))
- 移除所有的 @Accessors(chain = true)，并全局配置禁止使用 ([76c6546](https://github.com/continew-org/continew-admin/commit/76c65463c2e5ddf0c90fa1622fd86706a4373c80))

## [v1.0.1](https://github.com/continew-org/continew-admin/compare/v1.0.0...v1.0.1) (2023-08-17)

### 💎 功能优化

- 优化根据 ID 查询用户昵称方法 ([4a8af1f](https://github.com/continew-org/continew-admin/commit/4a8af1f72d9249afa1c013e08674f492f453b020))
- 优化 BaseController 中部分权限码的使用 ([b0b1127](https://github.com/continew-org/continew-admin/commit/b0b1127b5bd39e9bc431e9fa9c86201bbc18e891))
- 优化分页总记录数数据类型 ([76f04dd](https://github.com/continew-org/continew-admin/commit/76f04dd38f90aad6abf82d2dccba031d4d9108cf))
- 优化通用查询注解解析器 ([a623acd](https://github.com/continew-org/continew-admin/commit/a623acd4a5529ae42898ec359f595716acc5bab8)) ([b632c18](https://github.com/continew-org/continew-admin/commit/b632c183994ac71382180a38bf7bdb7a6315c1e6))
- 优化数据库表结构中部分类型长度 ([f3fabea](https://github.com/continew-org/continew-admin/commit/f3fabea7dd736d94badecbc08091eec6274f5fb7))
- 使用常量优化部分魔法值 ([e6f7429](https://github.com/continew-org/continew-admin/commit/e6f7429fa30cbc87c03a073a53b6f7df24d33d8d))
- 优化部分 Properties 用法 ([48de2e8](https://github.com/continew-org/continew-admin/commit/48de2e85e0fbf60f10769cd3529f79ac3c531e92))

### 🐛 问题修复

- 修复获取字典参数为空时的判断条件 ([#6](https://github.com/continew-org/continew-admin/pull/6)) ([104f69e](https://github.com/continew-org/continew-admin/commit/104f69e8a09ce36163f6f9680b2d8d61bb45f11a))
- 完善查询用户数据权限 ([026247f](https://github.com/continew-org/continew-admin/commit/026247f677110ae199124a67c68503729cbaec92))
- 解决 IDE 报 Delete ␍ eslint(prettier/prettier) 警告的问题 ([8743ed1](https://github.com/continew-org/continew-admin/commit/8743ed14d927ab52814ed5f5f166afaa7a6b78b2))
- 修复分页查询条件默认值未生效的问题 ([2d2a7e7](https://github.com/continew-org/continew-admin/commit/2d2a7e7c8e31763ac3ea514d8a92c3938376dd3a))
- 完善各模块事务注解 ([18c54a7](https://github.com/continew-org/continew-admin/commit/18c54a74fc6ff0650ff53eeadc094d7e1df0b0a5))
- 修复邮箱健康检查报错问题并优化部分配置写法 ([5968f40](https://github.com/continew-org/continew-admin/commit/5968f402ed478244d36f5825373190ed00d8c1f1))
- 完善各模块参数校验 ([8b955a0](https://github.com/continew-org/continew-admin/commit/8b955a0b1bde4e8959fc0dfbc11a326d9eec0b45))

## v1.0.0 (2023-03-26)

### ✨ 新特性

* 用户管理：提供用户的相关配置，新增用户后，默认密码为 123456
* 角色管理：对权限与菜单进行分配，可根据部门设置角色的数据权限
* 部门管理：可配置系统组织架构，树形表格展示
* 菜单管理：已实现菜单动态路由，后端可配置化，支持多级菜单
* 在线用户：管理当前登录用户，可一键踢下线
* 日志管理：提供在线用户监控、登录日志监控、操作日志监控和系统日志监控等监控功能
