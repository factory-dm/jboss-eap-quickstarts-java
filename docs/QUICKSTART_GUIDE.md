# JBoss EAP Quickstarts – Comprehensive Guide

Welcome to the **JBoss EAP Quickstarts** repository!  
These quickstarts are **self-contained, runnable examples** that demonstrate how to implement common enterprise patterns and Jakarta EE features on Red Hat JBoss Enterprise Application Platform (EAP). Use them to:

* Learn API usage by example  
* Jump-start new projects with proven configurations  
* Compare alternative approaches to the same problem  
* Troubleshoot environment or configuration issues

---

## Quickstart Comparison Matrix

| Quickstart | Purpose / Feature Highlight | Complexity | Primary Technologies |
|------------|----------------------------|------------|----------------------|
| **helloworld** | Simplest servlet returning “Hello World” | Basic | Servlet, CDI |
| **helloworld-ws** | SOAP web-service echoing messages | Basic | JAX-WS, CDI |
| **websocket-hello** | Hello World over WebSocket channel | Basic | WebSocket API |
| **jaxrs-client** | Shows calling a REST endpoint from a JAX-RS client | Basic | JAX-RS client, JSON-B |
| **temperature-converter** | JSF form with CDI backing bean converting °C/°F | Basic | JSF, CDI, Bean Validation |
| **numberguess** | JSF game demonstrating CDI & Bean Validation scope | Basic | JSF, CDI, BV |
| **kitchensink** | Full-stack CRUD webapp (JPA, JSF, REST) | Intermediate | JSF, JPA, JAX-RS, CDI |
| **cmt** | Container-managed transactions with EJBs | Intermediate | EJB, JPA, JTA |
| **ee-security** | Elytron programmatic security in Jakarta EE app | Intermediate | Elytron, Servlet Security, CDI |
| **servlet-security** | Declarative security in web.xml / annotations | Intermediate | Servlet Security, Elytron |
| **todo-backend** | REST + JPA backend for a SPA Todo app | Intermediate | JAX-RS, JPA, CDI, OpenAPI |
| **thread-racing** | Demonstrates concurrency utilities & thread safety | Advanced | Concurrency Utilities, CDI |
| **remote-helloworld-mdb** | JMS Message-Driven Bean consuming from remote broker | Advanced | JMS, MDB, ActiveMQ Artemis |

Legend:  
_Basic_ – few classes, minimal configuration  
_Intermediate_ – adds persistence, security, or transactions  
_Advanced_ – distributed messaging, concurrency, or remote resources

---

## Getting Started

### Prerequisites

* **Java 17** (or version required by the quickstart branch)  
* **Maven 3.9+**  
* **JBoss EAP 8** (ZIP installation or container image)  
* Optional: Docker/Podman & OpenShift client for container-based runs  
* Internet access to resolve Maven dependencies

### Initial Setup

1. **Clone the repo**

   ```bash
   git clone https://github.com/factory-dm/jboss-eap-quickstarts-java.git
   cd jboss-eap-quickstarts-java
   ```

2. **Install / start JBoss EAP**

   ```bash
   unzip jboss-eap-8*.zip -d $HOME
   $HOME/jboss-eap-8*/bin/standalone.sh
   ```

3. **Build a quickstart**

   ```bash
   cd helloworld        # or any folder listed above
   mvn clean package
   ```

4. **Deploy**

   *CLI:*  
   ```bash
   mvn wildfly:deploy
   ```
   *or* copy the generated `target/*.war` into `$JBOSS_HOME/standalone/deployments/`.

5. **Verify** – Open the URL printed in the console, usually  
   `http://localhost:8080/<context-path>`.

> Tip: Many quickstarts include `scripts/` or `.cli` files to auto-configure datasources, JMS, or Elytron realms—consult each README.

---

## Choosing the Right Quickstart

| If you need… | Start with… | Rationale |
|--------------|-------------|-----------|
| A minimal servlet and deployment structure | `helloworld` | Fastest path to validate environment |
| REST endpoints & client calls | `jaxrs-client` (client) and `kitchensink` (server) | Shows both sides of JAX-RS |
| Form-based JSF UI + CDI | `numberguess` or `temperature-converter` | Demonstrates stateful views & validation |
| Secure endpoints (role-based) | `ee-security` or `servlet-security` | Covers Elytron and servlet annotations |
| Transactions & persistence | `cmt` or `kitchensink` | Uses EJB, JTA, and JPA |
| Messaging with JMS | `remote-helloworld-mdb` | Consumes messages from a remote broker |
| Concurrency examples | `thread-racing` | Illustrates managed threads & race conditions |
| Reference full-stack app blueprint | `kitchensink` | Combines REST, JSF, JPA, security in one |

---

## Troubleshooting Common Issues

| Symptom | Possible Cause | Resolution |
|---------|----------------|------------|
| `java.lang.UnsupportedClassVersionError` | Incompatible JDK level | Compile & run with the required Java version (see README) |
| Maven fails to resolve `jboss-eap-bom` | Missing Red Hat Maven repo | Run `mvn -s $JBOSS_HOME/maven-settings.xml ...` or add repository per README |
| Deployment hangs / fails | EAP not running or wrong port | Ensure `standalone.sh` is active on 8080, adjust `maven.plugin.wildfly.port` |
| `WFLYSEC0042: Authentication failed` | Elytron realm not configured | Execute the provided `configure-*.cli` before deploying |
| `org.h2.jdbc.JdbcSQLNonTransientConnectionException` | Datasource not bound | Verify datasource `jndi-name` matches `persistence.xml` and CLI scripts executed |
| Messages not consumed in MDB example | Remote broker unreachable | Check broker host/port & credentials in `env.sh` or `application.properties` |
| Port already in use | Another server running | Stop conflicting service or start EAP on alternate port (`-Djboss.socket.binding.port-offset=100`) |

---

### Additional Resources
* Individual quickstart **README.adoc** files for step-by-step instructions  
* [JBoss EAP Documentation](https://access.redhat.com/documentation/en-us/red_hat_jboss_enterprise_application_platform/)  
* [Jakarta EE Specifications](https://jakarta.ee/specifications/)  

Happy coding!
