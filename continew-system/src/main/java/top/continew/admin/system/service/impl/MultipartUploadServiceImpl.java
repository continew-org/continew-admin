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
import top.continew.admin.system.constant.MultipartUploadConstants;
import top.continew.admin.system.dao.MultipartUploadDao;
import top.continew.admin.system.enums.FileTypeEnum;
import top.continew.admin.system.factory.StorageHandlerFactory;
import top.continew.admin.system.handler.StorageHandler;
import top.continew.admin.system.handler.impl.LocalStorageHandler;
import top.continew.admin.system.model.entity.FileDO;
import top.continew.admin.system.model.entity.StorageDO;
import top.continew.admin.system.model.req.MultipartUploadInitReq;
import top.continew.admin.system.model.resp.file.FilePartInfo;
import top.continew.admin.system.model.resp.file.MultipartUploadInitResp;
import top.continew.admin.system.model.resp.file.MultipartUploadResp;
import top.continew.admin.system.mapper.FileMapper;
import top.continew.admin.system.service.FileService;
import top.continew.admin.system.service.MultipartUploadService;
import top.continew.admin.system.service.StorageService;
import top.continew.admin.system.util.FileNameGenerator;
import top.continew.starter.core.exception.BaseException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

    private final StorageHandlerFactory storageHandlerFactory;

    private final MultipartUploadDao multipartUploadDao;

    private final FileService fileService;

    private final FileMapper fileMapper;

    @Override
    public MultipartUploadInitResp initMultipartUpload(MultipartUploadInitReq multiPartUploadInitReq) {
        // 后续可以增加storageCode参数 指定某个存储平台 当前设计是默认存储平台
        StorageDO storageDO = storageService.getByCode(null);
        // 根据文件Md5查询当前存储平台是否初始化过分片
        String uploadId = multipartUploadDao.getUploadIdByMd5(multiPartUploadInitReq.getFileMd5());
        if (StrUtil.isNotBlank(uploadId)) {
            MultipartUploadInitResp multipartUpload = multipartUploadDao.getMultipartUpload(uploadId);
            //对比存储平台和分片大小是否一致 一致则返回结果
            if (multipartUpload != null && multipartUpload.getPartSize()
                .equals(MultipartUploadConstants.MULTIPART_UPLOAD_PART_SIZE) && multipartUpload.getPlatform()
                    .equals(storageDO.getCode())) {
                // 获取已上传分片信息
                List<FilePartInfo> fileParts = multipartUploadDao.getFileParts(uploadId);
                Set<Integer> partNumbers = fileParts.stream()
                    .map(FilePartInfo::getPartNumber)
                    .collect(Collectors.toSet());
                multipartUpload.setUploadedPartNumbers(partNumbers);
                return multipartUpload;
            }
            //todo else 待定 更换存储平台 或分片大小有变更 是否需要删除原先分片

        }

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

        StorageHandler storageHandler = storageHandlerFactory.createHandler(storageDO.getType());
        //文件元信息
        Map<String, String> metaData = multiPartUploadInitReq.getMetaData();
        MultipartUploadInitResp multipartUploadInitResp = storageHandler
            .initMultipartUpload(storageDO, multiPartUploadInitReq);
        // 缓存文件信息,md5和uploadId映射
        multipartUploadDao.setMultipartUpload(multipartUploadInitResp.getUploadId(), multipartUploadInitResp, metaData);
        multipartUploadDao.setMd5Mapping(multiPartUploadInitReq.getFileMd5(), multipartUploadInitResp.getUploadId());
        return multipartUploadInitResp;
    }

    @Override
    public MultipartUploadResp uploadPart(MultipartFile file, String uploadId, Integer partNumber, String path) {
        StorageDO storageDO = storageService.getByCode(null);
        StorageHandler storageHandler = storageHandlerFactory.createHandler(storageDO.getType());
        MultipartUploadResp resp = storageHandler.uploadPart(storageDO, path, uploadId, partNumber, file);
        FilePartInfo partInfo = new FilePartInfo();
        partInfo.setUploadId(uploadId);
        partInfo.setBucket(storageDO.getBucketName());
        partInfo.setPath(path);
        partInfo.setPartNumber(partNumber);
        partInfo.setPartETag(resp.getPartETag());
        partInfo.setPartSize(resp.getPartSize());
        partInfo.setStatus("SUCCESS");
        partInfo.setUploadTime(LocalDateTime.now());
        multipartUploadDao.setFilePart(uploadId, partInfo);
        return resp;
    }

    @Override
    public FileDO completeMultipartUpload(String uploadId) {
        StorageDO storageDO = storageService.getByCode(null);
        // 从 FileRecorder 获取所有分片信息
        List<FilePartInfo> recordedParts = multipartUploadDao.getFileParts(uploadId);
        MultipartUploadInitResp initResp = multipartUploadDao.getMultipartUpload(uploadId);
        // 转换为 MultipartUploadResp
        List<MultipartUploadResp> parts = recordedParts.stream().map(partInfo -> {
            MultipartUploadResp resp = new MultipartUploadResp();
            resp.setPartNumber(partInfo.getPartNumber());
            resp.setPartETag(partInfo.getPartETag());
            resp.setPartSize(partInfo.getPartSize());
            resp.setSuccess("SUCCESS".equals(partInfo.getStatus()));
            return resp;
        }).collect(Collectors.toList());

        // 如果没有记录，使用客户端传入的分片信息
        if (parts.isEmpty()) {
            throw new BaseException("没有找到任何分片信息");
        }

        // 验证分片完整性
        validatePartsCompleteness(parts);

        // 获取策略，判断是否需要验证
        boolean needVerify = true;
        StorageHandler storageHandler = storageHandlerFactory.createHandler(storageDO.getType());
        if (storageHandler instanceof LocalStorageHandler) {
            needVerify = false;
        }

        // 完成上传
        storageHandler.completeMultipartUpload(storageDO, parts, initResp.getPath(), uploadId, needVerify);
        // 文件名已在初始化阶段处理为唯一文件名
        String uniqueFileName = initResp.getFileName().replaceFirst("^[/\\\\]+", "");
        FileDO file = new FileDO();
        file.setName(uniqueFileName);
        file.setOriginalName(uniqueFileName);
        file.setPath(initResp.getPath());
        file.setParentPath(initResp.getParentPath());
        file.setSize(initResp.getFileSize());
        file.setSha256(initResp.getFileMd5());
        file.setExtension(initResp.getExtension());
        file.setContentType(initResp.getContentType());
        file.setType(FileTypeEnum.getByExtension(FileUtil.extName(uniqueFileName)));
        file.setStorageId(storageDO.getId());
        fileService.save(file);
        multipartUploadDao.deleteMultipartUpload(uploadId);
        return file;
    }

    @Override
    public void cancelMultipartUpload(String uploadId) {
        StorageDO storageDO = storageService.getByCode(null);
        multipartUploadDao.deleteMultipartUploadAll(uploadId);
        StorageHandler storageHandler = storageHandlerFactory.createHandler(storageDO.getType());
        storageHandler.cleanPart(storageDO, uploadId);
    }

    /**
     * 验证分片完整性
     *
     * @param parts 分片信息
     */
    private void validatePartsCompleteness(List<MultipartUploadResp> parts) {
        if (parts.isEmpty()) {
            throw new BaseException("没有找到任何分片信息");
        }

        // 检查分片编号连续性
        List<Integer> partNumbers = parts.stream().map(MultipartUploadResp::getPartNumber).sorted().toList();

        for (int i = 0; i < partNumbers.size(); i++) {
            if (partNumbers.get(i) != i + 1) {
                throw new BaseException("分片编号不连续，缺失分片: " + (i + 1));
            }
        }

        // 检查是否所有分片都成功
        List<Integer> failedParts = parts.stream()
            .filter(part -> !part.isSuccess())
            .map(MultipartUploadResp::getPartNumber)
            .toList();

        if (!failedParts.isEmpty()) {
            throw new BaseException("存在失败的分片: " + failedParts);
        }
    }
}
