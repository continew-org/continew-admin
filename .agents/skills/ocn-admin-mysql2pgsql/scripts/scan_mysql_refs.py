#!/usr/bin/env python3
"""扫描切换到 PostgreSQL 所需的**配置**接入点，输出需要改动的清单。

范围：数据源、MyBatis 方言、Liquibase 入口、运行时驱动。**不含 SQL 语句本身**——
mapper 的 databaseId 语句、迁移脚本内容都属既有 SQL 资产，本技能不改。

用法：
    python scan_mysql_refs.py <项目根目录> [-p dev|local]

只读操作，不修改任何文件。输出按严重程度分级：
    MUST  必须改，否则切换后启动失败或运行期报错
    WARN  建议确认，多为测试/CI 或 SQL 资产，按需处理
    INFO  已确认或已兼容，无需改动
"""

import argparse
import os
import re
import sys
import xml.etree.ElementTree as ET

SKIP_DIRS = {
    ".git", "node_modules", "target", "build", "dist", ".idea", ".vscode",
    ".workbuddy", ".pnpm-store", "__pycache__", "logs",
}

# 代码文件：Java/Kotlin 里可能内联 SQL，XML 是 mapper 载体
CODE_EXT = {".java", ".kt", ".xml"}

# 读取失败的路径，用于最终判断扫描是否完整
READ_ERRORS = []

# 本地 Maven 仓库：解析有效 POM 的受管 scope 时要查父 POM 与 BOM。
# 只用惯例位置和显式环境变量，不写死某台机器的路径——技能是随仓库走的。
MAVEN_REPOS = [repo for repo in (
    os.environ.get('MAVEN_REPO_LOCAL'), os.path.expanduser('~/.m2/repository')) if repo]


def walk(root):
    def onerror(exc):
        message = f'{exc.filename}：无法遍历目录（{type(exc).__name__}）'
        if message not in READ_ERRORS:
            READ_ERRORS.append(message)

    for dirpath, dirnames, filenames in os.walk(root, onerror=onerror):
        dirnames[:] = [d for d in dirnames if d not in SKIP_DIRS]
        for name in filenames:
            yield os.path.join(dirpath, name)


def rel(path, root):
    return os.path.relpath(path, root).replace(os.sep, "/")


def read(path, root=None):
    """读文件；失败时记录到 READ_ERRORS，使最终结论能区分「没问题」与「读不到」。

    早期实现失败直接返回空串，结果是「文件读不了 → 没匹配到问题 → 报告切换完成」，
    这是危险的假阴性。
    """
    try:
        with open(path, "r", encoding="utf-8-sig") as f:
            return f.read()
    except (OSError, UnicodeError) as exc:
        shown = rel(path, root) if root else path
        message = f'{shown}: {type(exc).__name__}：文件无法读取或不是有效 UTF-8。'
        if message not in READ_ERRORS:
            READ_ERRORS.append(message)
        return ""


class Finding:
    def __init__(self, level, tag, path, line, message):
        self.level = level
        self.tag = tag
        self.path = path
        self.line = line
        self.message = message

    def __str__(self):
        loc = f"{self.path}:{self.line}" if self.line else self.path
        return f"[{self.level}] {self.tag}\n    {loc}\n    {self.message}"


def parse_url(url):
    """从 JDBC URL 抽出 host / port / database，兼容 ${VAR:default} 占位符。

    同时返回 `*_raw` 原始表达式：`${APP_PORT:5432}` 与 `${MIGRATION_PORT:5432}`
    解析出的默认值相同，但运行时可以指向不同实例，只比默认值会误判成同库。
    """
    if not url:
        return None
    body = url
    idx = body.find("://")
    if idx < 0:
        return None
    body = body[idx + 3:]
    # 先砍掉行尾注释：YAML 里常见 `url: jdbc:mysql://... # 说明`，
    # 不处理会把注释文字并进库名。
    body = re.split(r"\s+#", body, maxsplit=1)[0]
    body = body.split("?")[0].strip()
    if "/" not in body:
        return None
    authority, db = body.split("/", 1)
    db = db.strip()
    # authority 形如 ${DB_HOST:10.10.100.3}:${DB_PORT:3306} 或 host:3306。
    # 按「花括号外的最后一个冒号」切分，避免被占位符内部的冒号干扰。
    depth = 0
    cut = -1
    for i, ch in enumerate(authority):
        if ch == "{":
            depth += 1
        elif ch == "}":
            depth -= 1
        elif ch == ":" and depth == 0 and i > authority.rfind(']'):
            cut = i
    host_raw, port_raw = (authority[:cut], authority[cut + 1:]) if cut > 0 else (authority, "")
    default_port = '5432' if 'postgresql://' in url else '3306'
    return {
        "host": parse_default(host_raw) if host_raw else None,
        "host_raw": host_raw or None,
        "port": parse_default(port_raw) if port_raw else default_port,
        "port_raw": port_raw or default_port,
        "db": db or None,
        "db_raw": db or None,
    }


def url_param(url, name):
    """从 JDBC URL 的查询串里取参数值，取不到返回 None。

    用于 currentSchema 这类「同库不同 schema」仍会跑错地方的参数。
    """
    if not url or '?' not in url:
        return None
    query = url.split('?', 1)[1]
    for part in re.split(r'[&;]', query):
        key, sep, value = part.partition('=')
        if sep and key.strip().lower() == name.lower():
            return value.strip() or None
    return None


def parse_default(value):
    """从 ${VAR:default} 里取出默认值；无默认值返回 None。"""
    if value is None:
        return None
    m = re.match(r"^\$\{[^:}]+:(.*)\}$", value.strip())
    if m:
        return m.group(1)
    if value.strip().startswith("${"):
        return None  # 只有变量、无默认值，无法静态比对
    return value.strip()


def _indent_of(line):
    return len(line) - len(line.lstrip())


def _read_block_scalar(rows, pos, base_indent, header):
    """读取块标量正文，返回 (折叠后的文本, 下一行下标)。

    正文里的 `k: v` 只是文本，不能当成配置；但同时它的折叠结果**就是该键的真实值**。
    直接丢弃这个键，会让共享文件里的旧值继续冒充生效值——例如目标 profile 用
    `spring.datasource.url: >-` 写 MySQL 地址时，扫描器仍按共享的 PG 地址验收通过。

    正文缩进由首个非空行确定，之后只要不比它浅就仍属正文；回落到所属键缩进即结束。
    """
    body = []
    body_indent = None
    while pos < len(rows):
        text = rows[pos][1].rstrip('\r\n')
        if not text.strip():
            body.append('')
            pos += 1
            continue
        cur = _indent_of(text)
        if body_indent is None:
            if cur <= base_indent:
                break
            body_indent = cur
        elif cur < body_indent:
            break
        body.append(text[body_indent:])
        pos += 1
    while body and not body[-1].strip():
        body.pop()
    if not body:
        return '', pos
    if header.startswith('|'):
        return '\n'.join(body).strip('\n'), pos
    folded = ''
    for line in body:
        if not line.strip():
            folded += '\n'
        elif folded and not folded.endswith('\n'):
            folded += ' ' + line.strip()
        else:
            folded += line.strip()
    return folded, pos


def check_credential_parity(rp, blocks, findings):
    """比对 MySQL 与 PostgreSQL 的 host / 库名 / 用户名 / 密码是否一致。

    本仓约定：切换时这四项沿用 MySQL 原值，只改端口。
    """
    mysqls = [b for b in blocks if b["kind"] == "mysql"]
    pgs = [b for b in blocks if b["kind"] == "postgres"]
    if not mysqls or not pgs:
        return

    # 优先拿生效的那份当基准，都没有则用注释里的
    mysql_ref = next((b for b in mysqls if not b["commented"]), mysqls[0])
    pg_ref = next((b for b in pgs if not b["commented"]), pgs[0])

    m_url = parse_url(mysql_ref["url"])
    p_url = parse_url(pg_ref["url"])
    if not m_url or not p_url:
        findings.append(Finding('MUST', 'parity', rp, pg_ref['line'], 'JDBC URL 无法拆分，无法比对连接信息。'))
        return

    diffs = []
    skipped = []
    # 地址与库名必须比原始表达式：`${MYSQL_HOST:db}` 与 `${PG_HOST:db}` 的默认值相同，
    # 但两个变量在部署时可以分别覆写成不同实例，只比默认值会把「换了地址」判成一致。
    for label, key in (("地址", "host"), ("库名", "db")):
        m_val, p_val = m_url[key + "_raw"], p_url[key + "_raw"]
        if m_val is None or p_val is None:
            skipped.append(label)
        elif m_val != p_val:
            diffs.append(f'{label}：不一致')

    m_user = mysql_ref['username'] or None
    p_user = pg_ref['username'] or None
    if m_user is None or p_user is None:
        skipped.append("用户名")
    elif m_user != p_user:
        diffs.append('用户名：不一致')

    m_pwd = mysql_ref['password']
    p_pwd = pg_ref['password']
    # 显式空字符串也可逐项沿用；连接是否接受该密码不属于静态验收。
    if m_pwd is None or p_pwd is None:
        skipped.append("密码")
    elif m_pwd != p_pwd:
        diffs.append("密码：不一致（MySQL 实际值已隐藏）")

    skip_note = ""
    if skipped:
        skip_note = ("\n    以下项缺少可比对的配置，请人工核对："
                     + "、".join(skipped))

    # 端口单独判定：本仓约定是「只改端口」，但它不属于「须一致」的四项。
    # 只有两侧都能解析出端口时才谈具体值，不解析就无法宣称「仅端口不同」。
    m_port, p_port = m_url["port"], p_url["port"]
    port_note = ""
    if not all(port and re.fullmatch(r'[0-9]{1,5}', port) and 0 < int(port) <= 65535
               for port in (m_port, p_port)):
        skipped.append('有效端口')
        port_note = '\n    至少一侧端口无效或没有静态默认值。'
    elif m_port == p_port:
        diffs.append('两侧端口相同，未确认按约定切换端口。')
    else:
        port_note = '\n    端口已区分。'

    if diffs:
        findings.append(Finding(
            "MUST", "parity", rp, pg_ref["line"],
            "按本仓约定，切换时须沿用 MySQL 的 host / 库名 / 用户名 / 密码。以下差异阻塞切换：\n"
            + "\n".join(f"    · {d}" for d in diffs) + skip_note + port_note))
    elif not skipped:
        findings.append(Finding(
            "INFO", "parity", rp, pg_ref["line"],
            "host / 库名 / 用户名 / 密码与 MySQL 一致，符合本仓约定。" + port_note))
    else:
        findings.append(Finding(
            "MUST", "parity", rp, pg_ref["line"],
            "无法确认全部连接信息一致。" + skip_note + port_note))


def is_test_resource(path, root):
    """测试资源不是独立部署的应用，不参与配置验收（但仍参与测试引用扫描）。"""
    relp = '/' + rel(path, root).lower().replace(os.sep, '/')
    return '/src/test/' in relp or '/src/integrationtest/' in relp


