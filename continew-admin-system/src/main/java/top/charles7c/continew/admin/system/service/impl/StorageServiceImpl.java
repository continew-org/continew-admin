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

package top.charles7c.continew.admin.system.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import jakarta.annotation.Resource;
import jakarta.servlet.ServletContext;

import lombok.RequiredArgsConstructor;

import org.dromara.x.file.storage.core.FileStorageProperties;
import org.dromara.x.file.storage.core.FileStorageService;
import org.dromara.x.file.storage.core.FileStorageServiceBuilder;
import org.dromara.x.file.storage.core.platform.FileStorage;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.util.UrlPathHelper;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;

import top.charles7c.continew.admin.common.enums.DisEnableStatusEnum;
import top.charles7c.continew.admin.system.enums.StorageTypeEnum;
import top.charles7c.continew.admin.system.mapper.StorageMapper;
import top.charles7c.continew.admin.system.model.entity.StorageDO;
import top.charles7c.continew.admin.system.model.query.StorageQuery;
import top.charles7c.continew.admin.system.model.req.StorageReq;
import top.charles7c.continew.admin.system.model.resp.StorageDetailResp;
import top.charles7c.continew.admin.system.model.resp.StorageResp;
import top.charles7c.continew.admin.system.service.FileService;
import top.charles7c.continew.admin.system.service.StorageService;
import top.charles7c.continew.starter.core.util.validate.CheckUtils;
import top.charles7c.continew.starter.core.util.validate.ValidationUtils;
import top.charles7c.continew.starter.extension.crud.base.BaseServiceImpl;

/**
 * 存储库业务实现
 *
 * @author Charles7c
 * @since 2023/12/26 22:09
 */
