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
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.recorder.FileRecorder;
import org.dromara.x.file.storage.core.upload.FilePartInfo;
import org.springframework.stereotype.Component;
import top.continew.admin.system.mapper.FileMapper;
import top.continew.admin.system.mapper.StorageMapper;
import top.continew.admin.system.model.entity.FileDO;
import top.continew.admin.system.model.entity.StorageDO;
import top.continew.starter.core.constant.StringConstants;
import top.continew.starter.core.util.URLUtils;

import java.util.List;
import java.util.Map;
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
        // 保存文件信息
        FileDO file = new FileDO(fileInfo);
        StorageDO storage = (StorageDO)fileInfo.getAttr().get(ClassUtil.getClassName(StorageDO.class, false));
        file.setStorageId(storage.getId());
        fileMapper.insert(file);
        // 方便文件上传完成后获取文件信息
        fileInfo.setId(String.valueOf(file.getId()));
        if (!URLUtils.isHttpUrl(fileInfo.getUrl())) {
            String prefix = storage.getUrlPrefix();
            String url = URLUtil.normalize(prefix + fileInfo.getUrl(), false, true);
            fileInfo.setUrl(url);
            if (StrUtil.isNotBlank(fileInfo.getThUrl())) {
                fileInfo.setThUrl(URLUtil.normalize(prefix + fileInfo.getThUrl(), false, true));
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
        StorageDO storageDO = storageMapper.lambdaQuery().eq(StorageDO::getId, file.getStorageId()).one();
        return file.toFileInfo(storageDO);
    }

    @Override
    public boolean delete(String url) {
        FileDO file = this.getFileByUrl(url);
        if (file == null) {
            return false;
        }
        return fileMapper.lambdaUpdate().eq(FileDO::getId, file.getId()).remove();
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
            return queryWrapper.eq(FileDO::getPath, StrUtil.prependIfMissing(url, StringConstants.SLASH)).one();
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
        List<StorageDO> storageList = storageMapper.selectByIds(list.stream().map(FileDO::getStorageId).toList());
        Map<Long, StorageDO> storageMap = storageList.stream()
            .collect(Collectors.toMap(StorageDO::getId, storage -> storage));
        return list.stream().filter(file -> {
            // http://localhost:8000/file/user/avatar/6825e687db4174e7a297a5f8.png => http://localhost:8000/file/user/avatar
            String urlPrefix = StrUtil.subBefore(url, StringConstants.SLASH, true);
            // http://localhost:8000/file/ + /user/avatar => http://localhost:8000/file/user/avatar
            StorageDO storage = storageMap.get(file.getStorageId());
            return urlPrefix.equals(URLUtil.normalize(storage.getUrlPrefix() + file.getParentPath(), false, true));
        }).findFirst().orElse(null);
    }
}