def _split_inline_map(text):
    """把 `{a: 1, b: {c: 2}}` 拆成 [(a, '1'), (b, '{c: 2}')]；不是合法行内映射时返回 None。"""
    text = text.strip()
    if len(text) < 2 or text[0] != '{' or text[-1] != '}':
        return None
    body = text[1:-1].strip()
    if not body:
        return []
    pairs, depth, quote, buf = [], 0, None, []
    for ch in body:
        if quote:
            buf.append(ch)
            if ch == quote:
                quote = None
            continue
        if ch in '"\'':
            quote = ch
            buf.append(ch)
        elif ch in '{[':
            depth += 1
            buf.append(ch)
        elif ch in '}]':
            depth -= 1
            buf.append(ch)
        elif ch == ',' and depth == 0:
            pairs.append(''.join(buf))
            buf = []
        else:
            buf.append(ch)
    if quote or depth:
        return None
    pairs.append(''.join(buf))
    out = []
    for item in pairs:
        key, sep, value = item.partition(':')
        if not sep or not key.strip():
            return None
        out.append((key.strip().strip('"\''), value.strip()))
    return out


def _expand_inline(values, full_key, text, origin, line, depth=0):
    """把行内映射展开成子键，避免它整体存成字符串后保留旧子键值。

    返回 False 表示内容无法解析（调用方据此报告解析不完整）。
    """
    pairs = _split_inline_map(text)
    if pairs is None:
        return False
    if not pairs:
        return True
    for key, value in pairs:
        # 行内映射里的合并键 `{<<: *alias}` 与块形式的 `<<: *alias` 是同一件事：
        # 被合并的键在别处定义，这里展开不了。存成 `spring.datasource.<<` 会让
        # 「通过锚点合并覆盖了 MySQL URL」看起来像没覆盖过，从而按共享 PG 值放行。
        if key.strip() == '<<':
            READ_ERRORS.append(
                f'{origin}:{line}：YAML 合并键 << 无法静态展开，无法确认目标配置。')
            return True
        child = f'{full_key}.{_normalize_key(key)}'
        if value.startswith('{'):
            if depth > 4 or not _expand_inline(values, child, value, origin, line, depth + 1):
                return False
            continue
        # 行内映射里的子节点也可能是别名（`{datasource: *legacy}`）。锚点内容在别处，
        # 存成普通字符串会让 `spring.datasource.url` 继续沿用共享文件的 PG 值。
        if re.fullmatch(r'\*[\w.-]+', value.strip()):
            READ_ERRORS.append(
                f'{origin}:{line}：YAML 别名 {value.strip()} 无法静态展开，'
                '无法确认目标配置。')
            continue
        values[child] = (value.strip('"\''), origin, line)
    return True


def _normalize_key(text):
    return re.sub(r'(?<=[a-z0-9])([A-Z])', r'-\1', text).replace('_', '-').lower()


# 条件文档开关：命中即无法把该文档当成无条件生效的配置。
CONDITION_KEYS = ('spring.config.activate.on-profile',
                  'spring.config.activate.on-cloud-platform',
                  'spring.profiles', 'spring.config.import')


def _check_condition_keys(full_key, text, origin, line, depth=0):
    """条件文档检查，行内映射逐层递归。

    `spring.config.activate: {on-profile: staging}` 展开后才得到条件键，若只在普通键
    分支检查，这段文档就会被当成无条件生效，从而用 staging 的值覆盖 dev；整段写成
    `spring: {config: {activate: {on-profile: staging}}}` 时还需要逐层往下查，
    只查一层同样会漏。`on-cloud-platform` 与 `on-profile` 同为激活条件，
    漏掉它会让「仅 K8s 生效」的文档冒充无条件覆盖。
    """
    plain = full_key.lstrip('#')
    if plain in CONDITION_KEYS and (text or plain != 'spring.profiles'):
        READ_ERRORS.append(f'{origin}:{line}：不支持条件文档，无法确认目标配置。')
        return
    if depth > 4:
        return
    for key, value in _split_inline_map(text) or ():
        _check_condition_keys(f'{plain}.{_normalize_key(key)}', value, origin, line, depth + 1)


def _join_properties_lines(lines):
    """按 Java Properties 语义把行末反斜杠的续行拼成逻辑行。

    返回 [(起始行号, 逻辑行文本)]。逐行匹配会漏掉 `spring.datasource.\\` + `url=...`
    这种折行写法的覆盖项，从而误用共享文件里的旧值。

    三处必须遵循 Properties 的真实规则，否则仍然漏项：
      · 续行开头的前导空白会被跳过，不能把缩进当成键值与分隔符的一部分；
      · 是否续行取决于行末连续反斜杠数量的奇偶，`\\\\` 是转义后的字面反斜杠而非续行符；
      · `#` / `!` 注释行不支持续行，行末的反斜杠只是注释文字。若当成续行，
        下一条有效配置（例如 `spring.datasource.url`）会被并进注释而整条丢失。
    """
    out = []
    buf, start = '', None
    for i, raw in enumerate(lines, 1):
        text = raw.rstrip('\r\n')
        if start is None:
            natural = text.lstrip()
            if natural[:1] in ('#', '!'):
                # 注释行整体独立成一条逻辑行，不参与续行拼接。
                out.append((i, natural))
                continue
            start = i
            stripped = natural
        else:
            # 续行：前导空白（空格 / 制表符 / 换页）一律跳过。
            stripped = text.lstrip(' \t\f')
        trailing = len(stripped) - len(stripped.rstrip('\\'))
        if trailing % 2 == 1:
            buf += stripped[:-1]
            continue
        out.append((start, buf + stripped))
        buf, start = '', None
    if start is not None:
        out.append((start, buf))
    return out


def _parse_yaml_key(line):
    """拆出 YAML 映射键，支持 `"quoted.key": value`、`'k': v` 与合并键 `<<`。

    带引号的键是合法 YAML；正则只认 [\\w.-] 会整行静默跳过，
    目标 profile 用引号写的 MySQL 覆盖就白写了。

    合并键 `<<: *alias` 也是合法映射键：Spring 会把锚点里的键合并进来，
    静默跳过会让「目标 profile 通过合并覆盖 MySQL URL」看起来像没覆盖过。
    """
    m = re.match(r'''^("(?:[^"\\]|\\.)*"|'(?:[^']|'')*')\s*:\s*(.*)$''', line)
    if m:
        return m.group(1)[1:-1], m.group(2)
    m = re.fullmatch(r'(<<)\s*:\s*(.*)', line)
    if m:
        return m.groups()
    m = re.fullmatch(r'([\w.-]+)\s*:\s*(.*)', line)
    return m.groups() if m else None

def _decode_properties(text):
    """按 Java Properties 规则解码 `\\uXXXX` 与常见单字符转义。

    Properties 的键和值都支持转义：`spring.datasource.\\u0075rl` 在运行时就是
    `spring.datasource.url`。不解码会把它当成另一个键，目标 profile 指向 MySQL 的
    覆盖被忽略，最后拿共享文件的 PG 地址验收。
    只处理行内已拼好的逻辑行，不含转义时原样返回。
    """
    if '\\' not in text:
        return text
    out = []
    i = 0
    simple = {'t': '\t', 'n': '\n', 'r': '\r', 'f': '\f'}
    while i < len(text):
        ch = text[i]
        if ch != '\\' or i + 1 >= len(text):
            out.append(ch)
            i += 1
            continue
        nxt = text[i + 1]
        if nxt == 'u' and i + 5 < len(text) + 1:
            hexpart = text[i + 2:i + 6]
            if len(hexpart) == 4 and all(c in '0123456789abcdefABCDEF' for c in hexpart):
                out.append(chr(int(hexpart, 16)))
                i += 6
                continue
        out.append(simple.get(nxt, nxt))
        i += 2
    return ''.join(out)


def _split_properties_line(line):
    """按 Properties 规则把逻辑行拆成 (key, value)。

    关键顺序：**先用未转义的分隔符拆，再各自解码**。反过来先解码会改变分隔符判定：
    `password=\\ secret` 里反斜杠把空格转义成了值的一部分，解码后再用 `\\s*` 匹配
    会把这段空格当成键值之间的空白吃掉，值变成 `secret`，于是与 `password=secret`
    错误地判成一致——而运行时两者的密码并不相同。

    规则要点：
      · 第一个未转义的 `:` / `=` 是分隔符；没有分隔符时第一个未转义的空白也算；
      · 分隔符两侧的空白（未转义）会被跳过，但值内部与转义出来的空白属于值；
      · 没有任何分隔符的纯键行，值为空字符串（会把共享配置清空）。
    """
    i = 0
    n = len(line)
    key_chars = []
    sep_index = -1
    while i < n:
        ch = line[i]
        if ch == '\\' and i + 1 < n:
            key_chars.append(line[i:i + 2])
            i += 2
            continue
        if ch in ':= ' or ch == '\t' or ch == '\f':
            sep_index = i
            break
        key_chars.append(ch)
        i += 1
    if sep_index < 0:
        return _decode_properties(''.join(key_chars)), ''
    # 分隔符之后：跳过未转义的空白，再可选的 `:` / `=` 与其后的空白。
    j = sep_index
    while j < n and line[j] in ' \t\f':
        j += 1
    if j < n and line[j] in ':=':
        j += 1
        while j < n and line[j] in ' \t\f':
            j += 1
    return _decode_properties(''.join(key_chars)), _decode_properties(line[j:])


