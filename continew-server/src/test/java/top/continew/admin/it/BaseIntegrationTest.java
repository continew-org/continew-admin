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

package top.continew.admin.it;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

/**
 * Testcontainers 集成测试基类
 *
 * <p>
 * 采用 singleton 容器模式：MySQL 与 Redis 容器声明为静态字段，跨测试类复用同一实例，
 * Liquibase 建表与 Spring 上下文启动成本仅支付一次。无 Docker 环境（本地开发机）经
 * {@code @Testcontainers(disabledWithoutDocker)} 自动跳过全部集成测试。
 * </p>
 * <p>
 * 容器不使用 {@code @Container} 注解管理（该注解会在每个测试类结束后停止容器，破坏
 * singleton 复用），改由 {@link #startContainers()} 在首个测试类启动，JVM 退出后由
 * Ryuk 守护容器自动回收。测试上下文沿用 dev 配置（凭据均为仓库内声明的开发示例值），
 * 仅覆盖容器相关的数据源与 Redis 连接项。
 * </p>
 *
 * @author Charles7c
 * @since 5.0.0
 */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
public abstract class BaseIntegrationTest {

    private static final MySQLContainer<?> MYSQL = new MySQLContainer<>(
        DockerImageName.parse("mysql:8.0.42")).withDatabaseName("continew_admin");

    private static final GenericContainer<?> REDIS = new GenericContainer<>(
        DockerImageName.parse("redis:7.2.8")).withExposedPorts(6379)
        .withCommand("redis-server", "--requirepass", "123456");

    /**
     * 启动 singleton 容器（已运行时直接复用）
     */
    @BeforeAll
    static void startContainers() {
        if (!MYSQL.isRunning()) {
            Startables.deepStart(MYSQL, REDIS).join();
        }
    }

    /**
     * 将容器连接信息注入 Spring 上下文
     *
     * <p>
     * 数据源沿用 dev 配置的 p6spy 驱动与连接参数，仅将主机与端口指向容器；Redis 容器
     * 以 dev 配置的弱默认值作为访问密码（Redisson 对空串密码仍会发送 AUTH 导致连接
     * 失败，容器侧设密码与 dev 配置对齐，其余连接项原样沿用）。
     * </p>
     *
     * @param registry 动态属性注册器
     */
    @DynamicPropertySource
    static void containerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", BaseIntegrationTest::mysqlUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
    }

    /**
     * 构造指向容器的 p6spy JDBC URL（MySQLContainer 原生 URL 无 p6spy 前缀，
     * 而 dev 配置的 driver-class-name 固定为 P6SpyDriver）
     *
     * @return JDBC URL
     */
    private static String mysqlUrl() {
        String params = "?serverTimezone=Asia/Shanghai&useSSL=false&useUnicode=true"
            + "&characterEncoding=utf8&allowPublicKeyRetrieval=true&rewriteBatchedStatements=true";
        return "jdbc:p6spy:mysql://" + MYSQL.getHost() + ":" + MYSQL.getMappedPort(3306) + "/"
            + MYSQL.getDatabaseName() + params;
    }
}
