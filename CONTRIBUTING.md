# 贡献指南

> 本指南适用于 OpenContiNew 社区旗下的 ContiNew 系列项目（ContiNew Admin / ContiNew Starter / ContiNew Admin UI 等），社区各项目通用。

感谢您对 ContiNew 开源项目的关注！无论是修复一个错别字、报告一个 Bug，还是实现一个新功能，每一份贡献都很有价值。

ContiNew（Continue New）系列项目致力于通过持续迭代，为开发者提供舒适的开发体验。我们的初衷是希望通过开源协作模式，提升技术透明度、放大集体智慧、共创优秀实践，源源不断地为企业级项目开发提供助力。除代码之外，我们同样重视文档以及与其它开源项目的整合，欢迎在这些方面做出贡献。

## 行为准则

参与本项目即表示您同意遵守我们的[行为准则](CODE_OF_CONDUCT.md)。请在所有交流中保持友善和建设性。

## 贡献方式

贡献并不仅限于写代码，以下方式都非常欢迎：

| 方式 | 说明 |
|:-----|:-----|
| 报告 Bug | 通过 [Issue 表单](https://github.com/continew-org/continew-admin/issues/new/choose) 提交，并附上版本号、复现步骤与错误日志 |
| 建议功能 | 通过 [Feature 表单](https://github.com/continew-org/continew-admin/issues/new/choose) 描述使用场景与期望效果 |
| 改进文档 | 修复错别字、完善表述、补充使用示例（包括 [官方文档](https://continew.top)） |
| 审查 PR | 帮助我们审查其他贡献者的 [Pull Request](https://github.com/continew-org/continew-admin/pulls) |
| 编写代码 | 修复 Bug、开发新功能、提升性能 |

如果是比较复杂的修改（如新功能、重构），建议先提交 Issue 讨论方案，达成基本共识后再动手，避免重复劳动。

## 分支说明

ContiNew 系列项目采用清晰的分支策略，确保开发与维护有序进行。提交 PR 前，请确认目标分支是否处于活跃维护状态。

| 分支  | 说明                                                         |
| ----- | ------------------------------------------------------------ |
| dev   | 开发分支，用于下个大版本的 SNAPSHOT 开发，接受新功能或功能优化 PR |
| x.x.x | 维护分支，用于特定版本（如 vx.x.x）的 bug 修复，仅接受已有功能的修复 PR，不接受新功能 |

## 环境准备

| 要求 | 说明 |
|:-----|:-----|
| JDK 17+ | 项目基于 Java 17 |
| Maven 3.9+ | 推荐直接使用仓库内置的 `./mvnw`（Windows 为 `mvnw.cmd`），无需单独安装 |
| MySQL / PostgreSQL | 按需准备数据库，Liquibase 会在首次启动时自动初始化 |
| Redis | 缓存、认证令牌等依赖 Redis |
| IDE（可选） | IntelliJ IDEA，可导入项目提供的代码风格配置（见下） |

### 代码规范配置

ContiNew Admin 遵循阿里巴巴《Java 开发手册(黄山版)》，代码格式以 Spotless 为唯一事实源。`style/` 目录下提供了全套配置文件，建议在 IDE 中导入以辅助开发：

| 文件 | 用途 |
|:-----|:-----|
| [`style/ocn-eclipse-formatter.xml`](style/ocn-eclipse-formatter.xml) | Eclipse Formatter 配置（Spotless 使用，代码格式的唯一事实源） |
| [`style/ocn-idea-code-style.xml`](style/ocn-idea-code-style.xml) | IntelliJ IDEA 代码风格配置（近似映射，辅助阅读） |
| [`style/ocn-checkstyle.xml`](style/ocn-checkstyle.xml) | Checkstyle 配置（可配合 Checkstyle-IDEA 插件实时检查） |
| [`style/STYLE.md`](style/STYLE.md) | 代码风格说明（各配置文件的导入方式与规则摘要） |

## 报告 Issue

在提交 Issue 之前，请先：

1. 确认使用的是[最新版本](https://github.com/continew-org/continew-admin/releases)，项目由维护者利用业余时间维护，没有额外精力回溯修复历史版本的问题；
2. 搜索 [Issue 广场](https://continew.top/docs/admin/issue-hub.html) 与 [已有 Issue](https://github.com/continew-org/continew-admin/issues)，避免重复提交；
3. 查阅 [使用指南](https://continew.top/docs/admin/guide/quick-start.html) 或询问 [DeepWiki](https://deepwiki.com/continew-org/continew-admin)。

一份好的 Bug 报告应当做到：

- **具体**：包含版本号、环境信息、相关配置，如涉及启动失败请附上完整日志；
- **可复现**：提供清晰的复现步骤，最好附上最小复现示例；
- **唯一**：不与已存在的问题重复。

> [!IMPORTANT]
> **请勿通过公开 Issue 报告安全漏洞**，请参阅 [安全策略](SECURITY.md)，通过 GitHub 安全通告负责任地披露。

## 代码贡献流程

### 1. Fork 仓库并克隆到本地

将 [continew-org/continew-admin](https://github.com/continew-org/continew-admin) Fork 到您的账号下，然后克隆到本地：

```bash
git clone https://github.com/<您的用户名>/continew-admin.git
cd continew-admin
```

> 上述地址以 GitHub 为例。社区在 AtomGit、Gitee 等平台也提供有官方仓库（见 [README](README.md)），在对应平台贡献时，将命令中的仓库地址替换为相应平台地址即可，流程一致。

### 2. 关联上游仓库

```bash
git remote add upstream https://github.com/continew-org/continew-admin.git
git fetch upstream
```

> `upstream` 仅用于同步主仓库最新代码，请勿直接向其推送，所有贡献都应推送到您的 fork 并通过 Pull Request 提交。

### 3. 创建特性分支

基于目标分支（通常为 dev）创建新分支，请勿直接在源分支上修改（源分支仅做同步 ContiNew 最新代码用）：

```bash
git checkout -b feat/your-feature upstream/dev
```

分支命名建议使用前缀标明变更类型：`feat/`（新功能）、`fix/`（Bug 修复）、`docs/`（文档）、`refactor/`（重构）、`test/`（测试）、`chore/`（构建、CI 或工具链变更）。

### 4. 开发与自测

开发完成后，推送前请在本地执行：

```bash
./mvnw verify         # Windows 为 mvnw.cmd verify
```

该命令会通过四道门禁：validate 阶段的 **Enforcer**（构建环境与依赖合规）、**Spotless check**（代码格式）、**Checkstyle**（代码规范），以及编译后 verify 阶段的 **SpotBugs**（字节码缺陷）。构建过程不会修改任何源码。

- 如果 Spotless 检查报红，执行 `./mvnw compile -Pformat` 自动修复格式，修复后请再执行一次正常构建确认通过；
- License Header 由 Spotless 在检查阶段自动校验，新建文件请从现有文件复制头部或让 `-Pformat` 自动补全；
- 请勿依赖 IDE 手工格式化，不同 IDE 的配置可能引入格式差异，请以 Spotless 检查结果为准。

### 5. 提交 Commit

提交信息请遵循 [Conventional Commits（约定式提交）1.0.0](https://www.conventionalcommits.org/zh-hans/v1.0.0/)规范，结构如下：

```
<类型>[可选作用域]: <描述>

[可选的正文]

[可选的脚注]
```

- **类型（type）**：说明变更性质。`feat` 表示新增功能，`fix` 表示 Bug 修复；其余常用类型：`docs`（文档）、`refactor`（重构）、`perf`（性能优化）、`test`（测试）、`style`（格式调整，不影响功能）、`build`（构建或依赖变更）、`ci`（CI 配置或脚本）、`chore`（其他杂项）、`revert`（回退提交）；
- **作用域（scope）**：可选，表示变更影响的模块。可取值：`common` `system` `server` `plugin` `extension` `auth` `open` `tenant` `schedule` `generator` `docker`；
- **描述（description）**：简短说明本次变更；
- **破坏性变更（breaking change）**：在类型或作用域后追加 `!`（如 `feat!:`），或在脚注中以 `BREAKING CHANGE: <说明>` 标注（对应主版本）。

示例：

```
feat(system): 用户管理支持批量导入
fix(tenant): 修复创建租户接口未加事务导致残留孤儿租户
refactor!: 移除已废弃的 XXX 配置项
```

> **PR 标题会被 CI 自动校验**：本项目采用 Squash and merge，PR 标题即最终提交信息，
> 因此 PR 标题同样必须符合上述规范，否则 `PR Validation / Validate PR title` 检查会失败。
> 标题写错时直接编辑标题即可重新触发校验。

### 6. 同步与变基

提交 PR 前，请同步主仓库最新代码并变基，保持提交历史单链清晰（避免产生 `Merge branch` 类提交）：

```bash
git fetch upstream
git rebase upstream/dev
```

### 7. 推送并创建 PR

```bash
git push origin feat/your-feature
```

在所在代码托管平台上向 **dev** 分支创建 Pull Request，并按 [PR 模板](.github/PULL_REQUEST_TEMPLATE.md) 填写说明信息（参考[已合并的 PR](https://github.com/continew-org/continew-admin/pulls?q=is%3Apr+is%3Amerged) 有助于提高合并率）。

### 8. 签署 CLA

提交 PR 后，所在平台的 CLA 校验机器人会提示签署 [CLA（贡献者许可协议）](CLA.md)（GitHub、AtomGit、Gitee 等主流代码托管平台均已支持，按机器人提示点击同意即可）。请确保 commit 使用的邮箱与您在对应代码托管平台账号绑定的邮箱一致。

在 GitHub 上贡献时，CI 还会校验每个 commit 的作者邮箱是否已绑定 GitHub 账号，未绑定的 PR 会被机器人评论提醒。如果不一致，最简单的方式是将该邮箱添加到 [GitHub 账号](https://github.com/settings/emails)，无需改写提交历史；也可以将本地 git 配置改为已绑定的邮箱后修订提交并强推，无需重新创建 PR：

```bash
git commit --amend --reset-author --no-edit   # 单提交：仅改作者，保留原提交信息
git push --force-with-lease
```

多个提交需要修改时，使用 `git rebase -i` 将相应提交标记为 `edit`，逐个执行上述 `amend` 后 `git rebase --continue`。推送后，CI 会自动重新校验。

### 9. 代码审查与合并

维护者会尽快审查您的 PR，并可能提出修改意见，这是正常协作的一部分。根据意见修改后推送即可（变基后再次推送如提示冲突，可 `git push -f` 强推到您自己的 fork 分支）。PR 合并后，下次贡献前请先同步最新代码，再从第 3 步开始。

## PR 检查清单

提交 PR 前，请对照以下清单自检：

- [ ] 一个 PR 只解决一个 Issue（只做一件事），不夹带无关改动
- [ ] 代码遵循已有风格，注释完善（含接口文档和参数示例），符合阿里巴巴《Java 开发手册(黄山版)》
- [ ] 本地 `./mvnw verify` 四道门禁全部通过
- [ ] 如有行为变更，已同步更新相关文档（含 Liquibase 变更日志）
- [ ] 按 PR 模板完整填写 Changelog 表格，并关联相关 Issue（Closes/Fixes/Resolves #<issue号>）
- [ ] commit message 符合 Conventional Commits（约定式提交）规范
- [ ] commit 作者邮箱已绑定所在代码托管平台账号

## 让 PR 更快被合并

- **尽早签署 CLA**：不少首次贡献者因忽略 CLA 机器人评论而卡住，未签署 CLA 的 PR 无法合并；
- **保证本地检查通过**：CI 未通过的 PR 不会被审查，推送前先在本地完整执行一遍 `./mvnw verify`；
- **保持改动聚焦且精简**：只做一件事的 PR 远比混杂无关改动的 PR 更容易审查，改动较大时请拆分为多个独立 PR；
- **撰写清晰的描述**：说明改了*什么*以及*为什么*，描述务必与实际 diff 一致；如果开发过程中范围发生了变化，请在请求审查前更新描述；
- **及时回应审查意见**：审查者提出修改意见后请尽快处理；如有不同意见，请说明理由而不是忽略评论。

## 文档贡献

文档与代码同样重要。发现文档存在错别字、表述不清或示例缺失，欢迎直接提 PR 修复（建议使用 `docs/` 前缀分支）；纯文档 PR 无需更新测试，但请确保文中涉及的命令、配置和代码片段准确无误。官方文档（[continew.top](https://continew.top)）的问题也可以先提 Issue 反馈。

## 社区

- **Issue 广场**：https://continew.top/docs/admin/issue-hub.html
- **官方交流群**：[入群方式](https://continew.top/discussion.html)。欢迎先提交 Issue 沉淀问题，再将 Issue 链接分享至交流群并 @ 我们，即可与维护团队及其他大佬用户直接交流探讨
- **DeepWiki AI**：https://deepwiki.com/continew-org/continew-admin

## 许可

向 ContiNew Admin 贡献代码即表示您同意您的贡献以 [Apache-2.0](LICENSE) 许可证进行许可。
