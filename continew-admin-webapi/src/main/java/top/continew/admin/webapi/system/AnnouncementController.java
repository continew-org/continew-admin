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

package top.continew.admin.webapi.system;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import cn.dev33.satoken.annotation.SaCheckPermission;

import top.continew.admin.system.model.query.AnnouncementQuery;
import top.continew.admin.system.model.req.AnnouncementReq;
import top.continew.admin.system.model.resp.AnnouncementDetailResp;
import top.continew.admin.system.model.resp.AnnouncementResp;
import top.continew.admin.system.service.AnnouncementService;
import top.continew.starter.core.util.validate.ValidationUtils;
import top.continew.starter.extension.crud.annotation.CrudRequestMapping;
import top.continew.starter.extension.crud.controller.BaseController;
import top.continew.starter.extension.crud.util.ValidateGroup;
import top.continew.starter.web.model.R;

/**
 * 公告管理 API
 *
 * @author Charles7c
 * @since 2023/8/20 10:55
 */
@Tag(name = "公告管理 API")
@RestController
@CrudRequestMapping("/system/announcement")
public class AnnouncementController extends BaseController<AnnouncementService, AnnouncementResp, AnnouncementDetailResp, AnnouncementQuery, AnnouncementReq> {

    @Override
    @SaCheckPermission("system:announcement:add")
    public R<Long> add(@Validated(ValidateGroup.Crud.Add.class) @RequestBody AnnouncementReq req) {
        this.checkTime(req);
        return super.add(req);
    }

    @Override
    @SaCheckPermission("system:announcement:update")
    public R<Void> update(@Validated(ValidateGroup.Crud.Update.class) @RequestBody AnnouncementReq req,
                          @PathVariable Long id) {
        this.checkTime(req);
        return super.update(req, id);
    }

    /**
     * 检查时间
     *
     * @param req 创建或修改信息
     */
    private void checkTime(AnnouncementReq req) {
        LocalDateTime effectiveTime = req.getEffectiveTime();
        LocalDateTime terminateTime = req.getTerminateTime();
        if (null != effectiveTime && null != terminateTime) {
            ValidationUtils.throwIf(terminateTime.isBefore(effectiveTime), "终止时间必须晚于生效时间");
        }
    }
}