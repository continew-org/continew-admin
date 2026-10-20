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
import top.continew.admin.system.util.StoragePathUtils;
import top.continew.starter.core.exception.BusinessException;
import top.continew.starter.data.util.QueryWrapperHelper;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.resp.PageResp;
import top.continew.starter.storage.core.FileStorageService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
     * 探测与移动的失败均仅记录告警，不阻断主文件还原。
     * </p>
     *
     * @param file    文件
     * @param storage 存储配置
     */
    private void moveThumbnailBack(FileDO file, StorageDO storage) {
        if (StrUtil.isBlank(file.getThumbnailName())) {
            return;
        }
        String targetPath = normalizeStoragePath(file.getThumbnailPath());
        String sourcePath = normalizeStoragePath(storage.getRecycleBinPath() + targetPath);
        try {
            if (fileStorageService.exists(storage.getCode(), storage.getBucketName(), sourcePath)) {
                fileStorageService.move(storage.getCode(), storage.getBucketName(), storage
                    .getBucketName(), sourcePath, targetPath);
            }
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
                + normalizeStoragePath(file.getThumbnailPath()));
            try {
                fileStorageService
                    .delete(storage.getCode(), storage.getBucketName(), thumbnailPath);
            } catch (Exception e) {
                log.warn("缩略图 [{}] 删除失败", thumbnailPath, e);
            }
        }
        // 补偿清理遗留在原目录的缩略图（历史数据未随迁或移动失败被捕获的场景）
        this.cleanupOriginalThumbnail(file, storage);
    }

    /**
     * 补偿清理遗留在原目录的缩略图
     *
     * <p>
     * 历史回收记录的缩略图可能未随主文件移入回收站（含移动失败被捕获的场景），主文件
     * 清除时一并补偿清理。原目录路径是其他有效记录的主文件路径，或缩略图仍被同存储
     * 其他有效记录引用时跳过，防止误删恰与缩略图同名的有效主文件（回收记录自身已逻辑
     * 删除，不会命中）。删除本身幂等（本地 deleteIfExists / 对象存储 deleteObject），
     * 无需探测存在性；补偿为尽力而为，失败仅记录告警。
     * </p>
     *
     * @param file    文件
     * @param storage 存储配置
     */
    private void cleanupOriginalThumbnail(FileDO file, StorageDO storage) {
        if (StrUtil.isBlank(file.getThumbnailName())) {
            return;
        }
        String dbOriginalPath = file.getThumbnailPath();
        String originalPath = normalizeStoragePath(dbOriginalPath);
        try {
            boolean referenced = fileMapper.lambdaQuery()
                .eq(FileDO::getStorageId, storage.getId())
                .and(wrapper -> wrapper.eq(FileDO::getPath, dbOriginalPath)
                    .or(nested -> nested.eq(FileDO::getParentPath, file.getParentPath())
                        .eq(FileDO::getThumbnailName, file.getThumbnailName())))
                .exists();
            if (referenced) {
                return;
            }
            fileStorageService.delete(storage.getCode(), storage.getBucketName(), originalPath);
        } catch (Exception e) {
            log.warn("原目录缩略图 [{}] 补偿清理失败", originalPath, e);
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
                List<FileDO> files = entry.getValue()
                    .stream()
                    .filter(file -> !FileTypeEnum.DIR.equals(file.getType()))
                    .toList();
                // 遗留原目录缩略图候选（库内形态 → 物理形态）
                Map<String, String> originalThCandidates = new HashMap<>();
                Set<String> thumbnailNames = new HashSet<>();
                for (FileDO file : files) {
                    String thumbnailDbPath = file.getThumbnailPath();
                    if (thumbnailDbPath == null) {
                        continue;
                    }
                    originalThCandidates.put(thumbnailDbPath,
                        normalizeStoragePath(thumbnailDbPath));
                    thumbnailNames.add(file.getThumbnailName());
                }
                // 批量预查有效引用，构建跳过集合（防误删有效主文件/仍被引用的缩略图）
                Set<String> skipPaths = this.buildThumbnailSkipPaths(storage, originalThCandidates
                    .keySet(), thumbnailNames);
                // 回收站目录主文件与缩略图 + 未被引用的遗留原目录缩略图，合并单次批量删除；
                // 使用 LinkedHashSet 去重，避免缩略图路径与其他记录主路径重合时重复删除
                Set<String> deletePaths = new LinkedHashSet<>();
                files
                    .forEach(file -> deletePaths.addAll(this.buildRecycleFilePaths(storage, file)));
                originalThCandidates.forEach((dbPath, physicalPath) -> {
                    if (!skipPaths.contains(dbPath)) {
                        deletePaths.add(physicalPath);
                    }
                });
                if (CollUtil.isNotEmpty(deletePaths)) {
                    fileStorageService.batchDelete(storage.getCode(), storage.getBucketName(),
                        new ArrayList<>(deletePaths));
                }
            }
        } finally {
            InterceptorIgnoreHelper.clearIgnoreStrategy();
        }
    }

    private String normalizeStoragePath(String path) {
        return StoragePathUtils.normalize(path);
    }

    /**
     * 批量预查遗留原目录缩略图的有效引用，构建跳过集合
     *
     * <p>同存储下候选路径被其他有效记录用作主文件路径，或被其他有效记录用作缩略图路径时，
     * 对应库内路径加入跳过集合，防止补偿清理误删有效文件。主文件路径查询走
     * {@code idx_file_path}；缩略图路径先按名称缩小，再在内存中校验精确路径，避免
     * {@code parent_path × thumbnail_name} 的笛卡尔积语义误保护。</p>
     *
     * @param storage        存储配置
     * @param candidatePaths 遗留原目录缩略图候选（库内路径）
     * @param thumbnailNames 候选缩略图名称集合
     * @return 跳过集合（库内路径形态）
     */
    private Set<String> buildThumbnailSkipPaths(StorageDO storage, Set<String> candidatePaths,
        Set<String> thumbnailNames) {
        if (CollUtil.isEmpty(candidatePaths)) {
            return Set.of();
        }
        // 候选路径被其他有效记录用作主文件路径
        List<FileDO> mainPathFiles = fileMapper.lambdaQuery()
            .eq(FileDO::getStorageId, storage.getId())
            .in(FileDO::getPath, candidatePaths)
            .list();
        // 候选路径被其他有效记录用作缩略图路径
        List<FileDO> thumbnailFiles = CollUtil.isEmpty(thumbnailNames) ? List.of() : fileMapper
            .lambdaQuery()
            .eq(FileDO::getStorageId, storage.getId())
            .in(FileDO::getThumbnailName, thumbnailNames)
            .list();
        return resolveSkipPaths(candidatePaths, mainPathFiles, thumbnailFiles);
    }

    /**
     * 从有效记录中计算需要跳过的候选路径
     *
     * @param candidatePaths 候选路径
     * @param mainPathFiles  主文件路径命中的有效记录
     * @param thumbnailFiles 缩略图名称命中的有效记录
     * @return 跳过集合
     */
    static Set<String> resolveSkipPaths(Set<String> candidatePaths, List<FileDO> mainPathFiles,
        List<FileDO> thumbnailFiles) {
        Set<String> skipPaths = new HashSet<>();
        mainPathFiles.forEach(record -> skipPaths.add(record.getPath()));
        thumbnailFiles.forEach(record -> {
            String thumbnailPath = record.getThumbnailPath();
            if (thumbnailPath != null && candidatePaths.contains(thumbnailPath)) {
                skipPaths.add(thumbnailPath);
            }
        });
        return skipPaths;
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
        if (file.getThumbnailPath() != null) {
            paths.add(normalizeStoragePath(storage.getRecycleBinPath()
                + normalizeStoragePath(file.getThumbnailPath())));
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
