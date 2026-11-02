#!/usr/bin/env bash
# 停止并清理 L3 测试环境（含数据卷），仅作用于 project：continew-admin-test
# down 前先打印容器清单，确认不涉及其他 compose project（评审 P0-1：演示环境 18000 不受影响）
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
COMPOSE_FILE="$REPO_ROOT/docker/docker-compose-test.yml"
TEST_DIR="$REPO_ROOT/docker/test"
ENV_FILE="$TEST_DIR/.env"
PORTS_FILE="$TEST_DIR/.ports.env"
PROJECT="continew-admin-test"

to_host_path() {
    if command -v cygpath >/dev/null 2>&1; then
        cygpath -m "$1"
    else
        printf '%s\n' "$1"
    fi
}

# .env 缺失时回退 .env.example（凭据不影响 down 行为），保证从未 up 过也能清理
ENV_SOURCE="$ENV_FILE"
if [[ ! -f "$ENV_SOURCE" ]]; then
    ENV_SOURCE="$TEST_DIR/.env.example"
fi
ENV_ARGS=(--env-file "$(to_host_path "$ENV_SOURCE")")
if [[ -f "$PORTS_FILE" ]]; then
    ENV_ARGS+=(--env-file "$(to_host_path "$PORTS_FILE")")
fi

echo "目标容器清单（project：$PROJECT，仅清理以下内容）："
docker compose "${ENV_ARGS[@]}" -f "$(to_host_path "$COMPOSE_FILE")" -p "$PROJECT" ps || true
docker compose "${ENV_ARGS[@]}" -f "$(to_host_path "$COMPOSE_FILE")" -p "$PROJECT" down -v --remove-orphans
echo "测试环境已停止，数据卷已清理"
