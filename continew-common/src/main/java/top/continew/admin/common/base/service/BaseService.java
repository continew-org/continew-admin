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

package top.continew.admin.common.base.service;

import top.continew.starter.extension.crud.service.CrudService;

/**
 * 业务接口基类
 *
 * <p>
 * 根据实际项目需要，自行重写 CRUD 接口或增加自定义通用业务方法
 * </p>
 * 
 * @param <L> 列表类型
 * @param <D> 详情类型
 * @param <Q> 查询条件类型
 * @param <C> 创建或修改请求参数类型
 * @author Charles7c
 * @since 2024/12/6 20:30
 */
public interface BaseService<L, D, Q, C> extends CrudService<L, D, Q, C> {
}
