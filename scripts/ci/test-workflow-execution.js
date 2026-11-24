'use strict';

const assert = require('node:assert/strict');
const { spawnSync } = require('node:child_process');
const fs = require('node:fs');
const os = require('node:os');
const path = require('node:path');
const test = require('node:test');

const repoRoot = path.resolve(__dirname, '..', '..');
const { constructor: AsyncFunction } = Object.getPrototypeOf(async function () { return; });

function readWorkflow(name) {
    return fs.readFileSync(path.join(repoRoot, '.github', 'workflows', `${name}.yml`), 'utf8');
}

// 只提取具名步骤的字面量块，不在 fixture 里复制生产脚本。
function stepBlock(workflow, stepName, key) {
    const lines = workflow.split(/\r?\n/);
    const start = lines.findIndex(line => line.trim() === `- name: ${stepName}`);
    assert.notEqual(start, -1, `缺少工作流步骤：${stepName}`);
    const indent = line => line.match(/^ */)[0].length;
    const end = lines.findIndex((line, index) => index > start &&
        line.trim() !== '' && indent(line) <= indent(lines[start]));
    const step = lines.slice(start + 1, end === -1 ? lines.length : end);
    const block = step.findIndex(line => line.trim() === `${key}: |`);
    assert.notEqual(block, -1, `步骤 ${stepName} 缺少 ${key}: |`);
    const body = [];
    for (const line of step.slice(block + 1)) {
        if (line.trim() !== '' && indent(line) <= indent(step[block])) {
            break;
        }
        body.push(line);
    }
    const content = body.filter(line => line.trim() !== '');
    assert.ok(content.length, `步骤 ${stepName} 的 ${key} 为空`);
    const base = Math.min(...content.map(indent));
    return body.map(line => line.slice(base)).join('\n');
}

const repo = { owner: 'continew-org', repo: 'continew-admin' };
const bot = { login: 'github-actions[bot]', type: 'Bot' };
const greetingMarker = '<!-- continew-admin:greeting -->';
const greet = new AsyncFunction('github', 'context',
    stepBlock(readWorkflow('greeting'), 'Greet first-time contributor', 'script'));

function greetingFixture({
    event = 'pull_request', association = 'FIRST_TIME_CONTRIBUTOR',
    author = { login: 'example-author', type: 'User' },
    history = [[{ number: 42 }]], comments = [], apiError = '',
} = {}) {
    const item = { number: 42, user: author, author_association: association };
    const context = { repo, payload: { [event]: item }, actor: 'different-rerun-actor' };
    const calls = [];
    const posts = [];
    const listForRepo = Symbol('issues.listForRepo');
    const listComments = Symbol('issues.listComments');
    const github = {
        rest: {
            issues: {
                listForRepo,
                listComments,
                async createComment(params) {
                    posts.push(params);
                    comments.push({ user: bot, body: params.body });
                },
            },
        },
        paginate: {
            async *iterator(method, params) {
                if (method === listForRepo) {
                    calls.push('history');
                    assert.deepEqual(params, {
                        ...repo, creator: item.user.login, state: 'all', sort: 'created',
                        direction: 'asc', per_page: 100,
                    });
                    if (apiError === 'history') {
                        throw new Error('history API failed');
                    }
                    for (const page of history) {
                        yield { data: page };
                    }
                } else {
                    calls.push('comments');
                    assert.equal(method, listComments);
                    assert.deepEqual(params, { ...repo, issue_number: 42, per_page: 100 });
                    if (apiError === 'comments') {
                        throw new Error('comments API failed');
                    }
                    yield { data: comments };
                }
            },
        },
    };
    return { run: () => greet(github, context), calls, posts };
}

for (const association of ['OWNER', 'MEMBER', 'COLLABORATOR', 'UNKNOWN', 'MANNEQUIN']) {
    test(`Greeting 不查询或欢迎 ${association}`, async () => {
        const fixture = greetingFixture({ association });
        await fixture.run();
        assert.deepEqual(fixture.calls, []);
        assert.deepEqual(fixture.posts, []);
    });
}

test('Greeting 不查询或欢迎机器人', async () => {
    const fixture = greetingFixture({ author: bot });
    await fixture.run();
    assert.deepEqual(fixture.calls, []);
    assert.deepEqual(fixture.posts, []);
});

