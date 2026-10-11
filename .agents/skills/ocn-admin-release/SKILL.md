---
name: ocn-admin-release
description: >
  continew-admin 仓库发版技能：预检 → CHANGELOG → release commit → tag+push 三远端 →
  GitHub/Gitee/AtomGit 三平台 release → 切维护分支。应用项目，无 Maven Central deploy 步骤。
  版本号不手动传：从根 pom.xml 的 <revision> 自动推导发版号（去掉 -SNAPSHOT）。
  用户说"发版"、"发布新版"、"release"、"准备发版"、"tag"、"打 tag"、"新建 GitHub Release"、
  "切维护分支"，或想看发版流程清单时使用。
  专用于 continew-admin 后端仓库，不适用其它项目；前端 continew-admin-ui 独立仓库独立流程，
  仅在完成后提醒同步发版。发版前业务侧自验（演示环境功能验收、前后端联调）由用户自行完成，skill 只做门禁与流程。
---

# continew-admin 发版

## 铁律

- **CHANGELOG 段必须先经用户校对确认才能做 release commit 与 tag**：第 2 步生成段落后停下展示，等用户明确回复（删改意见或确认），不得把校对环节与后面的推送确认合并成一步。用户校对版是发版全程唯一事实源，校对时删掉的条目不得再补回。
- **不可逆操作（git push / 建 release）执行前必须展示命令，等用户确认。**
- 版本号不传参，全部由 LLM 从 `<revision>` 推导并代入（后面所有命令都用这些变量）：

  | 变量 | 含义 | 推导 |
  |:--|:--|:--|
  | `${NEW_VERSION}` | 发版号 | `<revision>` 去掉 `-SNAPSHOT`（如 `4.2.0-SNAPSHOT` → `4.2.0`） |
  | `${PREV_TAG}` | 上一个 tag | 本地无新增 tag 时取本地最新；否则比对 `git ls-remote --tags origin` 取远端最新发布 tag（维护分支 tag 不在 dev 上） |
  | `${NEW_TAG}` | 本次 tag | `v${NEW_VERSION}` |
  | `${MAINT_BRANCH}` | 维护分支 | `${NEW_VERSION}` 的 `major.minor` + `.x` |
  | `${TODAY}` | 发版日期 | 当天 `YYYY-MM-DD` |

- **三平台 release 内容一致**：GitHub / Gitee / AtomGit 都放同一份 CHANGELOG 段。
- 发版后 dev 保持 `<revision>=${NEW_VERSION}`（不回 SNAPSHOT）；下个开发周期开始时由用户手动
  `build: 更新项目版本号至<major>.<minor+1>.0-SNAPSHOT`，README 徽章同时改回 SNAPSHOT。
- **维护分支修复必须即时合回 dev**：维护分支（x.y.x）上合入的每个 fix，合并时立即 cherry-pick
  （或按 dev 现状重写）到 dev，不要攒到发版前。
- **CHANGELOG 只认用户校对版**：用户校对时删掉的条目，发版全程不得再补回（三平台 release body
  同步以校对版为唯一事实源）。

## 流程

### 1. 预检（任一失败即停）

| 检查 | 命令 | 失败时 |
|:--|:--|:--|
| 在 dev 分支 | `git branch --show-current` | `git checkout dev` |
| 工作区只有发版文件 | `git status --short` | 多余改动先 commit / stash |
| `<revision>` 是 SNAPSHOT | `grep '<revision>[0-9.]*-SNAPSHOT' pom.xml` | 手动改 pom |
| 父 POM 是已发布的 starter 正式版 | `grep -A4 '<parent>' pom.xml` 确认 version 无 `-SNAPSHOT`，且该版本已发布（对照本地 continew-starter 仓库 tag 或 Maven Central） | 等 starter 发版后再发 admin |
| application.yml 版本一致 | `grep 'version:' continew-server/src/main/resources/config/application.yml` 应为 `${NEW_VERSION}-SNAPSHOT` | 同步改 |
| 上一个发布 tag 已 fetch 且有提交 | `git fetch origin --tags`，取最新发布 tag 为 ${PREV_TAG}，`git log ${PREV_TAG}..HEAD --oneline` | 区间空则停；网络失败（GitHub 连接重置时有发生）换代理重试 |
| 三远端 dev 同步状态 | `git ls-remote origin/gitee/atomgit refs/heads/dev` | 记录差异，第 4 步补推 |
| 门禁全过 | `./mvnw verify` | 先修到全过（Enforcer → Spotless → Checkstyle → 单测 → SpotBugs，见 AGENTS.md） |

