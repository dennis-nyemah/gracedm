# Build stage - uses available Maven image
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests && \
    wget -q https://repo1.maven.org/maven2/com/heroku/webapp-runner/10.1.36.0/webapp-runner-10.1.36.0.jar -O webapp-runner.jar

# Runtime stage
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY --from=builder /app/target/gracedm.war ./app.war
COPY --from=builder /app/webapp-runner.jar .
CMD ["sh", "-c", "java $JAVA_OPTS -jar webapp-runner.jar --port $PORT app.war"]