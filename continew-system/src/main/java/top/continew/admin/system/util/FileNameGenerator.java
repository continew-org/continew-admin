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

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import cn.hutool.core.util.StrUtil;

import top.continew.admin.system.enums.FileTypeEnum;
import top.continew.admin.system.mapper.FileMapper;
import top.continew.admin.system.model.entity.FileDO;
import top.continew.starter.core.util.CollUtils;

/**
 * 文件名生成工具类
 *
 * <p>
 * 提供文件重名检测和自动重命名功能。当文件名冲突时，自动添加序号后缀，如：file.txt → file(1).txt
 * </p>
 *
 * @author fjwupeng
 * @since 2026/2/06
 */
@Slf4j
public class FileNameGenerator {

    private FileNameGenerator() {
        // 工具类禁止实例化
    }

    /**
     * 生成唯一文件名
     *
     * <p>
     * 当目标目录存在同名文件时，自动添加序号后缀：
     * <ul>
     * <li>file.txt → file(1).txt → file(2).txt → ...</li>
     * <li>无扩展名：README → README(1) → README(2) → ...</li>
     * <li>隐藏文件：.gitignore → .gitignore(1) → .gitignore(2) → ...</li>
     * </ul>
     * </p>
     *
     * @param fileName   原始文件名
     * @param parentPath 上级目录路径
     * @param storageId  存储ID
     * @param fileMapper 文件Mapper
     * @return 唯一文件名
     */
    public static String generateUniqueName(String fileName, String parentPath, Long storageId, FileMapper fileMapper) {
        // 1. 先检查原始文件名是否可用
        boolean exists = existsByName(parentPath, storageId, fileName, fileMapper);
        if (!exists) {
            return fileName;
        }

        // 2. 解析文件名
        String[] parts = parseFileName(fileName);
        String baseName = parts[0];
        String extension = parts[1];

        // 3. 获取该目录下所有可能的冲突文件名（优化：批量查询）
        List<String> existingNames = selectNamesByParentPath(parentPath, storageId, baseName, fileMapper);

        // 4. 寻找第一个可用的序号
        int counter = 1;
        while (true) {
            String newName = buildFileNameWithCounter(baseName, extension, counter);
            if (!existingNames.contains(newName)) {
                log.debug("文件名 [{}] 重命名为 [{}]", fileName, newName);
                return newName;
            }
            counter++;

            // 安全限制，防止无限循环
            if (counter > 9999) {
                log.warn("文件名重命名超过最大限制，使用当前时间戳: {}", fileName);
                return baseName + "_" + System.currentTimeMillis() + (StrUtil.isNotBlank(extension)
                    ? "." + extension
                    : "");
            }
        }
    }

    /**
     * 解析文件名为基础名和扩展名
     *
     * <p>
     * 示例：
     * </p>
     * <ul>
     * <li>"document.pdf" → ["document", "pdf"]</li>
     * <li>"README" → ["README", ""]</li>
     * <li>".gitignore" → [".gitignore", ""]</li>
     * <li>"archive.tar.gz" → ["archive.tar", "gz"]</li>
     * </ul>
     *
     * @param fileName 文件名
     * @return 数组 [基础名, 扩展名]，扩展名可能为空字符串
     */
    public static String[] parseFileName(String fileName) {
        if (StrUtil.isBlank(fileName)) {
            return new String[] {"", ""};
        }

        // 处理隐藏文件（以.开头）
        boolean isHidden = fileName.startsWith(".");
        String nameWithoutDot = isHidden ? fileName.substring(1) : fileName;

        // 处理空文件名（如只有"."的情况）
        if (nameWithoutDot.isEmpty()) {
            return new String[] {fileName, ""};
        }

        // 查找最后一个点号位置
        int lastDotIndex = nameWithoutDot.lastIndexOf('.');

        // 点号不存在或在开头（如 ".bashrc"），视为无扩展名
        if (lastDotIndex <= 0) {
            return new String[] {fileName, ""};
        }

        String baseName = isHidden
            ? "." + nameWithoutDot.substring(0, lastDotIndex)
            : nameWithoutDot.substring(0, lastDotIndex);
        String extension = nameWithoutDot.substring(lastDotIndex + 1);

        // 扩展名不应包含路径分隔符（安全检查）
        if (extension.contains("/") || extension.contains("\\")) {
            return new String[] {fileName, ""};
        }

        return new String[] {baseName, extension};
    }

    /**
     * 构建带序号的文件名
     *
     * @param baseName  基础名
     * @param extension 扩展名（可能为空）
     * @param counter   序号（必须 >= 1）
     * @return 新文件名，如 "file(1).txt"
     */
    public static String buildFileNameWithCounter(String baseName, String extension, int counter) {
        if (counter < 1) {
            throw new IllegalArgumentException("序号必须大于等于1");
        }

        StringBuilder sb = new StringBuilder(baseName);
        sb.append("(").append(counter).append(")");

        if (StrUtil.isNotBlank(extension)) {
            sb.append(".").append(extension);
        }

        return sb.toString();
    }

    /**
     * 检查文件是否存在
     *
     * @param parentPath 上级目录
     * @param storageId  存储ID
     * @param name       文件名
     * @param fileMapper 文件Mapper
     * @return true: 存在
     */
    private static boolean existsByName(String parentPath, Long storageId, String name, FileMapper fileMapper) {
        return fileMapper.lambdaQuery()
            .eq(FileDO::getParentPath, parentPath)
            .eq(FileDO::getStorageId, storageId)
            .eq(FileDO::getName, name)
            .ne(FileDO::getType, FileTypeEnum.DIR)
            .exists();
    }

    /**
     * 查询指定目录下的文件名称列表（用于重名检测）
     *
     * @param parentPath 上级目录
     * @param storageId  存储ID
     * @param namePrefix 名称前缀（可为null，表示查询所有）
     * @param fileMapper 文件Mapper
     * @return 文件名列表
     */
    private static List<String> selectNamesByParentPath(String parentPath,
                                                        Long storageId,
                                                        String namePrefix,
                                                        FileMapper fileMapper) {
        var wrapper = fileMapper.lambdaQuery()
            .eq(FileDO::getParentPath, parentPath)
            .eq(FileDO::getStorageId, storageId)
            .ne(FileDO::getType, FileTypeEnum.DIR)
            .select(FileDO::getName)
            .last("LIMIT 10000"); // 限制最大查询数量，防止内存溢出

        if (StrUtil.isNotBlank(namePrefix)) {
            wrapper.likeRight(FileDO::getName, namePrefix);
        }

        return CollUtils.mapToList(wrapper.list(), FileDO::getName);
    }
}
