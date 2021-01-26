package com.huellapositiva.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huellapositiva.application.dto.AuthenticationRequestDto;
import com.huellapositiva.application.dto.JwtResponseDto;
import com.huellapositiva.application.exception.InvalidJwtTokenException;
import com.huellapositiva.domain.model.valueobjects.Roles;
import com.huellapositiva.infrastructure.orm.entities.JpaCredential;
import com.huellapositiva.infrastructure.orm.entities.Role;
import com.huellapositiva.infrastructure.security.JwtService;
import com.huellapositiva.util.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.stream.Collectors;

import static com.huellapositiva.domain.model.valueobjects.Roles.VOLUNTEER;
import static com.huellapositiva.domain.model.valueobjects.Roles.VOLUNTEER_NOT_CONFIRMED;
import static com.huellapositiva.util.TestData.DEFAULT_EMAIL;
import static com.huellapositiva.util.TestData.DEFAULT_PASSWORD;
import static com.huellapositiva.util.TestUtils.loginAndGetJwtTokens;
import static com.huellapositiva.util.TestUtils.loginRequest;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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

    private JwtResponseDto createVolunteerWithRoleAndGetAccessToken(Roles role) {
        JpaCredential credentials = testData.createCredential(DEFAULT_EMAIL, role);
        List<String> roles = credentials.getRoles().stream().map(Role::getName).collect(Collectors.toList());
        return jwtService.create(DEFAULT_EMAIL, roles);
    }

    @Test
    void generate_new_access_token() throws Exception {
        //GIVEN
        testData.createVolunteer(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);
        String accessToken = jwtResponseDto.getAccessToken();
        String refreshToken = jwtResponseDto.getRefreshToken();

        //WHEN
        await().atMost(2, SECONDS).pollDelay(100, MILLISECONDS).untilAsserted(() -> {
            String jsonResponse = mvc.perform(post("/api/v1/authentication/refresh")
                    .with(csrf())
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
            assertThat(jwtResponseDto.getRoles()).containsExactly(VOLUNTEER);
            assertAll(
                    () -> assertThat(newAccessToken).isNotEqualTo(accessToken),
                    () -> assertThat(newRefreshToken).isNotEqualTo(refreshToken)
            );
        });
    }

    @Test
    void fail_to_generate_new_access_token_if_refresh_token_is_malformed() throws Exception {
        //WHEN + THEN
        mvc.perform(post("/api/v1/authentication/refresh")
                .with(csrf())
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
                .with(csrf())
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
                .with(csrf())
                .accept(APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void deny_access_when_do_not_provide_any_authorization() throws Exception {
        //WHEN + THEN
        mvc.perform(get(testJwtUri)
                .with(csrf())
                .accept(APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void return_401_when_token_has_expired() {
        //GIVEN
        String accessToken = createVolunteerWithRoleAndGetAccessToken(VOLUNTEER_NOT_CONFIRMED).getAccessToken();

        //WHEN + THEN
        await().atMost(3, SECONDS).pollDelay(100, MILLISECONDS).untilAsserted(() ->
                mvc.perform(get(testJwtUri)
                        .header(AUTHORIZATION, "Bearer " + accessToken)
                        .accept(APPLICATION_JSON))
                        .andExpect(status().isUnauthorized())
        );
    }

    @Test
    void support_multiple_sessions() throws Exception {
        //GIVEN
        testData.createVolunteer(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        JwtResponseDto sessionOneJwtDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        //WHEN
        // Login with second device
        loginRequest(mvc, new AuthenticationRequestDto(DEFAULT_EMAIL, DEFAULT_PASSWORD));
        //THEN
        // Access token from first login has been revoked due to the second login
        await().atMost(2, SECONDS).untilAsserted(() ->
                assertThrows(InvalidJwtTokenException.class, () -> jwtService.getUserDetails(sessionOneJwtDto.getAccessToken()))
        );
        // Refresh token from first login can still get access tokens issued
        String refreshResponse = mvc.perform(post("/api/v1/authentication/refresh")
                .with(csrf())
                .content(sessionOneJwtDto.getRefreshToken())
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JwtResponseDto jwtResponseDto = objectMapper.readValue(refreshResponse, JwtResponseDto.class);
        assertThat(jwtResponseDto.getAccessToken()).isNotNull();
        assertThat(jwtResponseDto.getRoles()).containsExactly(VOLUNTEER);
    }

    @Test
    void return_400_when_body_is_empty_in_refresh_request() throws Exception {
        //WHEN + THEN
        mvc.perform(post("/api/v1/authentication/refresh")
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
