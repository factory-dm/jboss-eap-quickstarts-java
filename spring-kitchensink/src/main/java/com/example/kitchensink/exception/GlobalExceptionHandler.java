package com.example.kitchensink.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Global exception handler for the application.
 * This class handles exceptions thrown by controllers and converts them to appropriate HTTP responses.
 * It replaces the exception handling logic in the original MemberResourceRESTService class.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = Logger.getLogger(GlobalExceptionHandler.class.getName());

    /**
     * Handles validation exceptions thrown by @Valid annotations on method parameters.
     * 
     * @param ex The MethodArgumentNotValidException thrown when validation fails
     * @return A ResponseEntity containing field errors and BAD_REQUEST status
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        log.fine("Validation completed. violations found: " + errors.size());
        return ResponseEntity.badRequest().body(errors);
    }

    /**
     * Handles constraint violation exceptions thrown by bean validation.
     * 
     * @param ex The ConstraintViolationException thrown when validation fails
     * @return A ResponseEntity containing field errors and BAD_REQUEST status
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, String>> handleConstraintViolationException(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            String propertyPath = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            errors.put(propertyPath, message);
        }
        
        log.fine("Validation completed. violations found: " + errors.size());
        return ResponseEntity.badRequest().body(errors);
    }

    /**
     * Handles exceptions thrown when attempting to register a member with an email that already exists.
     * 
     * @param ex The EmailAlreadyExistsException
     * @return A ResponseEntity with CONFLICT status and an error message
     */
    @ExceptionHandler(EmailAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<Map<String, String>> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex) {
        Map<String, String> responseObj = new HashMap<>();
        responseObj.put("email", "Email taken");
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(responseObj);
    }

    /**
     * Handles all other exceptions not specifically handled by other methods.
     * 
     * @param ex The exception that was thrown
     * @return A ResponseEntity with BAD_REQUEST status and an error message
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        Map<String, String> responseObj = new HashMap<>();
        responseObj.put("error", ex.getMessage());
        
        return ResponseEntity.badRequest().body(responseObj);
    }
}
