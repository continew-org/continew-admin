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

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RestController;
import top.continew.admin.common.controller.BaseController;
import top.continew.admin.system.model.query.SmsRecordQuery;
import top.continew.admin.system.model.req.SmsRecordReq;
import top.continew.admin.system.model.resp.SmsRecordDetailResp;
import top.continew.admin.system.model.resp.SmsRecordResp;
import top.continew.admin.system.service.SmsRecordService;
import top.continew.starter.extension.crud.annotation.CrudRequestMapping;
import top.continew.starter.extension.crud.enums.Api;

/**
 * 短信记录管理 API
 *
 * @author luoqiz
 * @since 2025/03/15 22:15
 */
@Tag(name = "短信记录管理 API")
@RestController
@CrudRequestMapping(value = "/system/smsRecord", api = {Api.PAGE, Api.DETAIL, Api.ADD, Api.UPDATE, Api.DELETE,
    Api.EXPORT})
public class SmsRecordController extends BaseController<SmsRecordService, SmsRecordResp, SmsRecordDetailResp, SmsRecordQuery, SmsRecordReq> {
}