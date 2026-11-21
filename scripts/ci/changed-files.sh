#!/usr/bin/env bash
# 按事件形态解析改动文件清单并交给 detect-changes.sh 分类，stdout 输出 GITHUB_OUTPUT
# 键值对，人读日志走 stderr。供 ci.yml / deploy.yml 的 "Detect changed paths"
# 步骤共用；deploy.yml 的 simulate 清单分支留在工作流内（含用户可见的歧义输入校验）。
#
# 事件 -> 清单：
#   pull_request       基线取 origin/$GITHUB_BASE_REF 与 HEAD 的 merge-base。
#                      不用事件 payload 的 base.sha：它是事件产生时的 base tip，
#                      而 checkout 检出的 refs/pull/N/merge 已按当前 base tip 重新
#                      生成父提交，两者不同步时三点 diff 会展开成 base 期间合入的
#                      全部提交（实测 4 文件 PR 被判 523 文件）。merge-base 即当前
#                      base tip，diff 结果为「合并进 base 后的净变更」；
#                      PR 与 base 改动同一文件、或作者变基后删除 base 新增文件，
#                      也都如实计入。
#   push               git diff --name-only "$before" "$GITHUB_SHA"，before 取自
#                      事件载荷 GITHUB_EVENT_PATH（GitHub 未提供对应环境变量）；
#                      缺失/全零/不可达时改用 --all（默认全跑）
#   其余（dispatch）   直接 --all
#
# 兜底原则：任何一步取清单失败一律降级 --all 并在 stderr 说明原因，脚本自身
# 不以 git 错误退出。detect 作业失败会让下游必需检查变 skipped，而 GitHub 把
# skipped 视为通过——宁可多跑，不可假绿。
#
# 以 `bash scripts/ci/changed-files.sh` 方式调用（工作目录为仓库根），不依赖执行位。
set -uo pipefail

SELF_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
CLASSIFIER="$SELF_DIR/detect-changes.sh"

fallback_all() {
    echo "changed-files: $1，降级为全类别放行" >&2
    bash "$CLASSIFIER" --all
}

if [ ! -f "$CLASSIFIER" ]; then
    fallback_all "分类器 $CLASSIFIER 不存在"
    exit 0
fi

event=${GITHUB_EVENT_NAME:-}
if [ "$event" = 'pull_request' ]; then
    if [ -z "${GITHUB_BASE_REF:-}" ]; then
        fallback_all 'pull_request 事件缺少 GITHUB_BASE_REF'
        exit 0
    fi
    base_ref="origin/${GITHUB_BASE_REF}"
    if ! git rev-parse --verify --quiet "${base_ref}^{commit}" >/dev/null 2>&1; then
        fallback_all "无法解析 ${base_ref}（checkout 未取到 base 分支引用）"
        exit 0
    fi
    if ! merge_base=$(git merge-base "$base_ref" HEAD 2>/dev/null); then
        fallback_all "merge-base(${base_ref}, HEAD) 失败"
        exit 0
    fi
    echo "changed-files: base=${base_ref}@${merge_base} head=$(git rev-parse --short HEAD)" >&2
    if ! git diff --name-only "${merge_base}...HEAD" | bash "$CLASSIFIER"; then
        fallback_all 'diff 或分类失败'
    fi
    exit 0
fi

if [ "$event" = 'push' ]; then
    # before 仅存在于事件载荷（GitHub 未提供对应环境变量），从 GITHUB_EVENT_PATH 解析；
    # 解析不出时按缺失处理，走下方 --all 兜底
    before=$(jq -r '.before // empty' "${GITHUB_EVENT_PATH:-/dev/null}" 2>/dev/null || true)
    if [ -n "$before" ] \
        && [ "$before" != '0000000000000000000000000000000000000000' ] \
        && git cat-file -e "${before}^{commit}" 2>/dev/null; then
        if git diff --name-only "$before" "${GITHUB_SHA}" | bash "$CLASSIFIER"; then
            exit 0
        fi
        fallback_all 'diff 或分类失败'
        exit 0
    fi
    fallback_all 'push 的 before 缺失/全零/不可达（新分支、force-push）'
    exit 0
fi

fallback_all "事件 ${event:-未知} 无 diff 基线"
exit 0
