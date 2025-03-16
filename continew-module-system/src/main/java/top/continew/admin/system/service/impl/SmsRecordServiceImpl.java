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

package top.continew.admin.system.service.impl;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import top.continew.starter.extension.crud.service.BaseServiceImpl;
import top.continew.admin.system.mapper.SmsRecordMapper;
import top.continew.admin.system.model.entity.SmsRecordDO;
import top.continew.admin.system.model.query.SmsRecordQuery;
import top.continew.admin.system.model.req.SmsRecordReq;
import top.continew.admin.system.model.resp.SmsRecordDetailResp;
import top.continew.admin.system.model.resp.SmsRecordResp;
import top.continew.admin.system.service.SmsRecordService;

/**
 * 短信记录业务实现
 *
 * @author luoqiz
 * @since 2025/03/15 22:15
 */
@Service
@RequiredArgsConstructor
public class SmsRecordServiceImpl extends BaseServiceImpl<SmsRecordMapper, SmsRecordDO, SmsRecordResp, SmsRecordDetailResp, SmsRecordQuery, SmsRecordReq> implements SmsRecordService {}