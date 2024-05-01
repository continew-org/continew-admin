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

package top.continew.admin.system.service;

import org.dromara.x.file.storage.core.FileInfo;
import org.springframework.web.multipart.MultipartFile;
import top.continew.admin.system.model.entity.FileDO;
import top.continew.admin.system.model.query.FileQuery;
import top.continew.admin.system.model.req.FileReq;
import top.continew.admin.system.model.resp.FileResp;
import top.continew.admin.system.model.resp.FileStatisticsResp;
import top.continew.starter.data.mybatis.plus.service.IService;
import top.continew.starter.extension.crud.service.BaseService;

import java.util.List;

/**
 * 文件业务接口
 *
 * @author Charles7c
 * @since 2023/12/23 10:38
 */
public interface FileService extends BaseService<FileResp, FileResp, FileQuery, FileReq>, IService<FileDO> {

    /**
     * 上传到默认存储
     *
     * @param file 文件信息
     * @return 文件信息
     */
    default FileInfo upload(MultipartFile file) {
        return upload(file, null);
    }

    /**
     * 上传到指定存储
     *
     * @param file        文件信息
     * @param storageCode 存储编码
     * @return 文件信息
     */
    FileInfo upload(MultipartFile file, String storageCode);

    /**
     * 根据存储 ID 列表查询
     *
     * @param storageIds 存储 ID 列表
     * @return 文件数量
     */
    Long countByStorageIds(List<Long> storageIds);

    /**
     * 查询文件资源统计信息
     *
     * @return 资源统计信息
     */
    FileStatisticsResp statistics();
}