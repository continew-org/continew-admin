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

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import top.continew.admin.common.base.service.BaseServiceImpl;
import top.continew.admin.common.context.UserContextHolder;
import top.continew.admin.common.enums.DisEnableStatusEnum;
import top.continew.admin.system.enums.FileTypeEnum;
import top.continew.admin.system.mapper.FileMapper;
import top.continew.admin.system.model.entity.FileDO;
import top.continew.admin.system.model.entity.StorageDO;
import top.continew.admin.system.model.query.FileQuery;
import top.continew.admin.system.model.req.FileReq;
import top.continew.admin.system.model.resp.file.FileUploadProgressResp;
import top.continew.admin.system.model.resp.file.FileResp;
import top.continew.admin.system.model.resp.file.FileStatisticsResp;
import top.continew.admin.system.enums.FileUploadProgressStatusEnum;
import top.continew.admin.system.service.FileService;
import top.continew.admin.system.service.StorageService;
import top.continew.admin.system.util.FileNameGenerator;
import top.continew.starter.cache.redisson.util.RedisLockUtils;
import top.continew.starter.cache.redisson.util.RedisUtils;
import top.continew.starter.core.constant.StringConstants;
import top.continew.starter.core.util.CollUtils;
import top.continew.starter.core.util.StrUtils;
import top.continew.starter.core.util.validation.CheckUtils;
import top.continew.starter.core.util.validation.ValidationUtils;
import top.continew.starter.storage.core.FileStorageService;
import top.continew.starter.storage.core.UploadPretreatment;
import top.continew.starter.storage.domain.file.EnhancedMultipartFile;
import top.continew.starter.storage.domain.model.resp.FileInfo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 文件业务实现
 *
 * @author Charles7c
 * @since 2023/12/23 10:38
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl
    extends BaseServiceImpl<FileMapper, FileDO, FileResp, FileResp, FileQuery, FileReq>
    implements FileService {

    private static final String FILE_UPLOAD_PROGRESS_PREFIX = "file:upload:progress:";
    private static final Duration FILE_UPLOAD_PROGRESS_EXPIRE = Duration.ofHours(1);

    private final FileStorageService fileStorageService;
    @Lazy
    @Resource
    private StorageService storageService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> ids) {
        List<FileDO> fileList = baseMapper.lambdaQuery().in(FileDO::getId, ids).list();
        if (CollUtil.isEmpty(fileList)) {
            return;
        }
        // 批量获取存储配置
        Map<Long, List<FileDO>> fileListGroup =
            fileList.stream().collect(Collectors.groupingBy(FileDO::getStorageId));
        List<StorageDO> storageList = storageService.listByIds(fileListGroup.keySet());
        Map<Long, StorageDO> storageGroup = storageList.stream()
            .collect(Collectors.toMap(StorageDO::getId, Function.identity(),
                (existing, replacement) -> existing));
        // 删除记录
        for (Map.Entry<Long, List<FileDO>> entry : fileListGroup.entrySet()) {
            StorageDO storage = storageGroup.get(entry.getKey());
            List<Long> idList = CollUtils.mapToList(entry.getValue(), FileDO::getId);
            if (Boolean.TRUE.equals(storage.getRecycleBinEnabled())) {
                baseMapper.deleteByIds(idList);
            } else {
                baseMapper.deleteWithoutRecycleBin(idList, UserContextHolder.getUserId());
            }
        }
        // 删除实际文件
        for (Map.Entry<Long, List<FileDO>> entry : fileListGroup.entrySet()) {
            StorageDO storage = storageGroup.get(entry.getKey());
            entry.getValue().forEach(file -> this.deleteFile(file, storage));
        }
    }

    @Override
    public FileInfo upload(MultipartFile file, String parentPath, String storageCode) {
        return this.upload(file, parentPath, storageCode, null);
    }

    @Override
    public FileInfo upload(MultipartFile file, String parentPath, String storageCode,
        String uploadTaskId) {
        return this.doUpload(file, parentPath, storageCode, FileNameUtil.extName(file
            .getOriginalFilename()), uploadTaskId);
    }

    @Override
    public FileInfo upload(File file, String parentPath, String storageCode) {
        return this.doUpload(file, parentPath, storageCode, FileNameUtil.extName(file.getName()),
            null);
    }

    @Override
    public Long createDir(FileReq req) {
        String parentPath = req.getParentPath();
        FileDO file = baseMapper.lambdaQuery()
            .eq(FileDO::getParentPath, parentPath)
            .eq(FileDO::getName, req.getOriginalName())
            .eq(FileDO::getType, FileTypeEnum.DIR)
            .one();
        CheckUtils.throwIfNotNull(file, "文件夹已存在");
        // 存储引擎需要一致
        StorageDO storage = storageService.getDefaultStorage();
        if (!StringConstants.SLASH.equals(parentPath)) {
            FileDO parentFile = baseMapper.lambdaQuery()
                .eq(FileDO::getPath, parentPath)
                .eq(FileDO::getType, FileTypeEnum.DIR)
                .one();
            CheckUtils.throwIfNull(parentFile, "父级文件夹不存在");
            CheckUtils.throwIfNotEqual(parentFile.getStorageId(), storage.getId(),
                "文件夹和父级文件夹存储引擎不一致");
        }
        // 创建文件夹
        FileDO dirFile = new FileDO();
        String originalName = req.getOriginalName();
        dirFile.setName(originalName);
        dirFile.setOriginalName(originalName);
        dirFile.setParentPath(parentPath);
        dirFile.setType(FileTypeEnum.DIR);
        dirFile.setStorageId(storage.getId());
        baseMapper.insert(dirFile);
        return dirFile.getId();
    }

    @Override
    public FileStatisticsResp statistics() {
        FileStatisticsResp resp = new FileStatisticsResp();
        List<FileStatisticsResp> statisticsList = baseMapper.statistics();
        if (CollUtil.isEmpty(statisticsList)) {
            return resp;
        }
        resp.setData(statisticsList);
        resp.setSize(statisticsList.stream().mapToLong(FileStatisticsResp::getSize).sum());
        resp.setNumber(statisticsList.stream().mapToLong(FileStatisticsResp::getNumber).sum());
        return resp;
    }

    @Override
    public FileResp check(String fileHash) {
        FileDO file = baseMapper.lambdaQuery().eq(FileDO::getSha256, fileHash).one();
        if (file != null) {
            return get(file.getId());
        }
        return null;
    }

    @Override
    public Long calcDirSize(Long id) {
        FileDO dirFile = super.getById(id);
        ValidationUtils.throwIfNotEqual(dirFile.getType(), FileTypeEnum.DIR,
            "ID 为 [{}] 的不是文件夹，不支持计算大小", id);
        // 查询当前文件夹下的所有子文件和子文件夹
        List<FileDO> children =
            baseMapper.lambdaQuery().eq(FileDO::getParentPath, dirFile.getPath()).list();
        if (CollUtil.isEmpty(children)) {
            return 0L;
        }
        // 累加子文件大小和递归计算子文件夹大小
        return children.stream().mapToLong(child -> {
            if (FileTypeEnum.DIR.equals(child.getType())) {
                // 递归计算子文件夹大小
                return calcDirSize(child.getId());
            } else {
                return child.getSize();
            }
        }).sum();
    }

    @Override
    public FileUploadProgressResp getUploadProgress(String uploadTaskId) {
        ValidationUtils.throwIfBlank(uploadTaskId, "上传任务 ID 不能为空");
        String key = FILE_UPLOAD_PROGRESS_PREFIX + uploadTaskId;
        Object value = RedisUtils.get(key);
        if (value == null) {
            FileUploadProgressResp resp = new FileUploadProgressResp();
            resp.setUploadTaskId(uploadTaskId);
            resp.setStatus(FileUploadProgressStatusEnum.NOT_FOUND);
            resp.setPercentage(0);
            resp.setBytesRead(0L);
            resp.setTotalBytes(0L);
            return resp;
        }
        FileUploadProgressResp resp =
            JSONUtil.toBean(value.toString(), FileUploadProgressResp.class);
        if (StrUtil.isBlank(resp.getUploadTaskId())) {
            resp.setUploadTaskId(uploadTaskId);
        }
        return resp;
    }

    @Override
    public Long countByStorageIds(List<Long> storageIds) {
        if (CollUtil.isEmpty(storageIds)) {
            return 0L;
        }
        return baseMapper.lambdaQuery().in(FileDO::getStorageId, storageIds).count();
    }

    @Override
    protected void fill(Object obj) {
        super.fill(obj);
        if (obj instanceof FileResp fileResp) {
            StorageDO storage = storageService.getById(fileResp.getStorageId());
            String prefix = storage.getUrlPrefix();
            String url = URLUtil.normalize(prefix + fileResp.getPath(), false, true);
            fileResp.setUrl(url);
            String parentPath = StringConstants.SLASH.equals(fileResp.getParentPath())
                ? StringConstants.EMPTY
                : fileResp.getParentPath();
            String thumbnailUrl =
                StrUtils.blankToDefault(fileResp.getThumbnailName(), url, thName -> URLUtil
                    .normalize(prefix + parentPath + StringConstants.SLASH + thName, false, true));
            fileResp.setThumbnailUrl(thumbnailUrl);
            fileResp.setStorageName("%s (%s)".formatted(storage.getName(), storage.getCode()));
        }
    }

    /**
     * 上传文件并返回上传后的文件信息
     *
     * @param file         文件
     * @param parentPath   上级目录
     * @param storageCode  存储引擎编码
     * @param extName      文件扩展名
     * @param uploadTaskId 上传任务 ID
     * @return 文件信息
     */
    private FileInfo doUpload(Object file, String parentPath, String storageCode, String extName,
        String uploadTaskId) {
        List<String> allExtensions = FileTypeEnum.getAllExtensions();
        CheckUtils.throwIf(!allExtensions.contains(extName), "不支持的文件类型，仅支持 {} 格式的文件", String
            .join(StringConstants.COMMA, allExtensions));
        // 构建上传预处理对象
        StorageDO storage = storageService.getByCode(storageCode);
        CheckUtils.throwIf(DisEnableStatusEnum.DISABLE.equals(storage.getStatus()), "请先启用存储 [{}]",
            storage.getCode());

        // 创建父级目录
        this.createParentDir(parentPath, storage);

        // 生成唯一文件名（处理重名情况）
        String originalFileName = getOriginalFileName(file);
        String uniqueFileName =
            FileNameGenerator.generateUniqueName(originalFileName, parentPath, storage
                .getId(), baseMapper);
        String sha256 = calculateSha256(file);
        String normalizedUploadTaskId = StrUtil.emptyToNull(StrUtil.trim(uploadTaskId));
        long totalBytes = this.getFileSize(file);
        if (StrUtil.isNotBlank(normalizedUploadTaskId)) {
            this.saveUploadProgress(normalizedUploadTaskId, FileUploadProgressStatusEnum.INIT, 0L,
                totalBytes, 0, null, null, null);
        }

        UploadPretreatment uploadPretreatment = fileStorageService.of(normalizeUploadSource(file))
            .platform(storage.getCode())
            .path(this.pretreatmentPath(parentPath))
            .fileName(uniqueFileName)
            .metadata("sha256", sha256)
            .metadata("storageCode", storage.getCode())
            .metadata("storageId", String.valueOf(storage.getId()));
        // 图片文件生成缩略图
        if (FileTypeEnum.IMAGE.getExtensions().contains(extName)) {
            uploadPretreatment.thumbnail(100, 100);
        }
        uploadPretreatment.onProgress((progressSize, allSize, percentage) -> {
            log.info("文件 [{}] 已上传 [{}]，总大小 [{}]，进度 [{}%]", uniqueFileName, progressSize, allSize,
                percentage);
            if (StrUtil.isNotBlank(normalizedUploadTaskId)) {
                FileUploadProgressStatusEnum status =
                    (allSize > 0 && progressSize >= allSize) || percentage >= 100
                        ? FileUploadProgressStatusEnum.FINALIZING
                        : FileUploadProgressStatusEnum.UPLOADING;
                this.saveUploadProgress(normalizedUploadTaskId, status, progressSize, allSize,
                    percentage, null, null, null);
            }
        });
        try {
            // 上传
            log.info("开始上传文件: {}", uniqueFileName);
            FileInfo fileInfo = uploadPretreatment.upload();
            log.info("文件 [{}] 上传完成", uniqueFileName);
            FileInfo result = this.postProcessUploadResult(fileInfo, storage);
            if (StrUtil.isNotBlank(normalizedUploadTaskId)) {
                long completedSize = result.getSize() == null ? totalBytes : result.getSize();
                this.saveUploadProgress(normalizedUploadTaskId,
                    FileUploadProgressStatusEnum.COMPLETED, completedSize, totalBytes, 100, null,
                    result
                        .getFileId(),
                    result.getUrl());
            }
            return result;
        } catch (RuntimeException e) {
            if (StrUtil.isNotBlank(normalizedUploadTaskId)) {
                this.saveUploadProgress(normalizedUploadTaskId, FileUploadProgressStatusEnum.FAILED,
                    0L, totalBytes, 0, e
                        .getMessage(),
                    null, null);
            }
            throw e;
        }
    }

    /**
     * 获取原始文件名
     *
     * @param file 文件对象（MultipartFile 或 File）
     * @return 原始文件名
     */
    private String getOriginalFileName(Object file) {
        if (file instanceof MultipartFile multipartFile) {
            return multipartFile.getOriginalFilename();
        } else if (file instanceof File ioFile) {
            return ioFile.getName();
        }
        return "unknown";
    }

    /**
     * 规范化上传源对象。
     *
     * <p>
     * 如果传入的是 {@link File}，则先读取文件内容并包装为 {@link EnhancedMultipartFile}，
     * 避免 starter-storage 将其按普通对象走 JSON 分支处理；其他类型保持原样返回。
     * </p>
     *
     * @param file 上传源对象
     * @return 可交给 starter-storage 处理的上传对象
     */
    private static Object normalizeUploadSource(Object file) {
        if (!(file instanceof File ioFile)) {
            return file;
        }
        try {
            String contentType = Files.probeContentType(ioFile.toPath());
            if (StrUtil.isBlank(contentType)) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }
            return EnhancedMultipartFile.create(ioFile.getName(), ioFile.getName(), contentType,
                Files
                    .readAllBytes(ioFile.toPath()));
        } catch (IOException e) {
            throw new IllegalStateException("读取上传文件失败", e);
        }
    }

    /**
     * 处理路径
     *
     * <p>
     * 1.如果 path 为 {@code /}，则保持为 {@code /}（避免触发自动日期目录） <br />
     * 2.如果 path 不以 {@code /} 结尾，则添加后缀 {@code /} <br />
     * 3.如果 path 以 {@code /} 开头，则移除前缀 {@code /} <br />
     * 示例：yyyy/MM/dd/
     * </p>
     *
     * @param path 路径
     * @return 处理路径
     */
    private String pretreatmentPath(String path) {
        if (StringConstants.SLASH.equals(path)) {
            // 传递根路径本身，避免 starter 将空路径识别为“自动日期目录”
            return StringConstants.SLASH;
        }
        return StrUtil.appendIfMissing(StrUtil.removePrefix(path, StringConstants.SLASH),
            StringConstants.SLASH);
    }

    /**
     * 创建上级文件夹（支持多级）
     *
     * <p>
     * user/avatar/ => user（path：/user）、avatar（path：/user/avatar）
     * </p>
     *
     * @param parentPath 上级目录
     * @param storage    存储配置
     */
    @Override
    public void createParentDir(String parentPath, StorageDO storage) {
        String lockKey = StrUtil.format("Lock:{}:{}", storage.getCode(), parentPath);
        try (RedisLockUtils lock = RedisLockUtils.tryLock(lockKey)) {
            if (!lock.isLocked()) {
                return; // 获取锁失败，直接返回
            }
            if (StrUtil.isBlank(parentPath) || StringConstants.SLASH.equals(parentPath)) {
                return;
            }
            // user/avatar/ => user、avatar
            String[] parentPathParts = StrUtil.split(parentPath, StringConstants.SLASH, false, true)
                .toArray(String[]::new);
            String lastPath = StringConstants.SLASH;
            StringBuilder currentPathBuilder = new StringBuilder();
            for (int i = 0; i < parentPathParts.length; i++) {
                String parentPathPart = parentPathParts[i];
                if (i > 0) {
                    lastPath = currentPathBuilder.toString();
                }
                // /user、/user/avatar
                currentPathBuilder.append(StringConstants.SLASH).append(parentPathPart);
                String currentPath = currentPathBuilder.toString();
                // 文件夹和文件存储引擎需要一致
                FileDO dirFile = baseMapper.lambdaQuery()
                    .eq(FileDO::getPath, currentPath)
                    .eq(FileDO::getType, FileTypeEnum.DIR)
                    .one();
                if (dirFile != null) {
                    CheckUtils.throwIfNotEqual(dirFile.getStorageId(), storage.getId(),
                        "文件夹和上传文件存储引擎不一致");
                    continue;
                }
                FileDO file = new FileDO();
                file.setName(parentPathPart);
                file.setOriginalName(parentPathPart);
                file.setPath(currentPath);
                file.setParentPath(lastPath);
                file.setType(FileTypeEnum.DIR);
                file.setStorageId(storage.getId());
                baseMapper.insert(file);
            }
        }
    }

    /**
     * 删除实际文件
     *
     * @param file    文件
     * @param storage 存储配置
     */
    private void deleteFile(FileDO file, StorageDO storage) {
        Long storageId = storage.getId();
        if (FileTypeEnum.DIR.equals(file.getType())) {
            // 不允许删除非空文件夹
            boolean exists = baseMapper.lambdaQuery()
                .eq(FileDO::getParentPath, file.getPath())
                .eq(FileDO::getStorageId, storageId)
                .exists();
            CheckUtils.throwIf(exists, "文件夹 [{}] 不为空，请先删除文件夹下的内容", file.getName());
            return;
        }
        String sourcePath = normalizeStoragePath(file.getPath());
        if (Boolean.TRUE.equals(storage.getRecycleBinEnabled())) {
            // 移动到回收站目录
            String targetPath = normalizeStoragePath(storage.getRecycleBinPath() + sourcePath);
            fileStorageService.move(storage.getCode(), storage.getBucketName(), storage
                .getBucketName(), sourcePath, targetPath);
        } else {
            // 删除文件
            fileStorageService.delete(storage.getCode(), storage.getBucketName(), sourcePath);
        }
    }

    private String calculateSha256(Object file) {
        try {
            if (file instanceof MultipartFile multipartFile) {
                return DigestUtil.sha256Hex(multipartFile.getInputStream());
            } else if (file instanceof File ioFile) {
                return DigestUtil.sha256Hex(ioFile);
            }
            return null;
        } catch (IOException e) {
            throw new IllegalStateException("计算文件 SHA256 失败", e);
        }
    }

    private FileInfo postProcessUploadResult(FileInfo fileInfo, StorageDO storage) {
        if (fileInfo.getMetadata() == null) {
            fileInfo.setMetadata(new HashMap<>());
        }
        fileInfo.setUrl(URLUtil.normalize(storage.getUrlPrefix() + normalizeStoragePath(fileInfo
            .getPath()), false, true));
        if (StrUtil.isNotBlank(fileInfo.getThumbnailPath()) && !StrUtil.startWithAny(fileInfo
            .getThumbnailPath(), "http://", "https://")) {
            fileInfo.setThumbnailPath(
                URLUtil.normalize(storage.getUrlPrefix() + normalizeStoragePath(fileInfo
                    .getThumbnailPath()), false, true));
        }
        return fileInfo;
    }

    private String normalizeStoragePath(String path) {
        return StrUtil.removePrefix(path.replace("\\", StringConstants.SLASH)
            .replaceAll("/+", StringConstants.SLASH), StringConstants.SLASH);
    }

    private long getFileSize(Object file) {
        if (file instanceof MultipartFile multipartFile) {
            return multipartFile.getSize();
        }
        if (file instanceof File ioFile) {
            return ioFile.length();
        }
        return 0L;
    }

    private void saveUploadProgress(String uploadTaskId,
        FileUploadProgressStatusEnum status,
        long bytesRead,
        long totalBytes,
        int percentage,
        String message,
        String fileId,
        String url) {
        String key = FILE_UPLOAD_PROGRESS_PREFIX + uploadTaskId;
        FileUploadProgressResp current = null;
        Object value = RedisUtils.get(key);
        if (value != null) {
            current = JSONUtil.toBean(value.toString(), FileUploadProgressResp.class);
        }
        if (current != null && isFinalStatus(current.getStatus()) && !isFinalStatus(status)) {
            return;
        }
        if (!isFinalStatus(status) && current != null) {
            bytesRead =
                Math.max(bytesRead, current.getBytesRead() == null ? 0L : current.getBytesRead());
            percentage =
                Math.max(percentage, current.getPercentage() == null ? 0 : current.getPercentage());
        }

        FileUploadProgressResp resp = new FileUploadProgressResp();
        resp.setUploadTaskId(uploadTaskId);
        resp.setStatus(status);
        resp.setBytesRead(bytesRead);
        resp.setTotalBytes(totalBytes);
        resp.setPercentage(percentage);
        resp.setMessage(
            StrUtil.blankToDefault(message, current == null ? null : current.getMessage()));
        resp.setFileId(
            StrUtil.blankToDefault(fileId, current == null ? null : current.getFileId()));
        resp.setUrl(StrUtil.blankToDefault(url, current == null ? null : current.getUrl()));
        RedisUtils.set(key, JSONUtil.toJsonStr(resp), FILE_UPLOAD_PROGRESS_EXPIRE);
    }

    private boolean isFinalStatus(String status) {
        return StrUtil.equalsAnyIgnoreCase(status, FileUploadProgressStatusEnum.COMPLETED
            .getValue(), FileUploadProgressStatusEnum.FAILED.getValue());
    }

    private boolean isFinalStatus(FileUploadProgressStatusEnum status) {
        return status == FileUploadProgressStatusEnum.COMPLETED
            || status == FileUploadProgressStatusEnum.FAILED;
    }
}
