package com.huellapositiva.unit;

import com.huellapositiva.application.exception.NoVolunteerSubscribedException;
import com.huellapositiva.domain.actions.ManageNewsletterExcelAction;
import com.huellapositiva.domain.model.valueobjects.EmailAddress;
import com.huellapositiva.domain.service.EmailCommunicationService;
import com.huellapositiva.domain.service.RemoteStorageService;
import com.huellapositiva.infrastructure.orm.repository.JpaVolunteerRepository;
import com.huellapositiva.util.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import java.io.IOException;

import static com.huellapositiva.util.TestData.DEFAULT_EMAIL;
import static com.huellapositiva.util.TestData.DEFAULT_PASSWORD;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestData.class)
class ManageNewsletterExcelActionShould {

    @Autowired
    private TestData testData;

    @Autowired
    private JpaVolunteerRepository jpaVolunteerRepository;

    @MockBean
    private EmailCommunicationService emailCommunicationService;

    @MockBean
    private RemoteStorageService remoteStorageService;

    @BeforeEach
    void beforeEach() {
        testData.resetData();
    }

    @Test
    void send_an_empty_newsletter_email_when_there_are_no_subscribed_volunteers() throws IOException {
        // GIVEN
        ManageNewsletterExcelAction manageNewsletterExcelAction = new ManageNewsletterExcelAction(
                jpaVolunteerRepository, remoteStorageService, emailCommunicationService);

        // WHEN + THEN
        assertThrows(NoVolunteerSubscribedException.class, () -> manageNewsletterExcelAction.execute(DEFAULT_EMAIL));
    }

    @Test
    void send_a_newsletter_email_when_there_are_subscribed_volunteers() throws IOException {
        testData.createSubscribedVolunteer(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        // GIVEN
        ManageNewsletterExcelAction manageNewsletterExcelAction = new ManageNewsletterExcelAction(
                jpaVolunteerRepository, remoteStorageService, emailCommunicationService);

        // WHEN
        manageNewsletterExcelAction.execute(DEFAULT_EMAIL);

        // THEN
        verify(emailCommunicationService).sendNewsletterSubscriptorsEmail(EmailAddress.from(DEFAULT_EMAIL),null);
    }
}