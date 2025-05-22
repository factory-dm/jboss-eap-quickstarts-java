package com.example.kitchensink.service;

import com.example.kitchensink.model.Member;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for Member entity operations.
 * This interface defines the business operations that can be performed on Member entities.
 * It replaces the functionality provided by MemberRegistration in the original JBoss application
 * and adds additional methods for retrieving members.
 */
public interface MemberService {
    
    /**
     * Registers a new member.
     * 
     * @param member The member to register
     * @throws Exception If registration fails (e.g., due to validation or duplicate email)
     */
    void register(Member member) throws Exception;
    
    /**
     * Finds a member by their ID.
     * 
     * @param id The ID of the member to find
     * @return An Optional containing the member if found, or empty if not found
     */
    Optional<Member> findById(Long id);
    
    /**
     * Finds a member by their email address.
     * 
     * @param email The email address to search for
     * @return An Optional containing the member if found, or empty if not found
     */
    Optional<Member> findByEmail(String email);
    
    /**
     * Retrieves all members ordered by name.
     * 
     * @return A list of all members sorted by name in ascending order
     */
    List<Member> findAllOrderedByName();
    
    /**
     * Checks if a member with the given email already exists.
     * 
     * @param email The email to check
     * @return true if a member with the email exists, false otherwise
     */
    boolean emailAlreadyExists(String email);
}
