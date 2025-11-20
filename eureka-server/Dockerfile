FROM eclipse-temurin:21-jre-alpine
LABEL maintainer="sphinx46"
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "/app/app.jar"]