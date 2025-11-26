FROM eclipse-temurin:21-jdk-jammy
LABEL maintainer="sphinx46"
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

