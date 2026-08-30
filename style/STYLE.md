# 代码风格（OCN-CodeStyle）

OCN-CodeStyle（OCN 即 OpenContiNew 缩写）基于 Apache Nacos 社区代码风格调整而来，整体遵循《阿里巴巴 Java 开发手册》。全部风格配置集中于 `style/` 目录：

| 文件 | 用途 |
|:--|:--|
| `ocn-eclipse-formatter.xml` | Eclipse Formatter Profile，供 Spotless 与 Eclipse 使用 |
| `ocn-idea-code-style.xml` | IntelliJ IDEA Code Style Scheme（与 Eclipse Formatter 同源映射，仅覆盖 Java/XML） |
| `ocn-checkstyle.xml` | Checkstyle 规则（Maven `validate` 阶段门禁） |
| `ocn-spotbugs-exclude.xml` | SpotBugs 排除清单（豁免需留痕） |
| `license-header` | License Header 模板（Spotless 校验与补全） |

## IntelliJ IDEA

### 导入代码风格

```
Settings/Preferences → Editor → Code Style → 齿轮图标 → Import Scheme → IntelliJ IDEA code style XML
```

选择 `style/ocn-idea-code-style.xml`，导入后确认当前 Scheme 为 `OCN-CodeStyle`。

### 插件（可选，用于开发期实时发现问题）

#### Checkstyle

[Checkstyle-IDEA](https://plugins.jetbrains.com/plugin/1065-checkstyle-idea)

1. `Settings → Tools → Checkstyle`
2. 插件内的 Checkstyle 版本设置为 **11.0.0**（与项目 `checkstyle.version` 一致，Java 17 支持所需）
3. 扫描范围勾选 `Include test sources`
4. 添加规则文件 `style/ocn-checkstyle.xml` 并设为 Active

#### SpotBugs

[SpotBugs-IDEA](https://plugins.jetbrains.com/plugin/14014-spotbugs)

1. `Settings → Plugins` 搜索 "SpotBugs" 并安装
2. 提交前对修改的代码运行分析，提前发现潜在缺陷

## Eclipse

`Window → Preferences → Java → Code Style → Formatter → Import`，选择 `style/ocn-eclipse-formatter.xml`，并在 `Active profile` 中选用新导入的 profile。

## Spotless 自动格式化（最终事实）

Spotless 使用 Eclipse JDT 格式化引擎执行 `ocn-eclipse-formatter.xml`，绑定在 Maven `validate` 阶段作为门禁：

```bash
./mvnw verify             # 门禁检查：格式不符合将直接构建失败（四道门禁）
./mvnw compile -Pformat   # 自动修复：格式化 + 清理 import + 补 License Header
```

### 关键规则

| 规则 | 值 |
|:--|:--|
| 缩进 | 4 空格（禁用 Tab） |
| 续行缩进 | 4 空格 |
| 行宽 | 100 字符 |
| 空行保留 | 1 行 |
| 行尾空白 | 禁止（`trimTrailingWhitespace` 强制，空行必须完全空白） |
| Import | 自动清理无用 import |
| License Header | `validate` 阶段校验，缺失或不符合自动补全 |
| 区间跳过 | `// @formatter:off` 与 `// @formatter:on` 之间的代码不参与格式化 |

### 与 IDE 格式化的关系

`ocn-idea-code-style.xml` 是 `ocn-eclipse-formatter.xml` 的同源映射，逐项对齐其语义（空行必须完全空白、字段间空行有则保留无则不插、注释不参与重排、保留手工换行等）。两套格式化引擎仍是不同实现，个别换行场景无法字节级一致。**Spotless 检查是唯一事实标准**：以 `./mvnw verify` 通过为准，IDE 内格式化仅用于编辑期舒适度。

建议同时开启 `Settings → Editor → General → On Save → Remove trailing spaces: All`：打字期产生的行尾空白（含空行缩进）在保存时自动清除。
