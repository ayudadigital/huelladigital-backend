package com.huellapositiva.infrastructure;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.*;
import com.huellapositiva.domain.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(name = "huellapositiva.feature.email.enabled", havingValue = "true")
public class AwsEmailService implements EmailService{

    @Autowired
    private AmazonSimpleEmailService awsSesClient;

    @Override
    public boolean sendEmail(Email email) {
        try {
            SendEmailRequest request = new SendEmailRequest()
                    .withDestination(
                            new Destination().withToAddresses(email.getTo()))
                    .withMessage(new Message()
                            .withBody(new Body()
                                    .withHtml(new Content()
                                            .withCharset("UTF-8").withData(email.getBody())))
                            .withSubject(new Content()
                                    .withCharset("UTF-8").withData(email.getSubject())))
                    .withSource(email.getFrom());
            awsSesClient.sendEmail(request);
        } catch(Exception ex) {
            log.error("Email from {} with subject {} could not sent.", email.getFrom(), email.getSubject(), ex);
            return false;
        }

        return true;
    }
}
