package com.huellapositiva.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huellapositiva.application.dto.CredentialsVolunteerRequestDto;
import com.huellapositiva.application.dto.JwtResponseDto;
import com.huellapositiva.domain.Roles;
import com.huellapositiva.infrastructure.orm.model.Credential;
import com.huellapositiva.infrastructure.orm.model.Role;
import com.huellapositiva.infrastructure.security.JwtService;
import com.huellapositiva.util.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.stream.Collectors;

import static com.huellapositiva.domain.Roles.VOLUNTEER;
import static com.huellapositiva.domain.Roles.VOLUNTEER_NOT_CONFIRMED;
import static com.huellapositiva.util.TestData.DEFAULT_EMAIL;
import static com.huellapositiva.util.TestData.DEFAULT_PASSWORD;
import static com.huellapositiva.util.TestUtils.loginRequest;
import static com.huellapositiva.util.TestUtils.refreshRequest;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(TestData.class)
class JwtControllerShould {

    private static final String testJwtUri = "/api/v1/test-jwt-authorization";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private TestData testData;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JwtService jwtService;

    @BeforeEach
    void beforeEach() {
        testData.resetData();
    }

    @Test
    void generate_new_access_token() {
        //GIVEN
        JwtResponseDto response = createVolunteerWithRoleAndGetAccessToken(VOLUNTEER_NOT_CONFIRMED);
        String accessToken = response.getAccessToken();
        String refreshToken = response.getRefreshToken();

        //WHEN
        await().atMost(2, SECONDS).pollDelay(100, MILLISECONDS).untilAsserted(() -> {
            String jsonResponse = mvc.perform(post("/api/v1/refresh")
                    .contentType(APPLICATION_JSON)
                    .content(refreshToken)
                    .accept(APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            JwtResponseDto refreshResponse = objectMapper.readValue(jsonResponse, JwtResponseDto.class);
            String newAccessToken = refreshResponse.getAccessToken();
            String newRefreshToken = refreshResponse.getRefreshToken();

            //THEN
            assertThat(newAccessToken).isNotNull();
            assertThat(newRefreshToken).isNotNull();
            assertAll(
                    () -> assertThat(newAccessToken).isNotEqualTo(accessToken),
                    () -> assertThat(newRefreshToken).isNotEqualTo(refreshToken)
            );
        });

    }

    @Test
    void fail_to_generate_new_access_token_if_refresh_token_is_malformed() throws Exception {
        //WHEN + THEN
        mvc.perform(post("/api/v1/refresh")
                .contentType(APPLICATION_JSON)
                .content("malformed JWT string")
                .accept(APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void grant_access_when_token_contains_valid_role() throws Exception {
        //GIVEN
        String accessToken = createVolunteerWithRoleAndGetAccessToken(VOLUNTEER).getAccessToken();

        //WHEN
        mvc.perform(get(testJwtUri)
                .header(AUTHORIZATION, "Bearer " + accessToken)
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void deny_access_when_token_contains_invalid_role() throws Exception {
        //GIVEN
        String accessToken = createVolunteerWithRoleAndGetAccessToken(VOLUNTEER_NOT_CONFIRMED).getAccessToken();

        //WHEN + THEN
        mvc.perform(get(testJwtUri)
                .header(AUTHORIZATION, "Bearer " + accessToken)
                .accept(APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void deny_access_when_do_not_provide_any_authorization() throws Exception {
        //WHEN + THEN
        mvc.perform(get(testJwtUri)
                .accept(APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void return_401_when_token_has_expired() {
        //GIVEN
        String accessToken = createVolunteerWithRoleAndGetAccessToken(VOLUNTEER_NOT_CONFIRMED).getAccessToken();

        //WHEN + THEN
        await().atMost(2, SECONDS).pollDelay(100, MILLISECONDS).until(() -> {
            int responseStatus = mvc.perform(get(testJwtUri)
                    .header(AUTHORIZATION, "Bearer " + accessToken)
                    .accept(APPLICATION_JSON))
                    .andReturn().getResponse().getStatus();
            return responseStatus == HttpStatus.UNAUTHORIZED.value();
        });
    }

    @Test
    void support_multiple_sessions() throws Exception {
        testData.createVolunteer(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        CredentialsVolunteerRequestDto loginDto = new CredentialsVolunteerRequestDto(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        String jsonBody = objectMapper.writeValueAsString(loginDto);
        String regexToken = "^[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.?[A-Za-z0-9-_.+/=]*$";

        //GIVEN
        JwtResponseDto sessionOneJwtDto = objectMapper.readValue(
                loginRequest(mvc, jsonBody).getContentAsString(), JwtResponseDto.class);
        JwtResponseDto sessionTwoJwtDto = objectMapper.readValue(
                loginRequest(mvc, jsonBody).getContentAsString(), JwtResponseDto.class);

        //WHEN
        Thread.sleep(1000);
        String sessionOneNewAccessToken = objectMapper.readValue(
                refreshRequest(mvc, sessionOneJwtDto.getRefreshToken()).getContentAsString(), JwtResponseDto.class)
                .getAccessToken();
        String sessionTwoNewAccessToken = objectMapper.readValue(
                refreshRequest(mvc, sessionTwoJwtDto.getRefreshToken()).getContentAsString(), JwtResponseDto.class)
                .getAccessToken();

        //THEN
        assertAll(
                () -> assertTrue(sessionOneNewAccessToken.matches(regexToken)),
                () -> assertTrue(sessionTwoNewAccessToken.matches(regexToken)),
                () -> assertNotEquals(sessionOneNewAccessToken, sessionOneJwtDto.getAccessToken()),
                () -> assertNotEquals(sessionTwoNewAccessToken, sessionTwoJwtDto.getAccessToken())
        );
    }

    private JwtResponseDto createVolunteerWithRoleAndGetAccessToken(Roles role) {
        Credential credentials = testData.createCredential(DEFAULT_EMAIL, role);
        List<String> roles = credentials.getRoles().stream().map(Role::getName).collect(Collectors.toList());
        return jwtService.create(DEFAULT_EMAIL, roles);
    }
}
