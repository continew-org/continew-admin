#!/usr/bin/env python3
"""辅助排查工具：探测 PostgreSQL 端口与凭据。

**定位说明**：这不是切换流程的必经步骤。本 skill 的职责只到「配置改对」为止，
连通性由开发者自行负责。本脚本只作为开发者遇到连接问题时的排查辅助——
比如确认 5432 是否可达，或验证一个明确指定的账号能否登录。

原理：用 JDK 的单文件源码模式（`java Foo.java`）现场编译执行一段 JDBC 探测代码，
不污染项目、不写临时 .class。驱动 jar 自动从本地 Maven 仓库查找。

用法（仅供开发者另行获授权后，在本脚本所在目录运行；不用于切换验收）：
    # 单账号验证：在终端提示后输入密码，输入不回显
    python probe_pg.py --host localhost --db continew_admin --user app_user

    # 仅检查明确指定的单一端口
    python probe_pg.py --host localhost --port 5432 --port-only

凭据模式必须指定一个用户；标准输入不是终端或无法关闭回显时拒绝读取密码。
密码仅通过子进程管道传给 Java，不进入命令行。不会尝试默认凭据，也不重试。
凭据模式校验服务端证书和主机名（verify-full），需预先配置信任。端口模式无需用户、密码或 JDBC 驱动。

退出码：0 = 目标探测成功；1 = 目标探测失败；2 = 环境或执行问题（JDK/驱动缺失、子进程异常）
"""

import argparse
import getpass
import ipaddress
import os
import re
import shutil
import subprocess
import sys
import tempfile
import warnings
from pathlib import Path
from urllib.parse import quote

JAVA_CANDIDATES = [os.environ.get("JAVA_HOME")]
MAVEN_REPOS = [os.path.expanduser("~/.m2/repository")]
# verify-ca / verify-full 自 42.2.5 起支持；更老的驱动会忽略 sslmode，
# 等于不校验证书就发送密码，因此只接受不低于此版本的主 jar。
MIN_TLS_DRIVER_VERSION = (42, 2, 5)
if os.name == "nt":
    JAVA_CANDIDATES.extend([
        "D:/software/java/jdk-21.0.5",
        "D:/software/java/jdk-21.0.12.1",
        "D:/software/java/jdk-17.0.10",
        "C:/Program Files/Java/jdk-21",
    ])
    MAVEN_REPOS.append("D:/maven/maven_repository")

