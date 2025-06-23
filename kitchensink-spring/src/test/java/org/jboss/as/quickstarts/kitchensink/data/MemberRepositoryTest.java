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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.jboss.as.quickstarts.kitchensink.model.Member;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test class for the MemberRepository.
 * Uses Spring Boot's @DataJpaTest which configures an in-memory database,
 * sets up JPA repositories, and enables transaction management.
 */
@DataJpaTest
@ActiveProfiles("test")
public class MemberRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    public void testFindById() {
        // given
        Member member = new Member();
        member.setName("John Doe");
        member.setEmail("john.doe@example.com");
        member.setPhoneNumber("1234567890");
        entityManager.persist(member);
        entityManager.flush();

        // when
        Member found = memberRepository.findById(member.getId()).orElse(null);

        // then
        assertNotNull(found);
        assertEquals(member.getName(), found.getName());
        assertEquals(member.getEmail(), found.getEmail());
        assertEquals(member.getPhoneNumber(), found.getPhoneNumber());
    }

    @Test
    public void testFindByEmail() {
        // given
        Member member = new Member();
        member.setName("Jane Doe");
        member.setEmail("jane.doe@example.com");
        member.setPhoneNumber("9876543210");
        entityManager.persist(member);
        entityManager.flush();

        // when
        Member found = memberRepository.findByEmail("jane.doe@example.com");

        // then
        assertNotNull(found);
        assertEquals(member.getName(), found.getName());
        assertEquals(member.getEmail(), found.getEmail());
        assertEquals(member.getPhoneNumber(), found.getPhoneNumber());
    }

    @Test
    public void testFindAllOrderedByName() {
        // given
        Member member1 = new Member();
        member1.setName("Charlie");
        member1.setEmail("charlie@example.com");
        member1.setPhoneNumber("1234567890");
        entityManager.persist(member1);

        Member member2 = new Member();
        member2.setName("Alice");
        member2.setEmail("alice@example.com");
        member2.setPhoneNumber("1234567890");
        entityManager.persist(member2);

        Member member3 = new Member();
        member3.setName("Bob");
        member3.setEmail("bob@example.com");
        member3.setPhoneNumber("1234567890");
        entityManager.persist(member3);

        entityManager.flush();

        // when
        List<Member> members = memberRepository.findAllOrderedByName();

        // then
        assertNotNull(members);
        assertEquals(3, members.size());
        
        // Verify ordering - should be Alice, Bob, Charlie
        assertEquals("Alice", members.get(0).getName());
        assertEquals("Bob", members.get(1).getName());
        assertEquals("Charlie", members.get(2).getName());
    }

    @Test
    public void testSave() {
        // given
        Member member = new Member();
        member.setName("New Member");
        member.setEmail("new.member@example.com");
        member.setPhoneNumber("5551234567");

        // when
        Member saved = memberRepository.save(member);
        
        // then
        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals(member.getName(), saved.getName());
        assertEquals(member.getEmail(), saved.getEmail());
        assertEquals(member.getPhoneNumber(), saved.getPhoneNumber());
        
        // Verify it was actually saved to the database
        Member found = entityManager.find(Member.class, saved.getId());
        assertNotNull(found);
        assertEquals(saved.getId(), found.getId());
    }

    @Test
    public void testDelete() {
        // given
        Member member = new Member();
        member.setName("To Delete");
        member.setEmail("to.delete@example.com");
        member.setPhoneNumber("1231231234");
        entityManager.persist(member);
        entityManager.flush();
        
        Long id = member.getId();
        
        // when
        memberRepository.delete(member);
        
        // then
        Member found = entityManager.find(Member.class, id);
        assertThat(found).isNull();
    }
}
