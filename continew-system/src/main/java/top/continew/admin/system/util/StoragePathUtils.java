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

/**
 * 存储路径工具
 *
 * <p>统一处理库内路径、物理路径与缩略图路径的形态转换，避免各服务重复拼接。</p>
 *
 * @author luoqiz
 * @since 4.2.0
 */
public final class StoragePathUtils {

    private StoragePathUtils() {
    }

    /**
     * 归一化为物理存储路径
     *
     * <p>统一分隔符、折叠连续斜杠并移除首尾斜杠。</p>
     *
     * @param path 路径
     * @return 物理存储路径（无前导斜杠）
     */
    public static String normalize(String path) {
        if (path == null) {
            return StringConstants.EMPTY;
        }
        String normalized = path.replace("\\", StringConstants.SLASH)
            .replaceAll("/+", StringConstants.SLASH);
        return StrUtil.removeSuffix(StrUtil.removePrefix(normalized, StringConstants.SLASH),
            StringConstants.SLASH);
    }

    /**
     * 转换为库内路径形态
     *
     * @param path 路径
     * @return 库内路径（带前导斜杠）
     */
    public static String toDbPath(String path) {
        return StringConstants.SLASH + normalize(path);
    }

    /**
     * 构建缩略图库内路径
     *
     * @param parentPath    上级目录
     * @param thumbnailName 缩略图名称
     * @return 缩略图库内路径；名称为空时返回 null
     */
    public static String thumbnailPath(String parentPath, String thumbnailName) {
        if (StrUtil.isBlank(thumbnailName)) {
            return null;
        }
        String parent =
            StrUtil.removeSuffix(StrUtil.nullToEmpty(parentPath), StringConstants.SLASH);
        return (StrUtil.isBlank(parent) ? StringConstants.SLASH : parent + StringConstants.SLASH)
            + thumbnailName;
    }
}
