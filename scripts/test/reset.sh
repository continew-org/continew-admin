#!/usr/bin/env bash
# 强制重建 L3 测试环境：先清理（含数据卷），再全新启动
# 用法：bash scripts/test/reset.sh [--build | --keep]，参数透传给 up.sh
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "== 重置测试环境：down -v 后重新启动 =="
bash "$SCRIPT_DIR/down.sh"
exec bash "$SCRIPT_DIR/up.sh" "$@"
