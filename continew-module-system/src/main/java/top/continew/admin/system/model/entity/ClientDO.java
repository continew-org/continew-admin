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

package top.continew.admin.system.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import top.continew.admin.common.enums.DisEnableStatusEnum;
import top.continew.admin.common.model.entity.BaseDO;

import java.io.Serial;
import java.util.List;

/**
 * 客户端实体
 *
 * @author KAI
 * @author Charles7c
 * @since 2024/12/03 16:04
 */
@Data
@TableName(value = "sys_client", autoResultMap = true)
public class ClientDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 客户端 ID
     */
    private String clientId;

    /**
     * 客户端类型
     */
    private String clientType;

    /**
     * 登录类型
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> authType;

    /**
     * Token 最低活跃频率（单位：秒，-1：不限制，永不冻结）
     */
    private Long activeTimeout;

    /**
     * Token 有效期（单位：秒，-1：永不过期）
     */
    private Long timeout;

    /**
     * 状态
     */
    private DisEnableStatusEnum status;
}