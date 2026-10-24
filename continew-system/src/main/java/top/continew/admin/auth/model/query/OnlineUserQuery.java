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

package top.continew.admin.auth.model.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 在线用户查询条件
 *
 * @author Charles7c
 * @since 1.0.0
 */
@Data
@Schema(description = "在线用户查询条件")
public class OnlineUserQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户昵称
     */
    @Schema(description = "用户昵称", example = "张三")
    private String nickname;

    /**
     * 客户端 ID
     */
    @Schema(description = "客户端 ID", example = "ef51c9a3e9046c4f2ea45142c8a8344a")
    private String clientId;

    /**
     * 登录时间
     */
    @Schema(description = "登录时间", example = "2023-08-08 00:00:00,2023-08-08 23:59:59")
    private List<LocalDateTime> loginTime;

    /**
     * 是否在线
     *
     * <p>为空表示不过滤。在线是按活跃时间派生的展示口径，与「会话是否有效」不是一回事。</p>
     */
    @Schema(description = "是否在线（为空表示不过滤）", example = "true")
    private Boolean online;

    /**
     * 用户 ID
     */
    @Schema(hidden = true)
    private Long userId;

    /**
     * 角色 ID
     */
    @Schema(hidden = true)
    private Long roleId;
}
