package com.huellapositiva.integration;

import cloud.localstack.docker.LocalstackDockerExtension;
import cloud.localstack.docker.annotation.LocalstackDockerProperties;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.VerifyEmailIdentityRequest;
import com.huellapositiva.domain.model.valueobjects.EmailMessage;
import com.huellapositiva.infrastructure.AwsEmailService;
import com.huellapositiva.util.AwsEnvVariablesExtension;
import com.huellapositiva.util.DockerCondition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@ExtendWith(DockerCondition.class)
@ExtendWith(AwsEnvVariablesExtension.class)
@ExtendWith(LocalstackDockerExtension.class)
@LocalstackDockerProperties(services = { "ses" }, imageTag = "0.11.5")
@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"huellapositiva.feature.email.enabled=true"})
class AwsEmailServiceShould {

    private final static String emailAddress = "test@huellapositiva.com";

    @Autowired
    private AwsEmailService awsEmailService;

    @Autowired
    private AmazonSimpleEmailService sesClient;

    @Test
    void send_an_email() {
        VerifyEmailIdentityRequest request = new VerifyEmailIdentityRequest().withEmailAddress(emailAddress);
        sesClient.verifyEmailIdentity(request);
        EmailMessage emailMessage = EmailMessage.builder()
                .from(emailAddress)
                .to(emailAddress)
                .subject("test subject")
                .body("test body")
                .build();

        assertThatCode(() -> awsEmailService.sendEmail(emailMessage)).doesNotThrowAnyException();
    }
}