def target_configs(root, profile):
    """在同一资源目录内逐键覆盖；不借用其他模块或 profile 的值。"""
    groups = {}
    for path in walk(root):
        if is_test_resource(path, root):
            continue
        name, ext = os.path.splitext(os.path.basename(path))
        if name in ('application', f'application-{profile}') and ext in ('.yaml', '.yml', '.properties'):
            groups.setdefault(os.path.dirname(path), []).append(path)
    for directory, paths in groups.items():
        values = {}
        # YAML 标量列表项（`contexts:` 下面写 `- dev`）没有键，普通键值循环会整条丢掉。
        # 值只可能是「父键的值集合」，单独存下来供 contexts / labels 一类多值配置使用。
        seq_scalars = {}
        # 记录每个列表键最近写入的文件：换文件说明更高优先级覆盖了它，元素要整体替换。
        seq_owner = {}
        # 同层 properties 优先于 YAML；目标 profile 优先于公共文件。
        paths.sort(key=lambda p: ('-' in os.path.basename(p), p.endswith('.properties'), p))
        for path in paths:
            parents = []
            comment_parents = []
            seen_keys = {}
            # YAML 序列里每个 `- ` 元素是独立的映射，元素之间同名键不算重复。
            # epoch 记录各缩进层的元素序号，参与判重 marker。
            epoch = {}
            target_file = os.path.splitext(os.path.basename(path))[0] == f'application-{profile}'
            is_props = path.endswith('.properties')
            text = read(path, root)
            rows = (_join_properties_lines(text.splitlines()) if is_props else
                    [(n, line) for n, line in enumerate(text.splitlines(), 1)])
            pos = 0
            while pos < len(rows):
                i, raw = rows[pos]
                pos += 1
                # Properties 的值末尾空白属于值本身：`password=abc ` 与 `password=abc` 是
                # 两个不同的密码，运行时按原样送进 JDBC。整行 strip 会让两者看起来一致，
                # 所以 properties 只去掉行首缩进，保留行尾字符。
                content = raw.lstrip() if is_props else raw.strip()
                commented = content.startswith('#')
                if commented:
                    if not target_file:
                        continue
                    content = content[1:].lstrip() if is_props else content[1:].strip()
                line = (content if is_props else
                        re.split(r'''\s+#(?=(?:[^"']|"[^"]*"|'[^']*')*$)''', content, maxsplit=1)[0])
                # 序列项：`- key: value` 或单独一行 `-`。前者仍然带键，剥掉前缀继续解析。
                is_item = bool(not is_props and re.match(r'^-(?:\s|$)', line))
                if is_item:
                    dash_indent = _indent_of(raw) + (1 if commented else 0)
                    # 注释不改变 YAML 结构：注释行上的 `-` 只是注释文字，不能推进
                    # 活动序列的计数，也不能清掉同层的 epoch。否则 `spring.datasource:`
                    # 下先写一个裸 `-`、再写同缩进的注释、然后缩进写 `url:` 时，
                    # 这个 url 会被当成 spring.datasource.url（实际它在列表元素里）。
                    if not commented:
                        epoch[dash_indent] = epoch.get(dash_indent, 0) + 1
                        for level in [lv for lv in epoch if lv > dash_indent]:
                            del epoch[level]
                    line = line[1:].lstrip()
                if line in ('---', '#---', '!---'):
                    # 新的 YAML 文档：父键、判重、序列计数全部归零。
                    parents.clear()
                    comment_parents.clear()
                    seen_keys.clear()
                    epoch.clear()
                if is_props:
                    # 先按未转义的分隔符拆键值，再各自解码；顺序反了会让
                    # `password=\ secret` 的前导空格被当成键值间隔而丢失。
                    match = _split_properties_line(line)
                else:
                    match = _parse_yaml_key(line)
                    if match and not is_item:
                        # 同缩进的普通键意味着上面的列表已结束（YAML 允许 `-` 与所属键同缩进），
                        # 必须连同该层的序列计数一起清掉，否则后续键会被误加 `[n]`。
                        for level in [lv for lv in epoch if lv >= _indent_of(raw)]:
                            del epoch[level]
                if not match:
                    # 标量列表项：`- dev` 在剥掉 `-` 之后没有键值结构。记录它属于哪个父键，
                    # 供 contexts / labels 这类多值配置解析；其余情况下忽略（不影响键值覆盖）。
                    # 注释行在上面已被剥掉 `#`，必须用 commented 显式排除，否则被注释掉的
                    # `# - staging` 会变成真实的列表元素，把 staging context 打开。
                    if is_item and not is_props and not commented and line and not line.startswith('#'):
                        # YAML 允许序列项与所属键同列（`contexts:` 下一行 `- dev` 顶格）。
                        # 这里必须用 <= 而不是 <：严格小于会把同列父键排除，列表就丢了
                        # 归属，active_set() 会以为没配过滤而放行仅限 staging 的脚本。
                        owner = '.'.join(_normalize_key(p[1]) for p in parents
                                         if p[0] <= _indent_of(raw))
                        item = line.strip()
                        if owner and item and not re.fullmatch(r'[\w.-]+\s*:.*', item):
                            # Spring Boot 的列表是整体覆盖而非追加：更高优先级的文件写了
                            # 同名键时低优先级的元素全部作废。共享文件 contexts 是 staging、
                            # dev 覆盖成 dev 时若继续追加，仍会放行仅限 staging 的脚本。
                            # 因此同一个键一旦换了文件就清空重来（同文件内多个元素照常累积）。
                            if seq_owner.get(owner) != path:
                                seq_scalars.pop(owner, None)
                                seq_owner[owner] = path
                            seq_scalars.setdefault(owner, []).append(item.strip('"\''))
                    continue
                key, value = match
                indent = _indent_of(raw)
                # YAML 合并键 `<<: *alias`：被合并的键在本文件别处（甚至别的文档）定义，
                # 这里不做锚点展开，直接记为解析不完整——静默跳过会让「通过合并覆盖了
                # MySQL URL」看起来像没覆盖过，从而按共享文件的 PG 地址放行。
                if key == '<<' and not commented:
                    READ_ERRORS.append(
                        f'{rel(path, root)}:{i}：YAML 合并键 << 无法静态展开，'
                        '无法确认目标配置。')
                    continue
                # 块标量（`key: |` / `key: >`，可带缩进指示与 chomping 修饰）：
                # 正文既不能当配置解析，也不能整段丢掉——折叠后的文本就是该键的值，
                # 丢掉它会让共享文件里的旧值继续冒充生效值。
                # 注释行不参与块标量判定：`# 示例: |` 是注释文字，不是标量头，
                # 否则它会按注释所在的浅缩进吞掉后面真正的 datasource 子树。
                is_block = bool(not is_props and not commented and re.fullmatch(
                    r'[|>][0-9]*[+-]?|[|>][+-][0-9]*', value.strip()))
                if is_block:
                    value, pos = _read_block_scalar(rows, pos, indent, value.strip())
                if commented and not is_props:
                    suffix = raw[indent + 1:].removeprefix(' ')
                    indent += _indent_of(suffix)
                scope = [p for p in (comment_parents if commented and comment_parents else parents) if p[0] < indent]
                # YAML 序列元素是列表项，实际属性路径带 [n] 索引（spring.datasource[1].url）。
                # 不带上索引会让 `- url:` 冒充 `spring.datasource.url`，等于确认一个运行时
                # 根本不存在的数据源；带上索引后它自然不会命中验收键。
                segments = [p[1] for p in scope]
                for level in sorted(epoch):
                    if level > indent:
                        break
                    index = f'[{epoch[level] - 1}]'
                    if segments:
                        segments[-1] += index
                    else:
                        segments.append(index)
                full_key = '.'.join(segments + [key])
                full_key = _normalize_key(full_key)
                if commented:
                    comment_parents = scope
                    if full_key not in (DS + 'url', DS + 'username', DS + 'password', DS + 'driver-class-name'):
                        if not value and not path.endswith('.properties'):
                            comment_parents.append((indent, key))
                        continue
                else:
                    parents = scope
                    comment_parents.clear()
                    # 同一文件同一层级重复的活动键会被解析器覆盖（有的直接拒绝加载），
                    # 但 profile 文件覆盖共享文件是合法的，所以只在本文件内判重。
                    # 序列元素各自是独立映射，用所属元素序号区分，否则不同元素的同名键会误报。
                    elem = max((lv for lv in epoch if lv <= indent), default=None)
                    elem_id = (elem, epoch[elem]) if elem is not None else None
                    marker = (tuple(p[1] for p in scope), key, elem_id)
                    if marker in seen_keys:
                        READ_ERRORS.append(
                            f'{rel(path, root)}:{i}：与第 {seen_keys[marker]} 行重复的配置键 {key}，'
                            '无法确认实际生效值。')
                    else:
                        seen_keys[marker] = i
                if commented:
                    full_key = '#' + full_key
                    if full_key in values:
                        READ_ERRORS.append(f'{rel(path, root)}:{i}：目标数据源的注释基线不唯一。')
                _check_condition_keys(full_key, value, rel(path, root), i)
                # Properties 的分隔符后空白由解析器跳过，值末尾的空白属于值本身，不能 strip；
                # YAML 的标量语义相反，首尾空白都不属于值。
                scalar = value if is_props else value.strip()
                # 块标量的内容永远是字符串，不会因首字符是 `{` 变成行内映射，
                # 也不存在「加引号 / null」的语义，按字面值直接参与比对。
                quoted = (not is_props and len(scalar) >= 2 and scalar[0] == scalar[-1]
                          and scalar[0] in ('\"', "'"))
                if quoted:
                    scalar = scalar[1:-1]
                is_inline_map = not is_props and not is_block and not quoted and scalar.startswith('{')
                if not is_props and not is_block and not is_inline_map and not quoted and \
                        scalar in ('', '~', 'null', 'Null', 'NULL'):
                    # 只有未加引号的标量才是 YAML 的 null / 空值；
                    # `password: ""` 与 `password: "null"` 是明确写下的字面值。
                    scalar = None if full_key.lstrip('#') == DS + 'password' else ''
                if is_inline_map and not commented:
                    if not _expand_inline(values, full_key, scalar, rel(path, root), i):
                        READ_ERRORS.append(
                            f'{rel(path, root)}:{i}：行内映射无法解析，无法确认目标配置。')
                    continue
                if quoted and scalar.startswith('{') and full_key.lstrip('#') == DS.rstrip('.'):
                    # 数据源节点被写成字符串，Spring 绑不出子键，按字符串展开会凭空造出
                    # 一个并不存在的数据源。
                    READ_ERRORS.append(
                        f'{rel(path, root)}:{i}：{full_key} 是字符串而非映射，无法确认目标配置。')
                # YAML 别名作节点值（`spring.datasource: *legacy`）：运行时取的是锚点里的
                # 内容，可能是另一份含 MySQL URL 的映射。不作为普通标量处理，否则只写入
                # 父键、继续拿共享文件的 PG 值验收。
                # 只在 scalar 是字符串时匹配：`password: null` 会先被转成 None，
                # 直接丢进 re.fullmatch 会抛 TypeError 把整次扫描打断。
                if not is_props and not is_block and not quoted and isinstance(scalar, str) \
                        and re.fullmatch(r'\*[\w.-]+', scalar):
                    READ_ERRORS.append(
                        f'{rel(path, root)}:{i}：{full_key} 使用了 YAML 别名 {scalar}，'
                        '无法静态展开，无法确认目标配置。')
                values[full_key] = (scalar, rel(path, root), i)
                plain_key = full_key.lstrip('#')
                # 标量覆盖同名键时，低优先级文件里的列表元素全部作废：Spring Boot 的
                # 列表是整体覆盖而非合并。共享文件写 `contexts:` 块列表为 staging、
                # dev 用 `contexts: dev` 标量覆盖时，若不清掉旧列表，active_set() 会把
                # 两者并集，仅限 staging 的脚本仍被算进 dev 的活动清单。
                if not commented and plain_key in seq_scalars:
                    seq_scalars.pop(plain_key, None)
                    seq_owner.pop(plain_key, None)
                # `spring.profiles.include` / `spring.profiles.group.<名>` 会额外激活别的
                # profile 文件，那里可以单独设置 spring.liquibase.url 或数据源。这里只加载
                # 共享文件 + 目标 profile，看不到这些文件，必须报无法确认而不是按复用业务
                # 数据源放行。
                # 不含 `spring.profiles.active`：它选的就是被扫的目标 profile 本身，
                # 不引入本流程之外的文件。
                if not commented and (plain_key == 'spring.profiles.include'
                                      or plain_key == 'spring.profiles.group'
                                      or plain_key.startswith('spring.profiles.group.')):
                    READ_ERRORS.append(
                        f'{rel(path, root)}:{i}：{plain_key} 会额外激活其它配置文件，'
                        '无法静态确认目标配置。')
                if not commented and not is_block and not value and not is_props:
                    parents.append((indent, key))
        yield directory, rel(paths[-1], root), values, seq_scalars


