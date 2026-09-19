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

package top.continew.admin.auth.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.convert.Convert;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.continew.admin.auth.model.query.OnlineUserQuery;
import top.continew.admin.auth.model.SessionView;
import top.continew.admin.auth.model.resp.OnlineUserResp;
import top.continew.admin.auth.service.OnlineUserService;
import top.continew.admin.auth.service.SessionInvalidationService;
import top.continew.admin.auth.service.SessionQueryService;
import top.continew.admin.common.context.UserContextHolder;
import top.continew.admin.auth.api.AuthSessionConstants;
import top.continew.starter.core.util.validation.CheckUtils;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.resp.PageResp;
import top.continew.starter.extension.tenant.context.TenantContextHolder;

import java.util.Objects;

/**
 * 在线用户 API
 *
 * @author Charles7c
 * @since 2023/1/20 21:51
 */
@Tag(name = "在线用户 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/monitor/online")
public class OnlineUserController {

    private final OnlineUserService baseService;
    private final SessionInvalidationService sessionInvalidationService;
    private final SessionQueryService sessionQueryService;

    /**
     * 分页查询在线用户
     *
     * @param query 查询条件
     * @param pageQuery 分页查询条件
     * @return 在线用户分页信息
     */
    @Operation(summary = "分页查询列表", description = "分页查询列表")
    @SaCheckPermission("monitor:online:list")
    @GetMapping
    public PageResp<OnlineUserResp> page(@Valid OnlineUserQuery query, @Valid PageQuery pageQuery) {
        return baseService.page(query, pageQuery);
    }

    /**
     * 强退在线用户
     *
     * @param sessionId 登录会话 ID
     */
    @Operation(summary = "强退在线用户", description = "强退在线用户")
    @Parameter(name = "sessionId", description = "登录会话 ID",
        example = "Zm9vYmFyYmF6cXV4MTIzNA",
        in = ParameterIn.PATH)
    @SaCheckPermission("monitor:online:kickout")
    @DeleteMapping("/{sessionId}")
    public void kickout(@PathVariable String sessionId) {
        String currentSessionId = Convert.toStr(StpUtil.getExtra(StpUtil.getTokenValue(),
            AuthSessionConstants.SESSION_ID_CLAIM));
        CheckUtils.throwIfEqual(sessionId, currentSessionId, "不能强退自己");
        SessionView targetSession = sessionQueryService.getSession(sessionId);
        CheckUtils.throwIfNull(targetSession, "登录会话不存在");
        // 超级管理员可以跨租户管理在线用户；其他管理员只能操作当前租户，避免仅凭
        // 一个 Access Token 就跨租户撤销会话。
        if (TenantContextHolder.isTenantEnabled() && !UserContextHolder.isSuperAdmin()) {
            CheckUtils.throwIf(() -> !Objects.equals(TenantContextHolder.getTenantId(),
                targetSession.getTenantId()), "您当前没有操作该租户登录会话的权限");
        }
        // Session 是登录态的权威记录；删除后该设备的全部 Access/Refresh Token 都失效。
        sessionInvalidationService.revokeSession(sessionId);
    }
}
