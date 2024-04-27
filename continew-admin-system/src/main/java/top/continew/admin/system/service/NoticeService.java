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

import top.continew.admin.system.model.entity.NoticeDO;
import top.continew.admin.system.model.query.NoticeQuery;
import top.continew.admin.system.model.req.NoticeReq;
import top.continew.admin.system.model.resp.NoticeDetailResp;
import top.continew.admin.system.model.resp.NoticeResp;
import top.continew.admin.system.model.resp.DashboardNoticeResp;
import top.continew.starter.extension.crud.service.BaseService;
import top.continew.starter.data.mybatis.plus.service.IService;

import java.util.List;

/**
 * 公告业务接口
 *
 * @author Charles7c
 * @since 2023/8/20 10:55
 */
public interface NoticeService extends BaseService<NoticeResp, NoticeDetailResp, NoticeQuery, NoticeReq>, IService<NoticeDO> {

    /**
     * 查询仪表盘公告列表
     *
     * @return 仪表盘公告列表
     */
    List<DashboardNoticeResp> listDashboard();
}