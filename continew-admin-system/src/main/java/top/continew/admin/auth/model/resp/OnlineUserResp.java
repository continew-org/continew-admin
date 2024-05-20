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

import cn.crane4j.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.continew.admin.auth.service.OnlineUserService;
import top.continew.admin.common.constant.ContainerConstants;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 在线用户信息
 *
 * @author Charles7c
 * @since 2023/1/20 21:54
 */
@Data
@Schema(description = "在线用户信息")
public class OnlineUserResp implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Schema(description = "ID", example = "1")
    @Assemble(prop = ":nickname", container = ContainerConstants.USER_NICKNAME)
    private Long id;

    /**
     * 令牌
     */
    @Schema(description = "令牌", example = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJsb2dpblR5cGUiOiJsb2dpbiIsImxvZ2luSWQiOjEsInJuU3RyIjoiTUd6djdyOVFoeHEwdVFqdFAzV3M5YjVJRzh4YjZPSEUifQ.7q7U3ouoN7WPhH2kUEM7vPe5KF3G_qavSG-vRgIxKvE")
    @AssembleMethod(prop = ":lastActiveTime", targetType = OnlineUserService.class, method = @ContainerMethod(bindMethod = "getLastActiveTime", type = MappingType.ORDER_OF_KEYS))
    private String token;

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
     * 最后活跃时间
     */
    @Schema(description = "最后活跃时间", example = "2023-08-08 08:08:08", type = "string")
    private LocalDateTime lastActiveTime;
}
