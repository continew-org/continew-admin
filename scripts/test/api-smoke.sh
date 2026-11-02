#!/usr/bin/env bash
# 运行 L3 API 黑盒测试（@Tag("api")），目标为 scripts/test/up.sh 起的独立实例
# 用法：bash scripts/test/api-smoke.sh [--base-url http://127.0.0.1:18020]
# base-url 优先级：--base-url 参数 > docker/test/.ports.env > 默认 18020（计划 4.4）
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

ENV_SOURCE="$ENV_FILE"
if [[ ! -f "$ENV_SOURCE" ]]; then
    ENV_SOURCE="$TEST_DIR/.env.example"
fi
ENV_FILE_DOCKER="$(to_host_path "$ENV_SOURCE")"

# 所有 compose 调用统一 -f 绝对路径 + -p 项目名（与 up.sh 一致）
compose() {
    local env_args=(--env-file "$ENV_FILE_DOCKER")
    if [[ -f "$PORTS_FILE" ]]; then
        env_args+=(--env-file "$(to_host_path "$PORTS_FILE")")
    fi
    docker compose "${env_args[@]}" "$@" -f "$(to_host_path "$COMPOSE_FILE")" -p "$PROJECT"
}

BASE_URL=""
if [[ "${1:-}" == "--base-url" ]]; then
    if [[ $# -lt 2 || -z "${2:-}" ]]; then
        echo "错误：--base-url 需要一个 URL 参数" >&2
        exit 1
    fi
    BASE_URL="$2"
elif [[ -n "${1:-}" ]]; then
    echo "未知参数：$1（支持 --base-url <url>）" >&2
    exit 1
fi

if [[ -z "$BASE_URL" && -f "$PORTS_FILE" ]]; then
    # shellcheck disable=SC1090
    source "$PORTS_FILE"
    BASE_URL="http://127.0.0.1:${APP_PORT:-18020}"
fi
BASE_URL="${BASE_URL:-http://127.0.0.1:18020}"
echo "L3 测试目标：$BASE_URL"

# 本地测试容器在运行时先重跑验证码关闭（--keep 复用场景，计划 T1.2）；
# 指向外部实例时跳过，由实例自行保证验证码已关闭。
# 原理与顺序约束见 scripts/test/up.sh 的 disable_login_captcha，两处保持同步。
if compose ps --format '{{.Service}}' 2>/dev/null | grep -q '^app-test'; then
    echo "关闭登录验证码（sys_option.LOGIN_CAPTCHA_ENABLED）..."
    compose exec -T mysql-test sh -c "mysql -uroot -p\"\$MYSQL_ROOT_PASSWORD\" continew_admin -e \"UPDATE sys_option SET value='0' WHERE code='LOGIN_CAPTCHA_ENABLED'\"" >/dev/null
    compose exec -T redis-test sh -c 'REDISCLI_AUTH="$REDIS_PASSWORD" redis-cli DEL "OPTION:LOGIN_CAPTCHA_ENABLED"' >/dev/null
else
    echo "提示：本地测试容器未运行，跳过验证码关闭（外部实例需自行关闭验证码）"
fi

# 测试侧读取优先级：系统属性 > 环境变量 > 默认值（计划 T1.2），此处经环境变量传入
export API_BASE_URL="$BASE_URL"

cd "$REPO_ROOT"
exec ./mvnw -B -pl :continew-server -am -Papi-test test -Dsurefire.failIfNoSpecifiedTests=false