> 发版文件只有 4 类：`CHANGELOG.md` / `README.md` / `pom.xml` /
> `continew-server/src/main/resources/config/application.yml`（外加可选的 docs 版本字样，见第 2 步）。
> 不检查：origin/dev 领先落后、GPG、Central 凭证（应用项目无 deploy 步骤）。

通过后展示并确认：

```
发版号: ${NEW_VERSION}   上一个 tag: ${PREV_TAG}   维护分支: ${MAINT_BRANCH}   日期: ${TODAY}
继续？[Enter 继续 / n 退出]
```

### 2. CHANGELOG 段 + README + docs

```bash
git log ${PREV_TAG}..HEAD --format='%h %aI %an %s'
```

按前缀分桶，**桶内按时间倒序**（对齐 conventional-changelog 生态与 GitHub auto release notes 的主流做法；Keep a Changelog 仅规定版本倒序，段内条目倒序为社区默认）。

| commit 前缀 | 归到 | 说明 |
|:--|:--|:--|
| `feat` | ✨ 新特性 | |
| `fix` | 🐛 问题修复 | |
| `refactor` / `perf` / `chore` / `style` | 💎 功能优化 | chore/style 默认进，校对时删纯 housekeeping |
| `docs` / `ci` / `test` / 其它 `build:` | 跳过 | |

行格式**沿用 `CHANGELOG.md` 既有版本段**（对齐 v4.1.0 段写法）：`- 描述 ([短哈希](commit_url))`，
外部贡献者在描述前带 `平台#编号@用户名`，PR 合并在末尾补 `(#PR)`。

**贡献者署名防误引（4.2.0 发版教训）**：GitHub 用户用裸 `@用户名` 结尾（mention 到本人，正确归因）；
**非 GitHub 平台贡献者禁止裸 `@`**，用 `Gitee@用户名` / `GitCode@用户名` 前缀——GitHub 会把裸 @ 解析为
mention，@ 到同名的无关账号（liquor、KAI 先例）。`(GitHub#PR@用户名)` 括号内格式不受 mention 解析影响；
正文中 `@since` 等非用户名的 @ 词用反引号包裹。判断依据：贡献历史里的平台前缀（如 4.1.0 的
`(GitHub#76@ali-pay)` 说明 ali-pay 是真 GitHub 用户，可裸 @）。

**依赖升级以父 POM 实际变更为准**：升级可能内嵌在 feat 提交里（如 4.2.0 周期 `continew-starter
2.16.1 => 2.17.0` 藏在 58803b5d），用 `git log ${PREV_TAG}..HEAD -p -- pom.xml` 中 parent
version 的变化推导，不全靠 `build(dependencies):` 前缀。

**写入前硬性校验**：每个写进 CHANGELOG 的提交 hash 必须真实存在于当前 dev，逐条
`git merge-base --is-ancestor <hash> HEAD` 验证。

组装成段 prepend 到 `CHANGELOG.md` 顶部，段标题格式
`## [v${NEW_VERSION}](https://github.com/continew-org/continew-admin/compare/${PREV_TAG}...${NEW_TAG}) (${TODAY})`，
然后**停下展示给用户校对，等明确确认后才能进入第 3 步 release commit**（铁律）。

同时改（都并入发版文件）：

- **README 徽章**：`badge/SNAPSHOT-v${NEW_VERSION}` → `badge/RELEASE-v${NEW_VERSION}`。
- **README 功能列表**：本周期有用户可感知的新特性时同步措辞（v4.1.0 先例：回收站、分片上传入列）。
- **docs 版本字样**（4 处 `4.2.0-SNAPSHOT` → `4.2.0`，纯文档改动不触发门禁，可并入发版 commit）：
  `docs/introduction.mdx`（认证段）、`docs/development/backend/structure.mdx`（`${revision}` 说明）、
  `docs/reference/deployment.mdx`（容器镜像 tag 表）、`docs/reference/faq.mdx`（starter 版本问答）。
  发版时按当期实际位置重新 `grep -rn "${NEW_VERSION}-SNAPSHOT" docs/` 定位。
- **plan.mdx 历史任务**（需用户确认内容后加）：`docs/reference/plan.mdx` 按惯例每版本补一行主要任务。

### 3. release commit

- `git add` 上述 4 类发版文件（README、docs 有改动才加）→ `git commit -m "release: v${NEW_VERSION}"`。

### 4. tag + push（一次确认）

```bash
git tag -a ${NEW_TAG} -m "release: v${NEW_VERSION}"
git push origin dev
git push origin ${NEW_TAG}
```