DS = 'spring.datasource.'
DEFAULT_LOG = 'classpath:/db/changelog/db.changelog-master.yaml'
REQUIRED_SCRIPTS = ('main_table.sql', 'main_data.sql', 'plugin/plugin_open.sql',
                    'plugin/plugin_tenant.sql', 'plugin/plugin_schedule.sql',
                    'plugin/plugin_generator_removal.sql')


def resource_path(base, ref, resource_dir):
    ref = re.sub(r'^classpath:/?', '', ref)
    if os.path.splitdrive(ref)[0] or ref.startswith(('//', '\\')):
        return None
    path = os.path.realpath(os.path.join(base, ref.lstrip('/')))
    try:
        within = os.path.relpath(path, os.path.realpath(resource_dir))
    except ValueError:  # Windows 跨盘路径
        return None
    return None if within == '..' or within.startswith('..' + os.sep) else path


def check_effective_datasource(configs, prod_configs, findings, profile='dev'):
    for directory, source, values, _seq in configs:
        def issue(tag, message, key=DS + 'url', level='MUST'):
            _, path, line = values.get(key, (None, source, 0))
            findings.append(Finding(level, tag, path, line, message))

        prod = prod_configs.get(directory)
        prod_url = prod.get(DS + 'url', ('',))[0] if prod else ''
        if prod is not None and not re.match(r'^jdbc:(?:p6spy:)?(?:mysql|postgresql)://', prod_url):
            issue('isolation', '无法确认 prod 的有效数据源，不能证明环境隔离。')
        if (not prod_url.startswith('jdbc:mysql://') or
                (prod or {}).get(DS + 'driver-class-name', ('',))[0] != 'com.mysql.cj.jdbc.Driver'):
            issue('isolation', '未确认 prod 保留 MySQL 裸 URL 和对应驱动。')
        if prod and 'mysql://' in prod_url:
            for key, expected in (('mybatis-plus.configuration.database-id', 'mysql'),
                                  ('mybatis-plus.extension.pagination.db-type', 'MYSQL')):
                actual, origin, n = prod.get(key, (None, source, 0))
                if actual != expected:
                    findings.append(Finding('MUST', 'isolation', origin, n,
                                            f'prod 使用 MySQL，但其有效 {key} 不是 {expected}；请隔离目标覆盖。'))
            key = 'spring.liquibase.change-log'
            if all(c.get('spring.liquibase.enabled', ('true',))[0] != 'false' for c in (prod, values)):
                def entry(c):
                    return os.path.normpath(re.sub(r'^classpath:/?', '', c.get(key, (DEFAULT_LOG,))[0]))
                if entry(prod) == entry(values):
                    issue('isolation', '目标 PG 与 MySQL prod 共用 Liquibase 入口，未隔离。', key)

        for config, tag in ((values, 'datasource'), (prod or {}, 'isolation')):
            if any(k.startswith(DS + 'dynamic.') or k in (DS + 'hikari.jdbc-url', DS + 'jndi-name') for k in config):
                issue(tag, '目标或 prod 存在动态数据源、连接池 URL 覆盖或 JNDI；无法仅凭标准 URL 验收。')
        url = values.get(DS + 'url', ('',))[0]
        if not re.fullmatch(r'jdbc:(?:p6spy:)?postgresql://[^\s]+', url):
            issue('datasource', '未确认目标 spring.datasource.url 是 PostgreSQL JDBC URL。')
            continue
        issue('datasource', '目标有效数据源 URL 使用 PostgreSQL。', level='INFO')
        if re.findall(r'(?:\?|&)stringtype=([^&#]*)', url) != ['unspecified']:
            issue('datasource', 'PG URL 必须有唯一的 stringtype=unspecified 参数。')
        driver = values.get(DS + 'driver-class-name', (None,))[0]
        expected = 'com.p6spy.engine.spy.P6SpyDriver' if url.startswith('jdbc:p6spy:') else 'org.postgresql.Driver'
        if profile == 'dev' and not url.startswith('jdbc:p6spy:'):
            issue('datasource', 'dev 必须保留 P6Spy JDBC 包装。')
        if driver != expected and (profile == 'dev' or driver is not None):
            issue('datasource', '目标有效驱动与 JDBC URL 不匹配。', DS + 'driver-class-name')
        baseline, origin, n = values.get('#' + DS + 'url', ('', source, 0))
        if not re.fullmatch(r'jdbc:(?:p6spy:)?mysql://[^\s]+', baseline):
            issue('parity', '目标 profile 未保留原 MySQL 数据源注释，无法核对四项沿用。')
        else:
            if baseline.startswith('jdbc:p6spy:'):
                backup_driver, driver_path, _ = values.get('#' + DS + 'driver-class-name', (None, None, 0))
                if driver_path != origin or backup_driver != 'com.p6spy.engine.spy.P6SpyDriver':
                    issue('parity', '原 MySQL P6Spy 数据源注释缺少对应驱动。', '#' + DS + 'url')
            mysql = dict(kind='mysql', commented=True, url=baseline, line=n)
            pg = dict(kind='postgres', commented=False, url=url, line=values[DS + 'url'][2])
            for key in ('username', 'password'):
                value, path, _ = values.get('#' + DS + key, (None, None, 0))
                mysql[key] = value if path == origin else None
                pg[key] = values.get(DS + key, (None,))[0]
            check_credential_parity(values[DS + 'url'][1], [mysql, pg], findings)



def check_mybatis(configs, findings):
    for _, source, values, _seq in configs:
        for key, expected in (("mybatis-plus.configuration.database-id", "pgsql"),
                              ("mybatis-plus.extension.pagination.db-type", "POSTGRE_SQL")):
            value, rp, line = values.get(key, (None, source, 0))
            valid = value == expected
            findings.append(Finding("INFO" if valid else "MUST", "mybatis", rp, line,
                                    f"{key} 已确认为 {expected}。" if valid else f"未确认 {key} 为 {expected}。"))


def dbms_runs(value, dialect):
    """按 Liquibase 语义判断 changeSet/sqlFile 的 dbms 是否命中目标库。

    支持逗号分隔的库名、`!` 排除项以及 `all` / `none`。这里按 Liquibase 的数据库
    短名精确匹配：`mysql` 与 `mariadb` 是两个不同的短名，不能按方言相近程度扩展别名，
    否则只标 `dbms: mariadb` 的脚本会被当成 MySQL 的活动迁移。
    """
    names = [n.strip().lower() for n in value.split(',') if n.strip()]
    if not names:
        return True
    target = dialect.lower()
    excluded = {n[1:].strip() for n in names if n.startswith('!')}
    if target in excluded:
        return False
    includes = [n for n in names if not n.startswith('!')]
    # 只有排除项（如 `!mysql`）时，目标库只要没被排除就应当执行。
    if not includes:
        return True
    if 'none' in includes:
        return False
    if 'all' in includes:
        return True
    return target in includes


def filter_runs(expression, active, kind):
    """判断 Liquibase 的 contextFilter / labels 是否命中当前激活集合。

    Liquibase 的过滤表达式支持 `a and b`、`a or b`、`!a`、`a, b`（逗号等于 or）与括号。
    这里只实现这几种形式做保守判定；遇到不认识的写法返回 None，由调用方报「无法确认」，
    而不是当作命中——否则把必需脚本限定在 staging 时，dev 验收仍会把它算进活动清单。

    `@name` 是「显式 context」：只有在运行时显式指定了该 context 才执行，不接受
    隐式继承或默认值。所以未配置 contexts 时 `@staging` 一律不跑，不能按命中处理。
    """
    text = (expression or '').strip()
    if not text:
        return True
    if active is None and '@' not in text:
        # 配置里没写 contexts / labels 时，Liquibase 不施加过滤；
        # 但 `@xxx` 要求显式指定，未配置就等于没指定，走下面的正常判定。
        return True

    def tokenize(s):
        out = []
        i = 0
        while i < len(s):
            ch = s[i]
            if ch.isspace() or ch == ',':
                if ch == ',':
                    out.append(('OR', ','))
                i += 1
                continue
            if ch in '()!':
                out.append((ch, ch))
                i += 1
                continue
            j = i
            while j < len(s) and not s[j].isspace() and s[j] not in '(),!':
                j += 1
            word = s[i:j]
            low = word.lower()
            out.append(('OP' if low in ('and', 'or') else 'NAME', low))
            i = j
        return out

    tokens = tokenize(text)

    def is_name(tok_value):
        """`@name` 只在显式配置了该 context 时命中；未配置 contexts 时一律不命中。"""
        if tok_value.startswith('@'):
            name = tok_value[1:]
            return active is not None and name in active
        return active is not None and tok_value in active

    pos = [0]

    def peek():
        return tokens[pos[0]] if pos[0] < len(tokens) else (None, None)

    def take():
        tok = peek()
        pos[0] += 1
        return tok

    def parse_or():
        left = parse_and()
        while peek()[0] in ('OP', 'OR') and peek()[1] in ('or', ','):
            take()
            right = parse_and()
            left = left or right
        return left

    def parse_and():
        left = parse_unary()
        while peek()[0] == 'OP' and peek()[1] == 'and':
            take()
            right = parse_unary()
            left = left and right
        return left

    def parse_unary():
        kind_tok, val = peek()
        if kind_tok == '!':
            take()
            return not parse_unary()
        if kind_tok == '(':
            take()
            inner = parse_or()
            if peek()[0] != ')':
                raise ValueError('括号不匹配')
            take()
            return inner
        if kind_tok == 'NAME':
            take()
            return is_name(val)
        raise ValueError('无法识别的表达式')

    try:
        result = parse_or()
        if pos[0] != len(tokens):
            return None
    except (ValueError, IndexError):
        return None
    return result


