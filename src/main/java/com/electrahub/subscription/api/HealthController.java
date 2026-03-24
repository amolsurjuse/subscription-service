package com.electrahub.subscription.api;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {
    private static final Logger LOGGER = LoggerFactory.getLogger(HealthController.class);


    /**
     * Executes health for `HealthController`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.api`.
     * @return result produced by health.
     */
    @GetMapping
    public Map<String, String> health() {
        LOGGER.info("CODEx_ENTRY_LOG: Entering HealthController#health");
        LOGGER.debug("CODEx_ENTRY_LOG: Entering HealthController#health with debug context");
        return Map.of("status", "UP");
    }
}
