# Spring Boot Kitchensink Application

This project is a Spring Boot migration of the JBoss EAP Kitchensink quickstart application. It demonstrates how to build a complete web application using Spring Boot, Spring Data JPA, Spring MVC, and Bean Validation.

## Application Overview

The Spring Kitchensink application is a member registration system that allows users to:
- Register new members with validation
- View a list of registered members
- Access member information via a RESTful API

This application serves as a reference implementation for migrating Jakarta EE applications to Spring Boot, preserving the core functionality while adopting Spring Boot conventions and best practices.

## Architecture

### Application Structure

```
spring-kitchensink/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/kitchensink/
│   │   │       ├── controller/       # REST controllers
│   │   │       ├── exception/        # Exception handling
│   │   │       ├── model/            # Domain entities
│   │   │       ├── repository/       # Data access layer
│   │   │       ├── service/          # Business logic
│   │   │       └── SpringKitchensinkApplication.java
│   │   └── resources/
│   │       └── application.properties # Application configuration
│   └── test/
│       └── java/
│           └── com/example/kitchensink/
│               ├── controller/       # Controller tests
│               ├── repository/       # Repository tests
│               ├── service/          # Service tests
│               └── SpringKitchensinkApplicationTests.java
└── pom.xml                           # Maven configuration
```

### Key Components

- **Domain Model**: `Member.java` - JPA entity representing a member
- **Data Access**: `MemberRepository.java` - Spring Data JPA repository
- **Business Logic**: `MemberService.java` and `MemberServiceImpl.java` - Service layer
- **REST API**: `MemberController.java` - Spring MVC REST controller
- **Exception Handling**: `GlobalExceptionHandler.java` - Centralized exception handling
- **Configuration**: `application.properties` - Application and database configuration

## Migration Details

### What Was Migrated

- Domain model (Member entity)
- Data access layer (Repository)
- Business logic (Service)
- REST API (Controller)
- Exception handling
- Bean validation

### Key Changes

| JBoss EAP Component | Spring Boot Equivalent |
|---------------------|------------------------|
| JPA Entity | JPA Entity (minimal changes) |
| CDI @Inject | Spring @Autowired |
| EJB @Stateless | Spring @Service |
| JAX-RS @Path | Spring @RestController, @RequestMapping |
| CDI Events | Spring ApplicationEventPublisher |
| persistence.xml | application.properties |
| Bean Validation | Bean Validation (same annotations) |
| Manual exception handling | @ControllerAdvice, @ExceptionHandler |

### Notable Improvements

- Simplified repository layer using Spring Data JPA
- Centralized exception handling with @ControllerAdvice
- Streamlined configuration with application.properties
- Comprehensive test coverage with Spring Boot Test
- Embedded database for easy development and testing

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- Git (optional, for cloning the repository)

## Building and Running

### Build the Application

```bash
mvn clean package
```

### Run the Application

```bash
mvn spring-boot:run
```

Or run the JAR file directly:

```bash
java -jar target/spring-kitchensink-1.0.0-SNAPSHOT.jar
```

The application will be available at: http://localhost:8080/kitchensink

### API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/members` | GET | Get all members ordered by name |
| `/members/{id}` | GET | Get a specific member by ID |
| `/members` | POST | Create a new member |

## Testing

Run the tests with:

```bash
mvn test
```

The test suite includes:
- Unit tests for services and controllers
- Integration tests for repositories
- Basic application context loading test

## Database Configuration

The application uses an H2 in-memory database by default. The H2 console is enabled and available at: http://localhost:8080/kitchensink/h2-console

Database configuration can be customized in `application.properties`:

```properties
# Database connection
spring.datasource.url=jdbc:h2:mem:kitchensinkdb
spring.datasource.username=sa
spring.datasource.password=

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=create-drop
```

To use a different database (e.g., PostgreSQL, MySQL), update the dependencies in `pom.xml` and modify the database configuration in `application.properties`.

## Additional Resources

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Data JPA Documentation](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [Original JBoss EAP Kitchensink Quickstart](https://github.com/jboss-developer/jboss-eap-quickstarts)
