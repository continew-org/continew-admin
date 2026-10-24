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

package top.continew.admin.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 在线用户配置属性
 *
 * @author luoqiz
 * @since 5.0.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "auth.online")
public class OnlineUserProperties {

    /**
     * 在线判定窗口
     *
     * <p>最后活跃时间落在该窗口内才视为「在线」，否则展示为「闲置」。它只影响界面展示与筛选，
     * 不参与任何失效判断；会话是否失效由客户端的闲置失效阈值决定。</p>
     */
    private Duration presenceWindow = Duration.ofMinutes(5);
}
