package com.huellapositiva.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huellapositiva.application.dto.CredentialsVolunteerRequestDto;
import com.huellapositiva.application.dto.JwtResponseDto;
import com.huellapositiva.util.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.web.servlet.MockMvc;

import javax.servlet.http.Cookie;

import static com.huellapositiva.util.TestData.DEFAULT_EMAIL;
import static com.huellapositiva.util.TestData.DEFAULT_PASSWORD;
import static com.huellapositiva.util.TestUtils.getCsrfTokenFromCookieHeader;
import static com.huellapositiva.util.TestUtils.loginRequest;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
@AutoConfigureMockMvc
@Import(TestData.class)
public class SecurityConfigShould {

    private static final String REFRESH_URL = "/api/v1/refresh";
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
    void not_check_csrf_token_when_request_is_get() throws Exception {
        // WHEN + THEN
        mvc.perform(get("/actuator/health")
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void not_check_csrf_token_when_request_endpoint_is_whitelisted() throws Exception {
        // GIVEN
        testData.createVolunteer(DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // WHEN + THEN
        assertThat(loginRequest(mvc, new CredentialsVolunteerRequestDto(DEFAULT_EMAIL, DEFAULT_PASSWORD))
                .getStatus(), is(MockHttpServletResponse.SC_OK));
    }

    @Test
    void return_csrf_token_for_any_type_of_request() throws Exception {
        // GIVEN
        testData.createVolunteer(DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // WHEN
        MockHttpServletResponse getResponse = mvc.perform(get("/actuator/health")
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        MockHttpServletResponse postResponse = loginRequest(mvc, new CredentialsVolunteerRequestDto(DEFAULT_EMAIL, DEFAULT_PASSWORD));

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
        MockHttpServletResponse loginResponse = loginRequest(mvc, new CredentialsVolunteerRequestDto(DEFAULT_EMAIL, DEFAULT_PASSWORD));
        JwtResponseDto jwtResponseDto = objectMapper.readValue(loginResponse.getContentAsString(), JwtResponseDto.class);

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
                .header("X-XSRF-TOKEN", "invalid token")
                .cookie(new Cookie("XSRF-TOKEN", "invalidCookie"))
                .accept(APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void allow_access_when_csrf_token_is_valid() throws Exception {
        // GIVEN
        testData.createVolunteer(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        MockHttpServletResponse loginResponse = loginRequest(mvc, new CredentialsVolunteerRequestDto(DEFAULT_EMAIL, DEFAULT_PASSWORD));
        JwtResponseDto jwtResponseDto = objectMapper.readValue(loginResponse.getContentAsString(), JwtResponseDto.class);
        String xsrfTokenValue = getCsrfTokenFromCookieHeader(loginResponse.getHeader("Set-Cookie"));
        Cookie xsrfCookie = loginResponse.getCookie("XSRF-TOKEN");

        // WHEN + THEN
        mvc.perform(post(REFRESH_URL)
                .contentType(APPLICATION_JSON)
                .header("X-XSRF-TOKEN", xsrfTokenValue)
                .cookie(xsrfCookie)
                .content(jwtResponseDto.getRefreshToken())
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
