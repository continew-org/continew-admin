// 从 .github/workflows/pr-validation.yml 提取 validate-pr-body job 的 script 块（inline JS）。
// 校验规则回归的事实源工具：test-pr-body-validation.js 的被测对象是本模块提取出的真实生产
// 字节，测试里不另存规则副本——规则只有落回 workflow 文件才会被测试感知。
// 锚定策略：先定位 validate-pr-body job 键，再在其后定位 script 步骤名与第一个 script: |
// 块，全程不依赖行号（文件顶部将来增删内容不会漂移）；同文件 check-commit-authors job
// 也有 script 块，但位于本 job 之前且步骤名不同，不会被误取。
'use strict';

const fs = require('fs');
const path = require('path');

// job 键行：只匹配 jobs: 下的顶层键（行首缩进 + 键名 + 冒号后无其他内容），
// 不匹配注释或行内出现的 "validate-pr-body" 字样
const JOB_KEY_RE = /^\s*validate-pr-body:\s*$/;
// script 步骤名（step 级 name，非 job 级显示名 "Validate PR body"）
const STEP_NAME = 'Validate PR body against template';
// 校验核心与评论包装的分节横幅，核心部分切分点
const WRAP_MARKER = '// ===== 评论包装 =====';

// 定位 script 块所在行号下标；任一锚点缺失即抛错（调用方按提取失败处理，不静默降级）
function locateScriptLine(lines) {
    const jobIdx = lines.findIndex(l => JOB_KEY_RE.test(l));
    if (jobIdx < 0) {
        throw new Error('pr-validation.yml 中未找到 validate-pr-body job 键');
    }
    const stepIdx = lines.findIndex((l, i) => i > jobIdx && l.includes(`name: ${STEP_NAME}`));
    if (stepIdx < 0) {
        throw new Error(`pr-validation.yml 中未找到 script 步骤「${STEP_NAME}」`);
    }
    const scriptIdx = lines.findIndex((l, i) => i > stepIdx && /^\s*script: \|\s*$/.test(l));
    if (scriptIdx < 0) {
        throw new Error(`「${STEP_NAME}」步骤之后未找到 script: | 块`);
    }
    return scriptIdx;
}

// 提取完整 inline JS：收集 script 块内缩进更深的连续行（空行保留），按最小缩进剥壳
function extractInlineScript(ymlSource) {
    const lines = ymlSource.split('\n');
    const scriptIdx = locateScriptLine(lines);
    const scriptIndent = lines[scriptIdx].match(/^ */)[0].length;
    const block = [];
    for (let i = scriptIdx + 1; i < lines.length; i++) {
        const line = lines[i];
        if (line.trim() === '') {
            block.push('');
            continue;
        }
        if (line.match(/^ */)[0].length <= scriptIndent) {
            break;
        }
        block.push(line);
    }
    while (block.length && block[block.length - 1] === '') {
        block.pop();
    }
    if (block.length === 0) {
        throw new Error('script 块内容为空');
    }
    const minIndent = Math.min(
        ...block.filter(l => l.trim() !== '').map(l => l.match(/^ */)[0].length)
    );
    return block.map(l => l.slice(minIndent)).join('\n') + '\n';
}

// 提取校验核心（评论包装分节横幅之前的部分），补 module.exports 行使其可作模块加载。
function extractCoreSource(ymlSource) {
    const script = extractInlineScript(ymlSource);
    const wrapIdx = script.indexOf(WRAP_MARKER);
    if (wrapIdx < 0) {
        throw new Error(`inline JS 中未找到「${WRAP_MARKER}」分节横幅`);
    }
    const corePart = script.slice(0, wrapIdx).replace(/\s+$/, '\n');
    return corePart + 'module.exports = { validatePrBody, stripFences, splitSections, checkboxStats };\n';
}

module.exports = { extractInlineScript, extractCoreSource };

// CLI：默认将完整 inline JS 输出到 stdout；--out <dir> 同时落盘完整脚本与核心模块两个文件
if (require.main === module) {
    const repoRoot = path.resolve(__dirname, '..', '..');
    const ymlPath = path.join(repoRoot, '.github', 'workflows', 'pr-validation.yml');
    const ymlSource = fs.readFileSync(ymlPath, 'utf8');
    const args = process.argv.slice(2);
    const outIdx = args.indexOf('--out');
    if (outIdx >= 0) {
        const outDir = args[outIdx + 1];
        if (!outDir) {
            throw new Error('--out 需要指定输出目录');
        }
        const inline = extractInlineScript(ymlSource);
        fs.mkdirSync(outDir, { recursive: true });
        fs.writeFileSync(path.join(outDir, 'validate-pr-body.inline.js'), inline, 'utf8');
        fs.writeFileSync(path.join(outDir, 'validate-pr-body.core.js'), extractCoreSource(ymlSource), 'utf8');
        console.log(`inline JS ${inline.split('\n').length - 1} 行，已写出：`);
        console.log(`  ${path.join(outDir, 'validate-pr-body.inline.js')}`);
        console.log(`  ${path.join(outDir, 'validate-pr-body.core.js')}`);
    } else {
        process.stdout.write(extractInlineScript(ymlSource));
    }
}
