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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import org.junit.jupiter.api.Test;
import top.continew.admin.system.enums.FileTypeEnum;
import top.continew.admin.system.mapper.FileMapper;
import top.continew.admin.system.model.entity.FileDO;
import top.continew.admin.system.model.entity.StorageDO;
import top.continew.admin.system.service.StorageService;
import top.continew.starter.storage.core.FileStorageService;

import java.util.List;
import java.util.Set;

/**
 * 回收站缩略图清理测试
 *
 * @author luoqiz
 * @since 4.2.0
 */
class FileRecycleServiceImplTest {

    @Test
    void shouldSkipCandidateUsedAsMainPath() {
        String candidate = "/dir/thumb.jpg";
        FileDO file = new FileDO();
        file.setPath(candidate);
        Set<String> skipPaths =
            FileRecycleServiceImpl.resolveSkipPaths(Set.of(candidate), List.of(file), List.of());
        assertEquals(Set.of(candidate), skipPaths);
    }

    @Test
    void shouldSkipCandidateUsedAsThumbnail() {
        String candidate = "/dir/thumb.jpg";
        FileDO file = new FileDO();
        file.setParentPath("/dir");
        file.setThumbnailName("thumb.jpg");
        Set<String> skipPaths =
            FileRecycleServiceImpl.resolveSkipPaths(Set.of(candidate), List.of(), List.of(file));
        assertTrue(skipPaths.contains(candidate));
    }

    @Test
    void shouldNotSkipSameThumbnailNameInDifferentDirectory() {
        String candidate = "/dir/thumb.jpg";
        FileDO file = new FileDO();
        file.setParentPath("/other");
        file.setThumbnailName("thumb.jpg");
        Set<String> skipPaths =
            FileRecycleServiceImpl.resolveSkipPaths(Set.of(candidate), List.of(), List.of(file));
        assertTrue(skipPaths.isEmpty());
    }

    @Test
    void shouldCopyRecycleThumbnailWhenReferencedByOtherRecord() {
        // A 的缩略图路径恰好等于 B 的主文件路径，二者同批删除时只移动一次物理文件，
        // B 仍留在回收站：单独还原 A 时缩略图应采用复制而非移动，保留 B 的源文件
        FileMapper fileMapper = mock(FileMapper.class);
        StorageService storageService = mock(StorageService.class);
        FileStorageService fileStorageService = mock(FileStorageService.class);
        FileRecycleServiceImpl recycleService =
            new FileRecycleServiceImpl(fileMapper, storageService, fileStorageService);

        StorageDO storage = new StorageDO();
        storage.setId(1L);
        storage.setCode("local");
        storage.setBucketName("bucket");
        // 回收站路径与库内路径直接拼接，生产数据以 “/” 结尾（见 StorageTypeEnum#pretreatment）
        storage.setRecycleBinPath("recycle/");

        FileDO file = recycledFile(10L, "a.png", "thumb.png");
        when(fileMapper.selectByIdInRecycleBin(10L)).thenReturn(file);
        when(storageService.getById(1L)).thenReturn(storage);
        when(fileStorageService.exists(eq("local"), eq("bucket"),
            eq("recycle/user/avatar/thumb.png"))).thenReturn(true);
        // 主文件无其他回收记录引用（移动），缩略图仍被 B 引用（复制）
        when(fileMapper.countRecycleBinReferences(eq(1L), eq("/user/avatar/a.png"), any(), any(),
            eq(10L))).thenReturn(0L);
        when(fileMapper.countRecycleBinReferences(eq(1L), eq("/user/avatar/thumb.png"), any(),
            any(), eq(10L))).thenReturn(1L);

        recycleService.restore(10L);

        // 主文件正常移动；缩略图改为复制，"先还原 A" 不会取走 B 的主文件，
        // 之后再还原 B 时源文件仍在回收站
        verify(fileStorageService).move("local", "bucket", "bucket",
            "recycle/user/avatar/a.png", "user/avatar/a.png");
        verify(fileStorageService).copy("local", "bucket", "bucket",
            "recycle/user/avatar/thumb.png", "user/avatar/thumb.png");
        verify(fileStorageService, never()).move(anyString(), anyString(), anyString(),
            eq("recycle/user/avatar/thumb.png"), anyString());
    }

    @Test
    void shouldSkipRecycleBinThumbnailDeleteWhenReferencedByOtherRecord() {
        // A 的缩略图路径恰好等于 B 的主文件路径，B 仍在回收站：单独彻底删除 A 时
        // 不能删除回收站中的该物理文件，否则 B 将无法还原
        FileMapper fileMapper = mock(FileMapper.class);
        StorageService storageService = mock(StorageService.class);
        FileStorageService fileStorageService = mock(FileStorageService.class);
        FileRecycleServiceImpl recycleService =
            new FileRecycleServiceImpl(fileMapper, storageService, fileStorageService);

        StorageDO storage = new StorageDO();
        storage.setId(1L);
        storage.setCode("local");
        storage.setBucketName("bucket");
        // 回收站路径与库内路径直接拼接，生产数据以 “/” 结尾（见 StorageTypeEnum#pretreatment）
        storage.setRecycleBinPath("recycle/");

        FileDO file = recycledFile(10L, "a.png", "thumb.png");
        when(fileMapper.selectByIdInRecycleBin(10L)).thenReturn(file);
        when(storageService.getById(1L)).thenReturn(storage);
        // 主文件无其他回收记录引用（删除），缩略图仍被 B 引用（跳过）
        when(fileMapper.countRecycleBinReferences(eq(1L), eq("/user/avatar/a.png"), any(), any(),
            eq(10L))).thenReturn(0L);
        when(fileMapper.countRecycleBinReferences(eq(1L), eq("/user/avatar/thumb.png"), any(),
            any(), eq(10L))).thenReturn(1L);
        // 原目录缩略图仍被有效记录引用，跳过补偿清理
        mockOriginalThumbnailReferenced(fileMapper);

        recycleService.delete(10L);

        // 回收站主文件照常删除；回收站缩略图因仍被 B 引用而保留
        verify(fileStorageService).delete("local", "bucket", "recycle/user/avatar/a.png");
        verify(fileStorageService, never()).delete(eq("local"), eq("bucket"),
            eq("recycle/user/avatar/thumb.png"));
    }

    /**
     * 模拟原目录缩略图仍被有效记录引用，回收站清理时跳过原目录补偿删除
     *
     * @param fileMapper 文件 Mapper
     */
    private void mockOriginalThumbnailReferenced(FileMapper fileMapper) {
        LambdaQueryChainWrapper<FileDO> wrapper = mock(LambdaQueryChainWrapper.class);
        when(fileMapper.lambdaQuery()).thenReturn(wrapper);
        when(wrapper.eq(any(), any())).thenReturn(wrapper);
        when(wrapper.and(any())).thenReturn(wrapper);
        when(wrapper.exists()).thenReturn(true);
    }

    private FileDO recycledFile(Long id, String name, String thumbnailName) {
        FileDO file = new FileDO();
        file.setId(id);
        file.setName(name);
        file.setParentPath("/user/avatar");
        file.setThumbnailName(thumbnailName);
        file.setType(FileTypeEnum.IMAGE);
        file.setStorageId(1L);
        return file;
    }
}
