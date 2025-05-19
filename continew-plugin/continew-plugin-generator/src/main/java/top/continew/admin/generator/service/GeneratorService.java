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

package top.continew.admin.generator.service;

import jakarta.servlet.http.HttpServletResponse;
import top.continew.admin.generator.model.entity.FieldConfigDO;
import top.continew.admin.generator.model.entity.GenConfigDO;
import top.continew.admin.generator.model.query.GenConfigQuery;
import top.continew.admin.generator.model.req.GenConfigReq;
import top.continew.admin.generator.model.resp.GeneratePreviewResp;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.resp.PageResp;

import java.sql.SQLException;
import java.util.List;

/**
 * 代码生成业务接口
 *
 * @author Charles7c
 * @since 2023/4/12 23:57
 */
public interface GeneratorService {

    /**
     * 分页查询生成配置列表
     *
     * @param query     查询条件
     * @param pageQuery 分页查询条件
     * @return 分页列表信息
     */
    PageResp<GenConfigDO> pageGenConfig(GenConfigQuery query, PageQuery pageQuery);

    /**
     * 查询生成配置信息
     *
     * @param tableName 表名称
     * @return 生成配置信息
     * @throws SQLException /
     */
    GenConfigDO getGenConfig(String tableName) throws SQLException;

    /**
     * 查询字段配置列表
     *
     * @param tableName   表名称
     * @param requireSync 是否需要同步
     * @return 字段配置列表
     */
    List<FieldConfigDO> listFieldConfig(String tableName, Boolean requireSync);

    /**
     * 保存代码生成配置信息
     *
     * @param req       请求参数
     * @param tableName 表名称
     */
    void saveConfig(GenConfigReq req, String tableName);

    /**
     * 生成预览
     *
     * @param tableNames 表名称列表
     * @return 预览信息
     */
    List<GeneratePreviewResp> preview(List<String> tableNames);

    /**
     * 生成下载代码
     *
     * @param tableNames 表名称列表
     * @param response   响应对象
     */
    void downloadCode(List<String> tableNames, HttpServletResponse response);

    /**
     * 生成下载代码
     *
     * @param tableNames 表名称列表
     */
    void generateCode(List<String> tableNames);
}
