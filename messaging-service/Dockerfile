FROM eclipse-temurin:21-jdk-jammy
LABEL maintainer="sphinx46"
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

