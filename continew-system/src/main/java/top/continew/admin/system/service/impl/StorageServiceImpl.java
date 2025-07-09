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

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.dromara.x.file.storage.core.FileStorageProperties;
import org.dromara.x.file.storage.core.FileStorageService;
import org.dromara.x.file.storage.core.FileStorageServiceBuilder;
import org.dromara.x.file.storage.core.platform.FileStorage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.continew.admin.common.enums.DisEnableStatusEnum;
import top.continew.admin.common.model.req.CommonStatusUpdateReq;
import top.continew.admin.common.util.SecureUtils;
import top.continew.admin.system.enums.StorageTypeEnum;
import top.continew.admin.system.mapper.StorageMapper;
import top.continew.admin.system.model.entity.StorageDO;
import top.continew.admin.system.model.query.StorageQuery;
import top.continew.admin.system.model.req.StorageReq;
import top.continew.admin.system.model.resp.StorageResp;
import top.continew.admin.system.service.FileService;
import top.continew.admin.system.service.StorageService;
import top.continew.starter.core.constant.StringConstants;
import top.continew.starter.core.util.ExceptionUtils;
import top.continew.starter.core.validation.CheckUtils;
import top.continew.starter.core.validation.ValidationUtils;
import top.continew.starter.extension.crud.service.BaseServiceImpl;
import top.continew.starter.core.util.SpringWebUtils;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 存储业务实现
 *
 * @author Charles7c
 * @since 2023/12/26 22:09
 */
@Service
@RequiredArgsConstructor
public class StorageServiceImpl extends BaseServiceImpl<StorageMapper, StorageDO, StorageResp, StorageResp, StorageQuery, StorageReq> implements StorageService {

    private final FileStorageService fileStorageService;
    @Resource
    private FileService fileService;

    @Override
    public void beforeCreate(StorageReq req) {
        // 解密密钥
        if (StorageTypeEnum.OSS.equals(req.getType())) {
            req.setSecretKey(this.decryptSecretKey(req.getSecretKey(), null));
        }
        // 指定配置参数校验及预处理
        StorageTypeEnum storageType = req.getType();
        storageType.validate(req);
        storageType.pretreatment(req);
        // 校验存储编码
        String code = req.getCode();
        CheckUtils.throwIf(this.isCodeExists(code, null), "新增失败，[{}] 已存在", code);
        // 需要独立操作来指定默认存储
        req.setIsDefault(false);
        // 加载存储引擎
        if (DisEnableStatusEnum.ENABLE.equals(req.getStatus())) {
            this.load(BeanUtil.copyProperties(req, StorageDO.class));
        }
    }

    @Override
    public void beforeUpdate(StorageReq req, Long id) {
        // 解密密钥
        StorageDO oldStorage = super.getById(id);
        if (StorageTypeEnum.OSS.equals(req.getType())) {
            req.setSecretKey(this.decryptSecretKey(req.getSecretKey(), oldStorage));
        }
        // 校验存储编码、存储类型、状态
        CheckUtils.throwIfNotEqual(req.getType(), oldStorage.getType(), "不允许修改存储类型");
        CheckUtils.throwIfNotEqual(req.getCode(), oldStorage.getCode(), "不允许修改存储编码");
        DisEnableStatusEnum newStatus = req.getStatus();
        CheckUtils.throwIf(Boolean.TRUE.equals(oldStorage.getIsDefault()) && DisEnableStatusEnum.DISABLE
            .equals(newStatus), "[{}] 是默认存储，不允许禁用", oldStorage.getName());
        // 指定配置参数校验及预处理
        StorageTypeEnum storageType = req.getType();
        storageType.validate(req);
        storageType.pretreatment(req);
        // 卸载存储引擎
        this.unload(oldStorage);
        // 加载存储引擎
        if (DisEnableStatusEnum.ENABLE.equals(newStatus)) {
            BeanUtil.copyProperties(req, oldStorage);
            this.load(oldStorage);
        }
    }

