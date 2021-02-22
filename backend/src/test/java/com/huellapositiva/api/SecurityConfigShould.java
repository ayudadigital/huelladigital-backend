package com.huellapositiva.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huellapositiva.application.dto.AuthenticationRequestDto;
import com.huellapositiva.application.dto.JwtResponseDto;
import com.huellapositiva.util.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

import javax.servlet.http.Cookie;

import static com.huellapositiva.domain.model.valueobjects.ProposalStatus.PUBLISHED;
import static com.huellapositiva.util.TestData.*;
import static com.huellapositiva.util.TestUtils.loginAndGetJwtTokens;
import static com.huellapositiva.util.TestUtils.loginRequest;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(printOnlyOnFailure = false)
@Import(TestData.class)
class SecurityConfigShould {

    private static final String REFRESH_URL = "/api/v1/authentication/refresh";
    private static final String HEALTH_URL = "/actuator/health";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private TestData testData;

    @Autowired
    private MockMvc mvc;

    @BeforeEach
    void beforeEach() {
        testData.resetData();
    }

    @Test
    void do_not_check_csrf_token_when_request_is_get() throws Exception {
        // WHEN + THEN
        mvc.perform(get(HEALTH_URL)
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void do_not_check_csrf_token_when_request_endpoint_is_allowlisted() throws Exception {
        // GIVEN
        testData.createVolunteer(DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // WHEN + THEN
        MockHttpServletResponse httpServletResponse = loginRequest(mvc, new AuthenticationRequestDto(DEFAULT_EMAIL, DEFAULT_PASSWORD));

        assertNotNull(httpServletResponse.getCookie("XSRF-TOKEN"));
    }

    @Test
    void return_csrf_token_for_any_type_of_request() throws Exception {
        // GIVEN
        testData.createVolunteer(DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // WHEN
        MockHttpServletResponse getResponse = mvc.perform(get(HEALTH_URL)
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        MockHttpServletResponse postResponse = loginRequest(mvc, new AuthenticationRequestDto(DEFAULT_EMAIL, DEFAULT_PASSWORD));

        // THEN
        assertAll(
                () -> assertNotNull(getResponse.getCookie("XSRF-TOKEN")),
                () -> assertNotNull(postResponse.getCookie("XSRF-TOKEN"))
        );
    }

    @Test
    void deny_access_when_no_csrf_token_was_provided() throws Exception {
        // GIVEN
        testData.createVolunteer(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // WHEN + THEN
        mvc.perform(post(REFRESH_URL)
                .contentType(APPLICATION_JSON)
                .content(jwtResponseDto.getRefreshToken())
                .accept(APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void deny_access_when_csrf_token_is_invalid() throws Exception {
        // WHEN + THEN
        mvc.perform(post(REFRESH_URL)
                .contentType(APPLICATION_JSON)
                .header("X-XSRF-TOKEN", "IllicitValue")
                .cookie(new Cookie("XSRF-TOKEN", "TokenValue"))
                .accept(APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void allow_access_when_csrf_token_is_valid() throws Exception {
        // GIVEN
        testData.createVolunteer(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        MockHttpServletResponse loginResponse = loginRequest(mvc, new AuthenticationRequestDto(DEFAULT_EMAIL, DEFAULT_PASSWORD));
        JwtResponseDto jwtResponseDto = objectMapper.readValue(loginResponse.getContentAsString(), JwtResponseDto.class);
        Cookie xsrfCookie = loginResponse.getCookie("XSRF-TOKEN");

        // WHEN + THEN
        mvc.perform(post(REFRESH_URL)
                .contentType(APPLICATION_JSON)
                .header("X-XSRF-TOKEN", xsrfCookie.getValue())
                .cookie(xsrfCookie)
                .content(jwtResponseDto.getRefreshToken())
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void allow_access_only_to_allowed_roles() throws Exception {
        // GIVEN
        testData.createESALJpaContactPerson(VALID_NAME, VALID_SURNAME, VALID_PHONE, DEFAULT_EMAIL, DEFAULT_PASSWORD);
        MockHttpServletResponse loginResponse = loginRequest(mvc, new AuthenticationRequestDto(DEFAULT_EMAIL, DEFAULT_PASSWORD));
        JwtResponseDto jwtResponseDto = objectMapper.readValue(loginResponse.getContentAsString(), JwtResponseDto.class);
        String proposalId = testData.registerESALAndProposal(PUBLISHED).getId();

        // WHEN + THEN
        mvc.perform(post("/api/v1/proposals/" + proposalId + "/join")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .contentType(APPLICATION_JSON)
                .with(csrf())
                .accept(APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
