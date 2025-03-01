# Use a lightweight OpenJDK image
FROM openjdk:21-jdk-slim

# Set working directory
WORKDIR /app

# Copy the JAR file from the target directory (make sure it's built first)
COPY target/*.jar app.jar

# Expose the port (Spring Boot default port)
EXPOSE 8080

# Run the Spring Boot application
CMD ["java", "-jar", "app.jar"]
