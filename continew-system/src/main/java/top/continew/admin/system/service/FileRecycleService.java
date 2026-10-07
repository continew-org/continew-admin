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

import top.continew.admin.system.model.query.FileQuery;
import top.continew.admin.system.model.resp.file.FileResp;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.resp.PageResp;

import jakarta.validation.Valid;

/**
 * 文件回收站业务接口
 *
 * @author Charles7c
 * @since 4.1.0
 */
public interface FileRecycleService {

    /**
     * 分页查询列表
     *
     * @param query     查询参数
     * @param pageQuery 分页参数
     * @return 文件列表
     */
    PageResp<FileResp> page(@Valid FileQuery query, @Valid PageQuery pageQuery);

    /**
     * 还原文件
     *
     * @param id ID
     */
    void restore(Long id);

    /**
     * 删除文件
     *
     * @param id ID
     */
    void delete(Long id);

    /**
     * 清空回收站
     */
    void clean();
}