    @Override
    public void beforeDelete(List<Long> ids) {
        CheckUtils.throwIf(fileService.countByStorageIds(ids) > 0, "所选存储存在文件或文件夹关联，请删除后重试");
        List<StorageDO> storageList = baseMapper.lambdaQuery().in(StorageDO::getId, ids).list();
        storageList.forEach(storage -> {
            CheckUtils.throwIfEqual(Boolean.TRUE, storage.getIsDefault(), "[{}] 是默认存储，不允许删除", storage.getName());
            // 卸载存储引擎
            this.unload(storage);
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(CommonStatusUpdateReq req, Long id) {
        StorageDO storage = super.getById(id);
        // 状态未改变
        DisEnableStatusEnum newStatus = req.getStatus();
        if (storage.getStatus().equals(newStatus)) {
            return;
        }
        // 修改状态
        baseMapper.lambdaUpdate().eq(StorageDO::getId, id).set(StorageDO::getStatus, newStatus).update();
        // 加载、卸载存储引擎
        switch (newStatus) {
            case ENABLE:
                this.load(storage);
                break;
            case DISABLE:
                CheckUtils.throwIfEqual(Boolean.TRUE, storage.getIsDefault(), "[{}] 是默认存储，不允许禁用", storage.getName());
                this.unload(storage);
                break;
            default:
                break;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefaultStorage(Long id) {
        StorageDO storage = super.getById(id);
        if (Boolean.TRUE.equals(storage.getIsDefault())) {
            return;
        }
        // 启用状态才能设为默认存储
        CheckUtils.throwIfNotEqual(DisEnableStatusEnum.ENABLE, storage.getStatus(), "请先启用所选存储");
        baseMapper.lambdaUpdate().eq(StorageDO::getIsDefault, true).set(StorageDO::getIsDefault, false).update();
        baseMapper.lambdaUpdate().eq(StorageDO::getId, id).set(StorageDO::getIsDefault, true).update();
    }

    @Override
    public StorageDO getDefaultStorage() {
        StorageDO storage = baseMapper.lambdaQuery().eq(StorageDO::getIsDefault, true).one();
        CheckUtils.throwIfNull(storage, "请先指定默认存储");
        return storage;
    }

    @Override
    public StorageDO getByCode(String code) {
        if (StrUtil.isBlank(code)) {
            return this.getDefaultStorage();
        }
        StorageDO storage = baseMapper.lambdaQuery().eq(StorageDO::getCode, code).one();
        CheckUtils.throwIfNotExists(storage, "存储", "code", code);
        return storage;
    }

    @Override
    public void load(StorageDO storage) {
        CopyOnWriteArrayList<FileStorage> fileStorageList = fileStorageService.getFileStorageList();
        switch (storage.getType()) {
            case LOCAL -> {
                FileStorageProperties.LocalPlusConfig config = new FileStorageProperties.LocalPlusConfig();
                config.setPlatform(storage.getCode());
                config.setStoragePath(storage.getBucketName());
                fileStorageList.addAll(FileStorageServiceBuilder.buildLocalPlusFileStorage(Collections
                    .singletonList(config)));
                // 注册资源映射
                SpringWebUtils.registerResourceHandler(MapUtil.of(URLUtil.url(storage.getDomain()).getPath(), storage
                    .getBucketName()));
            }
            case OSS -> {
                FileStorageProperties.AmazonS3Config config = new FileStorageProperties.AmazonS3Config();
                config.setPlatform(storage.getCode());
                config.setAccessKey(storage.getAccessKey());
                config.setSecretKey(storage.getSecretKey());
                config.setEndPoint(storage.getEndpoint());
                config.setBucketName(storage.getBucketName());
                fileStorageList.addAll(FileStorageServiceBuilder.buildAmazonS3FileStorage(Collections
                    .singletonList(config), null));
            }
            default -> throw new IllegalArgumentException("不支持的存储类型：%s".formatted(storage.getType()));
        }
    }

    @Override
    public void unload(StorageDO storage) {
        FileStorage fileStorage = fileStorageService.getFileStorage(storage.getCode());
        if (fileStorage == null) {
            return;
        }
        CopyOnWriteArrayList<FileStorage> fileStorageList = fileStorageService.getFileStorageList();
        fileStorageList.remove(fileStorage);
        fileStorage.close();
        // 本地存储引擎需要移除资源映射
        if (StorageTypeEnum.LOCAL.equals(storage.getType())) {
            SpringWebUtils.deRegisterResourceHandler(MapUtil.of(URLUtil.url(storage.getDomain()).getPath(), storage
                .getBucketName()));
        }
    }

    /**
     * 解密 SecretKey
     *
     * @param encryptSecretKey 加密的 SecretKey
     * @param storage          存储信息
     * @return 解密后的 SecretKey
     */
    private String decryptSecretKey(String encryptSecretKey, StorageDO storage) {
        // 修改时，如果 SecretKey 不修改，需要手动修正
        if (storage != null) {
            boolean isSecretKeyNotUpdate = StrUtil.isBlank(encryptSecretKey) || encryptSecretKey
                .contains(StringConstants.ASTERISK);
            if (isSecretKeyNotUpdate) {
                return storage.getSecretKey();
            }
        }
        // 新增场景，直接解密 SecretKey
        String secretKey = ExceptionUtils.exToNull(() -> SecureUtils.decryptByRsaPrivateKey(encryptSecretKey));
        ValidationUtils.throwIfNull(secretKey, "私有密钥解密失败");
        ValidationUtils.throwIf(secretKey.length() > 255, "私有密钥长度不能超过 255 个字符");
        return secretKey;
    }

    /**
     * 编码是否存在
     *
     * @param code 编码
     * @param id   ID
     * @return 是否存在
     */
    private boolean isCodeExists(String code, Long id) {
        return baseMapper.lambdaQuery().eq(StorageDO::getCode, code).ne(id != null, StorageDO::getId, id).exists();
    }
}