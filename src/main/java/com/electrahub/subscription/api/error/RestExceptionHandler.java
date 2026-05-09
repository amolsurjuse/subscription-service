package com.electrahub.subscription.api.error;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;

@RestControllerAdvice
public class RestExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestExceptionHandler.class);


    /**
     * Processes handle not found for `RestExceptionHandler`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.api.error`.
     * @param exception input consumed by handleNotFound.
     * @param request input consumed by handleNotFound.
     * @return result produced by handleNotFound.
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NotFoundException exception, HttpServletRequest request) {
        LOGGER.info(" Entering RestExceptionHandler#handleNotFound");
        LOGGER.debug(" Entering RestExceptionHandler#handleNotFound with debug context");
        return build(HttpStatus.NOT_FOUND, exception.getMessage(), request.getRequestURI());
    }

    /**
     * Processes handle conflict for `RestExceptionHandler`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.api.error`.
     * @param exception input consumed by handleConflict.
     * @param request input consumed by handleConflict.
     * @return result produced by handleConflict.
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflict(ConflictException exception, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler({
            BadRequestException.class,
            IllegalArgumentException.class,
            IllegalStateException.class,
            ConstraintViolationException.class,
            MethodArgumentNotValidException.class
    })
    /**
     * Processes handle bad request for `RestExceptionHandler`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.api.error`.
     * @param exception input consumed by handleBadRequest.
     * @param request input consumed by handleBadRequest.
     * @return result produced by handleBadRequest.
     */
    public ResponseEntity<ApiError> handleBadRequest(Exception exception, HttpServletRequest request) {
        String message = exception instanceof MethodArgumentNotValidException invalidException
                ? invalidException.getBindingResult().getAllErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Validation failed")
                : exception.getMessage();
        return build(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
    }

    /**
     * Processes handle generic for `RestExceptionHandler`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.api.error`.
     * @param exception input consumed by handleGeneric.
     * @param request input consumed by handleGeneric.
     * @return result produced by handleGeneric.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception exception, HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", request.getRequestURI());
    }

    /**
     * Creates build for `RestExceptionHandler`.
     *
     * <p>Detailed behavior: follows the current implementation path and
     * enforces component-specific rules in `com.electrahub.subscription.api.error`.
     * @param status input consumed by build.
     * @param message input consumed by build.
     * @param path input consumed by build.
     * @return result produced by build.
     */
    private ResponseEntity<ApiError> build(HttpStatus status, String message, String path) {
        return ResponseEntity.status(status).body(new ApiError(
                OffsetDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path
        ));
    }
}