for (const [label, fields] of [
    ['缺失', {}],
    ['空字符串', { login: '' }],
    ['纯空白', { login: ' ' }],
    ['非字符串', { login: 42 }],
]) {
    test(`Greeting 不查询或欢迎 login 为${label}的 NONE 作者`, async () => {
        const fixture = greetingFixture({ association: 'NONE', author: { type: 'User', ...fields } });
        await fixture.run();
        assert.deepEqual(fixture.calls, []);
        assert.deepEqual(fixture.posts, []);
    });
}

for (const association of ['CONTRIBUTOR', 'FIRST_TIMER', 'NONE']) {
    test(`Greeting 欢迎首次投稿的 ${association} 用户`, async () => {
        const fixture = greetingFixture({ event: 'issue', association });
        await fixture.run();
        assert.deepEqual(fixture.calls, ['history', 'comments']);
        assert.equal(fixture.posts.length, 1);
        assert.ok(fixture.posts[0].body.includes('@example-author'));
    });
}

for (const [label, prior] of [
    ['已关闭的 PR', { number: 41, state: 'closed', pull_request: {} }],
    ['已提交的 Issue', { number: 41, state: 'open' }],
]) {
    test(`Greeting 有${label}的作者不再欢迎`, async () => {
        const fixture = greetingFixture({ history: [[{ number: 42 }], [prior]] });
        await fixture.run();
        assert.deepEqual(fixture.calls, ['history']);
        assert.deepEqual(fixture.posts, []);
    });
}

for (const [event, prefix] of [
    ['issue', '🙏 感谢 @example-author 向 ContiNew Admin 提交第一个 Issue！'],
    ['pull_request', '🎉 恭喜 @example-author，这是您对 ContiNew Admin 仓库的首次贡献！'],
]) {
    test(`Greeting ${event} 首次欢迎一次，忽略伪造标记且重跑不重复`, async () => {
        const fixture = greetingFixture({
            event,
            history: [[{ number: 42 }, { number: 43 }]],
            comments: [{ user: { login: 'example-author', type: 'User' }, body: greetingMarker }],
        });
        await fixture.run();
        assert.equal(fixture.posts.length, 1);
        const [comment] = fixture.posts;
        assert.deepEqual({ owner: comment.owner, repo: comment.repo, issue_number: comment.issue_number },
            { ...repo, issue_number: 42 });
        assert.ok(comment.body.startsWith(prefix));
        assert.ok(comment.body.endsWith(greetingMarker));
        await fixture.run();
        assert.equal(fixture.posts.length, 1);
    });
}

test('Greeting 识别旧 Bot 的无标记欢迎原文', async () => {
    const fixture = greetingFixture({
        comments: [{
            user: bot,
            body: '🎉 恭喜 @example-author，这是您对 ContiNew Admin 仓库的首次贡献！我们衷心感谢您付出的努力。\n\n旧评论正文',
        }],
    });
    await fixture.run();
    assert.deepEqual(fixture.posts, []);
});

for (const apiError of ['history', 'comments']) {
    test(`Greeting ${apiError} API 失败时不发帖`, async () => {
        const fixture = greetingFixture({ apiError });
        await assert.rejects(fixture.run, new RegExp(`${apiError} API failed`));
        assert.deepEqual(fixture.posts, []);
    });
}

function runBash(script, env) {
    const result = spawnSync('bash', ['--noprofile', '--norc', '-s'], {
        cwd: repoRoot,
        env: { ...process.env, BASH_ENV: '', ...env },
        input: `${script}\n`,
        encoding: 'utf8',
        timeout: 10000,
    });
    assert.ifError(result.error);
    return result;
}

const ocr = readWorkflow('ocr-review');
const diagnose = stepBlock(ocr, 'Diagnose LLM endpoint', 'run');
const captureCurl = String.raw`
curl() {
    while [ "$#" -gt 1 ]; do
        if [ "$1" = '-d' ]; then
            printf 'request:%s\n' "$2" >&2
            printf '200'
            return
        fi
        shift
    done
    return 1
}
head() { printf '{}'; }
`;

function prepareOcrProtocol(value) {
    const prepare = stepBlock(ocr, 'Resolve OCR protocol', 'run');
    const output = `${temporaryDirectory('continew-ocr-config-')}/output`;
    const result = runBash(prepare, { OCR_LLM_USE_ANTHROPIC: value, GITHUB_OUTPUT: output });
    assert.equal(result.status, 0, result.stderr);
    const contents = fs.readFileSync(output, 'utf8').trim();
    const protocol = contents.match(/^use_anthropic=(true|false)$/);
    assert.ok(protocol, contents);
    return protocol[1];
}