def check_liquibase(root, configs, findings, dialect='postgresql'):
    """Liquibase changelog 引用的 SQL 脚本方言。

    只在「确认引用了方言 SQL 脚本」时才要求 PG 版。若 changelog 全是原生 changeSet
    （createTable / addColumn 等由 Liquibase 自行生成方言 SQL），则不受影响。
    """
    for directory, source, values, seq_scalars in configs:
        if values.get('spring.liquibase.enabled', ('true',))[0].lower() != 'true':
            findings.append(Finding('MUST', 'liquibase', source, 0, '未确认 Liquibase 启用，不能验收活动迁移入口。'))
            continue
        # Liquibase 默认复用业务数据源；一旦显式配了 url，就会连到完全不同的库，
        # 此时配置层「切到 PG」也不代表迁移脚本会跑在同一个库上。
        override = values.get('spring.liquibase.url', (None,))[0]
        if override is not None:
            origin, line = values['spring.liquibase.url'][1], values['spring.liquibase.url'][2]
            if not re.fullmatch(rf'jdbc:(?:p6spy:)?{dialect}://[^\s]+', override):
                findings.append(Finding(
                    'MUST', 'liquibase', origin, line,
                    f'spring.liquibase.url 未指向 {dialect}，迁移会在另一个库上执行。'))
                continue
            # 协议相同还不够：同服务器上的另一个库同样会把迁移跑错地方。
            business = values.get(DS + 'url', (None,))[0]
            lhs, rhs = parse_url(business or ''), parse_url(override)
            if not lhs or not rhs:
                findings.append(Finding(
                    'MUST', 'liquibase', origin, line,
                    '无法静态确认 spring.liquibase.url 与业务数据源是否同库，请人工核对。'))
                continue
            # 比表达式而非只比默认值：${APP_PORT:5432} 与 ${MIGRATION_PORT:5432} 的默认
            # 值相同，但运行时可以分别覆写成不同实例；两个无默认值变量也不该算一致。
            diff = []
            for label, key in (('地址', 'host'), ('端口', 'port'), ('库名', 'db')):
                left, right = lhs.get(key + '_raw'), rhs.get(key + '_raw')
                if left is None or right is None:
                    diff.append(f'{label}（无法静态确认）')
                elif left != right:
                    diff.append(label)
            if diff:
                findings.append(Finding(
                    'MUST', 'liquibase', origin, line,
                    'spring.liquibase.url 与业务数据源的' + '、'.join(diff)
                    + '不一致，迁移会在另一个库上执行（具体值已省略）。'))
                continue
            # 同库同 schema 才算同一个迁移目标：schema 不一致时，迁移建的表在业务连接
            # 的默认 schema 里找不到。
            b_schema = url_param(business, 'currentSchema')
            l_schema = url_param(override, 'currentSchema')
            if b_schema != l_schema:
                findings.append(Finding(
                    'MUST', 'liquibase', origin, line,
                    'spring.liquibase.url 与业务数据源的 currentSchema 不一致，'
                    '迁移会在另一个 schema 上执行。'))
                continue
        # Liquibase 复用业务数据源时也能用 default-schema 单独指定迁移的默认 schema，
        # 这条路径不经过上面的 URL 覆盖分支，必须独立核对，否则同库不同 schema 会漏判。
        default_schema, schema_path, schema_line = values.get(
            'spring.liquibase.default-schema', (None, source, 0))
        if default_schema:
            business_schema = url_param(values.get(DS + 'url', (None,))[0], 'currentSchema')
            if (business_schema or 'public') != default_schema:
                findings.append(Finding(
                    'MUST', 'liquibase', schema_path, schema_line,
                    'spring.liquibase.default-schema 与业务连接的 schema 不一致，'
                    '迁移建的表在业务查询的 schema 里找不到。'))
        entry, source, line = values.get('spring.liquibase.change-log', (
            'classpath:/db/changelog/db.changelog-master.yaml', source, 0))
        resource_dir = os.path.dirname(directory) if os.path.basename(directory) == 'config' else directory
        path = resource_path(resource_dir, entry, resource_dir)
        if dialect == 'mysql' and path != resource_path(resource_dir, DEFAULT_LOG, resource_dir):
            findings.append(Finding('MUST', 'isolation', source, line, '未确认 prod 保留原 master 入口。'))
        if not path or not path.endswith(('.yml', '.yaml')) or not os.path.isfile(path):
            findings.append(Finding('MUST', 'liquibase', source, line,
                                    '目标 changelog 不存在或无法静态检查。'))
            continue
        rp = rel(path, root)
        lines = read(path, root).splitlines()
        if not any(re.match(r'^databaseChangeLog\s*:', s.strip()) for s in lines):
            findings.append(Finding('MUST', 'liquibase', rp, 0, '入口缺少 databaseChangeLog。'))
            continue
        refs = []
        kinds = []
        tag = 'liquibase' if dialect == 'postgresql' else 'isolation'

        def active_set(key):
            """读取配置里的 contexts / labels（活动集合）。

            返回 None 表示「未配置，不施加过滤」。支持两种写法：`contexts: dev,staging`
            这样的标量串，以及 `contexts:` 下面写 `- dev` 的 YAML 列表。
            """
            raw, _p, _l = values.get(key, (None, None, 0))
            parts = set()
            if raw:
                parts.update(p.strip() for p in re.split(r'[,:\s]+', raw) if p.strip())
            parts.update(seq_scalars.get(key) or ())
            return parts or None

        def filter_expr(key):
            """读取 `spring.liquibase.contexts` / `label-filter` 这类过滤表达式。

            `label-filter` 与 `labels` 语义不同：它是**运行时表达式**，对每个 changeset
            的标签集合求值，不是「活动标签集合」。写成 `dev` 表示只跑带 dev 标签的
            changeset，此时标了 staging 的必需脚本不会执行。这里原样返回表达式，交给
            filter_runs 按 direction 求值。
            """
            raw, _p, _l = values.get(key, (None, None, 0))
            if raw:
                return raw
            items = seq_scalars.get(key)
            if items:
                return ','.join(items)
            return None

        liquibase_contexts = active_set('spring.liquibase.contexts')
        liquibase_labels = active_set('spring.liquibase.labels')
        # label-filter 是对 changeset 标签求值的表达式；Spring Boot 的键名带连字符。
        label_filter = filter_expr('spring.liquibase.label-filter')
        # 每个 include/sqlFile 独立收集属性；开关可出现在路径前或后。
        stack = []
        items = {}
        meta = {}
        dbms = {}
        ignored = set()
        for i, line in enumerate(lines, 1):
            match = re.match(r'^(\s*)(-\s+)?([\w-]+):\s*(.*?)\s*$', line)
            if not match:
                continue
            spaces, dash, key, value = match.groups()
            indent = len(spaces) + len(dash or '')
            while stack and stack[-1][0] >= indent:
                stack.pop()
            value = re.split(r'\s+#', value, maxsplit=1)[0].strip('\"\'')
            # rollback 子树里的引用是回滚动作，不计入正向迁移清单。
            in_rollback = any(s[2] == 'rollback' for s in stack)
            # 只有挂在 databaseChangeLog 下的引用才是活动迁移入口。`databaseChangeLog: []`
            # 之后另起一个顶层列表时，栈里没有这个祖先，那些 include 不会被执行。
            in_changelog = any(s[2] == 'databaseChangeLog' for s in stack)
            # 祖先里有 databaseChangeLog 还不够：Liquibase 只认根 `databaseChangeLog`
            # 的直接 include。放进 changeSet.changes 的 include 不会被当作迁移执行，
            # 只按「祖先存在」放行会让这种错误结构也通过清单校验。
            direct_include = bool(stack) and stack[-1][2] == 'databaseChangeLog'
            enclosing = next((s[1] for s in reversed(stack) if s[2] == 'changeSet'), None)
            if key in ('file', 'path', 'relativeToChangelogFile', 'ignore',
                       'contextFilter', 'context', 'labels', 'labelFilter'):
                if stack and (stack[-1][2], key) in (
                    ('include', 'file'), ('sqlFile', 'path'),
                    ('include', 'relativeToChangelogFile'), ('sqlFile', 'relativeToChangelogFile'),
                    ('include', 'ignore'),
                    ('include', 'contextFilter'), ('sqlFile', 'contextFilter'),
                    ('include', 'context'), ('sqlFile', 'context'),
                    ('include', 'labels'), ('sqlFile', 'labels'),
                    ('include', 'labelFilter'), ('sqlFile', 'labelFilter'),
                ):
                    items.setdefault(stack[-1][1], {})[key] = (value, i)
                    if key == 'ignore' and value.lower() == 'true':
                        ignored.add(stack[-1][1])
            elif key == 'dbms' and stack and stack[-1][2] in ('changeSet', 'sqlFile'):
                # sqlFile 自己也支持 dbms，与 changeSet 是两层条件，必须分别记下来。
                dbms[stack[-1][1]] = value.lower()
            elif key in ('include', 'sqlFile', 'rollback'):
                if in_rollback:
                    continue
                if key in ('include', 'sqlFile') and not in_changelog:
                    # 不在 databaseChangeLog 下的引用不会进入迁移，不能计入清单。
                    findings.append(Finding('MUST', tag, rp, i,
                                            '该引用不在 databaseChangeLog 下，不会被 Liquibase 执行。'))
                    stack.append((indent, i, key))
                    continue
                if key == 'include' and not direct_include:
                    # include 出现在 changeSet / changeSet.changes 下时 Liquibase 不会执行它，
                    # 但路径、顺序和类型都能通过下面的清单校验，必须在这里阻塞。
                    findings.append(Finding('MUST', tag, rp, i,
                                            'include 必须是 databaseChangeLog 的直接子项；'
                                            '放在 changeSet 等位置不会被执行。'))
                    stack.append((indent, i, key))
                    continue
                if key in ('include', 'sqlFile'):
                    items[i] = {}
                    # 记录所属 changeSet 与引用类型：dbms 不匹配时该引用不会执行；
                    # 类型决定 Liquibase 用的是被包含 changelog 的身份还是外层 changeSet 的身份。
                    meta[i] = enclosing
                stack.append((indent, i, key))
            elif key == 'includeAll':
                if not in_rollback:
                    findings.append(Finding('MUST', tag, rp, i, '无法静态确认 includeAll 的加载清单。'))
                stack.append((indent, i, key))
            else:
                # 只有块形式的键（后面跟缩进子节点）才压栈。`databaseChangeLog: []`
                # 这类带行内值的键本身没有子节点，压栈会让紧随其后的顶层列表
                # 被误认为它的子项并计入活动清单。
                if value == '':
                    stack.append((indent, i, key))
        for parent_line, fields in items.items():
            if parent_line in ignored:
                findings.append(Finding('MUST', tag, rp, fields.get(
                    'ignore', ('', parent_line))[1],
                    '该 include 标记了 ignore: true，清单里的脚本不会被执行。'))
                continue
            ref, i = fields.get('file', fields.get('path', ('', parent_line)))
            if not ref:
                findings.append(Finding('MUST', tag, rp, i, '迁移引用缺少可确认的路径。'))
                continue
            owner = meta.get(parent_line)
            if owner and dbms.get(owner) and not dbms_runs(dbms[owner], dialect):
                findings.append(Finding('MUST', tag, rp, i,
                                        f'脚本所属 changeSet 的 dbms={dbms[owner]}，在 {dialect} 下不会执行。'))
                continue
            own_dbms = dbms.get(parent_line)
            if own_dbms and not dbms_runs(own_dbms, dialect):
                findings.append(Finding('MUST', tag, rp, i,
                                        f'该 sqlFile 的 dbms={own_dbms}，在 {dialect} 下不会执行。'))
                continue
            # 限定了 contextFilter / labels 的脚本只在匹配时才执行。判成不命中就阻塞：
            # 必需建表脚本若限定在 staging，而目标 profile 的 contexts 是 dev，
            # Liquibase 根本不会跑它，把这个引用算进清单等于确认了没跑过的迁移。
            #
            # 两类过滤方向不同，不能混用同一套集合匹配：
            #   · contextFilter / context 与「活动 context 集合」比对（运行时选择跑哪些）；
            #   · labels 是 changeset 自身的标签集合，由运行时 label-filter 表达式筛选。
            # 因此有 label-filter 时用它来判定 labels；没有时退化用 labels 集合。
            filter_ok = True
            filter_bad = None
            for field, active, kind in (
                ('contextFilter', liquibase_contexts, 'context'),
                ('context', liquibase_contexts, 'context'),
                ('labels', liquibase_labels, 'label'),
                ('labelFilter', liquibase_labels, 'label'),
            ):
                if field not in fields:
                    continue
                expr = fields[field][0]
                verdict = filter_runs(expr, active, kind)
                # label 一侧改用运行时 label-filter 求值：表达式写了 `dev` 时，
                # 标 staging 的 changeset 不会执行，即便 labels 集合本身配置了 staging。
                if verdict and kind == 'label' and label_filter is not None:
                    verdict = filter_runs(label_filter, {n.strip().lower()
                                                         for n in re.split(r'[,:\s]+', expr) if n.strip()},
                                          'label')
                if verdict is None:
                    filter_bad = (field, expr, '无法静态确认')
                    filter_ok = False
                    break
                if not verdict:
                    filter_bad = (field, expr, '不会执行')
                    filter_ok = False
                    break
            if not filter_ok:
                field, expr, why = filter_bad
                findings.append(Finding('MUST', tag, rp, fields[field][1],
                                        f'该 include 的 {field}={expr} 与目标 profile 的过滤配置'
                                        f'不匹配（{why}），清单里的脚本不会被执行。'))
                continue
            relative = fields.get('relativeToChangelogFile', ('false',))[0].lower()
            if relative not in ('true', 'false'):
                findings.append(Finding('MUST', tag, rp, i, '无法确认 relativeToChangelogFile。'))
                continue
            base = os.path.dirname(path) if relative == 'true' else resource_dir
            actual = resource_path(base, ref, resource_dir)
            if not actual:
                findings.append(Finding('MUST', tag, rp, i, '脚本路径超出资源目录。'))
                continue
            ref = rel(actual, os.path.realpath(resource_dir))
            if not ref.endswith('.sql'):
                findings.append(Finding('MUST', 'liquibase', rp, i,
                                        '无法确认嵌套引用的 SQL 方言，请核对。'))
                continue
            refs.append(ref)
            kinds.append('include' if fields.get('file') else 'sqlFile')
            if dialect not in ref.split('/') or not os.path.isfile(actual):
                findings.append(Finding('MUST', tag, rp, i, f'未确认此引用指向有效的 {dialect} SQL 脚本。'))
            elif not read(actual, root).strip():
                findings.append(Finding('WARN', tag, rel(actual, root), 0,
                                        '迁移脚本为空，切换后不会建表；属既有 SQL 资产，需人工补齐。'))
            elif dialect == 'postgresql':
                # 既有 SQL 资产不在本技能修改范围，只提示，不阻塞配置验收。
                body = read(actual, root)
                for offset, desc in mysql_hits(body):
                    findings.append(Finding('WARN', tag, rel(actual, root),
                                            body.count('\n', 0, offset) + 1,
                                            f'{desc}；属既有 SQL 资产，本技能不改语句，需另评估。'))
        if not refs and not any(re.match(r'^\s*-\s+changeSet\s*:', s) for s in lines):
            findings.append(Finding('MUST', tag, rp, 0, '入口没有可确认的活动脚本或 changeSet。'))
        # 入口里夹带原生 changeSet（createTable / dropTable 等）同样会执行，但它既不在
        # include 清单里、也没有可核对的 SQL 脚本，无法确认它属于预期迁移。只比 refs
        # 会让「六个 include 之间插一个 dropTable」悄悄通过。
        # 两种写法都要认：块形式 `- changeSet:` 与行内形式 `- {changeSet: {...}}`。
        native = [n for n, s in enumerate(lines, 1)
                  if re.match(r'^\s*-\s+changeSet\s*:', s)
                  or re.match(r'^\s*-\s*\{\s*changeSet\s*:', s)]
        if native:
            findings.append(Finding('MUST', tag, rp, native[0],
                                    '入口包含原生 changeSet，无法确认属于预期迁移清单；'
                                    '请只保留 include 的 SQL 脚本入口。'))
        if refs != [f'db/changelog/{dialect}/{name}' for name in REQUIRED_SCRIPTS]:
            findings.append(Finding('MUST', tag, rp, 0, '活动脚本的路径或顺序不符合本仓迁移清单。'))
        elif any(kind != 'include' for kind in kinds):
            # sqlFile 由外层 changeSet 身份执行，不会沿用被包含 changelog 里已发布的
            # changeset 身份，可能导致迁移重跑；本仓清单要求用 include。
            findings.append(Finding('MUST', tag, rp, 0,
                                    '迁移清单中的引用须为 include；用 sqlFile 会改变 changeset 身份。'))
        if all(dialect in ref.split('/') for ref in refs):
            yield directory, [re.sub(rf'(^|/){dialect}/', r'\1', ref) for ref in refs]


