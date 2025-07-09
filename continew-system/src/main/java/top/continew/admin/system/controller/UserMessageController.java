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

package top.continew.admin.system.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.continew.admin.common.context.UserContextHolder;
import top.continew.admin.system.enums.NoticeMethodEnum;
import top.continew.admin.system.enums.NoticeScopeEnum;
import top.continew.admin.system.model.query.MessageQuery;
import top.continew.admin.system.model.query.NoticeQuery;
import top.continew.admin.system.model.resp.message.MessageDetailResp;
import top.continew.admin.system.model.resp.message.MessageResp;
import top.continew.admin.system.model.resp.message.MessageUnreadResp;
import top.continew.admin.system.model.resp.notice.NoticeDetailResp;
import top.continew.admin.system.model.resp.notice.NoticeResp;
import top.continew.admin.system.model.resp.notice.NoticeUnreadCountResp;
import top.continew.admin.system.service.MessageService;
import top.continew.admin.system.service.NoticeService;
import top.continew.starter.core.validation.CheckUtils;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.req.IdsReq;
import top.continew.starter.extension.crud.model.resp.BasePageResp;
import top.continew.starter.extension.crud.model.resp.PageResp;
import top.continew.starter.log.annotation.Log;

import java.util.Collections;
import java.util.List;

/**
 * 个人消息 API
 *
 * @author Charles7c
 * @since 2025/4/5 21:30
 */
@Tag(name = "个人消息 API")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/user/message")
public class UserMessageController {

    private final NoticeService noticeService;
    private final MessageService messageService;

    @Log(ignore = true)
    @Operation(summary = "查询未读消息数量", description = "查询当前用户的未读消息数量")
    @Parameter(name = "isDetail", description = "是否查询详情", example = "true", in = ParameterIn.QUERY)
    @GetMapping("/unread")
    public MessageUnreadResp countUnreadMessage(@RequestParam(required = false) Boolean detail) {
        return messageService.countUnreadByUserId(UserContextHolder.getUserId(), detail);
    }

    @Operation(summary = "分页查询消息列表", description = "分页查询消息列表")
    @GetMapping
    public PageResp<MessageResp> page(MessageQuery query, @Validated PageQuery pageQuery) {
        query.setUserId(UserContextHolder.getUserId());
        return messageService.page(query, pageQuery);
    }

    @Operation(summary = "查询消息", description = "查询消息详情")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @GetMapping("/{id}")
    public MessageDetailResp getMessage(@PathVariable Long id) {
        MessageDetailResp detail = messageService.get(id);
        CheckUtils.throwIf(detail == null || (NoticeScopeEnum.USER.equals(detail.getScope()) && !detail.getUsers()
            .contains(UserContextHolder.getUserId().toString())), "消息不存在或无权限访问");
        messageService.readMessage(Collections.singletonList(id), UserContextHolder.getUserId());
        detail.setIsRead(true);
        return detail;
    }

    @Operation(summary = "删除消息", description = "删除消息")
    @DeleteMapping
    public void delete(@Validated @RequestBody IdsReq req) {
        messageService.delete(req.getIds());
    }

    @Operation(summary = "消息标记为已读", description = "将消息标记为已读状态")
    @PatchMapping("/read")
    public void read(@Validated @RequestBody IdsReq req) {
        messageService.readMessage(req.getIds(), UserContextHolder.getUserId());
    }

    @Operation(summary = "消息全部已读", description = "将所有消息标记为已读状态")
    @PatchMapping("/readAll")
    public void readAll() {
        messageService.readMessage(null, UserContextHolder.getUserId());
    }

    @Log(ignore = true)
    @Operation(summary = "查询未读公告数量", description = "查询当前用户的未读公告数量")
    @GetMapping("/notice/unread")
    public NoticeUnreadCountResp countUnreadNotice() {
        List<Long> list = noticeService.listUnreadIdsByUserId(null, UserContextHolder.getUserId());
        return new NoticeUnreadCountResp(list.size());
    }

    @Log(ignore = true)
    @Operation(summary = "查询未读公告", description = "查询当前用户的未读公告")
    @Parameter(name = "method", description = "通知方式", example = "LOGIN_POPUP", in = ParameterIn.PATH)
    @GetMapping("/notice/unread/{method}")
    public List<Long> listUnreadNotice(@PathVariable String method) {
        return noticeService.listUnreadIdsByUserId(NoticeMethodEnum.valueOf(method), UserContextHolder.getUserId());
    }

    @Operation(summary = "分页查询公告列表", description = "分页查询公告列表")
    @GetMapping("/notice")
    public BasePageResp<NoticeResp> pageNotice(@Validated NoticeQuery query, @Validated PageQuery pageQuery) {
        query.setUserId(UserContextHolder.getUserId());
        return noticeService.page(query, pageQuery);
    }

    @Operation(summary = "查询公告", description = "查询公告详情")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @GetMapping("/notice/{id}")
    public NoticeDetailResp getNotice(@PathVariable Long id) {
        NoticeDetailResp detail = noticeService.get(id);
        CheckUtils.throwIf(detail == null || (NoticeScopeEnum.USER.equals(detail.getNoticeScope()) && !detail
            .getNoticeUsers()
            .contains(UserContextHolder.getUserId().toString())), "公告不存在或无权限访问");
        noticeService.readNotice(id, UserContextHolder.getUserId());
        return detail;
    }
}
