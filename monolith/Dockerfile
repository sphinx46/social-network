FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml ./pom.xml
COPY monolith/pom.xml ./monolith/pom.xml
COPY monolith/src ./monolith/src
WORKDIR /app
RUN mvn -q -e -U -DskipTests dependency:go-offline -pl monolith -am || true
WORKDIR /app/monolith
RUN mvn -q -DskipTests clean package

FROM eclipse-temurin:21-jre
WORKDIR /app

RUN mkdir -p /var/log/social-network

COPY --from=build /app/monolith/target/*.jar app.jar
ENV JAVA_OPTS="-Xms256m -Xmx512m"
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

