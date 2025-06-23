# Kitchensink Spring Boot

A Spring Boot 3 rewrite of the classic *kitchensink* Jakarta EE quick-start.  
It demonstrates a minimal but complete CRUD application using:

* Spring Boot 3 / Spring Framework 6  
* Spring Data JPA & Hibernate  
* Bean Validation (Jakarta Validation)  
* H2 in-memory database (dev & test)  
* Lombok to reduce boiler-plate  
* Docker / Buildpacks for containerisation

---

## 1  Prerequisites

| Tool | Version | Purpose |
|------|---------|---------|
| JDK 17+ | Tested with Temurin 17 | Compile & run |
| Maven 3.9+ | Build lifecycle | `mvn …` |
| Docker 20+ | Container build & run | Optional |

---

## 2  Project Layout (abridged)

```
kitchensink-spring
 ├─ src/main/java/org/jboss/as/quickstarts/kitchensink
 │   ├─ KitchensinkApplication.java   # Spring Boot entry-point
 │   ├─ model      # JPA entity
 │   ├─ data       # Spring Data repository + producer
 │   ├─ service    # Business layer
 │   └─ rest       # REST controllers + error handling
 ├─ src/main/resources
 │   ├─ application.yml              # profile-aware config
 │   └─ import.sql                   # seed data
 ├─ src/test/...                     # unit & slice tests
 ├─ Dockerfile
 └─ docker-compose.yml
```

---

## 3  Building the Jar

```bash
cd kitchensink-spring
mvn clean package
```

The runnable fat-jar is produced at  
`target/kitchensink-spring-<version>.jar`.

---

## 4  Running Locally

### 4.1  Via Spring Boot Maven Plugin (hot-reloading)

```bash
mvn spring-boot:run
```

### 4.2  Directly from the Jar

```bash
java -jar target/kitchensink-spring-*.jar \
     --spring.profiles.active=dev
```

The app starts on `http://localhost:8080/kitchensink`.

| Profile | DB | DDL mode | Port |
|---------|----|----------|------|
| `dev` *(default)* | H2 in-mem | `create-drop` | 8080 |
| `test` | H2 in-mem | `create-drop` | random (0) |
| `prod` | H2 file (or external) | `validate` | `${PORT:-8080}` |

The H2 console is enabled in *dev* at  
`http://localhost:8080/kitchensink/h2-console`  
(driver class: `org.h2.Driver`, JDBC URL printed at startup).

---

## 5  REST API Cheat-Sheet

| Verb | Path | Description |
|------|------|-------------|
| GET  | `/members` | List all members |
| GET  | `/members/{id}` | Member by id |
| POST | `/members` | Create member (JSON body) |

Example:

```bash
curl -X POST http://localhost:8080/kitchensink/members \
     -H "Content-Type: application/json" \
     -d '{ "name":"Alice","email":"alice@example.com","phoneNumber":"1234567890" }'
```

An [Actuator] health probe is exposed at  
`/kitchensink/actuator/health` (dev only).

---

## 6  Running the Test-Suite

```bash
mvn test            # unit & slice tests
mvn verify          # plus integration tests (if any)
```

Coverage is maintained in parity with the Jakarta EE original.

---

## 7  Containerisation

### 7.1  Build using Spring Boot Buildpacks

```bash
mvn spring-boot:build-image \
    -Ddocker.image.prefix=kitchensink
```

Resulting image: `kitchensink/kitchensink-spring:1.0.0-SNAPSHOT`

### 7.2  Build with Dockerfile

```bash
docker build -t kitchensink/kitchensink-spring:latest .
```

### 7.3  Run the Container

```bash
docker run --rm -p 8080:8080 \
           -e SPRING_PROFILES_ACTIVE=dev \
           kitchensink/kitchensink-spring:latest
```

### 7.4  docker-compose (recommended for local dev)

```bash
docker-compose up --build
```

Hot-reloading is possible by rebuilding the jar; `docker-compose`
bind-mounts `./target` so the new jar is picked up on restart.

---

## 8  Configuration via Environment Variables

| Variable | Effect | Example |
|----------|--------|---------|
| `SPRING_PROFILES_ACTIVE` | Select profile | `prod` |
| `SERVER_PORT` | Override port | `8090` |
| `DB_PASSWORD` | Prod DB password | `secret` |
| Standard Spring Boot `SPRING_DATASOURCE_*` | External DB |

---

## 9  Next Steps

* Switch `prod` profile to use PostgreSQL/MySQL.
* Enable Actuator security & metrics integration.
* Deploy the container to Kubernetes or any orchestrator.

Happy coding!
