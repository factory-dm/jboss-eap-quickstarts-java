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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;

import org.jboss.as.quickstarts.kitchensink.data.MemberRepository;
import org.jboss.as.quickstarts.kitchensink.model.Member;
import org.jboss.as.quickstarts.kitchensink.service.MemberRegistration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Test class for the MemberResourceRESTController.
 * Uses Spring's MockMvc to test the REST endpoints without starting a full HTTP server.
 */
@WebMvcTest(MemberResourceRESTController.class)
@ActiveProfiles("test")
public class MemberResourceRESTControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MemberRepository memberRepository;

    @MockBean
    private MemberRegistration memberRegistration;

    @MockBean
    private Validator validator;

    @Mock
    private ConstraintViolation<Member> violation;

    private Member member1;
    private Member member2;

    @BeforeEach
    public void setUp() {
        // Create test members
        member1 = new Member();
        member1.setId(1L);
        member1.setName("John Doe");
        member1.setEmail("john.doe@example.com");
        member1.setPhoneNumber("1234567890");

        member2 = new Member();
        member2.setId(2L);
        member2.setName("Jane Doe");
        member2.setEmail("jane.doe@example.com");
        member2.setPhoneNumber("0987654321");
    }

    @Test
    public void testListAllMembers() throws Exception {
        // Setup
        List<Member> members = Arrays.asList(member1, member2);
        when(memberRepository.findAllOrderedByName()).thenReturn(members);

        // Execute and verify
        mockMvc.perform(get("/members")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("John Doe")))
                .andExpect(jsonPath("$[0].email", is("john.doe@example.com")))
                .andExpect(jsonPath("$[1].name", is("Jane Doe")))
                .andExpect(jsonPath("$[1].email", is("jane.doe@example.com")));
    }

    @Test
    public void testLookupMemberById() throws Exception {
        // Setup
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member1));

        // Execute and verify
        mockMvc.perform(get("/members/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.email", is("john.doe@example.com")))
                .andExpect(jsonPath("$.phoneNumber", is("1234567890")));
    }

    @Test
    public void testLookupMemberByIdNotFound() throws Exception {
        // Setup
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        // Execute and verify
        mockMvc.perform(get("/members/99")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testCreateMember() throws Exception {
        // Setup
        Member newMember = new Member();
        newMember.setName("New Member");
        newMember.setEmail("new.member@example.com");
        newMember.setPhoneNumber("5551234567");

        when(validator.validate(any(Member.class))).thenReturn(new HashSet<>());
        when(memberRepository.findByEmail(anyString())).thenReturn(null);

        // Execute and verify
        mockMvc.perform(post("/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newMember)))
                .andExpect(status().isOk());
    }

    @Test
    public void testCreateMemberWithValidationErrors() throws Exception {
        // Setup
        Member invalidMember = new Member();
        invalidMember.setName("123"); // Invalid name with numbers
        invalidMember.setEmail("invalid-email"); // Invalid email
        invalidMember.setPhoneNumber("123"); // Too short phone number

        Set<ConstraintViolation<Member>> violations = new HashSet<>();
        violations.add(violation);
        
        when(validator.validate(any(Member.class))).thenThrow(new ConstraintViolationException(violations));

        // Execute and verify
        mockMvc.perform(post("/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidMember)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateMemberWithDuplicateEmail() throws Exception {
        // Setup
        Member duplicateMember = new Member();
        duplicateMember.setName("Duplicate");
        duplicateMember.setEmail("john.doe@example.com"); // Already exists
        duplicateMember.setPhoneNumber("5551234567");

        when(validator.validate(any(Member.class))).thenReturn(new HashSet<>());
        when(memberRepository.findByEmail(anyString())).thenReturn(member1);

        // Execute and verify
        mockMvc.perform(post("/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateMember)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.email", is("Email taken")));
    }

    @Test
    public void testCreateMemberWithRegistrationError() throws Exception {
        // Setup
        Member newMember = new Member();
        newMember.setName("Error Member");
        newMember.setEmail("error.member@example.com");
        newMember.setPhoneNumber("5551234567");

        when(validator.validate(any(Member.class))).thenReturn(new HashSet<>());
        when(memberRepository.findByEmail(anyString())).thenReturn(null);
        doThrow(new RuntimeException("Registration failed")).when(memberRegistration).register(any(Member.class));

        // Execute and verify
        mockMvc.perform(post("/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newMember)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Registration failed")));
    }
}
