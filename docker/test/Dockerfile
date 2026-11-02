# L3 测试环境自包含镜像（docker compose --build 模式使用）
# 适用于没有本地 Maven 环境的社区场景：依赖在构建容器内下载，首次构建较慢
# 构建上下文为仓库根（见 docker/docker-compose-test.yml 的 app-test-built.build.context）

# 构建阶段与 CI 部署保持一致（.github/workflows/deploy.yml）
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /repo
# 仓库根 .dockerignore 已排除 target、.git 等无关内容
COPY . .
# 产出瘦包布局 target/app（bin + lib + config），与 ./mvnw package 默认产物一致
RUN ./mvnw -B -pl :continew-server -am package -DskipTests

# 运行时镜像与生产一致（docker/continew-admin/Dockerfile），测试结论对生产运行时成立
FROM bellsoft/liberica-openjdk-debian:17.0.14 AS runtime
WORKDIR /app
COPY --from=build /repo/continew-server/target/app/ /app/
EXPOSE 18020
# JAVA_OPTS 经 compose environment 注入，由 sh 展开；启动命令与默认挂载模式一致
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/bin/*.jar"]
