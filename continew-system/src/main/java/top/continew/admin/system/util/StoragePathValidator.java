/*
 * Copyright (c) 2022-present Charles7c Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package top.continew.admin.system.util;

import cn.hutool.core.util.StrUtil;
import top.continew.starter.core.constant.StringConstants;
import top.continew.starter.core.exception.BadRequestException;

import java.util.regex.Pattern;

/**
 * 存储路径校验工具
 *
 * <p>
 * 用户在上传文件、创建文件夹、修改文件时可指定上级目录（parentPath）和名称（name），二者都会参与最终存储路径的
 * 拼接（见 {@code FileDO#setParentPath}）。为防止路径穿越攻击（如 {@code ../}、绝对路径）导致文件被写到
 * 存储根目录之外，所有参与路径拼接的入参都必须先经过本校验。
 * </p>
 *
 * @author Charles7c
 * @since 2026/8/31
 */
public class StoragePathValidator {

    /**
     * 操作系统绝对路径（Windows 盘符形式，如 {@code C:/data}、{@code /D:/data}）
     *
     * <p>
     * 使用 {@link Pattern#DOTALL} 使 {@code .} 匹配行终止符，避免通过换行符绕过整串匹配。
     * </p>
     */
    private static final Pattern OS_ABSOLUTE_PATH_PATTERN =
        Pattern.compile("^/?[a-zA-Z]:.*", Pattern.DOTALL);

    private StoragePathValidator() {
    }

    /**
     * 校验上级目录路径是否合法
     *
     * <p>
     * 合法路径要求：
     * </p>
     * <p>
     * 1. 允许为空或 {@code /}（表示存储根目录）； <br />
     * 2. 允许以单个 {@code /} 开头的虚拟目录路径（如 {@code /user/avatar}，前导斜杠会被规范化），
     * 但不允许 Windows 盘符等操作系统级绝对路径，也不允许 {@code //} 开头的 UNC 或根路径形式； <br />
     * 3. 不允许包含控制字符； <br />
     * 4. 任一路径段不允许为 {@code .}、{@code ..} 等纯点号形式，避免目录穿越。
     * </p>
     *
     * @param parentPath 上级目录路径
     */
    public static void validate(String parentPath) {
        if (StrUtil.isBlank(parentPath) || StringConstants.SLASH.equals(parentPath)) {
            return;
        }
        String normalized = parentPath.replace(StringConstants.BACKSLASH, StringConstants.SLASH);
        throwIf(containsControlChar(normalized), "非法的目录路径：不允许包含控制字符");
        throwIf(OS_ABSOLUTE_PATH_PATTERN.matcher(normalized).matches(),
            "非法的目录路径：不允许使用操作系统绝对路径");
        // 连续斜杠会产生空路径段，去掉前导斜杠后仍是绝对路径，需拒绝
        throwIf(normalized.contains(StringConstants.DOUBLE_SLASH), "非法的目录路径：不允许包含连续的 /");
        for (String segment : normalized.split(StringConstants.SLASH)) {
            throwIf(isDotSegment(segment), "非法的目录路径：不允许包含 . 或 .. 路径段");
        }
    }

    /**
     * 校验文件或文件夹名称是否合法
     *
     * <p>
     * 名称同样会参与存储路径拼接，因此不允许包含路径分隔符、盘符分隔符、控制字符，也不允许是
     * {@code .}、{@code ..} 等纯点号形式。
     * </p>
     *
     * @param name 文件或文件夹名称
     */
    public static void validateName(String name) {
        throwIf(StrUtil.isBlank(name), "名称不能为空");
        throwIf(containsControlChar(name), "非法的名称：不允许包含控制字符");
        throwIf(StrUtil.containsAny(name, StringConstants.SLASH, StringConstants.BACKSLASH),
            "非法的名称：不允许包含路径分隔符");
        // 拒绝盘符与 NTFS 数据流形式，如 C:evil、file.txt:evil
        throwIf(StrUtil.contains(name, StringConstants.COLON), "非法的名称：不允许包含 :");
        throwIf(isDotSegment(name), "非法的名称：不允许为 . 或 .. 形式");
    }

    /**
     * 条件成立时抛出 400 异常
     *
     * <p>
     * 这里不使用 {@code ValidationUtils}：其静态初始化依赖运行中的 Spring 容器，
     * 会让本工具类无法脱离容器使用与测试。
     * </p>
     *
     * @param condition 条件
     * @param message   异常信息
     */
    private static void throwIf(boolean condition, String message) {
        if (condition) {
            throw new BadRequestException(message);
        }
    }

    /**
     * 是否为点号路径段
     *
     * <p>
     * 直接比较 {@code "."}、{@code ".."} 并不充分：Windows 会剥离路径分量末尾的点号与空格，
     * {@code ".. "} 在落盘时会还原为 {@code ".."}。因此这里将「仅由点号和空白字符组成」的路径段
     * 一律视为点号段，正常的文件名不会是这种形式。
     * </p>
     *
     * @param segment 路径段
     * @return 是否为点号路径段
     */
    private static boolean isDotSegment(String segment) {
        if (StrUtil.isEmpty(segment)) {
            return false;
        }
        for (int i = 0; i < segment.length(); i++) {
            char c = segment.charAt(i);
            if (c != '.' && !Character.isWhitespace(c)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 是否包含控制字符
     *
     * @param value 待检测的字符串
     * @return 是否包含控制字符
     */
    private static boolean containsControlChar(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (Character.isISOControl(value.charAt(i))) {
                return true;
            }
        }
        return false;
    }
}
