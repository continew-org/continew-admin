# bellsoft/liberica-openjdk-alpine:17.0.14 仅包含必要运行时，体积小（约 128MB），适用于对镜像大小敏感的场景
# bellsoft/liberica-openjdk-debian:17.0.14 为全量 JDK 版本，包含更多系统工具与依赖，兼容性更好
# 若对镜像大小有严格要求，可将当前镜像替换为 alpine 版本
FROM bellsoft/liberica-openjdk-debian:17.0.14

MAINTAINER Charles7c charles7c@126.com

ARG JAR_FILE=./bin/*.jar
COPY ${JAR_FILE} /app/bin/app.jar
WORKDIR /app/bin

ENTRYPOINT ["java", \
            "-jar", \
            "-XX:+UseZGC", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-Dspring.profiles.active=prod", \
            "app.jar"]