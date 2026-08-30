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

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.continew.admin.system.constant.MultipartUploadConstants;
import top.continew.admin.system.mapper.FileMapper;
import top.continew.admin.system.mapper.StorageMapper;
import top.continew.admin.system.model.entity.FileDO;
import top.continew.admin.system.model.entity.StorageDO;
import top.continew.admin.system.model.resp.file.MultipartUploadCreateResp;
import top.continew.starter.cache.redisson.util.RedisUtils;
import top.continew.starter.core.constant.StringConstants;
import top.continew.starter.core.util.CollUtils;
import top.continew.starter.core.util.URLUtils;
import top.continew.starter.storage.domain.model.resp.FileInfo;
import top.continew.starter.storage.domain.model.resp.FilePartInfo;
import top.continew.starter.storage.domain.model.resp.MultipartInitResp;
import top.continew.starter.storage.service.FileRecorder;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 文件记录实现类
 *
 * @author Charles7c
 * @since 2023/12/24 22:31
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileRecorderImpl implements FileRecorder {

    private final FileMapper fileMapper;
    private final StorageMapper storageMapper;

    @Override
    public boolean save(FileInfo fileInfo) {
        // 分片初始化阶段不落库，只记录分片会话和分片信息
        if ("UPLOADING".equalsIgnoreCase(fileInfo.getMetadata() != null
            ? fileInfo.getMetadata().get("status")
            : null)) {
            return true;
        }
        StorageDO storage = this.getStorageByPlatform(fileInfo.getPlatform());
        if (storage == null) {
            return true;
        }
        FileDO file = new FileDO(fileInfo);
        file.setStorageId(storage.getId());
        fileMapper.insert(file);
        fileInfo.setFileId(String.valueOf(file.getId()));
        if (!URLUtils.isHttpUrl(fileInfo.getUrl())) {
            String prefix = storage.getUrlPrefix();
            String url = URLUtil.normalize(prefix + fileInfo.getPath(), false, true);
            fileInfo.setUrl(url);
            if (StrUtil.isNotBlank(fileInfo.getThumbnailPath())
                && !URLUtils.isHttpUrl(fileInfo.getThumbnailPath())) {
                fileInfo.setThumbnailPath(
                    URLUtil.normalize(prefix + fileInfo.getThumbnailPath(), false, true));
            }
        }
        return true;
    }

    @Override
    public FileInfo getByUrl(String url) {
        FileDO file = this.getFileByUrl(url);
        if (file == null) {
            return null;
        }
        StorageDO storageDO =
            storageMapper.lambdaQuery().eq(StorageDO::getId, file.getStorageId()).one();
        return file.toFileInfo(storageDO);
    }

    @Override
    public boolean delete(String platform, String path) {
        StorageDO storage = this.getStorageByPlatform(platform);
        if (storage == null) {
            return true;
        }
        String normalizedPath = StrUtil.prependIfMissing(path, StringConstants.SLASH);
        FileDO file = fileMapper.lambdaQuery()
            .eq(FileDO::getStorageId, storage.getId())
            .eq(FileDO::getPath, normalizedPath)
            .one();
        if (file == null) {
            return true;
        }
        return fileMapper.lambdaUpdate().eq(FileDO::getId, file.getId()).remove();
    }

    @Override
    public boolean update(FileInfo fileInfo) {
        StorageDO storage = this.getStorageByPlatform(fileInfo.getPlatform());
        if (storage == null) {
            return false;
        }
        FileDO file = new FileDO(fileInfo);
        file.setStorageId(storage.getId());
        FileDO existFile = fileMapper.lambdaQuery()
            .eq(FileDO::getStorageId, storage.getId())
            .eq(FileDO::getPath, file.getPath())
            .one();
        if (existFile == null) {
            fileMapper.insert(file);
            fileInfo.setFileId(String.valueOf(file.getId()));
            return true;
        } else {
            file.setId(existFile.getId());
            boolean updated = fileMapper.updateById(file) > 0;
            fileInfo.setFileId(String.valueOf(existFile.getId()));
            return updated;
        }
    }

    @Override
    public void saveFilePart(FilePartInfo filePartInfo) {
        String key = MultipartUploadConstants.MULTIPART_PARTS_PREFIX + filePartInfo.getUploadId();
        String partKey = filePartInfo.getPartNumber().toString();
        try {
            RedisUtils.hSet(key, partKey, JSONUtil.toJsonStr(filePartInfo));
            RedisUtils.expire(key, Duration.ofHours(MultipartUploadConstants.DEFAULT_EXPIRE_HOURS));
            log.debug("缓存分片信息: uploadId={}, partNumber={}", filePartInfo.getUploadId(), partKey);
        } catch (Exception e) {
            log.error("缓存分片信息失败: uploadId={}, partNumber={}", filePartInfo.getUploadId(), partKey,
                e);
            throw new RuntimeException("缓存分片信息失败", e);
        }
    }

    @Override
    public List<FilePartInfo> getFileParts(String fileId) {
        String key = MultipartUploadConstants.MULTIPART_PARTS_PREFIX + fileId;
        try {
            Map<String, Object> entries = RedisUtils.hGetAll(key);
            if (CollUtil.isEmpty(entries)) {
                return new ArrayList<>();
            }
            return entries.values()
                .stream()
                .map(value -> JSONUtil.toBean(value.toString(), FilePartInfo.class))
                .sorted(Comparator.comparing(FilePartInfo::getPartNumber))
                .toList();
        } catch (Exception e) {
            log.error("获取分片列表失败: uploadId={}", fileId, e);
            return new ArrayList<>();
        }
    }

    @Override
    public void deleteFileParts(String fileId) {
        try {
            RedisUtils.delete(MultipartUploadConstants.MULTIPART_PARTS_PREFIX + fileId);
            log.debug("删除所有分片信息: uploadId={}", fileId);
        } catch (Exception e) {
            log.error("删除所有分片信息失败: uploadId={}", fileId, e);
        }
    }

    @Override
    public String getUploadIdByMd5(String md5) {
        String md5Key = MultipartUploadConstants.MD5_TO_UPLOAD_ID_PREFIX + md5;
        try {
            return RedisUtils.hGet(md5Key, "uploadId");
        } catch (Exception e) {
            log.error("根据MD5获取uploadId失败: md5={}", md5, e);
            return null;
        }
    }

    @Override
    public void setMd5Mapping(String md5, String uploadId) {
        String md5Key = MultipartUploadConstants.MD5_TO_UPLOAD_ID_PREFIX + md5;
        try {
            RedisUtils.hSet(md5Key, "uploadId", uploadId);
            RedisUtils.expire(md5Key,
                Duration.ofHours(MultipartUploadConstants.DEFAULT_EXPIRE_HOURS));
            log.debug("缓存MD5映射: md5={}, uploadId={}", md5, uploadId);
        } catch (Exception e) {
            log.error("缓存MD5映射失败: md5={}, uploadId={}", md5, uploadId, e);
            throw new RuntimeException("缓存MD5映射失败", e);
        }
    }

    @Override
    public void deleteMd5Mapping(String md5) {
        try {
            RedisUtils.delete(MultipartUploadConstants.MD5_TO_UPLOAD_ID_PREFIX + md5);
            log.debug("删除MD5映射: md5={}", md5);
        } catch (Exception e) {
            log.error("删除MD5映射失败: md5={}", md5, e);
        }
    }

    @Override
    public void saveMultipartSession(String uploadId, MultipartInitResp initResp,
        Map<String, String> metadata) {
        MultipartUploadCreateResp target = new MultipartUploadCreateResp();
        target.setFileId(initResp.getFileId());
        target.setUploadId(initResp.getUploadId());
        target.setBucket(initResp.getBucket());
        target.setPlatform(initResp.getPlatform());
        target.setFileName(initResp.getFileName());
        target.setFileMd5(initResp.getFileMd5());
        target.setFileSize(ObjectUtil.defaultIfNull(initResp.getFileSize(), 0L));
        target.setExtension(initResp.getExtension());
        target.setContentType(initResp.getContentType());
        target.setParentPath(initResp.getParentPath());
        target.setPath(initResp.getPath());
        target.setPartSize(initResp.getPartSize());
        target.setUploadedPartNumbers(initResp.getUploadedPartNumbers());
        String key = MultipartUploadConstants.MULTIPART_UPLOAD_PREFIX + uploadId;
        String metadataKey = MultipartUploadConstants.MULTIPART_METADATA_PREFIX + uploadId;
        try {
            RedisUtils.set(key, JSONUtil.toJsonStr(target), Duration
                .ofHours(MultipartUploadConstants.DEFAULT_EXPIRE_HOURS));
            if (metadata != null && !metadata.isEmpty()) {
                for (Map.Entry<String, String> entry : metadata.entrySet()) {
                    RedisUtils.hSet(metadataKey, entry.getKey(), entry.getValue());
                }
                RedisUtils.expire(metadataKey,
                    Duration.ofHours(MultipartUploadConstants.DEFAULT_EXPIRE_HOURS));
            }
            log.debug("缓存分片上传信息: uploadId={}", uploadId);
        } catch (Exception e) {
            log.error("缓存分片上传信息失败: uploadId={}", uploadId, e);
            throw new RuntimeException("缓存分片上传信息失败", e);
        }
    }

    @Override
    public MultipartInitResp getMultipartSession(String uploadId) {
        MultipartUploadCreateResp source = null;
        try {
            Object value =
                RedisUtils.get(MultipartUploadConstants.MULTIPART_UPLOAD_PREFIX + uploadId);
            if (value != null) {
                source = JSONUtil.toBean(value
                    .toString(),
                    MultipartUploadCreateResp.class);
            }
        } catch (Exception e) {
            log.error("获取分片上传信息失败: uploadId={}", uploadId, e);
        }
        if (source == null) {
            return null;
        }
        MultipartInitResp target = new MultipartInitResp();
        target.setFileId(source.getFileId());
        target.setUploadId(source.getUploadId());
        target.setBucket(source.getBucket());
        target.setPlatform(source.getPlatform());
        target.setFileName(source.getFileName());
        target.setFileMd5(source.getFileMd5());
        target.setFileSize(source.getFileSize());
        target.setExtension(source.getExtension());
        target.setContentType(source.getContentType());
        target.setParentPath(source.getParentPath());
        target.setPath(source.getPath());
        target.setPartSize(source.getPartSize());
        target.setUploadedPartNumbers(source.getUploadedPartNumbers());
        return target;
    }

    @Override
    public void deleteMultipartSession(String uploadId) {
        try {
            String key = MultipartUploadConstants.MULTIPART_UPLOAD_PREFIX + uploadId;
            String metadataKey = MultipartUploadConstants.MULTIPART_METADATA_PREFIX + uploadId;
            String expireKey = MultipartUploadConstants.MULTIPART_EXPIRE_PREFIX + uploadId;
            MultipartInitResp initResp = getMultipartSession(uploadId);
            String fileMd5 = initResp != null ? initResp.getFileMd5() : null;
            if (StrUtil.isNotBlank(fileMd5)) {
                deleteMd5Mapping(fileMd5);
            }
            RedisUtils.delete(key);
            RedisUtils.delete(metadataKey);
            RedisUtils.delete(expireKey);
            log.debug("删除分片上传信息: uploadId={}", uploadId);
        } catch (Exception e) {
            log.error("删除分片上传信息失败: uploadId={}", uploadId, e);
        }
    }

    /**
     * 根据 URL 查询文件
     *
     * @param url URL
     * @return 文件信息
     */
    private FileDO getFileByUrl(String url) {
        LambdaQueryChainWrapper<FileDO> queryWrapper = fileMapper.lambdaQuery()
            .eq(FileDO::getName, StrUtil.subAfter(url, StringConstants.SLASH, true));
        // 非 HTTP URL 场景
        if (!URLUtils.isHttpUrl(url)) {
            return queryWrapper
                .eq(FileDO::getPath, StrUtil.prependIfMissing(url, StringConstants.SLASH)).one();
        }
        // HTTP URL 场景
        List<FileDO> list = queryWrapper.list();
        if (CollUtil.isEmpty(list)) {
            return null;
        }
        if (list.size() == 1) {
            return list.get(0);
        }
        // 结合存储配置进行匹配
        List<StorageDO> storageList =
            storageMapper.selectByIds(CollUtils.mapToList(list, FileDO::getStorageId));
        Map<Long, StorageDO> storageMap = storageList.stream()
            .collect(Collectors.toMap(StorageDO::getId, Function.identity(),
                (existing, replacement) -> existing));
        return list.stream().filter(file -> {
            // http://localhost:8000/file/user/avatar/6825e687db4174e7a297a5f8.png => http://localhost:8000/file/user/avatar
            String urlPrefix = StrUtil.subBefore(url, StringConstants.SLASH, true);
            // http://localhost:8000/file/ + /user/avatar => http://localhost:8000/file/user/avatar
            StorageDO storage = storageMap.get(file.getStorageId());
            return urlPrefix.equals(
                URLUtil.normalize(storage.getUrlPrefix() + file.getParentPath(), false, true));
        }).findFirst().orElse(null);
    }

    private StorageDO getStorageByPlatform(String platform) {
        if (StrUtil.isBlank(platform)) {
            return null;
        }
        return storageMapper.lambdaQuery().eq(StorageDO::getCode, platform).one();
    }
}
