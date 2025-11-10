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
FROM debian:12-slim
ENV JAVA_HOME=/opt/java/openjdk
COPY --from=eclipse-temurin:21 $JAVA_HOME $JAVA_HOME
ENV PATH="${JAVA_HOME}/bin:${PATH}"

WORKDIR /app

# Install build tools including g++ and LaTeX
RUN apt-get update && apt-get install -y \
    g++ \
    gcc \
    make \
    texlive-latex-base \
    texlive-latex-extra \
    texlive-fonts-recommended \
    texlive-fonts-extra \
    && rm -rf /var/lib/apt/lists/*

# Create non-root user for security
RUN groupadd -r spring && useradd -r -g spring spring
USER spring

COPY --from=builder /app/build/libs/*.jar app.jar
COPY src/main/resources/application.properties ./config/application.properties

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
