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
package org.jboss.as.quickstarts.kitchensink.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for the Member entity.
 * Tests the validation constraints defined on the Member class.
 */
public class MemberTest {

    private Validator validator;
    private Member member;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        
        // Create a valid member for testing
        member = new Member();
        member.setId(1L);
        member.setName("John Doe");
        member.setEmail("john.doe@example.com");
        member.setPhoneNumber("1234567890");
    }

    @Test
    public void testValidMember() {
        Set<ConstraintViolation<Member>> violations = validator.validate(member);
        assertTrue(violations.isEmpty(), "Valid member should not have constraint violations");
    }

    @Test
    public void testNameNull() {
        member.setName(null);
        Set<ConstraintViolation<Member>> violations = validator.validate(member);
        
        assertEquals(1, violations.size(), "Should have 1 violation");
        assertEquals("must not be null", violations.iterator().next().getMessage());
    }

    @Test
    public void testNameTooShort() {
        member.setName("");
        Set<ConstraintViolation<Member>> violations = validator.validate(member);
        
        assertEquals(1, violations.size(), "Should have 1 violation");
        assertTrue(violations.iterator().next().getMessage().contains("size must be between 1 and 25"));
    }

    @Test
    public void testNameTooLong() {
        member.setName("This name is way too long to be valid according to our constraints");
        Set<ConstraintViolation<Member>> violations = validator.validate(member);
        
        assertEquals(1, violations.size(), "Should have 1 violation");
        assertTrue(violations.iterator().next().getMessage().contains("size must be between 1 and 25"));
    }

    @Test
    public void testNameWithNumbers() {
        member.setName("John123");
        Set<ConstraintViolation<Member>> violations = validator.validate(member);
        
        assertEquals(1, violations.size(), "Should have 1 violation");
        assertEquals("Must not contain numbers", violations.iterator().next().getMessage());
    }

    @Test
    public void testEmailNull() {
        member.setEmail(null);
        Set<ConstraintViolation<Member>> violations = validator.validate(member);
        
        assertEquals(1, violations.size(), "Should have 1 violation");
        assertEquals("must not be null", violations.iterator().next().getMessage());
    }

    @Test
    public void testEmailEmpty() {
        member.setEmail("");
        Set<ConstraintViolation<Member>> violations = validator.validate(member);
        
        assertEquals(1, violations.size(), "Should have 1 violation");
        assertTrue(violations.iterator().next().getMessage().contains("must not be empty"));
    }

    @Test
    public void testEmailInvalid() {
        member.setEmail("invalid-email");
        Set<ConstraintViolation<Member>> violations = validator.validate(member);
        
        assertEquals(1, violations.size(), "Should have 1 violation");
        assertTrue(violations.iterator().next().getMessage().contains("must be a well-formed email address"));
    }

    @Test
    public void testPhoneNumberNull() {
        member.setPhoneNumber(null);
        Set<ConstraintViolation<Member>> violations = validator.validate(member);
        
        assertEquals(1, violations.size(), "Should have 1 violation");
        assertEquals("must not be null", violations.iterator().next().getMessage());
    }

    @Test
    public void testPhoneNumberTooShort() {
        member.setPhoneNumber("123456789");
        Set<ConstraintViolation<Member>> violations = validator.validate(member);
        
        assertEquals(1, violations.size(), "Should have 1 violation");
        assertTrue(violations.iterator().next().getMessage().contains("size must be between 10 and 12"));
    }

    @Test
    public void testPhoneNumberTooLong() {
        member.setPhoneNumber("1234567890123");
        Set<ConstraintViolation<Member>> violations = validator.validate(member);
        
        assertEquals(1, violations.size(), "Should have 1 violation");
        assertTrue(violations.iterator().next().getMessage().contains("size must be between 10 and 12"));
    }

    @Test
    public void testPhoneNumberWithLetters() {
        member.setPhoneNumber("12345abcde");
        Set<ConstraintViolation<Member>> violations = validator.validate(member);
        
        assertEquals(1, violations.size(), "Should have 1 violation");
        assertTrue(violations.iterator().next().getMessage().contains("numeric value out of bounds"));
    }
}
