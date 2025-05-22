package com.example.kitchensink.repository;

import com.example.kitchensink.model.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the MemberRepository.
 * Uses Spring Boot's @DataJpaTest to set up an in-memory test database.
 */
@DataJpaTest
public class MemberRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MemberRepository memberRepository;

    private Member johnDoe;
    private Member janeDoe;
    private Member bobSmith;

    @BeforeEach
    public void setup() {
        // Create test members
        johnDoe = new Member();
        johnDoe.setName("John Doe");
        johnDoe.setEmail("john.doe@example.com");
        johnDoe.setPhoneNumber("1234567890");

        janeDoe = new Member();
        janeDoe.setName("Jane Doe");
        janeDoe.setEmail("jane.doe@example.com");
        janeDoe.setPhoneNumber("0987654321");

        bobSmith = new Member();
        bobSmith.setName("Bob Smith");
        bobSmith.setEmail("bob.smith@example.com");
        bobSmith.setPhoneNumber("5555555555");

        // Persist test members
        entityManager.persist(johnDoe);
        entityManager.persist(janeDoe);
        entityManager.persist(bobSmith);
        entityManager.flush();
    }

    @Test
    public void findByEmail_WhenMemberExists_ShouldReturnMember() {
        // When
        Optional<Member> found = memberRepository.findByEmail("john.doe@example.com");

        // Then
        assertTrue(found.isPresent());
        assertEquals("John Doe", found.get().getName());
        assertEquals("john.doe@example.com", found.get().getEmail());
        assertEquals("1234567890", found.get().getPhoneNumber());
    }

    @Test
    public void findByEmail_WhenMemberDoesNotExist_ShouldReturnEmpty() {
        // When
        Optional<Member> found = memberRepository.findByEmail("nonexistent@example.com");

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    public void findAllOrderedByName_ShouldReturnMembersInAlphabeticalOrder() {
        // When
        List<Member> members = memberRepository.findAllOrderedByName();

        // Then
        assertEquals(3, members.size());
        assertEquals("Bob Smith", members.get(0).getName()); // First alphabetically
        assertEquals("Jane Doe", members.get(1).getName());  // Second alphabetically
        assertEquals("John Doe", members.get(2).getName());  // Third alphabetically
    }

    @Test
    public void findById_WhenMemberExists_ShouldReturnMember() {
        // Given
        Long id = johnDoe.getId();

        // When
        Optional<Member> found = memberRepository.findById(id);

        // Then
        assertTrue(found.isPresent());
        assertEquals("John Doe", found.get().getName());
        assertEquals("john.doe@example.com", found.get().getEmail());
    }

    @Test
    public void findById_WhenMemberDoesNotExist_ShouldReturnEmpty() {
        // When
        Optional<Member> found = memberRepository.findById(999L);

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    public void save_ShouldPersistAndReturnMember() {
        // Given
        Member newMember = new Member();
        newMember.setName("Alice Johnson");
        newMember.setEmail("alice.johnson@example.com");
        newMember.setPhoneNumber("1112223333");

        // When
        Member saved = memberRepository.save(newMember);

        // Then
        assertNotNull(saved.getId());
        assertEquals("Alice Johnson", saved.getName());
        assertEquals("alice.johnson@example.com", saved.getEmail());
        assertEquals("1112223333", saved.getPhoneNumber());

        // Verify it was actually saved to the database
        Member found = entityManager.find(Member.class, saved.getId());
        assertNotNull(found);
        assertEquals("Alice Johnson", found.getName());
    }
}
