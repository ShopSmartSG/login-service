# Stage 1: Build the application using Maven
FROM maven:3.9.7-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Set up the runtime environment
# Use an official OpenJDK runtime as a parent image
FROM openjdk:21-jdk-slim

# Create the log directory and set proper permissions
RUN mkdir -p /var/log/login-service && \
    chmod -R 777 /var/log/login-service  # Ensure the app can write to the log directory

# Copy the project’s jar file into the container at /app
COPY --from=build /app/target/login-service.jar login-app.jar

# Make port 8080 available to the world outside this container
EXPOSE 8080

# Run the jar file
ENTRYPOINT ["java", "-jar", "login-app.jar"]

# Docker commands for reference:
# docker build -t login-service:latest .
# docker-compose up --build
# docker push your-dockerhub-username/login-service:latest
# docker-compose down : shutdown the container
# Ensure docker-compose and Dockerfile ports match
