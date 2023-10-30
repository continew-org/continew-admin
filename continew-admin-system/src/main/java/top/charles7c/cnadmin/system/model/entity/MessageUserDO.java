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

package top.charles7c.cnadmin.system.model.entity;

import java.time.LocalDateTime;

import lombok.Data;

import com.baomidou.mybatisplus.annotation.TableName;

import top.charles7c.cnadmin.common.base.BaseDO;

/**
 * 消息和用户关联实体
 *
 * @author BULL_BCLS
 * @since 2023/10/15 20:25
 */
@Data
@TableName("sys_message_user")
public class MessageUserDO extends BaseDO {

    private static final long serialVersionUID = 1L;

    /**
     * 消息ID
     */
    private Long messageId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 读取状态 （0未读 1已读）
     */
    private Boolean readStatus;

    /**
     * 读取时间
     */
    private LocalDateTime readTime;
}