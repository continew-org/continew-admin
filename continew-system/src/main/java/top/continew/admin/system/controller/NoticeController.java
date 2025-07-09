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

import cn.hutool.core.collection.CollUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RestController;
import top.continew.admin.common.controller.BaseController;
import top.continew.admin.system.enums.NoticeMethodEnum;
import top.continew.admin.system.enums.NoticeScopeEnum;
import top.continew.admin.system.model.query.NoticeQuery;
import top.continew.admin.system.model.req.NoticeReq;
import top.continew.admin.system.model.resp.notice.NoticeDetailResp;
import top.continew.admin.system.model.resp.notice.NoticeResp;
import top.continew.admin.system.service.NoticeService;
import top.continew.starter.core.validation.ValidationUtils;
import top.continew.starter.extension.crud.annotation.CrudApi;
import top.continew.starter.extension.crud.annotation.CrudRequestMapping;
import top.continew.starter.extension.crud.enums.Api;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * 公告管理 API
 *
 * @author Charles7c
 * @since 2023/8/20 10:55
 */
@Tag(name = "公告管理 API")
@RestController
@CrudRequestMapping(value = "/system/notice", api = {Api.PAGE, Api.GET, Api.CREATE, Api.UPDATE, Api.DELETE})
public class NoticeController extends BaseController<NoticeService, NoticeResp, NoticeDetailResp, NoticeQuery, NoticeReq> {

    @Override
    public void preHandle(CrudApi crudApi, Object[] args, Method targetMethod, Class<?> targetClass) throws Exception {
        super.preHandle(crudApi, args, targetMethod, targetClass);
        Api api = crudApi.value();
        if (!(Api.CREATE.equals(api) || Api.UPDATE.equals(api))) {
            return;
        }
        NoticeReq req = (NoticeReq)args[0];
        // 校验通知范围
        if (NoticeScopeEnum.USER.equals(req.getNoticeScope())) {
            ValidationUtils.throwIfEmpty(req.getNoticeUsers(), "通知用户不能为空");
        }
        // 校验通知方式
        List<Integer> noticeMethods = req.getNoticeMethods();
        if (CollUtil.isNotEmpty(noticeMethods)) {
            List<Integer> validMethods = Arrays.stream(NoticeMethodEnum.values())
                .map(NoticeMethodEnum::getValue)
                .toList();
            noticeMethods.forEach(method -> ValidationUtils.throwIf(!validMethods
                .contains(method), "通知方式 [{}] 不正确", method));
        }
    }
}