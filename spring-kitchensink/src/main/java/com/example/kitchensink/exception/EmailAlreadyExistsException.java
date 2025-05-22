package com.example.kitchensink.exception;

/**
 * Exception thrown when attempting to register a member with an email address that already exists.
 * This custom exception replaces the generic ValidationException used in the original application
 * and provides more specific error handling for email uniqueness violations.
 */
public class EmailAlreadyExistsException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    private final String email;

    /**
     * Constructs a new EmailAlreadyExistsException with the specified email.
     *
     * @param email the email address that already exists in the system
     */
    public EmailAlreadyExistsException(String email) {
        super("Email already exists: " + email);
        this.email = email;
    }

    /**
     * Constructs a new EmailAlreadyExistsException with the specified email and cause.
     *
     * @param email the email address that already exists in the system
     * @param cause the cause of the exception
     */
    public EmailAlreadyExistsException(String email, Throwable cause) {
        super("Email already exists: " + email, cause);
        this.email = email;
    }

    /**
     * Gets the email address that caused this exception.
     *
     * @return the email address that already exists
     */
    public String getEmail() {
        return email;
    }
}
