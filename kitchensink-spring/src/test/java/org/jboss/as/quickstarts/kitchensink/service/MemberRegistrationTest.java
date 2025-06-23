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
package org.jboss.as.quickstarts.kitchensink.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.jboss.as.quickstarts.kitchensink.data.MemberRepository;
import org.jboss.as.quickstarts.kitchensink.model.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Test class for the MemberRegistration service.
 * Uses Mockito to mock dependencies and verify interactions.
 */
@ExtendWith(MockitoExtension.class)
public class MemberRegistrationTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private MemberRegistration memberRegistration;

    private Member member;

    @BeforeEach
    public void setUp() {
        // Create a test member
        member = new Member();
        member.setId(1L);
        member.setName("Test User");
        member.setEmail("test.user@example.com");
        member.setPhoneNumber("1234567890");
    }

    @Test
    public void testRegister() throws Exception {
        // Setup
        when(memberRepository.save(any(Member.class))).thenReturn(member);

        // Execute
        memberRegistration.register(member);

        // Verify
        verify(memberRepository, times(1)).save(member);
        verify(eventPublisher, times(1)).publishEvent(member);
    }

    @Test
    public void testRegisterWithRepositoryException() {
        // Setup
        doThrow(new RuntimeException("Database error")).when(memberRepository).save(any(Member.class));

        // Execute and verify
        assertThrows(Exception.class, () -> {
            memberRegistration.register(member);
        });

        // Verify that event was not published
        verify(eventPublisher, times(0)).publishEvent(any());
    }

    @Test
    public void testRegisterWithEventPublisherException() {
        // Setup
        when(memberRepository.save(any(Member.class))).thenReturn(member);
        doThrow(new RuntimeException("Event publishing error")).when(eventPublisher).publishEvent(any());

        // Execute and verify
        assertThrows(Exception.class, () -> {
            memberRegistration.register(member);
        });

        // Verify that save was called
        verify(memberRepository, times(1)).save(member);
    }
}
