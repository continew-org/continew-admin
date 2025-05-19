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

package top.continew.admin.common.model.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import top.continew.admin.common.enums.DisEnableStatusEnum;

import java.io.Serializable;

/**
 * 状态修改请求参数
 *
 * @author Charles7c
 * @since 2025/3/4 20:09
 */
@Data
@Schema(description = "状态修改请求参数")
public class CommonStatusUpdateReq implements Serializable {

    /**
     * 状态
     */
    @Schema(description = "状态", example = "1")
    @NotNull(message = "状态无效")
    private DisEnableStatusEnum status;
}
