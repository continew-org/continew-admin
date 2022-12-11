FROM java:8

MAINTAINER Charles7c charles7c@126.com

ARG JAR_FILE=./server/*.jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", \
            "-jar", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "app.jar"]