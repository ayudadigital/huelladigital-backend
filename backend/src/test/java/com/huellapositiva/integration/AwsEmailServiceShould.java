package com.huellapositiva.integration;

import cloud.localstack.docker.annotation.LocalstackDockerProperties;
import com.huellapositiva.domain.Email;
import com.huellapositiva.infrastructure.AwsEmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@LocalstackDockerProperties(services = { "ses" })
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, properties = {"huellapositiva.feature.email.enabled=true"})
class AwsEmailServiceShould {

    @Autowired
    private AwsEmailService awsEmailService;

    @Test
    void send_an_email() {
        // GIVEN
        Email email = Email.builder()
                .from("noreply@huellapositiva.com")
                .to("noreply-user@huellapositiva.com")
                .subject("test-subject")
                .body("test-body").build();

        // WHEN
        boolean result = awsEmailService.sendEmail(email);

        // THEN
        assertThat(result, is(true));
    }
}
