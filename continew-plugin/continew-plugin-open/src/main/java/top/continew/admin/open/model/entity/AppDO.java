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

package top.continew.admin.open.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import top.continew.admin.common.enums.DisEnableStatusEnum;
import top.continew.admin.common.model.entity.BaseDO;
import top.continew.starter.security.crypto.annotation.FieldEncrypt;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 应用实体
 *
 * @author chengzi
 * @author Charles7c
 * @since 2024/10/17 16:03
 */
@Data
@TableName("sys_app")
public class AppDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 名称
     */
    private String name;

    /**
     * Access Key（访问密钥）
     */
    @FieldEncrypt
    private String accessKey;

    /**
     * Secret Key（私有密钥）
     */
    @FieldEncrypt
    private String secretKey;

    /**
     * 失效时间
     */
    private LocalDateTime expireTime;

    /**
     * 描述
     */
    private String description;

    /**
     * 状态
     */
    private DisEnableStatusEnum status;

    /**
     * 是否已过期
     *
     * @return true：已过期；false：未过期
     */
    public boolean isExpired() {
        if (expireTime == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(expireTime);
    }
}