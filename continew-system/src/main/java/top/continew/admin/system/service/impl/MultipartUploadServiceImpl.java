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

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import top.continew.admin.system.enums.FileTypeEnum;
import top.continew.admin.system.model.entity.FileDO;
import top.continew.admin.system.model.entity.StorageDO;
import top.continew.admin.system.model.req.MultipartUploadInitReq;
import top.continew.admin.system.model.resp.file.MultipartUploadInitResp;
import top.continew.admin.system.model.resp.file.MultipartUploadResp;
import top.continew.admin.system.mapper.FileMapper;
import top.continew.admin.system.service.FileService;
import top.continew.admin.system.service.MultipartUploadService;
import top.continew.admin.system.service.StorageService;
import top.continew.admin.system.util.FileNameGenerator;
import top.continew.starter.core.exception.BaseException;
import top.continew.starter.core.constant.StringConstants;
import top.continew.starter.storage.core.FileStorageService;
import top.continew.starter.storage.domain.model.resp.FileInfo;
import top.continew.starter.storage.domain.model.resp.MultipartInitResp;

import java.io.IOException;

/**
 * 分片上传业务实现
 *
 * @author KAI
 * @since 2025/7/31 9:30
 */
@Service
@RequiredArgsConstructor
public class MultipartUploadServiceImpl implements MultipartUploadService {

    private final StorageService storageService;
    private final FileStorageService fileStorageService;
    private final FileService fileService;
    private final FileMapper fileMapper;

    @Override
    public MultipartUploadInitResp initMultipartUpload(MultipartUploadInitReq multiPartUploadInitReq) {
        StorageDO storageDO = storageService.getByCode(null);
        // 检测文件名是否已存在（同一目录下文件名不能重复）
        String originalFileName = multiPartUploadInitReq.getFileName();
        String parentPath = multiPartUploadInitReq.getParentPath();
        boolean exists = fileMapper.lambdaQuery()
            .eq(FileDO::getParentPath, parentPath)
            .eq(FileDO::getStorageId, storageDO.getId())
            .eq(FileDO::getName, originalFileName)
            .ne(FileDO::getType, FileTypeEnum.DIR)
            .exists();
        if (exists) {
            throw new BaseException("文件名已存在：" + originalFileName);
        }

        // 生成唯一文件名（处理重名情况）
        String uniqueFileName = FileNameGenerator.generateUniqueName(originalFileName, parentPath, storageDO
            .getId(), fileMapper);
        multiPartUploadInitReq.setFileName(uniqueFileName);
        fileService.createParentDir(StrUtil.blankToDefault(parentPath, StringConstants.SLASH), storageDO);

        top.continew.starter.storage.domain.model.req.MultipartUploadInitReq storageReq = new top.continew.starter.storage.domain.model.req.MultipartUploadInitReq();
        storageReq.setPlatform(storageDO.getCode());
        storageReq.setBucket(storageDO.getBucketName());
        storageReq.setFileName(uniqueFileName);
        storageReq.setFileSize(multiPartUploadInitReq.getFileSize());
        storageReq.setFileMd5(multiPartUploadInitReq.getFileMd5());
        storageReq.setContentType(multiPartUploadInitReq.getContentType());
        storageReq.setParentPath(StrUtil.blankToDefault(parentPath, StringConstants.SLASH));
        storageReq.setMetadata(multiPartUploadInitReq.getMetaData());
        MultipartInitResp initResp = fileStorageService.initMultipartUpload(storageReq);
        return this.toAdminInitResp(initResp);
    }

    @Override
    public MultipartUploadResp uploadPart(MultipartFile file, String uploadId, Integer partNumber, String path) {
        MultipartInitResp session = fileStorageService.getMultipartSession(uploadId);
        if (session == null) {
            throw new BaseException("无效的 uploadId: " + uploadId);
        }
        validatePartSize(file, session, partNumber);
        String targetPath = StrUtil.blankToDefault(session.getPath(), path);
        try {
            top.continew.starter.storage.domain.model.resp.MultipartUploadResp resp = fileStorageService
                .uploadPart(session.getPlatform(), session
                    .getBucket(), normalizeStoragePath(targetPath), uploadId, partNumber, file.getInputStream());
            return this.toAdminUploadResp(resp);
        } catch (IOException e) {
            throw new BaseException("上传分片失败: " + e.getMessage(), e);
        }
    }

