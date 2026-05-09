package com.electrahub.subscription.api.error;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
public class BadRequestException extends RuntimeException {
    private static final Logger LOGGER = LoggerFactory.getLogger(BadRequestException.class);


    /**
     * Executes bad request exception for `BadRequestException`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.api.error`.
     * @param message input consumed by BadRequestException.
     */
    public BadRequestException(String message) {
        super(message);
        LOGGER.info(" Entering BadRequestException#BadRequestException");
        LOGGER.debug(" Entering BadRequestException#BadRequestException with debug context");
    }
}
