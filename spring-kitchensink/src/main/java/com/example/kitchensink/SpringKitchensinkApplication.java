package com.example.kitchensink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot application class.
 * This serves as the entry point for the Spring Boot version of the kitchensink application.
 * The @SpringBootApplication annotation enables auto-configuration, component scanning,
 * and additional configuration capabilities.
 */
@SpringBootApplication
public class SpringKitchensinkApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringKitchensinkApplication.class, args);
    }
}
