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

package top.charles7c.continew.admin.system.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.recorder.FileRecorder;
import org.springframework.stereotype.Component;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.EscapeUtil;

import top.charles7c.continew.admin.common.util.helper.LoginHelper;
import top.charles7c.continew.admin.system.enums.FileTypeEnum;
import top.charles7c.continew.admin.system.mapper.FileMapper;
import top.charles7c.continew.admin.system.mapper.StorageMapper;
import top.charles7c.continew.admin.system.model.entity.FileDO;
import top.charles7c.continew.admin.system.model.entity.StorageDO;

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
        file.setName(EscapeUtil.unescape(fileInfo.getOriginalFilename()));
        file.setSize(fileInfo.getSize());
        file.setUrl(fileInfo.getUrl());
        file.setExtension(fileInfo.getExt());
        file.setType(FileTypeEnum.getByExtension(file.getExtension()));
        StorageDO storage = storageMapper.lambdaQuery().eq(StorageDO::getCode, fileInfo.getPlatform()).one();
        file.setStorageId(storage.getId());
        file.setCreateTime(DateUtil.toLocalDateTime(fileInfo.getCreateTime()));
        file.setUpdateUser(LoginHelper.getUserId());
        file.setUpdateTime(file.getCreateTime());
        fileMapper.insert(file);
        return true;
    }

    @Override
    public FileInfo getByUrl(String url) {
        FileDO file = fileMapper.lambdaQuery().eq(FileDO::getUrl, url).one();
        FileInfo fileInfo = new FileInfo();
        fileInfo.setOriginalFilename(file.getName());
        fileInfo.setSize(file.getSize());
        fileInfo.setUrl(file.getUrl());
        fileInfo.setExt(file.getExtension());
        return fileInfo;
    }

    @Override
    public boolean delete(String url) {
        return fileMapper.lambdaUpdate().eq(FileDO::getUrl, url).remove();
    }
}