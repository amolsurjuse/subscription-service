package com.electrahub.subscription.config;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebConfig.class);


    private final CorsProperties corsProperties;

    /**
     * Executes web config for `WebConfig`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.config`.
     * @param corsProperties input consumed by WebConfig.
     */
    public WebConfig(CorsProperties corsProperties) {
        LOGGER.info("CODEx_ENTRY_LOG: Entering WebConfig#WebConfig");
        LOGGER.debug("CODEx_ENTRY_LOG: Entering WebConfig#WebConfig with debug context");
        this.corsProperties = corsProperties;
    }

    /**
     * Creates add cors mappings for `WebConfig`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.config`.
     * @param registry input consumed by addCorsMappings.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(corsProperties.getAllowedOriginPatterns().toArray(String[]::new))
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
