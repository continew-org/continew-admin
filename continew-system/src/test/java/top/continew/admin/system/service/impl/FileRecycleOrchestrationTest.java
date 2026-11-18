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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.hutool.core.io.file.FileNameUtil;
import com.baomidou.mybatisplus.core.plugins.IgnoreStrategy;
import com.baomidou.mybatisplus.core.plugins.InterceptorIgnoreHelper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import top.continew.admin.common.context.UserContextHolder;
import top.continew.admin.system.enums.FileTypeEnum;
import top.continew.admin.system.mapper.FileMapper;
import top.continew.admin.system.model.entity.FileDO;
import top.continew.admin.system.model.entity.StorageDO;
import top.continew.admin.system.service.StorageService;
import top.continew.starter.core.exception.BusinessException;
import top.continew.starter.storage.core.FileStorageService;

import java.util.List;
import java.util.Set;

/**
 * 文件回收站还原与删除编排单元测试。
 *
 * <p>覆盖共享引用的物理文件走复制/跳过而非移动/删除、缩略图随主文件迁移、
 * 清空的批量删除与补偿清理、回收记录不存在的拒绝。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class FileRecycleOrchestrationTest {

    private static final long FILE_ID = 9L;
    private static final long STORAGE_ID = 2L;
    private static final String STORAGE_CODE = "local";
    private static final String BUCKET = "admin";
    private static final String RECYCLE_BIN_PATH = "recycle-bin/";

    private FileMapper fileMapper;
    private StorageService storageService;
    private FileStorageService fileStorageService;
    private FileRecycleServiceImpl fileRecycleService;
    private MockedStatic<UserContextHolder> userContextHolderMock;

    @BeforeEach
    void setUp() {
        fileMapper = mock(FileMapper.class);
        storageService = mock(StorageService.class);
        fileStorageService = mock(FileStorageService.class);
        fileRecycleService = new FileRecycleServiceImpl(fileMapper, storageService,
            fileStorageService);
        userContextHolderMock = mockStatic(UserContextHolder.class);
        userContextHolderMock.when(UserContextHolder::getUserId).thenReturn(1L);
    }

    @AfterEach
    void tearDown() {
        userContextHolderMock.close();
    }

    @Test
    void shouldThrowWhenRecycleRecordMissing() {
        when(fileMapper.selectByIdInRecycleBin(FILE_ID)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> fileRecycleService
            .restore(FILE_ID));

        assertEquals("ID 为 [9] 的文件已不存在", exception.getMessage());
        verify(fileMapper, never()).restoreInRecycleBin(anyLong(), any(), any());
    }

    @Test
    void shouldOnlyRestoreRecordForDir() {
        when(fileMapper.selectByIdInRecycleBin(FILE_ID)).thenReturn(file(FileTypeEnum.DIR, null));

        fileRecycleService.restore(FILE_ID);

        verify(fileMapper).restoreInRecycleBin(eq(FILE_ID), eq(1L), any());
        verifyNoStorageOperations();
    }

    @Test
    void shouldMoveMainFileBackWhenNotReferenced() {
        stubRestore(file(FileTypeEnum.UNKNOWN, "upload/a.txt"), 0L);

        fileRecycleService.restore(FILE_ID);

        verify(fileStorageService).move(STORAGE_CODE, BUCKET, BUCKET, "recycle-bin/upload/a.txt",
            "upload/a.txt");
        verify(fileStorageService, never()).copy(anyString(), anyString(), anyString(), anyString(),
            anyString());
    }

    @Test
    void shouldCopyMainFileBackWhenReferencedByOtherRecord() {
        stubRestore(file(FileTypeEnum.UNKNOWN, "upload/a.txt"), 1L);

        fileRecycleService.restore(FILE_ID);

        // 共享引用的物理文件走复制，回收站副本保留给其他回收记录
        verify(fileStorageService).copy(STORAGE_CODE, BUCKET, BUCKET, "recycle-bin/upload/a.txt",
            "upload/a.txt");
        verify(fileStorageService, never()).move(anyString(), anyString(), anyString(), anyString(),
            anyString());
    }

    @Test
    void shouldMoveThumbnailBackWithMainFile() {
        FileDO file = file(FileTypeEnum.UNKNOWN, "upload/a.txt");
        // 缩略图路径为派生属性（parentPath + / + thumbnailName），无 setter
        file.setThumbnailName("a_thumb.png");
        stubRestore(file, 0L);
        when(fileStorageService.exists(STORAGE_CODE, BUCKET,
            "recycle-bin/upload/a_thumb.png")).thenReturn(true);

        fileRecycleService.restore(FILE_ID);

        verify(fileStorageService).move(STORAGE_CODE, BUCKET, BUCKET,
            "recycle-bin/upload/a_thumb.png", "upload/a_thumb.png");
    }

    @Test
    void shouldSkipMissingThumbnailOnRestore() {
        FileDO file = file(FileTypeEnum.UNKNOWN, "upload/a.txt");
        // 缩略图路径为派生属性（parentPath + / + thumbnailName），无 setter
        file.setThumbnailName("a_thumb.png");
        stubRestore(file, 0L);
        when(fileStorageService.exists(STORAGE_CODE, BUCKET,
            "recycle-bin/upload/a_thumb.png")).thenReturn(false);

        fileRecycleService.restore(FILE_ID);

        // 历史数据可能未随主文件入回收站，缩略图不存在时跳过且不报错
        verify(fileStorageService, never()).copy(anyString(), anyString(), anyString(), anyString(),
            anyString());
        verify(fileStorageService).move(STORAGE_CODE, BUCKET, BUCKET, "recycle-bin/upload/a.txt",
            "upload/a.txt");
    }

    @Test
    void shouldDeleteRecordAndSkipStorageForDir() {
        when(fileMapper.selectByIdInRecycleBin(FILE_ID)).thenReturn(file(FileTypeEnum.DIR, null));

        fileRecycleService.delete(FILE_ID);

        verify(fileMapper).deleteWithoutRecycleBin(eq(List.of(FILE_ID)), eq(1L), any());
        verifyNoStorageOperations();
    }

    @Test
    void shouldDeletePhysicalFileWhenNotReferenced() {
        stubDelete(file(FileTypeEnum.UNKNOWN, "upload/a.txt"), 0L);

        fileRecycleService.delete(FILE_ID);

        verify(fileStorageService).delete(STORAGE_CODE, BUCKET, "recycle-bin/upload/a.txt");
    }

    @Test
    void shouldKeepSharedMainFileOnDelete() {
        stubDelete(file(FileTypeEnum.UNKNOWN, "upload/a.txt"), 1L);

        fileRecycleService.delete(FILE_ID);

        // 主文件仍被其他回收记录引用时跳过物理删除
        verify(fileStorageService, never()).delete(anyString(), anyString(),
            eq("recycle-bin/upload/a.txt"));
    }

    @Test
    void shouldCleanupOriginalThumbnailOnDelete() {
        FileDO file = file(FileTypeEnum.UNKNOWN, "upload/a.txt");
        // 缩略图路径为派生属性（parentPath + / + thumbnailName），无 setter
        file.setThumbnailName("a_thumb.png");
        stubDelete(file, 0L);
        mockChainExists(false);

        fileRecycleService.delete(FILE_ID);

        // 历史遗留原目录缩略图补偿清理
        verify(fileStorageService).delete(STORAGE_CODE, BUCKET, "upload/a_thumb.png");
    }

    @Test
    void shouldNotFailDeleteWhenThumbnailStorageDown() {
        FileDO file = file(FileTypeEnum.UNKNOWN, "upload/a.txt");
        file.setThumbnailName("a_thumb.png");
        stubDelete(file, 0L);
        mockChainExists(true);
        // 主文件已删除成功后，缩略图删除失败仅告警
        doThrow(new RuntimeException("storage down")).when(fileStorageService)
            .delete(STORAGE_CODE, BUCKET, "upload/a_thumb.png");

        fileRecycleService.delete(FILE_ID);

        verify(fileMapper).deleteWithoutRecycleBin(eq(List.of(FILE_ID)), eq(1L), any());
    }

    @Test
    void shouldBatchDeleteByStorageOnClean() {
        when(fileMapper.selectListInRecycleBin()).thenReturn(List.of(file(FileTypeEnum.UNKNOWN,
            "upload/a.txt")));
        when(storageService.listByIds(Set.of(STORAGE_ID))).thenReturn(List.of(storage()));
        mockChainList(List.of(), List.of());

        try (MockedStatic<InterceptorIgnoreHelper> ignoreHelper = mockStatic(
            InterceptorIgnoreHelper.class)) {
            fileRecycleService.clean();
            ignoreHelper.verify(() -> InterceptorIgnoreHelper.handle(any(IgnoreStrategy.class)));
            ignoreHelper.verify(InterceptorIgnoreHelper::clearIgnoreStrategy);
        }

        verify(fileMapper).cleanRecycleBin(eq(1L), any());
        List<String> paths = captureBatchDeletePaths();
        assertTrue(paths.contains("recycle-bin/upload/a.txt"));
    }

    @Test
    void shouldReturnEarlyWhenRecycleBinEmptyOnClean() {
        when(fileMapper.selectListInRecycleBin()).thenReturn(List.of());

        fileRecycleService.clean();

        verify(fileMapper, never()).cleanRecycleBin(anyLong(), any());
        verifyNoStorageOperations();
    }

    @Test
    void shouldKeepReferencedThumbnailCandidatesOnClean() {
        FileDO file = file(FileTypeEnum.UNKNOWN, "upload/a.txt");
        // 缩略图路径为派生属性（parentPath + / + thumbnailName），无 setter
        file.setThumbnailName("a_thumb.png");
        when(fileMapper.selectListInRecycleBin()).thenReturn(List.of(file));
        when(storageService.listByIds(Set.of(STORAGE_ID))).thenReturn(List.of(storage()));
        // 候选缩略图路径已被有效记录用作主文件路径
        mockChainList(List.of(file(FileTypeEnum.UNKNOWN, "upload/a_thumb.png")), List.of());

        try (MockedStatic<InterceptorIgnoreHelper> ignoreHelper = mockStatic(
            InterceptorIgnoreHelper.class)) {
            fileRecycleService.clean();
        }

        List<String> paths = captureBatchDeletePaths();
        // 回收站内主文件与缩略图正常删除；遗留原目录缩略图因已被有效记录用作主文件路径而跳过
        assertEquals(List.of("recycle-bin/upload/a.txt", "recycle-bin/upload/a_thumb.png"), paths);
    }

    @SuppressWarnings("unchecked")
    private void mockChainList(List<FileDO> mainPathFiles, List<FileDO> thumbnailFiles) {
        LambdaQueryChainWrapper<FileDO> chain = mock(LambdaQueryChainWrapper.class);
        doReturn(chain).when(fileMapper).lambdaQuery();
        doReturn(chain).when(chain).eq(any(), any());
        doReturn(chain).when(chain).in(any(), any(java.util.Collection.class));
        // 主文件路径查询与缩略图名称查询各调用一次 list()
        when(chain.list()).thenReturn(mainPathFiles, thumbnailFiles);
    }

    /**
     * 桩补偿清理候选查询（lambdaQuery 单链，exists 语义）
     *
     * @param exists 候选路径是否仍被有效记录引用
     */
    private void mockChainExists(boolean exists) {
        LambdaQueryChainWrapper<FileDO> chain = mock(LambdaQueryChainWrapper.class);
        doReturn(chain).when(fileMapper).lambdaQuery();
        doReturn(chain).when(chain).eq(any(), any());
        doReturn(chain).when(chain).and(any());
        when(chain.exists()).thenReturn(exists);
    }

    private void stubRestore(FileDO file, long referenceCount) {
        when(fileMapper.selectByIdInRecycleBin(FILE_ID)).thenReturn(file);
        when(storageService.getById(STORAGE_ID)).thenReturn(storage());
        when(fileMapper.countRecycleBinReferences(anyLong(), anyString(), anyString(), anyString(),
            anyLong())).thenReturn(referenceCount);
    }

    private void stubDelete(FileDO file, long referenceCount) {
        stubRestore(file, referenceCount);
    }

    private List<String> captureBatchDeletePaths() {
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> pathsCaptor = ArgumentCaptor.forClass(List.class);
        verify(fileStorageService).batchDelete(eq(STORAGE_CODE), eq(BUCKET), pathsCaptor.capture());
        return pathsCaptor.getValue();
    }

    private void verifyNoStorageOperations() {
        verify(fileStorageService, never()).move(anyString(), anyString(), anyString(), anyString(),
            anyString());
        verify(fileStorageService, never()).copy(anyString(), anyString(), anyString(), anyString(),
            anyString());
        verify(fileStorageService, never()).delete(anyString(), anyString(), anyString());
        verify(fileStorageService, never()).batchDelete(anyString(), anyString(), anyList());
    }

    private FileDO file(FileTypeEnum type, String path) {
        // 库内 path 由 parentPath + name 派生（setParentPath 会联动重算 path），顺序不可颠倒
        FileDO file = new FileDO();
        file.setId(FILE_ID);
        file.setType(type);
        file.setName(FileNameUtil.getName(path));
        file.setStorageId(STORAGE_ID);
        file.setParentPath("upload");
        return file;
    }

    private StorageDO storage() {
        StorageDO storage = new StorageDO();
        storage.setId(STORAGE_ID);
        storage.setCode(STORAGE_CODE);
        storage.setBucketName(BUCKET);
        storage.setRecycleBinPath(RECYCLE_BIN_PATH);
        return storage;
    }
}