    @Override
    public FileDO completeMultipartUpload(String uploadId) {
        MultipartInitResp session = fileStorageService.getMultipartSession(uploadId);
        if (session == null) {
            throw new BaseException("无效的 uploadId: " + uploadId);
        }
        FileInfo fileInfo = fileStorageService.completeMultipartUpload(uploadId, null);
        StorageDO storageDO = storageService.getByCode(session.getPlatform());
        FileDO file = fileMapper.lambdaQuery()
            .eq(FileDO::getStorageId, storageDO.getId())
            .eq(FileDO::getPath, StringConstants.SLASH + normalizeStoragePath(session.getPath()))
            .one();
        if (file != null) {
            return file;
        }
        // 兼容兜底：记录器未完成落库时补录
        FileDO fallback = new FileDO(fileInfo);
        fallback.setStorageId(storageDO.getId());
        fallback.setType(FileTypeEnum.getByExtension(FileUtil.extName(fallback.getName())));
        fileService.save(fallback);
        return fallback;
    }

    @Override
    public void cancelMultipartUpload(String uploadId) {
        fileStorageService.abortMultipartUpload(uploadId);
    }

    private MultipartUploadInitResp toAdminInitResp(MultipartInitResp source) {
        MultipartUploadInitResp target = new MultipartUploadInitResp();
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
        target.setPath(StringConstants.SLASH + normalizeStoragePath(source.getPath()));
        target.setPartSize(source.getPartSize());
        target.setUploadedPartNumbers(source.getUploadedPartNumbers());
        return target;
    }

    private MultipartUploadResp toAdminUploadResp(top.continew.starter.storage.domain.model.resp.MultipartUploadResp source) {
        MultipartUploadResp target = new MultipartUploadResp();
        target.setPartNumber(source.getPartNumber());
        target.setPartETag(source.getPartETag());
        target.setPartSize(source.getPartSize());
        target.setSuccess(source.isSuccess());
        target.setErrorMessage(source.getErrorMessage());
        return target;
    }

    /**
     * 基于分片会话固化参数校验当前分片大小一致性。
     *
     * <p>
     * 规则：
     * </p>
     * <p>
     * 1. 分片序号必须在合法区间（1..totalParts）
     * </p>
     * <p>
     * 2. 非最后一片大小必须等于会话 partSize
     * </p>
     * <p>
     * 3. 最后一片大小必须等于剩余字节数（支持整除时等于 partSize）
     * </p>
     *
     * @param file       当前上传分片
     * @param session    分片上传会话
     * @param partNumber 分片序号
     */
    private void validatePartSize(MultipartFile file, MultipartInitResp session, Integer partNumber) {
        if (partNumber == null || partNumber < 1) {
            throw new BaseException("分片序号不合法: " + partNumber);
        }
        Long sessionPartSize = session.getPartSize();
        Long sessionFileSize = session.getFileSize();
        if (sessionPartSize == null || sessionPartSize <= 0 || sessionFileSize == null || sessionFileSize <= 0) {
            return;
        }
        long partSize = sessionPartSize;
        long fileSize = sessionFileSize;
        long totalParts = (fileSize + partSize - 1) / partSize;
        if (partNumber > totalParts) {
            throw new BaseException("分片序号超出范围: " + partNumber);
        }
        long expectedPartSize = partNumber < totalParts ? partSize : fileSize - (totalParts - 1) * partSize;
        long actualPartSize = file.getSize();
        if (actualPartSize != expectedPartSize) {
            throw new BaseException("分片大小不匹配: partNumber=%s, expected=%s, actual=%s"
                .formatted(partNumber, expectedPartSize, actualPartSize));
        }
    }

    private String normalizeStoragePath(String path) {
        return StrUtil.removePrefix(path.replace("\\", StringConstants.SLASH)
            .replaceAll("/+", StringConstants.SLASH), StringConstants.SLASH);
    }
}
