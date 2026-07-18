# Stage 1: Build the JAR with Maven
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Cache dependencies layer separately for faster rebuilds
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Build the application JAR (skip tests for faster builds)
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Minimal runtime image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the built JAR from Stage 1
COPY --from=build /app/target/*.jar app.jar

# Expose the Spring Boot default port
EXPOSE 8080

# JVM tuning flags for containerized environments:
# -XX:+UseContainerSupport       -> Respects Docker memory limits
# -XX:MaxRAMPercentage=75.0      -> Use max 75% of available RAM
# -Djava.security.egd=...        -> Faster startup (Tomcat entropy fix)
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
