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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import top.continew.admin.system.enums.FileTypeEnum;
import top.continew.admin.system.mapper.FileMapper;
import top.continew.admin.system.model.entity.FileDO;
import top.continew.admin.system.model.entity.StorageDO;
import top.continew.admin.system.model.req.MultipartUploadCreateReq;
import top.continew.admin.system.model.resp.file.MultipartUploadCreateResp;
import top.continew.admin.system.model.resp.file.MultipartUploadPartResp;
import top.continew.admin.system.service.FileService;
import top.continew.admin.system.service.StorageService;
import top.continew.starter.core.exception.BaseException;
import top.continew.starter.storage.core.FileStorageService;
import top.continew.starter.storage.domain.model.req.MultipartUploadInitReq;
import top.continew.starter.storage.domain.model.resp.FileInfo;
import top.continew.starter.storage.domain.model.resp.MultipartInitResp;
import top.continew.starter.storage.domain.model.resp.MultipartUploadResp;

import jakarta.validation.Validator;
import java.io.IOException;

/**
 * 分片上传业务实现单元测试。
 *
 * <p>覆盖初始化时的扩展名白名单、路径校验、文件名查重与请求组装，
 * 上传分片的会话校验、序号/大小校验与路径回退，完成上传的查库命中与补录，以及取消透传。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class MultipartUploadServiceImplTest {

    private static final String UPLOAD_ID = "upload-1";
    private static final String PLATFORM = "local";
    private static final String BUCKET = "admin";

    private StorageService storageService;
    private FileStorageService fileStorageService;
    private FileService fileService;
    private FileMapper fileMapper;
    private MultipartUploadServiceImpl service;
    private MockedStatic<SpringUtil> springUtilMock;

    @BeforeEach
    void setUp() {
        // CheckUtils 父类 Validator 的静态实例取自 Spring 上下文，单测环境以恒通过的 mock 供给
        springUtilMock = mockStatic(SpringUtil.class);
        springUtilMock.when(() -> SpringUtil.getBean(Validator.class))
            .thenReturn(mock(Validator.class));
        storageService = mock(StorageService.class);
        fileStorageService = mock(FileStorageService.class);
        fileService = mock(FileService.class);
        fileMapper = mock(FileMapper.class);
        service = new MultipartUploadServiceImpl(storageService, fileStorageService, fileService,
            fileMapper);
    }

    @AfterEach
    void tearDown() {
        springUtilMock.close();
    }

    @Test
    void shouldRejectUnsupportedExtensionOnInit() {
        BaseException exception = assertThrows(BaseException.class,
            () -> service.initMultipartUpload(req("evil.jsp")));

        assertTrue(exception.getMessage().contains("不支持的文件类型"));
        verify(fileMapper, never()).lambdaQuery();
    }

    @Test
    void shouldRejectFileNameWithPathSeparatorOnInit() {
        BaseException exception = assertThrows(BaseException.class,
            () -> service.initMultipartUpload(req("../evil.png")));

        assertTrue(exception.getMessage().contains("路径分隔符"));
        verify(fileMapper, never()).lambdaQuery();
    }

    @Test
    void shouldRejectDuplicateFileNameOnInit() {
        when(storageService.getByCode(null)).thenReturn(storage());
        stubExists(true);

        BaseException exception = assertThrows(BaseException.class,
            () -> service.initMultipartUpload(req("photo.png")));

        assertEquals("文件名已存在：photo.png", exception.getMessage());
        verify(fileService, never()).createParentDir(anyString(), any());
    }

    @Test
    void shouldInitUploadWithGeneratedStoredName() {
        when(storageService.getByCode(null)).thenReturn(storage());
        stubExists(false);
        MultipartInitResp initResp = new MultipartInitResp();
        initResp.setFileId("fid-1");
        initResp.setUploadId(UPLOAD_ID);
        initResp.setPath("upload/photo.png");
        initResp.setFileSize(100L);
        when(fileStorageService.initMultipartUpload(any())).thenReturn(initResp);

        MultipartUploadCreateResp result = service.initMultipartUpload(req("photo.png"));

        assertEquals(UPLOAD_ID, result.getUploadId());
        assertEquals("/upload/photo.png", result.getPath());
        ArgumentCaptor<MultipartUploadInitReq> captor =
            ArgumentCaptor.forClass(MultipartUploadInitReq.class);
        verify(fileStorageService).initMultipartUpload(captor.capture());
        assertEquals(PLATFORM, captor.getValue().getPlatform());
        assertEquals(BUCKET, captor.getValue().getBucket());
        // 落盘名为唯一 ID，不等于原始文件名
        assertNotNull(captor.getValue().getFileName());
        assertTrue(!captor.getValue().getFileName().equals("photo.png"));
        verify(fileService).createParentDir(eq("upload"), any());
    }

    @Test
    void shouldRejectUploadPartWithInvalidUploadId() {
        when(fileStorageService.getMultipartSession(UPLOAD_ID)).thenReturn(null);

        BaseException exception = assertThrows(BaseException.class,
            () -> service.uploadPart(partFile(10), UPLOAD_ID, 1, "upload/photo.png"));

        assertEquals("无效的 uploadId: " + UPLOAD_ID, exception.getMessage());
    }

    @Test
    void shouldRejectUploadPartWithBadPartNumber() {
        when(fileStorageService.getMultipartSession(UPLOAD_ID)).thenReturn(session(100L, 50L));

        BaseException exception = assertThrows(BaseException.class,
            () -> service.uploadPart(partFile(50), UPLOAD_ID, 0, "upload/photo.png"));

        assertTrue(exception.getMessage().contains("分片序号不合法"));
    }

    @Test
    void shouldRejectUploadPartWithOutOfRangePartNumber() {
        when(fileStorageService.getMultipartSession(UPLOAD_ID)).thenReturn(session(100L, 50L));

        BaseException exception = assertThrows(BaseException.class,
            () -> service.uploadPart(partFile(50), UPLOAD_ID, 3, "upload/photo.png"));

        assertTrue(exception.getMessage().contains("分片序号超出范围"));
    }

    @Test
    void shouldRejectUploadPartWithWrongSize() {
        when(fileStorageService.getMultipartSession(UPLOAD_ID)).thenReturn(session(100L, 50L));

        BaseException exception = assertThrows(BaseException.class,
            () -> service.uploadPart(partFile(49), UPLOAD_ID, 1, "upload/photo.png"));

        assertTrue(exception.getMessage().contains("分片大小不匹配"));
    }

    @Test
    void shouldUploadPartUsingSessionPath() {
        MultipartInitResp session = session(100L, 50L);
        session.setPath("upload/stored.png");
        when(fileStorageService.getMultipartSession(UPLOAD_ID)).thenReturn(session);
        MultipartUploadResp resp = new MultipartUploadResp();
        resp.setPartNumber(1);
        resp.setPartETag("etag-1");
        resp.setPartSize(50L);
        resp.setSuccess(true);
        when(fileStorageService.uploadPart(eq(PLATFORM), eq(BUCKET), anyString(), eq(UPLOAD_ID),
            anyInt(), any())).thenReturn(resp);

        MultipartUploadPartResp result = service.uploadPart(partFile(50), UPLOAD_ID, 1,
            "ignored/path.png");

        assertEquals(1, result.getPartNumber());
        assertEquals("etag-1", result.getPartETag());
        assertTrue(result.isSuccess());
        // 会话带路径时优先用会话路径，忽略调用方入参
        verify(fileStorageService).uploadPart(eq(PLATFORM), eq(BUCKET), eq("upload/stored.png"),
            eq(UPLOAD_ID), eq(1), any());
    }

    @Test
    void shouldValidateFallbackPathWhenSessionHasNoPath() {
        when(fileStorageService.getMultipartSession(UPLOAD_ID)).thenReturn(session(100L, 50L));

        BaseException exception = assertThrows(BaseException.class,
            () -> service.uploadPart(partFile(50), UPLOAD_ID, 1, "../evil.png"));

        assertTrue(exception.getMessage().contains("路径段"));
    }

    @Test
    void shouldWrapIoFailureOnUploadPart() throws IOException {
        MultipartInitResp session = session(100L, 50L);
        session.setPath("upload/stored.png");
        when(fileStorageService.getMultipartSession(UPLOAD_ID)).thenReturn(session);
        MultipartFile brokenFile = mock(MultipartFile.class);
        when(brokenFile.getSize()).thenReturn(50L);
        when(brokenFile.getInputStream()).thenThrow(new IOException("boom"));

        BaseException exception = assertThrows(BaseException.class,
            () -> service.uploadPart(brokenFile, UPLOAD_ID, 1, "upload/stored.png"));

        assertTrue(exception.getMessage().contains("上传分片失败"));
    }

    @Test
    void shouldRejectCompleteWithInvalidUploadId() {
        when(fileStorageService.getMultipartSession(UPLOAD_ID)).thenReturn(null);

        BaseException exception = assertThrows(BaseException.class,
            () -> service.completeMultipartUpload(UPLOAD_ID));

        assertEquals("无效的 uploadId: " + UPLOAD_ID, exception.getMessage());
    }

    @Test
    void shouldReturnExistingFileOnComplete() {
        MultipartInitResp session = session(100L, 50L);
        session.setPath("upload/photo.png");
        when(fileStorageService.getMultipartSession(UPLOAD_ID)).thenReturn(session);
        when(fileStorageService.completeMultipartUpload(eq(UPLOAD_ID), any())).thenReturn(
            new FileInfo());
        when(storageService.getByCode(PLATFORM)).thenReturn(storage());
        FileDO existing = new FileDO();
        stubOne(existing);

        assertSame(existing, service.completeMultipartUpload(UPLOAD_ID));
        verify(fileService, never()).save(any());
    }

    @Test
    void shouldFallbackSaveWhenNoRecordOnComplete() {
        MultipartInitResp session = session(100L, 50L);
        session.setPath("upload/photo.png");
        when(fileStorageService.getMultipartSession(UPLOAD_ID)).thenReturn(session);
        FileInfo fileInfo = new FileInfo();
        fileInfo.setName("photo.png");
        fileInfo.setPath("upload/photo.png");
        fileInfo.setSize(100L);
        when(fileStorageService.completeMultipartUpload(eq(UPLOAD_ID), any())).thenReturn(fileInfo);
        when(storageService.getByCode(PLATFORM)).thenReturn(storage());
        stubOne(null);

        FileDO result = service.completeMultipartUpload(UPLOAD_ID);

        assertEquals(FileTypeEnum.IMAGE, result.getType());
        verify(fileService).save(any());
    }

    @Test
    void shouldAbortOnCancel() {
        service.cancelMultipartUpload(UPLOAD_ID);

        verify(fileStorageService).abortMultipartUpload(UPLOAD_ID);
    }

    private MultipartUploadCreateReq req(String fileName) {
        MultipartUploadCreateReq req = new MultipartUploadCreateReq();
        req.setFileName(fileName);
        req.setFileSize(100L);
        req.setFileMd5("md5");
        req.setParentPath("upload");
        return req;
    }

    private StorageDO storage() {
        StorageDO storage = new StorageDO();
        storage.setId(2L);
        storage.setCode(PLATFORM);
        storage.setBucketName(BUCKET);
        return storage;
    }

    private MultipartInitResp session(Long partSize, Long fileSize) {
        MultipartInitResp session = new MultipartInitResp();
        session.setPlatform(PLATFORM);
        session.setBucket(BUCKET);
        session.setPartSize(partSize);
        session.setFileSize(fileSize);
        return session;
    }

    private MockMultipartFile partFile(long size) {
        return new MockMultipartFile("file", new byte[(int) size]);
    }

    @SuppressWarnings("unchecked")
    private void stubExists(boolean exists) {
        LambdaQueryChainWrapper<FileDO> chain = mock(LambdaQueryChainWrapper.class);
        when(fileMapper.lambdaQuery()).thenReturn(chain);
        lenient().when(chain.eq(any(SFunction.class), any())).thenReturn(chain);
        lenient().when(chain.ne(any(SFunction.class), any())).thenReturn(chain);
        when(chain.exists()).thenReturn(exists);
    }

    @SuppressWarnings("unchecked")
    private void stubOne(FileDO result) {
        LambdaQueryChainWrapper<FileDO> chain = mock(LambdaQueryChainWrapper.class);
        when(fileMapper.lambdaQuery()).thenReturn(chain);
        lenient().when(chain.eq(any(SFunction.class), any())).thenReturn(chain);
        when(chain.one()).thenReturn(result);
    }
}
