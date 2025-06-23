FROM eclipse-temurin:17-jre-alpine

# Set up non-root user and group
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Set working directory
WORKDIR /app

# Copy the JAR file (assuming it's built with the name kitchensink-spring.jar)
COPY target/kitchensink-spring-*.jar app.jar

# Set ownership for security
RUN chown -R appuser:appgroup /app

# Expose the application port
EXPOSE 8080

# Switch to non-root user
USER appuser

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
