#!/usr/bin/env bash
# L3 测试环境一键启动：MySQL + Redis + 应用独立实例，与演示/生产环境完全隔离
# 用法：bash scripts/test/up.sh [--build | --keep]
#   默认   ：宿主 ./mvnw 构建瘦包，挂载 continew-server/target/app 启动（快）
#   --build：compose 多阶段构建自包含镜像（无需宿主 Maven，首次较慢）
#   --keep ：复用已有健康容器（仅本地人工调试，不参与验收与 CI）
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
COMPOSE_FILE="$REPO_ROOT/docker/docker-compose-test.yml"
TEST_DIR="$REPO_ROOT/docker/test"
ENV_FILE="$TEST_DIR/.env"
PORTS_FILE="$TEST_DIR/.ports.env"
PROJECT="continew-admin-test"

MODE="mount"
case "${1:-}" in
    --build) MODE="build" ;;
    --keep) MODE="keep" ;;
    "") ;;
    *) echo "未知参数：$1（支持 --build / --keep）" >&2; exit 1 ;;
esac

# 传给 docker CLI 的文件路径在 Git Bash 下转成 Windows 形式（Linux 无 cygpath 时原样返回）
to_host_path() {
    if command -v cygpath >/dev/null 2>&1; then
        cygpath -m "$1"
    else
        printf '%s\n' "$1"
    fi
}

# 凭据来源：.env 缺失时回退 .env.example（不影响 down 等不依赖凭据的操作）
ENV_SOURCE="$ENV_FILE"
if [[ ! -f "$ENV_SOURCE" ]]; then
    ENV_SOURCE="$TEST_DIR/.env.example"
fi
ENV_FILE_DOCKER="$(to_host_path "$ENV_SOURCE")"

# 所有 compose 调用统一 -f 绝对路径 + -p 项目名，禁止裸命令（评审 P0-1：防误伤同机其他 project）
compose() {
    local env_args=(--env-file "$ENV_FILE_DOCKER")
    # .ports.env 由本脚本探测生成；从未 up 过时允许缺失
    if [[ -f "$PORTS_FILE" ]]; then
        env_args+=(--env-file "$(to_host_path "$PORTS_FILE")")
    fi
    # 全局选项（--env-file/-f/-p）必须全部位于子命令之前："$@" 里混有子命令（ps/up/exec），
    # 若放在 -f/-p 之前，会展开成 "docker compose ... ps -f X"，子命令后的 -f 直接报错
    docker compose "${env_args[@]}" -f "$(to_host_path "$COMPOSE_FILE")" -p "$PROJECT" "$@"
}

# 端口占用探测：宿主侧 TCP 连接测试（Git Bash 与 Linux 的 bash 均支持 /dev/tcp）
port_in_use() {
    (exec 3<>"/dev/tcp/127.0.0.1/$1") 2>/dev/null
}

# 从起点逐个 +1 探测空闲端口；被占端口跳过继续探测下一个，连续 10 个候选全被占才报错
# （评审 R2：16380 已被演示 Redis 占用，遇占即停会误判；报错时打印已占用端口段）
detect_free_port() {
    local base="$1" label="$2"
    local occupied="" candidate i
    for i in $(seq 0 9); do
        candidate=$((base + i))
        if port_in_use "$candidate"; then
            occupied="${occupied:+$occupied, }$candidate"
        else
            printf '%s\n' "$candidate"
            return 0
        fi
    done
    echo "错误：$label 从 $base 起连续 10 个候选端口（$occupied）均被占用，无法启动测试环境" >&2
    return 1
}

# 关闭测试实例的登录验证码（计划 T1.2 / 评审 R1）
# 调用链核实：LOGIN_CAPTCHA_ENABLED 全库仅两处读取（CaptchaController /captcha/image、
# AccountLoginHandler.preLogin），均经 OptionServiceImpl.getValueByCode 以 RedisUtils 直读
# Redis（未命中查库后无 TTL 回写），不经过多级缓存 L1（无 @Cacheable）。
# 因此 UPDATE 直改库不会触发业务层 clearCache()，且 Redis 中可能残留旧值 '1'，
# UPDATE 后必须 DEL 对应 L2 键，登录时才确定性地回源数据库读到 '0'。
# 顺序约束：先 UPDATE 后 DEL，避免中间窗口内读取把旧值写回 Redis。
# 与 scripts/test/api-smoke.sh 中的同逻辑保持同步。
disable_login_captcha() {
    echo "关闭登录验证码（sys_option.LOGIN_CAPTCHA_ENABLED）..."
    compose exec -T mysql-test sh -c "mysql -uroot -p\"\$MYSQL_ROOT_PASSWORD\" continew_admin -e \"UPDATE sys_option SET value='0' WHERE code='LOGIN_CAPTCHA_ENABLED'\"" >/dev/null
    compose exec -T redis-test sh -c 'REDISCLI_AUTH="$REDIS_PASSWORD" redis-cli DEL "OPTION:LOGIN_CAPTCHA_ENABLED"' >/dev/null
}

