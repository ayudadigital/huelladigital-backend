package com.huellapositiva.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huellapositiva.application.dto.ChangePasswordDto;
import com.huellapositiva.application.dto.JwtResponseDto;
import com.huellapositiva.application.exception.UserNotFoundException;
import com.huellapositiva.domain.actions.UpdatePasswordAction;
import com.huellapositiva.domain.model.valueobjects.Roles;
import com.huellapositiva.infrastructure.orm.entities.JpaCredential;
import com.huellapositiva.infrastructure.orm.repository.JpaCredentialRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaVolunteerRepository;
import com.huellapositiva.util.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Stream;

import static com.huellapositiva.util.TestData.DEFAULT_EMAIL;
import static com.huellapositiva.util.TestData.DEFAULT_PASSWORD;
import static com.huellapositiva.util.TestUtils.loginAndGetJwtTokens;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(TestData.class)
class HandlerPasswordApiControllerShould {
    private static final String baseUri = "/api/v1/handling-password";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UpdatePasswordAction credentialsAction;

    @Autowired
    private JpaCredentialRepository jpaCredentialRepository;

    @Autowired
    private TestData testData;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JpaVolunteerRepository jpaVolunteerRepository;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void beforeEach() {
        testData.resetData();
    }

    @Test
    void return_204_when_sends_a_recovery_password_email() throws Exception{
        // GIVEN
        testData.createCredential(DEFAULT_EMAIL, DEFAULT_PASSWORD, UUID.randomUUID());

        // WHEN + THEN
        mvc.perform(post(baseUri + "/sendRecoveryPasswordEmail")
                .with(csrf())
                .param("email", DEFAULT_EMAIL)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void return_404_when_user_email_not_found() throws Exception {
        // WHEN + THEN
        mvc.perform(post(baseUri + "/sendRecoveryPasswordEmail")
                .with(csrf())
                .param("email", DEFAULT_EMAIL)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void return_204_when_changing_password_is_successful() throws Exception {
        testData.createCredential(DEFAULT_EMAIL, DEFAULT_PASSWORD, UUID.randomUUID());
        credentialsAction.executeGenerationRecoveryPasswordEmail(DEFAULT_EMAIL);
        JpaCredential jpaCredential = jpaCredentialRepository.findByEmail(DEFAULT_EMAIL).orElseThrow(UserNotFoundException::new);

        String hash = jpaCredential.getHashRecoveryPassword();

        // WHEN + THEN
        mvc.perform(post(baseUri + "/changePassword/" + hash)
                .with(csrf())
                .param("newPassword", "NEWPASSWORD")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void return_403_when_trying_to_change_password_and_time_has_expired() throws Exception {
        testData.createCredential(DEFAULT_EMAIL, DEFAULT_PASSWORD, UUID.randomUUID());
        credentialsAction.executeGenerationRecoveryPasswordEmail(DEFAULT_EMAIL);
        JpaCredential jpaCredential = jpaCredentialRepository.findByEmail(DEFAULT_EMAIL).orElseThrow(UserNotFoundException::new);

        LocalDateTime timeOfExpiration = jpaCredential.getCreatedRecoveryHashOn();

        jpaCredential.setCreatedRecoveryHashOn(timeOfExpiration.minusHours(2));
        jpaCredentialRepository.save(jpaCredential);

        String hash = jpaCredential.getHashRecoveryPassword();

        // WHEN + THEN
        mvc.perform(post(baseUri + "/changePassword/" + hash)
                .with(csrf())
                .param("newPassword", "NEWPASSWORD")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void return_204_when_changed_password_successfully() throws Exception {
        //GIVEN
        testData.createCredential(DEFAULT_EMAIL, UUID.randomUUID(), DEFAULT_PASSWORD, Roles.VOLUNTEER);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);
        ChangePasswordDto changePasswordDto = new ChangePasswordDto("N3wPassw0rd.,:+`%!@#$^'?(){}~_/-[]]", DEFAULT_PASSWORD);

        // WHEN + THEN
        mvc.perform(post(baseUri + "/editPassword/")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(changePasswordDto))
                .with(csrf())
                .param("email", DEFAULT_EMAIL)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isNoContent());

        String newPasswordInDB = jpaCredentialRepository.findByEmail(DEFAULT_EMAIL).get().getHashedPassword();
        assertThat(passwordEncoder.matches(changePasswordDto.getNewPassword(), newPasswordInDB)).isTrue();
    }

    private static Stream<Arguments> provideChangePasswordDtoWithWrongData() {
        return Stream.of(
                Arguments.of(new ChangePasswordDto("", "abcd")),
                Arguments.of(new ChangePasswordDto("NEWPASSWORD", "")),
                Arguments.of(new ChangePasswordDto(null, "12345678")),
                Arguments.of(new ChangePasswordDto("12345678", null)),
                Arguments.of(new ChangePasswordDto("abcd", "NEWPASSWORD")),
                Arguments.of(new ChangePasswordDto("MíÑèwp? âssw0rd", DEFAULT_PASSWORD))
        );
    }

    @ParameterizedTest
    @MethodSource("provideChangePasswordDtoWithWrongData")
    void return_400_when_ChangePasswordDto_is_wrong_data(ChangePasswordDto changePasswordDto) throws Exception {
        //GIVEN
        testData.createCredential(DEFAULT_EMAIL, UUID.randomUUID(), DEFAULT_PASSWORD, Roles.VOLUNTEER);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // WHEN + THEN
        mvc.perform(post(baseUri + "/editPassword/")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(changePasswordDto))
                .with(csrf())
                .param("email",DEFAULT_EMAIL)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    private static Stream<Arguments> provideChangePasswordDtoWithMismatchDataInDatabase() {
        return Stream.of(
                Arguments.of(new ChangePasswordDto("MYNEWPASSWORD", "12345678")),
                Arguments.of(new ChangePasswordDto(DEFAULT_PASSWORD, DEFAULT_PASSWORD))
        );
    }

    @ParameterizedTest
    @MethodSource("provideChangePasswordDtoWithMismatchDataInDatabase")
    void return_409_when_old_or_new_password_not_match_the_password_in_database(ChangePasswordDto changePasswordDto) throws Exception {
        //GIVEN
        testData.createCredential(DEFAULT_EMAIL, UUID.randomUUID(), DEFAULT_PASSWORD, Roles.VOLUNTEER);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // WHEN + THEN
        mvc.perform(post(baseUri + "/editPassword/")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(changePasswordDto))
                .with(csrf())
                .param("email",DEFAULT_EMAIL)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isConflict());
    }
}