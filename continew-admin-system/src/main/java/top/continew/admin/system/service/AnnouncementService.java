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

import top.continew.admin.system.model.entity.AnnouncementDO;
import top.continew.admin.system.model.query.AnnouncementQuery;
import top.continew.admin.system.model.req.AnnouncementReq;
import top.continew.admin.system.model.resp.AnnouncementDetailResp;
import top.continew.admin.system.model.resp.AnnouncementResp;
import top.continew.admin.system.model.resp.DashboardAnnouncementResp;
import top.continew.starter.extension.crud.service.BaseService;
import top.continew.starter.data.mybatis.plus.service.IService;

import java.util.List;

/**
 * 公告业务接口
 *
 * @author Charles7c
 * @since 2023/8/20 10:55
 */
public interface AnnouncementService extends BaseService<AnnouncementResp, AnnouncementDetailResp, AnnouncementQuery, AnnouncementReq>, IService<AnnouncementDO> {

    /**
     * 查询仪表盘公告列表
     *
     * @return 仪表盘公告列表
     */
    List<DashboardAnnouncementResp> listDashboard();
}