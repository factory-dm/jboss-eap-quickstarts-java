package com.factory.kitchensink.rest;

import com.factory.kitchensink.model.Member;
import com.factory.kitchensink.service.MemberService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for the MemberResource REST controller.
 * These tests verify that the REST endpoints work as expected,
 * including proper response handling and validation.
 */
@WebMvcTest(MemberResource.class)
class MemberResourceTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MemberService memberService;

    private Member testMember;
    private List<Member> testMembers;

    @BeforeEach
    void setUp() {
        // Set up test data
        testMember = new Member();
        testMember.setId(1L);
        testMember.setName("Test User");
        testMember.setEmail("test@example.com");
        testMember.setPhoneNumber("1234567890");

        Member member2 = new Member();
        member2.setId(2L);
        member2.setName("Another User");
        member2.setEmail("another@example.com");
        member2.setPhoneNumber("9876543210");

        testMembers = Arrays.asList(testMember, member2);
    }

    /**
     * Test getting all members.
     * Verifies that the endpoint returns a list of all members with a 200 OK status.
     */
    @Test
    void testListAllMembers() throws Exception {
        // Given
        when(memberService.findAllOrderedByName()).thenReturn(testMembers);

        // When/Then
        mockMvc.perform(get("/api/members"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Test User")))
                .andExpect(jsonPath("$[0].email", is("test@example.com")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Another User")));

        verify(memberService).findAllOrderedByName();
    }

    /**
     * Test getting a member by ID when the member exists.
     * Verifies that the endpoint returns the member with a 200 OK status.
     */
    @Test
    void testGetMemberByIdExists() throws Exception {
        // Given
        when(memberService.findById(1L)).thenReturn(Optional.of(testMember));

        // When/Then
        mockMvc.perform(get("/api/members/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Test User")))
                .andExpect(jsonPath("$.email", is("test@example.com")))
                .andExpect(jsonPath("$.phoneNumber", is("1234567890")));

        verify(memberService).findById(1L);
    }

    /**
     * Test getting a member by ID when the member does not exist.
     * Verifies that the endpoint returns a 404 Not Found status.
     */
    @Test
    void testGetMemberByIdNotFound() throws Exception {
        // Given
        when(memberService.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        mockMvc.perform(get("/api/members/999"))
                .andExpect(status().isNotFound());

        verify(memberService).findById(999L);
    }

    /**
     * Test creating a new member successfully.
     * Verifies that the endpoint returns the created member with a 201 Created status.
     */
    @Test
    void testCreateMemberSuccess() throws Exception {
        // Given
        Member newMember = new Member();
        newMember.setName("New User");
        newMember.setEmail("new@example.com");
        newMember.setPhoneNumber("5551234567");

        Member savedMember = new Member();
        savedMember.setId(3L);
        savedMember.setName("New User");
        savedMember.setEmail("new@example.com");
        savedMember.setPhoneNumber("5551234567");

        when(memberService.register(any(Member.class))).thenReturn(savedMember);

        // When/Then
        mockMvc.perform(post("/api/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newMember)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.name", is("New User")))
                .andExpect(jsonPath("$.email", is("new@example.com")))
                .andExpect(jsonPath("$.phoneNumber", is("5551234567")));

        verify(memberService).register(any(Member.class));
    }

    /**
     * Test creating a member with invalid data.
     * Verifies that the endpoint returns a 400 Bad Request status with validation errors.
     */
    @Test
    void testCreateMemberInvalidData() throws Exception {
        // Given
        Member invalidMember = new Member();
        // Missing required fields

        // When/Then
        mockMvc.perform(post("/api/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidMember)))
                .andExpect(status().isBadRequest());

        verify(memberService, never()).register(any(Member.class));
    }

    /**
     * Test creating a member with an email that already exists.
     * Verifies that the endpoint returns a 409 Conflict status.
     */
    @Test
    void testCreateMemberEmailExists() throws Exception {
        // Given
        Member newMember = new Member();
        newMember.setName("Duplicate Email");
        newMember.setEmail("existing@example.com");
        newMember.setPhoneNumber("5551234567");

        doThrow(new EntityExistsException("Email already exists"))
                .when(memberService).register(any(Member.class));

        // When/Then
        mockMvc.perform(post("/api/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newMember)))
                .andExpect(status().isConflict());

        verify(memberService).register(any(Member.class));
    }

    /**
     * Test handling of unexpected exceptions.
     * Verifies that the endpoint returns a 500 Internal Server Error status.
     */
    @Test
    void testHandleUnexpectedException() throws Exception {
        // Given
        when(memberService.findById(anyLong())).thenThrow(new RuntimeException("Unexpected error"));

        // When/Then
        mockMvc.perform(get("/api/members/1"))
                .andExpect(status().isInternalServerError());

        verify(memberService).findById(1L);
    }

    /**
     * Test validation of member fields.
     * Verifies that the endpoint returns a 400 Bad Request status with specific validation errors.
     */
    @Test
    void testValidationErrors() throws Exception {
        // Given
        Member invalidMember = new Member();
        invalidMember.setName("123Invalid"); // Contains numbers, violates pattern
        invalidMember.setEmail("not-an-email"); // Invalid email format
        invalidMember.setPhoneNumber("123"); // Too short

        // When/Then
        mockMvc.perform(post("/api/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidMember)))
                .andExpect(status().isBadRequest());

        verify(memberService, never()).register(any(Member.class));
    }
}
