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

package top.continew.admin.common.service;

import cn.crane4j.annotation.ContainerMethod;
import cn.crane4j.annotation.MappingType;
import top.continew.admin.common.constant.ContainerConstants;

/**
 * 公共用户业务接口
 *
 * @author Charles7c
 * @since 2025/1/9 20:17
 */
public interface CommonUserService {

    /**
     * 根据 ID 查询昵称
     *
     * <p>
     * 数据填充容器 {@link ContainerConstants#USER_NICKNAME}
     * </p>
     * 
     * @param id ID
     * @return 昵称
     */
    @ContainerMethod(namespace = ContainerConstants.USER_NICKNAME, type = MappingType.ORDER_OF_KEYS)
    String getNicknameById(Long id);
}
