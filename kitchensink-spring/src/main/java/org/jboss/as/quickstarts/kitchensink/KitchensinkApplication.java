package org.jboss.as.quickstarts.kitchensink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot application class for the Kitchensink application.
 * The @SpringBootApplication annotation enables auto-configuration and component scanning.
 * By placing this class at the root package, it will scan all sub-packages automatically:
 * - org.jboss.as.quickstarts.kitchensink.model
 * - org.jboss.as.quickstarts.kitchensink.data
 * - org.jboss.as.quickstarts.kitchensink.service
 * - org.jboss.as.quickstarts.kitchensink.rest
 */
@SpringBootApplication
public class KitchensinkApplication {

    public static void main(String[] args) {
        SpringApplication.run(KitchensinkApplication.class, args);
    }
}
