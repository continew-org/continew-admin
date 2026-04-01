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
import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.continew.admin.common.base.service.BaseServiceImpl;
import top.continew.admin.common.enums.DisEnableStatusEnum;
import top.continew.admin.common.model.req.CommonStatusUpdateReq;
import top.continew.admin.common.util.SecureUtils;
import top.continew.admin.system.enums.StorageTypeEnum;
import top.continew.admin.system.mapper.StorageMapper;
import top.continew.admin.system.model.entity.StorageDO;
import top.continew.admin.system.model.query.StorageQuery;
import top.continew.admin.system.model.req.StorageReq;
import top.continew.admin.system.model.resp.StorageResp;
import top.continew.admin.system.model.resp.file.FileUploadConfigResp;
import top.continew.admin.system.service.FileService;
import top.continew.admin.system.service.StorageService;
import top.continew.starter.core.util.ExceptionUtils;
import top.continew.starter.core.util.validation.CheckUtils;
import top.continew.starter.core.util.validation.ValidationUtils;
import top.continew.starter.storage.autoconfigure.properties.LocalStorageConfig;
import top.continew.starter.storage.autoconfigure.properties.OssStorageConfig;
import top.continew.starter.storage.autoconfigure.properties.StorageProperties;
import top.continew.starter.storage.common.constant.StorageConstant;
import top.continew.starter.storage.core.FileStorageService;
import top.continew.starter.storage.strategy.impl.LocalStorageStrategy;
import top.continew.starter.storage.strategy.impl.OssStorageStrategy;

