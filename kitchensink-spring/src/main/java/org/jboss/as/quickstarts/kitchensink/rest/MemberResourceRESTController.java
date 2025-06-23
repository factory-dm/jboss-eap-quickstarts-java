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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.persistence.NoResultException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;

import org.jboss.as.quickstarts.kitchensink.data.MemberRepository;
import org.jboss.as.quickstarts.kitchensink.model.Member;
import org.jboss.as.quickstarts.kitchensink.service.MemberRegistration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import lombok.extern.slf4j.Slf4j;

/**
 * Spring REST Controller
 * 
 * This class produces a RESTful service to read/write the contents of the members table.
 * It replaces the original JAX-RS based MemberResourceRESTService.
 */
@RestController
@RequestMapping("/members")
@Slf4j
public class MemberResourceRESTController {

    @Autowired
    private Validator validator;

    @Autowired
    private MemberRepository repository;

    @Autowired
    private MemberRegistration registration;

    /**
     * List all members
     * 
     * @return List of all members
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Member> listAllMembers() {
        return repository.findAllOrderedByName();
    }

    /**
     * Lookup a member by id
     * 
     * @param id The id of the member to find
     * @return The member if found
     * @throws ResponseStatusException if member not found
     */
    @GetMapping(value = "/{id:[0-9][0-9]*}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Member lookupMemberById(@PathVariable("id") long id) {
        Member member = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member with id " + id + " not found"));
        return member;
    }

    /**
     * Creates a new member from the values provided.
     * Performs validation, and will return a response with either 200 ok,
     * or with a map of fields, and related errors.
     * 
     * @param member The member to create
     * @return Response indicating success or failure
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createMember(@RequestBody Member member) {
        try {
            // Validates member using bean validation
            validateMember(member);
            
            // Register the member
            registration.register(member);
            
            // Return success response
            return ResponseEntity.ok().build();
            
        } catch (ConstraintViolationException ce) {
            // Handle bean validation issues
            return createViolationResponse(ce.getConstraintViolations());
            
        } catch (ValidationException e) {
            // Handle the unique constraint violation
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("email", "Email taken");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(responseObj);
            
        } catch (Exception e) {
            // Handle generic exceptions
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(responseObj);
        }
    }

    /**
     * Validates the given Member variable and throws validation exceptions based on the type of error.
     * If the error is standard bean validation errors then it will throw a ConstraintValidationException
     * with the set of the constraints violated.
     *
     * If the error is caused because an existing member with the same email is registered it throws a
     * regular validation exception so that it can be interpreted separately.
     *
     * @param member Member to be validated
     * @throws ConstraintViolationException If Bean Validation errors exist
     * @throws ValidationException If member with the same email already exists
     */
    private void validateMember(Member member) throws ConstraintViolationException, ValidationException {
        // Create a bean validator and check for issues.
        Set<ConstraintViolation<Member>> violations = validator.validate(member);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(new HashSet<>(violations));
        }

        // Check the uniqueness of the email address
        if (emailAlreadyExists(member.getEmail())) {
            throw new ValidationException("Unique Email Violation");
        }
    }

    /**
     * Creates a response including a map of all violation fields, and their message.
     * This can then be used by clients to show violations.
     *
     * @param violations A set of violations that needs to be reported
     * @return ResponseEntity containing all violations
     */
    private ResponseEntity<Map<String, String>> createViolationResponse(Set<ConstraintViolation<?>> violations) {
        log.debug("Validation completed. violations found: {}", violations.size());

        Map<String, String> responseObj = new HashMap<>();

        for (ConstraintViolation<?> violation : violations) {
            responseObj.put(violation.getPropertyPath().toString(), violation.getMessage());
        }

        return ResponseEntity.badRequest().body(responseObj);
    }

    /**
     * Checks if a member with the same email address is already registered.
     * This is the only way to easily capture the "@UniqueConstraint(columnNames = "email")"
     * constraint from the Member class.
     *
     * @param email The email to check
     * @return True if the email already exists, and false otherwise
     */
    public boolean emailAlreadyExists(String email) {
        Member member = null;
        try {
            member = repository.findByEmail(email);
        } catch (NoResultException e) {
            // ignore
        }
        return member != null;
    }
}
