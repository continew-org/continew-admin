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

package top.continew.admin.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.plugins.IgnoreStrategy;
import com.baomidou.mybatisplus.core.plugins.InterceptorIgnoreHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.continew.admin.common.context.UserContextHolder;
import top.continew.admin.system.enums.FileTypeEnum;
import top.continew.admin.system.mapper.FileMapper;
import top.continew.admin.system.model.entity.FileDO;
import top.continew.admin.system.model.entity.StorageDO;
import top.continew.admin.system.model.query.FileQuery;
import top.continew.admin.system.model.resp.file.FileResp;
import top.continew.admin.system.service.FileRecycleService;
import top.continew.admin.system.service.StorageService;
import top.continew.starter.core.constant.StringConstants;
import top.continew.starter.core.exception.BusinessException;
import top.continew.starter.data.util.QueryWrapperHelper;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.resp.PageResp;
import top.continew.starter.storage.core.FileStorageService;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 文件回收站业务实现
 *
 * @author Charles7c
 * @since 2025/11/11 21:28
 */
@Service
@RequiredArgsConstructor
public class FileRecycleServiceImpl implements FileRecycleService {

    private final FileMapper fileMapper;
    private final StorageService storageService;
    private final FileStorageService fileStorageService;

    @Override
    public PageResp<FileResp> page(FileQuery query, PageQuery pageQuery) {
        QueryWrapper<FileDO> queryWrapper = QueryWrapperHelper.build(query, pageQuery.getSort());
        Page<FileDO> page =
            fileMapper.selectPageInRecycleBin(new Page<>(pageQuery.getPage(), pageQuery
                .getSize()), queryWrapper.lambda().eq(FileDO::getDeleted, 1L));
        return PageResp.build(page, FileResp.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restore(Long id) {
        FileDO file = this.getById(id);
        // 恢复记录
        fileMapper.restoreInRecycleBin(id, UserContextHolder.getUserId());
        if (FileTypeEnum.DIR.equals(file.getType())) {
            return;
        }
        // 还原文件
        StorageDO storage = storageService.getById(file.getStorageId());
        String targetPath = normalizeStoragePath(file.getPath());
        String sourcePath = normalizeStoragePath(storage.getRecycleBinPath() + targetPath);
        fileStorageService.move(storage.getCode(), storage.getBucketName(), storage
            .getBucketName(), sourcePath, targetPath);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        FileDO file = this.getById(id);
        // 删除记录
        fileMapper.deleteWithoutRecycleBin(List.of(id), UserContextHolder.getUserId());
        if (FileTypeEnum.DIR.equals(file.getType())) {
            return;
        }
        // 删除文件
        StorageDO storage = storageService.getById(file.getStorageId());
        String sourcePath = normalizeStoragePath(
            storage.getRecycleBinPath() + normalizeStoragePath(file.getPath()));
        fileStorageService.delete(storage.getCode(), storage.getBucketName(), sourcePath);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clean() {
        // 查询回收站记录
        List<FileDO> list = fileMapper.selectListInRecycleBin();
        if (CollUtil.isEmpty(list)) {
            return;
        }
        try {
            InterceptorIgnoreHelper.handle(IgnoreStrategy.builder().blockAttack(true).build());
            // 删除记录
            fileMapper.cleanRecycleBin(UserContextHolder.getUserId());
            // 删除文件
            // 批量获取存储配置
            Map<Long, List<FileDO>> fileListGroup =
                list.stream().collect(Collectors.groupingBy(FileDO::getStorageId));
            List<StorageDO> storageList = storageService.listByIds(fileListGroup.keySet());
            Map<Long, StorageDO> storageGroup = storageList.stream()
                .collect(Collectors.toMap(StorageDO::getId, Function.identity(),
                    (existing, replacement) -> existing));
            // 删除文件
            for (Map.Entry<Long, List<FileDO>> entry : fileListGroup.entrySet()) {
                StorageDO storage = storageGroup.get(entry.getKey());
                List<String> deletePaths = entry.getValue()
                    .stream()
                    .filter(file -> !FileTypeEnum.DIR.equals(file.getType()))
                    .map(file -> normalizeStoragePath(
                        storage.getRecycleBinPath() + normalizeStoragePath(file
                            .getPath())))
                    .toList();
                if (CollUtil.isNotEmpty(deletePaths)) {
                    fileStorageService.batchDelete(storage.getCode(), storage.getBucketName(),
                        deletePaths);
                }
            }
        } finally {
            InterceptorIgnoreHelper.clearIgnoreStrategy();
        }
    }

    private String normalizeStoragePath(String path) {
        return StrUtil.removePrefix(path.replace("\\", StringConstants.SLASH)
            .replaceAll("/+", StringConstants.SLASH), StringConstants.SLASH);
    }

    /**
     * 根据 ID 查询
     *
     * @param id ID
     * @return 文件信息
     */
    private FileDO getById(Long id) {
        FileDO file = fileMapper.selectByIdInRecycleBin(id);
        if (file == null) {
            throw new BusinessException("ID 为 [%s] 的文件已不存在".formatted(id));
        }
        return file;
    }
}
