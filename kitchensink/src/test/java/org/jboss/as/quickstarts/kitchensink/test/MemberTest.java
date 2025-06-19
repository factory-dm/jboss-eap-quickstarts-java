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
package org.jboss.as.quickstarts.kitchensink.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.jboss.as.quickstarts.kitchensink.model.Member;
import org.junit.Before;
import org.junit.Test;

public class MemberTest {

    private Validator validator;

    @Before
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testGettersAndSetters() {
        // Create a new Member
        Member member = new Member();
        
        // Test id
        Long id = 1L;
        member.setId(id);
        assertEquals(id, member.getId());
        
        // Test name
        String name = "John Doe";
        member.setName(name);
        assertEquals(name, member.getName());
        
        // Test email
        String email = "john@example.com";
        member.setEmail(email);
        assertEquals(email, member.getEmail());
        
        // Test phoneNumber
        String phoneNumber = "1234567890";
        member.setPhoneNumber(phoneNumber);
        assertEquals(phoneNumber, member.getPhoneNumber());
    }

    @Test
    public void testValidMember() {
        // Create a valid Member
        Member member = new Member();
        member.setName("John Doe");
        member.setEmail("john@example.com");
        member.setPhoneNumber("1234567890");
        
        // Validate the member
        Set<ConstraintViolation<Member>> violations = validator.validate(member);
        
        // Should be no violations
        assertTrue(violations.isEmpty());
    }

    @Test
    public void testNameValidation() {
        Member member = new Member();
        member.setEmail("john@example.com");
        member.setPhoneNumber("1234567890");
        
        // Test null name
        member.setName(null);
        Set<ConstraintViolation<Member>> violations = validator.validate(member);
        assertEquals(1, violations.size());
        
        // Test empty name
        member.setName("");
        violations = validator.validate(member);
        assertEquals(1, violations.size());
        
        // Test name with numbers (should fail pattern validation)
        member.setName("John123");
        violations = validator.validate(member);
        assertEquals(1, violations.size());
        
        // Test name too long (> 25 chars)
        member.setName("ThisNameIsDefinitelyTooLongForValidation");
        violations = validator.validate(member);
        assertEquals(1, violations.size());
    }

    @Test
    public void testEmailValidation() {
        Member member = new Member();
        member.setName("John Doe");
        member.setPhoneNumber("1234567890");
        
        // Test null email
        member.setEmail(null);
        Set<ConstraintViolation<Member>> violations = validator.validate(member);
        assertEquals(1, violations.size());
        
        // Test empty email
        member.setEmail("");
        violations = validator.validate(member);
        assertEquals(1, violations.size());
        
        // Test invalid email format
        member.setEmail("not-an-email");
        violations = validator.validate(member);
        assertEquals(1, violations.size());
    }

    @Test
    public void testPhoneNumberValidation() {
        Member member = new Member();
        member.setName("John Doe");
        member.setEmail("john@example.com");
        
        // Test null phone
        member.setPhoneNumber(null);
        Set<ConstraintViolation<Member>> violations = validator.validate(member);
        assertEquals(1, violations.size());
        
        // Test phone too short (< 10 digits)
        member.setPhoneNumber("123456789");
        violations = validator.validate(member);
        assertEquals(1, violations.size());
        
        // Test phone too long (> 12 digits)
        member.setPhoneNumber("1234567890123");
        violations = validator.validate(member);
        assertEquals(1, violations.size());
        
        // Test non-numeric phone
        member.setPhoneNumber("123456789a");
        violations = validator.validate(member);
        assertEquals(1, violations.size());
    }

    @Test
    public void testMultipleValidationErrors() {
        // Create an invalid Member with multiple validation errors
        Member member = new Member();
        member.setName("John123"); // Invalid pattern
        member.setEmail("not-an-email"); // Invalid email
        member.setPhoneNumber("123"); // Too short
        
        // Validate the member
        Set<ConstraintViolation<Member>> violations = validator.validate(member);
        
        // Should have 3 violations
        assertEquals(3, violations.size());
    }
}
