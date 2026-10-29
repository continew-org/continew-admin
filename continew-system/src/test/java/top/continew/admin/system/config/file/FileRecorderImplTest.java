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
import static org.mockito.Mockito.when;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import top.continew.admin.system.mapper.FileMapper;
import top.continew.admin.system.mapper.StorageMapper;
import top.continew.admin.system.model.entity.FileDO;
import top.continew.admin.system.model.entity.StorageDO;
import top.continew.admin.system.model.resp.file.MultipartUploadCreateResp;
import top.continew.starter.storage.domain.model.resp.FileInfo;
import top.continew.starter.storage.domain.model.resp.MultipartInitResp;

import java.util.HashMap;
import java.util.Map;

/**
 * 文件记录展示名测试
 *
 * <p>
 * 分片上传的落盘文件名是唯一 ID，原始文件名由 starter 会话携带并在完成时回填到 {@link FileInfo}。这里固定三条契约：
 * 新增与更新都使用回填后的原始名；原始名为空时新增保留存储名、更新沿用已有展示名；会话 JSON 往返不丢原始名。
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
    void shouldUseOriginalNameWhenInserting() {
        this.stubFileQuery(null);
        ArgumentCaptor<FileDO> captor = ArgumentCaptor.forClass(FileDO.class);
        when(this.fileMapper.insert(captor.capture())).thenReturn(1);

        assertTrue(this.fileRecorder.update(this.buildFileInfo(ORIGINAL_NAME)));

        assertEquals(ORIGINAL_NAME, captor.getValue().getOriginalName());
        assertEquals(STORED_NAME, captor.getValue().getName());
    }

    @Test
    void shouldUseOriginalNameWhenUpdatingExistingRecord() {
        FileDO existFile = new FileDO();
        existFile.setId(9L);
        this.stubFileQuery(existFile);
        ArgumentCaptor<FileDO> captor = ArgumentCaptor.forClass(FileDO.class);
        when(this.fileMapper.updateById(captor.capture())).thenReturn(1);

        assertTrue(this.fileRecorder.update(this.buildFileInfo(ORIGINAL_NAME)));

        assertEquals(9L, captor.getValue().getId());
        assertEquals(ORIGINAL_NAME, captor.getValue().getOriginalName());
    }

    @Test
    void shouldKeepStoredNameWhenOriginalNameMissingOnInsert() {
        this.stubFileQuery(null);
        ArgumentCaptor<FileDO> captor = ArgumentCaptor.forClass(FileDO.class);
        when(this.fileMapper.insert(captor.capture())).thenReturn(1);

        assertTrue(this.fileRecorder.update(this.buildFileInfo(null)));

        assertEquals(STORED_NAME, captor.getValue().getOriginalName());
    }

    @Test
    void shouldKeepExistingOriginalNameWhenOriginalNameMissingOnUpdate() {
        FileDO existFile = new FileDO();
        existFile.setId(9L);
        existFile.setOriginalName(ORIGINAL_NAME);
        this.stubFileQuery(existFile);
        ArgumentCaptor<FileDO> captor = ArgumentCaptor.forClass(FileDO.class);
        when(this.fileMapper.updateById(captor.capture())).thenReturn(1);

        assertTrue(this.fileRecorder.update(this.buildFileInfo(null)));

        assertEquals(ORIGINAL_NAME, captor.getValue().getOriginalName());
    }

    @Test
    void shouldPreserveOriginalNameAcrossSessionJsonRoundTrip() {
        MultipartInitResp session = new MultipartInitResp();
        session.setUploadId("upload-1");
        session.setFileName(STORED_NAME);
        session.setOriginalFileName(ORIGINAL_NAME);
        session.setFileSize(1024L);

        MultipartUploadCreateResp stored = FileRecorderImpl.toCreateResp(session);
        MultipartUploadCreateResp restored = JSONUtil.toBean(JSONUtil.toJsonStr(stored),
            MultipartUploadCreateResp.class);
        MultipartInitResp actual = FileRecorderImpl.toInitResp(restored);

        assertEquals(ORIGINAL_NAME, actual.getOriginalFileName());
        assertEquals(STORED_NAME, actual.getFileName());
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

    private FileInfo buildFileInfo(String originalFileName) {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setPlatform("local");
        fileInfo.setBucket("test");
        fileInfo.setName(STORED_NAME);
        fileInfo.setPath("2026/10/21/" + STORED_NAME);
        fileInfo.setOriginalFileName(originalFileName);
        fileInfo.setSize(1024L);
        Map<String, String> metadata = new HashMap<>();
        metadata.put("uploadId", "upload-1");
        metadata.put("sha256", "e3b0c44298fc1c149afbf4c8996fb924");
        fileInfo.setMetadata(metadata);
        return fileInfo;
    }
}
