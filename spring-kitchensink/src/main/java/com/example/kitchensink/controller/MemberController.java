package com.example.kitchensink.controller;

import com.example.kitchensink.exception.EmailAlreadyExistsException;
import com.example.kitchensink.model.Member;
import com.example.kitchensink.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.logging.Logger;

/**
 * REST controller for managing Member entities.
 * This controller replaces the MemberResourceRESTService from the original JBoss kitchensink application.
 * It provides endpoints for retrieving, creating, and managing members.
 */
@RestController
@RequestMapping("/members")
public class MemberController {

    private final Logger log = Logger.getLogger(MemberController.class.getName());
    
    private final MemberService memberService;
    
    @Autowired
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }
    
    /**
     * GET /members : get all members ordered by name.
     * Equivalent to the listAllMembers method in the original MemberResourceRESTService.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of members in the body
     */
    @GetMapping
    public ResponseEntity<List<Member>> getAllMembers() {
        log.info("REST request to get all Members");
        List<Member> members = memberService.findAllOrderedByName();
        return ResponseEntity.ok(members);
    }
    
    /**
     * GET /members/{id} : get the member with the specified id.
     * Equivalent to the lookupMemberById method in the original MemberResourceRESTService.
     *
     * @param id the id of the member to retrieve
     * @return the ResponseEntity with status 200 (OK) and the member in the body,
     *         or with status 404 (Not Found) if the member is not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Member> getMember(@PathVariable Long id) {
        log.info("REST request to get Member with id: " + id);
        return memberService.findById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found with id: " + id));
    }
    
    /**
     * POST /members : create a new member.
     * Equivalent to the createMember method in the original MemberResourceRESTService.
     *
     * @param member the member to create
     * @return the ResponseEntity with status 200 (OK),
     *         or with status 400 (Bad Request) if the member has validation errors,
     *         or with status 409 (Conflict) if the email is already in use
     */
    @PostMapping
    public ResponseEntity<Void> createMember(@Valid @RequestBody Member member) {
        log.info("REST request to create Member: " + member.getName());
        
        // Check if email already exists
        if (memberService.emailAlreadyExists(member.getEmail())) {
            throw new EmailAlreadyExistsException(member.getEmail());
        }
        
        try {
            memberService.register(member);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.severe("Error registering member: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
