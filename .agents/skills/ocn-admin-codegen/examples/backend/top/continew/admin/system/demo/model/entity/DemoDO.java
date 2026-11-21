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
import top.continew.admin.common.base.model.entity.BaseDO;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 示例实体
 *
 * @author Charles7c
 * @since 5.0.0
 */
@Data
@TableName(value = "biz_demo", autoResultMap = true)
public class DemoDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 名称
     */
    private String name;

    /**
     * 数量
     */
    private Integer quantity;

    /**
     * 上线时间
     */
    private LocalDateTime onlineTime;

    /**
     * 扩展配置
     *
     * <p>
     * typeHandler 须与 @TableName(autoResultMap = true) 成对出现，缺后者读取恒为 null
     * </p>
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> extConfig;
}