import java.util.List;

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
    private final StorageProperties storageProperties;
    @Resource
    private FileService fileService;

    @Override
    public void beforeCreate(StorageReq req) {
        // 解密密钥
        if (StorageTypeEnum.OSS.equals(req.getType())) {
            ValidationUtils.throwIfBlank(req.getSecretKey(), "Secret Key不能为空");
            req.setSecretKey(this.decryptSecretKey(req.getSecretKey(), null));
        }
        // 指定配置参数校验及预处理
        StorageTypeEnum storageType = req.getType();
        storageType.validate(req);
        storageType.pretreatment(req);
        // 校验存储编码
        this.checkCodeRepeat(req.getCode(), null);
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
        // 校验存储类型、存储编码、回收站配置、状态
        CheckUtils.throwIfNotEqual(req.getType(), oldStorage.getType(), "不允许修改存储类型");
        CheckUtils.throwIfNotEqual(req.getCode(), oldStorage.getCode(), "不允许修改存储编码");
        CheckUtils.throwIfNotEqual(req.getRecycleBinEnabled(), oldStorage.getRecycleBinEnabled(), "不允许修改回收站配置");
        CheckUtils.throwIfNotEqual(req.getRecycleBinPath(), oldStorage.getRecycleBinPath(), "不允许修改回收站配置");
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
        CheckUtils.throwIfEqual(DisEnableStatusEnum.DISABLE, storage.getStatus(), "请先启用所选存储");
        baseMapper.lambdaUpdate().eq(StorageDO::getIsDefault, true).set(StorageDO::getIsDefault, false).update();
        baseMapper.lambdaUpdate().eq(StorageDO::getId, id).set(StorageDO::getIsDefault, true).update();
        fileStorageService.defaultStorage(storage.getCode());
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
    public FileUploadConfigResp getDefaultUploadConfig() {
        StorageDO storage = this.getDefaultStorage();
        FileUploadConfigResp resp = new FileUploadConfigResp();
        resp.setStorageId(storage.getId());
        resp.setStorageName(storage.getName());
        resp.setStorageCode(storage.getCode());
        resp.setStorageType(storage.getType());
        resp.setMultipartUploadThreshold(resolveMultipartUploadThreshold(storage));
        resp.setMultipartUploadPartSize(resolveMultipartUploadPartSize(storage));
        if (StorageTypeEnum.LOCAL.equals(storage.getType())) {
            resp.setMultipartTempDir(resolveLocalMultipartTempDir(storage));
        }
        return resp;
    }

    @Override
    public void load(StorageDO storage) {
        if (fileStorageService.exists(storage.getCode()) && fileStorageService.isDynamic(storage.getCode())) {
            fileStorageService.unload(storage.getCode());
        }
        switch (storage.getType()) {
            case LOCAL -> {
                LocalStorageConfig config = new LocalStorageConfig();
                config.setEnabled(true);
                config.setPlatform(storage.getCode());
                config.setBucketName(storage.getBucketName());
                config.setEndpoint(storage.getDomain());
                config.setMultipartUploadThreshold(resolveMultipartUploadThreshold(storage));
                config.setMultipartUploadPartSize(resolveMultipartUploadPartSize(storage));
                config.setMultipartTempDir(resolveLocalMultipartTempDir(storage));
                fileStorageService.register(new LocalStorageStrategy(config));
            }
            case OSS -> {
                OssStorageConfig config = new OssStorageConfig();
                config.setEnabled(true);
                config.setPlatform(storage.getCode());
                config.setAccessKey(storage.getAccessKey());
                config.setSecretKey(storage.getSecretKey());
                config.setEndpoint(storage.getEndpoint());
                config.setBucketName(storage.getBucketName());
                config.setDomain(storage.getDomain());
                config.setMultipartUploadThreshold(resolveMultipartUploadThreshold(storage));
                config.setMultipartUploadPartSize(resolveMultipartUploadPartSize(storage));
                config.setPathStyleAccessEnabled(true);
                fileStorageService.register(new OssStorageStrategy(config));
            }
            default -> throw new IllegalArgumentException("不支持的存储类型：%s".formatted(storage.getType()));
        }
        if (Boolean.TRUE.equals(storage.getIsDefault())) {
            fileStorageService.defaultStorage(storage.getCode());
        }
    }

    @Override
    public void unload(StorageDO storage) {
        if (!fileStorageService.exists(storage.getCode()) || !fileStorageService.isDynamic(storage.getCode())) {
            return;
        }
        fileStorageService.unload(storage.getCode());
    }

    /**
     * 解析分片上传阈值（字节）
     *
     * <p>
     * 当上传文件大小超过该阈值时，存储策略将走分片上传流程。
     * 取值优先级：
     * </p>
     * <p>
     * 1. 全局配置 {@code continew-starter.storage.multipart-upload-threshold}
     * </p>
     * <p>
     * 2. 框架默认值 {@link StorageConstant#DEFAULT_MULTIPART_UPLOAD_THRESHOLD}
     * </p>
     *
     * @return 最终生效的分片上传阈值（字节）
     */
    private long resolveMultipartUploadThreshold() {
        long threshold = storageProperties.getMultipartUploadThreshold();
        return threshold > 0 ? threshold : StorageConstant.DEFAULT_MULTIPART_UPLOAD_THRESHOLD;
    }

    /**
     * 解析存储实例分片上传阈值（字节）
     *
     * <p>
     * 取值优先级：
     * </p>
     * <p>
     * 1. 存储实例配置（{@link StorageDO#getMultipartUploadThreshold()}）
     * </p>
     * <p>
     * 2. 全局配置（见 {@link #resolveMultipartUploadThreshold()}）
     * </p>
     *
     * @param storage 存储配置实体
     * @return 最终生效的分片上传阈值（字节）
     */
    private long resolveMultipartUploadThreshold(StorageDO storage) {
        Long threshold = storage.getMultipartUploadThreshold();
        return (threshold != null && threshold > 0) ? threshold : resolveMultipartUploadThreshold();
    }

    /**
     * 解析全局分片大小（字节）
     *
     * <p>
     * 用于在存储实例未配置分片大小时作为回退值。
     * 取值优先级：
     * </p>
     * <p>
     * 1. 全局配置 {@code continew-starter.storage.multipart-upload-part-size}
     * </p>
     * <p>
     * 2. 框架默认值 {@link StorageConstant#DEFAULT_MULTIPART_UPLOAD_PART_SIZE}
     * </p>
     *
     * @return 最终生效的全局分片大小（字节）
     */
    private long resolveMultipartUploadPartSize() {
        long partSize = storageProperties.getMultipartUploadPartSize();
        return partSize > 0 ? partSize : StorageConstant.DEFAULT_MULTIPART_UPLOAD_PART_SIZE;
    }

    /**
     * 解析存储实例分片大小（字节）
     *
     * <p>
     * 取值优先级：
     * </p>
     * <p>
     * 1. 存储实例配置（{@link StorageDO#getMultipartUploadPartSize()}）
     * </p>
     * <p>
     * 2. 全局配置（见 {@link #resolveMultipartUploadPartSize()}）
     * </p>
     *
     * @param storage 存储配置实体
     * @return 最终生效的分片大小（字节）
     */
    private long resolveMultipartUploadPartSize(StorageDO storage) {
        Long partSize = storage.getMultipartUploadPartSize();
        return (partSize != null && partSize > 0) ? partSize : resolveMultipartUploadPartSize();
    }

    /**
     * 解析全局本地分片临时目录
     *
     * <p>
     * 用于本地存储实例未配置临时目录时的回退值。
     * 取值优先级：
     * </p>
     * <p>
     * 1. 全局配置 {@code continew-starter.storage.local-multipart-temp-dir}
     * </p>
     * <p>
     * 2. 框架默认值 {@link StorageConstant#DEFAULT_LOCAL_MULTIPART_TEMP_DIR}
     * </p>
     *
     * @return 最终生效的全局本地分片临时目录
     */
    private String resolveLocalMultipartTempDir() {
        return StrUtil.isBlank(storageProperties.getLocalMultipartTempDir())
            ? StorageConstant.DEFAULT_LOCAL_MULTIPART_TEMP_DIR
            : storageProperties.getLocalMultipartTempDir();
    }

    /**
     * 解析存储实例本地分片临时目录
     *
     * <p>
     * 仅本地存储策略使用该值。
     * 取值优先级：
     * </p>
     * <p>
     * 1. 存储实例配置（{@link StorageDO#getMultipartTempDir()}，并进行 trim）
     * </p>
     * <p>
     * 2. 全局配置（见 {@link #resolveLocalMultipartTempDir()}）
     * </p>
     *
     * @param storage 存储配置实体
     * @return 最终生效的本地分片临时目录
     */
    private String resolveLocalMultipartTempDir(StorageDO storage) {
        return StrUtil.isBlank(storage.getMultipartTempDir())
            ? resolveLocalMultipartTempDir()
            : storage.getMultipartTempDir().trim();
    }

    /**
     * 解密 SecretKey
     *
     * @param encryptSecretKey 加密的 SecretKey
     * @param oldStorage       旧存储配置
     * @return 解密后的 SecretKey
     */
    private String decryptSecretKey(String encryptSecretKey, StorageDO oldStorage) {
        // 修改时，SecretKey 为空将不更改
        if (oldStorage != null && StrUtil.isBlank(encryptSecretKey)) {
            return oldStorage.getSecretKey();
        }
        // 解密
        String secretKey = ExceptionUtils.exToNull(() -> SecureUtils.decryptByRsaPrivateKey(encryptSecretKey));
        ValidationUtils.throwIfNull(secretKey, "私有密钥解密失败");
        ValidationUtils.throwIf(secretKey.length() > 255, "私有密钥长度不能超过 255 个字符");
        return secretKey;
    }

    /**
     * 检查编码是否重复
     *
     * @param code 编码
     * @param id   ID
     */
    private void checkCodeRepeat(String code, Long id) {
        CheckUtils.throwIf(baseMapper.lambdaQuery()
            .eq(StorageDO::getCode, code)
            .ne(id != null, StorageDO::getId, id)
            .exists(), "编码为 [{}] 的存储配置已存在", code);
    }
}
