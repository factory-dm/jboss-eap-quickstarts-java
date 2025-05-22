package com.factory.kitchensink;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Basic test class to verify that the Spring application context loads successfully.
 * This is a standard test in Spring Boot applications to ensure that all beans
 * are properly configured and the application can start.
 */
@SpringBootTest
class KitchensinkApplicationTests {

    @Test
    void contextLoads() {
        // This test will fail if the application context cannot start
        // No assertions needed - the test passes if the context loads successfully
    }
}
