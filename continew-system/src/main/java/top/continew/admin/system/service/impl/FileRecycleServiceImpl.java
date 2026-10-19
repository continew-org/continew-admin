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
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.plugins.IgnoreStrategy;
import com.baomidou.mybatisplus.core.plugins.InterceptorIgnoreHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.continew.admin.common.constant.GlobalConstants;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 文件回收站业务实现
 *
 * @author Charles7c
 * @since 4.1.0
 */
@Slf4j
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
        fileMapper.restoreInRecycleBin(id, UserContextHolder.getUserId(), LocalDateTime
            .now(GlobalConstants.DEFAULT_ZONE_ID));
        if (FileTypeEnum.DIR.equals(file.getType())) {
            return;
        }
        // 还原文件
        StorageDO storage = storageService.getById(file.getStorageId());
        String targetPath = normalizeStoragePath(file.getPath());
        String sourcePath = normalizeStoragePath(storage.getRecycleBinPath() + targetPath);
        fileStorageService.move(storage.getCode(), storage.getBucketName(), storage
            .getBucketName(), sourcePath, targetPath);
        // 缩略图随主文件一并移回原目录
        this.moveThumbnailBack(file, storage);
    }

    /**
     * 将缩略图从回收站目录移回原目录
     *
     * <p>
     * 历史数据可能未随主文件移入回收站，源文件不存在时跳过；缩略图为附属文件，
     * 操作失败仅记录告警，不阻断主文件还原。
     * </p>
     *
     * @param file    文件
     * @param storage 存储配置
     */
    private void moveThumbnailBack(FileDO file, StorageDO storage) {
        if (StrUtil.isBlank(file.getThumbnailName())) {
            return;
        }
        String targetPath = normalizeStoragePath(file.getParentPath() + StringConstants.SLASH + file
            .getThumbnailName());
        String sourcePath = normalizeStoragePath(storage.getRecycleBinPath() + targetPath);
        if (!fileStorageService.exists(storage.getCode(), storage.getBucketName(), sourcePath)) {
            return;
        }
        try {
            fileStorageService.move(storage.getCode(), storage.getBucketName(), storage
                .getBucketName(), sourcePath, targetPath);
        } catch (Exception e) {
            log.warn("缩略图 [{}] 移回原目录失败", sourcePath, e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        FileDO file = this.getById(id);
        // 删除记录
        fileMapper.deleteWithoutRecycleBin(List.of(id), UserContextHolder.getUserId(), LocalDateTime
            .now(GlobalConstants.DEFAULT_ZONE_ID));
        if (FileTypeEnum.DIR.equals(file.getType())) {
            return;
        }
        // 删除文件
        StorageDO storage = storageService.getById(file.getStorageId());
        String sourcePath = normalizeStoragePath(
            storage.getRecycleBinPath() + normalizeStoragePath(file.getPath()));
        fileStorageService.delete(storage.getCode(), storage.getBucketName(), sourcePath);
        // 缩略图随主文件一并删除（附属文件，失败仅告警，不阻断主文件删除）
        if (StrUtil.isNotBlank(file.getThumbnailName())) {
            String thumbnailPath = normalizeStoragePath(storage.getRecycleBinPath()
                + normalizeStoragePath(file.getParentPath() + StringConstants.SLASH + file
                    .getThumbnailName()));
            try {
                fileStorageService
                    .delete(storage.getCode(), storage.getBucketName(), thumbnailPath);
            } catch (Exception e) {
                log.warn("缩略图 [{}] 删除失败", thumbnailPath, e);
            }
        }
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
            fileMapper.cleanRecycleBin(UserContextHolder.getUserId(), LocalDateTime
                .now(GlobalConstants.DEFAULT_ZONE_ID));
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
                    .map(file -> this.buildRecycleFilePaths(storage, file))
                    .flatMap(List::stream)
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
     * 构建回收站中文件待删除的物理路径列表（主文件 + 缩略图）
     *
     * @param storage 存储配置
     * @param file    文件
     * @return 物理路径列表
     */
    private List<String> buildRecycleFilePaths(StorageDO storage, FileDO file) {
        List<String> paths = new ArrayList<>();
        paths.add(normalizeStoragePath(storage.getRecycleBinPath() + normalizeStoragePath(file
            .getPath())));
        if (StrUtil.isNotBlank(file.getThumbnailName())) {
            paths.add(normalizeStoragePath(storage.getRecycleBinPath()
                + normalizeStoragePath(file.getParentPath() + StringConstants.SLASH + file
                    .getThumbnailName())));
        }
        return paths;
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