# 应用健康探测：对根路径做 HTTP 探测并接受任何状态码，只验证应用已开始响应。
# 刻意不用 /captcha/image：该接口会预热 OPTION:LOGIN_CAPTCHA_ENABLED 的 Redis 缓存（评审 R1）
wait_app_healthy() {
    local url deadline
    url="http://127.0.0.1:${1}/"
    deadline=$((SECONDS + 300))
    echo "等待应用就绪：${url}（最长 300 秒）..."
    while (( SECONDS < deadline )); do
        # curl 不带 -f：任何 HTTP 状态码（含 401/404）都代表应用已开始响应
        if curl -s -o /dev/null --max-time 2 "$url"; then
            echo "应用已就绪"
            return 0
        fi
        sleep 3
    done
    echo "错误：应用 300 秒内未就绪，查看日志：" >&2
    echo "  docker compose -f \"$(to_host_path "$COMPOSE_FILE")\" -p $PROJECT logs app" >&2
    return 1
}

# 容器状态清单（服务 状态 健康），无容器时输出为空
# app 服务挂在 mount/build profile 下，不带 --profile 的 ps/down 看不到也清不掉它们
compose_service_status() {
    compose --profile mount --profile build ps --format '{{.Service}} {{.State}} {{.Health}}' 2>/dev/null || true
}

print_ready_hint() {
    echo ""
    echo "测试环境已就绪（project：$PROJECT）"
    echo "  应用  ：http://127.0.0.1:${APP_PORT}"
    echo "  Redis ：127.0.0.1:${REDIS_PORT}"
    echo "  验证码：已关闭（sys_option.LOGIN_CAPTCHA_ENABLED=0）"
    echo "运行 L3 测试：bash scripts/test/api-smoke.sh"
    echo "停止环境    ：bash scripts/test/down.sh"
}

# 前置检查：docker 与 compose 插件可用
command -v docker >/dev/null 2>&1 || { echo "错误：未找到 docker 命令" >&2; exit 1; }
docker compose version >/dev/null 2>&1 || { echo "错误：docker compose 插件不可用" >&2; exit 1; }

# 环境文件：首次使用自动从示例复制（弱密码仅测试环境，不入库）
if [[ ! -f "$ENV_FILE" ]]; then
    cp "$TEST_DIR/.env.example" "$ENV_FILE"
    echo "已从 .env.example 生成 $ENV_FILE（默认弱密码仅用于测试环境）"
fi

# shellcheck disable=SC1090
source "$ENV_SOURCE"

# --keep：容器全部健康且端口文件存在才复用；存在但不健康直接报错，不自动修复（计划 4.6）
if [[ "$MODE" == "keep" ]]; then
    STATUS="$(compose_service_status)"
    if [[ -n "$STATUS" && -f "$PORTS_FILE" ]] \
        && grep -q '^mysql-test running healthy' <<<"$STATUS" \
        && grep -q '^redis-test running healthy' <<<"$STATUS" \
        && grep -Eq '^app-test(-built)? running' <<<"$STATUS"; then
        # shellcheck disable=SC1090
        source "$PORTS_FILE"
        echo "--keep：现有容器全部健康，复用（沿用 .ports.env 端口）"
        wait_app_healthy "$APP_PORT"
        disable_login_captcha
        print_ready_hint
        exit 0
    fi
    if [[ -n "$STATUS" ]]; then
        echo "错误：--keep 要求现有容器全部健康，当前状态：" >&2
        echo "$STATUS" >&2
        echo "请先执行 bash scripts/test/down.sh 清理，或 bash scripts/test/reset.sh 强制重建" >&2
        exit 1
    fi
    echo "--keep：无运行中容器，回退为全新启动"
    MODE="mount"
fi

echo "[1/4] 探测空闲端口（应用从 ${APP_PORT:-18020} 起、Redis 从 ${REDIS_PORT:-16379} 起）..."
APP_PORT="$(detect_free_port "${APP_PORT:-18020}" "应用端口")"
REDIS_PORT="$(detect_free_port "${REDIS_PORT:-16379}" "Redis 端口")"
cat > "$PORTS_FILE" <<EOF
# 由 scripts/test/up.sh 探测生成（不入库），docker compose 与 scripts/test/api-smoke.sh 共同消费
APP_PORT=$APP_PORT
REDIS_PORT=$REDIS_PORT
EOF
echo "  应用端口：$APP_PORT，Redis 端口：$REDIS_PORT（已写入 docker/test/.ports.env）"

if [[ "$MODE" == "mount" ]]; then
    echo "[2/4] 宿主构建瘦包（-DskipTests，产物挂载进容器）..."
    (cd "$REPO_ROOT" && ./mvnw -B -pl :continew-server -am package -DskipTests)
else
    echo "[2/4] 跳过宿主构建（--build 模式由 compose 多阶段构建，首次较慢）..."
fi

echo "[3/4] 清理既有测试环境（仅限 project：$PROJECT），当前容器清单："
compose --profile mount --profile build ps || true
compose --profile mount --profile build down -v --remove-orphans

echo "[4/4] 启动测试环境..."
if [[ "$MODE" == "mount" ]]; then
    compose --profile mount up -d
else
    compose --profile build up -d --build
fi

wait_app_healthy "$APP_PORT"
disable_login_captcha
print_ready_hint