# 凭据探测：密码通过子进程标准输入传递，以 EOF 为边界。
PROBE = r"""
import java.io.*;
import java.security.cert.CertificateException;
import java.sql.*;
import javax.net.ssl.SSLException;

public class Probe {
    static String guess(SQLException e) {
        // 服务端消息可能被本地化，先按具体 SQLState 判定，再退回英文消息匹配。
        String state = e.getSQLState();
        if (state != null) {
            if (state.equals("28P01")) return "凭据错误";
            if (state.equals("28000")) return "认证方式被拒绝";
            if (state.equals("3D000")) return "库不存在";
            if (state.equals("42501")) return "权限不足";
        }
        String msg = e.getMessage();
        if (msg != null) {
            String m = msg.toLowerCase();
            if (m.contains("scram") && m.contains("empty string")) return "空密码不被接受";
            if (m.contains("password authentication failed")) return "凭据错误";
            if (m.contains("database") && m.contains("does not exist")) return "库不存在";
            if (m.contains("role") && m.contains("does not exist")) return "用户不存在";
            if (m.contains("refused")) return "连接被拒绝";
            if (m.contains("timeout") || m.contains("timed out")) return "连接超时";
        }
        // 08xxx 是通用连接错误：只有上面没有更具体的原因时才用它兜底，
        // 否则「拒绝连接」这类可执行诊断会被笼统的「无法建立连接」吃掉。
        if (state != null && state.startsWith("08")) return "无法建立连接";
        return "其他";
    }

    /** 沿异常原因链找本地信任/证书问题；返回 null 表示不属于这类问题。
     *
     * 只匹配顶层消息会把退出码判反：驱动读不到本地 root.crt 时顶层消息里可能既没有
     * PKIX 也没有 trust 字样，而密码错误的顶层消息里又可能正好包含用户名
     * （如 trusted_user）。因此这里按异常类型判断，并区分「本地读取失败」与「校验失败」。
     */
    static String localTrust(Throwable t) {
        boolean ssl = false;
        for (Throwable c = t; c != null; c = c.getCause() == c ? null : c.getCause()) {
            if (c instanceof FileNotFoundException) {
                return "本地信任证书读取失败";
            }
            if (c instanceof CertificateException) {
                return "服务端证书校验失败";
            }
            if (c instanceof SSLException) {
                ssl = true;
            }
            String m = c.getMessage() == null ? "" : c.getMessage().toLowerCase();
            if (m.contains("pkix") || m.contains("certification path")
                || m.contains("root.crt") || m.contains("certificate")) {
                return "服务端证书校验失败";
            }
        }
        return ssl ? "SSL 协商失败" : null;
    }

    public static void main(String[] args) throws Exception {
        String host = args[0], port = args[1], db = args[2];
        Class.forName("org.postgresql.Driver");
        String pwd = new String(System.in.readAllBytes(), "UTF-8");
        String url = "jdbc:postgresql://" + host + ":" + port + "/" + db
            + "?sslmode=verify-full&connectTimeout=5&socketTimeout=8";
        try (Connection c = DriverManager.getConnection(url, args[3], pwd)) {
            System.out.println("RESULT 1");
        } catch (SQLException e) {
            // 先按认证类 SQLState 判定登录失败：此时账号已到认证阶段，结论明确。
            String state = e.getSQLState();
            if (state != null && state.startsWith("28")) {
                System.out.println(guess(e));
                System.out.println("RESULT 0");
                return;
            }
            // 非认证错误且落在 TLS/证书路径上：账号根本没验证过，不能报成登录失败。
            String trust = localTrust(e);
            if (trust != null) {
                System.out.println(trust + "，账号尚未验证");
                System.out.println("RESULT 2");
                return;
            }
            System.out.println(guess(e));
            System.out.println("RESULT 0");
        }
    }
}
"""

# 端口模式仅检查单一目标，不加载 JDBC 或读取密码。
PORT_PROBE = r"""
import java.net.*;

public class PortProbe {
    public static void main(String[] args) {
        try (Socket s = new Socket()) {
            s.connect(new InetSocketAddress(args[0], Integer.parseInt(args[1])), 3000);
            System.out.println("RESULT 1");
        } catch (UnknownHostException e) {
            // 名称解析失败时根本没发出 TCP 连接，不能笼统报成端口不可达。
            System.out.println("主机名解析失败");
            System.out.println("RESULT 2");
        } catch (java.net.SocketTimeoutException e) {
            System.out.println("连接超时");
            System.out.println("RESULT 0");
        } catch (java.net.ConnectException e) {
            // ConnectException 不专指对端拒绝：某些系统的底层超时也会落到这里，
            // 说成「被拒绝」会把网络路径问题误诊成主动拒绝。
            String m = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
            System.out.println(m.contains("refused") ? "连接被拒绝" : "无法建立 TCP 连接");
            System.out.println("RESULT 0");
        } catch (java.io.IOException e) {
            System.out.println("其他");
            System.out.println("RESULT 0");
        }
    }
}
"""


def source_mode_ok(exe):
    """候选 java 是否支持单文件源码模式（`java Foo.java`，Java 11 起提供）。

    能找到可执行文件不等于能跑源码模式：JAVA_HOME 指向 JDK 8 而 PATH 上是 17 时，
    直接返回 JDK 8 会让两种探测都在连接前失败。这里读 `java -version` 的版本号，
    低于 11 或读不出主版本就不接受，交给调用方继续往后找。
    """
    try:
        r = subprocess.run([exe, "-version"], capture_output=True, timeout=10)
    except (OSError, subprocess.TimeoutExpired):
        return False
    # `java -version` 把版本打到 stderr；旧版是 1.8.0_xxx 形式，新版是 17.0.10。
    text = (r.stderr or b"").decode("utf-8", errors="replace") + \
           (r.stdout or b"").decode("utf-8", errors="replace")
    m = re.search(r'version\s+"(\d+)(?:\.(\d+))?', text)
    if not m:
        return False
    major = int(m.group(1))
    if major == 1 and m.group(2):
        major = int(m.group(2))
    return major >= 11


