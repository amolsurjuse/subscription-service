package com.electrahub.subscription;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SubscriptionServiceApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionServiceApplication.class);


    /**
     * Executes main for `SubscriptionServiceApplication`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription`.
     * @param args input consumed by main.
     */
    public static void main(String[] args) {
        LOGGER.info(" Entering SubscriptionServiceApplication#main");
        LOGGER.debug(" Entering SubscriptionServiceApplication#main with debug context");
        SpringApplication.run(SubscriptionServiceApplication.class, args);
    }
}
