package com.huellapositiva.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huellapositiva.application.dto.CredentialsVolunteerRequestDto;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.http.MediaType.APPLICATION_JSON;
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
}
