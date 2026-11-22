// PR 正文校验回归：被测对象是 scripts/ci/extract-pr-body-script.js 从
// .github/workflows/pr-validation.yml 提取出的 validate-pr-body 真实 inline JS，
// 不是测试内置的规则副本——规则只有落回 yml 才算生效，测试才能感知；yml 中找不到
// 该 job 的 script 块时提取器直接报错退出，不会静默跳过。
// 除 16 样本回归外还做模板一致性断言：.github/PULL_REQUEST_TEMPLATE.md 与校验规则
// 是机器契约，不同步时失败并指向 AGENTS.md「PR 模板与校验规则是机器契约」。
// 纯 node 零第三方依赖（CommonJS、无顶层 await），GitHub 托管 runner 预装 node 可直接运行。
//
// 用法：node scripts/ci/test-pr-body-validation.js [--yml <path>] [--template <path>]
// --yml / --template 仅供本地变异验证（喂入改过的 yml/模板副本观察断言失败），
// CI 不传参，读的是检出工作区里的真实文件。
'use strict';

const fs = require('fs');
const path = require('path');
const vm = require('vm');
const { extractCoreSource } = require('./extract-pr-body-script.js');

const CONTRACT_HINT = '模板与校验规则不同步，见 AGENTS.md「PR 模板与校验规则是机器契约」';

function parseArg(name) {
    const idx = process.argv.indexOf(name);
    return idx >= 0 ? process.argv[idx + 1] : null;
}

const repoRoot = path.resolve(__dirname, '..', '..');
const ymlPath = parseArg('--yml') || path.join(repoRoot, '.github', 'workflows', 'pr-validation.yml');
const templatePath = parseArg('--template') || path.join(repoRoot, '.github', 'PULL_REQUEST_TEMPLATE.md');

// ===== 被测对象：从 yml 提取的校验核心（vm 加载，不经过文件系统落盘）=====
const coreSource = extractCoreSource(fs.readFileSync(ymlPath, 'utf8'));
const { validatePrBody } = (function loadExtractedCore(source) {
    const target = { exports: {} };
    vm.runInNewContext(source, { module: target, exports: target.exports },
        { filename: 'pr-validation.yml (validate-pr-body inline JS)' });
    return target.exports;
})(coreSource);

// ===== 规则侧取值：一律从提取出的核心源码解析，禁止在测试里复制规则常量 =====
// （复制即第二事实源：规则改版只落测试不落 yml 时，测试会继续放行，契约失效）

// EXPECTED_SECTIONS 未由核心导出，按源码字面解析（元素为无转义的单引号字符串）
function parseExpectedSections(source) {
    const m = source.match(/const EXPECTED_SECTIONS = \[([\s\S]*?)\];/);
    if (!m) {
        throw new Error('提取的核心源码中未找到 EXPECTED_SECTIONS 定义');
    }
    const items = [...m[1].matchAll(/'((?:[^'\\]|\\.)*)'/g)].map(x => x[1]);
    if (items.length === 0) {
        throw new Error('EXPECTED_SECTIONS 解析结果为空');
    }
    return items;
}

// R2 失败文案内嵌的变更类型选项数——核心中唯一可机读的 checkbox 计数
// （其余 checkbox 段规则刻意不锚定计数，见下方结构快照说明）
function parseR2TypeCount(source) {
    const m = source.match(/请在 (\d+) 个选项中勾选/);
    if (!m) {
        throw new Error('核心源码中未找到 R2 文案的选项数（请在 N 个选项中勾选）');
    }
    return Number(m[1]);
}

// ===== 模板侧解析：独立实现，读的是真实模板文件（与规则侧交叉比对）=====

