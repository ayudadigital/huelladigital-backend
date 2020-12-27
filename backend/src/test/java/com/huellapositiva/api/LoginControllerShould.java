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
import org.springframework.test.web.servlet.MockMvc;

import static com.huellapositiva.domain.model.valueobjects.Roles.VOLUNTEER;
import static com.huellapositiva.util.TestData.DEFAULT_EMAIL;
import static com.huellapositiva.util.TestData.DEFAULT_PASSWORD;
import static com.huellapositiva.util.TestUtils.loginAndGetJwtTokens;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(TestData.class)
class LoginControllerShould {

    private static final String loginUri = "/api/v1/authentication/login";
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
    void validate_user_and_send_tokens_and_roles() throws Exception {
        //GIVEN
        testData.createVolunteer(DEFAULT_EMAIL, DEFAULT_PASSWORD);

        //WHEN
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        //THEN
        assertThat(jwtResponseDto.getAccessToken()).isNotNull();
        assertThat(jwtResponseDto.getRefreshToken()).isNotNull();
        assertThat(jwtResponseDto.getRoles()).containsExactly(VOLUNTEER);
    }

    @Test
    void invalid_user_should_return_401() throws Exception {
        //GIVEN
        testData.createVolunteer(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        AuthenticationRequestDto dto = new AuthenticationRequestDto(DEFAULT_EMAIL, "invalidPassword");
        String body = objectMapper.writeValueAsString(dto);

        //WHEN + THEN
        mvc.perform(post(loginUri)
                .content(body)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
