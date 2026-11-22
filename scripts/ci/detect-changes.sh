#!/usr/bin/env bash
# 改动文件清单 -> 消费者类别判定（纯函数：不读仓库状态，只看传入的路径）。
#
# 供 scripts/ci/changed-files.sh（事件 -> 清单的编排层）调用；deploy.yml 的
# simulate 模拟清单不经编排层，直接传入本脚本。清单来源由编排层负责：
#   pull_request       origin/base 与 HEAD 的 merge-base 三点 diff
#   push               事件载荷 .before 与 GITHUB_SHA 的两点 diff
#   workflow_dispatch  编排层直接用 --all（默认全跑）
#
# 用法：
#   detect-changes.sh        # 从 stdin 读换行分隔的路径清单
#   detect-changes.sh --all  # 兜底模式：全类别判真（>300 文件、新分支、force-push、手动触发）
#
# 输出：stdout 为 GITHUB_OUTPUT 键值对（ui / backend / deploy_backend / infra /
# test_scripts / categories），人读日志走 stderr。以 `bash scripts/ci/detect-changes.sh` 方式调用，
# 不依赖执行位。
# 注：清单按换行分隔；本仓库路径不含换行符，git rename 仅上报目标路径（源路径类别随目标路径）。
set -euo pipefail

if [ "${1:-}" = '--all' ]; then
    echo "ui=true"
    echo "backend=true"
    echo "deploy_backend=true"
    echo "infra=true"
    echo "test_scripts=true"
    echo "categories=default-all"
    echo "detect-changes: no usable changed-file list, defaulting to run everything" >&2
    exit 0
fi

# ---------------------------------------------------------------------------
# 消费者归类映射（每个路径只归一类，case 顺序即判定优先级；改动本表需同步
# 复核各工作流的 job if 条件与 deploy.yml 的分流注释）：
#
# docs      docs/**、**.md、**.mdx
#           文档唯一权威源（官网 docs 站消费），无运行时消费者 -> 不触发检查与部署。
#           *.md 规则先于 continew-ui/*，故 continew-ui 内 README 也归 docs。
# infra     .github/**、scripts/ci/**
#           仅被 CI 工作流自身消费，无运行时消费者 -> 不触发部署、不改变后端检查类别；
#           脚本本身的正确性由 detect 作业失败兜底，无需借后端检查回归。
# ui        continew-ui/**
#           前端产物唯一来源 -> ci.yml 的 UI checks 与部署侧 Build UI dist 的消费者。
# backend   continew-server/**、continew-system/**、continew-common/**、
#           continew-plugin/**、continew-tests/**、pom.xml、*/pom.xml、
#           mvnw、mvnw.cmd、.mvn/**、docker/**、scripts/**（ci 之外）
#           后端构建/测试/运行时/部署链消费者 -> 后端检查 + 后端部署；
#           其中 scripts/test/** 不触达 Java 门禁、不改变服务运行时行为 ->
#           计入后端检查（脚本由后端检查作业真实执行）并产出 test_scripts=true
#           供 Lint workflows 的 bash -n 触发，但不触发部署。
# other     其余根文件（style/、LICENSE、.gitignore 等）
#           消费者不明：保守计入后端检查（不静默跳过 CI），无运行时证据 -> 不触发部署。
# ---------------------------------------------------------------------------

has_ui=false
has_backend=false
has_deploy_backend=false
has_docs=false
has_infra=false
has_test_scripts=false
has_other=false
count=0
while IFS= read -r path; do
    path=${path%$'\r'}
    [ -n "$path" ] || continue
    count=$((count + 1))
    case "$path" in
        .github/* | scripts/ci/*)
            has_infra=true
            ;;
        docs/* | *.md | *.mdx)
            has_docs=true
            ;;
        continew-ui/*)
            has_ui=true
            ;;
        scripts/test/*)
            # 测试脚本由后端检查作业真实执行，保持 backend 归类；但不改变服务运行时
            # 行为，不触发部署
            has_test_scripts=true
            has_backend=true
            ;;
        continew-server/* | continew-system/* | continew-common/* | continew-plugin/* | continew-tests/* | \
            pom.xml | */pom.xml | mvnw | mvnw.cmd | .mvn/* | docker/* | scripts/*)
            has_backend=true
            has_deploy_backend=true
            ;;
        *)
            has_other=true
            ;;
    esac
done

backend=false
if [ "$has_backend" = true ] || [ "$has_other" = true ]; then
    backend=true
fi

categories=()
[ "$has_ui" = true ] && categories+=(ui)
[ "$has_backend" = true ] && categories+=(backend)
[ "$has_docs" = true ] && categories+=(docs)
[ "$has_infra" = true ] && categories+=(infra)
[ "$has_other" = true ] && categories+=(other)
if [ "$count" -eq 0 ]; then
    categories=(empty)
    echo "detect-changes: empty changed-file list, every job will be skipped" >&2
fi

echo "ui=$has_ui"
echo "backend=$backend"
echo "deploy_backend=$has_deploy_backend"
echo "infra=$has_infra"
echo "test_scripts=$has_test_scripts"
echo "categories=$(IFS=,; echo "${categories[*]}")"
echo "detect-changes: files=$count ui=$has_ui backend=$backend deploy_backend=$has_deploy_backend infra=$has_infra test_scripts=$has_test_scripts categories=$(IFS=,; echo "${categories[*]}")" >&2
