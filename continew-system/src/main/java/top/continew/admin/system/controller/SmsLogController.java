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

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import top.continew.admin.common.controller.BaseController;
import top.continew.admin.system.model.query.SmsLogQuery;
import top.continew.admin.system.model.req.SmsLogReq;
import top.continew.admin.system.model.resp.SmsLogResp;
import top.continew.admin.system.service.SmsLogService;
import top.continew.starter.extension.crud.annotation.CrudRequestMapping;
import top.continew.starter.extension.crud.enums.Api;

/**
 * 短信日志管理 API
 *
 * @author luoqiz
 * @author Charles7c
 * @since 2025/03/15 22:15
 */
@Tag(name = "短信日志管理 API")
@Validated
@RestController
@CrudRequestMapping(value = "/system/smsLog", api = {Api.PAGE, Api.GET, Api.DELETE, Api.EXPORT})
public class SmsLogController extends BaseController<SmsLogService, SmsLogResp, SmsLogResp, SmsLogQuery, SmsLogReq> {
}