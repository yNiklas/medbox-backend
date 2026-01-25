# Build stage
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
COPY firebase.json /app
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
COPY firebase.json .
ENV TZ=Europe/Berlin
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
