FROM openjdk:17-jdk-slim

WORKDIR /app

COPY ./target/erp-api-2.12.0.jar erp-api.jar

CMD ["java", "-Duser.timezone=Asia/Shanghai", "-jar", "erp-api.jar"]