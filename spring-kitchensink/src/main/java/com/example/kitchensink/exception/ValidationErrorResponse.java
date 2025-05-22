package com.example.kitchensink.exception;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a validation error response that will be returned by the REST API
 * when validation fails. This class is similar to the validation response handling 
 * in the original MemberResourceRESTService class.
 * 
 * It contains a map of field names to error messages, which can be serialized to JSON
 * and returned to the client.
 */
public class ValidationErrorResponse {
    
    private final Map<String, String> errors;
    
    /**
     * Creates a new empty validation error response.
     */
    public ValidationErrorResponse() {
        this.errors = new HashMap<>();
    }
    
    /**
     * Creates a new validation error response with the given errors.
     * 
     * @param errors A map of field names to error messages
     */
    public ValidationErrorResponse(Map<String, String> errors) {
        this.errors = new HashMap<>(errors);
    }
    
    /**
     * Adds a validation error for a specific field.
     * 
     * @param field The name of the field that has a validation error
     * @param message The error message
     * @return This ValidationErrorResponse instance for method chaining
     */
    public ValidationErrorResponse addError(String field, String message) {
        errors.put(field, message);
        return this;
    }
    
    /**
     * Gets all validation errors.
     * 
     * @return A map of field names to error messages
     */
    public Map<String, String> getErrors() {
        return errors;
    }
    
    /**
     * Checks if there are any validation errors.
     * 
     * @return true if there are validation errors, false otherwise
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}
