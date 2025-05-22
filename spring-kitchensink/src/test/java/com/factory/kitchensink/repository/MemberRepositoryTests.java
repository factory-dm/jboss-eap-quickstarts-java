package com.factory.kitchensink.repository;

import com.factory.kitchensink.model.Member;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the MemberRepository.
 * These tests verify that the repository methods work as expected.
 * The @DataJpaTest annotation sets up an in-memory database for testing JPA repositories.
 */
@DataJpaTest
class MemberRepositoryTests {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MemberRepository memberRepository;

    /**
     * Test finding a member by email.
     */
    @Test
    void testFindByEmail() {
        // Given
        Member member = new Member();
        member.setName("Test User");
        member.setEmail("test@example.com");
        member.setPhoneNumber("1234567890");
        entityManager.persist(member);
        entityManager.flush();

        // When
        Optional<Member> found = memberRepository.findByEmail("test@example.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test User");
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
    }

    /**
     * Test finding a member by email when the member doesn't exist.
     */
    @Test
    void testFindByEmailNotFound() {
        // When
        Optional<Member> found = memberRepository.findByEmail("nonexistent@example.com");

        // Then
        assertThat(found).isEmpty();
    }

    /**
     * Test finding all members ordered by name.
     */
    @Test
    void testFindAllOrderedByName() {
        // Given
        Member member1 = new Member();
        member1.setName("Charlie");
        member1.setEmail("charlie@example.com");
        member1.setPhoneNumber("1234567890");

        Member member2 = new Member();
        member2.setName("Alice");
        member2.setEmail("alice@example.com");
        member2.setPhoneNumber("1234567890");

        Member member3 = new Member();
        member3.setName("Bob");
        member3.setEmail("bob@example.com");
        member3.setPhoneNumber("1234567890");

        entityManager.persist(member1);
        entityManager.persist(member2);
        entityManager.persist(member3);
        entityManager.flush();

        // When
        List<Member> members = memberRepository.findAllOrderedByName();

        // Then
        assertThat(members).hasSize(3);
        assertThat(members.get(0).getName()).isEqualTo("Alice");
        assertThat(members.get(1).getName()).isEqualTo("Bob");
        assertThat(members.get(2).getName()).isEqualTo("Charlie");
    }

    /**
     * Test saving a member.
     */
    @Test
    void testSaveMember() {
        // Given
        Member member = new Member();
        member.setName("New User");
        member.setEmail("new@example.com");
        member.setPhoneNumber("1234567890");

        // When
        Member saved = memberRepository.save(member);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("New User");
        assertThat(saved.getEmail()).isEqualTo("new@example.com");
    }

    /**
     * Test deleting a member.
     */
    @Test
    void testDeleteMember() {
        // Given
        Member member = new Member();
        member.setName("Delete User");
        member.setEmail("delete@example.com");
        member.setPhoneNumber("1234567890");
        Member saved = entityManager.persist(member);
        entityManager.flush();

        // When
        memberRepository.deleteById(saved.getId());
        Optional<Member> found = memberRepository.findById(saved.getId());

        // Then
        assertThat(found).isEmpty();
    }

    /**
     * Test that the data.sql script is loaded correctly.
     * This test verifies that the initial data from data.sql is loaded into the database.
     */
    @Test
    @Sql("/data.sql")
    void testInitialDataLoaded() {
        // When
        Optional<Member> found = memberRepository.findByEmail("john.smith@mailinator.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("John Smith");
        assertThat(found.get().getPhoneNumber()).isEqualTo("2125551212");
    }
}
