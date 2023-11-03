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

import java.util.List;

import lombok.RequiredArgsConstructor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import top.charles7c.cnadmin.common.model.query.PageQuery;
import top.charles7c.cnadmin.common.model.vo.PageDataVO;
import top.charles7c.cnadmin.common.model.vo.R;
import top.charles7c.cnadmin.common.util.helper.LoginHelper;
import top.charles7c.cnadmin.monitor.annotation.Log;
import top.charles7c.cnadmin.system.model.query.MessageQuery;
import top.charles7c.cnadmin.system.model.vo.MessageUnreadVO;
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
@RequestMapping("/system/message")
public class MessageController {

    private final MessageService baseService;
    private final MessageUserService messageUserService;

    @Operation(summary = "分页查询列表", description = "分页查询列表")
    @GetMapping
    public PageDataVO<MessageVO> page(MessageQuery query, @Validated PageQuery pageQuery) {
        query.setUserId(LoginHelper.getUserId());
        return baseService.page(query, pageQuery);
    }

    @Operation(summary = "删除数据", description = "删除数据")
    @Parameter(name = "ids", description = "ID 列表", example = "1,2", in = ParameterIn.PATH)
    @DeleteMapping("/{ids}")
    public R delete(@PathVariable List<Long> ids) {
        baseService.delete(ids);
        return R.ok("删除成功");
    }

    @Operation(description = "标记已读", summary = "将消息标记为已读状态")
    @Parameter(name = "ids", description = "消息ID列表", example = "1,2", in = ParameterIn.QUERY)
    @PatchMapping("/read")
    public void readMessage(@RequestParam(required = false) List<Long> ids) {
        messageUserService.readMessage(ids);
    }

    @Log(ignore = true)
    @Operation(description = "查询未读消息数量", summary = "查询当前用户的未读消息数量")
    @Parameter(name = "isDetail", description = "是否查询详情", example = "true", in = ParameterIn.QUERY)
    @GetMapping("/unread")
    public MessageUnreadVO countUnreadMessage(@RequestParam(required = false) Boolean detail) {
        return messageUserService.countUnreadMessageByUserId(LoginHelper.getUserId(), detail);
    }
}