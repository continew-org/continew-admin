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

package top.continew.admin.common.constant;

import top.continew.starter.extension.crud.constant.ContainerPool;

/**
 * 数据源容器相关常量（Crane4j 数据填充组件使用）
 *
 * @author Charles7c
 * @since 2024/1/20 12:33
 */
public class ContainerConstants extends ContainerPool {

    /**
     * 用户昵称
     */
    public static final String USER_NICKNAME = ContainerPool.USER_NICKNAME;

    /**
     * 用户角色 ID 列表
     */
    public static final String USER_ROLE_ID_LIST = "UserRoleIdList";

    /**
     * 用户角色名称列表
     */
    public static final String USER_ROLE_NAME_LIST = "UserRoleNameList";

    private ContainerConstants() {
    }
}