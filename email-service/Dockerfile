# Use Maven with OpenJDK 17 to build
FROM maven:3.9.6-amazoncorretto-17 AS builder

# Set working directory for the email service
WORKDIR /app

# Copy just this service's pom.xml and source
COPY email-service/pom.xml ./pom.xml
COPY email-service/src ./src

# Build as a standalone module, without relying on parent
RUN mvn package -DskipTests

# Runtime stage
FROM amazoncorretto:17

WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]