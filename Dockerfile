FROM gradle:8.10.2-jdk17 AS builder

WORKDIR /apps

COPY . /apps

RUN gradle clean build --no-daemon --parallel

FROM openjdk:17-jdk-slim

LABEL type="application"

WORKDIR /apps

COPY --from=builder /apps/build/libs/*-SNAPSHOT.jar /apps/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/apps/app.jar"]