package com.huellapositiva.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huellapositiva.application.dto.RegisterVolunteerRequestDto;
import com.huellapositiva.domain.actions.RegisterVolunteerAction;
import com.huellapositiva.domain.exception.EmailException;
import com.huellapositiva.infrastructure.orm.repository.JpaFailEmailConfirmationRepository;
import com.huellapositiva.infrastructure.orm.service.IssueService;
import com.huellapositiva.util.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(TestData.class)
class IssueServiceShould {

    @SpyBean
    RegisterVolunteerAction registerVolunteerAction;

    @Autowired
    IssueService issueService;

    @Autowired
    JpaFailEmailConfirmationRepository failEmailConfirmationRepository;

    @Autowired
    TestData testData;

    private static final String baseUri = "/api/v1/volunteers";

    @Autowired
    private MockMvc mvc;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void beforeEach() {
        testData.resetData();
    }


    @Test
    void verify_save_a_email() {
        //GIVEN
        RegisterVolunteerRequestDto dto = new RegisterVolunteerRequestDto("foo@huellapositiva.com", "plain-password");

        //WHEN
        try {
            throw new EmailException();
        } catch (EmailException ex) {
            issueService.registerVolunteerIssue(dto.getEmail(), ex);
        }

        //THEN
        assertThat(Objects.requireNonNull(failEmailConfirmationRepository.findByEmail(dto.getEmail()).orElse(null)).getEmailAddress(), is(dto.getEmail()));
    }

    @Test
    void fail_on_registering_a_volunteer_should_save_a_email_and_stacktrace() throws Exception {
        //GIVEN
        RegisterVolunteerRequestDto dto = new RegisterVolunteerRequestDto("foo@huellapositiva.com", "plain-password");

        //WHEN
        doThrow(new EmailException()).when(registerVolunteerAction).execute(dto);
        String body = objectMapper.writeValueAsString(dto);
        mvc.perform(post(baseUri)
                .content(body)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        //THEN
        assertThat(Objects.requireNonNull(failEmailConfirmationRepository.findByEmail(dto.getEmail()).orElse(null)).getEmailAddress(), is(dto.getEmail()));
    }
}