package com.factory.kitchensink.rest;

import com.factory.kitchensink.model.Member;
import com.factory.kitchensink.service.MemberService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for Member operations.
 * This class replaces the original JAX-RS MemberResourceRESTService from the JBoss EAP application.
 * It provides REST endpoints for member operations using Spring MVC.
 */
@RestController
@RequestMapping("/api/members")
@Slf4j
@RequiredArgsConstructor
public class MemberResource {

    private final MemberService memberService;

    /**
     * List all members ordered by name.
     *
     * @return a list of all members
     */
    @GetMapping
    public List<Member> listAllMembers() {
        return memberService.findAllOrderedByName();
    }

    /**
     * Get a member by ID.
     *
     * @param id the ID of the member to retrieve
     * @return the member with the specified ID
     * @throws EntityNotFoundException if no member is found with the specified ID
     */
    @GetMapping("/{id}")
    public Member getMemberById(@PathVariable("id") Long id) {
        return memberService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Member with id " + id + " not found"));
    }

    /**
     * Create a new member.
     * This endpoint validates the member using bean validation and handles unique email constraints.
     *
     * @param member the member to create
     * @return a response entity with the created member
     */
    @PostMapping
    public ResponseEntity<Member> createMember(@Valid @RequestBody Member member) {
        log.info("Creating member: {}", member.getName());
        Member createdMember = memberService.register(member);
        return new ResponseEntity<>(createdMember, HttpStatus.CREATED);
    }
}
