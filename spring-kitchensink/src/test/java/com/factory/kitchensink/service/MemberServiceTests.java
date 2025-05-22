package com.factory.kitchensink.service;

import com.factory.kitchensink.model.Member;
import com.factory.kitchensink.repository.MemberRepository;
import jakarta.persistence.EntityExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for the MemberService.
 * These tests verify that the service methods work as expected,
 * including transaction handling and event publishing.
 */
@ExtendWith(MockitoExtension.class)
class MemberServiceTests {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private MemberService memberService;

    @Captor
    private ArgumentCaptor<Member> memberCaptor;

    private Member testMember;

    @BeforeEach
    void setUp() {
        // Set up a test member for use in multiple tests
        testMember = new Member();
        testMember.setId(1L);
        testMember.setName("Test User");
        testMember.setEmail("test@example.com");
        testMember.setPhoneNumber("1234567890");
    }

    /**
     * Test registering a new member successfully.
     * Verifies that the member is saved and an event is published.
     */
    @Test
    void testRegisterMemberSuccess() {
        // Given
        Member newMember = new Member();
        newMember.setName("New User");
        newMember.setEmail("new@example.com");
        newMember.setPhoneNumber("1234567890");

        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(memberRepository.save(any(Member.class))).thenReturn(testMember);

        // When
        Member result = memberService.register(newMember);

        // Then
        assertThat(result).isEqualTo(testMember);
        
        // Verify repository interactions
        verify(memberRepository).findByEmail("new@example.com");
        verify(memberRepository).save(newMember);
        
        // Verify event publishing
        verify(eventPublisher).publishEvent(testMember);
    }

    /**
     * Test registering a member with an email that already exists.
     * Verifies that an EntityExistsException is thrown and the member is not saved.
     */
    @Test
    void testRegisterMemberEmailExists() {
        // Given
        Member newMember = new Member();
        newMember.setName("New User");
        newMember.setEmail("existing@example.com");
        newMember.setPhoneNumber("1234567890");

        when(memberRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(testMember));

        // When/Then
        assertThatThrownBy(() -> memberService.register(newMember))
            .isInstanceOf(EntityExistsException.class)
            .hasMessageContaining("Email already exists");
        
        // Verify repository interactions
        verify(memberRepository).findByEmail("existing@example.com");
        verify(memberRepository, never()).save(any(Member.class));
        
        // Verify no event was published
        verify(eventPublisher, never()).publishEvent(any());
    }

    /**
     * Test finding all members ordered by name.
     * Verifies that the repository method is called and the results are returned.
     */
    @Test
    void testFindAllOrderedByName() {
        // Given
        Member member1 = new Member();
        member1.setName("Alice");
        
        Member member2 = new Member();
        member2.setName("Bob");
        
        List<Member> expectedMembers = Arrays.asList(member1, member2);
        
        when(memberRepository.findAllOrderedByName()).thenReturn(expectedMembers);

        // When
        List<Member> result = memberService.findAllOrderedByName();

        // Then
        assertThat(result).isEqualTo(expectedMembers);
        verify(memberRepository).findAllOrderedByName();
    }

    /**
     * Test finding a member by ID when the member exists.
     * Verifies that the repository method is called and the member is returned.
     */
    @Test
    void testFindByIdExists() {
        // Given
        Long memberId = 1L;
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(testMember));

        // When
        Optional<Member> result = memberService.findById(memberId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testMember);
        verify(memberRepository).findById(memberId);
    }

    /**
     * Test finding a member by ID when the member does not exist.
     * Verifies that the repository method is called and an empty Optional is returned.
     */
    @Test
    void testFindByIdNotExists() {
        // Given
        Long memberId = 999L;
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        // When
        Optional<Member> result = memberService.findById(memberId);

        // Then
        assertThat(result).isEmpty();
        verify(memberRepository).findById(memberId);
    }

    /**
     * Test finding a member by email when the member exists.
     * Verifies that the repository method is called and the member is returned.
     */
    @Test
    void testFindByEmailExists() {
        // Given
        String email = "test@example.com";
        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(testMember));

        // When
        Optional<Member> result = memberService.findByEmail(email);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testMember);
        verify(memberRepository).findByEmail(email);
    }

    /**
     * Test finding a member by email when the member does not exist.
     * Verifies that the repository method is called and an empty Optional is returned.
     */
    @Test
    void testFindByEmailNotExists() {
        // Given
        String email = "nonexistent@example.com";
        when(memberRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When
        Optional<Member> result = memberService.findByEmail(email);

        // Then
        assertThat(result).isEmpty();
        verify(memberRepository).findByEmail(email);
    }

    /**
     * Test that multiple calls to the repository are handled correctly.
     * This verifies that the service correctly delegates to the repository.
     */
    @Test
    void testMultipleRepositoryCalls() {
        // Given
        String email = "test@example.com";
        Long id = 1L;
        
        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(testMember));
        when(memberRepository.findById(id)).thenReturn(Optional.of(testMember));

        // When
        memberService.findByEmail(email);
        memberService.findById(id);
        memberService.findByEmail(email);

        // Then
        verify(memberRepository, times(2)).findByEmail(email);
        verify(memberRepository, times(1)).findById(id);
    }
}
