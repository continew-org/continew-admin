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

package top.charles7c.cnadmin.system.service.impl;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import top.charles7c.cnadmin.common.base.BaseServiceImpl;
import top.charles7c.cnadmin.system.mapper.AnnouncementMapper;
import top.charles7c.cnadmin.system.model.entity.AnnouncementDO;
import top.charles7c.cnadmin.system.model.query.AnnouncementQuery;
import top.charles7c.cnadmin.system.model.request.AnnouncementRequest;
import top.charles7c.cnadmin.system.model.vo.AnnouncementDashboardVO;
import top.charles7c.cnadmin.system.model.vo.AnnouncementDetailVO;
import top.charles7c.cnadmin.system.model.vo.AnnouncementVO;
import top.charles7c.cnadmin.system.service.AnnouncementService;

/**
 * 公告业务实现
 *
 * @author Charles7c
 * @since 2023/8/20 10:55
 */
@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl extends BaseServiceImpl<AnnouncementMapper, AnnouncementDO, AnnouncementVO,
    AnnouncementDetailVO, AnnouncementQuery, AnnouncementRequest> implements AnnouncementService {

    @Override
    public List<AnnouncementDashboardVO> listDashboard() {
        return baseMapper.selectDashboardList();
    }
}