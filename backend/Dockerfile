# Build stage
FROM gradle:8.5-jdk21 AS builder
WORKDIR /app

# Build dependencies
COPY build.gradle.kts /app/
RUN gradle dependencies --no-daemon

# Build source
COPY src /app/src
RUN gradle build --no-daemon -x test

# Runtime stage
FROM openjdk:21-jdk-slim
WORKDIR /app

# Create non-root user for security
RUN groupadd -r spring && useradd -r -g spring spring
USER spring

COPY --from=builder /app/build/libs/*.jar app.jar
COPY src/main/resources/application.properties ./config/application.properties

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]