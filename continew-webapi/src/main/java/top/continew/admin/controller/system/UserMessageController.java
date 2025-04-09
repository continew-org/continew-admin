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

package top.continew.admin.controller.system;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.continew.admin.common.context.UserContextHolder;
import top.continew.admin.system.enums.NoticeScopeEnum;
import top.continew.admin.system.model.query.NoticeQuery;
import top.continew.admin.system.model.resp.NoticeDetailResp;
import top.continew.admin.system.model.resp.NoticeResp;
import top.continew.admin.system.service.NoticeService;
import top.continew.starter.core.validation.CheckUtils;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.resp.BasePageResp;

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

    @Operation(summary = "分页查询公告列表", description = "分页查询公告列表")
    @GetMapping("/notice")
    public BasePageResp<NoticeResp> pageNotice(@Validated NoticeQuery query, @Validated PageQuery pageQuery) {
        query.setUserId(UserContextHolder.getUserId());
        return noticeService.page(query, pageQuery);
    }

    @Operation(summary = "查询公告详情", description = "查询公告")
    @Parameter(name = "id", description = "ID", example = "1", in = ParameterIn.PATH)
    @GetMapping("/notice/{id}")
    public NoticeDetailResp getNotice(@PathVariable Long id) {
        NoticeDetailResp detail = noticeService.get(id);
        CheckUtils.throwIf(detail == null || (NoticeScopeEnum.USER.equals(detail.getNoticeScope()) && !detail
            .getNoticeUsers()
            .contains(UserContextHolder.getUserId().toString())), "公告不存在或无权限访问");
        return detail;
    }
}
