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

package top.charles7c.cnadmin.webapi.controller.monitor;

import lombok.RequiredArgsConstructor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;

import top.charles7c.cnadmin.auth.model.query.OnlineUserQuery;
import top.charles7c.cnadmin.auth.model.vo.OnlineUserVO;
import top.charles7c.cnadmin.auth.service.OnlineUserService;
import top.charles7c.cnadmin.common.model.query.PageQuery;
import top.charles7c.cnadmin.common.model.vo.PageDataVO;
import top.charles7c.cnadmin.common.model.vo.R;
import top.charles7c.cnadmin.common.util.validate.CheckUtils;

/**
 * 在线用户 API
 *
 * @author Charles7c
 * @since 2023/1/20 21:51
 */
@Tag(name = "在线用户 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/monitor/online/user")
public class OnlineUserController {

    private final OnlineUserService onlineUserService;

    @Operation(summary = "分页查询列表")
    @SaCheckPermission("monitor:online:user:list")
    @GetMapping
    public R<PageDataVO<OnlineUserVO>> page(@Validated OnlineUserQuery query, @Validated PageQuery pageQuery) {
        return R.ok(onlineUserService.page(query, pageQuery));
    }

    @Operation(summary = "强退在线用户")
    @SaCheckPermission("monitor:online:user:delete")
    @DeleteMapping("/{token}")
    public R kickout(@PathVariable String token) {
        String currentToken = StpUtil.getTokenValue();
        CheckUtils.throwIfEqual(token, currentToken, "不能强退当前登录");

        StpUtil.kickoutByTokenValue(token);
        return R.ok("强退成功");
    }
}
