# Build stage - context is the repo root
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# Copy entire repo structure to maintain relative paths
COPY backend/gradle ./backend/gradle
COPY backend/gradlew ./backend/gradlew
COPY backend/build.gradle.kts ./backend/build.gradle.kts
COPY backend/settings.gradle.kts ./backend/settings.gradle.kts
COPY openapi/spec.yaml ./openapi/spec.yaml
COPY backend/src ./backend/src

# Build from the backend directory
WORKDIR /workspace/backend
RUN ./gradlew bootJar --no-daemon -x testComponent -x test \
    -Dorg.gradle.jvmargs="-Xmx512m"

# Production stage
FROM eclipse-temurin:21-jre-alpine AS prod
WORKDIR /app

# Add a non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy the built JAR from the build stage
COPY --from=build /workspace/backend/build/libs/*.jar app.jar

# Set ownership
RUN chown appuser:appgroup app.jar

USER appuser

# Expose port
EXPOSE 8080

# Health check via actuator
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
