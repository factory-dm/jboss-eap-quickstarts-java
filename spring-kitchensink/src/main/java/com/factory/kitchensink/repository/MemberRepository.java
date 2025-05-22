package com.factory.kitchensink.repository;

import com.factory.kitchensink.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Member entities.
 * This interface leverages Spring Data JPA to provide CRUD operations and custom queries.
 * It replaces the original JPA-based repository in the JBoss EAP application.
 */
@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    
    /**
     * Find a member by email address.
     * 
     * @param email the email to search for
     * @return an Optional containing the member if found, or empty if not found
     */
    Optional<Member> findByEmail(String email);
    
    /**
     * Find all members ordered by name.
     * 
     * @return a list of all members ordered by name
     */
    @Query("SELECT m FROM Member m ORDER BY m.name ASC")
    List<Member> findAllOrderedByName();
}
