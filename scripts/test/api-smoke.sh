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
# 清理目标固定（评审 F4）：环境文件不可改写项目名
readonly PROJECT="continew-admin-test"

to_host_path() {
    if command -v cygpath >/dev/null 2>&1; then
        cygpath -m "$1"
    else
        printf '%s\n' "$1"
    fi
}

# 按 Compose dotenv 语义读键（评审 F4）：环境文件不进 shell 执行，只白名单取值。
# KEY=VALUE 单行、去除两端引号与空白、# 开头为注释；未命中时输出空。
dotenv_get() {
    local key="$1" file="${2:-$PORTS_FILE}" line
    [[ -f "$file" ]] || return 0
    while IFS= read -r line; do
        line="${line%%#*}"
        [[ "$line" == "$key="* ]] || continue
        line="${line#*=}"
        line="${line#"${line%%[![:space:]]*}"}"
        line="${line%"${line##*[![:space:]]}"}"
        line="${line%\"}" && line="${line#\"}"
        line="${line%\'}" && line="${line#\'}"
        printf '%s\n' "$line"
        return 0
    done < "$file"
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
    # 全局选项（--env-file/-f/-p）必须全部位于子命令之前，理由同 up.sh 的 compose()
    docker compose "${env_args[@]}" -f "$(to_host_path "$COMPOSE_FILE")" -p "$PROJECT" "$@"
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

if [[ -z "$BASE_URL" ]]; then
    # 端口读键不做 source（评审 F4）；优先级与文件头注释一致：--base-url > .ports.env > 默认值
    APP_PORT="$(dotenv_get APP_PORT)"
    BASE_URL="http://127.0.0.1:${APP_PORT:-18020}"
fi
BASE_URL="${BASE_URL:-http://127.0.0.1:18020}"
echo "L3 测试目标：$BASE_URL"

# 判定目标是否脚本管理的本地实例（评审 F5）：同时核对「本地容器在跑」与「目标 URL 端口
# 与 up.sh 探测并记录的端口一致」，两个条件都满足才对本地实例执行验证码关闭；
# 指向外部实例时完全跳过本地 Docker 写操作，由外部实例自行保证验证码已关闭。
# 只判容器在跑不判端口会把验证码 UPDATE/DEL 打到与测试无关的本地实例上。
LOCAL_PORT="$(dotenv_get APP_PORT)"
if [[ "$BASE_URL" == "http://127.0.0.1:${LOCAL_PORT}"* && -n "$LOCAL_PORT" ]] \
    && compose --profile mount --profile build ps --format '{{.Service}}' 2>/dev/null | grep -q '^app-test'; then
    echo "关闭登录验证码（sys_option.LOGIN_CAPTCHA_ENABLED）..."
    # 密码经 MYSQL_PWD 环境变量传入（与 Redis 侧 REDISCLI_AUTH 同理），不进 ps/命令行
    compose exec -T mysql-test sh -c "MYSQL_PWD=\"\$MYSQL_ROOT_PASSWORD\" mysql -uroot continew_admin -e \"UPDATE sys_option SET value='0' WHERE code='LOGIN_CAPTCHA_ENABLED'\"" >/dev/null
    compose exec -T redis-test sh -c 'REDISCLI_AUTH="$REDIS_PASSWORD" redis-cli DEL "OPTION:LOGIN_CAPTCHA_ENABLED"' >/dev/null
else
    echo "提示：测试目标非脚本管理的本地实例，跳过验证码关闭（外部实例需自行关闭验证码）"
fi

# 测试侧读取优先级：系统属性 > 环境变量 > 默认值（计划 T1.2），此处经环境变量传入
export API_BASE_URL="$BASE_URL"

cd "$REPO_ROOT"
exec ./mvnw -B -pl :continew-server -am -Papi-test test -Dsurefire.failIfNoSpecifiedTests=false
