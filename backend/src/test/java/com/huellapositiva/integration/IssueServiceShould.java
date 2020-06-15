package com.huellapositiva.integration;

import com.huellapositiva.application.dto.CredentialsVolunteerRequestDto;
import com.huellapositiva.infrastructure.orm.repository.JpaFailEmailConfirmationRepository;
import com.huellapositiva.infrastructure.orm.service.IssueService;
import com.huellapositiva.util.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestData.class)
class IssueServiceShould {

    @Autowired
    IssueService issueService;

    @Autowired
    JpaFailEmailConfirmationRepository failEmailConfirmationRepository;

    @Autowired
    TestData testData;

    @BeforeEach
    void beforeEach() {
        testData.resetData();
    }

    @Test
    void verify_save_a_email() {
        //GIVEN
        CredentialsVolunteerRequestDto dto = new CredentialsVolunteerRequestDto("foo@huellapositiva.com", "plain-password");

        //WHEN
        issueService.registerVolunteerIssue(dto.getEmail(), new RuntimeException());

        //THEN
        assertThat(failEmailConfirmationRepository.findByEmail(dto.getEmail()).get().getEmailAddress(), is(dto.getEmail()));
    }
}