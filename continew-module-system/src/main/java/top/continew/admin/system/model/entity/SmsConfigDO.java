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

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import top.continew.admin.common.enums.DisEnableStatusEnum;
import top.continew.admin.common.model.entity.BaseDO;
import top.continew.starter.security.crypto.annotation.FieldEncrypt;

import java.io.Serial;

/**
 * 短信配置实体
 *
 * @author luoqiz
 * @author Charles7c
 * @since 2025/03/15 18:41
 */
@Data
@TableName("sys_sms_config")
public class SmsConfigDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 名称
     */
    private String name;

    /**
     * 厂商
     */
    private String supplier;

    /**
     * Access Key
     */
    private String accessKey;

    /**
     * Secret Key
     */
    @FieldEncrypt
    private String secretKey;

    /**
     * 短信签名
     */
    private String signature;

    /**
     * 模板 ID
     */
    private String templateId;

    /**
     * 负载均衡权重
     */
    private Integer weight;

    /**
     * 重试间隔（单位：秒）
     */
    private Integer retryInterval;

    /**
     * 重试次数
     */
    private Integer maxRetries;

    /**
     * 发送上限
     */
    private Integer maximum;

    /**
     * 各个厂商独立配置
     */
    private String supplierConfig;

    /**
     * 是否为默认存储
     */
    private Boolean isDefault;

    /**
     * 状态
     */
    private DisEnableStatusEnum status;
}