package com.huellapositiva.integration;

import com.huellapositiva.domain.model.valueobjects.EmailConfirmation;
import com.huellapositiva.domain.service.EmailCommunicationService;
import com.huellapositiva.infrastructure.EmailService;
import com.huellapositiva.infrastructure.orm.service.IssueService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(MockitoExtension.class)
class EmailCommunicationServiceShould {

    @MockBean
    EmailService emailService;

    @MockBean
    IssueService issueService;

    @Autowired
    EmailCommunicationService communicationService;

    @Test
    void fail_on_registering_a_volunteer_should_save_a_email_and_stacktrace() {
        // GIVEN
        EmailConfirmation emailConfirmation = EmailConfirmation.from("foo@huellapositiva.com", "hello");
        // WHEN
        lenient().doThrow(new RuntimeException()).when(emailService).sendEmail(any());
        communicationService.sendRegistrationConfirmationEmail(emailConfirmation);
        // THEN
        verify(issueService).registerEmailConfirmationIssue(any(), any());
    }
}
