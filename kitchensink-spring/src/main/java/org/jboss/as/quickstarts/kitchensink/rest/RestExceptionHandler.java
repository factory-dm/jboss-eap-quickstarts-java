/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.as.quickstarts.kitchensink.rest;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.NoResultException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import lombok.extern.slf4j.Slf4j;

/**
 * Global exception handler for REST controllers.
 * 
 * This class centralizes exception handling across all controllers,
 * providing consistent error responses for various exception types.
 */
@ControllerAdvice
@Slf4j
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Handle ConstraintViolationException - occurs when @Valid validation fails
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        log.debug("Validation error: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            String propertyPath = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            errors.put(propertyPath, message);
        }
        
        return ResponseEntity.badRequest().body(errors);
    }
    
    /**
     * Handle ValidationException - used for custom validation errors like unique email
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Object> handleValidationException(ValidationException ex, WebRequest request) {
        log.debug("Validation exception: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        
        if (ex.getMessage().contains("Email")) {
            errors.put("email", "Email taken");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errors);
        } else {
            errors.put("error", ex.getMessage());
            return ResponseEntity.badRequest().body(errors);
        }
    }
    
    /**
     * Handle NoResultException and EntityNotFoundException - when an entity is not found
     */
    @ExceptionHandler({NoResultException.class, EntityNotFoundException.class})
    public ResponseEntity<Object> handleEntityNotFound(Exception ex, WebRequest request) {
        log.debug("Entity not found: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        errors.put("error", "Entity not found");
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errors);
    }
    
    /**
     * Handle ResponseStatusException - used by our controllers for custom HTTP status responses
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Object> handleResponseStatusException(ResponseStatusException ex, WebRequest request) {
        log.debug("Response status exception: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        errors.put("error", ex.getReason());
        
        return ResponseEntity.status(ex.getStatusCode()).body(errors);
    }
    
    /**
     * Handle MethodArgumentNotValidException - occurs when @Valid validation fails on a method argument
     */
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        
        log.debug("Method argument validation failed: {}", ex.getMessage());
        
        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                    FieldError::getField, 
                    fieldError -> fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "Invalid value",
                    (existing, replacement) -> existing + ", " + replacement
                ));
        
        return ResponseEntity.badRequest().body(errors);
    }
    
    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllOtherExceptions(Exception ex, WebRequest request) {
        log.error("Unhandled exception occurred", ex);
        
        Map<String, String> errors = new HashMap<>();
        errors.put("error", "An unexpected error occurred");
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errors);
    }
}
