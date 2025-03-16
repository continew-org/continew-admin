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

import top.continew.starter.extension.crud.enums.Api;

import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.*;

import top.continew.starter.extension.crud.annotation.CrudRequestMapping;
import top.continew.admin.common.controller.BaseController;
import top.continew.admin.system.model.query.SmsConfigQuery;
import top.continew.admin.system.model.req.SmsConfigReq;
import top.continew.admin.system.model.resp.SmsConfigDetailResp;
import top.continew.admin.system.model.resp.SmsConfigResp;
import top.continew.admin.system.service.SmsConfigService;

/**
 * 短信服务配置管理 API
 *
 * @author luoqiz
 * @since 2025/03/15 18:41
 */
@Tag(name = "短信服务配置管理 API")
@RestController
@CrudRequestMapping(value = "/system/smsConfig", api = {Api.PAGE, Api.DETAIL, Api.ADD, Api.UPDATE, Api.DELETE,
    Api.EXPORT})
public class SmsConfigController extends BaseController<SmsConfigService, SmsConfigResp, SmsConfigDetailResp, SmsConfigQuery, SmsConfigReq> {}