（不用 `git push --tags`，避免误推其它本地 tag。）

随后核对预检记录的三远端状态，落后的补推（**push dev 到 origin 会触发 Deploy 工作流自动重建
演示站点，属预期行为**）：

```bash
git ls-remote gitee refs/heads/dev refs/tags/${NEW_TAG}
git ls-remote atomgit refs/heads/dev refs/tags/${NEW_TAG}
# 落后则：
git push gitee dev && git push gitee ${NEW_TAG}
git push atomgit dev && git push atomgit ${NEW_TAG}
```

> admin 的 atomgit 远端直接指向 `atomgit.com/continew/continew-admin`。同类仓库 continew-starter
> 已于 2026-10-06 关闭镜像改为普通仓库，admin 是否仍有自动同步以 `ls-remote` 实测为准，落后就手动推。

### 5. 三平台 release（GitHub 确认一次，Gitee+AtomGit 合并确认一次）

> 凭证一律从本机环境变量取（`${GITEE_TOKEN}` / `${ATOMGIT_TOKEN}`），不写入仓库任何文件；
> 工具名随宿主而异，**不要假定某个 MCP 工具一定存在**，有对应工具就用，没有就走等价 REST。

- **GitHub**（优先 `gh` CLI）：`gh release create ${NEW_TAG} --title "v${NEW_VERSION}" --generate-notes --target dev`，
  再 `gh release edit ${NEW_TAG} --notes-file <CHANGELOG 段>`，必须两步，把完整 CHANGELOG 盖上去才跟 Gitee 一致。
- **Gitee**：REST（owner 是 `continew` 不是 `continew-org`）：
  ```bash
  curl -X POST "https://gitee.com/api/v5/repos/continew/continew-admin/releases?access_token=${GITEE_TOKEN}" \
    -H "Content-Type: application/json" \
    -d '{"tag_name":"'${NEW_TAG}'","name":"v'${NEW_VERSION}'","body":"<同一段>","target_commitish":"dev"}'
  ```
- **AtomGit**（REST API + curl，域名不是笔误：git 远端与网页用 `atomgit.com`，REST API 用 `api.atomgit.com`）：
  ```bash
  curl -X POST "https://api.atomgit.com/api/v5/repos/continew/continew-admin/releases?access_token=${ATOMGIT_TOKEN}" \
    -H "Content-Type: application/json" \
    -d '{"tag_name":"'${NEW_TAG}'","name":"v'${NEW_VERSION}'","body":"<同一段>","target_commitish":"dev"}'
  ```
  创建后必须再 PATCH 一次标记"最新版本"（POST 不带 release_status 字段；PATCH body 缺任何必填字段都 400）：
  ```bash
  curl -X PATCH "https://api.atomgit.com/api/v5/repos/continew/continew-admin/releases/${NEW_TAG}?access_token=${ATOMGIT_TOKEN}" \
    -H "Content-Type: application/json" \
    -d '{"tag_name":"'${NEW_TAG}'","name":"v'${NEW_VERSION}'","body":"<同一段>","release_status":"latest"}'
  ```
  验证：`GET .../releases/latest` 应返回 ${NEW_TAG}。

### 6. 维护分支

```bash
git checkout -b ${MAINT_BRANCH} ${NEW_TAG}
git push origin ${MAINT_BRANCH}
git push atomgit ${MAINT_BRANCH}
git checkout dev
```

（维护分支只是本次发版的快照，不做任何额外改动；4.1.x 先例在 origin 与 atomgit 都有，gitee 以实测为准。）

## 完成

展示最终状态并提醒后续：

```
✅  v${NEW_VERSION} 发版完成
  - release commit / tag: ${NEW_TAG} (pushed) / maintenance: ${MAINT_BRANCH} (pushed)
  - GitHub: https://github.com/continew-org/continew-admin/releases/tag/${NEW_TAG}
  - Gitee:  https://gitee.com/continew/continew-admin/releases/tag/${NEW_TAG}
  - AtomGit: https://atomgit.com/continew/continew-admin
后续：
  - push origin/dev 已触发 Deploy 工作流，演示站点会自动重建为 ${NEW_VERSION}
  - 前端 continew-admin-ui 同为 ${NEW_VERSION}-SNAPSHOT，需在其仓库按同一节奏发版（独立流程，本 skill 不覆盖）
  - 下个开发周期开始时，在 dev 手动 commit: "build: 更新项目版本号至<major>.<minor+1>.0-SNAPSHOT"，
    并把 README 徽章改回 SNAPSHOT-v<major>.<minor+1>.0
```