@Service
@RequiredArgsConstructor
public class StorageServiceImpl
    extends BaseServiceImpl<StorageMapper, StorageDO, StorageResp, StorageDetailResp, StorageQuery, StorageReq>
    implements StorageService {

    @Resource
    private FileService fileService;
    private final ApplicationContext applicationContext;
    private final FileStorageService fileStorageService;

    @Override
    public Long add(StorageReq req) {
        String code = req.getCode();
        CheckUtils.throwIf(this.isCodeExists(code, null), "新增失败，[{}] 已存在", code);
        req.setStatus(DisEnableStatusEnum.ENABLE);
        this.load(req);
        return super.add(req);
    }

    @Override
    public void update(StorageReq req, Long id) {
        String code = req.getCode();
        CheckUtils.throwIf(this.isCodeExists(code, id), "修改失败，[{}] 已存在", code);
        StorageDO oldStorage = super.getById(id);
        CheckUtils.throwIf(
            Boolean.TRUE.equals(oldStorage.getIsDefault()) && DisEnableStatusEnum.DISABLE.equals(req.getStatus()),
            "[{}] 是默认存储库，不允许禁用", oldStorage.getName());
        this.unload(oldStorage.getCode());
        this.load(req);
        super.update(req, id);
    }

    @Override
    public void delete(List<Long> ids) {
        CheckUtils.throwIf(fileService.countByStorageIds(ids) > 0, "所选存储库存在文件关联，请删除文件后重试");
        List<StorageDO> storageList = baseMapper.lambdaQuery().in(StorageDO::getId, ids).list();
        storageList.forEach(s -> this.unload(s.getCode()));
        super.delete(ids);
    }

    @Override
    public StorageDO getDefaultStorage() {
        return baseMapper.lambdaQuery().eq(StorageDO::getIsDefault, true).one();
    }

    @Override
    public StorageDO getByCode(String code) {
        return baseMapper.lambdaQuery().eq(StorageDO::getCode, code).one();
    }

    @Override
    public void load(StorageReq req) {
        CopyOnWriteArrayList<FileStorage> fileStorageList = fileStorageService.getFileStorageList();
        String bucketName = req.getBucketName();
        StorageTypeEnum type = req.getType();
        switch (type) {
            case LOCAL -> {
                ValidationUtils.throwIfBlank(bucketName, "存储路径不能为空");
                FileStorageProperties.LocalPlusConfig config = new FileStorageProperties.LocalPlusConfig();
                config.setPlatform(req.getCode());
                config.setStoragePath(bucketName);
                config.setDomain(req.getDomain());
                fileStorageList
                    .addAll(FileStorageServiceBuilder.buildLocalPlusFileStorage(Collections.singletonList(config)));
            }
            case S3 -> {
                String accessKey = req.getAccessKey();
                String secretKey = req.getSecretKey();
                String endpoint = req.getEndpoint();
                ValidationUtils.throwIfBlank(accessKey, "Access Key不能为空");
                ValidationUtils.throwIfBlank(secretKey, "Secret Key不能为空");
                ValidationUtils.throwIfBlank(endpoint, "Endpoint不能为空");
                ValidationUtils.throwIfBlank(bucketName, "桶名称不能为空");
                FileStorageProperties.AmazonS3Config config = new FileStorageProperties.AmazonS3Config();
                config.setPlatform(req.getCode());
                config.setAccessKey(accessKey);
                config.setSecretKey(secretKey);
                config.setEndPoint(endpoint);
                config.setBucketName(bucketName);
                config.setDomain(req.getDomain());
                fileStorageList.addAll(
                    FileStorageServiceBuilder.buildAmazonS3FileStorage(Collections.singletonList(config), null));
            }
        }
    }

    @Override
    public void unload(String code) {
        CopyOnWriteArrayList<FileStorage> fileStorageList = fileStorageService.getFileStorageList();
        FileStorage fileStorage = fileStorageService.getFileStorage(code);
        fileStorageList.remove(fileStorage);
        fileStorage.close();
    }

    /**
     * 编码是否存在
     *
     * @param code
     *            编码
     * @param id
     *            ID
     * @return 是否存在
     */
    private boolean isCodeExists(String code, Long id) {
        return baseMapper.lambdaQuery().eq(StorageDO::getCode, code).ne(null != id, StorageDO::getId, id).exists();
    }

    /**
     * 注册静态资源映射
     *
     * @param registerMapping
     *            静态资源映射列表
     */
    private void registerResource(Map<String, String> registerMapping) {
        final UrlPathHelper urlPathHelper = applicationContext.getBean("mvcUrlPathHelper", UrlPathHelper.class);
        final ContentNegotiationManager contentNegotiationManager =
            applicationContext.getBean("mvcContentNegotiationManager", ContentNegotiationManager.class);
        final ServletContext servletContext = applicationContext.getBean(ServletContext.class);
        final HandlerMapping resourceHandlerMapping =
            applicationContext.getBean("resourceHandlerMapping", HandlerMapping.class);
        // 已经注册的静态资源映射
        final Map<String, Object> handlerMap =
            (Map<String, Object>)ReflectUtil.getFieldValue(resourceHandlerMapping, "handlerMap");
        final ResourceHandlerRegistry resourceHandlerRegistry =
            new ResourceHandlerRegistry(applicationContext, servletContext, contentNegotiationManager, urlPathHelper);
        // 重新注册静态资源映射
        for (Map.Entry<String, String> entry : registerMapping.entrySet()) {
            String pathPattern = StrUtil.appendIfMissing(entry.getKey(), "/**");
            String resourceLocations = StrUtil.appendIfMissing(entry.getValue(), "/");
            // 移除之前注册过的
            handlerMap.remove(pathPattern);
            // 重新注册
            resourceHandlerRegistry.addResourceHandler(pathPattern).addResourceLocations("file:" + resourceLocations);
        }
        final Map<String, ?> additionalUrlMap =
            ReflectUtil.<SimpleUrlHandlerMapping>invoke(resourceHandlerRegistry, "getHandlerMapping").getUrlMap();
        ReflectUtil.<Void>invoke(resourceHandlerMapping, "registerHandlers", additionalUrlMap);
    }
}