test('OCR 评审与诊断使用同一协议，缺省为 OpenAI 且不注入默认 extra body', () => {
    assert.ok(ocr.includes("OCR_LLM_USE_ANTHROPIC: ${{ secrets.OCR_LLM_USE_ANTHROPIC || vars.OCR_LLM_USE_ANTHROPIC || 'false' }}"));
    assert.ok(ocr.includes('OCR_REASONING_EFFORT: ${{ vars.OCR_REASONING_EFFORT }}'));
    assert.ok(ocr.includes('llm_use_anthropic: ${{ steps.ocr-config.outputs.use_anthropic }}'));
    assert.ok(ocr.includes('DIAG_USE_ANTHROPIC: ${{ steps.ocr-config.outputs.use_anthropic }}'));
    assert.match(ocr, /^\s*llm_extra_body: '\{\}'$/m);
});

for (const [label, configured, effort, expectedProtocol, expectedEffort] of [
    ['Anthropic true', 'true', 'low', 'true', ''],
    ['Anthropic YeS', 'YeS', 'low', 'true', ''],
    ['Anthropic 1', '1', 'low', 'true', ''],
    ['OpenAI false', 'false', 'low', 'false', 'low'],
    ['OpenAI 未配置 effort', 'false', '', 'false', ''],
]) {
    test(`OCR ${label}的 action 实际 effort`, () => {
        // 只求值实际 with 中的布尔门控；Bash 步骤负责协议归一化。
        const input = ocr.match(/^\s+llm_reasoning_effort: \$\{\{ steps\.ocr-config\.outputs\.use_anthropic == '(true|false)' && env\.(\w+) \|\| '([^']*)' \}\}$/m);
        assert.ok(input, 'llm_reasoning_effort 必须由实际配置步骤的协议输出门控');
        const [, openAiValue, effortVar, fallback] = input;
        const protocol = prepareOcrProtocol(configured);
        assert.equal(protocol, expectedProtocol);
        const env = { OCR_REASONING_EFFORT: effort };
        const actualEffort = (protocol === openAiValue && env[effortVar]) || fallback;
        assert.equal(actualEffort, expectedEffort);
    });
}

for (const [label, effort, anthropic, expectedEfforts] of [
    ['未配置档位', '', 'false', [undefined]],
    ['合法显式档位', 'HiGh', 'false', [undefined, 'high']],
    ['非法档位', 'xhigh', 'false', [undefined]],
    ['Anthropic 协议', 'low', 'YeS', []],
]) {
    test(`OCR ${label}的探针请求字段`, () => {
        const result = runBash(`${captureCurl}\n${diagnose}`, {
            DIAG_USE_ANTHROPIC: prepareOcrProtocol(anthropic),
            OCR_REASONING_EFFORT: effort,
            DIAG_URL: 'https://example.invalid/api',
            DIAG_TOKEN: 'fixture',
            DIAG_MODEL: 'fixture',
        });
        assert.equal(result.status, 0, result.stderr);
        const bodies = result.stderr.split(/\r?\n/).filter(line => line.startsWith('request:'))
            .map(line => JSON.parse(line.slice('request:'.length)));
        assert.deepEqual(bodies.map(body => body.reasoning_effort), expectedEfforts);
        for (const body of bodies) {
            assert.equal(Object.hasOwn(body, 'thinking'), false);
        }
    });
}

const ci = readWorkflow('ci');
const installBrowser = stepBlock(ci, 'Install Playwright chromium', 'run');
// 运行真实 shell 和 Node 探针，仅替换 Playwright CLI 和浏览器。
const playwrightFixture = String.raw`
probe_stage=first
pnpm() {
    printf 'pnpm:%s\n' "$*" >&2
    case "$*" in
        'exec playwright install --only-shell chromium')
            if [ "$DOWNLOAD_FAILS" = 'yes' ]; then
                printf '%s\n' 'browser download failed' >&2
                return 7
            fi ;;
        'exec playwright install-deps chromium')
            if [ "$APT_FAILS" = 'yes' ]; then
                printf '%s\n' 'apt failed' >&2
                return 8
            fi
            probe_stage=second ;;
        *) return 99 ;;
    esac
}
node() {
    PROBE_STAGE="$probe_stage" command node -e '
        const assert = require("node:assert/strict");
        const source = require("node:fs").readFileSync(0, "utf8");
        const stage = process.env.PROBE_STAGE;
        const load = name => {
            assert.equal(name, "@playwright/test");
            return { chromium: { async launch(options) {
                assert.equal(options.headless, true);
                console.error("launch:" + stage);
                const message = stage === "first" ? process.env.FIRST_ERROR : process.env.SECOND_ERROR;
                if (message) {
                    throw new Error(message);
                }
                return { async close() { console.error("close:" + stage); } };
            } } };
        };
        new Function("require", "process", source)(load, process);
    '
}
`;

const download = 'pnpm:exec playwright install --only-shell chromium';
const installDeps = 'pnpm:exec playwright install-deps chromium';
const missingLibraries = 'Host system is missing dependencies to run browsers.';
const loaderError = 'error while loading shared libraries: libnss3.so: cannot open shared object file';

for (const { label, first = '', second = '', downloadFails = 'no', aptFails = 'no', status = 0,
    sequence, messages = [] } of [
    { label: '宿主有依赖时只下载和启动', sequence: [download, 'launch:first', 'close:first'] },
    { label: 'Playwright 确认缺库时安装再探测', first: missingLibraries,
        sequence: [download, 'launch:first', installDeps, 'launch:second', 'close:second'],
        messages: [missingLibraries] },
    { label: '动态加载器缺库后二次失败仍报错', first: loaderError, second: 'browser still fails', status: 1,
        sequence: [download, 'launch:first', installDeps, 'launch:second'],
        messages: [loaderError, 'browser still fails'] },
    { label: '非缺库的启动错误不调用 apt', first: 'browser launch timed out', status: 1,
        sequence: [download, 'launch:first'], messages: ['browser launch timed out'] },
    { label: '下载失败不启动探针和 apt', downloadFails: 'yes', status: 7,
        sequence: [download], messages: ['browser download failed'] },
    { label: 'apt 失败不继续探测', first: missingLibraries, aptFails: 'yes', status: 8,
        sequence: [download, 'launch:first', installDeps], messages: [missingLibraries, 'apt failed'] },
]) {
    test(`E2E Chromium ${label}`, () => {
        const result = runBash(`${playwrightFixture}\n${installBrowser}`, {
            FIRST_ERROR: first, SECOND_ERROR: second, DOWNLOAD_FAILS: downloadFails, APT_FAILS: aptFails,
        });
        assert.equal(result.status, status, `${result.stdout}\n${result.stderr}`);
        assert.deepEqual(result.stderr.split(/\r?\n/).filter(line => /^(pnpm|launch|close):/.test(line)), sequence);
        for (const message of messages) {
            assert.ok(result.stderr.includes(message), result.stderr);
        }
    });
}

const deploy = readWorkflow('deploy');
const detect = stepBlock(deploy, 'Detect changed paths', 'run');
// 不依赖 checkout 深度或 jq 安装；实际执行 changed-files.sh 和分类器，只替换 push 的基线和 diff。
const pushFixture = String.raw`
jq() { printf '%s\n' fixture-before; }
git() {
    case "$1" in
        cat-file) return 0 ;;
        diff) printf '%s\n' 'continew-ui/src/App.tsx' ;;
        *) return 1 ;;
    esac
}
export -f jq git
`;

const deployCases = [
    { name: '非演练 auto 不接受清单', simulate: 'false', paths: 'pom.xml',
        error: /simulate_paths.*simulate=true/ },
    { name: '演练按空格和换行分类', simulate: 'true', paths: 'docs/index.md continew-ui/src/App.tsx\npom.xml',
        expected: ['true', 'true', 'ui,backend,docs'] },
    { name: '演练的显式目标不接受清单', simulate: 'true', target: 'ui', paths: 'pom.xml',
        error: /deploy_target 与 simulate_paths 不能同时使用/ },
    { name: '正常手动显式目标不读取 diff', simulate: 'false', target: 'ui',
        expected: ['true', 'false', 'manual-ui'] },
    { name: '正常 push 无清单时读取 diff', event: 'push',
        expected: ['true', 'false', 'ui'] },
    { name: '正常手动 auto 无 diff 基线时仍全量', simulate: 'false',
        expected: ['true', 'true', 'default-all'] },
];

for (const { name, simulate = '', target = '', paths = '', event = 'workflow_dispatch',
    error = null, expected = [] } of deployCases) {
    test(`Deploy ${name}`, () => {
        const directory = fs.mkdtempSync(path.join(os.tmpdir(), 'continew-routing-'));
        const output = path.join(directory, 'github-output.txt');
        const result = runBash(`${event === 'push' ? pushFixture : ''}\n${detect}`, {
            SIMULATE: simulate,
            SIMULATE_PATHS: paths,
            DEPLOY_TARGET: target,
            GITHUB_EVENT_NAME: event,
            GITHUB_EVENT_PATH: '',
            GITHUB_SHA: 'fixture-head',
            GITHUB_OUTPUT: output.replaceAll('\\', '/'),
        });
        if (error) {
            assert.equal(result.status, 1, `${result.stdout}\n${result.stderr}`);
            assert.match(result.stdout, error);
            assert.equal(fs.existsSync(output), false);
            return;
        }
        assert.equal(result.status, 0, result.stderr);
        const outputs = Object.fromEntries(fs.readFileSync(output, 'utf8').trim().split(/\r?\n/)
            .map(line => line.split('=')));
        assert.deepEqual([outputs.ui, outputs.deploy_backend, outputs.categories], expected);
    });
}

function jobBlock(workflow, id) {
    const lines = workflow.split(/\r?\n/);
    const start = lines.indexOf(`  ${id}:`);
    assert.notEqual(start, -1, `缺少工作流作业：${id}`);
    const end = lines.findIndex((line, index) => index > start && /^ {2}[\w-]+:$/.test(line));
    return lines.slice(start + 1, end === -1 ? lines.length : end).join('\n');
}

const packageUi = jobBlock(deploy, 'package-ui');
const deployUi = jobBlock(deploy, 'build-ui');
const prepareUi = stepBlock(deployUi, 'Prepare artifact directory', 'run');
const publishUi = stepBlock(deployUi, 'Publish UI dist', 'run');
const verifyUi = stepBlock(deployUi, 'Verify published site', 'run');

test('Deploy UI 托管构建固定事件 SHA、演示变量和锁文件', () => {
    assert.match(packageUi, /^    runs-on: ubuntu-24\.04$/m);
    assert.match(packageUi, /^          ref: \$\{\{ github\.sha \}\}$/m);
    assert.match(packageUi, /^          persist-credentials: false$/m);
    assert.match(packageUi, /^          VITE_BAIDU_ANALYTICS_ID: \$\{\{ secrets\.BAIDU_ANALYTICS_ID \}\}$/m);
    assert.match(packageUi, /corepack install -g [\s\S]*- name: Set up Node 22/);
    assert.match(packageUi, /^          node-version: 22$/m);
    assert.match(packageUi, /^          cache-dependency-path: continew-ui\/pnpm-lock\.yaml$/m);
    assert.match(packageUi, /^        run: pnpm install --frozen-lockfile$/m);
    assert.match(packageUi, /^        run: pnpm build$/m);
    assert.match(packageUi, /^      artifact_id: \$\{\{ steps\.upload-dist\.outputs\.artifact-id \}\}$/m);
    assert.match(packageUi, /^          name: continew-ui-dist-\$\{\{ github\.run_attempt \}\}$/m);
});

test('Deploy UI 仅在通过原有守卫且构建成功时下载当前 run 的精确产物', () => {
    for (const job of [packageUi, deployUi]) {
        assert.match(job, /github\.repository == 'continew-org\/continew-admin'/);
        assert.match(job, /&& inputs\.simulate != true/);
        assert.match(job, /&& needs\.detect\.outputs\.ui == 'true'/);
    }
    assert.match(deployUi, /^    name: Deploy UI$/m);
    assert.match(deployUi, /^    runs-on: self-hosted$/m);
    assert.match(deployUi, /^    needs: \[detect, package-ui\]$/m);
    assert.match(deployUi, /&& needs\.package-ui\.result == 'success'/);
    assert.match(deployUi, /^          artifact-ids: \$\{\{ needs\.package-ui\.outputs\.artifact_id \}\}$/m);
    assert.match(deployUi, /^          path: \$\{\{ steps\.ui-dist\.outputs\.path \}\}$/m);
    assert.match(deployUi, /^          digest-mismatch: error$/m);
    assert.doesNotMatch(deployUi, /^          (?:github-token|run-id|repository|pattern|name):|actions\/checkout|pnpm install/m);
    assert.match(deployUi, /- name: Download exact UI artifact[\s\S]*- name: Publish UI dist[\s\S]*- name: Verify published site/);
});

function temporaryDirectory(prefix) {
    return fs.mkdtempSync(path.join(os.tmpdir(), prefix)).replaceAll('\\', '/');
}

test('Deploy UI 拒绝空或非单个 ID，不创建产物目录', () => {
    const directory = temporaryDirectory('continew-download-');
    const output = `${directory}/output`;
    for (const id of ['', '123,456', '0']) {
        const result = runBash(`set -e\n${prepareUi}`, { ARTIFACT_ID: id, GITHUB_OUTPUT: output, RUNNER_TEMP: directory });
        assert.equal(result.status, 1, result.stderr);
        assert.match(result.stdout, /未返回有效的 UI artifact ID/);
        assert.deepEqual(fs.readdirSync(directory), []);
    }
});

test('Deploy UI 为每次下载创建私有临时父目录和可发布的 dist 子目录', () => {
    const directory = temporaryDirectory('continew-download-');
    const isWindows = process.platform === 'win32';
    // Git Bash 无法在限制性掩码下补设 POSIX 权限。
    const umask = isWindows ? '022' : '077';
    const paths = ['out-a', 'out-b'].map(file => {
        const output = `${directory}/${file}`;
        const result = runBash(`set -e\numask ${umask}\n${prepareUi}`, {
            ARTIFACT_ID: '1234', GITHUB_OUTPUT: output, RUNNER_TEMP: directory,
            GITHUB_RUN_ID: '452', GITHUB_RUN_ATTEMPT: '2',
        });
        assert.equal(result.status, 0, result.stderr);
        const value = fs.readFileSync(output, 'utf8').trim();
        assert.ok(value.startsWith(`path=${directory}/continew-ui-dist-452-2.`), value);
        const artifactDir = value.slice('path='.length);
        assert.equal(path.basename(artifactDir), 'dist');
        const parent = path.dirname(artifactDir);
        assert.equal(path.dirname(parent), directory);
        const parentStat = fs.statSync(parent);
        const artifactStat = fs.statSync(artifactDir);
        assert.ok(parentStat.isDirectory());
        assert.ok(artifactStat.isDirectory());
        if (isWindows) {
            // Windows 的 stat 不反映 POSIX 权限，只检查实际运行的目录创建命令。
            assert.match(prepareUi, /^artifact_dir=\$\(mktemp -d -- /m);
            assert.match(prepareUi, /^mkdir -m 0755 -- "\$artifact_dir\/dist"$/m);
        } else {
            assert.equal(parentStat.mode & 0o777, 0o700);
            assert.equal(artifactStat.mode & 0o777, 0o755);
        }
        return parent;
    });
    assert.notEqual(paths[0], paths[1]);
});

const publishFixture = String.raw`
rsync() { printf '<%s>' "$@"; printf '\n'; }
date() { printf '20261124T000000Z\n'; }
du() { printf '4K\n'; }
rm() { printf 'unexpected rm call\n' >&2; return 99; }
`;

test('Deploy UI 从下载路径发布，严格保持快照、资源、入口、残留顺序', () => {
    const directory = temporaryDirectory('continew-publish-');
    const source = `${directory}/download dist`;
    const server = `${directory}/server path`;
    const web = `${server}/continew-admin/web`;
    const snapshot = `${server}/backups/web/web-20261124T000000Z`;
    fs.mkdirSync(`${source}/assets`, { recursive: true });
    fs.mkdirSync(web, { recursive: true });
    const env = { UI_DIST: source, SERVER_PATH: server, GITHUB_STEP_SUMMARY: `${directory}/summary` };
    const incomplete = runBash(`${publishFixture}\n${publishUi}`, env);
    assert.equal(incomplete.status, 1, incomplete.stderr);
    assert.match(incomplete.stdout, /UI artifact 缺少 index\.html 或 assets\//);
    assert.equal(fs.existsSync(`${server}/backups`), false);

    fs.writeFileSync(`${source}/index.html`, '<div id="root"></div>');
    const result = runBash(`${publishFixture}\n${publishUi}`, env);
    assert.equal(result.status, 0, result.stderr);
    assert.deepEqual(result.stdout.trim().split(/\r?\n/), [
        `<-a><--delete><${web}/><${snapshot}/>`,
        `<-a><--exclude></index.html><${source}/><${web}/>`,
        `<-a><${source}/index.html><${web}/index.html>`,
        `<-a><--delete><${source}/><${web}/>`,
    ]);
});

test('Deploy UI 的站点自检仍因入口引用缺失而失败', () => {
    const server = temporaryDirectory('continew-verify-');
    const web = `${server}/continew-admin/web`;
    fs.mkdirSync(`${web}/assets`, { recursive: true });
    fs.writeFileSync(`${web}/index.html`, '<div id="root"></div><script src="/assets/main.js"></script>');
    const env = { SERVER_PATH: server, GITHUB_STEP_SUMMARY: `${server}/summary` };
    const missing = runBash(verifyUi, env);
    assert.equal(missing.status, 1, missing.stderr);
    assert.match(missing.stdout, /index\.html 引用的资源缺失：\/assets\/main\.js/);
    fs.writeFileSync(`${web}/assets/main.js`, 'console.log("fixture");');
    const complete = runBash(verifyUi, env);
    assert.equal(complete.status, 0, complete.stderr);
    assert.match(fs.readFileSync(env.GITHUB_STEP_SUMMARY, 'utf8'), /index\.html bytes=\d+ assets=1/);
});

const checkOcrFiles = stepBlock(ocr, 'Check OCR diagnostic files', 'run');
const ocrFilesFixture = String.raw`
test() {
    if [[ "$#" -ne 2 || "$1" != '-f' ]]; then
        return 99
    fi
    [[ "$2" == "/tmp/$OCR_FIXTURE_FILE" ]]
}
`;

test('OCR 只归档实际产出的诊断文件，成功、失败和取消路径均允许归档', () => {
    assert.match(ocr, /- name: Run OpenCodeReview\r?\n\s+id: ocr\r?\n\s+uses: alibaba\/open-code-review@375f64cd6f67a48a135da83cfcf59c52669a09cb/);
    assert.match(ocr, /^          upload_artifacts: 'false'$/m);
    assert.match(ocr, /if: always\(\) && steps\.ocr\.outcome != '' && steps\.ocr\.outcome != 'skipped'/);
    assert.match(ocr, /if: always\(\) && steps\.ocr-diagnostics\.outputs\.present == 'true'/);
    assert.match(ocr, /^          name: ocr-review-result-\$\{\{ github\.run_id \}\}-\$\{\{ github\.run_attempt \}\}$/m);
    assert.equal(stepBlock(ocr, 'Upload review artifacts', 'path').trim(), '/tmp/ocr-result.json\n/tmp/ocr-stderr.log');
    assert.doesNotMatch(ocr, /^\s+retention-days:/m);
});

for (const file of ['', 'ocr-result.json', 'ocr-stderr.log']) {
    test(`OCR 归档门控：${file || '没有诊断文件'}`, () => {
        const output = `${temporaryDirectory('continew-ocr-artifact-')}/output`;
        const result = runBash(`${ocrFilesFixture}\n${checkOcrFiles}`, { OCR_FIXTURE_FILE: file, GITHUB_OUTPUT: output });
        assert.equal(result.status, 0, result.stderr);
        if (file) {
            assert.equal(fs.readFileSync(output, 'utf8').trim(), 'present=true');
        } else {
            assert.equal(fs.existsSync(output), false);
        }
    });
}

test('本地工作流 artifact 引用均固定为 Node 24 版本，回归接入 Lint workflows', () => {
    const pins = {
        upload: 'cf430e030ddbb5b0abf93d22962f4752f3646cd9',
        download: '9000827ccba6bdab643e8b6fd33ac0654aef8333',
    };
    const workflowsDir = path.join(repoRoot, '.github', 'workflows');
    const actions = fs.readdirSync(workflowsDir).filter(file => /\.ya?ml$/.test(file))
        .flatMap(file => [...fs.readFileSync(path.join(workflowsDir, file), 'utf8')
            .matchAll(/^\s*uses: actions\/(upload|download)-artifact@(\S+)/gm)]);
    assert.ok(actions.some(([, kind]) => kind === 'upload'));
    assert.ok(actions.some(([, kind]) => kind === 'download'));
    for (const [, kind, pin] of actions) {
        assert.equal(pin, pins[kind]);
    }
    assert.match(jobBlock(ci, 'lint-workflows'), /^        run: node scripts\/ci\/test-workflow-execution\.js$/m);
});
