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

package top.continew.admin;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import top.continew.admin.it.BaseIntegrationTest;
import top.continew.admin.system.service.UserService;

/**
 * 上下文加载测试
 *
 * <p>
 * 验证 Spring Boot 4 + 多级缓存 + Redisson + Sa-Token + CosID + Crane4j 全链路装配，
 * 以及 Liquibase 数据库结构初始化；依赖真实 MySQL/Redis 容器，无 Docker 环境自动跳过。
 * </p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
class ContiNewAdminApplicationTests extends BaseIntegrationTest {

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private UserService userService;

    @Test
    void contextLoads() {
        // 多级缓存管理器与核心业务 Service 装配成功，即代表全链路上下文可用
        assertThat(cacheManager).as("多级缓存管理器应完成装配").isNotNull();
        assertThat(userService).as("核心业务 Service 应完成装配").isNotNull();
    }
}
