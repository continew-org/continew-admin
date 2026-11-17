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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import top.continew.admin.common.enums.DisEnableStatusEnum;
import top.continew.admin.common.model.req.CommonStatusUpdateReq;
import top.continew.admin.system.enums.StorageTypeEnum;
import top.continew.admin.system.mapper.StorageMapper;
import top.continew.admin.system.model.entity.StorageDO;
import top.continew.admin.system.model.req.StorageReq;
import top.continew.admin.system.model.resp.file.FileUploadConfigResp;
import top.continew.admin.system.service.FileService;
import top.continew.starter.core.exception.BusinessException;
import top.continew.starter.data.mapper.BaseMapper;
import top.continew.starter.storage.autoconfigure.properties.StorageProperties;
import top.continew.starter.storage.core.FileStorageService;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

/**
 * 存储配置业务单元测试。
 *
 * <p>覆盖更新与删除的保护约束（类型/编码/回收站不可改、默认存储不可禁用删除）、
 * 状态切换与默认存储切换的引擎装载联动、默认存储与按编码查询的兜底语义。</p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
@ExtendWith(MockitoExtension.class)
class StorageServiceImplTest {

    @Mock
    private StorageMapper storageMapper;
    @Mock
    private FileStorageService fileStorageService;
    @Mock
    private FileService fileService;

    @Spy
    @InjectMocks
    private StorageServiceImpl storageService;

    private MockedStatic<SpringUtil> springUtilMock;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(storageService, "baseMapper", storageMapper);
        // CheckUtils 的静态块经 SpringUtil 取 Bean 校验器，以本地 Validator 供给
        springUtilMock = Mockito.mockStatic(SpringUtil.class);
        springUtilMock.when(() -> SpringUtil.getBean(Validator.class))
            .thenReturn(Validation.buildDefaultValidatorFactory().getValidator());
        // storageProperties 经构造注入，默认值即可支撑上传配置组装
        ReflectionTestUtils.setField(storageService, "storageProperties", new StorageProperties());
    }

    @AfterEach
    void tearDown() {
        springUtilMock.close();
    }

    @Test
    void shouldRejectTypeChangeOnUpdate() {
        StorageDO oldStorage = storage(1L, "local-1", StorageTypeEnum.LOCAL, false,
            DisEnableStatusEnum.ENABLE);
        stubOldStorage(oldStorage);
        StorageReq req = storageReq(StorageTypeEnum.OSS, "local-1", DisEnableStatusEnum.ENABLE);

        BusinessException exception = assertThrows(BusinessException.class, () -> storageService
            .beforeUpdate(req, 1L));
        assertEquals("不允许修改存储类型", exception.getMessage());
    }

    @Test
    void shouldRejectCodeChangeOnUpdate() {
        StorageDO oldStorage = storage(1L, "local-1", StorageTypeEnum.LOCAL, false,
            DisEnableStatusEnum.ENABLE);
        stubOldStorage(oldStorage);
        StorageReq req = storageReq(StorageTypeEnum.LOCAL, "local-2", DisEnableStatusEnum.ENABLE);

        BusinessException exception = assertThrows(BusinessException.class, () -> storageService
            .beforeUpdate(req, 1L));
        assertEquals("不允许修改存储编码", exception.getMessage());
    }

    @Test
    void shouldRejectRecycleBinChangeOnUpdate() {
        StorageDO oldStorage = storage(1L, "local-1", StorageTypeEnum.LOCAL, false,
            DisEnableStatusEnum.ENABLE);
        oldStorage.setRecycleBinEnabled(false);
        oldStorage.setRecycleBinPath("/recycle");
        stubOldStorage(oldStorage);
        StorageReq req = storageReq(StorageTypeEnum.LOCAL, "local-1", DisEnableStatusEnum.ENABLE);
        req.setRecycleBinEnabled(true);
        req.setRecycleBinPath("/recycle");

        BusinessException exception = assertThrows(BusinessException.class, () -> storageService
            .beforeUpdate(req, 1L));
        assertEquals("不允许修改回收站配置", exception.getMessage());
    }

    @Test
    void shouldRejectDisableDefaultStorageOnUpdate() {
        StorageDO oldStorage = storage(1L, "local-1", StorageTypeEnum.LOCAL, true,
            DisEnableStatusEnum.ENABLE);
        stubOldStorage(oldStorage);
        StorageReq req = storageReq(StorageTypeEnum.LOCAL, "local-1", DisEnableStatusEnum.DISABLE);
        req.setRecycleBinEnabled(false);
        req.setRecycleBinPath("/recycle");

        BusinessException exception = assertThrows(BusinessException.class, () -> storageService
            .beforeUpdate(req, 1L));
        assertEquals("[local-1] 是默认存储，不允许禁用", exception.getMessage());
    }

    @Test
    void shouldSkipStatusChangeWhenStatusUnchanged() {
        StorageDO storage = storage(1L, "local-1", StorageTypeEnum.LOCAL, false,
            DisEnableStatusEnum.ENABLE);
        stubOldStorage(storage);
        CommonStatusUpdateReq req = new CommonStatusUpdateReq();
        req.setStatus(DisEnableStatusEnum.ENABLE);

        storageService.updateStatus(req, 1L);

        Mockito.verifyNoInteractions(fileStorageService);
    }

    @Test
    void shouldLoadEngineWhenStatusEnabled() {
        StorageDO storage = storage(1L, "local-1", StorageTypeEnum.LOCAL, false,
            DisEnableStatusEnum.DISABLE);
        stubOldStorage(storage);
        CommonStatusUpdateReq req = new CommonStatusUpdateReq();
        req.setStatus(DisEnableStatusEnum.ENABLE);
        stubLambdaUpdateChain();
        when(fileStorageService.exists("local-1")).thenReturn(false);

        storageService.updateStatus(req, 1L);

        verify(fileStorageService).register(any());
    }

    @Test
    void shouldUnloadEngineWhenStatusDisabled() {
        StorageDO storage = storage(1L, "local-1", StorageTypeEnum.LOCAL, false,
            DisEnableStatusEnum.ENABLE);
        stubOldStorage(storage);
        CommonStatusUpdateReq req = new CommonStatusUpdateReq();
        req.setStatus(DisEnableStatusEnum.DISABLE);
        stubLambdaUpdateChain();
        when(fileStorageService.exists("local-1")).thenReturn(true);
        when(fileStorageService.isDynamic("local-1")).thenReturn(true);

        storageService.updateStatus(req, 1L);

        verify(fileStorageService).unload("local-1");
    }

    @Test
    void shouldRejectDisableDefaultStorageOnStatusChange() {
        StorageDO storage = storage(1L, "local-1", StorageTypeEnum.LOCAL, true,
            DisEnableStatusEnum.ENABLE);
        stubOldStorage(storage);
        CommonStatusUpdateReq req = new CommonStatusUpdateReq();
        req.setStatus(DisEnableStatusEnum.DISABLE);
        stubLambdaUpdateChain();

        BusinessException exception = assertThrows(BusinessException.class, () -> storageService
            .updateStatus(req, 1L));
        assertEquals("[local-1] 是默认存储，不允许禁用", exception.getMessage());
    }

    @Test
    void shouldSwitchDefaultStorageAndNotifyEngine() {
        StorageDO storage = storage(2L, "local-2", StorageTypeEnum.LOCAL, false,
            DisEnableStatusEnum.ENABLE);
        stubOldStorage(storage);
        stubLambdaUpdateChain();

        storageService.setDefaultStorage(2L);

        verify(fileStorageService).defaultStorage("local-2");
    }

    @Test
    void shouldSkipDefaultSwitchWhenAlreadyDefault() {
        StorageDO storage = storage(2L, "local-2", StorageTypeEnum.LOCAL, true,
            DisEnableStatusEnum.ENABLE);
        stubOldStorage(storage);

        storageService.setDefaultStorage(2L);

        Mockito.verifyNoInteractions(fileStorageService);
    }

    @Test
    void shouldRejectDefaultSwitchWhenDisabled() {
        StorageDO storage = storage(2L, "local-2", StorageTypeEnum.LOCAL, false,
            DisEnableStatusEnum.DISABLE);
        stubOldStorage(storage);

        BusinessException exception = assertThrows(BusinessException.class, () -> storageService
            .setDefaultStorage(2L));
        assertEquals("请先启用所选存储", exception.getMessage());
    }

    @Test
    void shouldFallbackToDefaultStorageWhenCodeBlank() {
        StorageDO storage = storage(1L, "local-1", StorageTypeEnum.LOCAL, true,
            DisEnableStatusEnum.ENABLE);
        LambdaQueryChainStub chain = stubLambdaQueryChain(storage, 0L);

        StorageDO result = storageService.getByCode("");

        assertEquals(storage, result);
        Mockito.verify(chain.query).eq(any(), any());
    }

    @Test
    void shouldRejectUnknownStorageCode() {
        stubLambdaQueryChain(null, 0L);

        BusinessException exception = assertThrows(BusinessException.class, () -> storageService
            .getByCode("missing"));
        assertEquals("code 为 [missing] 的 存储 记录已不存在", exception.getMessage());
    }

    @Test
    void shouldReturnNullWhenNoDefaultStorageConfigured() {
        stubLambdaQueryChain(null, 0L);
        // getDefaultStorage 在缺失默认存储时抛出，getByCode 的空编码兜底同样命中
        BusinessException exception = assertThrows(BusinessException.class, () -> storageService
            .getByCode(""));
        assertEquals("请先指定默认存储", exception.getMessage());
    }

    @Test
    void shouldAssembleUploadConfigForDefaultLocalStorage() {
        StorageDO storage = storage(1L, "local-1", StorageTypeEnum.LOCAL, true,
            DisEnableStatusEnum.ENABLE);
        stubLambdaQueryChain(storage, 0L);

        FileUploadConfigResp config = storageService.getDefaultUploadConfig();

        assertEquals("local-1", config.getStorageCode());
        assertEquals(StorageTypeEnum.LOCAL, config.getStorageType());
    }

    private StorageDO storage(Long id, String code, StorageTypeEnum type, boolean isDefault,
        DisEnableStatusEnum status) {
        StorageDO storage = new StorageDO();
        storage.setId(id);
        storage.setCode(code);
        storage.setName(code);
        storage.setType(type);
        storage.setIsDefault(isDefault);
        storage.setStatus(status);
        storage.setRecycleBinEnabled(false);
        storage.setRecycleBinPath("/recycle");
        return storage;
    }

    private StorageReq storageReq(StorageTypeEnum type, String code,
        DisEnableStatusEnum status) {
        StorageReq req = new StorageReq();
        req.setType(type);
        req.setCode(code);
        req.setName(code);
        req.setStatus(status);
        req.setRecycleBinEnabled(false);
        req.setRecycleBinPath("/recycle");
        return req;
    }

    private void stubOldStorage(StorageDO oldStorage) {
        doReturn(oldStorage).when(storageService).getById(oldStorage.getId());
    }

    private void stubLambdaUpdateChain() {
        LambdaUpdateChainStub stub = new LambdaUpdateChainStub();
        Mockito.doReturn(stub.chain).when((BaseMapper) storageMapper).lambdaUpdate();
    }

    private LambdaQueryChainStub stubLambdaQueryChain(StorageDO result, Long count) {
        LambdaQueryChainStub stub = new LambdaQueryChainStub(result, count);
        Mockito.doReturn(stub.chain).when((BaseMapper) storageMapper).lambdaQuery();
        return stub;
    }

    static class LambdaUpdateChainStub {

        final LambdaUpdateChainWrapper<StorageDO> chain = Mockito
            .mock(LambdaUpdateChainWrapper.class);

        LambdaUpdateChainStub() {
            Mockito.doReturn(chain).when(chain).eq(any(), any());
            Mockito.doReturn(chain).when(chain).set(any(), any());
            Mockito.lenient().when(chain.update()).thenReturn(true);
        }
    }

    static class LambdaQueryChainStub {

        final LambdaQueryChainWrapper<StorageDO> chain = Mockito
            .mock(LambdaQueryChainWrapper.class);
        final LambdaQueryChainWrapper<StorageDO> query = chain;

        LambdaQueryChainStub(StorageDO result, Long count) {
            Mockito.lenient().doReturn(chain).when(chain).eq(any(), any());
            Mockito.lenient().when(chain.one()).thenReturn(result);
            Mockito.lenient().when(chain.count()).thenReturn(count);
        }
    }
}
