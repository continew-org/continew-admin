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

import org.springframework.web.bind.annotation.RestController;

import top.charles7c.cnadmin.common.annotation.CrudRequestMapping;
import top.charles7c.cnadmin.common.base.BaseController;
import top.charles7c.cnadmin.system.model.query.PostQuery;
import top.charles7c.cnadmin.system.model.request.PostRequest;
import top.charles7c.cnadmin.system.model.vo.PostDetailVO;
import top.charles7c.cnadmin.system.model.vo.PostVO;
import top.charles7c.cnadmin.system.service.PostService;

/**
 * 岗位管理 API
 *
 * @author Charles7c
 * @since 2023/2/25 22:54
 */
@Tag(name = "岗位管理 API")
@RestController
@CrudRequestMapping("/system/post")
public class PostController extends BaseController<PostService, PostVO, PostDetailVO, PostQuery, PostRequest> {}
