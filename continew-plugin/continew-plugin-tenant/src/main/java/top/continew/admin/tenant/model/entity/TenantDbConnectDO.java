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

package top.continew.admin.tenant.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import top.continew.admin.common.base.model.entity.BaseDO;

import java.io.Serial;

/**
 * 租户数据连接实体
 *
 * @author 小熊
 * @since 2024/12/12 19:13
 */
@Data
@TableName("sys_tenant_db_connect")
public class TenantDbConnectDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 连接名称
     */
    private String connectName;

    /**
     * 连接类型
     */
    private Integer type;

    /**
     * 连接主机地址
     */
    private String host;

    /**
     * 连接端口
     */
    private Integer port;

    /**
     * 连接用户名
     */
    private String username;

    /**
     * 连接密码
     */
    private String password;
}