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

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.EscapeUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.recorder.FileRecorder;
import org.dromara.x.file.storage.core.upload.FilePartInfo;
import org.springframework.stereotype.Component;
import top.continew.admin.common.util.helper.LoginHelper;
import top.continew.admin.system.enums.FileTypeEnum;
import top.continew.admin.system.mapper.FileMapper;
import top.continew.admin.system.mapper.StorageMapper;
import top.continew.admin.system.model.entity.FileDO;
import top.continew.admin.system.model.entity.StorageDO;
import top.continew.starter.core.constant.StringConstants;

import java.util.Optional;

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
        FileDO file = new FileDO();
        String originalFilename = EscapeUtil.unescape(fileInfo.getOriginalFilename());
        file.setName(StrUtil.contains(originalFilename, StringConstants.DOT)
            ? StrUtil.subBefore(originalFilename, StringConstants.DOT, true)
            : originalFilename);
        file.setSize(fileInfo.getSize());
        file.setUrl(fileInfo.getPath() + fileInfo.getFilename());
        file.setExtension(fileInfo.getExt());
        file.setType(FileTypeEnum.getByExtension(file.getExtension()));
        file.setThumbnailUrl(fileInfo.getPath() + fileInfo.getThFilename());
        file.setThumbnailSize(fileInfo.getThSize());
        StorageDO storage = (StorageDO)fileInfo.getAttr().get(ClassUtil.getClassName(StorageDO.class, false));
        file.setStorageId(storage.getId());
        file.setCreateTime(DateUtil.toLocalDateTime(fileInfo.getCreateTime()));
        file.setUpdateUser(LoginHelper.getUserId());
        file.setUpdateTime(file.getCreateTime());
        fileMapper.insert(file);
        return true;
    }

    @Override
    public FileInfo getByUrl(String url) {
        FileDO file = this.getFileByUrl(url);
        if (null == file) {
            return null;
        }
        return file.toFileInfo(storageMapper.lambdaQuery().eq(StorageDO::getId, file.getStorageId()).one().getCode());
    }

    @Override
    public boolean delete(String url) {
        FileDO file = this.getFileByUrl(url);
        return fileMapper.lambdaUpdate().eq(FileDO::getUrl, file.getUrl()).remove();
    }

    /**
     * 根据 URL 查询文件
     *
     * @param url URL
     * @return 文件信息
     */
    private FileDO getFileByUrl(String url) {
        Optional<FileDO> fileOptional = fileMapper.lambdaQuery().eq(FileDO::getUrl, url).oneOpt();
        return fileOptional.orElseGet(() -> fileMapper.lambdaQuery()
            .eq(FileDO::getUrl, StrUtil.subAfter(url, StringConstants.SLASH, true))
            .oneOpt()
            .orElse(null));
    }

    @Override
    public void update(FileInfo fileInfo) {
        /* 不使用分片功能则无需重写 */
    }

    @Override
    public void saveFilePart(FilePartInfo filePartInfo) {
        /* 不使用分片功能则无需重写 */
    }

    @Override
    public void deleteFilePartByUploadId(String s) {
        /* 不使用分片功能则无需重写 */
    }
}