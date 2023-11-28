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

package top.charles7c.cnadmin.webapi.controller.system;

import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.*;

import top.charles7c.cnadmin.system.model.query.DictQuery;
import top.charles7c.cnadmin.system.model.req.DictReq;
import top.charles7c.cnadmin.system.model.resp.DictDetailResp;
import top.charles7c.cnadmin.system.model.resp.DictResp;
import top.charles7c.cnadmin.system.service.DictService;
import top.charles7c.continew.starter.extension.crud.annotation.CrudRequestMapping;
import top.charles7c.continew.starter.extension.crud.base.BaseController;

/**
 * 字典管理 API
 *
 * @author Charles7c
 * @since 2023/9/11 21:29
 */
@Tag(name = "字典管理 API")
@RestController
@CrudRequestMapping("/system/dict")
public class DictController extends BaseController<DictService, DictResp, DictDetailResp, DictQuery, DictReq> {}