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

package top.charles7c.cnadmin.system.model.request;

import java.util.Map;

import javax.validation.constraints.NotBlank;

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import org.hibernate.validator.constraints.Length;

import cn.hutool.core.util.StrUtil;

import top.charles7c.cnadmin.common.base.BaseRequest;

/**
 * 创建消息信息
 *
 * @author BULL_BCLS
 * @since 2023/10/15 19:05
 */
@Schema(description = "创建消息信息")
@Data
public class MessageRequest extends BaseRequest {

    private static final long serialVersionUID = 1L;

    /**
     * 主题
     */
    @Schema(description = "主题", example = "欢迎 xxx")
    @NotBlank(message = "主题不能为空")
    @Length(max = 50, message = "主题长度不能超过 {max} 个字符")
    private String title;

    /**
     * 内容
     */
    @Schema(description = "内容", example = "欢迎 xxx 来到 ContiNew Admin")
    @NotBlank(message = "内容不能为空")
    @Length(max = 255, message = "内容长度不能超过 {max} 个字符")
    private String content;

    /**
     * 类型（取值于字典 message_type）
     */
    @Schema(description = "类型（取值于字典 message_type）", example = "1")
    @NotBlank(message = "类型不能为空")
    @Length(max = 30, message = "类型长度不能超过 {max} 个字符")
    private String type;

    public void setContent(String content, Map<String, Object> contentMap) {
        this.content = StrUtil.format(content, contentMap);
    }
}