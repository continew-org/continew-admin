#!/usr/bin/env bash
# 守卫：统计真实执行的 L2 集成测试用例数（tests - skipped），低于下限即失败，
# 防止 Docker/环境异常导致集成测试被整体跳过而构建仍绿灯。
# 供 ci.yml 与 full-test.yml 共用；下限调整只需改这里一处。
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
# 下限沿用 full-test.yml 原守卫口径，给跳过留余量；调整下限只需改此处
readonly MIN_EXECUTED=20

total=$(python3 -I - "$REPO_ROOT" <<'PY'
import glob
import os
import sys
import xml.etree.ElementTree as ET

repo_root = sys.argv[1]
# 只统计 L2 报告（IntegrationTest + ApplicationTests），不纳入 API 遗留报告与 L1 smoke，
# 避免上限虚高掩盖「L2 全跳过」
patterns = (
    'continew-tests/target/surefire-reports/TEST-*IntegrationTest.xml',
    'continew-tests/target/surefire-reports/TEST-*ApplicationTests.xml',
)
total = 0
for pattern in patterns:
    for path in glob.glob(os.path.join(repo_root, pattern)):
        root = ET.parse(path).getroot()
        total += int(root.get('tests', '0')) - int(root.get('skipped', '0'))
print(total)
PY
)

echo "L2 integration tests executed (excluding skipped): $total"
if [ "$total" -lt "$MIN_EXECUTED" ]; then
    echo "::error::真实执行的 L2 集成测试用例数（$total）低于下限（$MIN_EXECUTED），可能全部被跳过"
    exit 1
fi
