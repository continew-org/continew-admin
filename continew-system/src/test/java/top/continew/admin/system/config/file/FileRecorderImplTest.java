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

package top.continew.admin.system.config.file;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import top.continew.admin.system.mapper.FileMapper;
import top.continew.admin.system.mapper.StorageMapper;
import top.continew.admin.system.model.entity.FileDO;
import top.continew.admin.system.model.entity.StorageDO;
import top.continew.admin.system.util.MultipartOriginalNameStore;
import top.continew.starter.storage.domain.model.resp.FileInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * 文件记录回填展示名测试
 *
 * <p>
 * 分片上传的落盘文件名是唯一 ID，启动器在完成上传时会把会话中的文件名（即唯一 ID）写入展示名，因此记录器必须用暂存的原始文件名覆盖它。
 * 这里固定两条契约：回填同时覆盖「新增」与「更新」两条落库分支；暂存缺失时保留原值，绝不写入空值。
 * </p>
 *
 * <p>
 * {@link MultipartOriginalNameStore} 的静态初始化只有日志器，不依赖 Spring 容器，因此可以直接静态 mock；这也是此处能写单元测试而
 * {@code RedisUtils} 不能的原因。
 * </p>
 *
 * @author luoqiz
 * @since 5.0.0
 */
class FileRecorderImplTest {

    private static final String STORED_NAME = "3f2a1c9e5b7d4a6f8c0e1d2b3a4c5d6e.pdf";

    private static final String ORIGINAL_NAME = "年度报告.pdf";

    private FileMapper fileMapper;

    private FileRecorderImpl fileRecorder;

    @BeforeEach
    void setUp() {
        this.fileMapper = mock(FileMapper.class);
        StorageMapper storageMapper = mock(StorageMapper.class);
        StorageDO storage = new StorageDO();
        storage.setId(1L);
        storage.setCode("local");
        this.stubStorageQuery(storageMapper, storage);
        this.fileRecorder = new FileRecorderImpl(this.fileMapper, storageMapper);
    }

    @Test
    void shouldBackfillOriginalNameWhenInserting() {
        this.stubFileQuery(null);
        ArgumentCaptor<FileDO> captor = ArgumentCaptor.forClass(FileDO.class);
        when(this.fileMapper.insert(captor.capture())).thenReturn(1);

        try (MockedStatic<MultipartOriginalNameStore> store =
            mockStatic(MultipartOriginalNameStore.class)) {
            store.when(() -> MultipartOriginalNameStore.resolveByMetadata(any()))
                .thenReturn(ORIGINAL_NAME);
            assertTrue(this.fileRecorder.update(this.buildFileInfo()));
        }

        assertEquals(ORIGINAL_NAME, captor.getValue().getOriginalName());
        assertEquals(STORED_NAME, captor.getValue().getName());
    }

    @Test
    void shouldBackfillOriginalNameWhenUpdatingExistingRecord() {
        FileDO existFile = new FileDO();
        existFile.setId(9L);
        this.stubFileQuery(existFile);
        ArgumentCaptor<FileDO> captor = ArgumentCaptor.forClass(FileDO.class);
        when(this.fileMapper.updateById(captor.capture())).thenReturn(1);

        try (MockedStatic<MultipartOriginalNameStore> store =
            mockStatic(MultipartOriginalNameStore.class)) {
            store.when(() -> MultipartOriginalNameStore.resolveByMetadata(any()))
                .thenReturn(ORIGINAL_NAME);
            assertTrue(this.fileRecorder.update(this.buildFileInfo()));
        }

        assertEquals(9L, captor.getValue().getId());
        assertEquals(ORIGINAL_NAME, captor.getValue().getOriginalName());
    }

    @Test
    void shouldKeepStoredNameWhenOriginalNameMissing() {
        this.stubFileQuery(null);
        ArgumentCaptor<FileDO> captor = ArgumentCaptor.forClass(FileDO.class);
        when(this.fileMapper.insert(captor.capture())).thenReturn(1);
        FileInfo fileInfo = this.buildFileInfo();
        fileInfo.getMetadata().remove("uploadId");

        try (MockedStatic<MultipartOriginalNameStore> store =
            mockStatic(MultipartOriginalNameStore.class)) {
            store.when(() -> MultipartOriginalNameStore.resolveByMetadata(any())).thenReturn(null);
            assertTrue(this.fileRecorder.update(fileInfo));
        }

        assertEquals(STORED_NAME, captor.getValue().getOriginalName());
    }

    @Test
    void shouldKeepExistingOriginalNameWhenOriginalNameMissing() {
        FileDO existFile = new FileDO();
        existFile.setId(9L);
        existFile.setOriginalName(ORIGINAL_NAME);
        this.stubFileQuery(existFile);
        ArgumentCaptor<FileDO> captor = ArgumentCaptor.forClass(FileDO.class);
        when(this.fileMapper.updateById(captor.capture())).thenReturn(1);
        FileInfo fileInfo = this.buildFileInfo();
        fileInfo.getMetadata().remove("uploadId");

        try (MockedStatic<MultipartOriginalNameStore> store =
            mockStatic(MultipartOriginalNameStore.class)) {
            store.when(() -> MultipartOriginalNameStore.resolveByMetadata(any())).thenReturn(null);
            assertTrue(this.fileRecorder.update(fileInfo));
        }

        assertEquals(ORIGINAL_NAME, captor.getValue().getOriginalName());
    }

    @SuppressWarnings("unchecked")
    private void stubStorageQuery(StorageMapper storageMapper, StorageDO storage) {
        LambdaQueryChainWrapper<StorageDO> query = mock(LambdaQueryChainWrapper.class);
        when(storageMapper.lambdaQuery()).thenReturn(query);
        when(query.eq(any(), any())).thenReturn(query);
        when(query.one()).thenReturn(storage);
    }

    @SuppressWarnings("unchecked")
    private void stubFileQuery(FileDO existFile) {
        LambdaQueryChainWrapper<FileDO> query = mock(LambdaQueryChainWrapper.class);
        when(this.fileMapper.lambdaQuery()).thenReturn(query);
        when(query.eq(any(), any())).thenReturn(query);
        when(query.one()).thenReturn(existFile);
    }

    private FileInfo buildFileInfo() {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setPlatform("local");
        fileInfo.setBucket("test");
        fileInfo.setName(STORED_NAME);
        fileInfo.setPath("2026/10/21/" + STORED_NAME);
        fileInfo.setOriginalFileName(STORED_NAME);
        fileInfo.setSize(1024L);
        Map<String, String> metadata = new HashMap<>();
        metadata.put("uploadId", "upload-1");
        metadata.put("sha256", "e3b0c44298fc1c149afbf4c8996fb924");
        fileInfo.setMetadata(metadata);
        return fileInfo;
    }
}
