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

package top.charles7c.continew.admin.system.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import top.charles7c.continew.admin.system.model.query.FileQuery;
import top.charles7c.continew.admin.system.model.req.FileReq;
import top.charles7c.continew.admin.system.model.resp.FileDetailResp;
import top.charles7c.continew.admin.system.model.resp.FileResp;
import top.charles7c.continew.starter.extension.crud.base.BaseService;

/**
 * 文件业务接口
 *
 * @author Charles7c
 * @since 2023/12/23 10:38
 */
public interface FileService extends BaseService<FileResp, FileDetailResp, FileQuery, FileReq> {

    /**
     * 上传到默认存储库
     *
     * @param file
     *            文件信息
     */
    default void upload(MultipartFile file) {
        upload(file, null);
    }

    /**
     * 上传到指定存储库
     *
     * @param file
     *            文件信息
     * @param storageCode
     *            存储库编码
     */
    void upload(MultipartFile file, String storageCode);

    /**
     * 根据存储库 ID 列表查询
     *
     * @param storageIds
     *            存储库 ID 列表
     * @return 文件数量
     */
    Long countByStorageIds(List<Long> storageIds);
}