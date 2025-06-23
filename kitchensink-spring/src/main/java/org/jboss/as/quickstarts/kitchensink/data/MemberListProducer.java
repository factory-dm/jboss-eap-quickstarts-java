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
package org.jboss.as.quickstarts.kitchensink.data;

import java.util.List;

import org.jboss.as.quickstarts.kitchensink.model.Member;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.PostConstruct;

/**
 * This Spring component provides the list of members to the UI.
 * It observes member registration events and updates the list accordingly.
 * This replaces the CDI-based MemberListProducer from the original application.
 */
@Component
@Slf4j
public class MemberListProducer {

    @Autowired
    private MemberRepository memberRepository;

    @Getter
    private List<Member> members;

    /**
     * Initialize the list of members on component creation
     */
    @PostConstruct
    public void retrieveAllMembersOrderedByName() {
        log.info("Retrieving all members ordered by name");
        members = memberRepository.findAllOrderedByName();
    }

    /**
     * Event listener that is notified when a new member is registered
     * This replaces the CDI @Observes functionality
     * 
     * @param member The newly registered member
     */
    @EventListener
    public void onMemberRegistration(Member member) {
        log.info("Processing new member registration event for: {}", member.getName());
        retrieveAllMembersOrderedByName();
    }
}
