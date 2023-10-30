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

package top.charles7c.cnadmin.webapi.controller.system;

import static top.charles7c.cnadmin.common.annotation.CrudRequestMapping.Api;

import java.util.List;

import lombok.RequiredArgsConstructor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import top.charles7c.cnadmin.common.annotation.CrudRequestMapping;
import top.charles7c.cnadmin.common.base.BaseController;
import top.charles7c.cnadmin.system.model.query.MessageQuery;
import top.charles7c.cnadmin.system.model.request.MessageRequest;
import top.charles7c.cnadmin.system.model.vo.MessageVO;
import top.charles7c.cnadmin.system.service.MessageService;
import top.charles7c.cnadmin.system.service.MessageUserService;

/**
 * 消息管理 API
 *
 * @author BULL_BCLS
 * @since 2023/10/15 19:05
 */
@Tag(name = "消息管理 API")
@RestController
@RequiredArgsConstructor
@CrudRequestMapping(value = "/system/message", api = {Api.PAGE, Api.GET, Api.DELETE, Api.LIST})
public class MessageController
    extends BaseController<MessageService, MessageVO, MessageVO, MessageQuery, MessageRequest> {

    private final MessageUserService messageUserService;

    @Operation(description = "标记已读", summary = "将消息标记为已读状态")
    @Parameter(name = "ids", description = "消息ID列表", example = "1,2", in = ParameterIn.QUERY)
    @PatchMapping("/read")
    public void readMessage(@RequestParam(required = false) List<Long> ids) {
        messageUserService.readMessage(ids);
    }
}