def _parse_pom(path, root):
    """解析 POM 文件；失败返回 (None, 原因)。注释里的依赖不算声明。"""
    raw = read(path, root)
    try:
        return ET.fromstring(raw), raw
    except ET.ParseError as exc:
        return None, exc


def _parent_pom_path(pom, path):
    """按 Maven 的规则找父 POM：先看 relativePath，再查本地仓库。"""
    parent = pom.find('{*}parent')
    if parent is None:
        return None
    group = (parent.findtext('{*}groupId') or '').strip()
    artifact = (parent.findtext('{*}artifactId') or '').strip()
    version = (parent.findtext('{*}version') or '').strip()
    if not artifact:
        return None
    rel = parent.findtext('{*}relativePath')
    rel = '../pom.xml' if rel is None else rel.strip()
    if rel:
        candidate = os.path.normpath(os.path.join(os.path.dirname(path), rel))
        if os.path.isfile(candidate):
            return candidate
    # relativePath 只是优先项：starter 这类前置依赖在本仓没有源码目录时，
    # 该路径自然不存在，必须回退到本地仓库，否则受管 scope 永远查不到。
    if not group or not version or '${' in version:
        return None
    sub = os.path.join(group.replace('.', os.sep), artifact, version,
                       f'{artifact}-{version}.pom')
    for repo in MAVEN_REPOS:
        candidate = os.path.join(os.path.expanduser(repo), sub)
        if os.path.isfile(candidate):
            return candidate
    return None


def _pom_properties(pom):
    """收集 POM 的 <properties>；用于展开 BOM 导入里 `${xxx.version}` 这类引用。"""
    props = {}
    node = pom.find('{*}properties')
    if node is None:
        return props
    for child in node:
        tag = child.tag.split('}')[-1]
        if child.text and child.text.strip():
            props[tag] = child.text.strip()
    return props


def _resolve_version(version, props, depth=0):
    """把 `${x.version}` 展开成字面量；展开不了返回 None。"""
    if not version:
        return None
    if '${' not in version:
        return version
    if depth > 4:
        return None
    m = re.fullmatch(r'\$\{([^}]+)\}', version.strip())
    if not m:
        # 形如 `1.0-${suffix}` 的拼接不在本技能要处理的范围内，按无法静态确认处理。
        return None
    key = m.group(1)
    inner = props.get(key)
    if inner is None:
        return None
    if '${' in inner:
        return _resolve_version(inner, props, depth + 1)
    return inner


def _repo_pom_path(group, artifact, version):
    """在本地仓库里定位指定坐标的 POM；找不到返回 None。"""
    if not group or not artifact or not version or '${' in version:
        return None
    sub = os.path.join(group.replace('.', os.sep), artifact, version,
                       f'{artifact}-{version}.pom')
    for repo in MAVEN_REPOS:
        candidate = os.path.join(os.path.expanduser(repo), sub)
        if os.path.isfile(candidate):
            return candidate
    return None


def _managed_entries(pom):
    """取出 POM 自身的 dependencyManagement 条目：(坐标, scope, type, version)。"""
    entries = []
    for dep in pom.findall('./{*}dependencyManagement/{*}dependencies/{*}dependency'):
        coord = ((dep.findtext('{*}groupId') or '').strip(),
                 (dep.findtext('{*}artifactId') or '').strip())
        scope = (dep.findtext('{*}scope') or '').strip()
        dtype = (dep.findtext('{*}type') or '').strip()
        version = (dep.findtext('{*}version') or '').strip()
        entries.append((coord, scope, dtype, version))
    return entries



def managed_scopes(path, root):
    """收集「有效 POM」里对 (groupId, artifactId) 的受管 scope。

    直接依赖省略 scope 时，Maven 用的是 dependencyManagement 里的受管 scope，而不是
    一律按 compile。受管 scope 为 test / provided 时驱动根本进不了运行时 classpath，
    照 compile 判定等于确认一个部署时并不存在的驱动。受管值解析不出来时返回的映射里
    就没有该坐标，由调用方报「无法确认」，而不是默认放行。

    `scope=import` + `type=pom` 的条目是 BOM 导入：Maven 会把这些 BOM 的
    dependencyManagement 并进有效 POM。Spring Boot 的驱动版本就是这条路径来的，
    只扫当前 POM 的 dependencyManagement 会把省略 scope 的驱动判成「受管值未知」，
    从而把本来正常的 compile 依赖误报成不可用。
    """
    scopes = {}
    # 自身与父链按「近者优先」收集；BOM 导入的条目只在未被更近来源定义时补进来。
    # 同时记下每个 POM 的文件路径：访问状态按路径判重，不依赖 Element 的 id。
    chain = []
    visited = set()
    current = path
    for _ in range(8):
        if not current or current in visited:
            break
        visited.add(current)
        pom, _raw = _parse_pom(current, root)
        if pom is None:
            break
        chain.append((pom, current))
        current = _parent_pom_path(pom, current)

    # BOM 导入的版本多写成 `${spring-boot.version}`，属性定义在声明 BOM 的 POM 或父链上。
    props = {}
    for pom, _p in reversed(chain):
        props.update(_pom_properties(pom))

    def explicit_entries(pom):
        """本 POM 自己写的非导入受管条目，按声明顺序。"""
        return [(coord, scope, dtype, version) for coord, scope, dtype, version
                in _managed_entries(pom)
                if not (scope == 'import' and (dtype or 'jar') == 'pom')]

    def bom_paths(pom, local):
        """本 POM 导入的 BOM，按 import 声明顺序。"""
        out = []
        for coord, scope, dtype, version in _managed_entries(pom):
            if scope == 'import' and (dtype or 'jar') == 'pom':
                resolved = _resolve_version(version, local)
                bom = _repo_pom_path(coord[0], coord[1], resolved)
                if bom:
                    out.append(bom)
        return out

    def collect(pom, seen, inherited, pom_path=None):
        """展开受管条目。

        顺序是 Maven 的优先次序：**本 POM 的显式条目优先于它导入的 BOM**。
        同一份 dependencyManagement 里先 import BOM、再把同一坐标显式写成 test 时，
        生效的是本 POM 的 test。先递归 BOM 再收本 POM 会让 BOM 的 compile 抢占，
        把部署时根本不存在的驱动判成运行时可用。

        访问状态按**规范化文件路径**记录，不用 `id(pom)`：每次导入都会重新解析出
        新的 Element，同一文件的 id 每次都不同，BOM 互相导入时会无限递归直到
        RecursionError；Element 被回收后 id 还可能被别的对象复用，导致真的漏查。
        """
        key = os.path.normcase(os.path.realpath(pom_path)) if pom_path else id(pom)
        if key in seen:
            return
        seen.add(key)
        local = dict(inherited)
        local.update(_pom_properties(pom))
        # 第一轮：本 POM 的显式条目先占位。
        for coord, scope, dtype, version in explicit_entries(pom):
            if coord not in scopes:
                scopes[coord] = scope or 'compile'
        # 第二轮：才用导入的 BOM 补缺。
        for bom in bom_paths(pom, local):
            imported, _raw = _parse_pom(bom, root)
            if imported is not None:
                collect(imported, seen, local, bom)

    seen = set()
    for pom, pom_path in chain:
        collect(pom, seen, props, pom_path)
    return scopes


