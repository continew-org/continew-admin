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

package top.continew.admin.system.model.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import top.continew.admin.common.enums.MessageTypeEnum;
import top.continew.starter.extension.crud.model.req.BaseReq;

import java.io.Serial;

/**
 * 创建消息信息
 *
 * @author Bull-BCLS
 * @since 2023/10/15 19:05
 */
@Data
@Schema(description = "创建消息信息")
public class MessageReq extends BaseReq {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 标题
     */
    @Schema(description = "标题", example = "欢迎注册 xxx")
    @NotBlank(message = "标题不能为空")
    @Length(max = 50, message = "标题长度不能超过 {max} 个字符")
    private String title;

    /**
     * 内容
     */
    @Schema(description = "内容", example = "尊敬的 xx，欢迎注册使用，请及时配置您的密码。")
    @NotBlank(message = "内容不能为空")
    @Length(max = 255, message = "内容长度不能超过 {max} 个字符")
    private String content;

    /**
     * 类型
     */
    @Schema(description = "类型（1：系统消息）", example = "1")
    @NotNull(message = "类型非法")
    private MessageTypeEnum type;
}