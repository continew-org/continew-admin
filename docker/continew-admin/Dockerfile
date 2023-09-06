FROM java:8

MAINTAINER Charles7c charles7c@126.com

ARG JAR_FILE=./bin/*.jar
COPY ${JAR_FILE} /app/bin/app.jar
WORKDIR /app

ENTRYPOINT ["java", \
            "-jar", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-Dspring.profiles.active=prod", \
            "./bin/app.jar"]