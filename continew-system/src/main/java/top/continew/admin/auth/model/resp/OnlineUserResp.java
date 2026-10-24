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

package top.continew.admin.auth.model.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 在线用户响应参数
 *
 * @author Charles7c
 * @since 1.0.0
 */
@Data
@Schema(description = "在线用户响应参数")
public class OnlineUserResp implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Schema(description = "ID", example = "1")
    private Long id;

    /**
     * 登录会话 ID
     */
    @Schema(description = "登录会话 ID", example = "Zm9vYmFyYmF6cXV4MTIzNA")
    private String sessionId;

    /**
     * 用户名
     */
    @Schema(description = "用户名", example = "zhangsan")
    private String username;

    /**
     * 昵称
     */
    @Schema(description = "昵称", example = "张三")
    private String nickname;

    /**
     * 客户端类型
     */
    @Schema(description = "客户端类型", example = "PC")
    private String clientType;

    /**
     * 客户端 ID
     */
    @Schema(description = "客户端 ID", example = "ef51c9a3e9046c4f2ea45142c8a8344a")
    private String clientId;

    /**
     * 登录 IP
     */
    @Schema(description = "登录 IP", example = "")
    private String ip;

    /**
     * 登录地点
     */
    @Schema(description = "登录地点", example = "中国北京北京市")
    private String address;

    /**
     * 浏览器
     */
    @Schema(description = "浏览器", example = "Chrome 115.0.0.0")
    private String browser;

    /**
     * 操作系统
     */
    @Schema(description = "操作系统", example = "Windows 10")
    private String os;

    /**
     * 登录时间
     */
    @Schema(description = "登录时间", example = "2023-08-08 08:08:08", type = "string")
    private LocalDateTime loginTime;

    /**
     * 最后刷新时间
     */
    @Schema(description = "最后刷新时间", example = "2023-08-08 08:08:08", type = "string")
    private LocalDateTime lastRefreshTime;

    /**
     * 最后活跃时间
     *
     * <p>只统计业务请求。WebSocket 消息、令牌刷新和退出登录不计入活跃，页面挂着不代表人在操作。</p>
     */
    @Schema(description = "最后活跃时间", example = "2023-08-08 08:08:08", type = "string")
    private LocalDateTime lastActiveTime;

    /**
     * 会话到期时间
     *
     * <p>这是登录后最长能维持的时间（绝对有效期），不会因令牌轮换而延长。</p>
     */
    @Schema(description = "会话到期时间（绝对有效期）", example = "2023-09-07 08:08:08", type = "string")
    private LocalDateTime sessionExpireTime;

    /**
     * 预计因闲置失效的时间
     *
     * <p>客户端未配置闲置失效阈值时为 null。</p>
     */
    @Schema(description = "预计因闲置失效的时间，未配置闲置失效时为 null", example = "2023-08-08 12:08:08",
        type = "string")
    private LocalDateTime idleExpireTime;

    /**
     * 是否已闲置超时
     *
     * <p>由服务端判定，客户端不要用本地时钟与 {@code idleExpireTime} 比较。</p>
     */
    @Schema(description = "是否已闲置超时", example = "false")
    private Boolean idleExpired;

    /**
     * 是否在线
     */
    @Schema(description = "是否在线", example = "true")
    private Boolean online;

    /**
     * 最后活跃时间是否精确
     *
     * <p>为 false 表示该会话没有活跃记录，时间按最近刷新时间推算，展示时需要提示是估算值。</p>
     */
    @Schema(description = "最后活跃时间是否精确（false 表示按最近刷新时间推算）", example = "true")
    private Boolean activeTimeExact;
}
