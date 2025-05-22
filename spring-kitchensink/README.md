# Spring Kitchensink Application

This application is a Spring Boot migration of the JBoss EAP "kitchensink" quickstart. It demonstrates a Spring-based web application with REST APIs, JPA persistence, and validation.

## Overview

The Spring Kitchensink application provides a simple member registration system where you can:
- Register new members with name, email, and phone number
- View all registered members
- Look up members by ID

This application demonstrates key Spring Boot features and best practices, including:
- Spring Data JPA for data persistence
- Spring MVC for REST endpoints
- Bean validation
- Exception handling
- Transaction management
- Application events

## Migration from JBoss EAP

This application was migrated from the JBoss EAP kitchensink quickstart, which originally demonstrated Jakarta EE technologies. The migration involved:

1. **Domain Model**: 
   - Converted JPA entity to use Spring Data JPA
   - Retained Jakarta validation annotations
   - Added Lombok for reduced boilerplate

2. **Data Access Layer**:
   - Replaced manual JPA repository with Spring Data JPA interface
   - Simplified queries using Spring Data method naming conventions and annotations

3. **Business Logic**:
   - Converted EJB stateless session bean to Spring Service
   - Replaced CDI dependency injection with Spring's DI
   - Implemented transaction management using Spring's @Transactional

4. **REST API**:
   - Migrated JAX-RS endpoints to Spring MVC REST controllers
   - Updated path mappings and response handling

5. **Application Properties**:
   - Replaced persistence.xml and datasource XML with Spring application.properties
   - Configured H2 database with similar settings to the original application

6. **Exception Handling**:
   - Implemented global exception handling using @ControllerAdvice
   - Standardized error responses

7. **Bean Validation**:
   - Retained Jakarta Bean Validation annotations
   - Integrated with Spring's validation framework

## Prerequisites

- Java 17 or later
- Maven 3.6.0 or later

## Building and Running the Application

### Building with Maven

```bash
mvn clean package
```

### Running the Application

```bash
mvn spring-boot:run
```

Or after building:

```bash
java -jar target/spring-kitchensink-0.0.1-SNAPSHOT.jar
```

The application will be accessible at: http://localhost:8080/spring-kitchensink/

### Accessing the H2 Console

The H2 database console is available at: http://localhost:8080/spring-kitchensink/h2-console

Connection details:
- JDBC URL: `jdbc:h2:mem:kitchensink`
- Username: `sa`
- Password: `sa`

## API Endpoints

- `GET /api/members` - List all members
- `GET /api/members/{id}` - Get a member by ID
- `POST /api/members` - Create a new member

Example POST request body:
```json
{
  "name": "Jane Doe",
  "email": "jane.doe@example.com",
  "phoneNumber": "1234567890"
}
```

## Technologies Used

- Spring Boot 3.1.5
- Spring Data JPA
- Spring Web MVC
- H2 Database
- Jakarta Bean Validation
- Lombok
- Maven
