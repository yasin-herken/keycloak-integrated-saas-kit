FROM eclipse-temurin:25-jdk-alpine AS builder

WORKDIR /app

# Copy Maven settings
COPY .mvn .mvn
COPY backend/pom.xml backend/pom.xml
COPY backend/archcore-core/pom.xml backend/archcore-core/pom.xml
COPY backend/archcore-security/pom.xml backend/archcore-security/pom.xml
COPY backend/archcore-app/pom.xml backend/archcore-app/pom.xml

# Download dependencies (cache layer)
RUN cd backend && mvn dependency:go-offline -s ../.mvn/settings.xml

# Copy source code
COPY backend backend

# Build
RUN cd backend && mvn clean package -DskipTests -s ../.mvn/settings.xml

# Runtime stage
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

# Create non-root user
RUN addgroup -S archcore && adduser -S archcore -G archcore

# Copy built jar
COPY --from=builder /app/backend/archcore-app/target/*.jar app.jar

# Create directory for secrets (will be mounted as volume)
RUN mkdir -p /etc/secrets && chown -R archcore:archcore /etc/secrets

USER archcore

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]