def find_java():
    for jh in JAVA_CANDIDATES:
        if not jh or not Path(jh).is_absolute():
            continue
        exe = os.path.join(jh, "bin", "java.exe" if os.name == "nt" else "java")
        # POSIX 上普通文件不代表可执行；不可执行时继续找，别把可用的 PATH java 拦住。
        if os.path.isfile(exe) and (os.name == "nt" or os.access(exe, os.X_OK)):
            if source_mode_ok(exe):
                return exe
    # 没设 JAVA_HOME 时，Linux/macOS 常见的情况是 java 已在 PATH 上；
    # 只查候选目录会让能正常运行的 JDK 被判定为「找不到」。PATH 上的 java 同样要过
    # 源码模式这一关：它可能正好是 8，那两种探测都会在启动阶段失败。
    found = shutil.which("java")
    if found and Path(found).is_absolute() and source_mode_ok(found):
        return found
    return None


def find_driver():
    """挑出支持 `sslmode=verify-full` 的驱动主 jar。

    verify-ca / verify-full 是 42.2.5 才支持的值；更老版本的驱动会静默忽略该参数，
    等于在不校验证书的情况下就把密码发出去。找不到合规版本时返回 None，由调用方
    在读取密码之前退出。
    """
    for repo in MAVEN_REPOS:
        root = Path(repo)
        if not root.is_absolute():
            continue
        hits = []
        for jar in (root / "org/postgresql/postgresql").glob("*/*.jar"):
            version = jar.parent.name
            if not (re.fullmatch(r"[0-9]+(?:\.[0-9]+)*", version)
                    and jar.name == f"postgresql-{version}.jar" and jar.is_file()):
                continue
            parsed = tuple(map(int, version.split(".")))
            if parsed < MIN_TLS_DRIVER_VERSION:
                continue
            hits.append((parsed, jar))
        if hits:
            return str(max(hits)[1])
    return None


def run_java(cmd, stdin_text=None):
    """强制 Java 用 UTF-8 输出，再按 UTF-8 解码；stdin 以 UTF-8 写入。

    Windows 下 java 默认按控制台 GBK 编码输出，含中文时会与 UTF-8 解码冲突。
    这里同时用 -Dfile.encoding=UTF-8 和 -Dstdout.encoding=UTF-8 双保险。
    """
    cmd = [cmd[0], "-Dfile.encoding=UTF-8", "-Dstdout.encoding=UTF-8",
           "-Dstderr.encoding=UTF-8"] + cmd[1:]
    r = subprocess.run(cmd, capture_output=True, timeout=30,
                       input=None if stdin_text is None else stdin_text.encode("utf-8"))
    out = r.stdout.decode("utf-8", errors="replace")
    err = r.stderr.decode("utf-8", errors="replace")
    return subprocess.CompletedProcess(r.args, r.returncode, out, err)


def parse_result(out):
    """从子进程输出里取 `RESULT <n>`。

    返回 (状态, 错误说明)。合法值 0 / 1 / 2；拿不到结果时返回 (None, 原因)，调用方据此
    返回 2——不能因为「没有 RESULT 0」就当成成功，那是编译失败/驱动缺失的典型表现。
    """
    lines = [l.strip() for l in out.splitlines() if l.strip()]
    if not lines:
        return None, "子进程没有任何输出"
    last = lines[-1]
    if last not in ("RESULT 0", "RESULT 1", "RESULT 2"):
        return None, "末行应为 RESULT 0、RESULT 1 或 RESULT 2"
    return int(last[-1]), None

def make_tmpdir(override=None):
    """建临时目录。

    显式指定 mkdtemp 的 dir，不使用默认临时目录：默认目录可能落在系统盘（Windows 是 C:\\Users\\...\\Temp），
    而本机约定 C 盘不放测试数据。也不落 /tmp——Git Bash 的 /tmp 实际也指向系统盘。
    脚本目录只读（如 skill 装在只读位置）时，用 --tmpdir 显式指定可写目录。
    """
    base = override or os.path.join(os.path.dirname(os.path.abspath(__file__)), ".tmp")
    os.makedirs(base, exist_ok=True)
    return tempfile.mkdtemp(prefix="pgprobe-", dir=base)


