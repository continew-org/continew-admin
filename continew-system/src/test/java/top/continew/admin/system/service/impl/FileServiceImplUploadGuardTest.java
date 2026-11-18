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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import cn.hutool.extra.spring.SpringUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import top.continew.admin.common.enums.DisEnableStatusEnum;
import top.continew.admin.system.model.entity.StorageDO;
import top.continew.admin.system.service.StorageService;
import top.continew.starter.core.exception.BadRequestException;
import top.continew.starter.core.exception.BusinessException;
import top.continew.starter.storage.core.FileStorageService;

import jakarta.validation.Validator;

/**
 * 文件上传入口守卫单元测试。
 *
 * <p>覆盖上传入口的路径穿越、非法文件名、扩展名白名单与存储启用状态校验。
 * 上传编排与进度状态机由集成测试覆盖，这里只固定进入存储层之前的守卫行为。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class FileServiceImplUploadGuardTest {

    private static final String PARENT_PATH = "upload";

    private StorageService storageService;
    private FileStorageService fileStorageService;
    private FileServiceImpl fileService;
    private MockedStatic<SpringUtil> springUtilMock;

    @BeforeEach
    void setUp() {
        springUtilMock = mockStatic(SpringUtil.class);
        Validator validator = mock(Validator.class);
        springUtilMock.when(() -> SpringUtil.getBean(Validator.class))
            .thenReturn(validator);
        storageService = mock(StorageService.class);
        fileStorageService = mock(FileStorageService.class);
        fileService = new FileServiceImpl(fileStorageService);
        ReflectionTestUtils.setField(fileService, "storageService", storageService);
    }

    @AfterEach
    void tearDown() {
        springUtilMock.close();
    }

    @Test
    void shouldRejectPathTraversalInParentPath() {
        MockMultipartFile file = file("photo.png");
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> fileService.upload(file, "../evil", null));

        assertTrue(exception.getMessage().contains("路径段"));
        verifyNoInteractions(fileStorageService);
    }

    @Test
    void shouldRejectFileNameWithSeparator() {
        MockMultipartFile file = file("../photo.png");
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> fileService.upload(file, PARENT_PATH, null));

        assertTrue(exception.getMessage().contains("路径分隔符"));
        verifyNoInteractions(fileStorageService);
    }

    @Test
    void shouldRejectUnsupportedExtension() {
        MockMultipartFile file = file("evil.jsp");
        BusinessException exception = assertThrows(BusinessException.class,
            () -> fileService.upload(file, PARENT_PATH, null));

        assertTrue(exception.getMessage().contains("不支持的文件类型"));
        verifyNoInteractions(fileStorageService);
    }

    @Test
    void shouldRejectDisabledStorage() {
        when(storageService.getByCode(null)).thenReturn(storage(DisEnableStatusEnum.DISABLE));
        MockMultipartFile file = file("photo.png");

        BusinessException exception = assertThrows(BusinessException.class,
            () -> fileService.upload(file, PARENT_PATH, null));

        assertTrue(exception.getMessage().contains("请先启用存储"));
        verifyNoInteractions(fileStorageService);
    }

    private MockMultipartFile file(String name) {
        return new MockMultipartFile("file", name, "image/png", new byte[] {1, 2, 3});
    }

    private StorageDO storage(DisEnableStatusEnum status) {
        StorageDO storage = new StorageDO();
        storage.setId(2L);
        storage.setCode("local");
        storage.setBucketName("admin");
        storage.setStatus(status);
        return storage;
    }
}
