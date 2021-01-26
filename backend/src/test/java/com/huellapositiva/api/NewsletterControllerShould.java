package com.huellapositiva.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huellapositiva.application.dto.JwtResponseDto;
import com.huellapositiva.domain.model.valueobjects.Roles;
import com.huellapositiva.infrastructure.orm.entities.JpaVolunteer;
import com.huellapositiva.infrastructure.orm.repository.JpaVolunteerRepository;
import com.huellapositiva.util.TestData;
import com.huellapositiva.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static com.huellapositiva.util.TestData.*;
import static com.huellapositiva.util.TestUtils.loginAndGetJwtTokens;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(TestData.class)
class NewsletterControllerShould {
    private static final String NEWSLETTER_URL = "/api/v1/newsletter";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private TestData testData;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JpaVolunteerRepository jpaVolunteerRepository;

    @BeforeEach
    void beforeEach() {
        testData.resetData();
    }

    @Test
    void return_200_when_state_of_subscribed_field_changed_successfully() throws Exception {
        testData.createVolunteerWithProfile(DEFAULT_EMAIL, DEFAULT_PASSWORD);

        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        mvc.perform(post(NEWSLETTER_URL + "/changeStatusNewsletterSubscription")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(Boolean.TRUE))
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        JpaVolunteer volunteer = jpaVolunteerRepository.findByEmail(DEFAULT_EMAIL).orElseThrow();
        assertThat(volunteer.getProfile().isSubscribed()).isTrue();

        mvc.perform(post(NEWSLETTER_URL + "/changeStatusNewsletterSubscription")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(Boolean.FALSE))
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        volunteer = jpaVolunteerRepository.findByEmail(DEFAULT_EMAIL).orElseThrow();
        assertThat(volunteer.getProfile().isSubscribed()).isFalse();
    }

    @Test
    void return_204_when_excel_sent_successfully() throws Exception {
        JpaVolunteer volunteer = testData.createSubscribedVolunteer(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        testData.createVolunteer(DEFAULT_EMAIL_2, DEFAULT_PASSWORD);
        JpaVolunteer volunteer3 = testData.createSubscribedVolunteer("foo_3@huellapositiva.com", DEFAULT_PASSWORD);

        testData.createCredential("revisor@huellapositiva.com", UUID.randomUUID(), DEFAULT_PASSWORD, Roles.REVISER);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, "revisor@huellapositiva.com", DEFAULT_PASSWORD);

        MockHttpServletResponse fetchResponse = mvc.perform(get(NEWSLETTER_URL + "/getNewsletterExcel")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andReturn().getResponse();
    }

    @Test
    void return_404_when_not_found_any_volunteers_subscribed_to_newsletter() throws Exception {
        testData.createCredential("revisor@huellapositiva.com", UUID.randomUUID(), DEFAULT_PASSWORD, Roles.REVISER);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, "revisor@huellapositiva.com", DEFAULT_PASSWORD);

        MockHttpServletResponse fetchResponse = mvc.perform(get(NEWSLETTER_URL + "/getNewsletterExcel")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
    }
}
