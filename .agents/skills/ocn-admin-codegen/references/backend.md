# P2 后端产出（9 个 Java 文件）

## 放置模块（第一轮问答的「放置位置」落地）

- **默认**：`continew-system` 的对应业务子包，路径
  `continew-system/src/main/java/top/continew/admin/system/<domain>/`（domain 如 `goods`、`demo`）
- **新建插件模块**（continew-plugin 下）：按 AGENTS.md 要求——根 POM `<modules>` 与根 POM
  `dependencyManagement` 两处注册（新模块坐标用 `${revision}`）；模块 POM 引公共模块时 groupId 写
  `${project.groupId}`；`continew-server/pom.xml` 加依赖；模块如有独立配置文件，
  记得在 `application.yml` 的 `profiles.include` 装载。模块命名 `continew-plugin-<name>`
- **跨模块暴露公共业务**：接口放 `continew-common` 的 `top.continew.admin.common.api` 包，
  由消费方模块 `api/` 目录提供实现（见 AGENTS.md「内部 API 解耦」），不要模块间直接互依赖实现类

## 逐文件清单（放 `<module>/src/main/java/top/continew/admin/<domain>/`）

1. `model/entity/<X>DO.java` —— extends `BaseDO`，`@TableName("<table>")`，`@Data`；
   **JSON 列字段必须同时加 `@TableField(typeHandler = JacksonTypeHandler.class)` 与
   `@TableName(..., autoResultMap = true)`**（漏后者会导致读出来恒为 null，消息删除接口踩过这个坑）
2. `model/query/<X>Query.java` —— 只放可筛选字段（字段映射轮用户选定的列），implements `Serializable`；
   分页不放进 Query：PAGE 接口的分页参数 `PageQuery` 由 CRUD 套件作为独立方法参数注入
   （参照 NoticeQuery 与 OnlineUserController.page 的真实签名，不要让 Query 继承 PageQuery）
3. `model/req/<X>Req.java` —— 校验注解齐全（@NotBlank/@NotNull/@Size），与 DDL 长度一致；
   复杂场景可加 SpEL Validator（`@SpelValidate`，照仓库既有用法）
4. `model/resp/<X>Resp.java` 与 `<X>DetailResp.java` —— Resp 放列表字段，DetailResp 继承
   `BaseDetailResp` 并加详情字段（形态照 UserDetailResp/NoticeDetailResp）
5. `mapper/<X>Mapper.java` —— extends `BaseMapper<<X>DO>`；有联表/自定义查询时再加
   `continew-system/src/main/resources/mapper/<X>Mapper.xml`（接口方法与 XML 语句成对出现）
6. `service/<X>Service.java` + `service/impl/<X>ServiceImpl.java` —— extends BaseService 体系：
   接口 extends `BaseService<<X>Resp, <X>DetailResp, <X>Query, <X>Req>`（必要时加 `IService<<X>DO>`），
   实现类 extends `BaseServiceImpl<<X>Mapper, <X>DO, <X>Resp, <X>DetailResp, <X>Query, <X>Req>>`
   并 implements 接口；类上加 `@Service`、`@RequiredArgsConstructor`，日志用 `@Slf4j`
7. `controller/<X>Controller.java` ——
   `@CrudRequestMapping(value = "/system/goods", api = {...用户选定的 Api 集...})`
   extends `BaseController<<X>Service, <X>Resp, <X>DetailResp, <X>Query, <X>Req>`
   **只写空类体**：CRUD 接口由基类生成，不要在子类里重复声明

## 硬规则（最常被违反的三条）

- 类级必须有 `@author`（署使用者本人）与 `@since`（5.0.0）；方法级不写 `@author`
- 注释只写当前代码的非显然约束，不写「本方法用于…」这类复述
- 新增 Java 文件必须带 Apache-2.0 License Header，否则 Spotless 会拦

## 交付前自查

- Api 集与第一轮问答一致；Api 里没有 export 就不造导出相关产物
- Query 字段与字段映射轮的「筛选字段」多选结果逐字一致
- JSON 列的 typeHandler 与 autoResultMap 成对；树形列表需 `Api.TREE` 时确认表有 `parent_id`
  且 Service 走树形组装（参照既有树形模块）
