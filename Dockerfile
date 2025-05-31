# Build stage with exact Maven version
FROM maven:3.9.9-openjdk-17 AS builder
WORKDIR /app
COPY pom.xml .
# Cache dependencies
RUN mvn dependency:go-offline
# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests && \
    wget -q https://repo1.maven.org/maven2/com/heroku/webapp-runner/10.1.36.0/webapp-runner-10.1.36.0.jar -O webapp-runner.jar

# Runtime stage
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=builder /app/target/gracedm.war ./app.war
COPY --from=builder /app/webapp-runner.jar .
CMD ["sh", "-c", "java $JAVA_OPTS -jar webapp-runner.jar --port $PORT app.war"]