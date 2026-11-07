
# ==========================================
# Stage 1: Build application
# ==========================================
FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /backend

# Copy dependency descriptor first for Docker caching
COPY pom.xml .

# Download dependencies
RUN mvn -B dependency:go-offline

# Copy application source
COPY src ./src

# Build Spring Boot application
RUN mvn -B clean package -DskipTests


# ==========================================
# Stage 2: Production runtime
# ==========================================
FROM eclipse-temurin:17-jre-alpine-3.24

WORKDIR /backend

# Update Alpine security packages
RUN apk upgrade --no-cache \
    && addgroup -S appgroup \
    && adduser -S appuser -G appgroup

# Copy executable JAR
COPY --from=build --chown=appuser:appgroup \
    /backend/target/*.jar ./app.jar

# Run without root privileges
USER appuser

EXPOSE 8000

ENTRYPOINT ["java", "-jar", "app.jar"]
