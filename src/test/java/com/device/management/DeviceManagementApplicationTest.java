package com.device.management;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect"
})
public class DeviceManagementApplicationTest {

    @Test
    void contextLoads() {
        // This test will pass if the Spring context loads successfully
    }
}
