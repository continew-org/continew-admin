# bellsoft/liberica-openjdk-alpine:17.0.14 仅包含必要运行时，体积小（约 128MB），适用于对镜像大小敏感的场景
# bellsoft/liberica-openjdk-debian:17.0.14 为全量 JDK 版本，包含更多系统工具与依赖，兼容性更好
# 若对镜像大小有严格要求，可将当前镜像替换为 alpine 版本
FROM bellsoft/liberica-openjdk-debian:17.0.14

ARG JAR_FILE=./bin/*.jar
COPY ${JAR_FILE} /app/bin/app.jar
WORKDIR /app/bin
# 确保 logs 目录存在，供 OOM 时生成堆转储（docker-compose 已将其挂载到宿主 /docker/continew-admin/logs/）
RUN mkdir -p /app/logs

# JVM 参数说明：
# -XX:MetaspaceSize / -XX:MaxMetaspaceSize：限制类元数据（Metaspace）上限，避免缓慢泄漏时无上限增长直至 OOM
# -XX:+HeapDumpOnOutOfMemoryError / -XX:HeapDumpPath：OOM 时自动生成堆转储，便于定位泄漏源（如行为验证码/动态类生成）
# -XX:ErrorFile：记录致命错误日志
ENTRYPOINT ["java", \
            "-XX:+UseZGC", \
            "-XX:MetaspaceSize=128m", \
            "-XX:MaxMetaspaceSize=256m", \
            "-XX:+HeapDumpOnOutOfMemoryError", \
            "-XX:HeapDumpPath=/app/logs/oom.hprof", \
            "-XX:ErrorFile=/app/logs/hs_err_pid%p.log", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-jar", \
            "app.jar"]