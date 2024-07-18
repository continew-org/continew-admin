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

package top.continew.admin.job.service;

import top.continew.admin.job.model.query.JobQuery;
import top.continew.admin.job.model.req.JobReq;
import top.continew.admin.job.model.req.JobStatusReq;
import top.continew.admin.job.model.resp.JobResp;
import top.continew.starter.extension.crud.model.resp.PageResp;

import java.util.List;

/**
 * 任务业务接口
 *
 * @author KAI
 * @author Charles7c
 * @since 2024/6/25 17:20
 */
public interface JobService {

    /**
     * 分页查询列表
     *
     * @param query 查询条件
     * @return 分页列表信息
     */
    PageResp<JobResp> page(JobQuery query);

    /**
     * 新增
     *
     * @param req 创建信息
     * @return 新增结果
     */
    boolean add(JobReq req);

    /**
     * 修改
     *
     * @param req 修改信息
     * @param id  ID
     * @return 修改结果
     */
    boolean update(JobReq req, Long id);

    /**
     * 修改状态
     *
     * @param req 修改状态信息
     * @param id  ID
     * @return 修改状态结果
     */
    boolean updateStatus(JobStatusReq req, Long id);

    /**
     * 删除
     *
     * @param id ID
     * @return 删除结果
     */
    boolean delete(Long id);

    /**
     * 执行
     *
     * @param id ID
     * @return 执行结果
     */
    boolean trigger(Long id);

    /**
     * 查询分组列表
     * 
     * @return 分组列表
     */
    List<String> listGroup();
}
