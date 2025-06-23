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

import org.jboss.as.quickstarts.kitchensink.data.MemberRepository;
import org.jboss.as.quickstarts.kitchensink.model.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

/**
 * Member registration service
 * 
 * This class replaces the original EJB Stateless bean with a Spring Service.
 * The @Transactional annotation provides transaction management (replacing @Stateless),
 * and Spring's ApplicationEventPublisher replaces CDI's Event handling.
 */
@Service
@Slf4j
public class MemberRegistration {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    /**
     * Register a new member
     * 
     * @param member The member to register
     * @throws Exception if registration fails
     */
    @Transactional
    public void register(Member member) throws Exception {
        log.info("Registering {}", member.getName());
        
        // Save the member using Spring Data JPA repository
        memberRepository.save(member);
        
        // Publish an event that a new member has been registered
        eventPublisher.publishEvent(member);
    }
}
