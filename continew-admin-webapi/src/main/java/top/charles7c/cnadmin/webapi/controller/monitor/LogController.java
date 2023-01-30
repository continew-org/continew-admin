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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import top.charles7c.cnadmin.common.model.query.PageQuery;
import top.charles7c.cnadmin.common.model.vo.PageDataVO;
import top.charles7c.cnadmin.common.model.vo.R;
import top.charles7c.cnadmin.monitor.model.query.LoginLogQuery;
import top.charles7c.cnadmin.monitor.model.query.OperationLogQuery;
import top.charles7c.cnadmin.monitor.model.query.SystemLogQuery;
import top.charles7c.cnadmin.monitor.model.vo.LoginLogVO;
import top.charles7c.cnadmin.monitor.model.vo.OperationLogVO;
import top.charles7c.cnadmin.monitor.model.vo.SystemLogDetailVO;
import top.charles7c.cnadmin.monitor.model.vo.SystemLogVO;
import top.charles7c.cnadmin.monitor.service.LogService;

/**
 * 日志管理 API
 *
 * @author Charles7c
 * @since 2023/1/18 23:55
 */
@Tag(name = "日志管理 API")
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/monitor/log", produces = MediaType.APPLICATION_JSON_VALUE)
public class LogController {

    private final LogService logService;

    @Operation(summary = "分页查询登录日志列表")
    @GetMapping("/login")
    public R<PageDataVO<LoginLogVO>> list(@Validated LoginLogQuery query, @Validated PageQuery pageQuery) {
        PageDataVO<LoginLogVO> pageDataVO = logService.list(query, pageQuery);
        return R.ok(pageDataVO);
    }

    @Operation(summary = "分页查询操作日志列表")
    @GetMapping("/operation")
    public R<PageDataVO<OperationLogVO>> list(@Validated OperationLogQuery query, @Validated PageQuery pageQuery) {
        PageDataVO<OperationLogVO> pageDataVO = logService.list(query, pageQuery);
        return R.ok(pageDataVO);
    }

    @Operation(summary = "分页查询系统日志列表")
    @GetMapping("/system")
    public R<PageDataVO<SystemLogVO>> list(@Validated SystemLogQuery query, @Validated PageQuery pageQuery) {
        PageDataVO<SystemLogVO> pageDataVO = logService.list(query, pageQuery);
        return R.ok(pageDataVO);
    }

    @Operation(summary = "查看系统日志详情")
    @GetMapping("/system/{logId}")
    public R<SystemLogDetailVO> detail(@PathVariable Long logId) {
        SystemLogDetailVO detailVO = logService.detail(logId);
        return R.ok(detailVO);
    }
}
