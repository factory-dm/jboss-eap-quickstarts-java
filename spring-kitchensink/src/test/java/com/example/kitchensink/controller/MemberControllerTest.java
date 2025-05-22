package com.example.kitchensink.controller;

import com.example.kitchensink.exception.EmailAlreadyExistsException;
import com.example.kitchensink.model.Member;
import com.example.kitchensink.service.MemberService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MemberController.class)
public class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MemberService memberService;

    @Autowired
    private ObjectMapper objectMapper;

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
    public void getAllMembers_ShouldReturnMembers() throws Exception {
        Member anotherMember = new Member();
        anotherMember.setId(2L);
        anotherMember.setName("Jane Doe");
        anotherMember.setEmail("jane.doe@example.com");
        anotherMember.setPhoneNumber("0987654321");

        given(memberService.findAllOrderedByName()).willReturn(Arrays.asList(testMember, anotherMember));

        mockMvc.perform(get("/members")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("John Doe")))
                .andExpect(jsonPath("$[1].name", is("Jane Doe")));

        verify(memberService, times(1)).findAllOrderedByName();
    }

    @Test
    public void getAllMembers_WhenNoMembers_ShouldReturnEmptyArray() throws Exception {
        given(memberService.findAllOrderedByName()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/members")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(memberService, times(1)).findAllOrderedByName();
    }

    @Test
    public void getMember_WhenMemberExists_ShouldReturnMember() throws Exception {
        given(memberService.findById(1L)).willReturn(Optional.of(testMember));

        mockMvc.perform(get("/members/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.email", is("john.doe@example.com")))
                .andExpect(jsonPath("$.phoneNumber", is("1234567890")));

        verify(memberService, times(1)).findById(1L);
    }

    @Test
    public void getMember_WhenMemberDoesNotExist_ShouldReturnNotFound() throws Exception {
        given(memberService.findById(999L)).willReturn(Optional.empty());

        mockMvc.perform(get("/members/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(memberService, times(1)).findById(999L);
    }

    @Test
    public void createMember_WithValidData_ShouldCreateMember() throws Exception {
        given(memberService.emailAlreadyExists(anyString())).willReturn(false);
        doNothing().when(memberService).register(any(Member.class));

        mockMvc.perform(post("/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testMember)))
                .andExpect(status().isOk());

        verify(memberService, times(1)).emailAlreadyExists(testMember.getEmail());
        verify(memberService, times(1)).register(any(Member.class));
    }

    @Test
    public void createMember_WithExistingEmail_ShouldReturnConflict() throws Exception {
        given(memberService.emailAlreadyExists(testMember.getEmail())).willReturn(true);

        mockMvc.perform(post("/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testMember)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.email", is("Email taken")));

        verify(memberService, times(1)).emailAlreadyExists(testMember.getEmail());
        verify(memberService, never()).register(any(Member.class));
    }

    @Test
    public void createMember_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Create an invalid member (missing required fields)
        Member invalidMember = new Member();
        invalidMember.setName(""); // Invalid: name is empty
        invalidMember.setEmail("not-an-email"); // Invalid: not a valid email
        invalidMember.setPhoneNumber("123"); // Invalid: too short

        mockMvc.perform(post("/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidMember)))
                .andExpect(status().isBadRequest());

        verify(memberService, never()).register(any(Member.class));
    }

    @Test
    public void createMember_WhenServiceThrowsException_ShouldReturnBadRequest() throws Exception {
        given(memberService.emailAlreadyExists(anyString())).willReturn(false);
        doThrow(new RuntimeException("Service error")).when(memberService).register(any(Member.class));

        mockMvc.perform(post("/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testMember)))
                .andExpect(status().isBadRequest());

        verify(memberService, times(1)).emailAlreadyExists(testMember.getEmail());
        verify(memberService, times(1)).register(any(Member.class));
    }
}
