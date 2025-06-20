 # Stage 1: Build the application# Stage 1: Build the application
FROM maven:3.8.7-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests

# Stage 2: Run the application
FROM tomcat:10.1.18-jdk17-temurin-jammy
ENV CATALINA_OPTS="-Xmx512m -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# Remove default Tomcat apps
RUN rm -rf /usr/local/tomcat/webapps/ROOT && \
    rm -rf /usr/local/tomcat/webapps/docs && \
    rm -rf /usr/local/tomcat/webapps/examples && \
    rm -rf /usr/local/tomcat/webapps/manager && \
    rm -rf /usr/local/tomcat/webapps/host-manager

# Copy WAR file from build stage
COPY --from=build /app/target/gracedm.war /usr/local/tomcat/webapps/ROOT.war

# Health check
HEALTHCHECK --interval=30s --timeout=5s \
  CMD curl -f http://localhost:8080/healthz || exit 1

EXPOSE 8080
CMD ["catalina.sh", "run"]