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

import com.alibaba.fastjson.JSONObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import top.continew.admin.common.enums.MessageTypeEnum;
import top.continew.admin.common.util.NoticeMsgUtils;
import top.continew.admin.common.util.WsUtils;
import top.continew.admin.system.model.query.NoticeQuery;
import top.continew.admin.system.model.req.MessageReq;
import top.continew.admin.system.model.req.NoticeReq;
import top.continew.admin.system.model.resp.NoticeDetailResp;
import top.continew.admin.system.model.resp.NoticeResp;
import top.continew.admin.system.service.MessageService;
import top.continew.admin.system.service.NoticeService;
import top.continew.admin.system.service.UserService;
import top.continew.starter.core.util.validate.ValidationUtils;
import top.continew.starter.extension.crud.annotation.CrudRequestMapping;
import top.continew.starter.extension.crud.controller.BaseController;
import top.continew.starter.extension.crud.enums.Api;
import top.continew.starter.extension.crud.util.ValidateGroup;
import top.continew.starter.web.model.R;

import java.time.LocalDateTime;

/**
 * 公告管理 API
 *
 * @author Charles7c
 * @since 2023/8/20 10:55
 */
@Tag(name = "公告管理 API")
@RestController
@RequiredArgsConstructor
@CrudRequestMapping(value = "/system/notice", api = {Api.PAGE, Api.GET, Api.ADD, Api.UPDATE, Api.DELETE})
public class NoticeController extends BaseController<NoticeService, NoticeResp, NoticeDetailResp, NoticeQuery, NoticeReq> {
    private final UserService userService;
    private final MessageService baseService;

    @Override
    public R<Long> add(@Validated(ValidateGroup.Crud.Add.class) @RequestBody NoticeReq req) {
        this.checkTime(req);

        //消息通知入库
        Long id = super.add(req).getData();
        //消息入库
        MessageReq messageReq = new MessageReq();
        messageReq.setTitle(req.getTitle());
        messageReq.setContent(req.getContent());
        messageReq.setType(MessageTypeEnum.SYSTEM);
        baseService.add(messageReq, userService.userIdList());
        WsUtils.sendToAll(NoticeMsgUtils.conversion(JSONObject.toJSONString(messageReq), String.valueOf(id)));
        return R.ok(id);
    }

    @Override
    public R<Void> update(@Validated(ValidateGroup.Crud.Update.class) @RequestBody NoticeReq req,
                          @PathVariable Long id) {
        this.checkTime(req);
        return super.update(req, id);
    }

    /**
     * 检查时间
     *
     * @param req 创建或修改信息
     */
    private void checkTime(NoticeReq req) {
        LocalDateTime effectiveTime = req.getEffectiveTime();
        LocalDateTime terminateTime = req.getTerminateTime();
        if (null != effectiveTime && null != terminateTime) {
            ValidationUtils.throwIf(terminateTime.isBefore(effectiveTime), "终止时间必须晚于生效时间");
        }
    }
}