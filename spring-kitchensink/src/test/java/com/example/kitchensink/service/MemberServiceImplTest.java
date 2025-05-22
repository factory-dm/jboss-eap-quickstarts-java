package com.example.kitchensink.service;

import com.example.kitchensink.model.Member;
import com.example.kitchensink.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MemberServiceImplTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private MemberServiceImpl memberService;

    private Member testMember;

    @BeforeEach
    public void setup() {
        testMember = new Member();
        testMember.setId(1L);
        testMember.setName("John Doe");
        testMember.setEmail("john.doe@example.com");
        testMember.setPhoneNumber("1234567890");
    }

    @Test
    public void register_ShouldPersistMemberAndPublishEvent() throws Exception {
        // Given
        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(memberRepository.save(any(Member.class))).thenReturn(testMember);
        doNothing().when(eventPublisher).publishEvent(any(Member.class));

        // When
        memberService.register(testMember);

        // Then
        verify(memberRepository, times(1)).findByEmail(testMember.getEmail());
        verify(memberRepository, times(1)).save(testMember);
        verify(eventPublisher, times(1)).publishEvent(testMember);
    }

    @Test
    public void register_WithExistingEmail_ShouldThrowException() {
        // Given
        when(memberRepository.findByEmail(testMember.getEmail())).thenReturn(Optional.of(testMember));

        // When & Then
        Exception exception = assertThrows(Exception.class, () -> {
            memberService.register(testMember);
        });

        assertTrue(exception.getMessage().contains("Email already exists"));
        verify(memberRepository, times(1)).findByEmail(testMember.getEmail());
        verify(memberRepository, never()).save(any(Member.class));
        verify(eventPublisher, never()).publishEvent(any(Member.class));
    }

    @Test
    public void findById_WhenMemberExists_ShouldReturnMember() {
        // Given
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));

        // When
        Optional<Member> result = memberService.findById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testMember, result.get());
        verify(memberRepository, times(1)).findById(1L);
    }

    @Test
    public void findById_WhenMemberDoesNotExist_ShouldReturnEmpty() {
        // Given
        when(memberRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<Member> result = memberService.findById(999L);

        // Then
        assertFalse(result.isPresent());
        verify(memberRepository, times(1)).findById(999L);
    }

    @Test
    public void findByEmail_WhenMemberExists_ShouldReturnMember() {
        // Given
        String email = "john.doe@example.com";
        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(testMember));

        // When
        Optional<Member> result = memberService.findByEmail(email);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testMember, result.get());
        verify(memberRepository, times(1)).findByEmail(email);
    }

    @Test
    public void findByEmail_WhenMemberDoesNotExist_ShouldReturnEmpty() {
        // Given
        String email = "nonexistent@example.com";
        when(memberRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When
        Optional<Member> result = memberService.findByEmail(email);

        // Then
        assertFalse(result.isPresent());
        verify(memberRepository, times(1)).findByEmail(email);
    }

    @Test
    public void findAllOrderedByName_ShouldReturnAllMembersOrderedByName() {
        // Given
        Member anotherMember = new Member();
        anotherMember.setId(2L);
        anotherMember.setName("Jane Doe");
        anotherMember.setEmail("jane.doe@example.com");
        anotherMember.setPhoneNumber("0987654321");

        List<Member> expectedMembers = Arrays.asList(testMember, anotherMember);
        when(memberRepository.findAllOrderedByName()).thenReturn(expectedMembers);

        // When
        List<Member> result = memberService.findAllOrderedByName();

        // Then
        assertEquals(expectedMembers.size(), result.size());
        assertEquals(expectedMembers, result);
        verify(memberRepository, times(1)).findAllOrderedByName();
    }

    @Test
    public void emailAlreadyExists_WhenEmailExists_ShouldReturnTrue() {
        // Given
        String email = "john.doe@example.com";
        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(testMember));

        // When
        boolean result = memberService.emailAlreadyExists(email);

        // Then
        assertTrue(result);
        verify(memberRepository, times(1)).findByEmail(email);
    }

    @Test
    public void emailAlreadyExists_WhenEmailDoesNotExist_ShouldReturnFalse() {
        // Given
        String email = "nonexistent@example.com";
        when(memberRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When
        boolean result = memberService.emailAlreadyExists(email);

        // Then
        assertFalse(result);
        verify(memberRepository, times(1)).findByEmail(email);
    }
}
