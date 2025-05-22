package com.factory.kitchensink.service;

import com.factory.kitchensink.model.Member;
import com.factory.kitchensink.repository.MemberRepository;
import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for Member operations.
 * This class replaces the original MemberRegistration EJB from the JBoss EAP application.
 * It provides transaction management and business logic for member operations.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Register a new member.
     * This method validates that the email is unique, persists the member,
     * and publishes an event that the member has been created.
     *
     * @param member the member to register
     * @return the registered member with its generated ID
     * @throws EntityExistsException if a member with the same email already exists
     */
    @Transactional
    public Member register(Member member) {
        log.info("Registering {}", member.getName());
        
        // Check if member with this email already exists
        if (memberRepository.findByEmail(member.getEmail()).isPresent()) {
            throw new EntityExistsException("Email already exists: " + member.getEmail());
        }
        
        // Save the member
        Member savedMember = memberRepository.save(member);
        
        // Publish an event that a new member has been registered
        eventPublisher.publishEvent(savedMember);
        
        return savedMember;
    }

    /**
     * Find all members ordered by name.
     *
     * @return a list of all members ordered by name
     */
    @Transactional(readOnly = true)
    public List<Member> findAllOrderedByName() {
        return memberRepository.findAllOrderedByName();
    }

    /**
     * Find a member by ID.
     *
     * @param id the ID of the member to find
     * @return an Optional containing the member if found, or empty if not found
     */
    @Transactional(readOnly = true)
    public Optional<Member> findById(Long id) {
        return memberRepository.findById(id);
    }

    /**
     * Find a member by email.
     *
     * @param email the email of the member to find
     * @return an Optional containing the member if found, or empty if not found
     */
    @Transactional(readOnly = true)
    public Optional<Member> findByEmail(String email) {
        return memberRepository.findByEmail(email);
    }
}