// ^## 段名清单（trim 后精确比对，口径与规则切段一致但实现独立）
function templateSections(text) {
    const names = [];
    for (const line of text.split('\n')) {
        const m = line.match(/^## (.*?)\s*$/);
        if (m) {
            names.push(m[1].trim());
        }
    }
    return names;
}

// 按段统计 checkbox 行数（含勾选与未勾选，口径同规则的 checkboxStats）
function templateCheckboxCounts(text) {
    const counts = {};
    let current = null;
    for (const line of text.split('\n')) {
        const m = line.match(/^## (.*?)\s*$/);
        if (m) {
            current = m[1].trim();
            continue;
        }
        if (current && /^\s*[-*] \[( |x|X)\]/.test(line)) {
            counts[current] = (counts[current] || 0) + 1;
        }
    }
    return counts;
}

// Changelog 段表头行各列名（规则 R7/R8 按列序读取第 2/3 列，列序是契约的一部分）
function templateChangelogHeaderCells(text) {
    let inChangelog = false;
    for (const line of text.split('\n')) {
        const m = line.match(/^## (.*?)\s*$/);
        if (m) {
            inChangelog = m[1].trim() === 'Changelog';
            continue;
        }
        if (inChangelog && line.trim().startsWith('|')) {
            return line.trim().replace(/^\||\|$/g, '').split('|').map(c => c.trim());
        }
    }
    return null;
}

// ===== 16 样本回归 =====
// S1 为 PR #320 真实 body 原文（内嵌，仓库不落样本文件）；TEMPLATE 为原始模板副本，
// 用于构造占位残留类对抗样本。
const PR320_BODY = [
    '<!-- 非常感谢您的 PR！贡献流程详见 CONTRIBUTING.md。 -->',
    '',
    '## 变更类型',
    '',
    '<!-- 只支持选择一种类型；如包含多种变更，建议拆分为多个 PR -->',
    '',
    '- [x] 新特性（feat）',
    '- [x] 问题修复（fix）',
    '- [x] 功能优化（refactor / perf / chore / style）',
    '- [ ] 文档变更（docs）',
    '- [ ] 构建、CI 或依赖升级（build / ci）',
    '- [x] 测试（test）',
    '- [ ] 其他',
    '',
    '> 主体是「接入 layouts 模块 + 消息中心 + 敏感信息组件」三个新特性，附带修复其在接入与 review 中暴露的回归，故按实际多选。',
    '',
    '## 影响范围',
    '',
    '<!-- 跨端改动需附两侧验证证据（后端 ./mvnw verify 与前端 pnpm 检查，详见 CONTRIBUTING.md「开发与自测」）。 -->',
    '',
    '- [ ] 后端（continew-server / system / common / plugin）',
    '- [x] 前端（continew-ui）',
    '- [ ] 跨端（后端契约或菜单字段变更，需两侧联调）',
    '- [ ] 文档 / 模板 / CI',
    '',
    '## 破坏性变更',
    '',
    '- [x] 本 PR 包含破坏性变更（BREAKING CHANGE），已在变更目的中说明影响与迁移方式',
    '',
    '> `de2c11c` 将原 `components/app-shell` 整体迁移为 `src/layouts/`，旧路径删除（commit message 含 `refactor!` 语义）；前端内部模块迁移，无后端契约变化，无需数据迁移。',
    '',
    '## 变更目的',
    '',
    '为 `continew-ui` 引入独立的 `layouts/` 模块（应用外壳、偏好设置面板、标签栏、头部组件、消息中心、登录弹窗），替换原 `app-shell` 的实现并扩展偏好设置能力；新增消息中心页（`/(app)/user/message`）与敏感信息遮罩组件；修复接入过程及 review 中暴露的回归与安全/性能问题。',
    '',
    '## 解决方案',
    '',
    '**新增功能（feat）**',
    '- `src/layouts/`：应用外壳 + 偏好设置面板（标签栏模式、导航固定、动画开关、半深色头部/侧栏、灰度/色弱模式）+ 标签栏 + 头部（搜索、刷新、通知、GitHub、锁屏、主题、全屏、用户菜单）+ 登录消息弹窗 + 消息中心页。',
    '- `components/sensitive-text.tsx` + `lib/mask.ts`：敏感字段（手机号/邮箱/身份证等）脱敏展示与点击显隐；新增 `tests/mask.test.ts`（71 行）/`tests/sanitize.test.ts`（50 行）两个测试。',
    '- `lib/date-locale.ts`：日历/日期选择器中文化收敛到单一入口。',
    '',
    '**安全修复（fix）**',
    '- `rich-text-editor.tsx`：后端返回的 HTML 统一经 `lib/sanitize.ts` 消毒后渲染，修复存储型 XSS。',
    '- `lib/http.ts`：会话过期只提示一次，不再每次请求弹 Toast。',
    '- `app-secret-dialog.tsx`：App Secret 改为脱敏展示。',
    '',
    '**性能/体验修复（perf/fix）**',
    '- `use-menu.ts`：菜单缓存加载，不再每次导航请求 `/auth/user/route`。',
    '- `dashboard-charts.tsx` / `use-available-height.ts`：侧栏折叠动画期间暂停图表/表格重排，消除卡顿。',
    '- `preferences-sheet.tsx`：标签切换动画在接入时误删→先恢复（`9e4f236`），再按「抖动」反馈改为 Base UI `Tabs.Indicator` 纯 CSS 滑动（250ms 无回弹）+ 面板 200ms 淡入上抬，移除弹簧高度形变（`3af2d5b`）。',
    '- `admin-header.tsx`：GitHub 帮助图标移到图标组最左（搜索框右侧）。',
    '- `profile-header.tsx`：补 `pt-5` 顶距（页面关闭了外壳 padding 导致贴标签栏）、头像 80→64px、占位文字加大。',
    '',
    '**重构（chore/refactor）**',
    '- `components/app-shell/*` 16 个文件迁移/合并到 `layouts/`；`stores/shell.ts` 合并进 `layouts/shell-store.ts`；`shell-preferences.ts` 拆到 `lib/preferences/`。',
    '- 新增依赖：`nprogress`、`@ctrl/tinycolor`、`ahooks`（过渡/主题/工具）。',
    '',
    '## 测试情况',
    '',
    '- `pnpm fmt` / `pnpm lint`（oxlint 0 新增告警）/ `pnpm typecheck`（0 error）/ `pnpm build` 全部通过。',
    '- 新增单元测试：`mask.test.ts`（脱敏）、`sanitize.test.ts`（HTML 消毒）。',
    '- 部署到内网预览环境实测：偏好面板切换标签时滑块 x=1079→1260 平滑滑动无回弹、面板透明度 0.58→1.00、高度全程不变（无抖动）；头部图标顺序、个人中心顶距 20px/头像 64px/占位字 20px 经 computed style + 截图确认；消息中心弹窗/列表走通。',
    '- 后端未改动，未执行 `./mvnw verify`。',
    '',
    '## Changelog',
    '',
    '| 模块 | Changelog | Related issues |',
    '|------|-----------|----------------|',
    '| continew-ui | 接入 layouts 模块与消息中心；新增敏感信息脱敏组件、日历中文化；修复富文本 XSS、会话过期重复提示、菜单重复请求、侧栏折叠卡顿、偏好面板动画抖动、个人中心贴边等问题 | — |',
    '',
    '## 提交前确认',
    '',
    '- [x] 一个 PR 只解决一个 Issue，不夹带无关改动（全部改动均为 layouts 接入及其配套修复）',
    '- [ ] 后端改动：本地 `./mvnw verify` 全部通过（本次无后端改动）',
    '- [x] 前端改动：`continew-ui` 下 `pnpm check` 全部通过（fmt/lint/typecheck/build）',
    '- [ ] （可选）已在本地完成 AI 自检（`open-code-review-delegate`）',
    '- [x] 代码注释只写当前约束；变更历史在 commit message，修复叙述在 PR 正文，均不混入注释',
    '- [x] 已完整填写 Changelog，并关联相关 Issue',
    '- [x] commit message 符合 [Conventional Commits（约定式提交）](https://www.conventionalcommits.org/zh-hans/v1.0.0/)规范',
    '- [x] 如包含 AI 生成的较大改动，相关 commit 已添加 `Assisted-by: <智能体>` 标记（layouts 接入及修复由 AI 辅助完成）',
    '',
    '',
].join('\n');

const TEMPLATE = `<!--
  非常感谢您的 PR！贡献流程详见 CONTRIBUTING.md。
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

## 影响范围

<!-- 跨端改动需附两侧验证证据（后端 ./mvnw verify 与前端 pnpm 检查，详见 CONTRIBUTING.md「开发与自测」）。 -->

- [ ] 后端（continew-server / system / common / plugin）
- [ ] 前端（continew-ui）
- [ ] 跨端（后端契约或菜单字段变更，需两侧联调）
- [ ] 文档 / 模板 / CI

## 破坏性变更

- [ ] 本 PR 包含破坏性变更（BREAKING CHANGE），已在变更目的中说明影响与迁移方式

## 变更目的

<!-- 描述本 PR 解决了什么问题。如果可以，请关联相关 Issue。
     以变更者第一人称视角陈述变更本身，不写成对特定读者的解释或会话回复（「如您所问」「请查看」）。 -->

## 解决方案

<!-- 描述是如何解决的。修复原因、旧实现缺陷与修复前后的行为对比写在这里，不要写进代码注释。 -->

## 测试情况

<!-- 描述如何验证本次变更，如新增或更新的单元测试、手动验证步骤、截图等。 -->

## Changelog

| 模块 | Changelog | Related issues |
|------|-----------|----------------|
|      |           |                |

<!-- Related issues 建议使用 Closes #<issue号>、Fixes #<issue号> 或 Resolves #<issue号>，合并时将自动关闭对应 Issue。 -->

## 提交前确认

- [ ] 一个 PR 只解决一个 Issue，不夹带无关改动
- [ ] 后端改动：本地 \`./mvnw verify\` 全部通过（被 Spotless 拦截时使用 \`-Pformat\` 修复）
- [ ] 前端改动：\`continew-ui\` 下 \`pnpm check\` 全部通过
- [ ] （可选）已在本地完成 AI 自检（\`open-code-review-delegate\`）
- [ ] 代码注释只写当前约束；变更历史在 commit message，修复叙述在 PR 正文，均不混入注释
- [ ] 已完整填写 Changelog，并关联相关 Issue
- [ ] commit message 符合 [Conventional Commits（约定式提交）](https://www.conventionalcommits.org/zh-hans/v1.0.0/)规范
- [ ] 如包含 AI 生成的较大改动，相关 commit 已添加 \`Assisted-by: <智能体>\` 标记
- [ ] 已签署 [CLA](../blob/dev/CLA.md)（首次贡献可在 PR 创建后按机器人提示完成签署）
- [ ] 目标分支正确：dev（新功能与优化）或 x.x.x 维护分支（仅 Bug 修复）
`;

// 在模板基础上替换段内容的辅助函数（section 标题保持，内容整体替换）
function fillTemplate(overrides) {
    let t = TEMPLATE;
    for (const [section, content] of Object.entries(overrides)) {
        const re = new RegExp(`(## ${section}\\n)[\\s\\S]*?(?=\\n## |\\n*$)`);
        t = t.replace(re, `$1\n${content}\n`);
    }
    return t;
}

// ===== 用例收集与输出（风格与 16 样本回归一致：逐条 PASS/FAIL + 汇总 + 非零退出码）=====
const cases = [];

function record(name, pass, detail) {
    cases.push({ name, pass, detail: pass ? '' : detail });
}

function expectSample(name, body, fn) {
    let pass = false;
    let detail = '';
    try {
        const r = validatePrBody(body);
        pass = fn(r);
        if (!pass) {
            detail = JSON.stringify(r, null, 2);
        }
    } catch (e) {
        detail = String((e && e.stack) || e);
    }
    record(name, pass, detail);
}

function expectContract(name, fn) {
    let pass = false;
    let detail = '';
    try {
        const r = fn();
        pass = r.pass;
        detail = r.detail;
    } catch (e) {
        detail = String((e && e.stack) || e);
    }
    record(name, pass, detail);
}

// S1 真实 PR #320 body（多选 4 项 + 改写 checkbox 文本 + 删除 2 个确认项 + Related issues 为 —）
expectSample('S1: PR #320 真实 body → violations 应为空，advisories 仅 R3 互斥', PR320_BODY, r =>
    r.violations.length === 0 &&
    r.advisories.length === 1 &&
    r.advisories[0].rule === 'R3-变更类型互斥' &&
    r.advisories[0].message.includes('4 项'));

// S2 空 body
expectSample('S2: 空 body → R1 全部缺段', '', r =>
    r.violations.filter(v => v.rule === 'R1-段落完整性').length === 8 && r.violations.length === 8);

// S3 null body（API 上 body 可为 null）
expectSample('S3: null body → R1 全部缺段', null, r =>
    r.violations.filter(v => v.rule === 'R1-段落完整性').length === 8 && r.violations.length === 8);

// S4 原始模板未填（占位注释残留 + checkbox 全不勾 + Changelog 空行）
expectSample('S4: 原始模板未填 → R2/R4/R6/R7 全部命中，R9 不误伤', TEMPLATE, r => {
    const rules = r.violations.map(v => v.rule);
    return rules.includes('R2-变更类型未勾选') &&
        rules.includes('R4-影响范围未勾选') &&
        rules.includes('R6-变更目的为空') &&
        rules.includes('R6-解决方案为空') &&
        rules.includes('R6-测试情况为空') &&
        rules.includes('R7-Changelog表格') &&
        r.violations.filter(v => v.rule.startsWith('R1')).length === 0 &&
        r.violations.filter(v => v.rule.startsWith('R9')).length === 0;
});

// S5 整段缺失（删掉「## 解决方案」整段）
expectSample('S5: 删除解决方案整段 → R1 命中，其余不误伤', TEMPLATE.replace(/## 解决方案[\s\S]*?(?=\n## 测试情况)/, ''), r => {
    const rules = r.violations.map(v => v.rule);
    return rules.includes('R1-段落完整性') &&
        r.violations.find(v => v.rule === 'R1-段落完整性').message.includes('解决方案');
});

// S6 破坏性变更勾选但变更目的为空
expectSample('S6: 勾选破坏性变更 + 变更目的空 → R5 命中', TEMPLATE
    .replace('- [ ] 本 PR 包含破坏性变更', '- [x] 本 PR 包含破坏性变更'), r =>
    r.violations.some(v => v.rule === 'R5-破坏性变更联动'));

// S7 关键词缺 # 号（正文与表格两处）+ 裸关键词容差
expectSample('S7a: 表格 Related issues 列裸关键词「Closes」→ R8 命中', TEMPLATE
    .replace('|      |           |                |', '| ui | 修复XSS | Closes |'), r =>
    r.violations.filter(v => v.rule === 'R8-关联issue关键词').length === 1);
expectSample('S7b: 正文「Fixes #」缺号 → R8 命中', fillTemplate({ 变更目的: '修复登录失败问题\n\nFixes #' }), r =>
    r.violations.filter(v => v.rule === 'R8-关联issue关键词').length >= 1);
expectSample('S7c: 正规「Closes #123」→ 不命中', fillTemplate({ 变更目的: '修复登录失败问题\n\nCloses #123' }), r =>
    r.violations.filter(v => v.rule === 'R8-关联issue关键词').length === 0);
expectSample('S7d: Related issues 列占位「—」→ 不命中', TEMPLATE
    .replace('|      |           |                |', '| ui | 修复XSS | — |'), r =>
    r.violations.filter(v => v.rule === 'R8-关联issue关键词').length === 0);
expectSample('S7e: 正文散文裸词「Fixes the bug」→ 不命中（裸词只在列内查）', fillTemplate({
    变更目的: 'Fixes the bug of login redirect.',
    解决方案: 'It fixes the redirect loop.',
}), r =>
    r.violations.filter(v => v.rule === 'R8-关联issue关键词').length === 0);

// S8 fenced block 内的假 heading / checkbox 不参与判定
expectSample('S8: 解决方案段贴含 ## Changelog 的代码块 → R1 不误判', fillTemplate({
    解决方案: '```\n## Changelog\n- [x] 假勾选\n```\n实际解决方案内容。',
}), r =>
    r.violations.filter(v => v.rule.startsWith('R1')).length === 0 &&
    r.violations.filter(v => v.rule === 'R7-Changelog表格').length === 1);

// S9 大小写变体与 X 勾选容忍
expectSample('S9: 「changelog」小写标题 + [X] 勾选', fillTemplate({ 变更类型: '- [X] 新特性（feat）' })
    .replace('## Changelog', '## changelog'), r => {
    // 小写 changelog 标题 → R1 报缺段（精确匹配，fail-closed）：
    // 设计取舍是标题必须与模板一致；这里验证的是不会因小写而漏检 Changelog 空表
    return r.violations.some(v => v.rule === 'R1-段落完整性' && v.message.includes('Changelog'));
});

// S10 提交前确认整段掏空（只留标题）
expectSample('S10: 提交前确认段被掏空 → R9 命中', TEMPLATE.replace(/## 提交前确认\n[\s\S]*$/, '## 提交前确认\n'), r =>
    r.violations.some(v => v.rule === 'R9-提交前确认'));

// S11 勾选状态写作 `* [x]`（星号列表）也计数
expectSample('S11: 星号 checkbox 计数', fillTemplate({ 变更类型: '* [x] 新特性（feat）' }), r =>
    r.violations.filter(v => v.rule === 'R2-变更类型未勾选').length === 0 &&
    r.violations.filter(v => v.rule === 'R3-变更类型互斥').length === 0);

// S12 超长 body（64k）线性可跑
const big = PR320_BODY + '\n\n<!-- padding -->' + 'x'.repeat(60000);
expectSample('S12: 63k 超长 body 判定结果与原 body 一致', big, r =>
    r.violations.length === 0 && r.advisories.length === 1);

// ===== 模板一致性断言（J2②）：真实模板文件 ↔ 提取出的规则常量，不同步即失败 =====
const templateText = fs.readFileSync(templatePath, 'utf8').replace(/\r\n/g, '\n');

expectContract('C1: 模板段名集合与 EXPECTED_SECTIONS（提取自 yml）一致', () => {
    const fromTemplate = templateSections(templateText);
    const fromRules = parseExpectedSections(coreSource);
    const sameSet = fromTemplate.length === fromRules.length &&
        [...fromTemplate].sort().join('\n') === [...fromRules].sort().join('\n');
    return {
        pass: sameSet,
        detail: `模板: [${fromTemplate.join(', ')}]\n规则: [${fromRules.join(', ')}]\n${CONTRACT_HINT}`,
    };
});

expectContract('C2: 模板「变更类型」checkbox 数与规则 R2 文案的选项数一致', () => {
    const counts = templateCheckboxCounts(templateText);
    const ruleCount = parseR2TypeCount(coreSource);
    return {
        pass: counts['变更类型'] === ruleCount,
        detail: `模板「变更类型」实际 ${counts['变更类型'] || 0} 项，规则 R2 文案 ${ruleCount} 项\n${CONTRACT_HINT}`,
    };
});

// checkbox 段结构快照：规则对 checkbox 计数刻意不敏感（R4/R5/R9 只判 0 / >1 / total===0），
// 计数变化不影响规则逻辑，但会使规则文案与设计前提失真；此快照锚定的是模板结构本身，
// 模板增删 checkbox 项时本断言失败，按 AGENTS.md 条款在同一 PR 同步规则并更新本快照。
// 快照值不是规则常量（规则内并无 4/1/10 这些数字），「变更类型」的 7 另受 C2 规则侧数字交叉约束。
const CHECKBOX_SECTION_SNAPSHOT = { 变更类型: 7, 影响范围: 4, 破坏性变更: 1, 提交前确认: 10 };

expectContract('C3: 模板各 checkbox 段数量与结构快照一致（变更类型 7 / 影响范围 4 / 破坏性变更 1 / 提交前确认 10）', () => {
    const counts = templateCheckboxCounts(templateText);
    const diffs = Object.entries(CHECKBOX_SECTION_SNAPSHOT)
        .filter(([name, n]) => counts[name] !== n)
        .map(([name, n]) => `「${name}」快照 ${n} 项 / 模板实际 ${counts[name] || 0} 项`);
    return {
        pass: diffs.length === 0,
        detail: `${diffs.join('\n')}\n${CONTRACT_HINT}`,
    };
});

expectContract('C4: 模板 Changelog 表头列序与规则读取列位一致（第 2 列 Changelog、第 3 列 Related issues）', () => {
    const cells = templateChangelogHeaderCells(templateText);
    const ok = !!cells && cells.length >= 3 && cells[1] === 'Changelog' && cells[2] === 'Related issues';
    return {
        pass: ok,
        detail: `表头: [${cells ? cells.join(', ') : '未找到表格'}]；R7/R8 按列序读取第 2/3 列\n${CONTRACT_HINT}`,
    };
});

// 真实模板整卷过真实规则：未填模板的违规签名是结构前提的行为化锁定——
// R1 静默（段名全部识别）、R9 静默（提交前确认段未被掏空）、R5 静默（破坏性变更未勾）等
// 一旦模板结构变化打破规则前提，签名即变化，此处失败
expectContract('C5: 真实模板整卷过提取规则 → 违规恰为 R2/R4/R6×3/R7，提醒为空', () => {
    const r = validatePrBody(templateText);
    const expected = ['R2-变更类型未勾选', 'R4-影响范围未勾选', 'R6-变更目的为空',
        'R6-解决方案为空', 'R6-测试情况为空', 'R7-Changelog表格'].sort();
    const actual = r.violations.map(v => v.rule).sort();
    return {
        pass: JSON.stringify(actual) === JSON.stringify(expected) && r.advisories.length === 0,
        detail: `violations: [${r.violations.map(v => v.rule).join(', ')}]` +
            ` advisories: [${r.advisories.map(a => a.rule).join(', ')}]\n${CONTRACT_HINT}`,
    };
});

// ===== 汇总输出 =====
let failed = 0;
console.log('== 16 样本回归（被测对象：pr-validation.yml 提取的 validatePrBody）==');
for (const c of cases.filter(x => x.name.startsWith('S'))) {
    console.log(`${c.pass ? 'PASS' : 'FAIL'}  ${c.name}`);
    if (!c.pass) {
        failed++;
        console.log(c.detail);
    }
}
console.log('');
console.log('== 模板一致性断言（PULL_REQUEST_TEMPLATE.md ↔ 提取的规则常量）==');
for (const c of cases.filter(x => x.name.startsWith('C'))) {
    console.log(`${c.pass ? 'PASS' : 'FAIL'}  ${c.name}`);
    if (!c.pass) {
        failed++;
        console.log(c.detail);
    }
}
console.log('');
console.log(`${cases.length - failed}/${cases.length} passed`);
process.exit(failed ? 1 : 0);
