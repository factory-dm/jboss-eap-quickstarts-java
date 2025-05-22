package com.example.kitchensink.service;

import com.example.kitchensink.model.Member;
import com.example.kitchensink.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Implementation of the MemberService interface.
 * This service replaces the MemberRegistration EJB from the original JBoss kitchensink application.
 * It uses Spring's transaction management and dependency injection instead of EJB.
 */
@Service
public class MemberServiceImpl implements MemberService {

    private final Logger log = Logger.getLogger(MemberServiceImpl.class.getName());
    
    private final MemberRepository memberRepository;
    
    private final ApplicationEventPublisher eventPublisher;
    
    @Autowired
    public MemberServiceImpl(MemberRepository memberRepository, ApplicationEventPublisher eventPublisher) {
        this.memberRepository = memberRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Registers a new member.
     * This method replaces the register method from the original MemberRegistration class.
     * It persists the member to the database and publishes an event.
     * 
     * @param member The member to register
     * @throws Exception If registration fails
     */
    @Override
    @Transactional
    public void register(Member member) throws Exception {
        log.info("Registering " + member.getName());
        
        // Check if email already exists
        if (emailAlreadyExists(member.getEmail())) {
            throw new Exception("Email already exists: " + member.getEmail());
        }
        
        // Save the member
        memberRepository.save(member);
        
        // Publish an event (equivalent to the CDI event in the original app)
        eventPublisher.publishEvent(member);
    }

    /**
     * Finds a member by their ID.
     * 
     * @param id The ID of the member to find
     * @return An Optional containing the member if found, or empty if not found
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Member> findById(Long id) {
        return memberRepository.findById(id);
    }

    /**
     * Finds a member by their email address.
     * 
     * @param email The email address to search for
     * @return An Optional containing the member if found, or empty if not found
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Member> findByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    /**
     * Retrieves all members ordered by name.
     * 
     * @return A list of all members sorted by name in ascending order
     */
    @Override
    @Transactional(readOnly = true)
    public List<Member> findAllOrderedByName() {
        return memberRepository.findAllOrderedByName();
    }

    /**
     * Checks if a member with the given email already exists.
     * 
     * @param email The email to check
     * @return true if a member with the email exists, false otherwise
     */
    @Override
    @Transactional(readOnly = true)
    public boolean emailAlreadyExists(String email) {
        return memberRepository.findByEmail(email).isPresent();
    }
}
