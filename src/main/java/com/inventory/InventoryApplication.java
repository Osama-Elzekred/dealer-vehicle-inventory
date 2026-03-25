package com.inventory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class InventoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryApplication.class, args);
    }
}

@Component
class StartupLogger {
    private static final Logger logger = LoggerFactory.getLogger(StartupLogger.class);

    private final Environment environment;

    public StartupLogger(Environment environment) {
        this.environment = environment;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void logApplicationStartup() {
        String port = environment.getProperty("server.port", "8080");
        String basePath = "http://localhost:" + port;

        logger.info("\n" +
                "========================================================================\n" +
                "                                                                        \n" +
                "  >> Dealer & Vehicle Inventory Application                            \n" +
                "  >> Application started successfully!                                  \n" +
                "                                                                        \n" +
                "========================================================================\n" +
                "                                                                        \n" +
                "  [1] Swagger UI Documentation:                                        \n" +
                "      " + basePath + "/swagger-ui.html                                  \n" +
                "                                                                        \n" +
                "  [2] OpenAPI JSON Docs:                                               \n" +
                "      " + basePath + "/api-docs                                         \n" +
                "                                                                        \n" +
                "  [3] H2 Database Console:                                             \n" +
                "      " + basePath + "/h2-console                                       \n" +
                "      (JDBC URL: jdbc:h2:~/test)                                         \n" +
                "                                                                        \n" +
                "========================================================================\n" +
                "  >> Server running on port: " + port + " | Ready to accept requests    \n" +
                "========================================================================\n");
    }
}
