package com.factory.kitchensink.exception;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the application.
 * This class handles exceptions thrown from controllers and converts them to appropriate HTTP responses.
 * It replaces the exception handling logic in the original JBoss EAP REST service.
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle bean validation exceptions.
     * This handles violations of constraints defined on the entity classes.
     *
     * @param ex the exception
     * @param request the current request
     * @return a response entity with validation errors
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleConstraintViolation(
            ConstraintViolationException ex, WebRequest request) {
        
        log.info("Validation error: {}", ex.getMessage());
        Map<String, String> errors = new HashMap<>();
        
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            String propertyPath = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            errors.put(propertyPath, message);
        }
        
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle validation exceptions from @Valid annotations on controller methods.
     *
     * @param ex the exception
     * @param request the current request
     * @return a response entity with validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        log.info("Method argument validation error: {}", ex.getMessage());
        Map<String, String> errors = new HashMap<>();
        
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle entity already exists exceptions.
     * This is thrown when trying to create an entity with a unique constraint violation.
     *
     * @param ex the exception
     * @param request the current request
     * @return a response entity with the conflict error
     */
    @ExceptionHandler(EntityExistsException.class)
    public ResponseEntity<Map<String, String>> handleEntityExists(
            EntityExistsException ex, WebRequest request) {
        
        log.info("Entity exists error: {}", ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("email", "Email already exists");
        
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    /**
     * Handle entity not found exceptions.
     * This is thrown when trying to access an entity that does not exist.
     *
     * @param ex the exception
     * @param request the current request
     * @return a response entity with the not found error
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleEntityNotFound(
            EntityNotFoundException ex, WebRequest request) {
        
        log.info("Entity not found error: {}", ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle all other exceptions.
     * This is a catch-all handler for any exceptions not specifically handled above.
     *
     * @param ex the exception
     * @param request the current request
     * @return a response entity with the error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleAllExceptions(
            Exception ex, WebRequest request) {
        
        log.error("Unhandled exception: ", ex);
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