def check_driver_dependency(root, findings, mysql_prod=False):
    """JDBC 驱动依赖检查。

    以 XML 解析结果为唯一判定依据，行匹配只用来定位行号：
      · 只看 compile / runtime scope；test / provided（如 Testcontainers 用的 MySQL）不算。
      · `<dependencyManagement>` 里的坐标只管理版本，不进 classpath。
      · `type` 非 jar、带 classifier、`<optional>true</optional>` 都不会把驱动传给
        依赖本模块的应用，不能算作已确认的运行时驱动。
      · 多数插件/业务模块本身不声明驱动，靠 common 传递，也不要给它们加。

    **关于注销 MySQL 驱动**：本 skill 只切目标 profile（默认 dev）。若同一应用还有
    其它 profile 仍连 MySQL（如 prod），共享 POM 就必须同时保留两个驱动，此时
    强制注销会让那些环境无法启动。因此这里只给「建议」而非「必须」。
    """
    RUNTIME_SCOPES = {"compile", "runtime"}
    DRIVERS = {
        ('org.postgresql', 'postgresql'): 'PG',
        ('com.mysql', 'mysql-connector-j'): 'MySQL',
        ('mysql', 'mysql-connector-java'): 'MySQL',
    }
    supported = set()

    for path in walk(root):
        if rel(path, root) != 'continew-common/pom.xml':
            continue
        rp = rel(path, root)
        raw = read(path, root)
        try:
            pom = ET.fromstring(raw)
        except ET.ParseError as exc:
            findings.append(Finding('MUST', 'dependency', rp, exc.position[0], 'POM 无法解析。'))
            continue

        # 省略 scope 时取有效 POM 的受管值；解析不到就不按下述「compile」放行。
        managed = managed_scopes(path, root)

        # XML 解析出的坐标 → (kind, 是否可传递到运行时, 说明)
        status = {}
        for dep in pom.findall('./{*}dependencies/{*}dependency'):
            group, artifact, scope, dtype = (
                (dep.findtext('{*}' + k) or '').strip()
                for k in ('groupId', 'artifactId', 'scope', 'type'))
            classifier = (dep.findtext('{*}classifier') or '').strip()
            optional = (dep.findtext('{*}optional') or '').strip().lower()
            kind = DRIVERS.get((group, artifact))
            if not kind:
                continue
            effective = scope or managed.get((group, artifact))
            problem = None
            if effective is None:
                problem = '未声明 scope，且父 POM 的受管 scope 无法解析，不能确认是运行时依赖'
            elif effective not in RUNTIME_SCOPES:
                problem = (f'有效 scope={effective}' + ('（由 dependencyManagement 受管）' if not scope else '')
                           + ' 不是运行时依赖')
            elif (dtype or 'jar') != 'jar':
                problem = f'type={dtype} 不是 jar，不含驱动类'
            elif classifier:
                problem = f'classifier={classifier} 不是默认驱动 JAR'
            elif optional == 'true':
                problem = '标记为 optional，不会传递给依赖本模块的应用'
            status[(group, artifact)] = (kind, problem is None, problem)
            if problem is None:
                supported.add((group, artifact))

        managed_any = bool(_dependency_management_coords(pom) & set(DRIVERS))
        if not status:
            if managed_any:
                findings.append(Finding(
                    'INFO', 'dependency', rp, 0,
                    '驱动坐标仅出现在 dependencyManagement 中（只管理版本，不进运行时 classpath），'
                    '不影响本次切换。'))
            continue

        def line_of(artifact):
            """行号仅用于定位，判定仍以 XML 为准。"""
            for i, line in enumerate(strip_xml_comments(raw).splitlines(), 1):
                if f'<artifactId>{artifact}</artifactId>' in line:
                    return i
            return 0

        pg_ok = [st for st in status.values() if st[0] == 'PG' and st[1]]
        pg_note = [st[2] for st in status.values() if st[0] == 'PG' and not st[1]]
        mysql_ok = [st for st in status.values() if st[0] == 'MySQL' and st[1]]

        if not pg_ok:
            extra = f"（已声明但不可用：{'；'.join(pg_note)}）" if pg_note else ''
            if not pg_note and managed_any:
                extra = '（dependencyManagement 里的坐标不算）'
            findings.append(Finding(
                'MUST', 'dependency', rp, 0,
                '没有可传递到运行时的 PostgreSQL 驱动依赖' + extra
                + '；否则运行期报 Unable to find a driver that accepts jdbc:postgresql://...'))
        if mysql_ok:
            findings.append(Finding(
                'WARN', 'dependency', rp, line_of('mysql-connector-j') or line_of('mysql-connector-java'),
                'MySQL 驱动仍以运行时 scope 生效。若所有运行环境都已切到 PostgreSQL，'
                '注销它可以减少 classpath 与包体；但只要还有其它 profile（如 prod）连 MySQL，'
                '就必须保留——**注销前先确认没有其它环境依赖它**。'))
        if pg_ok and not mysql_ok:
            findings.append(Finding(
                'INFO', 'dependency', rp, line_of('postgresql'),
                'PostgreSQL 驱动以运行时 scope 生效，无 MySQL 运行时驱动。'))

    if ('org.postgresql', 'postgresql') not in supported:
        findings.append(Finding('MUST', 'dependency', 'continew-common/pom.xml', 0,
                                '未确认可传递的 PostgreSQL 运行时驱动。'))
    if mysql_prod and ('com.mysql', 'mysql-connector-j') not in supported \
            and ('mysql', 'mysql-connector-java') not in supported:
        findings.append(Finding('MUST', 'isolation', 'continew-common/pom.xml', 0,
                                'prod 仍使用 MySQL，未确认保留其运行时驱动。'))


def _dependency_management_coords(pom):
    """dependencyManagement 里声明的 (groupId, artifactId)。"""
    return {
        ((dep.findtext('{*}groupId') or '').strip(),
         (dep.findtext('{*}artifactId') or '').strip())
        for dep in pom.findall('./{*}dependencyManagement/{*}dependencies/{*}dependency')}


def strip_xml_comments(text):
    """把 XML 注释替换为等长空格，保留换行，便于按行号比对。"""
    def blank(m):
        return re.sub(r"[^\n]", " ", m.group(0))
    return re.sub(r"<!--.*?-->", blank, text, flags=re.S)


MYSQL_PATTERNS = [
    (r"`\w+`", "反引号标识符（PG 用双引号）"),
    (r"\bIFNULL\s*\(", "IFNULL（PG 用 COALESCE）"),
    (r"\bGROUP_CONCAT\s*\(", "GROUP_CONCAT（PG 用 STRING_AGG）"),
    (r"\bDATE_FORMAT\s*\(", "DATE_FORMAT（PG 用 to_char）"),
    (r"\bSTR_TO_DATE\s*\(", "STR_TO_DATE（PG 用 to_timestamp）"),
    (r"\bON\s+DUPLICATE\s+KEY", "ON DUPLICATE KEY UPDATE（PG 用 ON CONFLICT）"),
    (r"\bLIMIT\s+(?:\d+|#\{[^}]*}|\?)\s*,\s*(?:\d+|#\{[^}]*}|\?)",
     "LIMIT a,b（PG 用 LIMIT b OFFSET a，含参数化分页）"),
    (r"\bENGINE\s*=", "ENGINE= 表选项"),
    (r"\bDEFAULT\s+CHARSET\s*=", "DEFAULT CHARSET 表选项"),
    (r"\bAUTO_INCREMENT\b", "AUTO_INCREMENT（PG 用 GENERATED ... AS IDENTITY）"),
]


SQL_SKIP = re.compile(r"--[^\n]*|/\*|(?<![\w$])\$(?P<tag>[A-Za-z_]\w*|)\$.*?\$(?P=tag)\$|"
                      r"\b[Ee]'(?:\\.|''|[^'\\])*'|'(?:''|[^'])*'|\"(?:\"\"|[^\"])*\"", re.S)
COMMENT_EDGE = re.compile(r'/\*|\*/')


def mysql_hits(sql):
    """产出 (命中的字符偏移, 说明)。

    返回字符偏移而不是行号：调用方要按原文（含被屏蔽的注释/字面量）自己换算行号，
    否则拼接出来的字符串无法映射回源码位置。
    """
    # 保留词边界；PG 块注释允许嵌套。
    chunks = []
    pos = 0
    while match := SQL_SKIP.search(sql, pos):
        chunks.append(sql[pos:match.start()])
        pos = match.end()
        if match[0] == '/*':
            nesting = 1
            for edge in COMMENT_EDGE.finditer(sql, pos):
                nesting += 1 if edge[0] == '/*' else -1
                if not nesting:
                    pos = edge.end()
                    break
            else:
                pos = len(sql)
        chunks.append(re.sub(r'[^\n]', ' ', sql[match.start():pos]))
    clean = ''.join(chunks) + sql[pos:]
    for pattern, desc in MYSQL_PATTERNS:
        for match in re.finditer(pattern, clean, re.I):
            yield match.start(), desc


