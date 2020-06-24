package com.huellapositiva.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huellapositiva.application.dto.CredentialsVolunteerRequestDto;
import com.huellapositiva.application.dto.JwtResponseDto;
import com.huellapositiva.domain.Roles;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.Collections;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public final class TestUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static UserDetails withMockUser(Roles role) {
        return new User("foo@huellapositiva.com", "pass", Collections.singleton(new SimpleGrantedAuthority("ROLE_" + role)));
    }

    public static UserDetails withMockUser(String username, Roles role) {
        return new User(username, "pass", Collections.singleton(new SimpleGrantedAuthority("ROLE_" + role)));
    }

    public static void authenticatedPostRequest(MockMvc mvc, String path, Roles role, String body, ResultMatcher expectedResult, ResultMatcher expectedHeader) throws Exception {
        mvc.perform(post(path)
                .with(user(withMockUser(role)))
                .content(body)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(expectedResult)
                .andExpect(expectedHeader);
    }

    public static void anonymousPostRequest(MockMvc mvc, String path, String body, ResultMatcher expectedResult, ResultMatcher expectedHeader) throws Exception {
        mvc.perform(post(path)
                .content(body)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(expectedResult)
                .andExpect(expectedHeader);
    }

    public static JwtResponseDto loginRequest(MockMvc mvc, CredentialsVolunteerRequestDto loginDto) throws Exception {
        String jsonBody = objectMapper.writeValueAsString(loginDto);
        String jsonResponse = mvc.perform(post("/api/v1/volunteers/login")
                .content(jsonBody)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(jsonResponse, JwtResponseDto.class);
    }

    public static JwtResponseDto refreshRequest(MockMvc mvc, String refreshToken) throws Exception {
        String jsonResponse =  mvc.perform(post("/api/v1/refresh")
                .content(refreshToken)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(jsonResponse, JwtResponseDto.class);
    }
}
