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

package top.charles7c.cnadmin.common.util;

import java.io.File;
import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.multipart.MultipartFile;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.IdUtil;

import top.charles7c.cnadmin.common.constant.StringConsts;

/**
 * 文件工具类
 *
 * @author Charles7c
 * @since 2023/1/2 21:34
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileUtils {

    /**
     * 上传文件
     *
     * @param multipartFile
     *            源文件对象
     * @param filePath
     *            文件路径
     * @param isKeepOriginalFilename
     *            是否保留原文件名
     * @return 目标文件对象
     */
    public static File upload(MultipartFile multipartFile, String filePath, boolean isKeepOriginalFilename) {
        String originalFilename = multipartFile.getOriginalFilename();
        String extensionName = FileNameUtil.extName(originalFilename);

        String fileName;
        if (isKeepOriginalFilename) {
            fileName = String.format("%s-%s.%s", FileNameUtil.getPrefix(originalFilename),
                DateUtil.format(LocalDateTime.now(), StringConsts.PURE_DATE_TIME_MS_PATTERN), extensionName);
        } else {
            fileName = String.format("%s.%s", IdUtil.fastSimpleUUID(), extensionName);
        }

        try {
            String pathname = filePath + fileName;
            File dest = new File(pathname).getCanonicalFile();
            // 如果父路径不存在，自动创建
            if (!dest.getParentFile().exists()) {
                if (!dest.getParentFile().mkdirs()) {
                    log.error("Create upload file parent path failed.");
                }
            }
            // 文件写入
            multipartFile.transferTo(dest);
            return dest;
        } catch (Exception e) {
            log.error("Upload file occurred an error: {}. fileName: {}.", e.getMessage(), fileName, e);
        }
        return null;
    }
}
