####
## Multi-stage Dockerfile for Quarkus Auth Service
####

## Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Copy Maven wrapper and pom.xml for dependency caching
COPY auth-service/mvnw .
COPY auth-service/.mvn .mvn
COPY auth-service/pom.xml .

# Download dependencies (this layer will be cached)
RUN mvn dependency:go-offline -B

# Copy source code
COPY auth-service/src ./src

# Build the application (package as uber-jar for easier deployment)
RUN mvn clean package -DskipTests -Dquarkus.package.type=uber-jar

## Stage 2: Create runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install curl for health checks
RUN apk add --no-cache curl

# Create non-root user for security
RUN addgroup -S quarkus && adduser -S quarkus -G quarkus

# Copy the uber-jar from build stage
COPY --from=build /build/target/*-runner.jar /app/app.jar

# Change ownership to non-root user
RUN chown -R quarkus:quarkus /app

# Switch to non-root user
USER quarkus

# Expose the application port
EXPOSE 8080

# Health check endpoint
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/q/health/live || exit 1

# Set Java options for container
ENV JAVA_OPTS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]