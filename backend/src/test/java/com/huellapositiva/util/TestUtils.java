package com.huellapositiva.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huellapositiva.application.dto.CredentialsVolunteerRequestDto;
import com.huellapositiva.application.dto.JwtResponseDto;
import com.huellapositiva.application.dto.ProposalRequestDto;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public final class TestUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static MockHttpServletResponse loginRequest(MockMvc mvc, CredentialsVolunteerRequestDto loginDto) throws Exception {
        String jsonBody = objectMapper.writeValueAsString(loginDto);
        return mvc.perform(post("/api/v1/volunteers/login")
                .content(jsonBody)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
    }

    public static JwtResponseDto loginAndGetJwtTokens(MockMvc mvc, String email, String password) throws Exception {
        MockHttpServletResponse loginResponse = loginRequest(mvc, new CredentialsVolunteerRequestDto(email, password));
        return objectMapper.readValue(loginResponse.getContentAsString(), JwtResponseDto.class);
    }

    public static void registerProposalRequest(MockMvc mvc, String accessToken, ProposalRequestDto proposalDto) throws Exception {
        mvc.perform(post("/api/v1/proposals")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .content(objectMapper.writeValueAsString(proposalDto))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isCreated());
    }
}