def main():
    class Parser(argparse.ArgumentParser):
        def error(self, message):
            # 不复述无效参数，以免旧调用方式中的密码出现在日志里。
            super().error("参数无效，详见 --help。")

        def validation_error(self, message):
            # 仅接收脚本内的固定提示，不传入命令行原文。
            super().error(message)

    ap = Parser(description="验证 PostgreSQL 连通性与凭据", allow_abbrev=False)
    ap.add_argument("--host", required=True)
    ap.add_argument("--port", default="5432")
    ap.add_argument("--db", help="凭据模式必须明确指定目标数据库")
    mode = ap.add_mutually_exclusive_group(required=True)
    mode.add_argument("--user", metavar="USER", action="append",
                      help="验证一个账号，在终端隐藏输入密码")
    mode.add_argument("--port-only", action="store_true", help="仅探测端口，不验证凭据")
    ap.add_argument("--tmpdir", help="可写的临时目录（脚本目录只读时必填）")
    args = ap.parse_args()
    # JDBC authority 只允许单一主机；不接受多主机列表、参数或用户信息。
    if ":" in args.host:
        host = args.host.removeprefix("[").removesuffix("]")
        try:
            ipaddress.IPv6Address(host)
        except ValueError:
            ap.validation_error("IPv6 地址无效")
        if args.host not in (host, f"[{host}]") or "%" in host:
            ap.validation_error("IPv6 地址无效")
        args.host = host if args.port_only else f"[{host}]"
    elif not re.fullmatch(r"[A-Za-z0-9][A-Za-z0-9.-]*", args.host):
        ap.validation_error("主机名无效")
    if not re.fullmatch(r"[0-9]{1,5}", args.port) or not 1 <= int(args.port) <= 65535:
        ap.validation_error("端口无效")
    if args.user:
        if not args.db or not args.db.strip():
            ap.validation_error("凭据模式必须通过 --db 指定非空数据库名")
        if len(args.user) != 1 or not args.user[0].strip():
            ap.validation_error("必须且只能指定一次 --user，且用户名不能为空")
        if sys.stdin is None or sys.stdin.closed or not sys.stdin.isatty():
            print("密码必须在交互终端中输入，不接受管道或重定向。", file=sys.stderr)
            return 2
        # Windows 的 getpass 直接读控制台缓冲，标准输入被替换时 isatty() 仍可能为
        # 真且调用会阻塞；用 GetConsoleMode 判断标准输入是否真的挂在控制台上。
        if os.name == "nt":
            import ctypes
            mode = ctypes.c_uint()
            handle = ctypes.windll.kernel32.GetStdHandle(-10)  # STD_INPUT_HANDLE
            if not ctypes.windll.kernel32.GetConsoleMode(handle, ctypes.byref(mode)):
                print("未检测到可交互终端，已取消凭据探测。", file=sys.stderr)
                return 2
        else:
            try:
                has_console = os.isatty(sys.stdin.fileno())
            except (OSError, ValueError):
                has_console = False
            if not has_console:
                print("未检测到可交互终端，已取消凭据探测。", file=sys.stderr)
                return 2

    java = find_java()
    if not java:
        print("找不到可用的 JDK：请设置 JAVA_HOME 后重试。", file=sys.stderr)
        return 2

    try:
        tmpdir = make_tmpdir(args.tmpdir)
    except OSError as exc:
        print(f"无法创建临时目录（{type(exc).__name__}）：用 --tmpdir 指定可写目录后重试。",
              file=sys.stderr)
        return 2
    try:
        if args.port_only:
            src = os.path.join(tmpdir, "PortProbe.java")
            with open(src, "w", encoding="utf-8") as f:
                f.write(PORT_PROBE)
            r = run_java([java, src, args.host, args.port])
            if r.returncode != 0:
                print(f"端口探测执行失败（退出码 {r.returncode}），结论不可用。", file=sys.stderr)
                return 2
            n, why = parse_result(r.stdout)
            if n is None:
                print(f"端口探测结果无法解析：{why}", file=sys.stderr)
                return 2
            # 子进程把具体原因写在 RESULT 行之前；名称解析失败不属于「端口不可达」。
            detail = [l.strip() for l in r.stdout.splitlines() if l.strip()][:-1]
            reason = detail[-1] if detail else "其他"
            if n == 1:
                print("端口可达。")
                return 0
            if n == 2:
                print(f"探测未完成：{reason}。请先核对主机名或 DNS。", file=sys.stderr)
                return 2
            print(f"端口不可达：{reason}。")
            return 1

        driver = find_driver()
        if not driver:
            print("本地未找到支持 sslmode=verify-full 的 PostgreSQL 驱动（需 42.2.5 及以上），"
                  "未执行凭据探测。", file=sys.stderr)
            return 2

        try:
            with warnings.catch_warnings():
                warnings.simplefilter("error", getpass.GetPassWarning)
                password = getpass.getpass("数据库密码：")
        except (getpass.GetPassWarning, EOFError, OSError, KeyboardInterrupt):
            print("未能安全读取密码，已取消探测。", file=sys.stderr)
            return 2

        src = os.path.join(tmpdir, "Probe.java")
        with open(src, "w", encoding="utf-8") as f:
            f.write(PROBE)
        r = run_java([java, "-cp", driver, src, args.host, args.port, quote(args.db, safe=""), args.user[0]],
                     stdin_text=password)
        # 驱动诊断可能包含敏感内容，不直接转发子进程输出。
        if r.returncode != 0:
            print(f"凭据探测执行失败（退出码 {r.returncode}），"
                  "常见原因是 JDK 版本过低、驱动加载失败或源码编译失败。", file=sys.stderr)
            return 2
        lines = r.stdout.splitlines()
        if lines[-1:] == ["RESULT 1"]:
            # 服务端若用 trust 认证，密码对错都能连上，所以不能宣称「密码正确」。
            print("账号登录成功；密码是否被校验取决于服务端认证方式。")
            return 0
        if lines[-1:] == ["RESULT 0"]:
            reasons = {"空密码不被接受", "凭据错误", "认证方式被拒绝", "库不存在", "用户不存在",
                       "权限不足", "无法建立连接", "连接被拒绝", "无法建立 TCP 连接",
                       "连接超时", "SSL 协商失败", "其他"}
            reason = lines[-2] if len(lines) >= 2 and lines[-2] in reasons else "其他"
            print(f"登录失败：{reason}。")
            return 1
        if lines[-1:] == ["RESULT 2"]:
            # 本地信任配置问题：账号尚未验证，不能报成登录失败。
            # 子进程给出的原因已含「账号尚未验证」，这里不再拼接这句以免重复。
            detail = lines[-2] if len(lines) >= 2 else "探测未完成"
            print(f"探测未完成：{detail}。请检查本机信任库与证书配置。", file=sys.stderr)
            return 2
        print("凭据探测结果无法解析。", file=sys.stderr)
        return 2
    finally:
        # 目录可能已被外部删除或变得不可读；清理失败不能覆盖已确定的探测结论。
        leftover = []
        try:
            names = os.listdir(tmpdir)
        except OSError:
            names = []
            leftover.append('（目录）')
        for name in names:
            try:
                os.remove(os.path.join(tmpdir, name))
            except OSError:
                leftover.append(name)
        try:
            os.rmdir(tmpdir)
        except OSError:
            if '（目录）' not in leftover:
                leftover.append('（目录）')
        # 只在用的是脚本自带的 .tmp 容器时才顺手清空目录：
        # --tmpdir 指向的是调用方的目录，即使它正好为空也不该被删掉。
        if args.tmpdir is None:
            parent = os.path.dirname(tmpdir)
            try:
                if os.path.isdir(parent) and not os.listdir(parent):
                    os.rmdir(parent)
            except OSError:
                pass
        # 不改变探测结论，但要让人知道残留位置，便于手动清理。
        if leftover:
            print(f"临时目录未能完全清除，请手动清理：{tmpdir}", file=sys.stderr)


if __name__ == "__main__":
    try:
        sys.exit(main())
    except (OSError, subprocess.TimeoutExpired):
        print("环境或子进程不可用。", file=sys.stderr)
        sys.exit(2)
