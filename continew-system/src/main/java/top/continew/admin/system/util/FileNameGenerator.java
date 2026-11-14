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

import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import top.continew.starter.core.constant.StringConstants;

/**
 * 存储文件名生成工具类
 *
 * <p>
 * 文件落盘时不使用用户上传的原始文件名，而是生成「唯一 ID + 原扩展名」作为存储名，例如
 * {@code 3f2a1c9e5b7d4a6f8c0e1d2b3a4c5d6e.png}。原始文件名只作为展示名（{@code sys_file.original_name}）保留。
 * </p>
 *
 * <p>
 * 这样处理的收益：同名文件不再需要 {@code file(1).txt} 式的重命名探测，也不会因为并发上传或回收站还原而互相覆盖；扩展名保持不变，存储引擎与浏览器的类型识别不受影响。
 * </p>
 *
 * @author fjwupeng
 * @since 4.2.0
 */
public class FileNameGenerator {

    private FileNameGenerator() {
        // 工具类禁止实例化
    }

    /**
     * 生成存储文件名
     *
     * <p>
     * 由 32 位随机十六进制串与原扩展名拼接而成，扩展名保留原有大小写：
     * </p>
     * <ul>
     * <li>{@code 报告.PNG} → {@code 3f2a...d6e.PNG}</li>
     * <li>{@code archive.tar.gz} → {@code 3f2a...d6e.gz}</li>
     * <li>{@code README} → {@code 3f2a...d6e}</li>
     * </ul>
     *
     * <p>
     * 唯一 ID 为 122 位随机数，无需查询数据库做存在性校验，也不做重试。
     * </p>
     *
     * @param originalFileName 原始文件名
     * @return 存储文件名
     */
    public static String generateStoredName(String originalFileName) {
        String storedName = IdUtil.fastSimpleUUID();
        String extension = FileNameUtil.extName(originalFileName);
        return StrUtil.isBlank(extension) ? storedName
            : storedName + StringConstants.DOT + extension;
    }
}
