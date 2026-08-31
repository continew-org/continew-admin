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
import top.continew.starter.core.exception.BaseException;

/**
 * 存储目录路径校验工具
 *
 * <p>
 * 用户在上传文件、创建文件夹时可指定上级目录（parentPath），该参数会参与最终存储路径的拼接。
 * 为防止路径穿越攻击（如 {@code ../}、绝对路径）导致文件被写到存储根目录之外，所有使用
 * parentPath 拼接路径的入口都必须先经过本校验。
 * </p>
 *
 * @author Charles7c
 * @since 2026/8/31
 */
public class StoragePathValidator {

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
     * 2. 允许以 {@code /} 开头的虚拟目录路径（如 {@code /user/avatar}，前导斜杠会被规范化），
     * 但不允许 Windows 盘符等操作系统级绝对路径； <br />
     * 3. 任一路径段不允许为 {@code .} 或 {@code ..}，避免目录穿越。
     * </p>
     *
     * @param parentPath 上级目录路径
     */
    public static void validate(String parentPath) {
        if (StrUtil.isBlank(parentPath) || StringConstants.SLASH.equals(parentPath)) {
            return;
        }
        String normalized = parentPath.replace('\\', '/');
        if (normalized.matches("^/?[a-zA-Z]:.*")) {
            throw new BaseException("非法的目录路径：不允许使用操作系统绝对路径");
        }
        for (String segment : normalized.split(StringConstants.SLASH)) {
            if (StringConstants.DOT.equals(segment) || "..".equals(segment)) {
                throw new BaseException("非法的目录路径：不允许包含 . 或 .. 路径段");
            }
        }
    }
}
