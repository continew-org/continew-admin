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

import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import top.charles7c.cnadmin.common.model.query.PageQuery;
import top.charles7c.cnadmin.common.model.vo.PageInfo;
import top.charles7c.cnadmin.common.model.vo.R;
import top.charles7c.cnadmin.monitor.model.query.LoginLogQuery;
import top.charles7c.cnadmin.monitor.model.vo.LoginLogVO;
import top.charles7c.cnadmin.monitor.service.LogService;

/**
 * 登录日志 API
 *
 * @author Charles7c
 * @since 2023/1/16 23:17
 */
@Tag(name = "登录日志 API")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/monitor/log/login", produces = MediaType.APPLICATION_JSON_VALUE)
public class LoginLogController {

    private final LogService logService;

    @Operation(summary = "分页查询登录日志列表")
    @GetMapping
    public R<PageInfo<LoginLogVO>> list(@Validated LoginLogQuery query, @Validated PageQuery pageQuery) {
        PageInfo<LoginLogVO> pageInfo = logService.list(query, pageQuery);
        return R.ok(pageInfo);
    }
}
