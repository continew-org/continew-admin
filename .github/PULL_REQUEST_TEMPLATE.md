<!--
  非常感谢您的 PR！提交前请确保本地执行 ./mvnw verify 通过全部门禁（Enforcer / Spotless / Checkstyle / SpotBugs）。
  贡献流程详见 CONTRIBUTING.md。
-->

## 变更类型

<!-- 只支持选择一种类型；如包含多种变更，建议拆分为多个 PR -->

- [ ] 新特性（feat）
- [ ] 问题修复（fix）
- [ ] 功能优化（refactor / perf / chore / style）
- [ ] 文档变更（docs）
- [ ] 构建、CI 或依赖升级（build / ci）
- [ ] 测试（test）
- [ ] 其他

## 破坏性变更

- [ ] 本 PR 包含破坏性变更（BREAKING CHANGE），已在变更目的中说明影响与迁移方式

## 变更目的

<!-- 描述本 PR 解决了什么问题。如果可以，请关联相关 Issue。 -->

## 解决方案

<!-- 描述是如何解决的。 -->

## 测试情况

<!-- 描述如何验证本次变更，如新增或更新的单元测试、手动验证步骤、截图等。 -->

## Changelog

| 模块 | Changelog | Related issues |
|------|-----------|----------------|
|      |           |                |

<!-- Related issues 建议使用 Closes #<issue号>、Fixes #<issue号> 或 Resolves #<issue号>，合并时将自动关闭对应 Issue。 -->

## 提交前确认

- [ ] 一个 PR 只解决一个 Issue，不夹带无关改动
- [ ] 本地 `./mvnw verify` 四道门禁全部通过（被 Spotless 拦截时使用 `-Pformat` 修复）
- [ ] 已完整填写 Changelog，并关联相关 Issue
- [ ] commit message 符合 [Conventional Commits（约定式提交）](https://www.conventionalcommits.org/zh-hans/v1.0.0/)规范
- [ ] 如包含 AI 生成的较大改动，相关 commit 已添加 `Assisted-by: <智能体>` 标记
- [ ] 已签署 [CLA](../blob/dev/CLA.md)（首次贡献可在 PR 创建后按机器人提示完成签署）
- [ ] 目标分支正确：dev（新功能与优化）或 x.x.x 维护分支（仅 Bug 修复）