def check_mapper_dialect(root, findings):
    statements, fragments = {}, {}
    for path in walk(root):
        if not path.endswith('.xml'):
            continue
        text = read(path, root)
        if not path.endswith('Mapper.xml') and not re.search(r'<mapper\b', text):
            continue
        # 测试资源与运行时 Mapper 共用 namespace + id 索引：测试定义若后遍历到，
        # 会覆盖主资源里真正部署的语句，把 MySQL 专有写法藏起来。
        if is_test_resource(path, root):
            continue
        rp = rel(path, root)
        try:
            mapper = ET.fromstring(text)
        except ET.ParseError as exc:
            findings.append(Finding('MUST', 'mapper', rp, exc.position[0], 'Mapper XML 无法解析。'))
            continue
        ns = mapper.get('namespace', '')
        for node in mapper:
            if node.tag not in ('sql', 'select', 'insert', 'update', 'delete') or not node.get('id'):
                continue
            index = fragments if node.tag == 'sql' else statements
            index.setdefault(ns + '.' + node.get('id'), {})[node.get('databaseId')] = (node, ns, rp)

    def guard(node):
        m = re.fullmatch(r"\s*_databaseId\s*(==|!=)\s*(['\"])(\w+)\2\s*", node.get('test', ''))
        if m:
            return (m[3] == 'pgsql') == (m[1] == '==')
        return None

    for sid, versions in statements.items():
        selected = versions.get('pgsql') or versions.get(None)
        if not selected:
            if 'mysql' in versions:
                findings.append(Finding('WARN', 'mapper', versions['mysql'][2], 0,
                                        f'{sid} 只有 MySQL 实现；属既有 SQL 资产，本技能不改语句，需另评估。'))
            continue

        def issue(rp, message):
            findings.append(Finding('WARN', 'mapper', rp, 0,
                                    f'{sid}：{message}（属既有 SQL 资产，本技能不改语句）'))

        def visit(node, ns, rp, chain):
            if node.tag == 'include':
                ref = node.get('refid', '')
                key = ref if '.' in ref else ns + '.' + ref
                options = fragments.get(key, {})
                target = options.get('pgsql') or options.get(None)
                if not target or key in chain:
                    issue(rp, '引用片段无法解析或存在循环。')
                else:
                    yield from visit(*target, chain | {key})
                return
            if node.tag in ('if', 'when') and guard(node) is False:
                return
            yield node.text or ''
            if node.tag == 'choose':
                for child in node:
                    yield from visit(child, ns, rp, chain)
                    if child.tag == 'otherwise' or guard(child) is True:
                        break
            else:
                for child in node:
                    yield from visit(child, ns, rp, chain)
                    yield child.tail or ''

        sql = ' '.join(visit(*selected, set()))
        if not sql.strip():
            issue(selected[2], '目标 PG 分支没有可执行的 SQL。')
        for desc in sorted({desc for _, desc in mysql_hits(sql)}):
            issue(selected[2], desc)


CODE_TOKEN = re.compile(r'//[^\n]*|/\*.*?\*/|""".*?"""|"(?:\\.|[^"\\\n])*"|\'(?:\\.|[^\'\\\n])*\'', re.S)
SQL_HINT = re.compile(r'\b(select|insert|update|delete|from|where|join|values|create|alter)\b', re.I)


def check_raw_sql(root, findings, verbose):
    hits = {}
    for path in walk(root):
        if not path.endswith(('.java', '.kt')):
            continue
        text = read(path, root)
        tokens = [t for t in CODE_TOKEN.finditer(text) if t[0].startswith('"')]
        # 常量拼接（"SELECT " + "IFNULL(...)"）会让提示词和方言分散在两个字面量里，
        # 必须先把相邻且只用 `+` 与空白分隔的字面量合并，再做匹配。
        groups = []
        for token in tokens:
            if groups:
                gap = text[groups[-1][-1].end():token.start()]
                if re.fullmatch(r'\s*\+\s*', gap):
                    groups[-1].append(token)
                    continue
            groups.append([token])
        for group in groups:
            # 记录每个字面量的正文起点在拼接串中的区间，命中位置才能映射回
            # 原始 token 的源码行（字面量之间可能跨行）。
            pieces, spans, cursor = [], [], 0
            for t in group:
                body = t[0][3:-3] if t[0].startswith('"""') else t[0][1:-1]
                pieces.append(body)
                spans.append((cursor, cursor + len(body), t))
                cursor += len(body)
            sql = ''.join(pieces)
            if not SQL_HINT.search(sql):
                continue

            def src_line(pos):
                """把拼接串里的命中位置映射回源码行号。

                命中落在某个字面量内部时，用该 token 自己的源码偏移换算，
                不能直接用拼接串前缀的换行数——字面量之间可能跨行。
                """
                for a, b, t in spans:
                    if a <= pos < b or (a == b == pos):
                        width = 3 if t[0].startswith('"""') else 1
                        return text.count('\n', 0, t.start() + width + (pos - a)) + 1
                a, b, t = spans[-1]
                width = 3 if t[0].startswith('"""') else 1
                return text.count('\n', 0, t.start() + width + max(0, pos - a)) + 1

            for offset, desc in mysql_hits(sql):
                hits.setdefault(desc, []).append(f'{rel(path, root)}:{src_line(offset)}')
    for desc, items in hits.items():
        shown = items if verbose else items[:6]
        findings.append(Finding('WARN', 'dialect', '(代码扫描)', 0,
                                f'{desc}，共 {len(items)} 处：' + '、'.join(shown)))


def check_tests(root, findings):
    """测试代码里的 MySQL 容器或硬编码。

    注意：testcontainers-mysql 的 pom 依赖由 check_other_dialect_config 负责（能给出行号），
    这里只管「实际用到 MySQL 容器/客户端」的代码文件，避免同一条问题报两遍。
    """
    for path in walk(root):
        ext = os.path.splitext(path)[1]
        if ext not in CODE_EXT:
            continue
        if os.path.basename(path) == "pom.xml":
            continue  # 依赖声明交给 check_other_dialect_config
        rp = rel(path, root)
        text = read(path, root)
        if re.search(r"MySQLContainer|mysql:\d", text):
            findings.append(Finding(
                "WARN", "test", rp, 0,
                "测试代码使用 MySQL 容器（Testcontainers）。切库后集成测试仍连 MySQL，"
                "本次保留，不据此判定 dev 切换失败。"))
        elif re.search(r"\bmysql -u|\bmysql-test\b", text):
            findings.append(Finding(
                "WARN", "test", rp, 0,
                "脚本/测试引用了 mysql 客户端或容器名，仅记录，不属于本次修改范围。"))


def check_other_dialect_config(root, findings):
    """其他隐蔽的方言相关配置。"""
    for path in walk(root):
        base = os.path.basename(path)
        if base != 'pom.xml':
            continue
        rp = rel(path, root)
        text = read(path, root)
        for i, line in enumerate(text.splitlines(), 1):
            if "testcontainers-mysql" in line and "pom.xml" in base:
                findings.append(Finding(
                    "WARN", "dependency", rp, i,
                    "测试依赖 testcontainers-mysql，本次不修改测试容器或测试 profile。"))


def main():
    ap = argparse.ArgumentParser(description="扫描 MySQL→PostgreSQL 切换需要改动的接入点")
    ap.add_argument("root", help="项目根目录")
    ap.add_argument("-v", "--verbose", action="store_true", help="代码扫描不做截断")
    ap.add_argument("-p", "--profile", choices=("dev", "local"), default="dev",
                    help="目标 profile（默认 dev）；另核对仍使用 MySQL 的 prod 是否保持隔离。")
    args = ap.parse_args()

    root = os.path.abspath(args.root)
    if not os.path.isdir(root):
        print(f"目录不存在：{root}", file=sys.stderr)
        return 2

    findings = []
    READ_ERRORS.clear()
    configs = list(target_configs(root, args.profile))
    prod_configs = {d: v for d, source, v, _s in target_configs(root, 'prod')
                    if os.path.basename(source).startswith('application-prod.')}
    # prod 的 YAML 列表（`contexts:` 写成块列表）同样要带过去：丢掉它们会让
    # active_set() 得到 None，从而把仅限 staging 的 include 也算进 prod 的活动清单。
    prod_seqs = {d: s for d, source, _v, s in target_configs(root, 'prod')
                 if os.path.basename(source).startswith('application-prod.')}
    if configs and not any(os.path.splitext(os.path.basename(source))[0] == f'application-{args.profile}'
                           for _, source, _, _s in configs):
        findings.append(Finding('MUST', 'datasource', '(目标配置)', 0, '未找到目标 profile 文件，不能仅凭共享配置验收。'))
    check_effective_datasource(configs, prod_configs, findings, args.profile)
    check_mybatis(configs, findings)
    target_scripts = dict(check_liquibase(root, configs, findings))
    mysql_prod = [(d, rel(d, root), v, prod_seqs.get(d, {})) for d, v in prod_configs.items()
                  if 'mysql://' in v.get(DS + 'url', ('',))[0]]
    for directory, scripts in check_liquibase(root, mysql_prod, findings, 'mysql'):
        if scripts and scripts != target_scripts.get(directory):
            findings.append(Finding('MUST', 'isolation', rel(directory, root), 0,
                                    '目标与 MySQL prod 的脚本清单或加载顺序不一致。'))
    check_driver_dependency(root, findings, any('mysql://' in v.get(DS + 'url', ('',))[0] for v in prod_configs.values()))
    check_mapper_dialect(root, findings)
    check_other_dialect_config(root, findings)
    check_tests(root, findings)
    check_raw_sql(root, findings, args.verbose)

    order = {"MUST": 0, "WARN": 1, "INFO": 2}
    findings.sort(key=lambda f: (order[f.level], f.tag, f.path))

    counts = {"MUST": 0, "WARN": 0, "INFO": 0}
    for f in findings:
        counts[f.level] += 1

    print(f"项目：{root}    目标 profile：{args.profile}")
    print(f"MUST {counts['MUST']}  WARN {counts['WARN']}  INFO {counts['INFO']}\n")
    for f in findings:
        print(f)
        print()

    # 读取失败会让「没扫到问题」变得不可信，必须显式暴露
    if READ_ERRORS:
        print(f"有 {len(READ_ERRORS)} 项读取或解析异常，扫描结果不完整：")
        for e in READ_ERRORS[:10]:
            print(f"    · {e}")
        if len(READ_ERRORS) > 10:
            print(f"    …… 另有 {len(READ_ERRORS) - 10} 个")
        print()

    # 「没扫到任何数据源」时不能下「已完成」的结论——可能路径给错了。
    if not any(f.tag in ("datasource", "parity") for f in findings):
        print("未发现任何数据源配置（application-*.yml/properties）。")
        print(f"请确认路径是否正确：{root}")
        print("常见原因：传了子模块目录而非仓库根目录，或配置不在该目录下。")
        return 1

    # 关键：「没有 MUST」与「目标 profile 确实是 PostgreSQL」是两件事。
    # 早期实现只要有任一 datasource 回执就报完成，导致「只有 prod 且仍用 MySQL」时
    # 输出「dev 已完成切换」——那是纯粹的假阳性。
    target_switched = any(
        f.tag == "datasource" and f.level == "INFO"
        and f.message == '目标有效数据源 URL 使用 PostgreSQL。'
        for f in findings)

    if not target_switched:
        print(f"无法确认 application-{args.profile}.yml 已切到 PostgreSQL：")
        print(f"  未找到该 profile 的「数据源已切到 PostgreSQL」证据。")
        print("  可能原因：该 profile 不存在、仍在使用 MySQL、或 PG 数据源处于注释状态。")
        if not counts["MUST"]:
            print("  注意：MUST 为 0 只表示当前可见范围内没有阻塞项，"
                  "不代表目标 profile 已完成切换。")
        return 1

    if counts["MUST"] or READ_ERRORS:
        return 1

    print(f"目标 {args.profile} 通过本次静态配置检查。")
    print("未验证运行时配置、依赖传递或 prod 与历史版本完全相同。")
    print()
    print("提醒开发者自行核对连接信息（本工具不验证连通性）：")
    print("  · PostgreSQL：host / 库名 / 用户名 / 密码是否与目标环境一致")
    print("  · Redis：spring.data.redis 的 host / port / password / database 是否仍适用")
    return 0


if __name__ == "__main__":
    sys.exit(main())
